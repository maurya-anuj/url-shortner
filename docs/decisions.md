# Design Decisions

## ADR-1: Code Generation Strategy

Decision: short codes are generated from a Postgres sequence (`short_url_code_seq`) and Base62-encoded.

Why: collisions are structurally impossible — the sequence is a monotonic counter and the encoding is a bijection, so no two sequence values ever produce the same code. There's no retry logic and no distributed coordination needed; the database is the single source of truth for the next value.

Trade-off: codes are sequential and therefore enumerable. An attacker can guess adjacent valid codes by incrementing/decrementing a known one. This is acceptable for the scope of this project; a production mitigation would be to XOR the sequence value with a secret before encoding it, which preserves uniqueness while breaking predictability.

Alternative considered: a random 7-character Base62 string with retry-on-collision. This is probabilistically safe at reasonable table sizes, but it requires retry logic and has a theoretically unbounded number of attempts under adversarial or pathological input.

## ADR-2: Duplicate URL Handling

Decision: submitting the same URL twice returns the same short code (the operation is idempotent). This is enforced with a partial unique index on `url_hash` where `is_custom = false`.

Concurrency: the insert uses `INSERT ... ON CONFLICT (url_hash) WHERE is_custom = false DO NOTHING`, which makes the check-and-insert a single atomic statement. There is no race window between checking for an existing row and inserting a new one.

Trade-off: a duplicate submission still burns a sequence number, since the code is generated before the conflict is discovered. Sequences are 64-bit, so the gaps this leaves are harmless.

Custom aliases are exempt from this dedup: the partial index only applies when `is_custom = false`, so the same URL can have any number of custom aliases pointing at it.

## ADR-3: Custom Alias Rules

Decision: custom aliases must match `[0-9A-Za-z_-]`, be 3-16 characters long, and are case-sensitive (no normalisation is applied).

A fixed list of reserved words is blocked to avoid colliding with existing or future routes: `shorten`, `api`, `health`, `admin`, `static`, `favicon.ico`.

A conflict on an already-taken alias returns `409 Conflict` rather than silently redirecting to the existing alias's URL — the caller asked for a specific alias and didn't get it, so the response should say so explicitly.

## ADR-4: URL Validation and Normalisation

Decision: only `http` and `https` schemes are accepted (blocking `javascript:`, `data:`, `file:`, etc.), with a maximum length of 2048 characters.

Normalisation before hashing and storage: lowercase the scheme and host, strip default ports (`:80` for http, `:443` for https), strip the fragment, and collapse a bare root path (`/`) to nothing.

Why strip fragments: fragments are resolved client-side only and are never sent to the server, so `https://example.com/page#a` and `https://example.com/page#b` are the same resource as far as the shortener is concerned — they should map to the same short code rather than being treated as distinct URLs.

## ADR-5: Redirect Status Code

Decision: `GET /{code}` responds with `301 Moved Permanently`.

Trade-off: browsers and CDNs cache 301s aggressively, which makes revoking or repointing a link effectively impossible without a cache-busting workaround for clients that already resolved it. A `Cache-Control: no-cache` header is added to mitigate this during development, so redirects stay observable while testing. A production system serving revocable links would likely use `302 Found` or `307 Temporary Redirect` instead.
