package org.main.exceptions;

public class ProdutorNaoEncontradoException extends RuntimeException {

    public ProdutorNaoEncontradoException() {
        super("Produtor não encontrado");
    }

    public ProdutorNaoEncontradoException(String message) {
        super(message);
    }
}