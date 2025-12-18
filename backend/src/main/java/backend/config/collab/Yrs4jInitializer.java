package backend.config.collab;

import at.yrs4j.api.LibLoader;
import at.yrs4j.api.Yrs4J;
import at.yrs4j.api.YrsLibNativeInterface;
import at.yrs4j.libnative.windows.WindowsLibLoader;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Configuration;

@Configuration
public class Yrs4jInitializer {

    @PostConstruct
    public void initYrs() {
        Yrs4J.init(WindowsLibLoader.create());
        // log.info("Yrs4J native library initialized");
    }
}
