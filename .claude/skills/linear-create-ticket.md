---
name: linear-create-ticket
description: "Create a Linear ticket end-to-end: classify type, resolve team/project/labels, draft title and description from the canonical template, then post the ticket. Use when asked to create, open, or add a Linear ticket (e.g. 'Create a ticket for …')."
argument-hint: "Short description of what the ticket is for"
allowed-tools: mcp__linear__list_teams, mcp__linear__list_projects, mcp__linear__list_issue_labels, mcp__linear__list_issue_statuses, mcp__linear__save_issue, mcp__linear__list_users, mcp__linear__list_issues
disable-model-invocation: false
---

# Create Linear Ticket — 90phase

Use this skill to create a well-structured Linear ticket following 90phase conventions.

The request is: `$ARGUMENTS`

---

## Step 1 — Classify the ticket

| Type | When | Example title |
|------|------|---------------|
| `feature` | New user-facing screen, composable, or use case | `Implement sleep calculator screen` |
| `bug` | Something broken or incorrect | `Fix notification not firing at 18:00` |
| `chore` | Technical work, no user-visible change | `Set up Hilt DI modules for data layer` |
| `spike` | Time-boxed investigation | `Spike: Evaluate WorkManager vs AlarmManager for exact triggers` |

Always use the word `ticket` (not "issue" or "task").

---

## Step 2 — Resolve team and project

Use `mcp__linear__list_teams` to get available teams.
Use `mcp__linear__list_projects` to look up the right project.

Default team: **90phase**
Default project: **Sleep Cycle Optimizer** — always assign every ticket here unless told otherwise.

---

## Step 2b — Determine the next SP number

Every ticket title must be prefixed with `SP-<N>` (e.g. `SP-05 Implement sleep calculator screen`).

To find the correct next number:

1. Call `mcp__linear__list_issues` with `project: "Sleep Cycle Optimizer"`, `limit: 250`, `orderBy: "createdAt"`
2. Scan all returned issue titles for the pattern `SP-(\d+)` at the start of the title
3. Take the highest number found — call it `MAX`
4. The next SP number is `MAX + 1`

Use this number to prefix the ticket title: `SP-<MAX+1> <drafted title>`
Use this number in the suggested branch name: `90p/SP-<MAX+1>-short-description`

If no SP-prefixed titles are found, start from `SP-1`.

---

## Step 3 — Resolve labels and statuses

Use `mcp__linear__list_issue_labels` to find correct label IDs matching:
- **Type**: feature / bug / chore / spike
- **Layer**: presentation / domain / data / infra
- **Module**: calculator / history / notifications / settings / onboarding / discovery / shared
- **Milestone**: MVP / Post-MVP (default to MVP)

Use `mcp__linear__list_issue_statuses` to confirm the "Backlog" status ID.

---

## Step 4 — Draft the ticket fields

### Title

- Feature: `SP-<N> <verb> <subject>` — e.g. `SP-05 Implement sleep calculator screen`
- Bug: `SP-<N> Fix <what's broken>` — e.g. `SP-12 Fix daily notification not triggering on Android 14`
- Chore: `SP-<N> <technical task>` — e.g. `SP-08 Configure Hilt modules for data layer`
- Spike: `SP-<N> Spike: <question>` — e.g. `SP-11 Spike: Evaluate exact alarm permission strategy`

### Description — Feature template

```markdown
### Context
<Why does this feature exist? What user problem does it solve?>

### Acceptance Criteria
- [ ] AC1: <Specific, testable outcome>
- [ ] AC2: <Specific, testable outcome>
- [ ] AC3: <Specific, testable outcome>

### Scope Boundary
In scope:
- <What is included>

Out of scope:
- <What is explicitly not included>

### Design Reference
- Follows Material3 theming — use `MaterialTheme.colorScheme`, `MaterialTheme.typography`, `MaterialTheme.shapes` only
- Relevant screen: <Calculator / History / Settings / Onboarding / Discovery>
- Dark mode must be verified

### Technical Notes
- Module: <calculator / history / notifications / settings / onboarding / discovery / shared>
- Layer(s) touched: <Presentation only / Presentation + Domain / Multi-layer / Data only>
- ViewModel state: <StateFlow exposed, MutableStateFlow kept private>
- New Room schema change: <Yes — describe migration / No>
- New Firebase schema change: <Yes — describe / No>
- New Gradle dependency: <Yes — name and reason / No>
- Domain purity check: <No Android/Firebase imports in :domain / N/A>
- Dependencies: <Other SP ticket IDs or "none">

### Edge Cases to Handle
- [ ] Loading state (show skeleton or progress indicator)
- [ ] Empty state (no sleep logs yet)
- [ ] Error state (Room failure, Firebase sync failure)
- [ ] Dark mode
- [ ] Back navigation (predictive back gesture on Android 14+)
- [ ] <Feature-specific edge case>
```

