package EspressoLinks.demo.repository;

import EspressoLinks.demo.entity.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UrlRepository extends JpaRepository<UrlMapping, Long> {

    Optional<UrlMapping> findByShortKey(String shortKey);

    Optional<UrlMapping> findByLongUrl(String longUrl); // for duplicate check
}