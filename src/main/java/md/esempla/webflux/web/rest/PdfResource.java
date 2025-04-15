package md.esempla.webflux.web.rest;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import md.esempla.webflux.domain.Post;
import md.esempla.webflux.service.PdfService;
import md.esempla.webflux.service.PostService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/pdf")
public class PdfResource {

    private final PdfService pdfService;
    private final PostService postService;

    public PdfResource(PdfService pdfService, PostService postService) {
        this.pdfService = pdfService;
        this.postService = postService;
    }

    @GetMapping(value = "/generate", produces = MediaType.APPLICATION_PDF_VALUE)
    public Mono<ResponseEntity<byte[]>> generatePdf() {
        return postService
            .findByCriteria(null, null)
            .collectList()
            .map(posts -> {
                Map<String, Object> data = new HashMap<>();
                data.put("posts", posts);
                return data;
            })
            .map(data -> {
                try {
                    return pdfService.generatePdfFromTemplate("templates/sample.mustache", data);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            })
            .map(pdfBytes ->
                ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"posts.pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes)
            );
    }
}
