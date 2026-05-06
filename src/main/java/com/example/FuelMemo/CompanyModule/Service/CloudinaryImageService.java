package com.example.FuelMemo.CompanyModule.Service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface CloudinaryImageService {

    //    Map<String,String> upload(MultipartFile file);
     public Map upload(MultipartFile file, Map<String, String> headers);
}
