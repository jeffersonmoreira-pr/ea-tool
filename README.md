# Application Portfolio MVP

Browser-only strategic mockup for an Application Portfolio catalog. It supports local review of Applications, Vendors, Departments, Business Areas, executive indicators, and catalog quality signals.

## Run locally

Open src/index.html directly in a browser.

No package install, backend, or service startup is required for the MVP. The page loads `src/catalog.js`, `src/app.js`, and `src/styles.css` from the working tree.

## Use the MVP

- Executive Overview: review portfolio totals, TIME/PACE/business-area/lifecycle/criticality/data-handling indicators, catalog quality counts, and executive filters.
- Applications: create, edit, delete, and review Applications with owners, organizational references, lifecycle dates, fit assessments, TIME classification, PACE, criticality, data handling, and verification status.
- Vendors: create, edit, delete, and classify Vendors as internal or external; delete is blocked while a Vendor is referenced by an Application.
- Departments: create, edit, and delete Departments; delete is blocked while a Department is referenced by an Application.
- Business Areas: create, edit, and delete Business Areas; delete is blocked while a Business Area is referenced by an Application.

## Data storage

Data is stored in this browser's local storage under application-portfolio.catalog.v1.

Clearing browser local storage resets the catalog back to the seeded MVP data on the next load.

## Validation roteiro

- [ ] Seed data: open the MVP and confirm the seeded portfolio shows 4 Applications, 3 Vendors, 3 Departments, and 3 Business Areas.
- [ ] Master data CRUD: add, rename, and delete a Vendor, Department, and Business Area; confirm delete is blocked when the record is referenced by an Application.
- [ ] Application CRUD: add, edit, and delete an Application with required owners, references, lifecycle state, fit assessments, PACE, criticality, data handling, and verification fields.
- [ ] Local persistence: refresh the browser after a create or edit and confirm the changed catalog remains visible.
- [ ] Derived TIME: change Business Fit and Tech Fit on an Application and confirm TIME Classification is recalculated from those inputs.
- [ ] Filters: use Executive Overview filters for Department, Vendor, Business Area, Lifecycle Status, TIME Classification, PACE Classification, and Criticality; confirm the Application list narrows with the selected filters.
- [ ] Indicators: after filtering or editing an Application, confirm Executive Overview indicators update for TIME, PACE, Business Area, Lifecycle Status, Criticality, data handling, and catalog quality.

## MVP scope

The MVP scope is defined in `docs/adr/0001-application-portfolio-mvp-scope.md`. This mockup stays focused on strategic Application Portfolio review and stays out of SAM, CMDB, ITSM, contracts, licenses, costs, tech stack, integrations, and environments.
