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
            if (System.getProperty(entry.getKey()) == null) {
                System.setProperty(entry.getKey(), entry.getValue());
            }
        });

        SpringApplication.run(AgroFraiburgoApplication.class, args);
    }
}