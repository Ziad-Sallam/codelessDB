package backend.config.collab;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExecutorConfig {
    @Bean(name = "collabWriter")
    public ExecutorService collabWriter() {
        return new ThreadPoolExecutor(
                2,          // core threads
                8,          // max threads
                60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(5000), // queue capacity
                new ThreadPoolExecutor.CallerRunsPolicy() // fallback
        );
    }
}
