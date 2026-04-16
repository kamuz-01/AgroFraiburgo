package org.main.exceptions;

public class ProdutoNaoEncontradoException extends RuntimeException {

    public ProdutoNaoEncontradoException() {
        super("Produto não encontrado");
    }

    public ProdutoNaoEncontradoException(String message) {
        super(message);
    }
}