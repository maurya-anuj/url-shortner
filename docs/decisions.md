# Design Decisions

## Code Generation Strategy

## Duplicate URL Handling
* On Duplicates url inputs the system checks for 


## Custom Alias Rules

## URL Validation Rules

## Redirect Caching

* The `GET /{code}` redirect returns `Cache-Control: no-cache` alongside the 301. A "permanent" redirect
  is normally cacheable indefinitely by browsers, which would make it impossible to observe changes
  (or re-test the redirect) without clearing browser cache. Trading away that caching benefit keeps
  the redirect behavior observable during development/testing; revisit before relying on CDN/browser
  caching for production traffic volumes.