### Description — Bug template

```markdown
### What Happens
<Actual behaviour>

### What Should Happen
<Expected behaviour>

### Steps to Reproduce
1. <Step 1>
2. <Step 2>
3. <Step 3>

### Environment
- Device: <e.g. Pixel 7, Samsung Galaxy A54>
- Android version: <e.g. Android 14 / API 34>
- App version / build: <e.g. debug build, commit SHA>
- Screen: <e.g. Calculator, History>

### Logs / Stack Trace
<Paste Logcat output or "Not yet captured">
```

### Description — Chore template

```markdown
### Context
<Why is this technical work needed? What does it enable?>

### Done When
- [ ] <Concrete, verifiable outcome>
- [ ] <Concrete, verifiable outcome>

### Technical Notes
- <Relevant details, constraints>
- Touches Room schema: <Yes — migration required / No>
- Touches Firebase schema: <Yes — describe / No>
- New Gradle dependencies: <Yes — name and reason / No>
- Domain purity maintained: <Yes / N/A>
```

### Description — Spike template

```markdown
### Question to Answer
<Specific question this spike must resolve>

### Time Box
<Maximum time, e.g. 2 hours>

### Output
- [ ] Written findings posted as comment on this ticket
- [ ] Recommendation with pros/cons
- [ ] Follow-up tickets created
```

### Priority

| User says | Priority |
|-----------|----------|
| critical / urgent / blocking | Urgent (1) |
| high / important | High (2) |
| normal / medium (default) | Medium (3) |
| low / nice-to-have | Low (4) |

---

## Step 5 — Create the ticket

Call `mcp__linear__save_issue` with:

```
title        → drafted title (step 4)
description  → drafted description (step 4)
team         → 90phase
project      → Sleep Cycle Optimizer
labels       → resolved label IDs (step 3)
state        → "Backlog"
priority     → mapped priority number (step 4)
```

---

## Step 6 — Report back

```
✅ Ticket created: SP-<N> (<LINEAR-ID>) — <Title>
🔗 <Linear URL>

Type: <feature / bug / chore / spike>
Priority: <priority>
Labels: <labels>
Module: <module>

Suggested branch: 90p/SP-<N>-short-description
```

---

## Branching logic

| Condition | Action |
|-----------|--------|
| Presentation-only scope | Set Layer to `Presentation only`, note fake preview data + `// TODO: wire to ViewModel` |
| Bug report | Use bug template, include Logcat/stack trace section |
| New Room schema change | Add note: `Schema change — write migration before merging` |
| New Firebase schema change | Add note: `Firebase schema change — document in docs/FIREBASE_SCHEMA.md` |
| Domain layer touched | Add note: `Domain purity check — no Android/Firebase imports in :domain` |
| User omits priority | Default to Medium (3) |
| User omits team | Default to 90phase |
| User omits project | Default to Sleep Cycle Optimizer |
| Related ticket exists | Add `Dependencies: SP-XX` in Technical Notes |

---

## Completion checks

- [ ] Ticket type classified, correct labels resolved
- [ ] Title matches imperative format for the type
- [ ] Correct template used with no sections removed
- [ ] Bug tickets include Steps to Reproduce and Environment (Android version + device)
- [ ] Presentation-only tickets note fake data and TODO markers
- [ ] Room or Firebase schema changes flagged
- [ ] Domain purity noted when :domain is touched
- [ ] Ticket posted and Linear URL reported back
- [ ] Next SP number determined from existing tickets
- [ ] Suggested branch name provided
