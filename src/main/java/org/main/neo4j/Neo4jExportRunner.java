package org.main.neo4j;

import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("neo4j-export")
public class Neo4jExportRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(Neo4jExportRunner.class);

    private final Neo4jExportService exportService;

    @Value("${agro.neo4j.export.dir:neo4j-export}")
    private String exportDir;

    public Neo4jExportRunner(Neo4jExportService exportService) {
        this.exportService = exportService;
    }

    @Override
    public void run(String... args) throws Exception {
        Neo4jExportService.ExportResultado result = exportService.exportToDirectory(Path.of(exportDir));
        log.info("Neo4j export gerado em: {}", result.outputDir().toAbsolutePath());
        log.info("Arquivos: {}, {}, {}, {}, {}",
                result.usersCsv().getFileName(),
                result.productsCsv().getFileName(),
                result.madeByCsv().getFileName(),
                result.favoritesCsv().getFileName(),
                result.importCypher().getFileName());
        log.info("Dica: copie os CSVs para a pasta import do Neo4j Desktop e rode o import.cypher no Neo4j Browser.");
    }
}
