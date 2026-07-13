# 001 - Application Portfolio MVP PRD

## Labels

ready-for-agent

## Problem Statement

Managers and directors need a quick strategic view of the Application landscape, but the first version must stay focused on the essential Application catalog rather than becoming Software Asset Management, CMDB, ITSM, contract management, or a technical landscape repository.

The catalog must help architecture and IT leadership identify which Applications deserve attention through TIME Classification, PACE Classification, lifecycle state, ownership, criticality, data-risk indicators, and catalog quality signals.

## Solution

Build a lightweight web MVP that runs in the browser and stores data locally. The MVP will provide CRUD for Applications and three related master data sets: Vendors, Departments, and Business Areas.

Application records will capture ownership, lifecycle, strategic classifications, fit assessments, criticality, data handling, verification state, and optional diagnostic references. Business Fit and Tech Fit are manually entered based on assessment processes outside the catalog. TIME Classification is derived automatically from Business Fit and Tech Fit. PACE Classification is manually assigned by architecture and IT leadership, with Unclassified used to make pending work visible.

The first screen should provide an executive portfolio view with simple counts by TIME, PACE, Business Area, Lifecycle Status, Criticality, data handling, and catalog quality.

## User Stories

1. As an architect, I want to create an Application with a unique name and short description, so that the portfolio has a clear and unambiguous catalog entry.
2. As an architect, I want to add Application Aliases, so that internal, commercial, historical, or commonly used names can be found.
3. As an architect, I want to optionally register an Application URL, so that a primary access or recognition link is available when relevant.
4. As an architect, I want to select a Lifecycle Status, so that managers can distinguish planned, active, retiring, and retired Applications.
5. As an architect, I want Planned Date to be required for planned Applications, so that planned entries are concrete rather than loose ideas.
6. As an architect, I want Retirement Date to be required for retiring or retired Applications, so that lifecycle decisions are visible and filterable.
7. As an architect, I want to record Business Owner name and optional email, so that business accountability is visible without creating a person master data.
8. As an architect, I want to record Tech Owner name and optional email, so that technical stewardship is visible without creating a person master data.
9. As an architect, I want to assign a Vendor to every Application, so that internal ownership is explicit rather than missing.
10. As an architect, I want to mark Vendors as internal or external, so that homegrown Applications are visible through an Internal Vendor.
11. As an architect, I want to assign one Department to each Application, so that the primary organizational association is clear.
12. As an architect, I want to assign one Business Area to each Application, so that strategic analysis can group Applications by business domain.
13. As an architect, I want to enter Business Fit on a 1-to-5 scale, so that leadership can quickly see business fit and adaptation effort.
14. As an architect, I want Business Fit level 1 to mean does not fit, so that poor business alignment is explicit.
15. As an architect, I want Business Fit level 2 to mean fits little, so that weak business fit is distinguishable from no fit.
16. As an architect, I want Business Fit level 3 to mean fits partially with customizations, so that customization dependence is visible.
17. As an architect, I want Business Fit level 4 to mean fits well with configuration or tool-built flows, so that healthy adaptation is visible.
18. As an architect, I want Business Fit level 5 to mean fits fully out of the box, so that strong fit is visible.
19. As an architect, I want to enter Tech Fit as low, medium, or high, so that technical health is captured without building a tech stack inventory.
20. As an architect, I want TIME Classification to be calculated automatically, so that the portfolio applies a consistent landscape signal.
21. As a manager, I want Invest Applications to be visible, so that evolution candidates can be considered with business case, value proposition, and ROI.
22. As a manager, I want Tolerate Applications to be visible, so that they can be assessed case by case.
23. As a manager, I want Migrate Applications to be visible, so that technical movement or discontinuation candidates stand out.
24. As a manager, I want Eliminate Applications to be visible, so that discontinuation candidates stand out.
25. As an architect, I want to assign PACE Classification manually, so that architecture and IT leadership can classify strategic role without automatic scoring.
26. As an architect, I want PACE to allow Unclassified, so that pending strategic classification is visible as catalog work.
27. As a director, I want to see Applications by PACE Classification, so that systems of record, differentiation, and innovation can be understood at portfolio level.
28. As an architect, I want to assign Criticality, so that business impact is visible without modeling SLA or operational incidents.
29. As a manager, I want high-criticality Applications to be visible, so that critical operations, revenue, safety, compliance, and executive decision-making risks can be spotted.
30. As an architect, I want to mark whether an Application handles personal data as Yes, No, or Unknown, so that data-risk visibility includes missing knowledge.
31. As an architect, I want to mark whether an Application handles sensitive business data as Yes, No, or Unknown, so that business-sensitive data risk is visible.
32. As a director, I want Unknown data handling to be counted, so that catalog gaps and data risks are visible.
33. As an architect, I want to set Information Status as Draft, Verified, or Needs Review, so that the trust state of catalog information is visible.
34. As an architect, I want Last Verification Date to be required when Information Status is Verified, so that verified information has a date.
35. As an architect, I want to optionally add a Diagnostic URL, so that the external diagnostic document supporting assessments can be referenced.
36. As a manager, I want to see Verified Applications compared with the total catalog, so that catalog quality can be tracked simply.
37. As a manager, I want to see Draft and Needs Review Applications compared with the total catalog, so that follow-up work is visible.
38. As a manager, I want to see Unclassified PACE compared with the total catalog, so that strategic classification gaps are visible.
39. As a manager, I want to filter Applications by Department, so that I can inspect a specific organizational area.
40. As a manager, I want to filter Applications by Vendor, so that I can inspect internally owned or supplier-owned Applications.
41. As a manager, I want to filter Applications by Business Area, so that I can inspect a strategic business domain.
42. As a manager, I want to filter Applications by Lifecycle Status, so that planned, active, retiring, and retired Applications can be separated.
43. As a manager, I want to filter Applications by TIME, so that attention areas can be reviewed quickly.
44. As a manager, I want to filter Applications by PACE, so that strategic roles can be reviewed quickly.
45. As a manager, I want to filter Applications by Criticality, so that high-impact Applications can be reviewed quickly.
46. As an architect, I want to create Vendors with unique names, so that Application references are consistent.
47. As an architect, I want to create Departments with unique names, so that organizational references are consistent.
48. As an architect, I want to create Business Areas with unique names, so that strategic grouping is consistent.
49. As an architect, I want referenced Vendors, Departments, and Business Areas to be protected from deletion, so that Application records remain consistent.
50. As a user of the MVP, I want browser-local persistence, so that the mockup can be used without backend infrastructure.
51. As a user of the MVP, I want seed data, so that the portfolio experience can be evaluated immediately.

