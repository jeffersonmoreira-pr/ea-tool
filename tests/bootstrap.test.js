const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const test = require("node:test");

const catalogApi = require("../src/catalog.js");
const appApi = require("../src/app.js");

const root = path.resolve(__dirname, "..");

function readSource(filePath) {
  return fs.readFileSync(path.join(root, filePath), "utf8");
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

test("static shell uses only local assets and source has no service dependency references", () => {
  const html = readSource("src/index.html");
  assert.match(html, /href="\.\/styles\.css"/);
  assert.match(html, /src="\.\/catalog\.js"/);
  assert.match(html, /src="\.\/app\.js"/);
  assert.doesNotMatch(html, /https?:\/\//);

  const source = ["src/index.html", "src/catalog.js", "src/app.js"].map(readSource).join("\n");
  const serviceReferenceCount = (source.match(/\bbackend\b/gi) || []).length;
  assert.equal(serviceReferenceCount, 0);
  assert.doesNotMatch(source, /\b(fetch|XMLHttpRequest)\b/);
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

test("catalog persists application name changes in browser storage", () => {
  const storage = createMemoryStorage();
  const catalog = catalogApi.loadCatalog(storage);
  catalogApi.updateApplicationName(catalog, "app-revenue-hub", "Revenue Hub Updated");
  catalogApi.saveCatalog(storage, catalog);

  const reloadedStorage = createMemoryStorage(storage.snapshot());
  const reloaded = catalogApi.loadCatalog(reloadedStorage);
  assert.equal(reloaded.applications[0].name, "Revenue Hub Updated");
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

test("navigation sections match the MVP catalog areas", () => {
  assert.deepEqual(catalogApi.getNavigationSections().map((section) => section.label), [
    "Executive Overview",
    "Applications",
    "Vendors",
    "Departments",
    "Business Areas",
  ]);
});

test("browser adapter renders the initial catalog shell without network calls", () => {
  const document = createDocument();
  const storage = createMemoryStorage();
  const networkCalls = [];
  const rendered = appApi.renderApp({
    document,
    storage,
    catalogApi,
    network: {
      fetch(...args) {
        networkCalls.push(args);
      },
    },
  });

  const text = collectText(rendered.root);
  assert.match(text, /Executive Overview/);
  assert.match(text, /Revenue Hub/);
  assert.match(text, /Vendors/);
  assert.equal(networkCalls.length, 0);
});
