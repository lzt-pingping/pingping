package com.pingping.search.service;

import com.pingping.item.dto.SpuDTO;
import com.pingping.search.pojo.Goods;

public interface SearchService {
    Goods buildGoods(SpuDTO spu);
}
