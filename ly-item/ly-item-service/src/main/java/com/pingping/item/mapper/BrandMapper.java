package com.pingping.item.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pingping.item.entity.Brand;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BrandMapper extends BaseMapper<Brand> {
    int insertCategoryBrand(Long id, List<Long> ids);
}