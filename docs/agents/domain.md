# Domain Docs

This repo is a single-context training repo for agentic development. Domain documentation exists to teach agents and learners the same project vocabulary.

## Layout

Use this layout:

```text
/
|-- AGENTS.md
|-- CONTEXT.md
|-- docs/
|   |-- agents/
|   |-- adr/
|   `-- backlog/
`-- src/
```

The repo may not have every file at all times. Missing domain docs are allowed during early exercises.

## Before Exploring

Before making design or architecture changes, read:

- `AGENTS.md`
- `docs/agents/*.md`
- `CONTEXT.md`, if it exists
- relevant ADRs under `docs/adr/`, if they exist
- the relevant backlog item under `docs/backlog/`, if the task came from the local backlog

If `CONTEXT.md` or ADRs do not exist, proceed silently and avoid pretending the project has decisions it has not written down.

## Context Document

Use `CONTEXT.md` for shared vocabulary:

- product or workshop goal
- important domain terms
- terms to avoid
- current architecture overview
- constraints learners should understand

Agents should reuse the names in `CONTEXT.md` when writing issue titles, tests, refactor proposals, and explanations.

## ADRs

Use `docs/adr/` for decisions that should survive a single exercise.

Create an ADR when a decision changes how future agents should work, for example:

- choosing a framework or library
- changing the testing strategy
- defining a persistent module boundary
- deciding how backlog items become GitHub Issues

Do not create an ADR for tiny implementation details that only matter inside one exercise.

## Workshop Rule

When in doubt, keep the learning trail visible: write down the assumption, the decision, or the verification step in the smallest appropriate place.
