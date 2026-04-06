package EspressoLinks.demo.controller;

import EspressoLinks.demo.dto.AnalyticsResponse;
import EspressoLinks.demo.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/{shortKey}")
    public ResponseEntity<AnalyticsResponse> getStats(@PathVariable String shortKey) {
        return ResponseEntity.ok(analyticsService.getAnalytics(shortKey));
    }
}