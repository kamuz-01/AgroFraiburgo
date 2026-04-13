package org.main.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

public final class UploadFileValidator {

    private static final Set<String> SUPPORTED_IMAGE_MIMES = Set.of(
            "image/png",
            "image/jpeg",
            "image/gif"
    );

    private UploadFileValidator() {
    }

    public static FileTypeInfo validarImagem(MultipartFile arquivo, String nomeCampo) throws IOException {
        byte[] conteudo = lerConteudo(arquivo, nomeCampo);
        String mimeType = detectarMimeType(conteudo);

        if (mimeType == null || !SUPPORTED_IMAGE_MIMES.contains(mimeType)) {
            throw new IllegalArgumentException(nomeCampo + " deve ser uma imagem válida (PNG, JPG, JPEG ou GIF).");
        }

        return new FileTypeInfo(mimeType, extensaoParaImagem(mimeType));
    }

    public static FileTypeInfo validarPdf(MultipartFile arquivo, String nomeCampo) throws IOException {
        byte[] conteudo = lerConteudo(arquivo, nomeCampo);
        String mimeType = detectarMimeType(conteudo);

        if (!"application/pdf".equals(mimeType)) {
            throw new IllegalArgumentException(nomeCampo + " deve ser um arquivo PDF válido.");
        }

        return new FileTypeInfo(mimeType, ".pdf");
    }

    public static String detectarMimeType(byte[] conteudo) {
        if (conteudo == null || conteudo.length < 4) {
            return null;
        }

        if (ehPng(conteudo)) {
            return "image/png";
        }
        if (ehJpeg(conteudo)) {
            return "image/jpeg";
        }
        if (ehGif(conteudo)) {
            return "image/gif";
        }
        if (ehBmp(conteudo)) {
            return "image/bmp";
        }
        if (ehWebp(conteudo)) {
            return "image/webp";
        }
        if (ehPdf(conteudo)) {
            return "application/pdf";
        }

        return null;
    }

    private static byte[] lerConteudo(MultipartFile arquivo, String nomeCampo) throws IOException {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException(nomeCampo + " é obrigatório.");
        }

        return arquivo.getBytes();
    }

    private static boolean ehPng(byte[] conteudo) {
        return conteudo.length >= 8
                && conteudo[0] == (byte) 0x89
                && conteudo[1] == 0x50
                && conteudo[2] == 0x4E
                && conteudo[3] == 0x47
                && conteudo[4] == 0x0D
                && conteudo[5] == 0x0A
                && conteudo[6] == 0x1A
                && conteudo[7] == 0x0A;
    }

    private static boolean ehJpeg(byte[] conteudo) {
        return conteudo.length >= 3
                && conteudo[0] == (byte) 0xFF
                && conteudo[1] == (byte) 0xD8
                && conteudo[2] == (byte) 0xFF;
    }

    private static boolean ehGif(byte[] conteudo) {
        return conteudo.length >= 6
                && conteudo[0] == 'G'
                && conteudo[1] == 'I'
                && conteudo[2] == 'F'
                && conteudo[3] == '8'
                && (conteudo[4] == '7' || conteudo[4] == '9')
                && conteudo[5] == 'a';
    }

    private static boolean ehBmp(byte[] conteudo) {
        return conteudo.length >= 2
                && conteudo[0] == 'B'
                && conteudo[1] == 'M';
    }

    private static boolean ehWebp(byte[] conteudo) {
        if (conteudo.length < 12) {
            return false;
        }

        boolean riff = conteudo[0] == 'R' && conteudo[1] == 'I' && conteudo[2] == 'F' && conteudo[3] == 'F';
        boolean webp = conteudo[8] == 'W' && conteudo[9] == 'E' && conteudo[10] == 'B' && conteudo[11] == 'P';
        return riff && webp;
    }

    private static boolean ehPdf(byte[] conteudo) {
        return conteudo.length >= 5
                && conteudo[0] == '%'
                && conteudo[1] == 'P'
                && conteudo[2] == 'D'
                && conteudo[3] == 'F'
                && conteudo[4] == '-';
    }

    private static String extensaoParaImagem(String mimeType) {
        return switch (mimeType) {
            case "image/png" -> ".png";
            case "image/jpeg" -> ".jpg";
            case "image/gif" -> ".gif";
            default -> throw new IllegalArgumentException("Tipo de imagem não suportado: " + mimeType);
        };
    }

    public record FileTypeInfo(String mimeType, String extension) {
    }
}