package EspressoLinks.demo.service;

import EspressoLinks.demo.dto.AnalyticsResponse;
import EspressoLinks.demo.entity.ClickAnalytics;
import EspressoLinks.demo.entity.UrlMapping;
import EspressoLinks.demo.exception.UrlNotFoundException;
import EspressoLinks.demo.repository.AnalyticsRepository;
import EspressoLinks.demo.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {
    private final AnalyticsRepository analyticsRepository;
    private final UrlRepository urlRepository;

    @Async
    public void logClick(String shortKey) {
        ClickAnalytics click = new ClickAnalytics();
        click.setShortKey(shortKey);
        click.setClickTimestamp(LocalDateTime.now());
        analyticsRepository.save(click);
    }

    public AnalyticsResponse getAnalytics(String shortKey) {
        // 1. Check if URL exists
        UrlMapping url = urlRepository.findByShortKey(shortKey)
                .orElseThrow(() -> new UrlNotFoundException("URL not found for key: " + shortKey));

        // 2. Fetch Click Data
        long totalClicks = analyticsRepository.countByShortKey(shortKey);
        List<LocalDateTime> recentTimestamps = analyticsRepository.findTop10ByShortKeyOrderByClickTimestampDesc(shortKey)
                .stream()
                .map(ClickAnalytics::getClickTimestamp)
                .collect(Collectors.toList());

        // 3. Wrap in DTO
        return AnalyticsResponse.builder()
                .shortKey(shortKey)
                .originalUrl(url.getLongUrl())
                .totalClicks(totalClicks)
                .createdAt(url.getCreatedAt())
                .recentClicks(recentTimestamps)
                .build();
    }
}