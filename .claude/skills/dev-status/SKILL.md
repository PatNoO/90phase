---
name: dev-status
description: "Developer situational-awareness briefing for 90phase. Scans GitHub Issues, open PRs, git branches, and codebase red flags to produce a prioritised 'what the human developer needs to do right now' report. Use when you want a status check, project overview, or need to know what needs testing, decisions, or follow-up after agents have worked."
allowed-tools: Bash(git branch -a), Bash(git log --oneline *), Bash(git status), Bash(git rev-parse --abbrev-ref HEAD), Bash(gh issue list *), Bash(gh issue view *), Bash(gh pr list *), Bash(gh pr view *), Bash(find * -type f -name "*.kt"), Bash(grep -r "TODO" *), Bash(grep -r "FIXME" *), Bash(grep -rn * domain *), Bash(grep -rn * --include="*.kt" *), Glob, Grep, Read
---

# Dev Status Briefing — 90phase

You are generating a situational-awareness report for the **human developer** who manages multiple Claude agents working on 90phase (Sleep Cycle Optimizer — native Android app).

The goal: in 5 minutes of reading, the developer knows exactly what needs their attention, what agents have completed, what still needs manual testing, and what decisions are pending.

Run every step. Do not skip.

---

## Step 1 — GitHub Issues: Fetch all ticket states

```bash
gh issue list --repo PatNoO/90phase --state open --json number,title,labels,milestone,assignees,updatedAt --limit 50
```

```bash
gh issue list --repo PatNoO/90phase --state closed --json number,title,labels,milestone,assignees,updatedAt --limit 30
```

For each issue, note: number, title, state (open/closed), labels, milestone, assignees, updatedAt.

Group issues by status signal:
- **Open + assigned** — actively being worked on by an agent
- **Open + unassigned** — queued in backlog, ready to delegate
- **Closed (last 30)** — recently completed, may need manual testing
- **Open with "blocked" or "needs-decision" label** — waiting on developer

---

## Step 2 — Git: Scan branch and PR state

```bash
git branch -a
```

```bash
gh pr list --repo PatNoO/90phase --state open --json number,title,headRefName,createdAt,reviewDecision,isDraft
```

For each open PR:
```bash
gh pr view <number> --repo PatNoO/90phase --json title,state,headRefName,statusCheckRollup,reviews,mergeStateStatus,body
```

Recent commits on main:
```bash
git log --oneline -20 origin/main
```

Identify:
- Feature branches with no open PR (`90p/PH-<N>-...` with no associated PR)
- PRs with failing CI checks (build / detekt / ktlint / test)
- PRs that have been open more than 3 days
- Branches that exist remotely but have no associated PR

---

## Step 3 — Codebase: Android/Kotlin red flag scan

Do not do a full audit — just find agent-left markers and architecture violations.

**TODO / FIXME markers:**
```bash
grep -rn "TODO\|FIXME" app/ domain/ data/ presentation/ --include="*.kt" -l
```

**Android imports inside :domain (architecture violation):**
```bash
grep -rn "import android\.\|import com\.google\.firebase\|import androidx\." domain/ --include="*.kt"
```

**Missing Result<T> — repository methods not returning Result:**
```bash
grep -rn "fun " domain/repositories/ --include="*.kt" | grep -v "Result<"
```

**Unchecked casts or Any types in domain/data layers:**
```bash
grep -rn "as Any\b\|: Any\b\| Any>" domain/ data/ --include="*.kt"
```

**Hardcoded colors in Compose (hex values):**
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

**Debug Log statements left in production code:**
```bash
grep -rn "Log\.d\|Log\.v\|Log\.i\b" app/ domain/ data/ presentation/ --include="*.kt"
```

**MutableStateFlow exposed from ViewModel (should be StateFlow only):**
```bash
grep -rn "val.*MutableStateFlow\|val.*MutableState\b" presentation/ --include="*.kt"
```

**GlobalScope or raw Thread usage:**
```bash
grep -rn "GlobalScope\|Thread(" app/ domain/ data/ presentation/ --include="*.kt"
```

