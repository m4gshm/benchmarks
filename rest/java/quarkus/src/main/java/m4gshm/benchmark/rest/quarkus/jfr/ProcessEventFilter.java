package m4gshm.benchmark.rest.quarkus.jfr;

import io.quarkus.arc.lookup.LookupIfProperty;
import m4gshm.benchmark.rest.java.jft.HttpEvent;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

import static m4gshm.benchmark.rest.quarkus.jfr.PreprocessEventFilter.JFR_HTTP_REQUEST_EVENT;

@Provider
@LookupIfProperty(name = "write.trace", stringValue = "true")
public class ProcessEventFilter implements ContainerRequestFilter {
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        var event = (HttpEvent) requestContext.getProperty(JFR_HTTP_REQUEST_EVENT);
        event.finishPreprocess();
    }
}
