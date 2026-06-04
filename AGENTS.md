# Agentic Development Workshop

This repository is a training environment for learning how to develop software with AI agents. It is intentionally small and explicit: people should be able to inspect the rules, change them, and see how agent behavior changes.

## How Agents Should Work Here

1. Read this file first.
2. Read the relevant files in `docs/agents/` before using an agentic workflow.
3. Keep changes small enough for a learner to review.
4. Explain important decisions in plain language.
5. Prefer issues, backlog items, and ADRs over hidden assumptions.
6. Do not publish secrets, tokens, credentials, or private data.

## Training Principles

- Optimize for learning over speed.
- Make intent visible before making broad changes.
- Use tracer-bullet slices: one useful end-to-end increment at a time.
- Keep commits reviewable and named after the learning goal or issue.
- When a task is ambiguous, state the assumption and keep the implementation reversible.

## Agent skills

### Issue tracker

Public, shareable work items live in GitHub Issues for `paulocorcino-recv/workshop_v1`; local workshop exercises may live in `docs/backlog/`. See `docs/agents/issue-tracker.md`.

### Triage labels

This repo uses the default Matt Pocock skills triage label vocabulary for GitHub Issues. See `docs/agents/triage-labels.md`.

### Domain docs

This is a single-context training repo: use root `CONTEXT.md` and `docs/adr/` when they exist. See `docs/agents/domain.md`.

## Expected Human Review

For workshop exercises, the human reviewer should check:

- Did the agent understand the goal?
- Is the change small and inspectable?
- Are assumptions written down?
- Are tests or verification steps included when relevant?
- Did the agent preserve unrelated files and user work?
