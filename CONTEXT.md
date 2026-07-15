# Application Portfolio

This context defines the language for a lightweight application portfolio catalog used to give managers and directors a strategic view of the application landscape.

## Language

**Application**:
A business-recognized system or product in the organization's application landscape, tracked with ownership, organizational context, vendor relationship, and strategic classifications. Each Application has a required unique name and a required short description within the portfolio catalog.
_Avoid_: IT service, software asset, technical component, module, microservice, database, integration

**Application Alias**:
An optional alternative name for an Application, used when internal, commercial, historical, or commonly used names differ from the official catalog name. An Application may have multiple aliases.
_Avoid_: duplicate application, application code, product version

**Application URL**:
An optional primary URL where an Application can be accessed or recognized.
_Avoid_: endpoint inventory, integration URL, environment URL

**Lifecycle Status**:
The current portfolio lifecycle state of an Application, such as planned, active, retiring, or retired. It records whether a lifecycle decision is underway or completed, while TIME Classification remains an analytical signal.
_Avoid_: operational state, deployment status, support phase

**Planned Date**:
The required target date for an Application whose Lifecycle Status is planned.
_Avoid_: tentative idea date, project schedule, roadmap milestone

**Retirement Date**:
The required target or official date for an Application whose Lifecycle Status is retiring or retired.
_Avoid_: project deadline, decommissioning task date, support end date

**Business Owner**:
The required person accountable for the business value, usage, and strategic relevance of an Application, recorded with a required name and optional email rather than a separate person record.
_Avoid_: user, requester, sponsor

**Tech Owner**:
The required person accountable for the technical stewardship and technology-facing decisions of an Application, recorded with a required name and optional email rather than a separate person record.
_Avoid_: support team, system administrator, infrastructure owner

**Business Fit**:
A manually entered 1-to-5 assessment of how well an Application satisfies business needs and how much adaptation is needed, based on objective criteria agreed with management outside the catalog. The levels are: 1 does not fit, 2 fits little, 3 fits partially with customizations, 4 fits well with configuration or tool-built flows, and 5 fits fully out of the box.
_Avoid_: business score, user satisfaction, capability coverage

**Tech Fit**:
A manually entered low, medium, or high assessment of how technically healthy and sustainable an Application is, based on an assessment process outside the catalog. High means supported and low-risk technology, medium means known but manageable technical limitations, and low means relevant technical risk such as obsolescence, weak maintainability, lack of support, fragile dependencies, or significant difficulty evolving.
_Avoid_: technology score, stack inventory, infrastructure status

**TIME Classification**:
A derived portfolio signal for an Application based on Business Fit and Tech Fit, using tolerate, invest, migrate, or eliminate. Invest indicates an Application is technically and business-wise suitable for evolution subject to business case, tolerate requires case-by-case assessment, migrate is mostly a candidate for discontinuation or technical movement, and eliminate is mostly a candidate for discontinuation.
_Avoid_: manual recommendation, definitive decision, disposition, action status

**Fit Band**:
The normalized low, medium, or high band used to compare Business Fit and Tech Fit for TIME Classification; Business Fit scores 1-2 are low, 3 is medium, and 4-5 are high.
_Avoid_: weighted score, maturity level, ranking

**PACE Classification**:
A strategic classification assigned to an Application by architecture and IT leadership as a system of record, system of differentiation, or system of innovation. Unclassified is used when the classification is pending and should be visible as catalog work to complete.
_Avoid_: automatic score, delivery phase, application tier

**Catalog Quality Gap**:
A visible indication that portfolio information is incomplete or not yet verified, such as unknown data handling or unclassified PACE.
_Avoid_: validation error, missing required field, defect

**Catalog Quality Measure**:
A simple count of Applications with complete or verified information compared with the total catalog, such as 25 of 100 Applications.
_Avoid_: weighted data quality score, audit result, maturity assessment

