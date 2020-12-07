package com.pingping.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.pingping.common.enums.ExceptionEnum;
import com.pingping.common.exceptions.LyException;
import com.pingping.common.utils.BeanHelper;
import com.pingping.common.vo.PageResult;
import com.pingping.item.dto.*;
import com.pingping.item.entity.Sku;
import com.pingping.item.entity.Spu;
import com.pingping.item.entity.SpuDetail;
import com.pingping.item.mapper.SkuMapper;
import com.pingping.item.mapper.SpuDetailMapper;
import com.pingping.item.mapper.SpuMapper;
import com.pingping.item.service.BrandService;
import com.pingping.item.service.CategoryService;
import com.pingping.item.service.GoodsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private SpuDetailMapper detailMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private SkuMapper skuMapper;
    public PageResult<SpuDTO> querySpuByPage(Integer page, Integer rows, Boolean saleable, String key) {
        // 1 分页
        Page<Spu> spuPage = new Page<Spu>(page, rows);
        // 2 过滤
        QueryWrapper<Spu> wrapper = new QueryWrapper<>();
        // 2.1 搜索条件过滤
        wrapper.like(StringUtils.isNotBlank(key),"name",key );
        // 2.2 上下架过滤
        wrapper.eq(saleable != null,"saleable", saleable);
        // 2.3 默认按时间排序，也可以按照创建时间排序，修改时间排序为了修改看效果方便
        wrapper.orderByDesc("update_time");
        // 3 查询结果
        spuMapper.selectPage(spuPage, wrapper);
        
        if(CollectionUtils.isEmpty(spuPage.getRecords())){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        // DTO转换
        List<SpuDTO> spuDTOList = BeanHelper.copyWithCollection(spuPage.getRecords(), SpuDTO.class);
        // 5 处理分类名称和品牌名称
        handleCategoryAndBrandName(spuDTOList);

        return new PageResult<>(spuPage.getTotal(), spuDTOList);
    }

    @Override
    @Transactional
    public void save(SpuDTO spuDTO) {
        /**
         * 1.保存spu，设置下架属性，并存数据库
         * 2.保存spuDetail，设置一对一关系，并存数据库
         * 3.将skus列表中数据保存到数据库注意设置sku和spu的对应关系（多对一关系）
         */

        // 从DTO中取出spu信息
        Spu spu = BeanHelper.copyProperties(spuDTO, Spu.class);
        spu.setSaleable(false);   //'是否上架，0下架，1上架'

        int count = spuMapper.insert(spu);
        if (count != 1) {
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }

        // 初始化SpuDetail信息
        SpuDetailDTO spuDetailDTO = spuDTO.getSpuDetail();
        SpuDetail spuDetail = BeanHelper.copyProperties(spuDetailDTO, SpuDetail.class);
        // 保存spu详情
        spuDetail.setSpuId(spu.getId());
        count = detailMapper.insert(spuDetail);
        if (count != 1) {
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }

        // 准备sku数据
        // 保存sku
        count = 0;  //初始化新增返回计数器
        List<SkuDTO> skus = spuDTO.getSkus();
        for (SkuDTO skuDTO : skus) {
            Sku sku = BeanHelper.copyProperties(SkuDTO.class, Sku.class);
            sku.setSpuId(spu.getId());
            count = count + skuMapper.insert(sku);
        }

        if (count != skus.size() || count == 0) {
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
    }

    @Override
    @Transactional
    public void updateSaleable(Long id, Boolean saleable) {
        // 1.更新SPU
        Spu spu = new Spu();
        spu.setId(id);
        spu.setSaleable(saleable);
        int count = spuMapper.updateById(spu);
        if (count != 1) {
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
        // 2.更新sku
        // 2.1.准备要更新的数据
        Sku sku = new Sku();
        sku.setEnable(saleable);

        UpdateWrapper<Sku> wrapper = new UpdateWrapper<>();
        wrapper.eq(id != null,"spu_id", id);
        // 2.2.参数1设置更新内容   参数2是传入的wrapper条件
        int size = skuMapper.update(sku, wrapper);
        //SqlHelper是Plus提供的一个静态方法，传入返回的数量可以内部进行判断
        boolean bool = SqlHelper.retBool(size);
        if (!bool) {
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
    }

    @Override
    public SpuDetailDTO querySpuDetailById(Long id) {
        SpuDetail spuDetail = detailMapper.selectById(id);
        if (spuDetail == null) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        return BeanHelper.copyProperties(spuDetail, SpuDetailDTO.class);
    }

    @Override
    public List<SkuDTO> querySkuListBySpuId(Long id) {
        Sku sku = new Sku();
        sku.setSpuId(id);
        List<Sku> list = skuMapper.selectList(new QueryWrapper<>(sku));
        if(CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(list, SkuDTO.class);
    }

    @Override
    @Transactional
    public void updateGoods(SpuDTO spuDTO) {
        Long spuId = spuDTO.getId();
        if (spuId == null) {
            // 请求参数有误
            throw new LyException(ExceptionEnum.INVALID_PARAM_ERROR);
        }

        //1.更新spu，修改下架状态，修改更新时间
        Spu spu = BeanHelper.copyProperties(spuDTO, Spu.class);
        spu.setUpdateTime(new Date());      //修改时间赋值
        int count = spuMapper.updateById(spu);
        if (count != 1) {
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }

        //2.更新spuDetail，修改更新时间
        SpuDetail spuDetail = BeanHelper.copyProperties(spuDTO.getSpuDetail(), SpuDetail.class);
        spuDetail.setUpdateTime(new Date());
        count = detailMapper.updateById(spuDetail);
        if (count != 1) {
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }

        //3.删除之前的sku列表，本次修改变新增操作
        Sku sku = new Sku();
        sku.setSpuId(spuId);
        //先删除
        count = skuMapper.delete(new QueryWrapper<>(sku));
        boolean bool = SqlHelper.retBool(count);
        if (!bool) {
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
        //再新增
        count = 0;
        List<SkuDTO> skuDtos = spuDTO.getSkus();
        for (SkuDTO skuDTO : skuDtos) {
            Sku skudb = BeanHelper.copyProperties(skuDTO, Sku.class);
            skudb.setSpuId(spu.getId());   //删除后本次是新增需要对外键关联重新设置
            count = count + skuMapper.insert(skudb);
        }
        if(count!=skuDtos.size()){
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
    }

    private void handleCategoryAndBrandName(List<SpuDTO> list) {
        for (SpuDTO spu : list) {
            // 查询分类
            String categoryName = categoryService.queryCategoryByIds(spu.getCategoryIds())
                    .stream()
                    .map(CategoryDTO::getName).collect(Collectors.joining("/"));
            spu.setCategoryName(categoryName);
            // 查询品牌
            BrandDTO brand = brandService.queryBrandById(spu.getBrandId());
            spu.setBrandName(brand.getName());
        }
    }
}