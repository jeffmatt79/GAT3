package br.ufc.huwc.gat3.exception;

public class PdfFormNotFoundException extends RuntimeException {
    public PdfFormNotFoundException(String message) {
        super(message);
    }
}
