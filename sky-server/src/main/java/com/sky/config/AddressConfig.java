package com.sky.config;

import com.sky.properties.AddressProperties;
import com.sky.properties.AliOssProperties;
import com.sky.utils.AddressUtil;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class AddressConfig {

    @Bean
    public AddressUtil getAddressUtil(AddressProperties addressProperties){
        log.info("地址工具类初始化，{}",addressProperties);
            return new AddressUtil(addressProperties.getAk());}
}
