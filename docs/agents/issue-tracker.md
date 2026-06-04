# Issue Tracker

This repo uses two work-tracking surfaces because it is a training repo:

- GitHub Issues for public, shareable work items.
- `docs/backlog/` for local workshop exercises, examples, and staged practice prompts.

Use GitHub Issues when the work should be visible on the published repository. Use `docs/backlog/` when the work is part of a classroom exercise or a local learning sequence.

## GitHub Repository

`paulocorcino-recv/workshop_v1`

## GitHub Issue Conventions

Run `gh` commands from the repo root so the repository is inferred from `git remote -v`.

- Create an issue: `gh issue create --title "..." --body "..."`
- Read an issue: `gh issue view <number> --comments`
- List issues: `gh issue list --state open --json number,title,body,labels,comments`
- Comment on an issue: `gh issue comment <number> --body "..."`
- Apply a label: `gh issue edit <number> --add-label "..."`
- Remove a label: `gh issue edit <number> --remove-label "..."`
- Close an issue: `gh issue close <number> --comment "..."`

## Backlog Conventions

Use `docs/backlog/` for workshop prompts that should be versioned with the training material.

Backlog items should be small enough for one focused agent session. Prefer this shape:

```markdown
# 001 - Short Learning Goal

## Goal

What the learner and agent should accomplish.

## Starting Context

What files, concepts, or constraints matter.

## Done When

- Observable result 1
- Observable result 2
- Verification step
```

## When a Skill Says "Publish to the Issue Tracker"

Create a GitHub issue unless the user explicitly says this is a local workshop exercise. For workshop exercises, create or update a file under `docs/backlog/`.

## When a Skill Says "Fetch the Relevant Ticket"

- For GitHub: run `gh issue view <number> --comments`.
- For local backlog: read the referenced file under `docs/backlog/`.
