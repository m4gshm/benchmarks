package m4gshm.benchmark.storage;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MemoryStorageConfiguration {

    @Bean
    public Storage<Task, String> storage() {
        return new MemoryStorage<>();
    }

}
