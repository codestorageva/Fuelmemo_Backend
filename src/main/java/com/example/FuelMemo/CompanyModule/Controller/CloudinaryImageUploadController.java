package com.example.FuelMemo.CompanyModule.Controller;

import com.example.FuelMemo.CompanyModule.Service.CloudinaryImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("cloudinary/upload")
public class CloudinaryImageUploadController {

    @Autowired
    private CloudinaryImageService cloudinaryImageService;

    @PostMapping
    public ResponseEntity<Map> uploxadImage(@RequestParam("image") MultipartFile file, @RequestHeader Map<String, String> headers) {
        Map data = this.cloudinaryImageService.upload(file, headers);
        return new ResponseEntity<>(data, HttpStatus.OK);

    }
}