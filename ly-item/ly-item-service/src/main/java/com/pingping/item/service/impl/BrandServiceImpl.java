package com.pingping.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pingping.common.enums.ExceptionEnum;
import com.pingping.common.exceptions.LyException;
import com.pingping.common.utils.BeanHelper;
import com.pingping.item.entity.Brand;
import com.pingping.item.mapper.BrandMapper;
import com.pingping.item.service.BrandService;
import com.pingping.common.vo.PageResult;
import com.pingping.item.dto.BrandDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class BrandServiceImpl implements BrandService {


    @Autowired
    private BrandMapper brandMapper;
    @Override
    public PageResult<BrandDTO> queryBrandByPage(Integer page, Integer rows, String key, String sortBy, Boolean desc) {

        //通过Page拼接分页参数
        Page<Brand> brandPage = new Page<>(page, rows);

        //准备wrapper查询
        QueryWrapper<Brand> wrapper = new QueryWrapper<>();
        //1.模糊搜索name列
        wrapper.like(StringUtils.isNotBlank(key), "name",key );
        //2.判断升降序，拼接传入的sortBy列
        if(desc){
            wrapper.orderBy(StringUtils.isNotBlank(sortBy),false,sortBy);
        }else{
            wrapper.orderBy(StringUtils.isNotBlank(sortBy),true,sortBy );
        }

        brandMapper.selectPage(brandPage, wrapper);

        //判断查询结果是否为空
        if(CollectionUtils.isEmpty(brandPage.getRecords())){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }

        //转成BrandDTO集合
        List<BrandDTO> brandDTOS = BeanHelper.copyWithCollection(brandPage.getRecords(), BrandDTO.class);

        return new PageResult<BrandDTO>(brandPage.getTotal(), brandDTOS);
    }

    @Override
    @Transactional
    public void saveBrand(BrandDTO brandDTO, List<Long> ids) {
        // 新增品牌
        Brand brand = BeanHelper.copyProperties(brandDTO, Brand.class);
        int count = brandMapper.insert(brand);
        if(count != 1){
            // 新增失败，抛出异常
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
        // 新增品牌和分类中间表
        count = brandMapper.insertCategoryBrand(brand.getId(), ids);
        // 如果新增到中间表的数量和ids的数量不一致说明新增失败
        if(count != ids.size()){
            // 新增失败，抛出异常
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
    }
    @Override
    public BrandDTO queryBrandById(Long id) {
        Brand brand = brandMapper.selectById(id);
        if (brand == null) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return BeanHelper.copyProperties(brand, BrandDTO.class);
    }
    @Override
    public List<BrandDTO> queryByCategoryId(Long categoryId) {
        List<Brand> list = brandMapper.queryByCategoryId(categoryId);
        // 判断是否为空
        if(CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(list, BrandDTO.class);
    }
}

