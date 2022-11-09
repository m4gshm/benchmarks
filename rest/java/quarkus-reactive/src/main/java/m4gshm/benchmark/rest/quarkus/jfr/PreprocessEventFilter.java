package m4gshm.benchmark.rest.quarkus.jfr;

import m4gshm.benchmark.rest.java.jft.HttpEvent;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
@PreMatching
public class PreprocessEventFilter implements ContainerRequestFilter {
    public static final String JFR_HTTP_REQUEST_EVENT = "JFR_HTTP_REQUEST_EVENT";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        var event = HttpEvent.start(requestContext.getMethod(), requestContext.getUriInfo().getPath());
        requestContext.setProperty(JFR_HTTP_REQUEST_EVENT, event);
    }

}
