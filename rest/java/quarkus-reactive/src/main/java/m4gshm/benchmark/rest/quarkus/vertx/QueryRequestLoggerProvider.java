package m4gshm.benchmark.rest.quarkus.vertx;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.vertx.core.impl.VertxBuilder;

@RegisterForReflection
public class QueryRequestLoggerProvider implements io.vertx.core.spi.VertxServiceProvider {
    public QueryRequestLoggerProvider() {
    }

    @Override
    public void init(VertxBuilder builder) {
        builder.tracer(new QueryRequestLogger());
    }

}
