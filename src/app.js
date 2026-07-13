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
    if (typeof settings.checked === "boolean") {
      element.checked = settings.checked;
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

  function renderMasterData(document, catalog, catalogApi, storage, rerender, config) {
    const section = makeElement(document, "section", {
      className: "content-section content-section--compact",
      attributes: { id: config.id },
    });
    appendTextBlock(document, section, "h2", "section-title", config.title);

    const status = makeElement(document, "p", {
      className: "master-data-status",
      attributes: { role: "status" },
    });
    section.appendChild(status);

    const createForm = makeElement(document, "form", { className: "master-data-form" });
    const createNameId = `${config.id}-create-name`;
    const createLabel = makeElement(document, "label", {
      className: "master-data-form__label",
      text: `${config.singular} name`,
      attributes: { for: createNameId },
    });
    const createName = makeElement(document, "input", {
      id: createNameId,
      name: "name",
      type: "text",
      attributes: { autocomplete: "off" },
    });
    createForm.append(createLabel, createName);
    let createInternal = null;
    if (config.kind === "vendor") {
      const checkboxLabel = makeElement(document, "label", { className: "master-data-check" });
      createInternal = makeElement(document, "input", {
        name: "isInternal",
        type: "checkbox",
        checked: false,
      });
      checkboxLabel.append(createInternal, document.createTextNode("Internal Vendor"));
      createForm.appendChild(checkboxLabel);
    }
    const createButton = makeElement(document, "button", { type: "submit", text: `Add ${config.singular}` });
    createForm.appendChild(createButton);
    createForm.addEventListener("submit", function onSubmit(event) {
      if (event && typeof event.preventDefault === "function") {
        event.preventDefault();
      }
      try {
        config.create(catalog, {
          name: createName.value,
          isInternal: createInternal ? createInternal.checked === true : undefined,
        });
        catalogApi.saveCatalog(storage, catalog);
        rerender(catalog);
      } catch (error) {
        status.textContent = error.message;
      }
    });
    section.appendChild(createForm);

    const list = makeElement(document, "ul", { className: "master-list master-list--editable" });
    for (const record of config.records(catalog)) {
      const item = makeElement(document, "li", { className: "master-list__item master-list__item--editable" });
      appendTextBlock(document, item, "span", "master-list__name", record.name);
      if (config.describe) {
        appendTextBlock(document, item, "small", "master-list__note", config.describe(record));
      }
      const editForm = makeElement(document, "form", { className: "master-data-row-form" });
      const editName = makeElement(document, "input", {
        name: "name",
        type: "text",
        value: record.name,
        attributes: { "aria-label": `${config.singular} name` },
      });
      editForm.appendChild(editName);
      let editInternal = null;
      if (config.kind === "vendor") {
        const editCheckLabel = makeElement(document, "label", { className: "master-data-check" });
        editInternal = makeElement(document, "input", {
          name: "isInternal",
          type: "checkbox",
          checked: record.isInternal === true,
        });
        editCheckLabel.append(editInternal, document.createTextNode("Internal Vendor"));
        editForm.appendChild(editCheckLabel);
      }
      const saveButton = makeElement(document, "button", { type: "submit", text: "Save" });
      editForm.appendChild(saveButton);
      editForm.addEventListener("submit", function onEdit(event) {
        if (event && typeof event.preventDefault === "function") {
          event.preventDefault();
        }
        try {
          config.update(catalog, record.id, {
            name: editName.value,
            isInternal: editInternal ? editInternal.checked === true : undefined,
          });
          catalogApi.saveCatalog(storage, catalog);
          rerender(catalog);
        } catch (error) {
          status.textContent = error.message;
        }
      });
      const deleteButton = makeElement(document, "button", {
        className: "master-data-delete",
        type: "button",
        text: "Delete",
      });
      deleteButton.addEventListener("click", function onDelete() {
        try {
          config.delete(catalog, record.id);
          catalogApi.saveCatalog(storage, catalog);
          rerender(catalog);
        } catch (error) {
          status.textContent = error.message;
        }
      });
      const actions = makeElement(document, "div", { className: "master-data-actions" });
      actions.append(editForm, deleteButton);
      item.appendChild(actions);
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
      renderMasterData(document, catalog, catalogApi, storage, rerender, {
        id: "vendors",
        title: "Vendors",
        singular: "Vendor",
        kind: "vendor",
        records: (currentCatalog) => currentCatalog.vendors,
        create: catalogApi.createVendor,
        update: catalogApi.updateVendor,
        delete: catalogApi.deleteVendor,
        describe: catalogApi.getVendorDisplayType,
      }),
      renderMasterData(document, catalog, catalogApi, storage, rerender, {
        id: "departments",
        title: "Departments",
        singular: "Department",
        records: (currentCatalog) => currentCatalog.departments,
        create: catalogApi.createDepartment,
        update: catalogApi.updateDepartment,
        delete: catalogApi.deleteDepartment,
      }),
      renderMasterData(document, catalog, catalogApi, storage, rerender, {
        id: "business-areas",
        title: "Business Areas",
        singular: "Business Area",
        records: (currentCatalog) => currentCatalog.businessAreas,
        create: catalogApi.createBusinessArea,
        update: catalogApi.updateBusinessArea,
        delete: catalogApi.deleteBusinessArea,
      }),
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
