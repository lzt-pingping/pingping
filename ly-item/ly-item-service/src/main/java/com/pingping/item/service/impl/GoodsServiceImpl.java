package com.pingping.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pingping.common.enums.ExceptionEnum;
import com.pingping.common.exceptions.LyException;
import com.pingping.common.utils.BeanHelper;
import com.pingping.common.vo.PageResult;
import com.pingping.item.dto.BrandDTO;
import com.pingping.item.dto.CategoryDTO;
import com.pingping.item.dto.SpuDTO;
import com.pingping.item.entity.Spu;
import com.pingping.item.mapper.SpuDetailMapper;
import com.pingping.item.mapper.SpuMapper;
import com.pingping.item.service.BrandService;
import com.pingping.item.service.CategoryService;
import com.pingping.item.service.GoodsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
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