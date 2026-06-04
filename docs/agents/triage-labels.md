# Triage Labels

The agentic workflow uses five canonical triage roles. This file maps those roles to the labels used in GitHub Issues.

| Canonical role | GitHub label | Meaning |
| --- | --- | --- |
| `needs-triage` | `needs-triage` | A maintainer or instructor needs to evaluate the issue. |
| `needs-info` | `needs-info` | The issue is waiting for more information from the reporter or learner. |
| `ready-for-agent` | `ready-for-agent` | The issue is specific enough for an AI agent to work on with minimal extra context. |
| `ready-for-human` | `ready-for-human` | The issue needs human judgment, teaching, or implementation. |
| `wontfix` | `wontfix` | The issue will not be actioned. |

## Workshop Usage

For training, labels are also a teaching tool:

- Use `needs-triage` to practice turning vague requests into clear tasks.
- Use `needs-info` when the agent should ask for missing context.
- Use `ready-for-agent` only when the issue has a clear goal, constraints, and done criteria.
- Use `ready-for-human` when the lesson requires human design judgment.
- Use `wontfix` to practice closing work respectfully with a reason.

## Rule for Agents

Do not invent new triage labels without being asked. If a label is missing in GitHub, tell the user and suggest creating the canonical label.