---

## Step 4 — Correlate: What needs the human?

Cross-reference Steps 1–3 to categorise everything.

| Signal | Category |
|--------|----------|
| GitHub issue closed but no merged PR found for that PH-N | Needs Your Test |
| GitHub issue closed and PR is merged | Needs Your Test (manual verification) |
| TODO/FIXME markers in code | Needs Your Attention |
| PR open > 3 days or failing CI | Needs Your Review |
| Open PR awaiting review | Needs Your Review |
| Open issue, unassigned, no schema/auth/Firebase labels | Ready to Delegate |
| Issues with labels: room / firebase / schema / auth / gradle | Needs Your Decision |
| `Any` types, exposed MutableStateFlow, Android imports in domain | Technical Debt |
| Remote branch with no PR, > 5 days old | Paused / Stale |

**Decision labels to watch for:** `room-migration`, `firebase`, `schema`, `auth`, `new-dependency`, `gradle`, `breaking-change`

---

## Step 5 — Output the report

Print the full report in this exact format:

```
# 90phase Dev Status
Date: <today>
Branch: <current branch>
Generated by: /dev-status

---

## 🔴 Needs Your Decision
> Tickets or situations where an agent is blocked or should not proceed without you.
> Typically: Room schema migrations, Firebase schema changes, new Gradle dependencies, auth changes, ambiguous scope.

- PH-XX: <title> — <why a human decision is needed>
  GitHub: #<number>

(If none: "Nothing blocked — agents can proceed.")

---

## 🟡 Needs Your Test
> Features that are closed in GitHub or have merged PRs but haven't been manually tested yet.
> Agents can't run the app on a device — you need to test this yourself.

- PH-XX: <title>
  What to test: <one-line summary of what was built>
  How to test: <specific numbered steps based on the ticket's acceptance criteria>

(If none: "No recently shipped features awaiting manual test.")

---

## 🔵 Needs Your Review
> Open PRs waiting on you.

- PR #XX: <title> (<branch>) — open <N> days
  Status: <CI passing/failing, reviews: approved/pending>
  GitHub: #<issue number>

(If none: "No open PRs waiting for review.")

---

## 🟠 Needs Your Attention
> Code quality signals left by agents: TODOs, FIXMEs, architecture violations, Any types, debug logs, hardcoded colors.
> These are not blockers but should be tracked.

- <file:line> — <what was found and why it matters>

(If none: "No agent debt found.")

---

## 🟢 Ready to Delegate
> GitHub issues that are open, unassigned, and safe to hand to an agent.
> No Room migration, Firebase schema change, new Gradle dep, or auth change required.

- PH-XX: <title> — <label/milestone> — suggested next ticket to implement

(If none: "No safe-to-delegate tickets in backlog.")

---

## ⏸ Paused / Stale
> Branches or issues that appear started but have no recent activity (> 5 days).
> May need to be resumed, closed, or reassigned.

- <branch or PH-XX>: last activity <date>

(If none: "No stale branches or stuck tickets.")

---

## Summary
<3–5 bullet points. What is the overall state of the project? What should the developer do first today?>

- Most urgent action: <one thing>
- Agents can start: <PH-N ticket title or "nothing ready">
- Shipped and awaiting test: <N features>
- Open PRs: <N>
- Backlog depth: <N tickets>
- Architecture violations: <N found or "none">
```

---

## Output rules

- Be specific — name files, PH-N IDs, PR numbers, and line numbers
- Do not invent information — only report what you found in Steps 1–3
- If a section has nothing to report, say so in one line — do not omit the section
- Manual test steps must be concrete and actionable (open the app, tap X, expect Y)
- "Ready to Delegate" tickets must be genuinely safe for an agent — flag any touching Room/Firebase/auth/Gradle as needing your decision first
- Keep the summary tight — the developer reads it first and decides whether to dig into sections
- Architecture violations (Android imports in :domain, exposed MutableStateFlow) are serious — always flag these in Needs Your Attention even if minor
