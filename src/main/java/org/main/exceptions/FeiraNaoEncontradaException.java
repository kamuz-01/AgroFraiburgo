package org.main.exceptions;

public class FeiraNaoEncontradaException extends RuntimeException {

    public FeiraNaoEncontradaException() {
        super("Feira não encontrada");
    }

    public FeiraNaoEncontradaException(String message) {
        super(message);
    }
}