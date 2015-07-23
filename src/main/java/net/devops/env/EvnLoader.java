package net.devops.env;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

@Slf4j
@Component
public class EvnLoader {

    private final ClassLoader classLoader = EvnLoader.class.getClassLoader();

    @Autowired
    private StandardEnvironment environment;

    private Properties properties;
    private File sourceScript;

    @PostConstruct
    private void init() throws URISyntaxException {
        URL resource = classLoader.getResource("setupenv.sh");

        if (resource == null) {
            throw new RuntimeException("Unable to load sourceScript setupenv.sh");
        }
        sourceScript = new File(resource.toURI());
        log.info("script location {}", sourceScript);

        properties = run();
    }

    @Scheduled(fixedDelay = 500)
    private void reloadEnvs() {
        properties = run();

//        for (String key : properties.stringPropertyNames()) {
//            log.info("env variable: {} value: {}", key, properties.getProperty(key));
//        }

        if (properties.keys().hasMoreElements()) {
            MutablePropertySources propertySources = environment.getPropertySources();
            propertySources.addFirst(new PropertiesPropertySource("system env reloadable", properties));
        }
    }


    public Properties run() {
        Properties envVars = new Properties();

        Process process;
        try {
            process = new ProcessBuilder(sourceScript.toString())
                    .directory(sourceScript.getParentFile())
                    .start();
        } catch (IOException e) {
            log.error("error sourcing settings", e);
            return null;
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                int idx = line.indexOf('=');
                if (idx == -1) {
                    continue;
                }
                String key = line.substring(0, idx);
                if (key.contains("()")) {
                    continue;
                }
                String value = line.substring(idx + 1);
                envVars.setProperty(key, value);
                // System.out.println( key + " = " + value );
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.warn("source script exit code {}", exitCode);
            }
        } catch (IOException e) {
            log.error("error reading evn variables from std", e);
        } catch (InterruptedException e) {
            log.error("interrupted", e);
        }
        return envVars;

    }
}
