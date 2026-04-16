package org.main.config;

import org.main.exceptions.FeiraNaoEncontradaException;
import org.main.exceptions.ProdutoNaoEncontradoException;
import org.main.exceptions.ProdutorNaoEncontradoException;
import org.main.exceptions.UsuarioNaoEncontradoException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err -> {
            errors.put(err.getField(), err.getDefaultMessage());
        });
        return ResponseEntity.badRequest().body(errors);
    }
    
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<?> handleMaxSizeException(MaxUploadSizeExceededException ex) {
        return ResponseEntity.badRequest().body(
            Map.of("error", "A imagem enviada é muito grande. O limite permitido é de 5MB.")
        );
    }

    @ExceptionHandler({
            UsuarioNaoEncontradoException.class,
            ProdutorNaoEncontradoException.class,
            FeiraNaoEncontradaException.class,
            ProdutoNaoEncontradoException.class
    })
    public ResponseEntity<?> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(404).body(Map.of("error", ex.getMessage()));
    }
}