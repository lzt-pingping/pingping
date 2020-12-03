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
}

