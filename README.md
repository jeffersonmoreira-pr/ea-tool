# Application Portfolio MVP

Browser-only strategic mockup for an Application Portfolio catalog. It supports local review of Applications, Vendors, Departments, Business Areas, executive indicators, and catalog quality signals.

## Run locally

Open src/index.html directly in a browser.

No package install, backend, or service startup is required for the MVP. The page loads `src/catalog.js`, `src/app.js`, `src/styles.css`, and the generated `src/tailwind.css` from the working tree.

## Backend (Spring Boot skeleton)

A Spring Boot backend is being introduced (see [ADR-0003](docs/adr/0003-backend-compartilhado-java-spring-postgres.md)) to support corporate SSO and shared persistence. At this stage it only serves the existing frontend files from the same origin and exposes a health-check endpoint; it does not yet persist data (see the [issue tracker](docs/agents/issue-tracker.md) for planned next steps).

Requirements: JDK 21+ and Maven (or use the Maven Wrapper if added later).

1. Start supporting services (PostgreSQL and Keycloak) with Docker Compose:

   ```bash
   docker compose up
   ```

2. In another terminal, run the backend from the `backend/` directory:

   ```bash
   cd backend
   mvn spring-boot:run
   ```

3. Open http://localhost:8080/ — the existing catalog UI loads from the same origin the backend serves.
4. Check the health endpoint: http://localhost:8080/actuator/health should respond with `{"status":"UP", ...}`.

PostgreSQL becomes available on `localhost:5432` (db/user/password: `ea_tool`) and Keycloak on http://localhost:8081 (admin/admin), ready for the authentication and persistence work that follows this skeleton.

## Styling (Tailwind, local build)

Styling combines the hand-written `src/styles.css` with a locally built Tailwind stylesheet. Tailwind runs as a build-time tool only — there is no CDN and no runtime network dependency, so the app stays offline/local-first.

- Install tooling once: `npm install`
- Rebuild the CSS after changing markup or `tailwind.config.js`: `npm run build:css`
- Rebuild automatically while editing: `npm run watch:css`

The build reads `src/tailwind.input.css`, scans `src/**/*.{html,js}` for used classes, and writes the purged `src/tailwind.css`, which is committed so the app opens without a build step.

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
