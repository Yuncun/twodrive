# Microsoft Graph response fixtures

JSON bodies that unit tests serve from MockWebServer in place of `graph.microsoft.com`. Tests load
them as test resources (see `core/data/build.gradle.kts`) and replace the
`https://graph.microsoft.com/v1.0/` prefix in `@odata.nextLink` / `@odata.deltaLink` with the
local server's URL.

| File | Graph call | What it exercises |
|------|------------|-------------------|
| `delta-initial-page-1.json` | `GET /me/drive/root/delta` | first page of a full enumeration, ends in `@odata.nextLink` |
| `delta-initial-page-2.json` | the `nextLink` above | last page, ends in `@odata.deltaLink` |
| `delta-incremental.json` | the `deltaLink` above | rename, move, new file, deleted folder (entry only, no children), deleted file with no `name` or timestamps |
| `error-410-resync-required.json` | an expired `deltaLink` | `410 Gone` with `resyncRequired` |
| `error-503-service-unavailable.json` | any | a server error sync cannot recover from |

## Provenance

These are not captured from a live account (none was available when M2.2 was built). They follow
the documented Graph v1.0 shapes for a OneDrive personal drive:

- [driveItem: delta](https://learn.microsoft.com/graph/api/driveitem-delta?view=graph-rest-1.0):
  paging through `@odata.nextLink`, the final `@odata.deltaLink`, `delta(token='…')` links, a deleted
  folder reported alone, and `410 Gone` / `resyncRequired` for an expired token.
- [driveItem resource](https://learn.microsoft.com/graph/api/resources/driveitem?view=graph-rest-1.0):
  the property set (`cTag`, `eTag`, `fileSystemInfo`, `file.hashes`, `folder.view`,
  `specialFolder`, `photo`, `parentReference.driveType`).
- [Error responses](https://learn.microsoft.com/graph/errors): the `error` / `innerError` body.

Ids use the personal-drive `<DRIVEID>!<n>` form. Replace a fixture with a real, scrubbed response
when one is captured; keep the ids and names the tests assert on.
