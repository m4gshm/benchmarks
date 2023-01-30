package m4gshm.benchmark.rest.spring.boot;


import lombok.NoArgsConstructor;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication(scanBasePackages = "m4gshm.benchmark", proxyBeanMethods = false)
@NoArgsConstructor
public class SpringMvcApplication {

    public static void main(String[] args) {
        run(SpringMvcApplication.class, args);
    }

}
