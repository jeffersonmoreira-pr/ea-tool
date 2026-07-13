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
        vendorId: "vendor-northstar",
        departmentId: "dept-finance",
        businessAreaId: "area-revenue",
        lifecycleStatus: "active",
        pace: "system of record",
        criticality: "high",
        informationStatus: "verified",
      },
      {
        id: "app-field-ops",
        name: "Field Ops Portal",
        description: "Operational portal for coordinating field work and local execution.",
        vendorId: "vendor-internal",
        departmentId: "dept-operations",
        businessAreaId: "area-field",
        lifecycleStatus: "active",
        pace: "system of differentiation",
        criticality: "high",
        informationStatus: "draft",
      },
      {
        id: "app-employee-directory",
        name: "Employee Directory",
        description: "Reference application for employee lookup and organizational context.",
        vendorId: "vendor-internal",
        departmentId: "dept-people",
        businessAreaId: "area-workforce",
        lifecycleStatus: "active",
        pace: "system of record",
        criticality: "medium",
        informationStatus: "needs review",
      },
      {
        id: "app-analytics-workbench",
        name: "Analytics Workbench",
        description: "Analytical workspace for management reporting and portfolio exploration.",
        vendorId: "vendor-orbit",
        departmentId: "dept-finance",
        businessAreaId: "area-revenue",
        lifecycleStatus: "planned",
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

  function normalizeCatalog(catalog) {
    const source = catalog && typeof catalog === "object" ? catalog : {};
    return {
      vendors: Array.isArray(source.vendors) ? source.vendors.map(normalizeVendor) : [],
      departments: Array.isArray(source.departments) ? source.departments.map(clone) : [],
      businessAreas: Array.isArray(source.businessAreas) ? source.businessAreas.map(clone) : [],
      applications: Array.isArray(source.applications) ? source.applications.map(clone) : [],
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
    const nextName = String(name || "").trim();
    if (!nextName) {
      throw new Error("Application name is required.");
    }
    const application = catalog.applications.find((candidate) => candidate.id === applicationId);
    if (!application) {
      throw new Error(`Application not found: ${applicationId}`);
    }
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
    createBusinessArea,
    createDepartment,
    createInitialCatalog,
    createVendor,
    deleteBusinessArea,
    deleteDepartment,
    deleteVendor,
    getNavigationSections,
    getVendorDisplayType,
    loadCatalog,
    saveCatalog,
    updateBusinessArea,
    updateApplicationName,
    updateDepartment,
    updateVendor,
  };
});
