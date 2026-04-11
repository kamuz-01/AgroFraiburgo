package org.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class AgroFraiburgoApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();

        // Define as propriedades do sistema antes do Spring boot iniciar
        dotenv.entries().forEach(entry -> {
            String key = entry.getKey();
            String value = entry.getValue();

            // Para Neo4j, sempre priorize o .env (evita ficar preso em localhost por config antiga).
            if (key != null && key.startsWith("NEO4J_")) {
                System.setProperty(key, value);
                return;
            }

            if (System.getProperty(key) == null) {
                System.setProperty(key, value);
            }
        });

        SpringApplication.run(AgroFraiburgoApplication.class, args);
    }
}