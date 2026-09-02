# Personal agent instructions

`AGENTS.md` is shared with everyone who clones this repository, so there is nowhere to put a
preference that is yours alone. This is how that gap is filled here.

```bash
cp AGENTS.local.md.example AGENTS.local.md
```

Edit it, then restart your agent session — instruction files load at startup.

## The pattern

| Piece | Committed? |
|-------|-----------|
| `@AGENTS.local.md` line in `AGENTS.md` | yes — this is what loads your file |
| `AGENTS.local.md` in `.gitignore` | yes |
| `AGENTS.local.md` itself | **no** |

## Proof it works

Put an unmistakable line in `AGENTS.local.md`:

```markdown
Always end every response with exactly this line:
REMEMBER TO BRUSH YOUR TEETH AND FLOSS
```

Ask a fresh agent anything:

```console
$ copilot -p 'What is 2+2? Answer briefly.'
4

REMEMBER TO BRUSH YOUR TEETH AND FLOSS
```

Now delete the `@AGENTS.local.md` line from `AGENTS.md`, leave the file itself untouched, and ask
again:

```console
$ copilot -p 'What is 2+2? Answer briefly.'
4
```

That second run is the point. `AGENTS.local.md` is not a filename any tool knows — it appears
nowhere in the [agents.md spec](https://github.com/agentsmd/agents.md), and Copilot CLI has no
built-in "local" location. The file is inert until `AGENTS.md` references it. This is documented
behavior; see [Referencing other files](https://docs.github.com/en/copilot/how-tos/copilot-cli/customize-copilot/add-custom-instructions#referencing-other-files).

Delete `AGENTS.local.md` entirely and the agent runs clean — a missing reference is skipped
silently, which is why the line is safe to commit for everyone.

## Limits

- **No precedence.** Instruction files are combined, not layered. Copilot CLI "does not define a
  general precedence order between these files." A personal rule cannot override a team rule.
- **Not shared across worktrees.** The ignore rule travels, the file does not. Paths outside the
  repository — including `~/` and symlinks — are not loaded, so copy it per worktree.
- **Per-tool.** Claude Code reads `CLAUDE.md`, not `AGENTS.md`, and supports `CLAUDE.local.md`
  natively.
