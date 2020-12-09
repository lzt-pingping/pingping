package com.pingping.search.service;

import com.pingping.common.vo.PageResult;
import com.pingping.item.dto.SpuDTO;
import com.pingping.search.dto.GoodsDTO;
import com.pingping.search.dto.SearchRequest;
import com.pingping.search.pojo.Goods;

public interface SearchService {
    Goods buildGoods(SpuDTO spu);

    PageResult<GoodsDTO> search(SearchRequest request);
}
