package m4gshm.benchmark.rest.quarkus.vertx;

import io.vertx.core.Context;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.spi.tracing.SpanKind;
import io.vertx.core.spi.tracing.TagExtractor;
import io.vertx.core.spi.tracing.VertxTracer;
import io.vertx.core.tracing.TracingPolicy;
import io.vertx.sqlclient.impl.tracing.QueryRequest;

import java.util.List;
import java.util.function.BiConsumer;

import static java.util.stream.IntStream.range;

class QueryRequestLogger implements VertxTracer<Object, Object> {
    private static final Logger sqlLog = LoggerFactory.getLogger("reactive.sql");

    @Override
    public Object receiveRequest(Context context, SpanKind kind, TracingPolicy policy, Object request,
                                 String operation, Iterable headers, TagExtractor tagExtractor) {
        return VertxTracer.super.receiveRequest(context, kind, policy, request, operation, headers, tagExtractor);
    }

    @Override
    public void sendResponse(Context context, Object response, Object payload, Throwable failure, TagExtractor tagExtractor) {
        VertxTracer.super.sendResponse(context, response, payload, failure, tagExtractor);
    }

    @Override
    public Object sendRequest(Context context, SpanKind kind, TracingPolicy policy, Object request,
                              String operation, BiConsumer headers, TagExtractor tagExtractor) {
        if (request instanceof QueryRequest queryRequest) {
            var sql = queryRequest.sql();
            if (sqlLog.isTraceEnabled()) {
                var tuples = queryRequest.tuples();
                var args = tuples.stream().map(tuple -> range(0, tuple.size()).mapToObj(tuple::getValue).map(v -> switch (v) {
                    case int[] a -> List.of(a);
                    case short[] a -> List.of(a);
                    case long[] a -> List.of(a);
                    case float[] a -> List.of(a);
                    case double[] a -> List.of(a);
                    case char[] a -> List.of(a);
                    case boolean[] a -> List.of(a);
                    case Object[] a -> List.of(a);
                    case null, default -> v;
                }).toList()).toList();
                sqlLog.trace(" SQL: " + sql + ";\tARGS: " + args);
            } else if (sqlLog.isDebugEnabled()) {
                sqlLog.debug("SQL: " + sql);
            }
        }
        return VertxTracer.super.sendRequest(context, kind, policy, request, operation, headers, tagExtractor);
    }

    @Override
    public void receiveResponse(Context context, Object response, Object payload, Throwable failure, TagExtractor tagExtractor) {
        VertxTracer.super.receiveResponse(context, response, payload, failure, tagExtractor);
    }

    @Override
    public void close() {
        VertxTracer.super.close();
    }
}
