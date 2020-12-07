package com.pingping.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pingping.item.entity.Category;
import com.pingping.item.mapper.CategoryMapper;
import com.pingping.item.service.CategoryService;
import com.pingping.common.enums.ExceptionEnum;
import com.pingping.common.exceptions.LyException;
import com.pingping.common.utils.BeanHelper;
import com.pingping.item.dto.CategoryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;
    @Override
    public List<CategoryDTO> queryListByParent(Long pid) {
        // 查询条件，mapper会把对象中的非空属性作为查询条件
        Category category = new Category();
        category.setParentId(pid);

        Wrapper<Category> wrapper = new QueryWrapper<>(category);
        List<Category> categories = categoryMapper.selectList(wrapper);

        //没有查到数据抛异常
        if(StringUtils.isEmpty(categories)){
            throw  new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }

        // 使用自定义工具类，把Category集合转为DTO的集合
        return BeanHelper.copyWithCollection(categories, CategoryDTO.class);
    }
    @Override
    public List<CategoryDTO> queryCategoryByIds(List<Long> ids){
        List<Category> list = categoryMapper.selectBatchIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            // 没找到，返回404
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(list, CategoryDTO.class);
    }
}
