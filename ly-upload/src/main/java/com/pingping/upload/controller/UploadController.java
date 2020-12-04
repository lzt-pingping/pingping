package com.pingping.upload.controller;

import com.pingping.upload.services.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("upload")
public class UploadController {
    @Autowired
    private UploadService uploadService;

    /**
     * 上传图片功能
     *
     * @param file
     * @return
     */
    @PostMapping("images")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        // 返回200，并且携带url路径
        return ResponseEntity.ok(uploadService.upload(file));
    }
    @GetMapping("signature")
    public ResponseEntity<Map<String,String>> getSignature(){
        return ResponseEntity.ok(uploadService.getSignature());
    }
}