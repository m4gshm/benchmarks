package m4gshm.benchmark.rest.spring.boot;

import kotlin.jvm.JvmInline;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import m4gshm.benchmark.rest.java.jfr.BaseEvent;
import m4gshm.benchmark.rest.java.jfr.RestControllerEvent;
import m4gshm.benchmark.rest.java.jfr.ScopeBasedEvent;
import m4gshm.benchmark.rest.java.jfr.StorageEvent;
import m4gshm.benchmark.rest.java.storage.model.impl.TaskImpl;
import org.jetbrains.annotations.NotNull;
import org.springframework.aot.hint.*;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static org.springframework.aot.hint.ExecutableMode.INVOKE;

@Slf4j
@Configuration
@ImportRuntimeHints(AotRuntimeHintsConfiguration.PropertyNamingStrategyRegistrar.class)
public class AotRuntimeHintsConfiguration {

    static class PropertyNamingStrategyRegistrar implements RuntimeHintsRegistrar {
        @Override
        @SneakyThrows
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            hints.resources()
                    .registerPattern("db/migration/*.sql")
                    .registerType(BaseEvent.class)
                    .registerType(ScopeBasedEvent.class)
                    .registerType(RestControllerEvent.class)
                    .registerType(StorageEvent.class)
            ;
            reRegApplicationContextInitializers(
                    hints.reflection()
                            .registerType(DataSourceAutoConfiguration.class, MemberCategory.values())
                            .registerType(R2dbcAutoConfiguration.class, MemberCategory.values())
                            .registerType(TaskImpl.class)
                            .registerType(TaskImpl[].class)
                            .registerType(JvmInline.class)
            );
        }
    }

    private static void reRegApplicationContextInitializers(ReflectionHints reflection) {
        var applicationContextInitializers = reflection.typeHints().map(h -> {
            var type = h.getType();
            var typeClass = type.getClass();
            var classType = "ReflectionTypeReference".equals(typeClass.getSimpleName())
                    ? (Class<?>) getFieldValue(typeClass, type)
                    : null;
            if (classType != null) try {
                if (ApplicationContextInitializer.class.isAssignableFrom(classType)) {
                    return classType;
                }
            } catch (Exception e) {
                log.error("ApplicationContextInitializer load error", e);
            }
            return null;
        }).filter(Objects::nonNull).toList();

        applicationContextInitializers.forEach(aClass -> {
            log.info("register type with getConstructors: {}", aClass);
            reflection.registerType(aClass, getConstructors());
        });
    }

    @SneakyThrows
    private static Object getFieldValue(Class<?> typeClass, Object type) {
        var field = typeClass.getDeclaredField("type");
        field.setAccessible(true);
        var o = field.get(type);
        return o;
    }

    @NotNull
    private static Consumer<TypeHint.Builder> getConstructors() {
        return builder -> builder
                .withMembers(MemberCategory.values())
                .withMethod("getConstructors", List.of(), INVOKE);
    }

}
