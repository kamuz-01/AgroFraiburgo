package org.main.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.main.models.Produto;
import org.main.repository.AvaliacaoRepository;
import org.main.repository.ProdutoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

@Service
public class RecomendacaoService {

    private static final Logger log = LoggerFactory.getLogger(RecomendacaoService.class);
    private static final double VIEW_HALF_LIFE_DAYS = 30.0;

    private final Neo4jClient neo4jClient;
    private final ProdutoRepository produtoRepository;
    private final AvaliacaoRepository avaliacaoRepository;

    @Value("${agro.recomendacao.neo4j.enabled:false}")
    private boolean neo4jEnabled;

    public RecomendacaoService(Neo4jClient neo4jClient,
                              ProdutoRepository produtoRepository,
                              AvaliacaoRepository avaliacaoRepository) {
        this.neo4jClient = neo4jClient;
        this.produtoRepository = produtoRepository;
        this.avaliacaoRepository = avaliacaoRepository;
    }

    public List<Produto> recomendarParaUsuario(Integer idUsuario, int limit) {
        if (limit <= 0) return List.of();

        // 1) Tenta Neo4j (colaborativo por FAVORITED e VIEWED)
        if (neo4jEnabled && idUsuario != null) {
            try {
                List<Integer> ids = recomendarIdsViaNeo4j(idUsuario, limit);
                if (!ids.isEmpty()) {
                    return carregarProdutosOrdenados(ids);
                }

                // 1.1) Se ainda não tem favoritos de produto, tenta recomendações pelos produtores favoritados
                List<Integer> porProdutor = recomendarIdsPorProdutoresFavoritadosViaNeo4j(idUsuario, limit);
                if (!porProdutor.isEmpty()) {
                    return carregarProdutosOrdenados(porProdutor);
                }

                // fallback dentro do Neo4j: trending por favoritos
                List<Integer> trendingIds = trendingIdsViaNeo4j(limit);
                if (!trendingIds.isEmpty()) {
                    return carregarProdutosOrdenados(trendingIds);
                }
            } catch (Exception ex) {
                log.warn("Recomendação Neo4j falhou; usando fallback. Erro: {}", ex.getMessage());
            }
        }

        // 2) Fallback: produtos dos produtores melhor avaliados
        List<Produto> best = recomendarPorProdutoresBemAvaliados(limit);
        if (!best.isEmpty()) return best;

        // 3) Último fallback: últimos produtos
        return produtoRepository.findTop4ByOrderByDataCriacaoDesc().stream().limit(limit).toList();
    }

    private List<Integer> recomendarIdsViaNeo4j(Integer idUsuario, int limit) {
        String cypher = """
                                MATCH (u:User {id: $userId})-[f1]->(p:Product)
                                WHERE type(f1) IN ['FAVORITED', 'VIEWED']

                                MATCH (p)<-[f2]-(other:User)-[f3]->(rec:Product)
                                WHERE type(f2) IN ['FAVORITED', 'VIEWED']
                                  AND type(f3) IN ['FAVORITED', 'VIEWED']
                                    AND NOT EXISTS {
                                        MATCH (u)-[f4]->(rec)
                                        WHERE type(f4) IN ['FAVORITED', 'VIEWED']
                                    }
                WITH rec,
                     sum(
                         (
                             CASE type(f1)
                                 WHEN 'FAVORITED' THEN 3.0
                                 ELSE 1.0 + exp(-toFloat(datetime().epochMillis - coalesce(f1.lastAt, f1.at, datetime()).epochMillis) / 86400000.0 / $viewHalfLifeDays)
                             END
                         )
                         *
                         (
                             CASE type(f2)
                                 WHEN 'FAVORITED' THEN 3.0
                                 ELSE 1.0 + exp(-toFloat(datetime().epochMillis - coalesce(f2.lastAt, f2.at, datetime()).epochMillis) / 86400000.0 / $viewHalfLifeDays)
                             END
                         )
                         *
                         (
                             CASE type(f3)
                                 WHEN 'FAVORITED' THEN 3.0
                                 ELSE 1.0 + exp(-toFloat(datetime().epochMillis - coalesce(f3.lastAt, f3.at, datetime()).epochMillis) / 86400000.0 / $viewHalfLifeDays)
                             END
                         )
                     ) AS score
                RETURN rec.id AS idProduto, score
                ORDER BY score DESC
                LIMIT $limit
                """;

        return new ArrayList<>(neo4jClient.query(cypher)
                .bind(idUsuario).to("userId")
                .bind(VIEW_HALF_LIFE_DAYS).to("viewHalfLifeDays")
                .bind(limit).to("limit")
                .fetchAs(Integer.class)
                .mappedBy((typeSystem, record) -> record.get("idProduto").asInt())
            .all());
    }

