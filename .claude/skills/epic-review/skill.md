---
name: epic-review
description: "Senior dev epic close-out review for 90phase. Run after all tickets in an epic are closed. Verifies architecture compliance, AC coverage, code quality, and integration health. Outputs a Pass/Fail report with a go/no-go verdict before moving to the next epic."
allowed-tools: Bash(git log --oneline *), Bash(git branch -a), Bash(gh issue list *), Bash(gh issue view *), Bash(gh pr list *), Bash(gh pr view *), Bash(grep -rn * --include="*.kt" *), Bash(grep -rn * domain *), Bash(grep -rn * data *), Bash(grep -rn * presentation *), Bash(find * -type f -name "*.kt"), Bash(./gradlew detekt *), Bash(./gradlew ktlintCheck *), Bash(./gradlew test *), Bash(./gradlew build *), Glob, Grep, Read
---

# Epic Review — 90phase

You are acting as a **senior Android engineer** performing a close-out review before an epic is marked done and the team moves on. Your job is to find problems now, not after they compound into the next epic.

Be honest. A "pass" that hides real problems is worse than a "fail" that stops the team for a day.

---

## Input

The user invokes `/epic-review` with an optional epic number: `/epic-review 2`

If no number is given, infer the current epic from the active branch name (`90p/PH-<N>-...`) or from the most recent closed milestone on GitHub.

---

## Step 1 — Identify the epic scope

```bash
gh issue list --repo PatNoO/90phase --state closed --json number,title,labels,milestone,body --limit 50
```

```bash
gh issue list --repo PatNoO/90phase --state open --json number,title,labels,milestone,body --limit 50
```

Identify:
- Which milestone corresponds to this epic (e.g. "Epic 2 — Data Layer")
- All tickets (PH-N) that belong to this epic
- Which are closed vs still open
- The acceptance criteria (ACs) for each closed ticket — extract from the issue body

Flag immediately if any tickets in the epic are still open — that is a blocker for the review.

---

## Step 2 — PR and merge audit

For each closed ticket, find its merged PR:

```bash
gh pr list --repo PatNoO/90phase --state merged --json number,title,headRefName,mergedAt,body --limit 50
```

For any PR that has no body or an incomplete PR template — flag it.

Check:
- Every closed ticket has a merged PR (no ticket closed without a PR)
- PR body includes What Was Done, Acceptance Criteria Coverage, and Manual Test Steps sections
- No ticket was closed directly without a PR merge

---

## Step 3 — Architecture compliance

**Android imports inside :domain (hard violation):**
```bash
grep -rn "import android\.\|import com\.google\.firebase\|import androidx\." domain/ --include="*.kt"
```

**Repository methods not returning Result<T>:**
```bash
grep -rn "fun " domain/repositories/ --include="*.kt" | grep -v "Result<"
```

**MutableStateFlow exposed from ViewModel:**
```bash
grep -rn "val.*MutableStateFlow\|val.*MutableState\b" presentation/ --include="*.kt"
```

**GlobalScope or raw Thread usage:**
```bash
grep -rn "GlobalScope\|Thread(" app/ domain/ data/ presentation/ --include="*.kt"
```

**Unchecked casts or Any types in domain/data:**
```bash
grep -rn "as Any\b\|: Any\b\| Any>" domain/ data/ --include="*.kt"
```

**Direct Firebase reads in use cases (Room must be source of truth):**
```bash
grep -rn "firebase\|firestore\|FirebaseFirestore" domain/ --include="*.kt"
```

---

## Step 4 — Code quality scan

**TODO / FIXME left in code:**
```bash
grep -rn "TODO\|FIXME" app/ domain/ data/ presentation/ --include="*.kt"
```

Classify each TODO: is it a known deferral (safe) or a missing implementation (blocker)?

**Debug logs left in production code:**
```bash
grep -rn "Log\.d\|Log\.v\|Log\.i\b" app/ domain/ data/ presentation/ --include="*.kt"
```

**Hardcoded colors in Compose:**
```bash
# Excludes theme/ — that package *defines* the tokens, so its Color(0x...) literals are correct.
# Color.Transparent is also fine (Scaffold containers let the gradient show through).
grep -rn "Color(0x\|Color(\"#" presentation/ app/ --include="*.kt" | grep -v "/theme/\|/build/"
```

**Hardcoded dp/sp values off the spacing scale (anything not 4,8,12,16,24,32,48,64):**
```bash
# Scale is 4/8/12/16/24/32/48/64. theme/ defines the tokens, so it is excluded.
grep -rn "\b[0-9]\+\.dp\b\|\b[0-9]\+\.sp\b" presentation/ app/ --include="*.kt" \
  | grep -v "/theme/\|/build/\|// spacing-ok" \
  | grep -vE "\b(4|8|12|16|24|32|48|64)\.(dp|sp)\b"
```

**Hardcoded user-facing strings in Compose (must be `stringResource`):**
```bash
grep -rn 'text = "\|contentDescription = "' presentation/src/main app/src/main --include="*.kt" \
  | grep -v "/build/" | grep -vE 'text = "[^"]{0,3}"'
```
Short literals (glyph icons like `←`, `✕`, `⚙`) are filtered out. Sample text inside `@Preview`
composables is acceptable; anything rendered to a real user must come from
`values/strings.xml` + `values-sv/strings.xml`.

