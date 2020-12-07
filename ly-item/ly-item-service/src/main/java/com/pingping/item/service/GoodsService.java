package com.pingping.item.service;

import com.pingping.common.vo.PageResult;
import com.pingping.item.dto.SkuDTO;
import com.pingping.item.dto.SpuDTO;
import com.pingping.item.dto.SpuDetailDTO;

import java.util.List;

public interface GoodsService {
    PageResult<SpuDTO> querySpuByPage(Integer page, Integer rows, Boolean saleable, String key);
    void save(SpuDTO spuDTO);

    void updateSaleable(Long id, Boolean saleable);

    SpuDetailDTO querySpuDetailById(Long id);

    List<SkuDTO> querySkuListBySpuId(Long id);

    void updateGoods(SpuDTO spu);
}
