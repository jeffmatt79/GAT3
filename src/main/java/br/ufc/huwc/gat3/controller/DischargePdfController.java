package br.ufc.huwc.gat3.controller;

import br.ufc.huwc.gat3.service.DischargePdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/discharges")
@RequiredArgsConstructor
public class DischargePdfController {

    private final DischargePdfService dischargePdfService;

  @GetMapping("/pdf/{id}")
    public ResponseEntity<byte[]> generatePdf(@PathVariable Long id) {

        byte[] pdf = dischargePdfService.generatePdf(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=discharge-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}