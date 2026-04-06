package EspressoLinks.demo.repository;

import EspressoLinks.demo.entity.ClickAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface AnalyticsRepository extends JpaRepository<ClickAnalytics, Long> {

    // Count total clicks for a key
    long countByShortKey(String shortKey);

    // Get recent clicks for the timeline (ordered by newest first)
    List<ClickAnalytics> findTop10ByShortKeyOrderByClickTimestampDesc(String shortKey);
}