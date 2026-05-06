package com.example.FuelMemo.Security.jwt;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Value("${CLOUDINARY_CLOUD_NAME:dxm8bszc0}")
    private String cloudName;

    @Value("${CLOUDINARY_API_KEY:779458221647337}")
    private String apiKey;

    @Value("${CLOUDINARY_API_SECRET:U0IBGhrjxXGJgRyNpD56IZiWqEQ}")
    private String apiSecret;

    @Bean
    public Cloudinary getCloudinary() {
        Map<String, Object> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);
        config.put("secure", true);

        return new Cloudinary(config);
    }
}

