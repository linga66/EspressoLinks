package EspressoLinks.demo.service;

import EspressoLinks.demo.entity.UrlMapping;
import EspressoLinks.demo.repository.UrlRepository;
import EspressoLinks.demo.util.Base62Converter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import this

import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepository repository;
    private final StringRedisTemplate redisTemplate;
    private final AnalyticsService analyticsService;

    @Transactional // Ensures the DB save is committed
    public String createShortUrl(String longUrl) {
        validateUrl(longUrl);

        // 1. Check if longUrl already exists in DB
        Optional<UrlMapping> existing = repository.findByLongUrl(longUrl);
        if (existing.isPresent()) {
            return existing.get().getShortKey();
        }

        Long id = redisTemplate.opsForValue().increment("url_counter");
        if (id == null) id = 1L;

        String shortKey = Base62Converter.encode(id);

        UrlMapping mapping = UrlMapping.builder()
                .longUrl(longUrl)
                .shortKey(shortKey)
                .createdAt(LocalDateTime.now())
                .build();

        // 💡 Use saveAndFlush to force it into the table right now
        repository.saveAndFlush(mapping);

        redisTemplate.opsForValue().set(shortKey, longUrl, Duration.ofDays(7));

        return shortKey;
    }

    public String getOriginalUrl(String shortKey) {
        // Try Cache first
        try {
            String cachedUrl = redisTemplate.opsForValue().get(shortKey);
            if (cachedUrl != null) {
                analyticsService.logClick(shortKey);
                return cachedUrl;
            }
        } catch (Exception e) {
            System.err.println("Redis unavailable: " + e.getMessage());
        }

        // Fallback to DB
        UrlMapping mapping = repository.findByShortKey(shortKey)
                .orElseThrow(() -> new RuntimeException("URL Not Found: " + shortKey));

        // Refill Cache
        try {
            redisTemplate.opsForValue().set(shortKey, mapping.getLongUrl(), Duration.ofDays(7));
        } catch (Exception e) {
            // Log error but don't stop execution
        }

        analyticsService.logClick(shortKey);
        return mapping.getLongUrl();
    }

    private void validateUrl(String url) {
        try {
            URI uri = URI.create(url);
            if (uri.getScheme() == null || (!uri.getScheme().startsWith("http"))) {
                throw new IllegalArgumentException("Invalid URL format");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid URL");
        }
    }
}