# url-shortener

A URL shortener service built with Spring Boot and Postgres.

## Prerequisites

- Java 21
- Maven 3.9+
- Docker & Docker Compose

## Quick Start

```bash
docker-compose up -d
mvn spring-boot:run
```

Flyway runs the schema migration automatically on startup, so the database is ready as soon as the app is up.

```bash
curl -X POST http://localhost:8080/shorten \
  -H "Content-Type: application/json" \
  -d '{"url": "https://example.com"}'

curl -i http://localhost:8080/<code>
```

## API Reference

### POST /shorten

Creates a short code for a URL, optionally with a custom alias.

Request:

```json
{
  "url": "https://example.com",
  "customAlias": "my-link"
}
```

`customAlias` is optional. Omit it (or leave it blank) to get a generated code.

Response (`201 Created`):

```json
{
  "shortCode": "my-link",
  "shortUrl": "http://localhost:8080/my-link",
  "originalUrl": "https://example.com"
}
```

Status codes:

- `201 Created` — short URL created (or the existing one, for a duplicate non-custom URL)
- `400 Bad Request` — missing/invalid URL, blocked scheme, alias fails validation, or alias is a reserved word
- `409 Conflict` — the requested custom alias is already taken

### GET /{code}

Redirects to the original URL.

Status codes:

- `301 Moved Permanently` — redirects via the `Location` header (see [docs/decisions.md](docs/decisions.md) for the caching trade-off)
- `404 Not Found` — no URL registered for that code

### GET /health

Returns `200 OK` with `{"status": "up"}`, for container/orchestration health checks.

## Running Tests

```bash
mvn verify
```

Integration and concurrency tests use Testcontainers to start their own disposable Postgres instance — no manual database setup is needed. Docker must be running for those tests; the pure unit tests (`Base62Codec`, `UrlValidator`, `AliasValidator`, `UrlShortenService`) don't require it.

## Design Decisions

Codes are generated from a Postgres sequence encoded as Base62, duplicate URLs are deduplicated via a partial unique index, and custom aliases are validated against a charset, length, and reserved-word list. See [docs/decisions.md](docs/decisions.md) for the full set of ADRs and their trade-offs.

## Project Structure

```text
src/main/java/com/urlshortener/
├── controller/     REST endpoints (shorten, redirect, health)
├── service/        Business logic (UrlShortenService)
├── repository/     Spring Data JPA repository
├── model/          JPA entity (ShortUrl)
├── dto/            Request/response payloads
├── exception/      Custom exceptions and the global exception handler
└── util/           Base62 codec, URL validation, alias validation
```
