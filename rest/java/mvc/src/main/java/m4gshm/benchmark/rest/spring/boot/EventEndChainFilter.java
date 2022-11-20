package m4gshm.benchmark.rest.spring.boot;

import m4gshm.benchmark.rest.java.jfr.HttpEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

import static m4gshm.benchmark.rest.spring.boot.EventFilter.JFR_HTTP_REQUEST_EVENT;
import static org.springframework.core.Ordered.LOWEST_PRECEDENCE;

@Order(LOWEST_PRECEDENCE)
@Component
@ConditionalOnProperty(value = EventFilter.JFR_HTTP_EVENT_ENABLED, havingValue = "true", matchIfMissing = true)
public class EventEndChainFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        var httpRequest = (HttpServletRequest) request;
        var event = (HttpEvent) httpRequest.getAttribute(JFR_HTTP_REQUEST_EVENT);
        event.finishPreprocess();
        chain.doFilter(request, response);
        event.finishProcess();
    }
}
