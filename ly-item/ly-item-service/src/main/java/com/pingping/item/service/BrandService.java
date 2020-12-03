package com.pingping.item.service;

import com.pingping.common.vo.PageResult;
import com.pingping.item.dto.BrandDTO;

public interface BrandService {
    PageResult<BrandDTO> queryBrandByPage(Integer page, Integer rows, String key, String sortBy, Boolean desc);
}
