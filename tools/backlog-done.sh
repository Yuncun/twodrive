#!/usr/bin/env bash
# Exit 0 when the backlog is finished: no unchecked items remain AND the latest CI run on main is green.
# Used as the graphcode goal predicate and by the fallback loop.
set -euo pipefail
cd "$(dirname "$0")/.."
if grep -qE '^- \[ \]' docs/BACKLOG.md; then
  echo "backlog: $(grep -cE '^- \[ \]' docs/BACKLOG.md) items open"; exit 1
fi
conclusion=$(gh run list --branch main --limit 1 --json conclusion --jq '.[0].conclusion' 2>/dev/null || echo unknown)
[ "$conclusion" = "success" ] || { echo "ci: $conclusion"; exit 1; }
echo "backlog empty and CI green"
