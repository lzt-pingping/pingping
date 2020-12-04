package com.pingping.upload.services;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface UploadService {
    String upload(MultipartFile file);
    Map<String, String> getSignature();
}
