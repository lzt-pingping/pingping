package com.pingping.item.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pingping.item.entity.Brand;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BrandMapper extends BaseMapper<Brand> {
    int insertCategoryBrand(@Param("bid")Long id, @Param("ids")List<Long> ids);

    List<Brand> queryByCategoryId(Long categoryId);
}