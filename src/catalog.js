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

  function createInitialCatalog() {
    return clone(seedCatalog);
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
      return parsed;
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

  function getNavigationSections() {
    return clone(navigationSections);
  }

  return {
    CATALOG_STORAGE_KEY,
    createInitialCatalog,
    getNavigationSections,
    loadCatalog,
    saveCatalog,
    updateApplicationName,
  };
});
