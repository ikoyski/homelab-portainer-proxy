package com.ikoyski.homelab.portainer.proxy.config;

import com.ikoyski.homelab.portainer.proxy.upstream.ResponseSizeLimitInterceptor;
import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Builds the upstream client using the JVM's default TLS context.
 *
 * <p>The default context honours the runtime-mounted JVM truststore. This preserves
 * normal certificate-chain and hostname/IP verification for the self-signed
 * Portainer certificate.</p>
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(PortainerProperties.class)
public class PortainerClientConfiguration {

    @Bean
    RestClient portainerRestClient(PortainerProperties properties) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(properties.connectTimeoutMs()))
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofMillis(properties.readTimeoutMs()));

        return RestClient.builder()
                .baseUrl(properties.baseUrl().toString())
                .defaultHeader("X-API-Key", properties.apiKey())
                .requestFactory(requestFactory)
                .requestInterceptor(new ResponseSizeLimitInterceptor(properties.maxResponseBytes()))
                .build();
    }
}
