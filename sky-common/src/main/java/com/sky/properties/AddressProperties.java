package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

@Component
@ConfigurationProperties(prefix = "sky.baidu")
@Data
public class AddressProperties {
    private String ak;
}
