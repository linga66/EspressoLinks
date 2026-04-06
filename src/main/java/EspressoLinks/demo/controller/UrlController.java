package EspressoLinks.demo.controller;

import EspressoLinks.demo.dto.UrlRequest;
import EspressoLinks.demo.dto.UrlResponse;
import EspressoLinks.demo.service.UrlService;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;
    private final Bucket bucket;

    @Value("${app.base-url}")
    private String baseUrl;

    @PostMapping("/shorten")
    public ResponseEntity<?> shorten(@RequestBody UrlRequest request,
                                     HttpServletRequest httpRequest) {

        // ✅ Rate limiting
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Too many requests!");
        }

        String shortKey = urlService.createShortUrl(request.getLongUrl());

        UrlResponse response = UrlResponse.builder()
                .shortUrl(baseUrl + "/api/v1/url/" + shortKey)
                .originalUrl(request.getLongUrl())
                .createdAt(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{shortKey}")
    public ResponseEntity<Void> redirect(@PathVariable String shortKey) {
        System.out.println("🚀 Request handled by Instance: " + System.getenv("HOSTNAME"));
        String originalUrl = urlService.getOriginalUrl(shortKey);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }
}