const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const test = require("node:test");

const catalogApi = require("../src/catalog.js");
const appApi = require("../src/app.js");
const apiClientApi = require("../src/apiClient.js");

const root = path.resolve(__dirname, "..");

function readSource(filePath) {
  return fs.readFileSync(path.join(root, filePath), "utf8");
}

// The real ApplicationPortfolioApiClient (src/apiClient.js) talks to the Spring Boot
// backend over fetch(). For UI-level tests we don't want to stand up a real HTTP
// server, so this mock apiClient delegates to the EXISTING pure catalogApi
// create/update/delete functions (the same functions the backend is faithfully
// porting), wrapped in Promises. Each call operates against a disposable "shadow"
// copy of the record arrays so that catalogApi's push/splice side effects never
// touch the real `catalog` object directly - app.js's own promise handlers are
// responsible for pushing/splicing the real catalog collections on success, exactly
// as they would when talking to the real backend. Record mutations performed via
// Object.assign (update) do touch the underlying shared record object, which is
// fine since it mirrors what the server would report back.
function shadowCatalog(catalog) {
  return {
    vendors: catalog.vendors.slice(),
    departments: catalog.departments.slice(),
    businessAreas: catalog.businessAreas.slice(),
    applications: catalog.applications.slice(),
  };
}

function toPromise(action) {
  try {
    return Promise.resolve(action());
  } catch (error) {
    return Promise.reject(error);
  }
}

function createMockApiClient(catalog) {
  return {
    listVendors: () => Promise.resolve(catalog.vendors.slice()),
    createVendor: (input) => toPromise(() => catalogApi.createVendor(shadowCatalog(catalog), input)),
    updateVendor: (id, input) => toPromise(() => catalogApi.updateVendor(shadowCatalog(catalog), id, input)),
    deleteVendor: (id) => toPromise(() => catalogApi.deleteVendor(shadowCatalog(catalog), id)),

    listDepartments: () => Promise.resolve(catalog.departments.slice()),
    createDepartment: (input) => toPromise(() => catalogApi.createDepartment(shadowCatalog(catalog), input)),
    updateDepartment: (id, input) => toPromise(() => catalogApi.updateDepartment(shadowCatalog(catalog), id, input)),
    deleteDepartment: (id) => toPromise(() => catalogApi.deleteDepartment(shadowCatalog(catalog), id)),

    listBusinessAreas: () => Promise.resolve(catalog.businessAreas.slice()),
    createBusinessArea: (input) => toPromise(() => catalogApi.createBusinessArea(shadowCatalog(catalog), input)),
    updateBusinessArea: (id, input) =>
      toPromise(() => catalogApi.updateBusinessArea(shadowCatalog(catalog), id, input)),
    deleteBusinessArea: (id) => toPromise(() => catalogApi.deleteBusinessArea(shadowCatalog(catalog), id)),

    listApplications: () => Promise.resolve(catalog.applications.slice()),
    createApplication: (input) => toPromise(() => catalogApi.createApplication(shadowCatalog(catalog), input)),
    updateApplication: (id, input) => toPromise(() => catalogApi.updateApplication(shadowCatalog(catalog), id, input)),
    deleteApplication: (id) => toPromise(() => catalogApi.deleteApplication(shadowCatalog(catalog), id)),
  };
}

function createUserStore(initialUsers) {
  const users = initialUsers.map((user) => ({ ...user }));
  const grants = new Map();
  const grantKey = (id, recordType, recordId) => `${id}|${recordType}|${recordId}`;
  return {
    users,
    grants,
    client: {
      listCatalogUsers: () => Promise.resolve(users.map((user) => ({ ...user }))),
      createLocalUser: (input) =>
        toPromise(() => {
          const created = {
            id: `u-${users.length + 1}`,
            name: input && input.name,
            email: input && input.email,
            role: (input && input.role) || "VIEWER",
            loginMethod: "LOCAL",
            scopedDepartmentIds: [],
            scopedBusinessAreaIds: [],
          };
          users.push(created);
          return { ...created };
        }),
      updateCatalogUserRole: (id, role) =>
        toPromise(() => {
          const target = users.find((user) => user.id === id);
          if (!target) {
            throw new Error("Catalog User not found.");
          }
          target.role = role;
          return { ...target };
        }),
      updateCatalogUserAccessScope: (id, scope) =>
        toPromise(() => {
          const target = users.find((user) => user.id === id);
          if (!target) {
            throw new Error("Catalog User not found.");
          }
          target.scopedDepartmentIds = (scope && scope.departmentIds) || [];
          target.scopedBusinessAreaIds = (scope && scope.businessAreaIds) || [];
          return { ...target };
        }),
      listCatalogUserEditPermissions: (id) =>
        toPromise(() =>
          Array.from(grants.values()).filter((grant) => grant.catalogUserId === id),
        ),
      grantCatalogUserEditPermission: (id, grant) =>
        toPromise(() => {
          const entry = {
            catalogUserId: id,
            recordType: grant && grant.recordType,
            recordId: grant && grant.recordId,
          };
          grants.set(grantKey(id, entry.recordType, entry.recordId), entry);
          return entry;
        }),
      revokeCatalogUserEditPermission: (id, recordType, recordId) =>
        toPromise(() => {
          grants.delete(grantKey(id, recordType, recordId));
          return undefined;
        }),
    },
  };
}

function createMemoryStorage(initial = {}) {
  const values = new Map(Object.entries(initial));

  return {
    getItem(key) {
      return values.has(key) ? values.get(key) : null;
    },
    setItem(key, value) {
      values.set(key, String(value));
    },
    removeItem(key) {
      values.delete(key);
    },
    snapshot() {
      return Object.fromEntries(values);
    },
  };
}

function collectText(node) {
  const own = node.textContent || "";
  const childText = (node.children || []).map(collectText).join(" ");
  return `${own} ${childText}`.trim();
}

function findAll(node, predicate) {
  const matches = predicate(node) ? [node] : [];
  for (const child of node.children || []) {
    matches.push(...findAll(child, predicate));
  }
  return matches;
}

function findField(form, name) {
  return findAll(form, (node) => ["INPUT", "TEXTAREA", "SELECT"].includes(node.tagName)).find(
    (field) => field.name === name,
  );
}

function createElement(tagName, ownerDocument) {
  return {
    tagName: tagName.toUpperCase(),
    ownerDocument,
    children: [],
    attributes: {},
    dataset: {},
    className: "",
    textContent: "",
    value: "",
    type: "",
    checked: false,
    id: "",
    name: "",
    append(...nodes) {
      for (const node of nodes) {
        this.children.push(typeof node === "string" ? ownerDocument.createTextNode(node) : node);
      }
    },
    appendChild(node) {
      this.children.push(node);
      return node;
    },
    replaceChildren(...nodes) {
      this.children = [];
      this.textContent = "";
      this.append(...nodes);
    },
    setAttribute(name, value) {
      this.attributes[name] = String(value);
      if (name === "class") {
        this.className = String(value);
      }
      if (name === "id") {
        this.id = String(value);
        ownerDocument.nodesById.set(this.id, this);
      }
    },
    removeAttribute(name) {
      delete this.attributes[name];
    },
    addEventListener(type, handler) {
      this[`on${type}`] = handler;
    },
  };
}

function createDocument() {
  const document = {
    nodesById: new Map(),
    createElement(tagName) {
      return createElement(tagName, document);
    },
    createTextNode(text) {
      return { textContent: String(text), children: [] };
    },
    getElementById(id) {
      return this.nodesById.get(id) || null;
    },
    addEventListener() {},
  };
  document.nodesById.set("app", createElement("main", document));
  return document;
}