**Missing Swedish translations:**
```bash
./gradlew lint && grep -c "MissingTranslation" */build/reports/lint-results-debug.html
```

---

## Step 5 — Validation suite

Run all four validation commands in sequence. Each must pass before moving on.

```bash
./gradlew detekt
```

```bash
./gradlew ktlintCheck
```

```bash
./gradlew test
```

```bash
./gradlew build
```

Capture pass/fail and any error output. A failure in any of these is a hard blocker — the epic cannot be closed.

---

## Step 6 — Acceptance criteria coverage check

For each closed ticket in the epic, extract its ACs from the GitHub issue body.

For each AC, answer: **is this verifiably implemented in code?**

Look in the relevant files for the implementation. Do not trust that "the ticket is closed" means the AC is met. Read the code.

Flag any AC that:
- Has no corresponding implementation found in code
- Is partially implemented (e.g. happy path done, error case missing)
- Is marked as a TODO in the PR notes

---

## Step 7 — Integration health check

Think like a senior dev: does the epic's output hold together as a whole?

Check:
- Are Hilt modules correctly providing all new dependencies added in this epic?
- Are new Room entities/DAOs registered in the database class?
- Are new repository implementations bound in the DI module?
- Are there any circular dependencies or missing bindings that would crash at runtime?

```bash
grep -rn "@Database\|@Provides\|@Binds\|@Singleton" app/ data/ --include="*.kt"
```

```bash
grep -rn "RoomDatabase\|AppDatabase" app/ data/ --include="*.kt"
```

---

## Step 8 — Output the report

Print the full report in this exact format:

```
# Epic N Review — <Epic Name>
Date: <today>
Tickets reviewed: PH-X, PH-Y, PH-Z
Generated by: /epic-review

---

## VERDICT: ✅ GO / ❌ NO-GO

<One sentence. Go means: all checks pass, ACs covered, build clean, architecture sound. No-go means: blockers found — list them here.>

---

## 1. Ticket Completeness
PASS / FAIL

- PH-XX: <title> — closed ✅ / still open ❌
- PR found: ✅ / ❌ (no merged PR for this ticket)
- PR template complete: ✅ / ❌

<Issues: list any tickets without PRs, or open tickets that should be closed.>

---

## 2. Architecture Compliance
PASS / FAIL

| Check | Result |
|-------|--------|
| No Android imports in :domain | ✅ / ❌ |
| All repository methods return Result<T> | ✅ / ❌ |
| No MutableStateFlow exposed from ViewModel | ✅ / ❌ |
| No GlobalScope or raw threads | ✅ / ❌ |
| No Firebase reads in use cases | ✅ / ❌ |
| No unchecked Any casts in domain/data | ✅ / ❌ |

<Violations: file:line — what was found and why it's a problem.>

---

## 3. Code Quality
PASS / FAIL

| Check | Result |
|-------|--------|
| No blocker TODOs | ✅ / ❌ |
| No debug logs | ✅ / ❌ |
| No hardcoded colors | ✅ / ❌ |
| No off-scale dp/sp values | ✅ / ❌ |

<Items found: file:line — what was found. Classify each as BLOCKER or WARNING.>

---

## 4. Validation Suite
PASS / FAIL

| Command | Result |
|---------|--------|
| ./gradlew detekt | ✅ PASS / ❌ FAIL |
| ./gradlew ktlintCheck | ✅ PASS / ❌ FAIL |
| ./gradlew test | ✅ PASS / ❌ FAIL |
| ./gradlew build | ✅ PASS / ❌ FAIL |

<Errors: paste relevant error output for any failure.>

---

## 5. Acceptance Criteria Coverage
PASS / FAIL

For each ticket:

**PH-XX — <title>**
- AC1: <text> — ✅ Implemented in <file:line> / ⚠️ Partial / ❌ Not found
- AC2: <text> — ✅ / ⚠️ / ❌

<Summary: N of M acceptance criteria fully covered.>

---

## 6. Integration Health
PASS / FAIL

| Check | Result |
|-------|--------|
| All new entities registered in @Database | ✅ / ❌ |
| All new repos bound in Hilt module | ✅ / ❌ |
| No missing @Provides for new dependencies | ✅ / ❌ |

<Issues: what is missing, what file to fix, what the fix looks like.>

---

## Blockers (must fix before Epic N+1)
<List every FAIL item that must be resolved. If none: "No blockers — safe to proceed.">

1. <blocker description — file:line — what to do>

## Warnings (should fix, not blockers)
<List WARNING items. If none: "No warnings.">

1. <warning description>

---

## Senior Dev Notes
<2–4 sentences. What's the overall quality of this epic's output? Any patterns or habits from the agent that should change? Anything that will cause pain in the next epic if not addressed now?>
```

---

## Output rules

- Never mark a section PASS if you found violations — even minor ones
- A single ❌ in Architecture Compliance or Validation Suite makes the overall verdict NO-GO
- TODOs classified as BLOCKER make the verdict NO-GO
- Be specific — name files, line numbers, and exact issues
- Senior Dev Notes must be honest — if quality is low, say so
- Do not skip sections — if something cannot be checked, say "Could not verify — <reason>"
