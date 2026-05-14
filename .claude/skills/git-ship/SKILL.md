---
name: git-ship
description: "Ship a completed 90phase feature branch end-to-end: run validation, sync with main, commit, push, and open a PR. Use when: shipping a finished feature, sending code for review, committing and pushing a completed change, open a PR."
argument-hint: "Optional: PR description notes or ticket ID (e.g. PH-05)"
allowed-tools: Bash(git status), Bash(git diff --stat), Bash(git diff), Bash(git log --oneline *), Bash(git add *), Bash(git commit -m *), Bash(git rev-parse --abbrev-ref HEAD), Bash(git rev-parse --abbrev-ref --symbolic-full-name @{u}), Bash(git fetch origin), Bash(git rebase origin/main), Bash(git push -u origin *), Bash(git push), Bash(gh pr create *), Bash(gh pr view *), Bash(./gradlew detekt), Bash(./gradlew ktlintCheck), Bash(./gradlew ktlintFormat), Bash(./gradlew test), Bash(./gradlew build)
---

# Git Ship — 90phase

Ships the current `90p/PH-<N>-...` feature branch to remote and opens a PR targeting `main`.
Run each step sequentially — do not skip steps.

The optional argument is: `$ARGUMENTS`

---

## Step 1 — Verify the branch

```bash
git rev-parse --abbrev-ref HEAD
```

- If branch is `main`, **stop** — direct pushes not allowed
- Confirm branch follows `90p/PH-<N>-...` convention
- Extract ticket ID (e.g. `PH-05`) — needed for PR

---

## Step 2 — Run validation

```bash
./gradlew detekt
./gradlew ktlintCheck
./gradlew test
./gradlew build
```

All four must pass before committing.

- `detekt` — fix all static analysis violations
- `ktlintCheck` — run `./gradlew ktlintFormat` first to auto-fix formatting, then re-check
- `test` — all unit tests must pass (domain + data layers)
- `build` — must succeed with no Kotlin compilation errors

**If any step fails, stop and fix before continuing.**

---

## Step 3 — Sync with main

```bash
git fetch origin
git rebase origin/main
```

If rebase has conflicts, **stop and report to developer**. Never auto-resolve conflicts.

---

## Step 4 — Commit all changes

1. `git status` and `git diff --stat`
2. If working tree is clean, skip to Step 5
3. Stage specific files — never `git add .` or `git add -A`
4. Commit with correct format:

**First commit on branch:**
```
[claude] (MODEL_NAME) PH-<N> Title Case Description
```

**Subsequent commits:**
```
[claude] (MODEL_NAME) [<type>] imperative description
```

Types: `feat` `fix` `style` `refactor` `chore` `docs` `perf` `test`

- Imperative tense, under 72 characters
- Never include `Co-Authored-By` footer
- Never `--no-verify`

**If commit fails, stop.**

---

## Step 5 — Push the branch

1. Show last 5 commits so user can confirm
2. Verify `[claude]` prefix on all commits — note any that don't match
3. Push:
   - No upstream set → `git push -u origin <branch>`
   - Upstream already set → `git push`
4. Never force-push without explicit user confirmation

**If push fails, stop.**

---

## Step 6 — Open a PR to main

```bash
gh pr create --base main --title "[claude] PH-<N> Title" --body "<body>"
```

### PR Title
```
[claude] PH-<N> Title Case Summary
```

### PR Body
```markdown
## PH-<N> Implementation Complete

Task: <Linear ticket URL>

### Summary
<One or two sentences describing what was built and why.>

### What Was Done
1. **Domain layer** — <item or "Not touched">
2. **Data layer** — <item or "Not touched">
3. **Presentation layer** — <item or "Not touched">
4. **DI / Hilt modules** — <item or "Not touched">
5. **Tests** — <item or "Not touched">

### Acceptance Criteria Coverage
- [ ] AC1: <description>
- [ ] AC2: <description>

### Manual Test Steps
1. <Step 1>
2. <Step 2>

### Notes / Tradeoffs
- <Placeholder data? TODO markers? New Gradle dependencies? Room migration? Firebase schema change? Domain purity impact?>
```

After PR is created, display the PR URL to the user.

---

## Completion checks

- [ ] Branch is not `main`
- [ ] Branch follows `90p/PH-<N>-...` convention
- [ ] `./gradlew detekt` passes
- [ ] `./gradlew ktlintCheck` passes
- [ ] `./gradlew test` passes
- [ ] `./gradlew build` passes
- [ ] Synced with `origin/main` via rebase — no conflicts
- [ ] Specific files staged only — no `git add .`
- [ ] Commit follows `[claude] (MODEL_NAME)` format
- [ ] No hooks skipped (`--no-verify` not used)
- [ ] Push succeeded
- [ ] PR created targeting `main` with template filled
- [ ] PR URL displayed to user
