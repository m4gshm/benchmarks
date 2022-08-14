package m4gshm.benchmark.rest.spring.boot;

import m4gshm.benchmark.rest.java.jft.HttpEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class WebStatsFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange serverWebExchange, WebFilterChain webFilterChain) {
        var request = serverWebExchange.getRequest();
        var method = request.getMethod();
        var path = request.getPath().value();
        var event = HttpEvent.create(method + ": " + path);
        return webFilterChain.filter(serverWebExchange).doOnSubscribe(s -> event.start()).doOnTerminate(event::finish);
    }
}
