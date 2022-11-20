package m4gshm.benchmark.rest.spring.boot;

import m4gshm.benchmark.rest.java.jfr.HttpEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@ConditionalOnProperty(value = WebStatsFilter.JFR_HTTP_EVENT_ENABLED, havingValue = "true", matchIfMissing = true)
public class WebStatsFilter implements WebFilter {

    public static final String JFR_HTTP_EVENT_ENABLED = "jfr.http-event.enabled";

    @Override
    public Mono<Void> filter(ServerWebExchange serverWebExchange, WebFilterChain webFilterChain) {
        var request = serverWebExchange.getRequest();
        var method = request.getMethod();
        var path = request.getPath().value();
        var event = HttpEvent.create(method, path);
        return webFilterChain.filter(serverWebExchange).doOnSubscribe(s -> event.start()).doOnTerminate(event::finish);
    }
}
