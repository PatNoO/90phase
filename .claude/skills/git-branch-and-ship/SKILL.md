---
name: git-branch-and-ship
description: "Move staged changes onto a proper 90p/PH-<N>-... feature branch, then hand off to git-ship. Use when: you have staged work on main or the wrong branch and want to ship it properly (e.g. 'branch and ship PH-05')."
argument-hint: "Ticket ID and optional hint, e.g. 'PH-05 sleep calculator screen'"
allowed-tools: Bash(git status), Bash(git diff --stat), Bash(git diff --name-only *), Bash(git stash), Bash(git stash pop), Bash(git checkout -b *), Bash(git rev-parse --abbrev-ref HEAD), Bash(git branch --show-current)
disable-model-invocation: false
---

# Git Branch and Ship — 90phase

Takes all currently **staged** changes, moves them onto a properly named
`90p/PH-<N>-...` feature branch, then hands off to `git-ship` to validate,
commit, push, and open a PR.

The argument (ticket ID + optional hint) is: `$ARGUMENTS`

---

## Step 1 — Survey staged changes ONLY

```bash
git diff --name-only --cached
```

**Only look at staged files** — ignore everything else in the working tree.
Unstaged and untracked files must never be touched by this skill.

If nothing is staged, **stop immediately**:
```
Nothing is staged. Stage the files you want to ship first with:
  git add <files>
Then re-run this skill.
```

Read the staged file list and understand what changed:
- Which layers are affected? (domain / data / presentation / DI / tests)
- What is the single-sentence description of the work?

---

## Step 2 — Resolve the SP number and branch name

Branch name format for 90phase:
```
90p/PH-<N>-short-kebab-description
```

Rules:
- `PH-<N>` is **required** — take it from `$ARGUMENTS` if provided (e.g. `PH-05`)
- If no SP number was given, **stop and ask the developer** which ticket this work belongs to
- Slug: 2–5 words, kebab-case, describing *what* changed
- Max 50 characters total after the `90p/` prefix
- Always lowercase slug — no ticket number duplication in the slug

Examples:
```
90p/PH-05-sleep-calculator-screen
90p/PH-08-hilt-data-layer-modules
90p/PH-12-fix-daily-notification-trigger
90p/PH-03-room-sleep-log-entity
```

Tell the user the branch name you chose before proceeding.

---

## Step 3 — Stash ONLY the staged changes

```bash
git stash push --staged -m "branch-and-ship: PH-<N>"
```

`--staged` moves only the index (staged files) into the stash.
Unstaged and untracked files stay exactly where they are — untouched.

If the stash fails, **stop and report** — never lose work.

Confirm the staged area is now clear and unstaged files are unchanged:
```bash
git diff --name-only --cached
# Must be empty
git status
# Unstaged/untracked files must still be present
```

---

## Step 4 — Create the branch from main

```bash
git checkout -b 90p/PH-<N>-short-description
```

If the branch already exists, stop and ask the developer whether to reuse it or pick a different name — never auto-append `-2`.

---

## Step 5 — Pop the stash

```bash
git stash pop
```

If pop has conflicts, **stop immediately** and tell the user:
```
Stash pop produced conflicts in: <files>
Resolve the conflicts manually, then run /git-ship to continue.
```
Never auto-resolve stash conflicts.

---

## Step 6 — Verify and hand off to git-ship

Confirm the staged area was restored correctly:
```bash
git diff --name-only --cached
# Must list exactly the files from Step 1 — nothing more, nothing less
```

Then invoke the `git-ship` skill to run validation (detekt, ktlint, tests, build),
commit with the correct `(MODEL_NAME) <type> [PH-<N>]` format, push, and open a PR to `main`.

**Important:** `git-ship` must only commit the staged files.
Never run `git add .` or `git add -A` — the index is already correct.

---

## Completion checks

- [ ] Something was staged before starting
- [ ] SP ticket ID confirmed — branch follows `90p/PH-<N>-...` convention
- [ ] Branch is NOT `main`
- [ ] Stash used `--staged` — unstaged/untracked files untouched
- [ ] Stash popped cleanly — no conflicts
- [ ] Staged area after pop matches exactly the files from Step 1
- [ ] git-ship completed: validated, committed with `(MODEL_NAME) type [PH-<N>]` format, pushed, PR opened to `main`
