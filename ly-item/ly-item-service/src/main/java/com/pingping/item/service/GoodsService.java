package com.pingping.item.service;

import com.pingping.common.vo.PageResult;
import com.pingping.item.dto.SpuDTO;

public interface GoodsService {
    PageResult<SpuDTO> querySpuByPage(Integer page, Integer rows, Boolean saleable, String key);
}
