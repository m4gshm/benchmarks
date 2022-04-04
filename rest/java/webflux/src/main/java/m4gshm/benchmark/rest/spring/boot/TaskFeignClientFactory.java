package m4gshm.benchmark.rest.spring.boot;

import feign.Feign;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

public interface TaskFeignClientFactory {
    static TaskAPI newClient(String rootUrl) {
        var converters = new HttpMessageConverters(new MappingJackson2HttpMessageConverter());
        return Feign.builder().contract(new SpringMvcContract())
                .encoder(new SpringEncoder(() -> converters))
                .decoder(new SpringDecoder(() -> converters))
//                .decode404()
                .target(TaskAPI.class, rootUrl + TaskAPI.ROOT_PATH_TASK);
    }
}
