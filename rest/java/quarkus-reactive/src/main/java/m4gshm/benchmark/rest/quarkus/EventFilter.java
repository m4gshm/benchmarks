package m4gshm.benchmark.rest.quarkus;

import m4gshm.benchmark.rest.java.jft.HttpEvent;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

@Provider
public class EventFilter implements ContainerRequestFilter, ContainerResponseFilter {

    public static final String JFR_EVENT = "jfr.event";

    @Override
    public void filter(ContainerRequestContext requestContext) {
        var event = requestContext.getMethod() + ": " + requestContext.getUriInfo().getPath();
        requestContext.setProperty(JFR_EVENT, HttpEvent.start(event));
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        if (requestContext.getProperty(JFR_EVENT) instanceof HttpEvent event) {
            event.finish();
        }
    }

}
