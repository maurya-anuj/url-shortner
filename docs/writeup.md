# Writeup

## Question 1
#### What did you ask the AI to do, and what did you write or decide yourself? 

I drove this project class-by-class rather than asking for the whole thing at once: Base62 codec, URL validator, alias validator, JPA entity/repository, DTOs and the global exception handler, the service layer, the controller, then the test suites (unit, Mockito, integration, concurrency), and finally the docs. For anything with a concrete algorithm or a well-defined contract — Base62 encode/decode, URL normalisation rules, the alias charset/reserved-word list, the ADR content in `decisions.md` — I specified the exact behavior (edge cases, status codes, method signatures, even error messages in some cases) and had the AI produce the implementation and tests against that spec.

What I decided myself, up front, was the shape of the system: Postgres sequence + Base62 for codes, a partial unique index for dedup instead of an application-level check-then-insert, `INSERT ... ON CONFLICT DO NOTHING` for the concurrency-safe path, and 301 vs 302 for the redirect. Those were product/architecture calls I made before writing any prompt; the AI's job was correct implementation and catching the edge cases I didn't spell out.

## Question 2
#### Where did you override, correct, or throw away the AI’s output — and why?

The clearest override was mid-integration-test-debugging: when the Testcontainers/Docker setup started failing, the AI began poking at `docker context ls` / `docker context inspect` to root-cause it. I cut that off and told it to stop digging into the environment and just retry — I didn't want it going down a rabbit hole rewriting Docker config on my machine for what was clearly a library/daemon version mismatch, not a code problem.

The other correction was really the AI catching its own earlier mistake, which I then confirmed rather than reversed: a few tasks in, it created `model.ShortUrl` to replace an existing `entity.ShortUrl` stub but left the old one in place, reasoning it was "unused, so harmless." I flagged that as a duplication I wanted resolved, said "yes" to deleting it, and later — during the final review pass — it turned out that file wasn't harmless at all: both classes were `@Entity`-annotated, so Hibernate picked up both and crashed on startup with a duplicate-entity-name error. Good thing it was already gone by then, and the AI updated its own understanding (and its saved notes) that "no imports reference it" isn't proof an annotated class is dead.

## Question 3
#### The two or three biggest trade-offs you made, and the alternatives you considered.

**Sequential codes vs. random codes.** Codes come from a Postgres sequence encoded as Base62, which makes collisions structurally impossible and needs no retry logic — but it also means codes are enumerable/guessable. I considered random 7-character Base62 with retry-on-collision, which is probabilistically safer but adds retry logic and an unbounded worst case. I went with the sequence and noted XOR-with-a-secret as the production fix if guessability ever actually matters.

**301 vs 302/307 for the redirect.** A permanent redirect is the "correct" semantic for a URL shortener, but it means browsers and CDNs will cache it aggressively, which makes a link effectively impossible to revoke or repoint once a client has resolved it once. I kept 301 but added `Cache-Control: no-cache` as a stopgap, and noted in the ADR that a revocable-links product would want 302 or 307 instead.

**Dedup burns a sequence value on every duplicate submission.** Because the code is generated before the `ON CONFLICT` check runs, a duplicate URL still consumes a sequence number even though the row isn't inserted. The alternative — check for an existing hash before generating a code — reopens the race window that `INSERT ... ON CONFLICT DO NOTHING` was specifically chosen to close. I accepted the gap; a 64-bit sequence isn't going to run out from wasted values.

## Question 4
#### What’s missing, or what you’d do with another day? 

No expiration or ownership model — every short URL lives forever and anyone can create one, but nobody can list, revoke, or reclaim theirs. With more time I'd add at minimum a way to delete/expire a link, and probably a lightweight API key or account model so aliases belong to someone.

No rate limiting on `/shorten`, which matters more once custom aliases and the reserved-word list exist — right now anything is submit-until-you-get-a-free-alias. I'd add per-IP or per-key throttling before this went anywhere near production traffic.

The sequential-code guessability trade-off from the ADR was never actually mitigated — I noted the XOR-with-a-secret approach as the fix but didn't implement it. That'd be close to the top of the list.

Finally, the Testcontainers-based integration and concurrency tests don't run in this dev environment at all (a Docker Desktop/`docker-java` version mismatch, not a bug in the tests) — they're correct and compile clean, but I never got a green run of them locally. I'd want CI to actually execute them, and I'd want to track down or work around the local Docker incompatibility rather than relying on manual `docker run` to sanity-check schema/startup issues by hand, which is what I ended up doing for the final review pass.
