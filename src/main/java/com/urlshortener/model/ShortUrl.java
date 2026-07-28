// JPA entity mapping to the short_url table
package com.urlshortener.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "short_url")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShortUrl {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String code;

  @Column(name = "long_url", nullable = false)
  private String longUrl;

  @Column(name = "url_hash", nullable = false)
  private String urlHash;

  @Column(name = "is_custom", nullable = false)
  private boolean isCustom;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @PrePersist
  void onPrePersist() {
    if (createdAt == null) {
      createdAt = Instant.now();
    }
  }
}
