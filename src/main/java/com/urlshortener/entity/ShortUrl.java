// JPA entity mapping to the short_url table
package com.urlshortener.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "short_url")
@Getter
@Setter
@NoArgsConstructor
public class ShortUrl {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 16)
  private String code;

  @Column(name = "long_url", nullable = false)
  private String longUrl;

  @Column(name = "url_hash", nullable = false, length = 64)
  private String urlHash;

  @Column(name = "is_custom", nullable = false)
  private boolean custom;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;
}
