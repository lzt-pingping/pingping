package com.pingping.item.controller;

import com.pingping.item.service.CategoryService;
import com.pingping.item.dto.CategoryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;
    /**
     * 根据父节点查询商品类目
     *http://api.pingping.com/api/item/category/of/parent?pid=0
     * @param pid
     * @return
     */
    @GetMapping("of/parent")
    public ResponseEntity<List<CategoryDTO>> queryByParentId(@RequestParam("pid") Long pid) {
        return ResponseEntity.ok(categoryService.queryListByParent(pid));
    }
    /**
     * 根据id的集合查询商品分类
     * @return 分类集合
     */
    @GetMapping("list")
    public ResponseEntity<List<CategoryDTO>> queryByIds(@RequestParam("ids") List<Long> ids){
        return ResponseEntity.ok(categoryService.queryCategoryByIds(ids));
    }
}
