package br.com.bg.ludopedia.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "ludopedia")
public record LudopediaProperties(
        String baseUrl,
        String accessToken,
        Duration connectTimeout,
        Duration readTimeout,
        Duration scrapeTimeout) {
}
