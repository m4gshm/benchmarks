package m4gshm.benchmark.rest.quarkus.jfr;

import io.quarkus.arc.lookup.LookupIfProperty;
import m4gshm.benchmark.rest.java.jfr.HttpEvent;

import javax.ws.rs.container.*;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

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
