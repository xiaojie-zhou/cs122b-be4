package com.github.klefstad_teaching.cs122b.gateway.repo;

import com.github.klefstad_teaching.cs122b.gateway.filter.GatewayRequestObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GatewayRepo
{
    private final NamedParameterJdbcTemplate template;
    @Autowired
    public GatewayRepo(NamedParameterJdbcTemplate template)
    {
        this.template = template;
    }

    public Mono<int[]> insertRequests(List<GatewayRequestObject> requests)
    {
        return Mono.fromCallable(() -> insert(requests));
    }

    public int[] insert(List<GatewayRequestObject> requests)
    {
        MapSqlParameterSource[] arrayOfSources = createSources(requests);

        return this.template.batchUpdate(
                "INSERT INTO gateway.request (ip_address, call_time, `path`) " +
                        "VALUES (:ip_address, :call_time, :path);",
                arrayOfSources
        );
    }

    public MapSqlParameterSource[] createSources(List<GatewayRequestObject> requests){
        return requests.stream()
                .map(i -> new MapSqlParameterSource()
                        .addValue("ip_address", i.getIp_address())
                        .addValue("call_time", i.getCall_time())
                        .addValue("path", i.getPath()))
                .toArray(MapSqlParameterSource[]::new);

    }

}
