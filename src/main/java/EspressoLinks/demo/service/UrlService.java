package EspressoLinks.demo.service;

import EspressoLinks.demo.entity.UrlMapping;
import EspressoLinks.demo.repository.UrlRepository;
import EspressoLinks.demo.util.Base62Converter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

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

    public String createShortUrl(String longUrl) {

        // ✅ Validate URL
        validateUrl(longUrl);

        // ✅ Check duplicate
        Optional<UrlMapping> existing = repository.findByLongUrl(longUrl);
        if (existing.isPresent()) {
            return existing.get().getShortKey();
        }

        // ✅ Generate ID from Redis
        Long id = redisTemplate.opsForValue().increment("url_counter");

        if (id == null) {
            throw new RuntimeException("Failed to generate ID from Redis");
        }

        String shortKey = Base62Converter.encode(id);

        UrlMapping mapping = UrlMapping.builder()
                .longUrl(longUrl)
                .shortKey(shortKey)
                .createdAt(LocalDateTime.now())
                .build();

        repository.save(mapping);

        // ✅ Cache (longer TTL)
        redisTemplate.opsForValue().set(shortKey, longUrl, Duration.ofDays(7));

        return shortKey;
    }

    public String getOriginalUrl(String shortKey) {

        String cachedUrl = null;

        // ✅ Try Redis (but DON'T fail if it breaks)
        try {
            cachedUrl = redisTemplate.opsForValue().get(shortKey);

            if (cachedUrl != null) {
                analyticsService.logClick(shortKey);
                return cachedUrl;
            }

        } catch (Exception e) {
            // ✅ Just log, don't throw
            System.out.println("Redis unavailable, falling back to DB");
        }

        // ✅ DB fallback (always works)
        UrlMapping mapping = repository.findByShortKey(shortKey)
                .orElseThrow(() -> new RuntimeException("URL Not Found"));

        // ✅ Try to cache again (optional, non-critical)
        try {
            redisTemplate.opsForValue()
                    .set(shortKey, mapping.getLongUrl(), Duration.ofDays(7));
        } catch (Exception e) {
            System.out.println("Could not write to Redis");
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