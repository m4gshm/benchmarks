package m4gshm.benchmark.rest.spring.boot;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static m4gshm.benchmark.rest.java.jft.HttpEvent.start;
import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

@Order(HIGHEST_PRECEDENCE)
@Component
public class EventFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        var httpRequest = (HttpServletRequest) request;
        try (var event = start(httpRequest.getMethod(), httpRequest.getRequestURI())) {
            chain.doFilter(request, response);
        }
    }
}