## Implementation Decisions

- Build a browser-only web MVP using HTML, CSS, and JavaScript.
- Use local browser storage for persistence in the MVP.
- Keep the model centered on Application CRUD and the three related master data sets: Vendor, Department, and Business Area.
- Enforce unique Application names in the catalog.
- Enforce unique names within each master data set.
- Require each Application to reference exactly one Vendor, one Department, and one Business Area.
- Prevent deletion of any Vendor, Department, or Business Area while referenced by an Application.
- Treat Business Owner and Tech Owner as fields on Application, not as person records.
- Make Business Owner and Tech Owner names required, with optional emails.
- Allow multiple Application Aliases.
- Make Application URL and Diagnostic URL optional URL fields.
- Support Lifecycle Status values planned, active, retiring, and retired.
- Require Planned Date when Lifecycle Status is planned.
- Require Retirement Date when Lifecycle Status is retiring or retired.
- Do not require a date for active Applications.
- Make Business Fit a manually entered 1-to-5 value.
- Make Tech Fit a manually entered low, medium, or high value.
- Derive Fit Band from Business Fit: 1-2 is low, 3 is medium, and 4-5 is high.
- Derive TIME Classification from Business Fit Band and Tech Fit.
- TIME matrix:

| Business Fit Band | Tech Fit | TIME |
|---|---|---|
| High | High | Invest |
| High | Medium | Invest |
| High | Low | Migrate |
| Medium | High | Tolerate |
| Medium | Medium | Tolerate |
| Medium | Low | Migrate |
| Low | High | Eliminate |
| Low | Medium | Eliminate |
| Low | Low | Eliminate |

- Make PACE Classification manual with values System of Record, System of Differentiation, System of Innovation, and Unclassified.
- Make Criticality manual with low, medium, and high values.
- Capture Personal Data Handling and Sensitive Business Data Handling as Yes, No, or Unknown.
- Capture Information Status as Draft, Verified, or Needs Review.
- Require Last Verification Date only when Information Status is Verified.
- Provide executive indicators as simple counts rather than complex analytics.
- Include seed data for Vendors, Departments, Business Areas, and Applications.
- Keep future V2 concerns outside the implementation, as recorded in the ADR.

## Testing Decisions

- Test behavior at the highest available seam: the browser-facing Application Catalog, Master Data, Portfolio Classification, Executive Indicators, and Browser Persistence behaviors.
- Tests should verify externally visible behavior: saved records, blocked invalid saves, derived TIME values, protected referenced master data, filters, and indicator counts.
- Tests should not assert internal implementation details such as private helper names or storage key layout unless those become a public migration concern.
- Application Catalog tests should cover required fields, unique Application names, conditional dates, owner fields, optional aliases and URLs, and Information Status rules.
- Master Data tests should cover unique names and preventing deletion while referenced.
- Portfolio Classification tests should cover the full TIME matrix.
- Executive Indicator tests should cover totals by TIME, PACE, Business Area, Lifecycle Status, Criticality, data handling, and catalog quality.
- Browser Persistence tests should cover data surviving reload in local storage for the MVP.
- Manual verification should include opening the app, editing seeded data, adding a new Application, changing fit values, confirming TIME recalculates, and confirming indicators update.

## Out of Scope

- Software Asset Management.
- CMDB.
- ITSM.
- Tech stack inventory.
- Business capabilities.
- Business processes.
- Integrations between Applications.
- Environments such as development, homologation, and production.
- Contracts, licenses, costs, and renewals.
- User counts or reach of use.
- Support or operations ownership.
- Person master data.
- Active Directory or directory integration.
- Diagnostic attachments or standardized diagnostic forms.
- Approval or verification workflow.
- Automatic calculation of Business Fit, Tech Fit, or PACE.
- Financial KPIs, SLA metrics, incident data, and operational reporting.

## Further Notes

- This PRD follows the domain glossary in `CONTEXT.md`.
- The deliberate MVP boundary is recorded in ADR 0001, "Application portfolio MVP scope".
- Future versions may add tech stack, business capabilities, business processes, integrations, CMDB environment data, contracts, licenses, costs, renewals, user counts, support ownership, diagnostic attachments or forms, verification workflows, and directory integration.
