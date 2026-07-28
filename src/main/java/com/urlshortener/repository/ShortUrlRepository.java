// Spring Data JPA repository for ShortUrl
package com.urlshortener.repository;

import com.urlshortener.model.ShortUrl;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {

  Optional<ShortUrl> findByCode(String code);

  Optional<ShortUrl> findByUrlHash(String urlHash);

  long countByUrlHash(String urlHash);
}
