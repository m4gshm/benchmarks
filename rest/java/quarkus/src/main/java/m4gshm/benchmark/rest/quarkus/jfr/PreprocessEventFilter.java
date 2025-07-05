package m4gshm.benchmark.rest.quarkus.jfr;

import io.quarkus.arc.lookup.LookupIfProperty;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.ext.Provider;
import m4gshm.benchmark.rest.java.jfr.HttpEvent;


@Provider
@PreMatching
@LookupIfProperty(name = "write.trace", stringValue = "true")
public class PreprocessEventFilter implements ContainerRequestFilter {
    public static final String JFR_HTTP_REQUEST_EVENT = "JFR_HTTP_REQUEST_EVENT";

    @Override
    public void filter(ContainerRequestContext requestContext) {
        var event = HttpEvent.start(requestContext.getMethod(), requestContext.getUriInfo().getPath());
        requestContext.setProperty(JFR_HTTP_REQUEST_EVENT, event);
    }

}