    private List<Integer> trendingIdsViaNeo4j(int limit) {
        String cypher = """
                MATCH (:User)-[f]->(p:Product)
                 WHERE type(f) IN ['FAVORITED', 'VIEWED']
                 WITH p,
                     sum(
                         CASE type(f)
                             WHEN 'FAVORITED' THEN 3.0
                             ELSE 1.0 + exp(-toFloat(datetime().epochMillis - coalesce(f.lastAt, f.at, datetime()).epochMillis) / 86400000.0 / $viewHalfLifeDays)
                         END
                     ) AS score
                 RETURN p.id AS idProduto, score
                ORDER BY score DESC
                LIMIT $limit
                """;

        return new ArrayList<>(neo4jClient.query(cypher)
                .bind(VIEW_HALF_LIFE_DAYS).to("viewHalfLifeDays")
                .bind(limit).to("limit")
                .fetchAs(Integer.class)
                .mappedBy((typeSystem, record) -> record.get("idProduto").asInt())
            .all());
    }

        private List<Integer> recomendarIdsPorProdutoresFavoritadosViaNeo4j(Integer idUsuario, int limit) {
        String cypher = """
                                MATCH (u:User {id: $userId})-[fp]->(pr:Producer)
                                WHERE type(fp) = 'FAVORITED_PRODUCER'

                                MATCH (p:Product)-[mb]->(pr)
                                WHERE type(mb) = 'MADE_BY'
                                    AND NOT EXISTS {
                                        MATCH (u)-[f]->(p)
                                        WHERE type(f) IN ['FAVORITED', 'VIEWED']
                                    }

                                RETURN p.id AS idProduto
                                ORDER BY idProduto DESC
                                LIMIT $limit
                                """;

        return new ArrayList<>(neo4jClient.query(cypher)
            .bind(idUsuario).to("userId")
            .bind(limit).to("limit")
            .fetchAs(Integer.class)
            .mappedBy((typeSystem, record) -> record.get("idProduto").asInt())
            .all());
        }

    private List<Produto> carregarProdutosOrdenados(List<Integer> idsOrdenados) {
        if (idsOrdenados == null || idsOrdenados.isEmpty()) return List.of();

        Map<Integer, Produto> byId = new HashMap<>();
        for (Produto p : produtoRepository.findAllById(idsOrdenados)) {
            if (p != null && p.getIdProduto() != null) {
                byId.put(p.getIdProduto(), p);
            }
        }

        List<Produto> ordered = new ArrayList<>();
        for (Integer id : idsOrdenados) {
            Produto p = byId.get(id);
            if (p != null) ordered.add(p);
        }
        return ordered;
    }

    private List<Produto> recomendarPorProdutoresBemAvaliados(int limit) {
        var top = avaliacaoRepository.listarTopProdutores(PageRequest.of(0, 3));
        if (top == null || top.isEmpty()) return List.of();

        List<Produto> produtos = new ArrayList<>();
        for (var row : top) {
            if (row == null || row.getIdProdutor() == null) continue;
            produtos.addAll(produtoRepository.findTop4ByProdutor_IdProdutorOrderByDataCriacaoDesc(row.getIdProdutor()));
            if (produtos.size() >= limit) break;
        }

        return produtos.stream().limit(limit).toList();
    }
}
