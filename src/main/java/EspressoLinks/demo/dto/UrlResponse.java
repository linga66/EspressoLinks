package EspressoLinks.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UrlResponse {
    private String shortUrl;
    private String originalUrl;
    private LocalDateTime createdAt;
}