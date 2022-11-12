package m4gshm.benchmark.rest.spring.boot;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.netty.ReactorNetty;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication
public class WebfluxApplication {
    public static void main(String[] args) {
//        System.setProperty(ReactorNetty.IO_SELECT_COUNT, "8");
//        System.setProperty(ReactorNetty.IO_WORKER_COUNT, "8");
//        System.setProperty(ReactorNetty.POOL_MAX_CONNECTIONS, "700");

        run(WebfluxApplication.class, args);
    }

}
