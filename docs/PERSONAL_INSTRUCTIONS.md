# Personal agent instructions

Coding agents read `AGENTS.md`, and everything in it is shared with everyone who clones this
repository. That leaves nowhere to put a preference that is yours alone — your own build flavor,
how you want replies written, a habit you want reinforced.

This repository demonstrates a pattern that fills that gap using only documented behavior.

## The pattern

Three pieces:

| Piece | Committed? | Role |
|-------|-----------|------|
| `@AGENTS.local.md` line in `AGENTS.md` | yes | tells the agent to read your file |
| `AGENTS.local.md` in `.gitignore` | yes | keeps everyone's copy out of git |
| `AGENTS.local.md` itself | **no** | your preferences, on your machine only |

To use it:

```bash
cp AGENTS.local.md.example AGENTS.local.md
```

Then edit it. Your next agent session picks it up — instruction files load at session start, so
restart an already-running session.

If you never create the file, nothing changes. A reference to a file that does not exist is
skipped silently, with no error.

## Why the reference line is required

`AGENTS.local.md` is not a filename any tool knows. Copilot CLI has no built-in "local" or
"personal" location, and the name appears nowhere in the [agents.md
specification](https://github.com/agentsmd/agents.md). It reads as familiar because Claude Code has
`CLAUDE.local.md`, which is real in that tool and absent from Copilot CLI.

So the file is inert on its own. The `@AGENTS.local.md` line in `AGENTS.md` is what loads it.

This is documented behavior. From [Adding custom instructions for Copilot
CLI](https://docs.github.com/en/copilot/how-tos/copilot-cli/customize-copilot/add-custom-instructions#referencing-other-files):

> In `.github/copilot-instructions.md`, `AGENTS.md`, or `CLAUDE.md`, use `@` followed by a relative
> path to include another file. Copilot CLI reads the referenced file immediately and supports
> references within referenced files.
>
> Referenced files must remain within the repository, or within the custom instructions directory
> for local instructions. Absolute paths and paths beginning with `~/` are not loaded. File
> references are not expanded in `GEMINI.md` or `*.instructions.md` files.

## Verifying it works

Put one unmistakable instruction in your `AGENTS.local.md`:

```markdown
Always end every response with exactly this line:
REMEMBER TO BRUSH YOUR TEETH AND FLOSS
```

Then ask a fresh agent anything:

```console
$ copilot -p 'What is 2+2? Answer briefly.'
4

REMEMBER TO BRUSH YOUR TEETH AND FLOSS
```

Remove the `@AGENTS.local.md` line from `AGENTS.md`, leave the file itself in place, and ask again:

```console
$ copilot -p 'What is 2+2? Answer briefly.'
4
```

The second run is the one that proves it. Same file, same directory — only the reference removed.

## What this pattern cannot do

**It does not override team rules.** Instruction files are combined, not layered. GitHub's
documentation states that Copilot CLI "does not define a general precedence order between these
files. Avoid conflicting instructions." Write your file as an addition; if it contradicts
`AGENTS.md`, which one wins is undefined.

**It does not follow you across worktrees.** A gitignored file exists only in the checkout where
you created it. The ignore rule is shared, because `.gitignore` is committed — the file is not.
Paths outside the repository are not loaded, so a home-directory file or a symlink cannot stand in.
Copy the file into each worktree.

**It is per-tool.** Copilot CLI reads `AGENTS.md` and expands `@` references in it. Other agents
differ. Claude Code reads `CLAUDE.md`, not `AGENTS.md`, and supports `CLAUDE.local.md` natively.

## Why `.gitignore` and not `.git/info/exclude`

Git offers a second way to hide a file: `.git/info/exclude`, which works the same way but is not
committed. It is the wrong choice here, for two reasons.

It cannot be shared. `.git/info/exclude` lives inside `.git/`, which git never tracks, so it can
never reach GitHub. Every person would have to add the line by hand, and nothing in the repository
would tell them to.

It also removes a protection. Git guards untracked files: if this repository ever committed a file
at a path where you have an untracked one, `git pull` stops with *"Please move or remove them before
you merge."* Once a file is ignored, git treats it as regenerable and overwrites it silently. That
risk is the reason the committed name here is fixed and reserved: `AGENTS.local.md` exists in
`.gitignore` and will never be committed, so nothing can arrive to overwrite yours.
