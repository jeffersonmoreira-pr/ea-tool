(function attachCatalog(root, factory) {
  const api = factory();
  if (typeof module === "object" && module.exports) {
    module.exports = api;
  }
  if (root) {
    root.ApplicationPortfolioCatalog = api;
  }
})(typeof globalThis !== "undefined" ? globalThis : undefined, function createCatalogApi() {
  const CATALOG_STORAGE_KEY = "application-portfolio.catalog.v1";

  const navigationSections = [
    { id: "overview", label: "Executive Overview" },
    { id: "applications", label: "Applications" },
    { id: "vendors", label: "Vendors" },
    { id: "departments", label: "Departments" },
    { id: "business-areas", label: "Business Areas" },
  ];

  const seedCatalog = {
    vendors: [
      { id: "vendor-internal", name: "Internal Digital Team", type: "Internal Vendor" },
      { id: "vendor-northstar", name: "Northstar Software", type: "External Vendor" },
      { id: "vendor-orbit", name: "Orbit Analytics", type: "External Vendor" },
    ],
    departments: [
      { id: "dept-finance", name: "Finance" },
      { id: "dept-operations", name: "Operations" },
      { id: "dept-people", name: "People" },
    ],
    businessAreas: [
      { id: "area-revenue", name: "Revenue Management" },
      { id: "area-field", name: "Field Operations" },
      { id: "area-workforce", name: "Workforce Services" },
    ],
    applications: [
      {
        id: "app-revenue-hub",
        name: "Revenue Hub",
        description: "Portfolio application for revenue planning and invoicing visibility.",
        aliases: ["RevHub", "Revenue Planning"],
        applicationUrl: "https://revenue.example.local",
        diagnosticUrl: "",
        businessOwnerName: "Maya Chen",
        businessOwnerEmail: "maya.chen@example.local",
        techOwnerName: "Theo Ramos",
        techOwnerEmail: "theo.ramos@example.local",
        vendorId: "vendor-northstar",
        departmentId: "dept-finance",
        businessAreaId: "area-revenue",
        lifecycleStatus: "active",
        plannedDate: "",
        retirementDate: "",
        businessFit: 4,
        techFit: "high",
        pace: "system of record",
        criticality: "high",
        informationStatus: "verified",
      },
      {
        id: "app-field-ops",
        name: "Field Ops Portal",
        description: "Operational portal for coordinating field work and local execution.",
        aliases: ["Field Portal"],
        applicationUrl: "",
        diagnosticUrl: "",
        businessOwnerName: "Rui Costa",
        businessOwnerEmail: "",
        techOwnerName: "Ana Silva",
        techOwnerEmail: "",
        vendorId: "vendor-internal",
        departmentId: "dept-operations",
        businessAreaId: "area-field",
        lifecycleStatus: "active",
        plannedDate: "",
        retirementDate: "",
        businessFit: 3,
        techFit: "medium",
        pace: "system of differentiation",
        criticality: "high",
        informationStatus: "draft",
      },
      {
        id: "app-employee-directory",
        name: "Employee Directory",
        description: "Reference application for employee lookup and organizational context.",
        aliases: ["People Directory"],
        applicationUrl: "",
        diagnosticUrl: "",
        businessOwnerName: "Nora Patel",
        businessOwnerEmail: "",
        techOwnerName: "Ilya Novak",
        techOwnerEmail: "",
        vendorId: "vendor-internal",
        departmentId: "dept-people",
        businessAreaId: "area-workforce",
        lifecycleStatus: "active",
        plannedDate: "",
        retirementDate: "",
        businessFit: 2,
        techFit: "medium",
        pace: "system of record",
        criticality: "medium",
        informationStatus: "needs review",
      },
      {
        id: "app-analytics-workbench",
        name: "Analytics Workbench",
        description: "Analytical workspace for management reporting and portfolio exploration.",
        aliases: ["Analytics Lab"],
        applicationUrl: "",
        diagnosticUrl: "https://diagnostics.example.local/analytics",
        businessOwnerName: "Priya Shah",
        businessOwnerEmail: "priya.shah@example.local",
        techOwnerName: "Mateo Alves",
        techOwnerEmail: "",
        vendorId: "vendor-orbit",
        departmentId: "dept-finance",
        businessAreaId: "area-revenue",
        lifecycleStatus: "planned",
        plannedDate: "2026-08-01",
        retirementDate: "",
        businessFit: 5,
        techFit: "low",
        pace: "system of innovation",
        criticality: "medium",
        informationStatus: "draft",
      },
    ],
  };

  function clone(value) {
    return JSON.parse(JSON.stringify(value));
  }

  function normalizeVendor(vendor) {
    const isInternal =
      typeof vendor.isInternal === "boolean"
        ? vendor.isInternal
        : vendor.type === "Internal Vendor";
    return {
      id: vendor.id,
      name: vendor.name,
      isInternal,
    };
  }

  function normalizeText(value) {
    return String(value || "").trim();
  }

  function normalizeAliases(aliases) {
    if (Array.isArray(aliases)) {
      return aliases.map(normalizeText).filter(Boolean);
    }
    return normalizeText(aliases)
      .split(",")
      .map(normalizeText)
      .filter(Boolean);
  }

  function normalizeApplication(application) {
    const lifecycleStatus = normalizeLifecycleStatus(application.lifecycleStatus);
    const businessFit = normalizeBusinessFit(application.businessFit === undefined ? 3 : application.businessFit);
    const techFit = normalizeTechFit(application.techFit === undefined ? "medium" : application.techFit);
    const businessFitBand = deriveBusinessFitBand(businessFit);
    return {
      ...clone(application),
      aliases: normalizeAliases(application.aliases),
      applicationUrl: normalizeText(application.applicationUrl),
      diagnosticUrl: normalizeText(application.diagnosticUrl),
      businessOwnerName: normalizeText(application.businessOwnerName),
      businessOwnerEmail: normalizeText(application.businessOwnerEmail),
      techOwnerName: normalizeText(application.techOwnerName),
      techOwnerEmail: normalizeText(application.techOwnerEmail),
      lifecycleStatus,
      plannedDate: normalizeText(application.plannedDate),
      retirementDate: normalizeText(application.retirementDate),
      businessFit,
      businessFitBand,
      techFit,
      timeClassification: deriveTimeClassification(businessFitBand, techFit),
    };
  }

  function normalizeCatalog(catalog) {
    const source = catalog && typeof catalog === "object" ? catalog : {};
    return {
      vendors: Array.isArray(source.vendors) ? source.vendors.map(normalizeVendor) : [],
      departments: Array.isArray(source.departments) ? source.departments.map(clone) : [],
      businessAreas: Array.isArray(source.businessAreas) ? source.businessAreas.map(clone) : [],
      applications: Array.isArray(source.applications) ? source.applications.map(normalizeApplication) : [],
    };
  }

  function createInitialCatalog() {
    return normalizeCatalog(seedCatalog);
  }

  function loadCatalog(storage) {
    if (!storage || typeof storage.getItem !== "function") {
      return createInitialCatalog();
    }

    const raw = storage.getItem(CATALOG_STORAGE_KEY);
    if (!raw) {
      const catalog = createInitialCatalog();
      saveCatalog(storage, catalog);
      return catalog;
    }

    try {
      const parsed = JSON.parse(raw);
      if (!parsed || !Array.isArray(parsed.applications)) {
        return createInitialCatalog();
      }
      return normalizeCatalog(parsed);
    } catch (_error) {
      return createInitialCatalog();
    }
  }

  function saveCatalog(storage, catalog) {
    if (!storage || typeof storage.setItem !== "function") {
      return;
    }
    storage.setItem(CATALOG_STORAGE_KEY, JSON.stringify(catalog));
  }

  function updateApplicationName(catalog, applicationId, name) {
    const application = catalog.applications.find((candidate) => candidate.id === applicationId);
    if (!application) {
      throw new Error(`Application not found: ${applicationId}`);
    }
    const nextName = normalizeName(name, "Application name is required.");
    assertUniqueName(catalog.applications, nextName, applicationId, "Application name must be unique.");
    application.name = nextName;
    return application;
  }

  function normalizeName(name, message) {
    const nextName = String(name || "").trim();
    if (!nextName) {
      throw new Error(message);
    }
    return nextName;
  }

  function nameKey(name) {
    return String(name || "").trim().toLocaleLowerCase();
  }

  function assertUniqueName(records, name, currentId, message) {
    const nextKey = nameKey(name);
    const duplicate = records.find((record) => record.id !== currentId && nameKey(record.name) === nextKey);
    if (duplicate) {
      throw new Error(message);
    }
  }

  function slugify(name) {
    return nameKey(name)
      .replace(/[^a-z0-9]+/g, "-")
      .replace(/^-+|-+$/g, "");
  }

  function createId(records, prefix, name) {
    const base = `${prefix}-${slugify(name) || "item"}`;
    const existing = new Set(records.map((record) => record.id));
    if (!existing.has(base)) {
      return base;
    }
    let suffix = 2;
    while (existing.has(`${base}-${suffix}`)) {
      suffix += 1;
    }
    return `${base}-${suffix}`;
  }

  function findRecord(records, id, label) {
    const record = records.find((candidate) => candidate.id === id);
    if (!record) {
      throw new Error(`${label} not found: ${id}`);
    }
    return record;
  }

  function requireReference(records, id, label) {
    const nextId = normalizeText(id);
    if (!nextId) {
      throw new Error(`${label} is required.`);
    }
    findRecord(records, nextId, label);
    return nextId;
  }

  function normalizeLifecycleStatus(value) {
    const lifecycleStatus = normalizeText(value) || "active";
    if (!["planned", "active", "retiring", "retired"].includes(lifecycleStatus)) {
      throw new Error("Lifecycle Status must be planned, active, retiring, or retired.");
    }
    return lifecycleStatus;
  }

  function normalizeBusinessFit(value) {
    const businessFit = Number(normalizeText(value));
    if (!Number.isInteger(businessFit) || businessFit < 1 || businessFit > 5) {
      throw new Error("Business Fit must be 1, 2, 3, 4, or 5.");
    }
    return businessFit;
  }

  function normalizeTechFit(value) {
    const techFit = normalizeText(value).toLocaleLowerCase();
    if (!["low", "medium", "high"].includes(techFit)) {
      throw new Error("Tech Fit must be low, medium, or high.");
    }
    return techFit;
  }

  function deriveBusinessFitBand(businessFit) {
    if (businessFit <= 2) {
      return "low";
    }
    if (businessFit === 3) {
      return "medium";
    }
    return "high";
  }

  function deriveTimeClassification(businessFitBand, techFit) {
    const matrix = {
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
    return matrix[`${businessFitBand}/${techFit}`];
  }

  function normalizeLifecycleInput(input) {
    const lifecycleStatus = normalizeLifecycleStatus(input && input.lifecycleStatus);
    const plannedDate = normalizeText(input && input.plannedDate);
    const retirementDate = normalizeText(input && input.retirementDate);
    if (lifecycleStatus === "planned" && !plannedDate) {
      throw new Error("Planned Date is required for planned Applications.");
    }
    if (["retiring", "retired"].includes(lifecycleStatus) && !retirementDate) {
      throw new Error(`Retirement Date is required for ${lifecycleStatus} Applications.`);
    }
    if (plannedDate && !isValidIsoDate(plannedDate)) {
      throw new Error("Planned Date must be a valid date.");
    }
    if (retirementDate && !isValidIsoDate(retirementDate)) {
      throw new Error("Retirement Date must be a valid date.");
    }
    return {
      lifecycleStatus,
      plannedDate,
      retirementDate,
    };
  }

  function isValidIsoDate(value) {
    if (!/^\d{4}-\d{2}-\d{2}$/.test(value)) {
      return false;
    }
    const date = new Date(`${value}T00:00:00.000Z`);
    return !Number.isNaN(date.getTime()) && date.toISOString().slice(0, 10) === value;
  }

  function normalizeApplicationInput(catalog, input, currentId) {
    const name = normalizeName(input && input.name, "Application name is required.");
    assertUniqueName(catalog.applications, name, currentId, "Application name must be unique.");
    const description = normalizeName(input && input.description, "Application description is required.");
    const businessOwnerName = normalizeName(input && input.businessOwnerName, "Business Owner Name is required.");
    const techOwnerName = normalizeName(input && input.techOwnerName, "Tech Owner Name is required.");
    const businessFit = normalizeBusinessFit(input && input.businessFit);
    const techFit = normalizeTechFit(input && input.techFit);
    const businessFitBand = deriveBusinessFitBand(businessFit);

    return {
      name,
      description,
      aliases: normalizeAliases(input && input.aliases),
      applicationUrl: normalizeText(input && input.applicationUrl),
      diagnosticUrl: normalizeText(input && input.diagnosticUrl),
      businessOwnerName,
      businessOwnerEmail: normalizeText(input && input.businessOwnerEmail),
      techOwnerName,
      techOwnerEmail: normalizeText(input && input.techOwnerEmail),
      vendorId: requireReference(catalog.vendors, input && input.vendorId, "Vendor"),
      departmentId: requireReference(catalog.departments, input && input.departmentId, "Department"),
      businessAreaId: requireReference(catalog.businessAreas, input && input.businessAreaId, "Business Area"),
      ...normalizeLifecycleInput(input),
      businessFit,
      businessFitBand,
      techFit,
      timeClassification: deriveTimeClassification(businessFitBand, techFit),
    };
  }

  function findReferencingApplication(catalog, fieldName, recordId) {
    return catalog.applications.find((application) => application[fieldName] === recordId);
  }

  function assertNotReferenced(catalog, fieldName, recordId, messagePrefix) {
    const application = findReferencingApplication(catalog, fieldName, recordId);
    if (application) {
      throw new Error(`${messagePrefix} is in use by Application: ${application.name}.`);
    }
  }

  function createVendor(catalog, input) {
    if (!input || typeof input.isInternal !== "boolean") {
      throw new Error("Vendor internal status is required.");
    }
    const name = normalizeName(input.name, "Vendor name is required.");
    assertUniqueName(catalog.vendors, name, null, "Vendor name must be unique.");
    const vendor = {
      id: createId(catalog.vendors, "vendor", name),
      name,
      isInternal: input.isInternal,
    };
    catalog.vendors.push(vendor);
    return vendor;
  }

  function createApplication(catalog, input) {
    const values = normalizeApplicationInput(catalog, input, null);
    const application = {
      id: createId(catalog.applications, "app", values.name),
      ...values,
      pace: normalizeText(input && input.pace) || "system of record",
      criticality: normalizeText(input && input.criticality) || "medium",
      informationStatus: normalizeText(input && input.informationStatus) || "draft",
    };
    catalog.applications.push(application);
    return application;
  }

  function updateApplication(catalog, applicationId, input) {
    const application = findRecord(catalog.applications, applicationId, "Application");
    const values = normalizeApplicationInput(catalog, input, applicationId);
    Object.assign(application, values, {
      pace: normalizeText(input && input.pace) || application.pace,
      criticality: normalizeText(input && input.criticality) || application.criticality,
      informationStatus: normalizeText(input && input.informationStatus) || application.informationStatus,
    });
    return application;
  }

  function deleteApplication(catalog, applicationId) {
    const application = findRecord(catalog.applications, applicationId, "Application");
    catalog.applications.splice(catalog.applications.indexOf(application), 1);
    return application;
  }

  function updateVendor(catalog, vendorId, input) {
    if (!input || typeof input.isInternal !== "boolean") {
      throw new Error("Vendor internal status is required.");
    }
    const vendor = findRecord(catalog.vendors, vendorId, "Vendor");
    const name = normalizeName(input.name, "Vendor name is required.");
    assertUniqueName(catalog.vendors, name, vendorId, "Vendor name must be unique.");
    vendor.name = name;
    vendor.isInternal = input.isInternal;
    return vendor;
  }

  function deleteVendor(catalog, vendorId) {
    assertNotReferenced(catalog, "vendorId", vendorId, "Vendor");
    const vendor = findRecord(catalog.vendors, vendorId, "Vendor");
    catalog.vendors.splice(catalog.vendors.indexOf(vendor), 1);
    return vendor;
  }

  function getVendorDisplayType(vendor) {
    return vendor && vendor.isInternal ? "Internal Vendor" : "External Vendor";
  }

  function createDepartment(catalog, input) {
    const name = normalizeName(input && input.name, "Department name is required.");
    assertUniqueName(catalog.departments, name, null, "Department name must be unique.");
    const department = {
      id: createId(catalog.departments, "dept", name),
      name,
    };
    catalog.departments.push(department);
    return department;
  }

  function updateDepartment(catalog, departmentId, input) {
    const department = findRecord(catalog.departments, departmentId, "Department");
    const name = normalizeName(input && input.name, "Department name is required.");
    assertUniqueName(catalog.departments, name, departmentId, "Department name must be unique.");
    department.name = name;
    return department;
  }

  function deleteDepartment(catalog, departmentId) {
    assertNotReferenced(catalog, "departmentId", departmentId, "Department");
    const department = findRecord(catalog.departments, departmentId, "Department");
    catalog.departments.splice(catalog.departments.indexOf(department), 1);
    return department;
  }

  function createBusinessArea(catalog, input) {
    const name = normalizeName(input && input.name, "Business Area name is required.");
    assertUniqueName(catalog.businessAreas, name, null, "Business Area name must be unique.");
    const businessArea = {
      id: createId(catalog.businessAreas, "area", name),
      name,
    };
    catalog.businessAreas.push(businessArea);
    return businessArea;
  }

  function updateBusinessArea(catalog, businessAreaId, input) {
    const businessArea = findRecord(catalog.businessAreas, businessAreaId, "Business Area");
    const name = normalizeName(input && input.name, "Business Area name is required.");
    assertUniqueName(catalog.businessAreas, name, businessAreaId, "Business Area name must be unique.");
    businessArea.name = name;
    return businessArea;
  }

  function deleteBusinessArea(catalog, businessAreaId) {
    assertNotReferenced(catalog, "businessAreaId", businessAreaId, "Business Area");
    const businessArea = findRecord(catalog.businessAreas, businessAreaId, "Business Area");
    catalog.businessAreas.splice(catalog.businessAreas.indexOf(businessArea), 1);
    return businessArea;
  }

  function getNavigationSections() {
    return clone(navigationSections);
  }

  return {
    CATALOG_STORAGE_KEY,
    createApplication,
    createBusinessArea,
    createDepartment,
    createInitialCatalog,
    createVendor,
    deleteApplication,
    deleteBusinessArea,
    deleteDepartment,
    deleteVendor,
    getNavigationSections,
    getVendorDisplayType,
    loadCatalog,
    saveCatalog,
    updateApplication,
    updateBusinessArea,
    updateApplicationName,
    updateDepartment,
    updateVendor,
  };
});
