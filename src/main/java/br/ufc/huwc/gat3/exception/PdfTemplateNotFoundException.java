package br.ufc.huwc.gat3.exception;

public class PdfTemplateNotFoundException extends RuntimeException {
    public PdfTemplateNotFoundException(String message) {
        super(message);
    }
}
