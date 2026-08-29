#!/usr/bin/env bash
# Metric for graphcode: number of open backlog items (direction: minimize).
grep -cE '^- \[ \]' "$(dirname "$0")/../docs/BACKLOG.md"
