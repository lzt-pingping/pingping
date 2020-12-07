package com.pingping.item.controller;

import com.pingping.common.vo.PageResult;
import com.pingping.item.dto.SkuDTO;
import com.pingping.item.dto.SpuDTO;
import com.pingping.item.dto.SpuDetailDTO;
import com.pingping.item.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    /**
     * 分页查询spu
     * @param page 当前页
     * @param rows 每页大小
     * @param saleable 上架商品或下降商品
     * @param key 关键字
     * @return 当前页商品数据
     */
    @GetMapping("spu/page")
    public ResponseEntity<PageResult<SpuDTO>> querySpuByPage(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "saleable", required = false) Boolean saleable,
            @RequestParam(value = "key", required = false) String key) {
 		return ResponseEntity.ok(goodsService.querySpuByPage(page, rows, saleable, key));
    }
    /**
     * 新增商品
     * @param spuDTO 页面提交商品信息
     * @return
     */
    @PostMapping("goods")
    public ResponseEntity<Void> saveGoods(@RequestBody SpuDTO spuDTO) {
        this.goodsService.save(spuDTO);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
    /**
     * 修改商品上下架
     * @param id 商品spu的id
     * @param saleable true：上架；false：下架
     * @return
     */
    @PutMapping("spu/saleable")
    public ResponseEntity<Void> updateSpuSaleable(@RequestParam("id") Long id, @RequestParam("saleable") Boolean saleable) {
        goodsService.updateSaleable(id, saleable);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    //在goodsController增加方法
    /**
     * 根据spuID查询spuDetail
     * @param id spuID
     * @return SpuDetail
     */
    @GetMapping("spu/detail")
    public ResponseEntity<SpuDetailDTO> querySpuDetailById(@RequestParam("id") Long id) {
        return ResponseEntity.ok(goodsService.querySpuDetailById(id));
    }
    /**
     * 根据spuID查询sku
     * @param id spuID
     * @return sku的集合
     */
    @GetMapping("sku/of/spu")
    public ResponseEntity<List<SkuDTO>> querySkuBySpuId(@RequestParam("id") Long id) {
        return ResponseEntity.ok(this.goodsService.querySkuListBySpuId(id));
    }
    /**
     * 修改商品
     * @param spu
     * @return
     */
    @PutMapping("goods")
    public ResponseEntity<Void> updateGoods(@RequestBody SpuDTO spu) {
        goodsService.updateGoods(spu);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}