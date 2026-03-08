package m4gshm.benchmark.rest.spring.boot;


import io.github.m4gshm.asyncprof.controller.AsyncProfController;
import lombok.NoArgsConstructor;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication(
        scanBasePackageClasses = { AsyncProfController.class},
        scanBasePackages = "m4gshm.benchmark",
        proxyBeanMethods = false)
@NoArgsConstructor
//@ComponentScan(excludeFilters = @ComponentScan.Filter(value = RestController.class))
public class SpringMvcApplication {

    public static void main(String[] args) {
        run(SpringMvcApplication.class, args);
    }

}
