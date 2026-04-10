package org.main.neo4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

@Service
public class Neo4jInteracaoService {

    private static final Logger log = LoggerFactory.getLogger(Neo4jInteracaoService.class);

    private final Neo4jClient neo4jClient;

    @Value("${agro.recomendacao.neo4j.enabled:false}")
    private boolean enabled;

    public Neo4jInteracaoService(Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
    }

    public void registrarVisualizacao(Integer idUsuario, Integer idProduto) {
        if (!enabled) return;
        if (idUsuario == null || idProduto == null) return;

        try {
            String cypher = """
                    MERGE (u:User {id: $userId})
                    MERGE (p:Product {id: $productId})
                    MERGE (u)-[v:VIEWED]->(p)
                    SET v.lastAt = datetime()
                    """;

            neo4jClient.query(cypher)
                    .bind(idUsuario).to("userId")
                    .bind(idProduto).to("productId")
                    .run();
        } catch (Exception ex) {
            log.warn("Neo4j: falha ao registrar VIEWED user={} product={}: {}", idUsuario, idProduto, ex.getMessage());
        }
    }

    public void atualizarFavorito(Integer idUsuario, Integer idProduto, boolean favoritado) {
        if (!enabled) return;
        if (idUsuario == null || idProduto == null) return;

        try {
            if (favoritado) {
                String cypher = """
                        MERGE (u:User {id: $userId})
                        MERGE (p:Product {id: $productId})
                        MERGE (u)-[f:FAVORITED]->(p)
                        SET f.at = datetime()
                        """;
                neo4jClient.query(cypher)
                        .bind(idUsuario).to("userId")
                        .bind(idProduto).to("productId")
                        .run();
            } else {
                String cypher = """
                        MATCH (u:User {id: $userId})-[f:FAVORITED]->(p:Product {id: $productId})
                        DELETE f
                        """;
                neo4jClient.query(cypher)
                        .bind(idUsuario).to("userId")
                        .bind(idProduto).to("productId")
                        .run();
            }
        } catch (Exception ex) {
            log.warn("Neo4j: falha ao atualizar FAVORITED user={} product={} favoritado={}: {}",
                    idUsuario, idProduto, favoritado, ex.getMessage());
        }
    }
}
