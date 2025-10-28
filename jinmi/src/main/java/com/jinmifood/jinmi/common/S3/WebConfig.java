package com.jinmifood.jinmi.common.S3;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class WebConfig {

    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter(ObjectMapper objectMapper) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
        List<MediaType> supportedMediaTypes = new ArrayList<>(converter.getSupportedMediaTypes());

        // application/octet-stream 미디어 타입을 JSON 컨버터가 지원하도록 추가
        supportedMediaTypes.add(MediaType.APPLICATION_OCTET_STREAM);

        converter.setSupportedMediaTypes(supportedMediaTypes);
        return converter;
    }
}