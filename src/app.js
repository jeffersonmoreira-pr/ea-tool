(function attachApp(root, factory) {
  const api = factory();
  if (typeof module === "object" && module.exports) {
    module.exports = api;
  }
  if (root) {
    root.ApplicationPortfolioApp = api;
    if (root.document && typeof root.document.addEventListener === "function") {
      root.document.addEventListener("DOMContentLoaded", function onReady() {
        api.init(root);
      });
    }
  }
})(typeof globalThis !== "undefined" ? globalThis : undefined, function createAppApi() {
  function makeElement(document, tagName, options) {
    const element = document.createElement(tagName);
    const settings = options || {};
    if (settings.className) {
      element.className = settings.className;
    }
    if (settings.text) {
      element.textContent = settings.text;
    }
    if (settings.type) {
      element.type = settings.type;
    }
    if (settings.value) {
      element.value = settings.value;
    }
    if (settings.name) {
      element.name = settings.name;
    }
    if (settings.id) {
      element.id = settings.id;
    }
    if (settings.attributes) {
      for (const [name, value] of Object.entries(settings.attributes)) {
        element.setAttribute(name, value);
      }
    }
    return element;
  }

  function appendTextBlock(document, parent, tagName, className, text) {
    parent.appendChild(makeElement(document, tagName, { className, text }));
  }

  function findById(records, id) {
    return records.find((record) => record.id === id);
  }

  function renderNavigation(document, catalogApi) {
    const nav = makeElement(document, "nav", {
      className: "portfolio-nav",
      attributes: { "aria-label": "Catalog sections" },
    });
    for (const section of catalogApi.getNavigationSections()) {
      const item = makeElement(document, "a", {
        className: "portfolio-nav__item",
        text: section.label,
        attributes: { href: `#${section.id}` },
      });
      nav.appendChild(item);
    }
    return nav;
  }

  function renderOverview(document, catalog) {
    const section = makeElement(document, "section", {
      className: "overview-band",
      attributes: { id: "overview" },
    });
    const copy = makeElement(document, "div", { className: "overview-band__copy" });
    appendTextBlock(document, copy, "p", "eyebrow", "Application Portfolio");
    appendTextBlock(document, copy, "h1", "page-title", "Strategic catalog, local-first");
    appendTextBlock(
      document,
      copy,
      "p",
      "lede",
      "A browser-only MVP for reviewing Applications, Vendors, Departments, and Business Areas with local persistence.",
    );
    section.appendChild(copy);

    const metrics = makeElement(document, "div", { className: "metric-strip" });
    const items = [
      ["Applications", catalog.applications.length],
      ["Vendors", catalog.vendors.length],
      ["Departments", catalog.departments.length],
      ["Business Areas", catalog.businessAreas.length],
    ];
    for (const [label, value] of items) {
      const metric = makeElement(document, "div", { className: "metric" });
      appendTextBlock(document, metric, "strong", "metric__value", String(value));
      appendTextBlock(document, metric, "span", "metric__label", label);
      metrics.appendChild(metric);
    }
    section.appendChild(metrics);
    return section;
  }

  function renderApplications(document, catalog, catalogApi, storage, rerender) {
    const section = makeElement(document, "section", {
      className: "content-section",
      attributes: { id: "applications" },
    });
    appendTextBlock(document, section, "h2", "section-title", "Applications");

    const list = makeElement(document, "div", { className: "application-grid" });
    for (const application of catalog.applications) {
      const vendor = findById(catalog.vendors, application.vendorId);
      const department = findById(catalog.departments, application.departmentId);
      const businessArea = findById(catalog.businessAreas, application.businessAreaId);
      const card = makeElement(document, "article", { className: "application-card" });
      appendTextBlock(document, card, "h3", "application-card__title", application.name);
      appendTextBlock(document, card, "p", "application-card__description", application.description);

      const meta = makeElement(document, "dl", { className: "meta-list" });
      for (const [term, description] of [
        ["Vendor", vendor ? vendor.name : "Unknown"],
        ["Department", department ? department.name : "Unknown"],
        ["Business Area", businessArea ? businessArea.name : "Unknown"],
        ["Lifecycle", application.lifecycleStatus],
        ["PACE", application.pace],
        ["Criticality", application.criticality],
      ]) {
        appendTextBlock(document, meta, "dt", "meta-list__term", term);
        appendTextBlock(document, meta, "dd", "meta-list__value", description);
      }
      card.appendChild(meta);

      if (application.id === "app-revenue-hub") {
        const form = makeElement(document, "form", { className: "edit-form" });
        const label = makeElement(document, "label", {
          className: "edit-form__label",
          text: "Application name",
          attributes: { for: "application-name-input" },
        });
        const input = makeElement(document, "input", {
          id: "application-name-input",
          name: "applicationName",
          type: "text",
          value: application.name,
        });
        const button = makeElement(document, "button", { type: "submit", text: "Save" });
        form.append(label, input, button);
        form.addEventListener("submit", function onSubmit(event) {
          if (event && typeof event.preventDefault === "function") {
            event.preventDefault();
          }
          catalogApi.updateApplicationName(catalog, application.id, input.value);
          catalogApi.saveCatalog(storage, catalog);
          rerender(catalog);
        });
        card.appendChild(form);
      }

      list.appendChild(card);
    }
    section.appendChild(list);
    return section;
  }

  function renderMasterData(document, id, title, records) {
    const section = makeElement(document, "section", {
      className: "content-section content-section--compact",
      attributes: { id },
    });
    appendTextBlock(document, section, "h2", "section-title", title);
    const list = makeElement(document, "ul", { className: "master-list" });
    for (const record of records) {
      const item = makeElement(document, "li", { className: "master-list__item" });
      appendTextBlock(document, item, "span", "master-list__name", record.name);
      if (record.type) {
        appendTextBlock(document, item, "small", "master-list__note", record.type);
      }
      list.appendChild(item);
    }
    section.appendChild(list);
    return section;
  }

  function renderApp(options) {
    const document = options.document;
    const storage = options.storage;
    const catalogApi = options.catalogApi;
    const root = options.root || document.getElementById("app");
    const catalog = options.catalog || catalogApi.loadCatalog(storage);

    function rerender(nextCatalog) {
      renderApp({ document, storage, catalogApi, root, catalog: nextCatalog });
    }

    root.replaceChildren(
      renderNavigation(document, catalogApi),
      renderOverview(document, catalog),
      renderApplications(document, catalog, catalogApi, storage, rerender),
      renderMasterData(document, "vendors", "Vendors", catalog.vendors),
      renderMasterData(document, "departments", "Departments", catalog.departments),
      renderMasterData(document, "business-areas", "Business Areas", catalog.businessAreas),
    );
    return { root, catalog };
  }

  function init(root) {
    const catalogApi = root.ApplicationPortfolioCatalog;
    const document = root.document;
    const storage = root.localStorage;
    if (!catalogApi || !document) {
      return null;
    }
    return renderApp({ document, storage, catalogApi });
  }

  return { init, renderApp };
});
