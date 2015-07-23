package net.devops.env;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PropertyLogger {

    @Autowired
    private Environment environment;

    private String template = "Many ${KAMIL_IS_SUPER} variable is ${KAMIL_IS_MASTER}";

    @Scheduled(fixedDelay = 1_000)
    public void showProperties() {
        log.info("KAMIL_IS_SUPER: {}", environment.getProperty("KAMIL_IS_SUPER"));

        log.info(environment.resolvePlaceholders(template));
    }

}
