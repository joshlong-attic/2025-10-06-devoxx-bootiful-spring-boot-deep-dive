package com.example.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.filter.TokenRelayFilterFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.web.servlet.function.RouterFunctions.route;

@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    // :8081/dogs/dogs  => :8080/dogs/dogs

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    RouterFunction<ServerResponse> apiRoute() {
        return route()
                .before(BeforeFilterFunctions.uri("http://localhost:8080"))
                .before(BeforeFilterFunctions.rewritePath("/dogs/", "/"))
                .filter(TokenRelayFilterFunctions.tokenRelay())
                .GET("/dogs/**", http())
                .build();
    }

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    RouterFunction<ServerResponse> uiRoute() {
        return route()
                .filter(TokenRelayFilterFunctions.tokenRelay())
                .before(BeforeFilterFunctions.uri("http://localhost:8020"))
                .GET("/**", http())
                .build();
    }

}
