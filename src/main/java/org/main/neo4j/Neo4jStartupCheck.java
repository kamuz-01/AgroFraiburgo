package org.main.neo4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;

@Component
public class Neo4jStartupCheck implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(Neo4jStartupCheck.class);

    private final Neo4jClient neo4jClient;
    private final Environment environment;

    @Value("${agro.recomendacao.neo4j.enabled:false}")
    private boolean enabled;

    public Neo4jStartupCheck(Neo4jClient neo4jClient, Environment environment) {
        this.neo4jClient = neo4jClient;
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) {
            log.info("Neo4j: integração desabilitada (agro.recomendacao.neo4j.enabled=false)");
            return;
        }

        String uri = environment.getProperty("spring.neo4j.uri");
        String database = environment.getProperty("spring.neo4j.database");
        String username = environment.getProperty("spring.neo4j.authentication.username");

        String sysNeo4jUri = System.getProperty("NEO4J_URI");
        String envNeo4jUri = System.getenv("NEO4J_URI");
        String springSeesNeo4jUriVar = environment.getProperty("NEO4J_URI");

        log.info("Neo4j: checagem de startup (uri={}, database={}, username={})", uri, database, username);
        log.info("Neo4j: debug fontes (System.NEO4J_URI={}, Env.NEO4J_URI={}, Spring.NEO4J_URI={})",
                sysNeo4jUri, envNeo4jUri, springSeesNeo4jUriVar);

        try {
            neo4jClient.query("RETURN 1 AS ok").fetch().one();
            log.info("Neo4j: conexão/autenticação OK");
        } catch (Exception ex) {
            log.warn("Neo4j: falha na checagem de startup (conexão/autenticação/database). " +
                            "Se for Aura, confirme NEO4J_URI (neo4j+s://...), NEO4J_USERNAME e NEO4J_PASSWORD no .env.",
                    ex);
        }
    }
}
