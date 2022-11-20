package m4gshm.benchmark.rest.quarkus.jfr;

import io.quarkus.arc.lookup.LookupIfProperty;
import m4gshm.benchmark.rest.java.jfr.HttpEvent;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

import static m4gshm.benchmark.rest.quarkus.jfr.PreprocessEventFilter.JFR_HTTP_REQUEST_EVENT;

@Provider
@LookupIfProperty(name = "write.trace", stringValue = "true")
public class FinishEventFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        var event = (HttpEvent) requestContext.getProperty(JFR_HTTP_REQUEST_EVENT);
        event.finishProcess();
        event.finish();
    }
}
