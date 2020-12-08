package com.pingping.item.controller;

import com.pingping.item.service.BrandService;
import com.pingping.common.vo.PageResult;
import com.pingping.item.dto.BrandDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("brand")
public class BrandController {

    @Autowired
    private BrandService brandService;
    
    @GetMapping("page")
    public ResponseEntity<PageResult<BrandDTO>> queryBrandByPage(
            @RequestParam(value = "page", defaultValue = "1")Integer page,
            @RequestParam(value = "rows", defaultValue = "5")Integer rows,
            @RequestParam(value = "key", required = false)String key,
            @RequestParam(value = "sortBy", required = false)String sortBy,
            @RequestParam(value = "desc", defaultValue = "false")Boolean desc
    ){
        return ResponseEntity
            .ok(brandService.queryBrandByPage(page,rows, key, sortBy, desc));
    }
    /**
     * 新增品牌
     * @param brand
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> saveBrand(BrandDTO brand, @RequestParam("cids") List<Long> ids) {
        brandService.saveBrand(brand, ids);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    /**
     * 根据分类查询品牌
     * @param categoryId 商品分类id
     * @return 该分类下的品牌的集合
     */
    @GetMapping("/of/category")
    public ResponseEntity<List<BrandDTO>> queryByCategoryId(@RequestParam("id") Long categoryId) {
        return ResponseEntity.ok(brandService.queryByCategoryId(categoryId));
    }
    /**
     * 根据id查询品牌
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public ResponseEntity<BrandDTO> queryById(@PathVariable("id") Long id){
        return ResponseEntity.ok(brandService.queryBrandById(id));
    }
}