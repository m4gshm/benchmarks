package m4gshm.benchmark.rest.spring.boot;

import m4gshm.benchmark.rest.spring.boot.api.ReactiveTaskAPI;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactivefeign.webclient.WebReactiveFeign;

import static org.springframework.web.reactive.function.client.WebClient.builder;
import static reactor.netty.http.client.HttpClient.create;
import static reactor.netty.resources.ConnectionProvider.builder;

public interface TaskReactiveFeignClientFactory {
    static ReactiveTaskAPI newClient(String rootUrl) {
        var httpConnector = new ReactorClientHttpConnector(create(builder("123").pendingAcquireMaxCount(10_000).build()));
        return WebReactiveFeign
                .<ReactiveTaskAPI>builder(builder(), builder -> builder.clientConnector(httpConnector))
                .contract(new SpringMvcContract())
//                .decode404()
                .target(ReactiveTaskAPI.class, rootUrl + ReactiveTaskAPI.ROOT_PATH_TASK);
    }
}
