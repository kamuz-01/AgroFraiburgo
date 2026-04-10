package org.main.neo4j;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.main.enums.StatusConta;
import org.main.enums.TipoUsuario;
import org.main.models.FavoritoProduto;
import org.main.models.Produto;
import org.main.models.Usuario;
import org.main.repository.FavoritoProdutoRepository;
import org.main.repository.ProdutoRepository;
import org.main.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

@Service
public class Neo4jExportService {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final UsuarioRepository usuarioRepository;
    private final ProdutoRepository produtoRepository;
    private final FavoritoProdutoRepository favoritoProdutoRepository;

    public Neo4jExportService(UsuarioRepository usuarioRepository,
                             ProdutoRepository produtoRepository,
                             FavoritoProdutoRepository favoritoProdutoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.produtoRepository = produtoRepository;
        this.favoritoProdutoRepository = favoritoProdutoRepository;
    }

    public ExportResultado exportToDirectory(Path outputDir) throws IOException {
        Files.createDirectories(outputDir);

        Path usersCsv = outputDir.resolve("users.csv");
        Path productsCsv = outputDir.resolve("products.csv");
        Path madeByCsv = outputDir.resolve("made_by.csv");
        Path favoritesCsv = outputDir.resolve("favorites.csv");
        Path importCypher = outputDir.resolve("import.cypher");

        writeUsers(usersCsv);
        writeProducts(productsCsv);
        writeMadeBy(madeByCsv);
        writeFavorites(favoritesCsv);
        writeImportCypher(importCypher);

        return new ExportResultado(outputDir, usersCsv, productsCsv, madeByCsv, favoritesCsv, importCypher);
    }

    private void writeUsers(Path out) throws IOException {
        List<Usuario> users = usuarioRepository.findAll().stream()
                .filter(u -> u != null && (u.getTipoUsuario() == TipoUsuario.CONSUMIDOR || u.getTipoUsuario() == TipoUsuario.MODERADOR))
                .filter(u -> u.getStatusConta() == null || u.getStatusConta() == StatusConta.ATIVO)
                .sorted(Comparator.comparing(Usuario::getIdUsuario))
                .toList();

        try (BufferedWriter w = Files.newBufferedWriter(out, StandardCharsets.UTF_8)) {
            w.write("id,tipo\n");
            for (Usuario u : users) {
                w.write(u.getIdUsuario() + "," + u.getTipoUsuario().name() + "\n");
            }
        }
    }

    private void writeProducts(Path out) throws IOException {
        List<Produto> produtos = produtoRepository.findAll().stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Produto::getIdProduto))
                .toList();

        try (BufferedWriter w = Files.newBufferedWriter(out, StandardCharsets.UTF_8)) {
            w.write("id,nome,idProdutor\n");
            for (Produto p : produtos) {
                Integer idProdutor = (p.getProdutor() != null) ? p.getProdutor().getIdProdutor() : null;
                String nome = safeCsv(p.getNomeProduto());
                w.write(p.getIdProduto() + "," + nome + "," + (idProdutor == null ? "" : idProdutor) + "\n");
            }
        }
    }

    private void writeMadeBy(Path out) throws IOException {
        List<Produto> produtos = produtoRepository.findAll().stream()
                .filter(p -> p != null && p.getProdutor() != null && p.getProdutor().getIdProdutor() != null)
                .sorted(Comparator.comparing(Produto::getIdProduto))
                .toList();

        try (BufferedWriter w = Files.newBufferedWriter(out, StandardCharsets.UTF_8)) {
            w.write("productId,producerId\n");
            for (Produto p : produtos) {
                w.write(p.getIdProduto() + "," + p.getProdutor().getIdProdutor() + "\n");
            }
        }
    }

    private void writeFavorites(Path out) throws IOException {
        List<FavoritoProduto> favoritos = favoritoProdutoRepository.findAll().stream()
                .filter(f -> f != null && f.getId() != null)
                .sorted(Comparator.comparing((FavoritoProduto f) -> f.getId().getIdUsuario())
                        .thenComparing(f -> f.getId().getIdProduto()))
                .toList();

        try (BufferedWriter w = Files.newBufferedWriter(out, StandardCharsets.UTF_8)) {
            w.write("userId,productId,at\n");
            for (FavoritoProduto f : favoritos) {
                String at = (f.getDataFavorito() == null) ? "" : DATE_TIME.format(f.getDataFavorito());
                w.write(f.getId().getIdUsuario() + "," + f.getId().getIdProduto() + "," + at + "\n");
            }
        }
    }

    private void writeImportCypher(Path out) throws IOException {
        String cypher = """
                // Rode este arquivo no Neo4j Browser (Neo4j Desktop) após copiar os CSVs para a pasta 'import' do DB.
                // Ex.: file:///users.csv
                
                CREATE CONSTRAINT user_id IF NOT EXISTS FOR (u:User) REQUIRE u.id IS UNIQUE;
                CREATE CONSTRAINT product_id IF NOT EXISTS FOR (p:Product) REQUIRE p.id IS UNIQUE;
                CREATE CONSTRAINT producer_id IF NOT EXISTS FOR (pr:Producer) REQUIRE pr.id IS UNIQUE;

                // Users
                LOAD CSV WITH HEADERS FROM 'file:///users.csv' AS row
                WITH row WHERE row.id IS NOT NULL AND row.id <> ''
                MERGE (u:User {id: toInteger(row.id)})
                SET u.tipo = row.tipo;

                // Products
                LOAD CSV WITH HEADERS FROM 'file:///products.csv' AS row
                WITH row WHERE row.id IS NOT NULL AND row.id <> ''
                MERGE (p:Product {id: toInteger(row.id)})
                SET p.nome = row.nome;

                // Producers (derivado de products.idProdutor)
                LOAD CSV WITH HEADERS FROM 'file:///products.csv' AS row
                WITH row WHERE row.idProdutor IS NOT NULL AND row.idProdutor <> ''
                MERGE (pr:Producer {id: toInteger(row.idProdutor)});

                // MADE_BY
                LOAD CSV WITH HEADERS FROM 'file:///made_by.csv' AS row
                WITH row WHERE row.productId IS NOT NULL AND row.productId <> '' AND row.producerId IS NOT NULL AND row.producerId <> ''
                MATCH (p:Product {id: toInteger(row.productId)})
                MATCH (pr:Producer {id: toInteger(row.producerId)})
                MERGE (p)-[:MADE_BY]->(pr);

                // FAVORITED
                LOAD CSV WITH HEADERS FROM 'file:///favorites.csv' AS row
                WITH row WHERE row.userId IS NOT NULL AND row.userId <> '' AND row.productId IS NOT NULL AND row.productId <> ''
                MATCH (u:User {id: toInteger(row.userId)})
                MATCH (p:Product {id: toInteger(row.productId)})
                MERGE (u)-[f:FAVORITED]->(p)
                SET f.at = row.at;
                """;

        Files.writeString(out, cypher, StandardCharsets.UTF_8);
    }

    private String safeCsv(String value) {
        if (value == null) return "";
        String v = value.replace("\r", " ").replace("\n", " ").trim();
        // CSV simplificado: troca vírgula por espaço para não quebrar coluna
        return v.replace(",", " ");
    }

    public record ExportResultado(Path outputDir,
                                 Path usersCsv,
                                 Path productsCsv,
                                 Path madeByCsv,
                                 Path favoritesCsv,
                                 Path importCypher) {
    }
}
