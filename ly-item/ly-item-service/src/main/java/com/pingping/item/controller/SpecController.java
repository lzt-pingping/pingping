package com.pingping.item.controller;


import com.pingping.item.dto.SpecGroupDTO;
import com.pingping.item.dto.SpecParamDTO;
import com.pingping.item.service.SpecService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("spec")
public class SpecController {

    @Autowired
    private SpecService specService;
    
    /**
    *基于分类id查询规格组列表
    */
    @GetMapping("groups/of/category")
    public ResponseEntity<List<SpecGroupDTO>> queryGroupByCategory(@RequestParam("id") Long cid) {
        return ResponseEntity.ok(specService.queryGroupByCategory(cid));
    }
    /**
     * 根据规格组id查询规格参数
     * @param gid
     * @return 规格组集合
     */
    @GetMapping("params")
    public ResponseEntity<List<SpecParamDTO>> querySpecParams(
            @RequestParam(value = "gid",required = false)Long gid,
            @RequestParam(value = "cid",required = false)Long cid,                           		 @RequestParam(value = "searching",required = false)Boolean searching){
        return ResponseEntity.ok(specService.querySpecParams(gid,cid,searching));
    }
}