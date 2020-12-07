package com.pingping.item.service;

import com.pingping.item.dto.CategoryDTO;
import java.util.List;

public interface CategoryService {
    List<CategoryDTO> queryListByParent(Long pid);
    List<CategoryDTO> queryCategoryByIds(List<Long> ids);
}
