package EspressoLinks.demo.repository;

import EspressoLinks.demo.entity.ClickAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface AnalyticsRepository extends JpaRepository<ClickAnalytics, Long> {

    long countByShortKey(String shortKey);

    List<ClickAnalytics> findTop10ByShortKeyOrderByClickTimestampDesc(String shortKey);
}
