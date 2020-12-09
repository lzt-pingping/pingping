package com.pingping.search.service.Impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.pingping.common.enums.ExceptionEnum;
import com.pingping.common.exceptions.LyException;
import com.pingping.common.utils.BeanHelper;
import com.pingping.common.utils.JsonUtils;
import com.pingping.common.vo.PageResult;
import com.pingping.item.client.ItemClient;
import com.pingping.item.dto.SkuDTO;
import com.pingping.item.dto.SpecParamDTO;
import com.pingping.item.dto.SpuDTO;
import com.pingping.item.dto.SpuDetailDTO;
import com.pingping.search.dto.GoodsDTO;
import com.pingping.search.dto.SearchRequest;
import com.pingping.search.pojo.Goods;
import com.pingping.search.service.SearchService;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {
    @Autowired
    private ItemClient itemClient;

    @Autowired
    private ElasticsearchTemplate esTemplate;
    /**
     * 通过该方法将spu对象转成goods对象
     *
     */
    @Override
    public Goods buildGoods(SpuDTO spu){

        //1.关键字查询的字符串  品牌名称 + 一级/二级/三级
        String keyStr = spu.getName() + spu.getBrandName() + spu.getCategoryName();

        //2.setSkus问题,根据spu的id获取sku列表
        List<SkuDTO> skuDTOList = itemClient.querySkuBySpuId(spu.getId());
        List<Map> skuList = new ArrayList<>();
        for (SkuDTO skuDTO : skuDTOList) {
            Map skuMap = new HashMap<>();
            skuMap.put("id", skuDTO.getId());
            skuMap.put("title", skuDTO.getTitle());
            if(StringUtils.isNotEmpty(skuDTO.getImages())){
                if(skuDTO.getImages().indexOf(",") != -1){  //如果有多张图片，取第一张图片
                    String[] images = skuDTO.getImages().split(",");
                    skuMap.put("image",images[0]);
                }else{
                    skuMap.put("image", skuDTO.getImages());  //如果图片只有1个直接赋值
                }
            }
            skuMap.put("price", skuDTO.getPrice());
            skuList.add(skuMap);
        }

        //3.skus的价格收成Set<Long> 的集合
        Set<Long> priceSet = skuDTOList.stream().map(SkuDTO::getPrice).collect(Collectors.toSet());

        //4.先获取spu的三级分类中的所有用于搜索的sepcParams
        //4.1获取spu对应的spuDeital对象
        SpuDetailDTO detailDTO = itemClient.querySpuDetailById(spu.getId());
        //4.2取出商品详情的通用规格&自定义规格
        String genericSpec = detailDTO.getGenericSpec();  //通用规格
        Map<Long, Object> genericSpecMap = JsonUtils.nativeRead(genericSpec, new TypeReference<Map<Long, Object>>() {
        });
        String specialSpec = detailDTO.getSpecialSpec();  //自定义规格

        Map<Long, Object> specialSpecMap = JsonUtils.nativeRead(specialSpec, new TypeReference<Map<Long, Object>>() {
        });


        Map<String, Object> specMap = new HashMap<>();
        List<SpecParamDTO> paramDTOS = itemClient.querySpecParams(null, spu.getCid3(), true);
        for (SpecParamDTO paramDTO : paramDTOS) {

            String key = paramDTO.getName();  //用于搜索的规格名称
            Object value = null;
            //判断是否是通用规格，如果是从通用规格的map中根据id取值
            if(paramDTO.getGeneric()){
                value = genericSpecMap.get(paramDTO.getId());
            }else{
                value = specialSpecMap.get(paramDTO.getId());
            }
            //判断是否是数值类型
            if(paramDTO.getIsNumeric()){
                //获取商品详情中对应价格的区间范围段 如果value值是1.5，最后的value值是1.5-2.0
                value = chooseSegment(value,paramDTO);
            }
            specMap.put(key,value);
        }

        Goods goods = new Goods();
        goods.setSubTitle(spu.getSubTitle());               //卖点
        goods.setCreateTime(spu.getCreateTime().getTime());  //创建时间;
        goods.setBrandId(spu.getBrandId());                 //品牌的id
        goods.setCategoryId(spu.getCid3());               //分类id保存spu上的三级分类id
        goods.setPrice(priceSet);  //所有sku的价格的set集合
        goods.setAll(keyStr);    //商品标题+分类名称（1，2，3级）+品牌名称
        goods.setSkus(JsonUtils.toString(skuList));   //当前的spu下的所有skus列表，json字符串（id,title,image,price）
        goods.setSpecs(specMap);  //规格参数的Map<String,Object>
        goods.setId(spu.getId());  //spu的id作为_id索引库的唯一值
        return goods;
    }

    @Override
    public PageResult<GoodsDTO> search(SearchRequest searchRequest) {
        /**
         * 1.判断关键字是否有值，没有抛异常
         * 2.new NativeSearchQueryBuilder原生查询构造器
         * 3.构造器中通过SourceFilter只抓取"id","subTitle","skus"字段内容
         * 4.构造器中设置查询条件，采用matchQuery，查询采用并且关系
         * 5.设置分页参数PageRequest.of(pageNo-1,pageSize)
         * 6.查询获取Goods结果,索引库保存的都是Goods
         * 7.PageResult拼接返回结果
         */

        //1.判断关键字是否有值，没有抛异常
        String key = searchRequest.getKey();
        if (StringUtils.isBlank(key)) {
            throw new LyException(ExceptionEnum.INVALID_PARAM_ERROR);
        }

        //2.new NativeSearchQueryBuilder原生查询构造器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        //3.构造器中通过SourceFilter只抓取"id","subTitle","skus"字段内容
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","subTitle","skus"}, null));

        //4.构造器中设置查询条件，采用matchQuery，查询采用并且关系
        queryBuilder.withQuery(QueryBuilders.matchQuery("all", key).operator(Operator.AND));

        //5.设置分页参数PageRequest.of(pageNo-1,pageSize)
        queryBuilder.withPageable(PageRequest.of(searchRequest.getPage() - 1,searchRequest.getSize()));

        //6.查询获取Goods结果,索引库保存的都是Goods
        AggregatedPage<Goods> goods = esTemplate.queryForPage(queryBuilder.build(), Goods.class);

        long totalElements = goods.getTotalElements();
        int totalPages = goods.getTotalPages();
        List<GoodsDTO> content = BeanHelper.copyWithCollection(goods.getContent(), GoodsDTO.class) ;

        return new PageResult<>(totalElements, totalPages,content);
    }

    /**
     * 比较区间范围段的代码
     * @param value
     * @param p
     * @return
     */
    private String chooseSegment(Object value, SpecParamDTO p) {
        //0-1.0,1.0-1.5,1.5-2.0,2.0-2.5,2.5-
        if (value == null || StringUtils.isBlank(value.toString())) {
            return "其它";
        }
        double val = parseDouble(value.toString());
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = parseDouble(segs[0]);
            double end = Double.MAX_VALUE; //1.7976931348623157E308
            if (segs.length == 2) {
                end = parseDouble(segs[1]);
            }
            // 判断是否在范围内
            if (val >= begin && val < end) {
                if (segs.length == 1) {
                    result = segs[0] + p.getUnit() + "以上";
                } else if (begin == 0) {
                    result = segs[1] + p.getUnit() + "以下";
                } else {
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    private double parseDouble(String str) {
        try {
            return Double.parseDouble(str);
        } catch (Exception e) {
            return 0;
        }
    }
}
