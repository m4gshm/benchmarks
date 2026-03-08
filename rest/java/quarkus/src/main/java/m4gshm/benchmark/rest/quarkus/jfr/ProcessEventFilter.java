package m4gshm.benchmark.rest.quarkus.jfr;

import io.quarkus.arc.lookup.LookupIfProperty;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import m4gshm.benchmark.rest.java.jfr.HttpEvent;



import static m4gshm.benchmark.rest.quarkus.jfr.PreprocessEventFilter.JFR_HTTP_REQUEST_EVENT;

@Provider
@LookupIfProperty(name = "write.trace", stringValue = "true")
public class ProcessEventFilter implements ContainerRequestFilter {
    @Override
    public void filter(ContainerRequestContext requestContext) {
        var event = (HttpEvent) requestContext.getProperty(JFR_HTTP_REQUEST_EVENT);
        event.finishPreprocess();
    }
}
