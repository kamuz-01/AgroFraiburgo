package org.Main.models;

import org.junit.jupiter.api.Test;
import org.main.models.DocumentosProdutor;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentosProdutorTest {

    @Test
    void validarCaminhoDocumentoDeveAceitarArquivoDentroDaPastaPrivada() {
        assertThatCode(() -> DocumentosProdutor.validarCaminhoDocumento(
                "/documentos-produtores/77/documento.pdf"))
                .doesNotThrowAnyException();
    }

    @Test
    void validarCaminhoDocumentoDeveRejeitarPathTraversalForaDaPastaPrivada() {
        assertThatThrownBy(() -> DocumentosProdutor.validarCaminhoDocumento(
                "/documentos-produtores/77/../../segredo.pdf"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Caminho de documento inválido.");
    }

    @Test
    void validarCaminhoDocumentoDeveRejeitarCaminhoAbsoluto() {
        assertThatThrownBy(() -> DocumentosProdutor.validarCaminhoDocumento(
                "C:\\segredo\\documento.pdf"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Caminho de documento inválido.");
    }

    @Test
    void validarCaminhoDocumentoObrigatorioDeveRejeitarDocumentoAusente() {
        assertThatThrownBy(() -> DocumentosProdutor.validarCaminhoDocumentoObrigatorio(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Documento obrigatório não informado.");
    }
}