**Executive Portfolio Indicator**:
A simple count that summarizes the Application landscape for management, such as totals by TIME, PACE, Business Area, Lifecycle Status, Criticality, data handling, or catalog quality.
_Avoid_: complex dashboard, financial KPI, operational SLA

**Information Status**:
The trust state of an Application's catalog information, using draft, verified, or needs review.
_Avoid_: approval status, workflow state, data quality score

**Last Verification Date**:
The date when an Application's catalog information was last reviewed for accuracy; it is required when Information Status is verified.
_Avoid_: last modified date, audit timestamp, approval date

**Diagnostic URL**:
An optional URL that points to the diagnostic document supporting an Application's assessments and classifications.
_Avoid_: attachment, document repository, evidence record

**Vendor**:
The uniquely named organization identified as responsible for supplying or internally owning an Application; every Application must have one Vendor so internal ownership is explicit rather than missing.
_Avoid_: supplier contract, license provider, procurement record

**Internal Vendor**:
A Vendor controlled by the organization itself, used for Applications whose lifecycle and development are fully managed internally.
_Avoid_: no vendor, unknown vendor, internal team

**Department**:
The uniquely named primary organizational area associated with an Application in the portfolio catalog.
_Avoid_: business capability, business process, cost center, stakeholder group

**Business Area**:
A uniquely named broad business category used to group Applications for strategic portfolio analysis, such as finance, operations, or reservoirs.
_Avoid_: department, business capability, business process

**Referenced Master Data**:
A Vendor, Department, or Business Area used by an Application; referenced master data cannot be removed while it is still in use.
_Avoid_: inactive record, archive, soft delete

**Criticality**:
A low, medium, or high assessment of the business impact if an Application is unavailable or fails to support its intended use. High criticality affects critical operations, revenue, safety, compliance, or executive decision-making; medium has meaningful but bounded impact; low has limited or locally tolerable impact.
_Avoid_: priority, severity, incident impact

**Personal Data Handling**:
A yes, no, or unknown indication that an Application handles personal data; unknown is used to make missing knowledge visible as a portfolio risk.
_Avoid_: privacy assessment, LGPD inventory, data catalog

**Sensitive Business Data Handling**:
A yes, no, or unknown indication that an Application handles business-sensitive data; unknown is used to make missing knowledge visible as a portfolio risk.
_Avoid_: information classification, data loss prevention, data catalog

**Catalog User**:
A person recorded locally with a Role so the catalog can authorize what they may do, authenticated either through the organization's Single Sign-On identity provider or, when SSO is not available or applicable, through Local Login.
_Avoid_: account, member, employee record

**Local Login**:
A password-based sign-in path for Catalog Users who cannot authenticate through the organization's Single Sign-On identity provider, used for break-glass emergency access and for external partner users without a corporate identity; a Local Login account is created and managed by an Admin, never self-registered.
_Avoid_: fallback auth, backup login, guest account

**Access Scope**:
The explicit set of Departments and/or Business Areas a Catalog User is allowed to see, regardless of how they authenticate; a Catalog User with no Access Scope configured cannot see any Applications, Departments, or Business Areas until an Admin assigns one. Access Scope does not apply to the Admin Role, which always sees the full catalog, and does not apply to Vendors, which remain visible to everyone. Access Scope governs visibility only, not editing.
_Avoid_: permission, visibility filter, tenant, data partition

**Edit Permission**:
An explicit, per-record grant that authorizes a specific Catalog User with the Editor Role to edit one specific Application, Vendor, Department, or Business Area; only an Admin can grant or revoke an Edit Permission. The Editor Role is a prerequisite — Edit Permission only narrows which specific records an Editor may change, it does not grant editing to a Viewer.
_Avoid_: ACL, ownership, ACL entry, record-level permission

**Role**:
The local authorization level assigned to a Catalog User: Viewer (read-only), Editor (create, edit, and delete Applications and master data), or Admin (Editor plus managing Catalog Users and Roles).
_Avoid_: permission, group, access level, claim
