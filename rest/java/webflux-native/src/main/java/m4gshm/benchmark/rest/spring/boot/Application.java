package m4gshm.benchmark.rest.spring.boot;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        run(WebfluxApplication.class, args);
    }

}
