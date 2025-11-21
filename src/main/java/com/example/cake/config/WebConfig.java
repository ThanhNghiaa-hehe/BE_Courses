//package com.example.cake.config;
//
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//@Configuration
//public class WebConfig implements WebMvcConfigurer {
//
//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        // Avatar user: http://localhost:8080/static/avatars/abc.jpg
//        registry.addResourceHandler("/static/avatars/**")
//                .addResourceLocations("file:uploads/avatars/");
//        //  Ảnh khóa học: http://localhost:8080/static/courses/abc.jpg
//        registry.addResourceHandler("/static/courses/**")
//                .addResourceLocations("file:uploads/courses/");
//
//    }
//}

package com.example.cake.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/avatars/**")
                .addResourceLocations("file:" + uploadDir + "avatars/");

        registry.addResourceHandler("/static/courses/**")
                .addResourceLocations("file:" + uploadDir + "courses/");
    }
}

