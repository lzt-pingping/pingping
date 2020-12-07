package com.pingping.item.service;

import com.pingping.common.vo.PageResult;
import com.pingping.item.dto.BrandDTO;

import java.util.List;

public interface BrandService {
    PageResult<BrandDTO> queryBrandByPage(Integer page, Integer rows, String key, String sortBy, Boolean desc);
    void saveBrand(BrandDTO brand, List<Long> ids);
    BrandDTO queryBrandById(Long id);
}
