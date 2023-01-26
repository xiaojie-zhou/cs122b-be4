package com.github.klefstad_teaching.cs122b.gateway.filter;

import com.github.klefstad_teaching.cs122b.core.result.Result;
import com.github.klefstad_teaching.cs122b.core.result.ResultMap;
import com.github.klefstad_teaching.cs122b.gateway.config.GatewayServiceConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Component
public class AuthFilter implements GatewayFilter
{
    private static final Logger LOG = LoggerFactory.getLogger(AuthFilter.class);

    private final GatewayServiceConfig config;
    private final WebClient webClient;

    @Autowired
    public AuthFilter(GatewayServiceConfig config)
    {
        this.config = config;

        this.webClient = WebClient.builder().build();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain)
    {
        Optional<String> actoken = getAccessTokenFromHeader(exchange);
        if (actoken.isPresent()){
            authenticate(actoken.get()).flatMap(result -> setToFail(exchange));
            return chain.filter(exchange);
        }

        return setToFail(exchange);
    }

    private Mono<Void> setToFail(ServerWebExchange exchange)
    {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    /**
     * Takes in a accessToken token and creates Mono chain that calls the idm and maps the value to
     * a Result
     *
     * @param accessToken a encodedJWT
     * @return a Mono that returns a Result
     */
    private Mono<Result> authenticate(String accessToken)
    {
        AuthResponse resp = new AuthResponse().setAccessToken(accessToken);
        return webClient
                .post()
                .uri(config.getIdmAuthenticate())
                .bodyValue(resp)
                .retrieve()
                .bodyToMono(AuthResponse.class)
                .map(response-> ResultMap.fromCode(response.getResult().getCode()));
    }

    private Optional<String> getAccessTokenFromHeader(ServerWebExchange exchange)
    {
        List<String> header = exchange.getRequest().getHeaders().get("Authorization");
        if (header == null|| header.get(0).length() == 0 || !header.get(0).substring(0,6).equalsIgnoreCase("Bearer")){
            return Optional.empty();
        }
        else {
            return header.stream().findFirst();
        }
    }
}
