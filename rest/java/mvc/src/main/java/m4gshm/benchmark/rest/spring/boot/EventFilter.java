package m4gshm.benchmark.rest.spring.boot;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

import static m4gshm.benchmark.rest.java.jft.HttpEvent.start;
import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

@Order(HIGHEST_PRECEDENCE)
@Component
@ConditionalOnProperty(value = EventFilter.JFR_HTTP_EVENT_ENABLED, havingValue = "true", matchIfMissing = true)
public class EventFilter implements Filter {

    public static final String JFR_HTTP_REQUEST_EVENT = "JFR_HTTP_REQUEST_EVENT";
    public static final String JFR_HTTP_EVENT_ENABLED = "jfr.http-event.enabled";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        var httpRequest = (HttpServletRequest) request;
        try (var event = start(httpRequest.getMethod(), httpRequest.getRequestURI())) {
            request.setAttribute(JFR_HTTP_REQUEST_EVENT, event);
            chain.doFilter(request, response);
        }
    }
}
