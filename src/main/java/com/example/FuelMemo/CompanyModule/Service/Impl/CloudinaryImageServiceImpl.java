package com.example.FuelMemo.CompanyModule.Service.Impl;

import com.cloudinary.Cloudinary;
import com.example.FuelMemo.CompanyModule.Service.CloudinaryImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryImageServiceImpl implements CloudinaryImageService {

    @Autowired
    private Cloudinary cloudinary;

    @Override
    public Map upload(MultipartFile file, Map<String, String> headers) {
        try {
            Map uploadResult = this.cloudinary.uploader().upload(file.getBytes(), Map.of());
            // Extract only the URL to store
            String url = (String) uploadResult.get("secure_url");
            return Map.of("url", url);
        } catch (IOException e) {
            throw new RuntimeException("Image uploading failed!", e);
        }
    }

}
