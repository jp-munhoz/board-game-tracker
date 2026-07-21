package br.com.bg.ludopedia.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

@Configuration
public class LudopediaClientConfig {

    @Bean
    RestClient ludopediaRestClient(LudopediaProperties properties) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.connectTimeout())
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.readTimeout());

        // Builder proprio (nao o Builder global do Spring) para evitar que o conversor XML
        // registrado para o BGG (jackson-dataformat-xml no classpath) seja usado por engano
        // ao ler as respostas JSON da Ludopedia.
        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.accessToken())
                .messageConverters(converters -> {
                    converters.clear();
                    converters.add(new MappingJackson2HttpMessageConverter());
                })
                .build();
    }
}