// NOTE: This test used to assert the frontend had NO fetch/XMLHttpRequest usage and
// no mention of "backend" anywhere - that was correct for the local-storage-only MVP,
// but is no longer true (or desirable) now that Departments/Vendors/Business Areas
// and Applications are persisted through a Spring Boot backend (see src/apiClient.js).
// The assertions below keep protecting the parts of the architecture that are still
// true: the static shell only references local script/style assets (no absolute
// URLs baked into HTML), apiClient.js exists and is wired up before app.js, and the
// fetch calls it makes use same-origin relative paths with credentials included
// (rather than a hardcoded absolute host:port), which is what keeps the frontend
// deployable behind the backend's static file serving.
test("static shell loads local assets plus the API client, and API calls use relative same-origin URLs", () => {
  const html = readSource("src/index.html");
  assert.match(html, /href="\.\/styles\.css"/);
  assert.match(html, /src="\.\/catalog\.js"/);
  assert.match(html, /src="\.\/apiClient\.js"/);
  assert.match(html, /src="\.\/app\.js"/);
  assert.doesNotMatch(html, /https?:\/\//);

  // apiClient.js must load before app.js so ApplicationPortfolioApiClient exists
  // when app.js's DOMContentLoaded handler calls init(root).
  const apiClientIndex = html.indexOf("apiClient.js");
  const appIndex = html.indexOf("app.js");
  assert.ok(apiClientIndex !== -1 && appIndex !== -1 && apiClientIndex < appIndex);

  const apiClientSource = readSource("src/apiClient.js");
  assert.match(apiClientSource, /credentials:\s*"include"/);
  assert.match(apiClientSource, /"\/api\//);
  assert.doesNotMatch(apiClientSource, /https?:\/\//);
});

test("README documents local execution and final MVP validation", () => {
  const readme = readSource("README.md");

  for (const label of [
    "Seed data",
    "Master data CRUD",
    "Application CRUD",
    "Derived TIME",
    "Filters",
    "Indicators",
  ]) {
    assert.match(readme, new RegExp(`\\b${label}\\b`));
  }

  const source = ["src/index.html", "src/catalog.js", "src/app.js"].map(readSource).join("\n");
  const forbiddenScopeTerms = [
    "SAM",
    "CMDB",
    "ITSM",
    "contracts",
    "licenses",
    "costs",
    "tech stack",
    "integrations",
    "environments",
  ];
  const foundForbiddenTerms = forbiddenScopeTerms.filter((term) =>
    new RegExp(`\\b${term.replace(" ", "\\s+")}\\b`, "i").test(source),
  );
  assert.deepEqual(foundForbiddenTerms, []);
});

test("seed catalog contains expected portfolio records", () => {
  const catalog = catalogApi.createInitialCatalog();
  assert.deepEqual(
    {
      vendors: catalog.vendors.length,
      departments: catalog.departments.length,
      businessAreas: catalog.businessAreas.length,
      applications: catalog.applications.length,
    },
    { vendors: 3, departments: 3, businessAreas: 3, applications: 4 },
  );
  assert.deepEqual(
    catalog.applications.map((application) => application.name),
    ["Revenue Hub", "Field Ops Portal", "Employee Directory", "Analytics Workbench"],
  );
});

test("catalog executive portfolio summary counts seed indicators exactly", () => {
  const summary = catalogApi.createExecutivePortfolioSummary(catalogApi.createInitialCatalog(), {});

  assert.equal(summary.totalApplications, 4);
  assert.deepEqual(summary.counts.timeClassification, {
    Invest: 1,
    Tolerate: 1,
    Migrate: 1,
    Eliminate: 1,
  });
  assert.deepEqual(summary.counts.pace, {
    "System of Record": 2,
    "System of Differentiation": 1,
    "System of Innovation": 1,
    Unclassified: 0,
  });
  assert.deepEqual(summary.counts.businessArea, {
    "Revenue Management": 2,
    "Field Operations": 1,
    "Workforce Services": 1,
  });
  assert.deepEqual(summary.counts.lifecycleStatus, {
    active: 3,
    planned: 1,
    retiring: 0,
    retired: 0,
  });
  assert.deepEqual(summary.counts.criticality, {
    high: 2,
    medium: 2,
    low: 0,
  });
  assert.deepEqual(summary.counts.personalDataHandling, {
    Yes: 2,
    Unknown: 1,
  });
  assert.deepEqual(summary.counts.sensitiveBusinessDataHandling, {
    Yes: 2,
    Unknown: 1,
  });
  assert.deepEqual(
    Object.values(summary.catalogQuality).map((measure) => measure.text),
    [
      "Verified 1 of 4",
      "Draft 2 of 4",
      "Needs Review 1 of 4",
      "Unclassified 0 of 4",
      "Personal Data Unknown 1 of 4",
      "Sensitive Business Data Unknown 1 of 4",
    ],
  );
});

test("catalog persists application name changes in browser storage", () => {
  const storage = createMemoryStorage();
  const catalog = catalogApi.loadCatalog(storage);
  catalogApi.updateApplicationName(catalog, "app-revenue-hub", "Revenue Hub Updated");
  catalogApi.saveCatalog(storage, catalog);

  const reloadedStorage = createMemoryStorage(storage.snapshot());
  const reloaded = catalogApi.loadCatalog(reloadedStorage);
  assert.equal(reloaded.applications[0].name, "Revenue Hub Updated");
});

test("catalog preserves name-only update compatibility for legacy stored applications", () => {
  const legacyCatalog = {
    vendors: [{ id: "vendor-internal", name: "Internal Digital Team", type: "Internal Vendor" }],
    departments: [{ id: "dept-operations", name: "Operations" }],
    businessAreas: [{ id: "area-field", name: "Field Operations" }],
    applications: [
      {
        id: "app-legacy",
        name: "Legacy App",
        description: "Stored before owner fields existed.",
        vendorId: "vendor-internal",
        departmentId: "dept-operations",
        businessAreaId: "area-field",
      },
      {
        id: "app-dispatch",
        name: "Dispatch Console",
        description: "Stored before owner fields existed.",
        vendorId: "vendor-internal",
        departmentId: "dept-operations",
        businessAreaId: "area-field",
      },
    ],
  };
  const storage = createMemoryStorage({
    [catalogApi.CATALOG_STORAGE_KEY]: JSON.stringify(legacyCatalog),
  });
  const catalog = catalogApi.loadCatalog(storage);

  const updated = catalogApi.updateApplicationName(catalog, "app-legacy", "Legacy App Renamed");
  assert.equal(updated.name, "Legacy App Renamed");
  assert.equal(updated.businessOwnerName, "");
  assert.equal(updated.techOwnerName, "");
  assert.equal(updated.businessFit, 3);
  assert.equal(updated.businessFitBand, "medium");
  assert.equal(updated.techFit, "medium");
  assert.equal(updated.timeClassification, "Tolerate");
  assert.throws(() => catalogApi.updateApplicationName(catalog, "app-legacy", " dispatch console "), {
    message: "Application name must be unique.",
  });
});

test("vendor CRUD enforces internal status, unique names, and referenced delete blocks", () => {
  const catalog = catalogApi.createInitialCatalog();

  const vendor = catalogApi.createVendor(catalog, { name: "Apex Labs", isInternal: true });
  assert.deepEqual(vendor, {
    id: "vendor-apex-labs",
    name: "Apex Labs",
    isInternal: true,
  });
  assert.equal(catalogApi.getVendorDisplayType(vendor), "Internal Vendor");
  assert.throws(
    () => catalogApi.createVendor(catalog, { name: " apex labs ", isInternal: false }),
    /Vendor name must be unique\./,
  );

  const renamed = catalogApi.updateVendor(catalog, vendor.id, {
    name: "Apex Labs Renamed",
    isInternal: true,
  });
  assert.equal(renamed.name, "Apex Labs Renamed");
  catalogApi.deleteVendor(catalog, vendor.id);
  assert.equal(catalog.vendors.some((candidate) => candidate.name === "Apex Labs Renamed"), false);
  assert.throws(() => catalogApi.deleteVendor(catalog, "vendor-northstar"), {
    message: "Vendor is in use by Application: Revenue Hub.",
  });
});

test("department CRUD enforces unique names and referenced delete blocks", () => {
  const catalog = catalogApi.createInitialCatalog();

  const department = catalogApi.createDepartment(catalog, { name: "Legal" });
  assert.deepEqual(department, { id: "dept-legal", name: "Legal" });
  assert.throws(
    () => catalogApi.createDepartment(catalog, { name: " legal " }),
    /Department name must be unique\./,
  );

  const renamed = catalogApi.updateDepartment(catalog, department.id, { name: "Legal Affairs" });
  assert.equal(renamed.name, "Legal Affairs");
  catalogApi.deleteDepartment(catalog, department.id);
  assert.equal(catalog.departments.some((candidate) => candidate.name === "Legal Affairs"), false);
  assert.throws(() => catalogApi.deleteDepartment(catalog, "dept-finance"), {
    message: "Department is in use by Application: Revenue Hub.",
  });
});

test("business area CRUD enforces unique names and referenced delete blocks", () => {
  const catalog = catalogApi.createInitialCatalog();

  const businessArea = catalogApi.createBusinessArea(catalog, { name: "Customer Growth" });
  assert.deepEqual(businessArea, { id: "area-customer-growth", name: "Customer Growth" });
  assert.throws(
    () => catalogApi.createBusinessArea(catalog, { name: " customer growth " }),
    /Business Area name must be unique\./,
  );

  const renamed = catalogApi.updateBusinessArea(catalog, businessArea.id, {
    name: "Customer Growth Strategy",
  });
  assert.equal(renamed.name, "Customer Growth Strategy");
  catalogApi.deleteBusinessArea(catalog, businessArea.id);
  assert.equal(catalog.businessAreas.some((candidate) => candidate.name === "Customer Growth Strategy"), false);
  assert.throws(() => catalogApi.deleteBusinessArea(catalog, "area-revenue"), {
    message: "Business Area is in use by Application: Revenue Hub.",
  });
});

test("application CRUD enforces identity, owners, aliases, optional URLs, references, and delete", () => {
  const catalog = catalogApi.createInitialCatalog();

  const application = catalogApi.createApplication(catalog, {
    name: "Dispatch Console",
    description: "Short dispatch operations catalog entry.",
    aliases: ["Ops Desk", "Dispatch Hub"],
    businessOwnerName: "Maya Chen",
    techOwnerName: "Rui Costa",
    vendorId: "vendor-internal",
    departmentId: "dept-operations",
    businessAreaId: "area-field",
    businessFit: 4,
    techFit: "high",
  });

  assert.equal(application.id, "app-dispatch-console");
  assert.equal(application.name, "Dispatch Console");
  assert.equal(application.description, "Short dispatch operations catalog entry.");
  assert.deepEqual(application.aliases, ["Ops Desk", "Dispatch Hub"]);
  assert.equal(application.applicationUrl, "");
  assert.equal(application.diagnosticUrl, "");
  assert.equal(application.businessOwnerName, "Maya Chen");
  assert.equal(application.businessOwnerEmail, "");
  assert.equal(application.techOwnerName, "Rui Costa");
  assert.equal(application.techOwnerEmail, "");
  assert.equal(application.vendorId, "vendor-internal");
  assert.equal(application.departmentId, "dept-operations");
  assert.equal(application.businessAreaId, "area-field");
  assert.equal(application.lifecycleStatus, "active");
  assert.equal(application.plannedDate, "");
  assert.equal(application.retirementDate, "");
  assert.equal(application.businessFit, 4);
  assert.equal(application.businessFitBand, "high");
  assert.equal(application.techFit, "high");
  assert.equal(application.timeClassification, "Invest");

  const lifecycleApplicationInput = {
    description: "Lifecycle validation application.",
    businessOwnerName: "Maya Chen",
    techOwnerName: "Rui Costa",
    vendorId: "vendor-internal",
    departmentId: "dept-operations",
    businessAreaId: "area-field",
    businessFit: 3,
    techFit: "medium",
  };
  assert.throws(
    () =>
      catalogApi.createApplication(catalog, {
        ...lifecycleApplicationInput,
        name: "Planned Missing Date",
        lifecycleStatus: "planned",
      }),
    { message: "Planned Date is required for planned Applications." },
  );
  assert.throws(
    () =>
      catalogApi.createApplication(catalog, {
        ...lifecycleApplicationInput,
        name: "Retiring Missing Date",
        lifecycleStatus: "retiring",
      }),
    { message: "Retirement Date is required for retiring Applications." },
  );
  assert.throws(
    () =>
      catalogApi.createApplication(catalog, {
        ...lifecycleApplicationInput,
        name: "Retired Missing Date",
        lifecycleStatus: "retired",
      }),
    { message: "Retirement Date is required for retired Applications." },
  );
  assert.throws(
    () =>
      catalogApi.createApplication(catalog, {
        ...lifecycleApplicationInput,
        name: "Invalid Lifecycle",
        lifecycleStatus: "sunset",
      }),
    { message: "Lifecycle Status must be planned, active, retiring, or retired." },
  );
  assert.throws(
    () =>
      catalogApi.createApplication(catalog, {
        ...lifecycleApplicationInput,
        name: "Invalid Planned Date",
        lifecycleStatus: "planned",
        plannedDate: "not-a-date",
      }),
    { message: "Planned Date must be a valid date." },
  );
  assert.throws(
    () =>
      catalogApi.createApplication(catalog, {
        ...lifecycleApplicationInput,
        name: "Invalid Retirement Date",
        lifecycleStatus: "retiring",
        retirementDate: "2026-02-31",
      }),
    { message: "Retirement Date must be a valid date." },
  );
  const plannedApplication = catalogApi.createApplication(catalog, {
    ...lifecycleApplicationInput,
    name: "Planned Dispatch Console",
    lifecycleStatus: "planned",
    plannedDate: "2026-08-01",
  });
  const retiringApplication = catalogApi.createApplication(catalog, {
    ...lifecycleApplicationInput,
    name: "Retiring Dispatch Console",
    lifecycleStatus: "retiring",
    retirementDate: "2026-12-31",
  });
  const retiredApplication = catalogApi.createApplication(catalog, {
    ...lifecycleApplicationInput,
    name: "Retired Dispatch Console",
    lifecycleStatus: "retired",
    retirementDate: "2025-12-31",
  });
  assert.deepEqual(
    [plannedApplication, application, retiringApplication, retiredApplication].map(
      (candidate) => candidate.lifecycleStatus,
    ),
    ["planned", "active", "retiring", "retired"],
  );

  assert.throws(
    () =>
      catalogApi.createApplication(catalog, {
        name: " dispatch console ",
        description: "Duplicate dispatch entry.",
        businessOwnerName: "Maya Chen",
        techOwnerName: "Rui Costa",
        vendorId: "vendor-internal",
        departmentId: "dept-operations",
        businessAreaId: "area-field",
        businessFit: 4,
        techFit: "high",
      }),
    { message: "Application name must be unique." },
  );
  assert.throws(
    () =>
      catalogApi.createApplication(catalog, {
        name: "Dispatch Console Blank Description",
        description: " ",
        businessOwnerName: "Maya Chen",
        techOwnerName: "Rui Costa",
        vendorId: "vendor-internal",
        departmentId: "dept-operations",
        businessAreaId: "area-field",
        businessFit: 4,
        techFit: "high",
      }),
    { message: "Application description is required." },
  );
  assert.throws(
    () =>
      catalogApi.createApplication(catalog, {
        name: "Dispatch Console Blank Business Owner",
        description: "Short dispatch operations catalog entry.",
        businessOwnerName: " ",
        techOwnerName: "Rui Costa",
        vendorId: "vendor-internal",
        departmentId: "dept-operations",
        businessAreaId: "area-field",
      }),
    { message: "Business Owner Name is required." },
  );
  assert.throws(
    () =>
      catalogApi.createApplication(catalog, {
        name: "Dispatch Console Blank Tech Owner",
        description: "Short dispatch operations catalog entry.",
        businessOwnerName: "Maya Chen",
        techOwnerName: "",
        vendorId: "vendor-internal",
        departmentId: "dept-operations",
        businessAreaId: "area-field",
        businessFit: 4,
        techFit: "high",
      }),
    { message: "Tech Owner Name is required." },
  );
  assert.throws(
    () =>
      catalogApi.createApplication(catalog, {
        name: "Dispatch Console Missing Vendor",
        description: "Short dispatch operations catalog entry.",
        businessOwnerName: "Maya Chen",
        techOwnerName: "Rui Costa",
        vendorId: "",
        departmentId: "dept-operations",
        businessAreaId: "area-field",
        businessFit: 4,
        techFit: "high",
      }),
    { message: "Vendor is required." },
  );

  const updated = catalogApi.updateApplication(catalog, application.id, {
    ...application,
    aliases: ["Dispatch Desk"],
    businessOwnerName: "Ana Silva",
    techOwnerName: "Theo Ramos",
  });
  assert.deepEqual(updated.aliases, ["Dispatch Desk"]);
  assert.equal(updated.businessOwnerName, "Ana Silva");
  assert.equal(updated.techOwnerName, "Theo Ramos");

  catalogApi.deleteApplication(catalog, application.id);
  assert.equal(catalog.applications.some((candidate) => candidate.name === "Dispatch Console"), false);
  assert.deepEqual(
    {
      vendors: catalog.vendors.length,
      departments: catalog.departments.length,
      businessAreas: catalog.businessAreas.length,
    },
    { vendors: 3, departments: 3, businessAreas: 3 },
  );
});

test("application catalog preserves manual PACE, criticality, and data handling indicators", () => {
  const catalog = catalogApi.createInitialCatalog();
  const baseInput = {
    description: "Application indicator verification entry.",
    businessOwnerName: "Maya Chen",
    techOwnerName: "Rui Costa",
    vendorId: "vendor-internal",
    departmentId: "dept-operations",
    businessAreaId: "area-field",
    businessFit: 5,
    techFit: "low",
  };

  const unclassified = catalogApi.createApplication(catalog, {
    ...baseInput,
    name: "Unclassified Manual Pace",
    timeClassification: "Manual Override",
    pace: "Unclassified",
    criticality: "low",
    personalDataHandling: "Unknown",
    sensitiveBusinessDataHandling: "Unknown",
  });
  assert.equal(unclassified.timeClassification, "Migrate");
  assert.equal(unclassified.pace, "Unclassified");
  assert.equal(unclassified.criticality, "low");
  assert.equal(unclassified.personalDataHandling, "Unknown");
  assert.equal(unclassified.sensitiveBusinessDataHandling, "Unknown");

  const record = catalogApi.createApplication(catalog, {
    ...baseInput,
    name: "System Record Pace",
    pace: "System of Record",
    criticality: "medium",
    personalDataHandling: "Yes",
    sensitiveBusinessDataHandling: "No",
  });
  assert.equal(record.pace, "System of Record");
  assert.equal(record.criticality, "medium");
  assert.equal(record.personalDataHandling, "Yes");
  assert.equal(record.sensitiveBusinessDataHandling, "No");

  const differentiation = catalogApi.updateApplication(catalog, record.id, {
    ...record,
    pace: "System of Differentiation",
    criticality: "high",
    personalDataHandling: "No",
    sensitiveBusinessDataHandling: "Yes",
  });
  assert.equal(differentiation.pace, "System of Differentiation");
  assert.equal(differentiation.criticality, "high");
  assert.equal(differentiation.personalDataHandling, "No");
  assert.equal(differentiation.sensitiveBusinessDataHandling, "Yes");

  const innovation = catalogApi.updateApplication(catalog, record.id, {
    ...differentiation,
    pace: "System of Innovation",
    criticality: "low",
    personalDataHandling: "Unknown",
    sensitiveBusinessDataHandling: "Unknown",
  });
  assert.equal(innovation.pace, "System of Innovation");
  assert.equal(innovation.criticality, "low");
  assert.equal(innovation.personalDataHandling, "Unknown");
  assert.equal(innovation.sensitiveBusinessDataHandling, "Unknown");

  const storage = createMemoryStorage();
  catalogApi.saveCatalog(storage, catalog);
  const reloaded = catalogApi.loadCatalog(createMemoryStorage(storage.snapshot()));
  const reloadedInnovation = reloaded.applications.find((application) => application.id === record.id);
  assert.equal(reloadedInnovation.pace, "System of Innovation");
  assert.equal(reloadedInnovation.criticality, "low");
  assert.equal(reloadedInnovation.personalDataHandling, "Unknown");
  assert.equal(reloadedInnovation.sensitiveBusinessDataHandling, "Unknown");

  const legacyCatalog = {
    vendors: catalog.vendors,
    departments: catalog.departments,
    businessAreas: catalog.businessAreas,
    applications: [
      {
        ...record,
        id: "app-legacy-pace",
        name: "Legacy Pace",
        pace: "system of differentiation",
        criticality: undefined,
        personalDataHandling: undefined,
        sensitiveBusinessDataHandling: undefined,
      },
      {
        ...record,
        id: "app-invalid-legacy-indicators",
        name: "Invalid Legacy Indicators",
        pace: "core system",
        criticality: "urgent",
        personalDataHandling: "Maybe",
        sensitiveBusinessDataHandling: "Restricted",
      },
    ],
  };
  const legacyReloaded = catalogApi.loadCatalog(
    createMemoryStorage({ [catalogApi.CATALOG_STORAGE_KEY]: JSON.stringify(legacyCatalog) }),
  );
  const legacyPace = legacyReloaded.applications.find((application) => application.id === "app-legacy-pace");
  assert.equal(legacyPace.pace, "System of Differentiation");
  assert.equal(legacyPace.criticality, "medium");
  assert.equal(legacyPace.personalDataHandling, "Unknown");
  assert.equal(legacyPace.sensitiveBusinessDataHandling, "Unknown");
  const invalidLegacyIndicators = legacyReloaded.applications.find(
    (application) => application.id === "app-invalid-legacy-indicators",
  );
  assert.equal(invalidLegacyIndicators.pace, "Unclassified");
  assert.equal(invalidLegacyIndicators.criticality, "medium");
  assert.equal(invalidLegacyIndicators.personalDataHandling, "Unknown");
  assert.equal(invalidLegacyIndicators.sensitiveBusinessDataHandling, "Unknown");

  const legacyVerifiedCatalog = {
    vendors: catalog.vendors,
    departments: catalog.departments,
    businessAreas: catalog.businessAreas,
    applications: [
      {
        ...record,
        id: "app-legacy-verified",
        name: "Legacy Verified",
        informationStatus: "verified",
        lastVerificationDate: undefined,
      },
    ],
  };
  const legacyVerifiedReloaded = catalogApi.loadCatalog(
    createMemoryStorage({ [catalogApi.CATALOG_STORAGE_KEY]: JSON.stringify(legacyVerifiedCatalog) }),
  );
  const legacyVerified = legacyVerifiedReloaded.applications.find(
    (application) => application.id === "app-legacy-verified",
  );
  assert.equal(legacyVerified.informationStatus, "Verified");
  assert.equal(legacyVerified.lastVerificationDate, "");

  assert.throws(() => catalogApi.createApplication(catalog, { ...baseInput, name: "Invalid Pace", pace: "core" }), {
    message:
      "PACE Classification must be System of Record, System of Differentiation, System of Innovation, or Unclassified.",
  });
  assert.throws(
    () => catalogApi.createApplication(catalog, { ...baseInput, name: "Invalid Criticality", criticality: "urgent" }),
    { message: "Criticality must be low, medium, or high." },
  );
  assert.throws(
    () =>
      catalogApi.createApplication(catalog, {
        ...baseInput,
        name: "Invalid Personal Data",
        personalDataHandling: "Maybe",
      }),
    { message: "Personal Data Handling must be Yes, No, or Unknown." },
  );
  assert.throws(
    () =>
      catalogApi.createApplication(catalog, {
        ...baseInput,
        name: "Invalid Sensitive Data",
        sensitiveBusinessDataHandling: "Maybe",
      }),
    { message: "Sensitive Business Data Handling must be Yes, No, or Unknown." },
  );
});

test("application catalog enforces information status and verification date rules", () => {
  const catalog = catalogApi.createInitialCatalog();
  const baseInput = {
    description: "Application information status verification entry.",
    businessOwnerName: "Maya Chen",
    techOwnerName: "Rui Costa",
    vendorId: "vendor-internal",
    departmentId: "dept-operations",
    businessAreaId: "area-field",
    businessFit: 5,
    techFit: "low",
    pace: "Unclassified",
    criticality: "medium",
    personalDataHandling: "Unknown",
    sensitiveBusinessDataHandling: "Unknown",
    diagnosticUrl: "",
  };

  assert.throws(
    () =>
      catalogApi.createApplication(catalog, {
        ...baseInput,
        name: "Archived Information Status",
        informationStatus: "Archived",
      }),
    { message: "Information Status must be Draft, Verified, or Needs Review." },
  );
  assert.throws(
    () =>
      catalogApi.createApplication(catalog, {
        ...baseInput,
        name: "Verified Missing Date",
        informationStatus: "Verified",
      }),
    { message: "Last Verification Date is required for Verified Applications." },
  );

  const verified = catalogApi.createApplication(catalog, {
    ...baseInput,
    name: "Verified Information Status",
    informationStatus: "Verified",
    lastVerificationDate: "2026-07-14",
  });
  const draft = catalogApi.createApplication(catalog, {
    ...baseInput,
    name: "Draft Information Status",
    informationStatus: "Draft",
    lastVerificationDate: "",
  });
  const needsReview = catalogApi.createApplication(catalog, {
    ...baseInput,
    name: "Needs Review Information Status",
    informationStatus: "Needs Review",
    lastVerificationDate: "",
  });

  assert.deepEqual(
    [verified, draft, needsReview].map((application) => ({
      informationStatus: application.informationStatus,
      lastVerificationDate: application.lastVerificationDate,
      diagnosticUrl: application.diagnosticUrl,
      pace: application.pace,
      personalDataHandling: application.personalDataHandling,
      sensitiveBusinessDataHandling: application.sensitiveBusinessDataHandling,
    })),
    [
      {
        informationStatus: "Verified",
        lastVerificationDate: "2026-07-14",
        diagnosticUrl: "",
        pace: "Unclassified",
        personalDataHandling: "Unknown",
        sensitiveBusinessDataHandling: "Unknown",
      },
      {
        informationStatus: "Draft",
        lastVerificationDate: "",
        diagnosticUrl: "",
        pace: "Unclassified",
        personalDataHandling: "Unknown",
        sensitiveBusinessDataHandling: "Unknown",
      },
      {
        informationStatus: "Needs Review",
        lastVerificationDate: "",
        diagnosticUrl: "",
        pace: "Unclassified",
        personalDataHandling: "Unknown",
        sensitiveBusinessDataHandling: "Unknown",
      },
    ],
  );
});

test("catalog derives TIME classification from manual fit assessments", () => {
  const expectedMatrix = {
    "high/high": "Invest",
    "high/medium": "Invest",
    "high/low": "Migrate",
    "medium/high": "Tolerate",
    "medium/medium": "Tolerate",
    "medium/low": "Migrate",
    "low/high": "Eliminate",
    "low/medium": "Eliminate",
    "low/low": "Eliminate",
  };
  const expectedBands = {
    1: "low",
    2: "low",
    3: "medium",
    4: "high",
    5: "high",
  };
  const fitForBand = {
    low: 1,
    medium: 3,
    high: 4,
  };

  for (const [businessFit, businessFitBand] of Object.entries(expectedBands)) {
    const catalog = catalogApi.createInitialCatalog();
    const application = catalogApi.createApplication(catalog, {
      name: `Band ${businessFit}`,
      description: "Fit band verification application.",
      businessOwnerName: "Maya Chen",
      techOwnerName: "Rui Costa",
      vendorId: "vendor-internal",
      departmentId: "dept-operations",
      businessAreaId: "area-field",
      businessFit,
      techFit: "medium",
    });
    assert.equal(application.businessFitBand, businessFitBand);
  }

  for (const [combination, timeClassification] of Object.entries(expectedMatrix)) {
    const [businessFitBand, techFit] = combination.split("/");
    const catalog = catalogApi.createInitialCatalog();
    const application = catalogApi.createApplication(catalog, {
      name: `TIME ${combination}`,
      description: "TIME matrix verification application.",
      businessOwnerName: "Maya Chen",
      techOwnerName: "Rui Costa",
      vendorId: "vendor-internal",
      departmentId: "dept-operations",
      businessAreaId: "area-field",
      businessFit: fitForBand[businessFitBand],
      techFit,
      timeClassification: "Manual Override",
    });
    assert.equal(application.businessFitBand, businessFitBand);
    assert.equal(application.timeClassification, timeClassification);
  }

  const catalog = catalogApi.createInitialCatalog();
  assert.throws(
    () =>
      catalogApi.createApplication(catalog, {
        name: "Invalid Business Fit",
        description: "Invalid Business Fit verification application.",
        businessOwnerName: "Maya Chen",
        techOwnerName: "Rui Costa",
        vendorId: "vendor-internal",
        departmentId: "dept-operations",
        businessAreaId: "area-field",
        businessFit: 6,
        techFit: "medium",
      }),
    { message: "Business Fit must be 1, 2, 3, 4, or 5." },
  );
  assert.throws(
    () =>
      catalogApi.createApplication(catalog, {
        name: "Invalid Tech Fit",
        description: "Invalid Tech Fit verification application.",
        businessOwnerName: "Maya Chen",
        techOwnerName: "Rui Costa",
        vendorId: "vendor-internal",
        departmentId: "dept-operations",
        businessAreaId: "area-field",
        businessFit: 3,
        techFit: "severe",
      }),
    { message: "Tech Fit must be low, medium, or high." },
  );
});

test("navigation sections match the MVP catalog areas", () => {
  assert.deepEqual(catalogApi.getNavigationSections().map((section) => section.label), [
    "Executive Overview",
    "Applications",
    "Vendors",
    "Departments",
    "Business Areas",
  ]);
});

// renderApp() itself is purely synchronous over an already-loaded catalog; it never
// calls the apiClient's list* methods directly (that's init()'s job, exercised
// separately below with a full Promise.all bootstrap). This test guards that
// invariant so rendering never triggers a surprise network round-trip.
test("browser adapter renders the initial catalog shell without calling the apiClient", () => {
  const document = createDocument();
  const storage = createMemoryStorage();
  const apiCalls = [];
  const catalog = catalogApi.createInitialCatalog();
  const apiClient = new Proxy(createMockApiClient(catalog), {
    get(target, prop) {
      const value = target[prop];
      if (typeof value === "function") {
        return (...args) => {
          apiCalls.push(prop);
          return value(...args);
        };
      }
      return value;
    },
  });
  const rendered = appApi.renderApp({ document, storage, catalogApi, apiClient, catalog });

  const text = collectText(rendered.root);
  assert.match(text, /Executive Overview/);
  assert.match(text, /Revenue Hub/);
  assert.match(text, /Vendors/);
  assert.equal(apiCalls.length, 0);
});

test("browser adapter bootstrap loads all four collections through the apiClient in parallel", async () => {
  const document = createDocument();
  const catalog = catalogApi.createInitialCatalog();
  const mockApiClient = createMockApiClient(catalog);
  const calls = [];
  const apiClient = {
    listVendors: () => {
      calls.push("listVendors");
      return mockApiClient.listVendors();
    },
    listDepartments: () => {
      calls.push("listDepartments");
      return mockApiClient.listDepartments();
    },
    listBusinessAreas: () => {
      calls.push("listBusinessAreas");
      return mockApiClient.listBusinessAreas();
    },
    listApplications: () => {
      calls.push("listApplications");
      return mockApiClient.listApplications();
    },
  };
  const root = {
    ApplicationPortfolioCatalog: catalogApi,
    ApplicationPortfolioApiClient: apiClient,
    document,
    localStorage: createMemoryStorage(),
  };

  const result = await appApi.init(root);
  assert.deepEqual(calls.sort(), ["listApplications", "listBusinessAreas", "listDepartments", "listVendors"]);
  const text = collectText(result.root);
  assert.match(text, /Revenue Hub/);
});

test("browser adapter filters applications and refreshes executive indicators after mutations", async () => {
  const document = createDocument();
  const storage = createMemoryStorage();
  const catalog = catalogApi.createInitialCatalog();
  const apiClient = createMockApiClient(catalog);
  const rendered = appApi.renderApp({ document, storage, catalogApi, apiClient, catalog });

  function getOverview() {
    return document.getElementById("overview");
  }

  function getApplicationsSection() {
    return document.getElementById("applications");
  }

  function applyCatalogFilters(values) {
    const section = getApplicationsSection();
    if (Object.prototype.hasOwnProperty.call(values, "filterBusinessArea")) {
      findField(section, "filterBusinessArea").value = values.filterBusinessArea;
    }
    if (Object.prototype.hasOwnProperty.call(values, "filterInformationStatus")) {
      findField(section, "filterInformationStatus").value = values.filterInformationStatus;
    }
    findAll(section, (node) => node.tagName === "BUTTON")
      .find((button) => button.textContent === "Apply Filters")
      .onclick();
  }

  applyCatalogFilters({ filterBusinessArea: "area-field" });
  let applicationsSection = getApplicationsSection();
  let cards = findAll(applicationsSection, (node) => node.tagName === "ARTICLE" && node.className === "application-card");
  assert.equal(cards.length, 1);
  assert.match(collectText(cards[0]), /Field Ops Portal/);
  assert.match(collectText(applicationsSection), /Showing 1 of 4 Applications/);
  const filteredOverviewText = collectText(getOverview());
  assert.match(filteredOverviewText, /Field Operations\s+1/);
  assert.match(filteredOverviewText, /Tolerate\s+1/);

  applyCatalogFilters({ filterBusinessArea: "", filterInformationStatus: "Needs Review" });
  applicationsSection = getApplicationsSection();
  cards = findAll(applicationsSection, (node) => node.tagName === "ARTICLE" && node.className === "application-card");
  assert.equal(cards.length, 1);
  assert.match(collectText(cards[0]), /Employee Directory/);
  assert.match(collectText(applicationsSection), /Showing 1 of 4 Applications/);

  findAll(getApplicationsSection(), (node) => node.tagName === "BUTTON")
    .find((button) => button.textContent === "Clear")
    .onclick();
  assert.match(collectText(getApplicationsSection()), /Showing 4 of 4 Applications/);

  appApi.renderApp({ document, storage, catalogApi, apiClient, root: rendered.root, catalog: rendered.catalog, filters: {} });
  applicationsSection = getApplicationsSection();
  const createForm = findAll(applicationsSection, (node) => node.tagName === "FORM")[0];
  findField(createForm, "name").value = "Dispatch Console";
  findField(createForm, "description").value = "Short dispatch operations catalog entry.";
  findField(createForm, "businessOwnerName").value = "Maya Chen";
  findField(createForm, "techOwnerName").value = "Rui Costa";
  findField(createForm, "vendorId").value = "vendor-internal";
  findField(createForm, "departmentId").value = "dept-operations";
  findField(createForm, "businessAreaId").value = "area-field";
  findField(createForm, "lifecycleStatus").value = "active";
  findField(createForm, "businessFit").value = "4";
  findField(createForm, "techFit").value = "low";
  findField(createForm, "pace").value = "System of Record";
  findField(createForm, "criticality").value = "medium";
  findField(createForm, "personalDataHandling").value = "Yes";
  findField(createForm, "sensitiveBusinessDataHandling").value = "No";
  findField(createForm, "informationStatus").value = "Draft";
  await createForm.onsubmit({ preventDefault() {} });

  assert.match(collectText(getOverview()), /5\s+Applications/);

  applicationsSection = getApplicationsSection();
  const dispatchCard = findAll(applicationsSection, (node) => node.tagName === "ARTICLE").find((card) =>
    collectText(card).includes("Dispatch Console"),
  );
  const editForm = findAll(dispatchCard, (node) => node.tagName === "FORM")[0];
  findField(editForm, "businessAreaId").value = "area-revenue";
  findField(editForm, "lifecycleStatus").value = "retired";
  findField(editForm, "retirementDate").value = "2025-12-31";
  findField(editForm, "businessFit").value = "5";
  findField(editForm, "techFit").value = "low";
  findField(editForm, "pace").value = "Unclassified";
  findField(editForm, "criticality").value = "high";
  findField(editForm, "personalDataHandling").value = "Unknown";
  findField(editForm, "sensitiveBusinessDataHandling").value = "Unknown";
  findField(editForm, "informationStatus").value = "Needs Review";
  findField(editForm, "lastVerificationDate").value = "";
  await editForm.onsubmit({ preventDefault() {} });

  const updatedOverviewText = collectText(getOverview());
  assert.match(updatedOverviewText, /Migrate\s+2/);
  assert.match(updatedOverviewText, /Unclassified\s+Unclassified 1 of 5/);
  assert.match(updatedOverviewText, /Needs Review\s+Needs Review 2 of 5/);
  assert.match(updatedOverviewText, /Personal Data Unknown\s+Personal Data Unknown 2 of 5/);
  assert.match(updatedOverviewText, /Sensitive Business Data Unknown\s+Sensitive Business Data Unknown 2 of 5/);

  applicationsSection = getApplicationsSection();
  const updatedDispatchCard = findAll(applicationsSection, (node) => node.tagName === "ARTICLE").find((card) =>
    collectText(card).includes("Dispatch Console"),
  );
  await findAll(updatedDispatchCard, (node) => node.tagName === "BUTTON")
    .find((button) => button.textContent === "Delete")
    .onclick();

  assert.match(collectText(getOverview()), /4\s+Applications/);
});

test("browser adapter manages application create edit delete with persistence", async () => {
  const document = createDocument();
  const storage = createMemoryStorage();
  const catalog = catalogApi.createInitialCatalog();
  const apiClient = createMockApiClient(catalog);
  const rendered = appApi.renderApp({ document, storage, catalogApi, apiClient, catalog });

  let applicationsSection = document.getElementById("applications");
  let createForm = findAll(applicationsSection, (node) => node.tagName === "FORM")[0];
  findField(createForm, "name").value = "Dispatch Console";
  findField(createForm, "description").value = "Short dispatch operations catalog entry.";
  findField(createForm, "aliases").value = "Ops Desk, Dispatch Hub";
  findField(createForm, "businessOwnerName").value = "Maya Chen";
  findField(createForm, "techOwnerName").value = "Rui Costa";
  findField(createForm, "vendorId").value = "vendor-internal";
  findField(createForm, "departmentId").value = "dept-operations";
  findField(createForm, "businessAreaId").value = "area-field";
  findField(createForm, "lifecycleStatus").value = "planned";
  findField(createForm, "businessFit").value = "4";
  findField(createForm, "techFit").value = "high";
  findField(createForm, "pace").value = "System of Innovation";
  findField(createForm, "criticality").value = "high";
  findField(createForm, "personalDataHandling").value = "Yes";
  findField(createForm, "sensitiveBusinessDataHandling").value = "Unknown";
  assert.ok(findField(createForm, "informationStatus"));
  assert.ok(findField(createForm, "lastVerificationDate"));
  await createForm.onsubmit({ preventDefault() {} });
  assert.match(collectText(applicationsSection), /Planned Date is required for planned Applications\./);

  findField(createForm, "plannedDate").value = "2026-08-01";
  findField(createForm, "informationStatus").value = "Verified";
  findField(createForm, "lastVerificationDate").value = "";
  await createForm.onsubmit({ preventDefault() {} });
  assert.match(collectText(applicationsSection), /Last Verification Date is required for Verified Applications\./);

  findField(createForm, "lastVerificationDate").value = "2026-07-14";
  await createForm.onsubmit({ preventDefault() {} });
  assert.match(collectText(rendered.root), /Dispatch Console/);
  assert.match(collectText(rendered.root), /Ops Desk, Dispatch Hub/);
  assert.match(collectText(rendered.root), /planned/);
  assert.match(collectText(rendered.root), /Business Fit/);
  assert.match(collectText(rendered.root), /Tech Fit/);
  assert.match(collectText(rendered.root), /Invest/);
  assert.match(collectText(rendered.root), /PACE Classification/);
  assert.match(collectText(rendered.root), /Criticality/);
  assert.match(collectText(rendered.root), /Personal Data Handling/);
  assert.match(collectText(rendered.root), /Sensitive Business Data Handling/);
  assert.match(collectText(rendered.root), /Information Status/);
  assert.match(collectText(rendered.root), /Verified/);
  assert.match(collectText(rendered.root), /Last Verification Date/);

  const createdCard = findAll(document.getElementById("applications"), (node) => node.tagName === "ARTICLE").find((card) =>
    collectText(card).includes("Dispatch Console"),
  );
  const createdForm = findAll(createdCard, (node) => node.tagName === "FORM")[0];
  assert.equal(findField(createdForm, "plannedDate").value, "2026-08-01");
  assert.equal(findField(createdForm, "lastVerificationDate").value, "2026-07-14");

  applicationsSection = document.getElementById("applications");
  createForm = findAll(applicationsSection, (node) => node.tagName === "FORM")[0];
  findField(createForm, "name").value = " dispatch console ";
  findField(createForm, "description").value = "Short dispatch operations catalog entry.";
  findField(createForm, "businessOwnerName").value = "Maya Chen";
  findField(createForm, "techOwnerName").value = "Rui Costa";
  findField(createForm, "vendorId").value = "vendor-internal";
  findField(createForm, "departmentId").value = "dept-operations";
  findField(createForm, "businessAreaId").value = "area-field";
  findField(createForm, "lifecycleStatus").value = "active";
  findField(createForm, "businessFit").value = "4";
  findField(createForm, "techFit").value = "high";
  findField(createForm, "pace").value = "System of Record";
  findField(createForm, "criticality").value = "medium";
  findField(createForm, "personalDataHandling").value = "No";
  findField(createForm, "sensitiveBusinessDataHandling").value = "No";
  await createForm.onsubmit({ preventDefault() {} });
  assert.match(collectText(applicationsSection), /Application name must be unique\./);

  const dispatchCard = findAll(applicationsSection, (node) => node.tagName === "ARTICLE").find((card) =>
    collectText(card).includes("Dispatch Console"),
  );
  const editForm = findAll(dispatchCard, (node) => node.tagName === "FORM")[0];
  findField(editForm, "aliases").value = "Dispatch Desk";
  findField(editForm, "businessOwnerName").value = "Ana Silva";
  findField(editForm, "techOwnerName").value = "Theo Ramos";
  findField(editForm, "lifecycleStatus").value = "retired";
  findField(editForm, "businessFit").value = "5";
  findField(editForm, "techFit").value = "low";
  findField(editForm, "pace").value = "System of Innovation";
  findField(editForm, "criticality").value = "high";
  findField(editForm, "personalDataHandling").value = "Yes";
  findField(editForm, "sensitiveBusinessDataHandling").value = "Unknown";
  findField(editForm, "retirementDate").value = "";
  await editForm.onsubmit({ preventDefault() {} });
  assert.match(collectText(applicationsSection), /Retirement Date is required for retired Applications\./);

  findField(editForm, "retirementDate").value = "2025-12-31";
  await editForm.onsubmit({ preventDefault() {} });

  // Simulate a page reload: build a brand new document/DOM, but reuse the same
  // `catalog` object and apiClient - in the real architecture this data would
  // instead come back from GET /api/applications (etc.) against the Postgres-backed
  // Spring Boot backend, so reusing the in-memory catalog here stands in for that
  // durable server-side persistence without needing an HTTP server in this test.
  const reloadedDocument = createDocument();
  const reloaded = appApi.renderApp({
    document: reloadedDocument,
    storage,
    catalogApi,
    apiClient,
    catalog,
  });
  const reloadedApplications = reloadedDocument.getElementById("applications");
  const reloadedDispatchCard = findAll(reloadedApplications, (node) => node.tagName === "ARTICLE").find((card) =>
    collectText(card).includes("Dispatch Console"),
  );
  const reloadedDispatchText = collectText(reloadedDispatchCard);
  assert.match(reloadedDispatchText, /Dispatch Desk/);
  assert.match(reloadedDispatchText, /retired/);
  assert.match(reloadedDispatchText, /Migrate/);
  assert.match(reloadedDispatchText, /PACE Classification/);
  assert.match(reloadedDispatchText, /Criticality/);
  assert.match(reloadedDispatchText, /Personal Data Handling/);
  assert.match(reloadedDispatchText, /Sensitive Business Data Handling/);
  assert.match(reloadedDispatchText, /Information Status/);
  assert.match(reloadedDispatchText, /Verified/);
  assert.match(reloadedDispatchText, /Last Verification Date/);
  assert.match(reloadedDispatchText, /System of Innovation/);
  assert.match(reloadedDispatchText, /high/);
  assert.match(reloadedDispatchText, /Yes/);
  assert.match(reloadedDispatchText, /Unknown/);
  const reloadedDispatchForm = findAll(reloadedDispatchCard, (node) => node.tagName === "FORM")[0];
  assert.equal(findField(reloadedDispatchForm, "businessOwnerName").value, "Ana Silva");
  assert.equal(findField(reloadedDispatchForm, "retirementDate").value, "2025-12-31");
  assert.equal(findField(reloadedDispatchForm, "lastVerificationDate").value, "2026-07-14");
  await findAll(reloadedDispatchCard, (node) => node.tagName === "BUTTON")
    .find((button) => button.textContent === "Delete")
    .onclick();
  assert.equal(catalog.applications.some((application) => application.name === "Dispatch Console"), false);
});

test("browser adapter manages vendor CRUD with persisted create and rendered block messages", async () => {
  const document = createDocument();
  const storage = createMemoryStorage();
  const catalog = catalogApi.createInitialCatalog();
  const apiClient = createMockApiClient(catalog);
  const rendered = appApi.renderApp({ document, storage, catalogApi, apiClient, catalog });

  let vendorsSection = document.getElementById("vendors");
  const createForm = findAll(vendorsSection, (node) => node.tagName === "FORM")[0];
  const createInputs = findAll(createForm, (node) => node.tagName === "INPUT");
  createInputs.find((input) => input.name === "name").value = "Apex Labs";
  createInputs.find((input) => input.name === "isInternal").checked = true;
  await createForm.onsubmit({ preventDefault() {} });

  const storedVendor = catalog.vendors.find((vendor) => vendor.name === "Apex Labs");
  assert.equal(storedVendor.isInternal, true);
  assert.match(collectText(rendered.root), /Apex Labs/);
  assert.match(collectText(rendered.root), /Internal Vendor/);

  vendorsSection = document.getElementById("vendors");
  const duplicateForm = findAll(vendorsSection, (node) => node.tagName === "FORM")[0];
  const duplicateInputs = findAll(duplicateForm, (node) => node.tagName === "INPUT");
  duplicateInputs.find((input) => input.name === "name").value = "Apex Labs";
  duplicateInputs.find((input) => input.name === "isInternal").checked = true;
  await duplicateForm.onsubmit({ preventDefault() {} });
  assert.match(collectText(vendorsSection), /Vendor name must be unique\./);

  const northstarItem = findAll(vendorsSection, (node) => node.tagName === "LI").find((item) =>
    collectText(item).includes("Northstar Software"),
  );
  const deleteButton = findAll(northstarItem, (node) => node.tagName === "BUTTON").find(
    (button) => button.textContent === "Delete",
  );
  await deleteButton.onclick();
  assert.match(collectText(vendorsSection), /Vendor is in use by Application: Revenue Hub\./);
});

test("browser adapter manages department and business area create edit delete controls", async () => {
  const document = createDocument();
  const storage = createMemoryStorage();
  const catalog = catalogApi.createInitialCatalog();
  const apiClient = createMockApiClient(catalog);
  const rendered = appApi.renderApp({ document, storage, catalogApi, apiClient, catalog });

  let departmentsSection = document.getElementById("departments");
  const departmentCreateForm = findAll(departmentsSection, (node) => node.tagName === "FORM")[0];
  findAll(departmentCreateForm, (node) => node.tagName === "INPUT").find((input) => input.name === "name").value =
    "Legal";
  await departmentCreateForm.onsubmit({ preventDefault() {} });
  assert.match(collectText(rendered.root), /Legal/);

  departmentsSection = document.getElementById("departments");
  const legalItem = findAll(departmentsSection, (node) => node.tagName === "LI").find((item) =>
    collectText(item).includes("Legal"),
  );
  const legalEditForm = findAll(legalItem, (node) => node.tagName === "FORM")[0];
  findAll(legalEditForm, (node) => node.tagName === "INPUT").find((input) => input.name === "name").value =
    "Legal Affairs";
  await legalEditForm.onsubmit({ preventDefault() {} });
  assert.match(collectText(rendered.root), /Legal Affairs/);

  departmentsSection = document.getElementById("departments");
  const financeItem = findAll(departmentsSection, (node) => node.tagName === "LI").find((item) =>
    collectText(item).includes("Finance"),
  );
  await findAll(financeItem, (node) => node.tagName === "BUTTON")
    .find((button) => button.textContent === "Delete")
    .onclick();
  assert.match(collectText(departmentsSection), /Department is in use by Application: Revenue Hub\./);

  departmentsSection = document.getElementById("departments");
  const legalAffairsItem = findAll(departmentsSection, (node) => node.tagName === "LI").find((item) =>
    collectText(item).includes("Legal Affairs"),
  );
  await findAll(legalAffairsItem, (node) => node.tagName === "BUTTON")
    .find((button) => button.textContent === "Delete")
    .onclick();
  assert.doesNotMatch(collectText(rendered.root), /Legal Affairs/);

  let businessAreasSection = document.getElementById("business-areas");
  const areaCreateForm = findAll(businessAreasSection, (node) => node.tagName === "FORM")[0];
  findAll(areaCreateForm, (node) => node.tagName === "INPUT").find((input) => input.name === "name").value =
    "Customer Growth";
  await areaCreateForm.onsubmit({ preventDefault() {} });
  assert.match(collectText(rendered.root), /Customer Growth/);

  businessAreasSection = document.getElementById("business-areas");
  const growthItem = findAll(businessAreasSection, (node) => node.tagName === "LI").find((item) =>
    collectText(item).includes("Customer Growth"),
  );
  const growthEditForm = findAll(growthItem, (node) => node.tagName === "FORM")[0];
  findAll(growthEditForm, (node) => node.tagName === "INPUT").find((input) => input.name === "name").value =
    "Customer Growth Strategy";
  await growthEditForm.onsubmit({ preventDefault() {} });
  assert.match(collectText(rendered.root), /Customer Growth Strategy/);

  businessAreasSection = document.getElementById("business-areas");
  const revenueItem = findAll(businessAreasSection, (node) => node.tagName === "LI").find((item) =>
    collectText(item).includes("Revenue Management"),
  );
  await findAll(revenueItem, (node) => node.tagName === "BUTTON")
    .find((button) => button.textContent === "Delete")
    .onclick();
  assert.match(collectText(businessAreasSection), /Business Area is in use by Application: Revenue Hub\./);

  businessAreasSection = document.getElementById("business-areas");
  const strategyItem = findAll(businessAreasSection, (node) => node.tagName === "LI").find((item) =>
    collectText(item).includes("Customer Growth Strategy"),
  );
  await findAll(strategyItem, (node) => node.tagName === "BUTTON")
    .find((button) => button.textContent === "Delete")
    .onclick();
  assert.doesNotMatch(collectText(rendered.root), /Customer Growth Strategy/);
});

function adminRoot(document, apiClient, currentUser) {
  return {
    ApplicationPortfolioCatalog: catalogApi,
    ApplicationPortfolioApiClient: apiClient,
    document,
    localStorage: createMemoryStorage(),
    __currentUser: currentUser,
  };
}

test("admin sees the User Management screen listing users with login method and role", async () => {
  const document = createDocument();
  const catalog = catalogApi.createInitialCatalog();
  const mockApiClient = createMockApiClient(catalog);
  const store = createUserStore([
    { id: "u-admin", name: "Ada Admin", email: "ada@example.com", role: "ADMIN", loginMethod: "SSO" },
    { id: "u-viewer", name: "Vic Viewer", email: "vic@example.com", role: "VIEWER", loginMethod: "LOCAL" },
  ]);
  const apiClient = Object.assign({}, mockApiClient, store.client, {
    getCurrentUser: () => Promise.resolve({ name: "Ada Admin", email: "ada@example.com", role: "ADMIN" }),
  });
  const root = {
    ApplicationPortfolioCatalog: catalogApi,
    ApplicationPortfolioApiClient: apiClient,
    document,
    localStorage: createMemoryStorage(),
  };

  const result = await appApi.init(root);
  const nav = findAll(result.root, (node) => collectText(node) === "User Management" && node.tagName === "A");
  assert.equal(nav.length, 1);

  const usersSection = document.getElementById("users");
  assert.ok(usersSection, "users section should be rendered for admins");
  const text = collectText(usersSection);
  assert.match(text, /Vic Viewer/);
  assert.match(text, /vic@example.com/);
  assert.match(text, /Local Login/);
  assert.match(text, /SSO/);
});

test("non-admin does not see the User Management screen", async () => {
  const document = createDocument();
  const catalog = catalogApi.createInitialCatalog();
  const mockApiClient = createMockApiClient(catalog);
  let listUsersCalled = false;
  const apiClient = Object.assign({}, mockApiClient, {
    getCurrentUser: () => Promise.resolve({ name: "Vic Viewer", email: "vic@example.com", role: "VIEWER" }),
    listCatalogUsers: () => {
      listUsersCalled = true;
      return Promise.resolve([]);
    },
  });
  const root = {
    ApplicationPortfolioCatalog: catalogApi,
    ApplicationPortfolioApiClient: apiClient,
    document,
    localStorage: createMemoryStorage(),
  };

  const result = await appApi.init(root);
  assert.equal(listUsersCalled, false, "viewer should not trigger the admin users fetch");
  assert.ok(!document.getElementById("users"), "users section must not render for non-admins");
  const nav = findAll(result.root, (node) => collectText(node) === "User Management" && node.tagName === "A");
  assert.equal(nav.length, 0);
});

test("admin changes another user's role through the User Management screen", async () => {
  const document = createDocument();
  const catalog = catalogApi.createInitialCatalog();
  const mockApiClient = createMockApiClient(catalog);
  const store = createUserStore([
    { id: "u-admin", name: "Ada Admin", email: "ada@example.com", role: "ADMIN", loginMethod: "SSO" },
    { id: "u-viewer", name: "Vic Viewer", email: "vic@example.com", role: "VIEWER", loginMethod: "SSO" },
  ]);
  const apiClient = Object.assign({}, mockApiClient, store.client, {
    getCurrentUser: () => Promise.resolve({ name: "Ada Admin", email: "ada@example.com", role: "ADMIN" }),
  });
  const root = {
    ApplicationPortfolioCatalog: catalogApi,
    ApplicationPortfolioApiClient: apiClient,
    document,
    localStorage: createMemoryStorage(),
  };

  await appApi.init(root);

  const usersSection = document.getElementById("users");
  const selects = findAll(usersSection, (node) => node.tagName === "SELECT" && node.name !== "role");
  // Only the non-self (Viewer) row exposes a per-user role select; the admin's
  // own row is read-only and the create-local-user form's Role select (name
  // "role") is excluded here.
  assert.equal(selects.length, 1);
  const select = selects[0];
  select.value = "EDITOR";
  await select.onchange();

  assert.equal(store.users.find((user) => user.id === "u-viewer").role, "EDITOR");
  const refreshed = document.getElementById("users");
  assert.match(collectText(refreshed), /Vic Viewer is now Editor\./);
});

test("admin cannot change their own role from the User Management screen", async () => {
  const document = createDocument();
  const catalog = catalogApi.createInitialCatalog();
  const mockApiClient = createMockApiClient(catalog);
  const store = createUserStore([
    { id: "u-admin", name: "Ada Admin", email: "ada@example.com", role: "ADMIN", loginMethod: "SSO" },
  ]);
  const apiClient = Object.assign({}, mockApiClient, store.client, {
    getCurrentUser: () => Promise.resolve({ name: "Ada Admin", email: "ada@example.com", role: "ADMIN" }),
  });
  const root = {
    ApplicationPortfolioCatalog: catalogApi,
    ApplicationPortfolioApiClient: apiClient,
    document,
    localStorage: createMemoryStorage(),
  };

  await appApi.init(root);
  const usersSection = document.getElementById("users");
  const selects = findAll(usersSection, (node) => node.tagName === "SELECT");
  // The admin's own row is read-only, but the create-local-user form still
  // exposes a Role select, so exactly one select remains on the screen.
  const roleSelects = selects.filter((node) => node.name !== "role");
  assert.equal(roleSelects.length, 0, "admin's own row must not offer a role select");
  assert.match(collectText(usersSection), /You/);
});

test("admin creates a Local Login account through the User Management screen", async () => {
  const document = createDocument();
  const catalog = catalogApi.createInitialCatalog();
  const mockApiClient = createMockApiClient(catalog);
  const store = createUserStore([
    { id: "u-admin", name: "Ada Admin", email: "ada@example.com", role: "ADMIN", loginMethod: "SSO" },
  ]);
  let createCall = null;
  const apiClient = Object.assign({}, mockApiClient, store.client, {
    getCurrentUser: () => Promise.resolve({ name: "Ada Admin", email: "ada@example.com", role: "ADMIN" }),
    createLocalUser: (input) => {
      createCall = input;
      return store.client.createLocalUser(input);
    },
  });
  const root = {
    ApplicationPortfolioCatalog: catalogApi,
    ApplicationPortfolioApiClient: apiClient,
    document,
    localStorage: createMemoryStorage(),
  };

  await appApi.init(root);

  const usersSection = document.getElementById("users");
  const forms = findAll(usersSection, (node) => node.className === "local-user-form");
  assert.equal(forms.length, 1, "admin should see the create-local-user form");
  const form = forms[0];

  findField(form, "name").value = "Percy Partner";
  findField(form, "email").value = "percy@partner.com";
  findField(form, "role").value = "EDITOR";

  await form.onsubmit({ preventDefault() {} });

  assert.deepEqual(createCall, { name: "Percy Partner", email: "percy@partner.com", role: "EDITOR" });
  assert.ok(
    store.users.some((user) => user.email === "percy@partner.com" && user.loginMethod === "LOCAL"),
    "the new Local Login user should be persisted",
  );

  const refreshed = document.getElementById("users");
  const text = collectText(refreshed);
  assert.match(text, /percy@partner\.com/);
  assert.match(text, /Local Login/);
  assert.match(text, /Invite sent to percy@partner\.com/);
});

test("admin sees each user's Access Scope summary on the User Management screen", async () => {
  const document = createDocument();
  const catalog = catalogApi.createInitialCatalog();
  const mockApiClient = createMockApiClient(catalog);
  const store = createUserStore([
    { id: "u-admin", name: "Ada Admin", email: "ada@example.com", role: "ADMIN", loginMethod: "SSO" },
    {
      id: "u-viewer",
      name: "Vic Viewer",
      email: "vic@example.com",
      role: "VIEWER",
      loginMethod: "SSO",
      scopedDepartmentIds: ["dept-finance"],
      scopedBusinessAreaIds: [],
    },
    {
      id: "u-empty",
      name: "Ned Newbie",
      email: "ned@example.com",
      role: "VIEWER",
      loginMethod: "SSO",
      scopedDepartmentIds: [],
      scopedBusinessAreaIds: [],
    },
  ]);
  const apiClient = Object.assign({}, mockApiClient, store.client, {
    getCurrentUser: () => Promise.resolve({ name: "Ada Admin", email: "ada@example.com", role: "ADMIN" }),
  });
  const root = {
    ApplicationPortfolioCatalog: catalogApi,
    ApplicationPortfolioApiClient: apiClient,
    document,
    localStorage: createMemoryStorage(),
  };

  await appApi.init(root);
  const usersSection = document.getElementById("users");
  const text = collectText(usersSection);
  assert.match(text, /Access Scope/);
  assert.match(text, /1 Department/);
  assert.match(text, /No scope assigned/);
});

test("admin assigns an Access Scope to a user through the User Management screen", async () => {
  const document = createDocument();
  const catalog = catalogApi.createInitialCatalog();
  const mockApiClient = createMockApiClient(catalog);
  const store = createUserStore([
    { id: "u-admin", name: "Ada Admin", email: "ada@example.com", role: "ADMIN", loginMethod: "SSO" },
    {
      id: "u-viewer",
      name: "Vic Viewer",
      email: "vic@example.com",
      role: "VIEWER",
      loginMethod: "SSO",
      scopedDepartmentIds: [],
      scopedBusinessAreaIds: [],
    },
  ]);
  const apiClient = Object.assign({}, mockApiClient, store.client, {
    getCurrentUser: () => Promise.resolve({ name: "Ada Admin", email: "ada@example.com", role: "ADMIN" }),
  });
  const root = {
    ApplicationPortfolioCatalog: catalogApi,
    ApplicationPortfolioApiClient: apiClient,
    document,
    localStorage: createMemoryStorage(),
  };

  await appApi.init(root);
  const usersSection = document.getElementById("users");
  const checkboxes = findAll(
    usersSection,
    (node) => node.tagName === "INPUT" && node.type === "checkbox",
  );
  // The Finance department checkbox comes from the initial catalog master data.
  const financeCheck = checkboxes.find((check) => check.value === "dept-finance");
  assert.ok(financeCheck, "a Departments checkbox for Finance should be rendered");
  financeCheck.checked = true;

  const saveButtons = findAll(
    usersSection,
    (node) => node.tagName === "BUTTON" && collectText(node) === "Save Scope",
  );
  assert.equal(saveButtons.length, 1);
  await saveButtons[0].onclick();

  const target = store.users.find((user) => user.id === "u-viewer");
  assert.deepEqual(target.scopedDepartmentIds, ["dept-finance"]);
  assert.deepEqual(target.scopedBusinessAreaIds, []);
  assert.match(collectText(document.getElementById("users")), /Access scope updated for Vic Viewer\./);
});


test("admin sees Edit Permissions management only for Editor users", async () => {
  const document = createDocument();
  const catalog = catalogApi.createInitialCatalog();
  const mockApiClient = createMockApiClient(catalog);
  const store = createUserStore([
    { id: "u-admin", name: "Ada Admin", email: "ada@example.com", role: "ADMIN", loginMethod: "SSO" },
    { id: "u-editor", name: "Eve Editor", email: "eve@example.com", role: "EDITOR", loginMethod: "SSO" },
    { id: "u-viewer", name: "Vic Viewer", email: "vic@example.com", role: "VIEWER", loginMethod: "SSO" },
  ]);
  const apiClient = Object.assign({}, mockApiClient, store.client, {
    getCurrentUser: () => Promise.resolve({ name: "Ada Admin", email: "ada@example.com", role: "ADMIN" }),
  });
  const root = {
    ApplicationPortfolioCatalog: catalogApi,
    ApplicationPortfolioApiClient: apiClient,
    document,
    localStorage: createMemoryStorage(),
  };

  await appApi.init(root);
  const usersSection = document.getElementById("users");
  const text = collectText(usersSection);
  assert.match(text, /Edit Permissions/);
  assert.match(text, /Manage permissions/);
  assert.match(text, /Editor Role required/);

  const permButtons = findAll(
    usersSection,
    (node) => node.tagName === "BUTTON" && collectText(node) === "Edit permissions",
  );
  assert.equal(permButtons.length, 1, "only the Editor row should offer an Edit permissions button");
});

test("admin grants and revokes an Edit Permission through the User Management screen", async () => {
  const document = createDocument();
  const catalog = catalogApi.createInitialCatalog();
  const mockApiClient = createMockApiClient(catalog);
  const store = createUserStore([
    { id: "u-admin", name: "Ada Admin", email: "ada@example.com", role: "ADMIN", loginMethod: "SSO" },
    { id: "u-editor", name: "Eve Editor", email: "eve@example.com", role: "EDITOR", loginMethod: "SSO" },
  ]);
  const apiClient = Object.assign({}, mockApiClient, store.client, {
    getCurrentUser: () => Promise.resolve({ name: "Ada Admin", email: "ada@example.com", role: "ADMIN" }),
  });
  const root = {
    ApplicationPortfolioCatalog: catalogApi,
    ApplicationPortfolioApiClient: apiClient,
    document,
    localStorage: createMemoryStorage(),
  };

  await appApi.init(root);
  const usersSection = document.getElementById("users");

  const permToggle = findAll(
    usersSection,
    (node) => node.tagName === "BUTTON" && collectText(node) === "Edit permissions",
  )[0];
  assert.ok(permToggle, "an Edit permissions toggle should be rendered for the Editor");
  await permToggle.onclick();

  // A Vendor record only appears in the Edit Permission checklist (not the
  // Access Scope editor), so its checkbox is unambiguous.
  const vendorCheck = findAll(
    usersSection,
    (node) => node.tagName === "INPUT" && node.type === "checkbox" && node.value === "vendor-northstar",
  )[0];
  assert.ok(vendorCheck, "a Vendors checkbox should be rendered in the Edit Permission editor");

  vendorCheck.checked = true;
  await vendorCheck.onchange();
  assert.ok(
    store.grants.has("u-editor|VENDOR|vendor-northstar"),
    "granting should record an Edit Permission for the Editor on that Vendor",
  );
  assert.match(collectText(document.getElementById("users")), /Edit permission granted/);

  vendorCheck.checked = false;
  await vendorCheck.onchange();
  assert.equal(
    store.grants.has("u-editor|VENDOR|vendor-northstar"),
    false,
    "revoking should remove the Edit Permission",
  );
  assert.match(collectText(document.getElementById("users")), /Edit permission revoked/);
});

test("admin sees the Email Delivery screen in the empty (no relay) state", async () => {
  const document = createDocument();
  const catalog = catalogApi.createInitialCatalog();
  const mockApiClient = createMockApiClient(catalog);
  const store = createUserStore([
    { id: "u-admin", name: "Ada Admin", email: "ada@example.com", role: "ADMIN", loginMethod: "SSO" },
  ]);
  const apiClient = Object.assign({}, mockApiClient, store.client, {
    getCurrentUser: () => Promise.resolve({ name: "Ada Admin", email: "ada@example.com", role: "ADMIN" }),
    getEmailDeliveryConfig: () => Promise.resolve({ configured: false, passwordSaved: false }),
  });
  const root = {
    ApplicationPortfolioCatalog: catalogApi,
    ApplicationPortfolioApiClient: apiClient,
    document,
    localStorage: createMemoryStorage(),
  };

  const result = await appApi.init(root);
  const nav = findAll(result.root, (node) => collectText(node) === "Email Delivery" && node.tagName === "A");
  assert.equal(nav.length, 1);

  const section = document.getElementById("email-delivery");
  assert.ok(section, "email delivery section should be rendered for admins");
  const text = collectText(section);
  assert.match(text, /No relay configured/);
  assert.match(text, /Email Delivery \(SMTP Relay\)/);
});

test("admin sees the Email Delivery screen in the active state without a password value", async () => {
  const document = createDocument();
  const catalog = catalogApi.createInitialCatalog();
  const mockApiClient = createMockApiClient(catalog);
  const store = createUserStore([
    { id: "u-admin", name: "Ada Admin", email: "ada@example.com", role: "ADMIN", loginMethod: "SSO" },
  ]);
  const apiClient = Object.assign({}, mockApiClient, store.client, {
    getCurrentUser: () => Promise.resolve({ name: "Ada Admin", email: "ada@example.com", role: "ADMIN" }),
    getEmailDeliveryConfig: () =>
      Promise.resolve({
        configured: true,
        host: "smtp.example.com",
        port: 587,
        encryption: "SSL_TLS",
        authEnabled: true,
        username: "relay-user",
        fromAddress: "no-reply@ea-tool.local",
        passwordSaved: true,
      }),
  });
  const root = {
    ApplicationPortfolioCatalog: catalogApi,
    ApplicationPortfolioApiClient: apiClient,
    document,
    localStorage: createMemoryStorage(),
  };

  await appApi.init(root);
  const section = document.getElementById("email-delivery");
  assert.ok(section, "email delivery section should be rendered for admins");
  const text = collectText(section);
  assert.match(text, /SMTP relay active/);
  assert.match(text, /SSL\/TLS/);
  assert.equal(findField(section, "host").value, "smtp.example.com");
  assert.equal(String(findField(section, "port").value), "587");
  assert.equal(findField(section, "username").value, "relay-user");
  assert.equal(findField(section, "fromAddress").value, "no-reply@ea-tool.local");
  const passwordInput = findField(section, "password");
  assert.equal(passwordInput.value, "", "password value must never be pre-filled");
  assert.match(passwordInput.attributes.placeholder || "", /\(saved\)/);
});

test("admin saves a valid SMTP relay configuration and lands in the active state", async () => {
  const document = createDocument();
  const catalog = catalogApi.createInitialCatalog();
  const mockApiClient = createMockApiClient(catalog);
  const store = createUserStore([
    { id: "u-admin", name: "Ada Admin", email: "ada@example.com", role: "ADMIN", loginMethod: "SSO" },
  ]);
  let savedPayload = null;
  const apiClient = Object.assign({}, mockApiClient, store.client, {
    getCurrentUser: () => Promise.resolve({ name: "Ada Admin", email: "ada@example.com", role: "ADMIN" }),
    getEmailDeliveryConfig: () => Promise.resolve({ configured: false, passwordSaved: false }),
    saveEmailDeliveryConfig: (payload) => {
      savedPayload = payload;
      return Promise.resolve({
        configured: true,
        host: payload.host,
        port: payload.port,
        encryption: payload.encryption,
        authEnabled: payload.authEnabled,
        username: payload.username,
        fromAddress: payload.fromAddress,
        passwordSaved: true,
      });
    },
  });
  const root = {
    ApplicationPortfolioCatalog: catalogApi,
    ApplicationPortfolioApiClient: apiClient,
    document,
    localStorage: createMemoryStorage(),
  };

  await appApi.init(root);
  let section = document.getElementById("email-delivery");
  findField(section, "host").value = "smtp.example.com";
  findField(section, "port").value = "587";
  const authInput = findField(section, "authEnabled");
  authInput.checked = true;
  authInput.onchange();
  findField(section, "username").value = "relay-user";
  findField(section, "password").value = "s3cr3t-pass";
  findField(section, "fromAddress").value = "no-reply@ea-tool.local";
  const form = findAll(section, (node) => node.tagName === "FORM")[0];
  await form.onsubmit({ preventDefault() {} });

  assert.ok(savedPayload, "saveEmailDeliveryConfig should be called");
  assert.equal(savedPayload.host, "smtp.example.com");
  assert.equal(savedPayload.port, 587);
  assert.equal(savedPayload.authEnabled, true);
  assert.equal(savedPayload.username, "relay-user");
  assert.equal(savedPayload.password, "s3cr3t-pass");
  assert.equal(savedPayload.fromAddress, "no-reply@ea-tool.local");

  section = document.getElementById("email-delivery");
  assert.match(collectText(section), /SMTP relay active/);
});

test("admin sees inline validation errors and no save call for an invalid configuration", async () => {
  const document = createDocument();
  const catalog = catalogApi.createInitialCatalog();
  const mockApiClient = createMockApiClient(catalog);
  const store = createUserStore([
    { id: "u-admin", name: "Ada Admin", email: "ada@example.com", role: "ADMIN", loginMethod: "SSO" },
  ]);
  let saveCalled = false;
  const apiClient = Object.assign({}, mockApiClient, store.client, {
    getCurrentUser: () => Promise.resolve({ name: "Ada Admin", email: "ada@example.com", role: "ADMIN" }),
    getEmailDeliveryConfig: () => Promise.resolve({ configured: false, passwordSaved: false }),
    saveEmailDeliveryConfig: () => {
      saveCalled = true;
      return Promise.resolve({ configured: true, passwordSaved: false });
    },
  });
  const root = {
    ApplicationPortfolioCatalog: catalogApi,
    ApplicationPortfolioApiClient: apiClient,
    document,
    localStorage: createMemoryStorage(),
  };

  await appApi.init(root);
  const section = document.getElementById("email-delivery");
  findField(section, "host").value = "not a host";
  findField(section, "port").value = "70000";
  findField(section, "fromAddress").value = "not-an-email";
  const form = findAll(section, (node) => node.tagName === "FORM")[0];
  await form.onsubmit({ preventDefault() {} });

  assert.equal(saveCalled, false, "invalid form must not call the save API");
  const text = collectText(section);
  assert.match(text, /valid hostname/i);
  assert.match(text, /Port must be between 1 and 65535/i);
  assert.match(text, /valid from address/i);
});

test("admin keeps the current password by submitting the password field blank", async () => {
  const document = createDocument();
  const catalog = catalogApi.createInitialCatalog();
  const mockApiClient = createMockApiClient(catalog);
  const store = createUserStore([
    { id: "u-admin", name: "Ada Admin", email: "ada@example.com", role: "ADMIN", loginMethod: "SSO" },
  ]);
  let savedPayload = null;
  const apiClient = Object.assign({}, mockApiClient, store.client, {
    getCurrentUser: () => Promise.resolve({ name: "Ada Admin", email: "ada@example.com", role: "ADMIN" }),
    getEmailDeliveryConfig: () =>
      Promise.resolve({
        configured: true,
        host: "smtp.example.com",
        port: 587,
        encryption: "STARTTLS",
        authEnabled: true,
        username: "relay-user",
        fromAddress: "no-reply@ea-tool.local",
        passwordSaved: true,
      }),
    saveEmailDeliveryConfig: (payload) => {
      savedPayload = payload;
      return Promise.resolve({ configured: true, passwordSaved: true });
    },
  });
  const root = {
    ApplicationPortfolioCatalog: catalogApi,
    ApplicationPortfolioApiClient: apiClient,
    document,
    localStorage: createMemoryStorage(),
  };

  await appApi.init(root);
  const section = document.getElementById("email-delivery");
  // Leave the password field blank; auth is already enabled with a saved password.
  const form = findAll(section, (node) => node.tagName === "FORM")[0];
  await form.onsubmit({ preventDefault() {} });

  assert.ok(savedPayload, "saveEmailDeliveryConfig should be called");
  assert.equal(savedPayload.password, "", "blank password must be sent to keep the current one");
});

function createActiveEmailDeliveryAdmin(extra) {
  const document = createDocument();
  const catalog = catalogApi.createInitialCatalog();
  const mockApiClient = createMockApiClient(catalog);
  const store = createUserStore([
    { id: "u-admin", name: "Ada Admin", email: "ada@example.com", role: "ADMIN", loginMethod: "SSO" },
  ]);
  const apiClient = Object.assign({}, mockApiClient, store.client, {
    getCurrentUser: () => Promise.resolve({ name: "Ada Admin", email: "ada@example.com", role: "ADMIN" }),
    getEmailDeliveryConfig: () =>
      Promise.resolve({
        configured: true,
        host: "smtp.example.com",
        port: 587,
        encryption: "STARTTLS",
        authEnabled: true,
        username: "relay-user",
        fromAddress: "no-reply@ea-tool.local",
        passwordSaved: true,
      }),
  }, extra || {});
  const root = {
    ApplicationPortfolioCatalog: catalogApi,
    ApplicationPortfolioApiClient: apiClient,
    document,
    localStorage: createMemoryStorage(),
  };
  return { document, root };
}

test("admin opens the test-email popover and sends a test message to a recipient", async () => {
  let sentTo = null;
  const { document, root } = createActiveEmailDeliveryAdmin({
    sendTestEmail: (recipient) => {
      sentTo = recipient;
      return Promise.resolve(undefined);
    },
  });

  await appApi.init(root);
  const section = document.getElementById("email-delivery");
  const testButton = findAll(section, (node) => node.tagName === "BUTTON" && (node.textContent || "").includes("Send test email"))[0];
  assert.ok(testButton, "active state should offer a Send test email button");

  const popover = document.getElementById("email-delivery-test-popover");
  assert.equal(popover.hidden, true, "popover starts hidden");
  testButton.onclick();
  assert.equal(popover.hidden, false, "clicking opens the popover");

  findField(popover, "testRecipient").value = "person@example.com";
  const sendButton = findAll(popover, (node) => node.tagName === "BUTTON" && node.textContent === "Send")[0];
  await sendButton.onclick();

  assert.equal(sentTo, "person@example.com");
  assert.equal(popover.hidden, true, "popover closes after a successful send");
  const toast = document.getElementById("email-delivery-toast");
  assert.equal(toast.hidden, false);
  assert.match(toast.textContent, /Test email sent to person@example\.com/);
  assert.match(toast.className, /--success/);
});

test("admin sees an error toast when the test email fails", async () => {
  const { document, root } = createActiveEmailDeliveryAdmin({
    sendTestEmail: () => Promise.reject(new Error("Test email failed: Connection refused: check host and port")),
  });

  await appApi.init(root);
  const section = document.getElementById("email-delivery");
  const testButton = findAll(section, (node) => node.tagName === "BUTTON" && (node.textContent || "").includes("Send test email"))[0];
  testButton.onclick();
  const popover = document.getElementById("email-delivery-test-popover");
  findField(popover, "testRecipient").value = "person@example.com";
  const sendButton = findAll(popover, (node) => node.tagName === "BUTTON" && node.textContent === "Send")[0];
  await sendButton.onclick();

  const toast = document.getElementById("email-delivery-toast");
  assert.equal(toast.hidden, false);
  assert.match(toast.textContent, /Connection refused/);
  assert.match(toast.className, /--error/);
});

test("test-email popover validates the recipient before calling the API", async () => {
  let called = false;
  const { document, root } = createActiveEmailDeliveryAdmin({
    sendTestEmail: () => {
      called = true;
      return Promise.resolve(undefined);
    },
  });

  await appApi.init(root);
  const section = document.getElementById("email-delivery");
  const testButton = findAll(section, (node) => node.tagName === "BUTTON" && (node.textContent || "").includes("Send test email"))[0];
  testButton.onclick();
  const popover = document.getElementById("email-delivery-test-popover");
  findField(popover, "testRecipient").value = "not-an-email";
  const sendButton = findAll(popover, (node) => node.tagName === "BUTTON" && node.textContent === "Send")[0];
  await sendButton.onclick();

  assert.equal(called, false, "invalid recipient must not call the API");
  assert.match(collectText(popover), /valid recipient email/i);
});

test("admin clears the configuration after confirming and returns to the empty state", async () => {
  let cleared = false;
  const { document, root } = createActiveEmailDeliveryAdmin({
    clearEmailDeliveryConfig: () => {
      cleared = true;
      return Promise.resolve(undefined);
    },
  });

  await appApi.init(root);
  const section = document.getElementById("email-delivery");
  const clearButton = findAll(section, (node) => node.tagName === "BUTTON" && (node.textContent || "").includes("Clear configuration"))[0];
  assert.ok(clearButton, "active state should offer a Clear configuration button");

  const confirmDialog = document.getElementById("email-delivery-clear-confirm");
  assert.equal(confirmDialog.hidden, true, "confirm dialog starts hidden");
  clearButton.onclick();
  assert.equal(confirmDialog.hidden, false, "clicking opens the confirm dialog");
  assert.match(collectText(confirmDialog), /Invite emails will stop being sent/i);

  const confirmButton = findAll(confirmDialog, (node) => node.tagName === "BUTTON" && node.textContent === "Remove configuration")[0];
  await confirmButton.onclick();

  assert.equal(cleared, true, "confirming calls the clear API");
  const rendered = document.getElementById("email-delivery");
  assert.match(collectText(rendered), /No relay configured/i, "screen returns to the empty state");
  assert.ok(
    !findAll(rendered, (node) => node.tagName === "BUTTON" && (node.textContent || "").includes("Clear configuration"))[0],
    "empty state has no Clear configuration button"
  );
});

test("clear confirm dialog can be cancelled without calling the API", async () => {
  let called = false;
  const { document, root } = createActiveEmailDeliveryAdmin({
    clearEmailDeliveryConfig: () => {
      called = true;
      return Promise.resolve(undefined);
    },
  });

  await appApi.init(root);
  const section = document.getElementById("email-delivery");
  const clearButton = findAll(section, (node) => node.tagName === "BUTTON" && (node.textContent || "").includes("Clear configuration"))[0];
  clearButton.onclick();
  const confirmDialog = document.getElementById("email-delivery-clear-confirm");
  const cancelButton = findAll(confirmDialog, (node) => node.tagName === "BUTTON" && node.textContent === "Cancel")[0];
  cancelButton.onclick();

  assert.equal(called, false, "cancelling must not call the API");
  assert.equal(confirmDialog.hidden, true, "cancelling closes the confirm dialog");
});

test("empty-state screen offers no Clear configuration button", async () => {
  const { document, root } = createActiveEmailDeliveryAdmin({
    getEmailDeliveryConfig: () => Promise.resolve({ configured: false }),
  });

  await appApi.init(root);
  const section = document.getElementById("email-delivery");
  assert.ok(
    !findAll(section, (node) => node.tagName === "BUTTON" && (node.textContent || "").includes("Clear configuration"))[0],
    "no Clear configuration button when nothing is configured"
  );
  assert.ok(!document.getElementById("email-delivery-clear-confirm"), "no confirm dialog when nothing is configured");
});

test("non-admin does not get a test-email option (screen hidden)", async () => {
  const document = createDocument();
  const catalog = catalogApi.createInitialCatalog();
  const mockApiClient = createMockApiClient(catalog);
  const apiClient = Object.assign({}, mockApiClient, {
    getCurrentUser: () => Promise.resolve({ name: "Vic Viewer", email: "vic@example.com", role: "VIEWER" }),
    getEmailDeliveryConfig: () => Promise.resolve({ configured: true, passwordSaved: true }),
    sendTestEmail: () => Promise.reject(new Error("should not be called")),
  });
  const root = {
    ApplicationPortfolioCatalog: catalogApi,
    ApplicationPortfolioApiClient: apiClient,
    document,
    localStorage: createMemoryStorage(),
  };

  await appApi.init(root);
  assert.ok(!document.getElementById("email-delivery-test-popover"), "no test popover for non-admins");
});

test("non-admin does not see the Email Delivery screen", async () => {
  const document = createDocument();
  const catalog = catalogApi.createInitialCatalog();
  const mockApiClient = createMockApiClient(catalog);
  let configFetched = false;
  const apiClient = Object.assign({}, mockApiClient, {
    getCurrentUser: () => Promise.resolve({ name: "Vic Viewer", email: "vic@example.com", role: "VIEWER" }),
    getEmailDeliveryConfig: () => {
      configFetched = true;
      return Promise.resolve({ configured: false });
    },
  });
  const root = {
    ApplicationPortfolioCatalog: catalogApi,
    ApplicationPortfolioApiClient: apiClient,
    document,
    localStorage: createMemoryStorage(),
  };

  const result = await appApi.init(root);
  assert.equal(configFetched, false, "viewer should not trigger the email delivery config fetch");
  assert.ok(!document.getElementById("email-delivery"), "email delivery section must not render for non-admins");
  const nav = findAll(result.root, (node) => collectText(node) === "Email Delivery" && node.tagName === "A");
  assert.equal(nav.length, 0);
});
