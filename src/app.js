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

  function makeBadge(document, group, value) {
    const slug = String(value)
      .toLowerCase()
      .replace(/[^a-z0-9]+/g, "-")
      .replace(/(^-|-$)/g, "");
    return makeElement(document, "span", {
      className: `badge badge--${group} badge--${group}-${slug}`,
      text: value,
    });
  }

  function findById(records, id) {
    return records.find((record) => record.id === id);
  }

  function formatAliases(aliases) {
    return Array.isArray(aliases) ? aliases.join(", ") : "";
  }

  function parseAliases(value) {
    return String(value || "")
      .split(",")
      .map((item) => item.trim())
      .filter(Boolean);
  }

  function appendApplicationField(document, form, labelText, name, value, options) {
    const settings = options || {};
    const fieldId = `${settings.prefix}-${name}`;
    const field = makeElement(document, "div", {
      className: settings.full ? "form-field form-field--full" : "form-field",
    });
    const label = makeElement(document, "label", {
      className: "form-field__label",
      text: labelText,
      attributes: { for: fieldId },
    });
    const input = makeElement(document, settings.multiline ? "textarea" : "input", {
      id: fieldId,
      name,
      type: settings.type || "text",
      value: value || "",
      attributes: settings.required ? { required: "required" } : undefined,
    });
    field.append(label, input);
    form.appendChild(field);
    return input;
  }

  function appendApplicationSelect(document, form, labelText, name, records, selectedId, prefix) {
    const fieldId = `${prefix}-${name}`;
    const field = makeElement(document, "div", { className: "form-field" });
    const label = makeElement(document, "label", {
      className: "form-field__label",
      text: labelText,
      attributes: { for: fieldId },
    });
    const select = makeElement(document, "select", {
      id: fieldId,
      name,
      value: selectedId,
      attributes: { required: "required" },
    });
    for (const record of records) {
      const option = makeElement(document, "option", {
        text: record.name,
        value: record.id,
        attributes: { value: record.id },
      });
      if (record.id === selectedId) {
        option.selected = true;
      }
      select.appendChild(option);
    }
    field.append(label, select);
    form.appendChild(field);
    return select;
  }

  function appendApplicationLifecycleStatus(document, form, selectedId, prefix) {
    appendApplicationSelect(
      document,
      form,
      "Lifecycle Status",
      "lifecycleStatus",
      [
        { id: "planned", name: "planned" },
        { id: "active", name: "active" },
        { id: "retiring", name: "retiring" },
        { id: "retired", name: "retired" },
      ],
      selectedId || "active",
      prefix,
    );
  }

  function appendApplicationBusinessFit(document, form, selectedId, prefix) {
    appendApplicationSelect(
      document,
      form,
      "Business Fit",
      "businessFit",
      [
        { id: "1", name: "1" },
        { id: "2", name: "2" },
        { id: "3", name: "3" },
        { id: "4", name: "4" },
        { id: "5", name: "5" },
      ],
      String(selectedId || 3),
      prefix,
    );
  }

  function appendApplicationTechFit(document, form, selectedId, prefix) {
    appendApplicationSelect(
      document,
      form,
      "Tech Fit",
      "techFit",
      [
        { id: "low", name: "low" },
        { id: "medium", name: "medium" },
        { id: "high", name: "high" },
      ],
      selectedId || "medium",
      prefix,
    );
  }

  function appendApplicationPace(document, form, selectedId, prefix) {
    appendApplicationSelect(
      document,
      form,
      "PACE Classification",
      "pace",
      [
        { id: "System of Record", name: "System of Record" },
        { id: "System of Differentiation", name: "System of Differentiation" },
        { id: "System of Innovation", name: "System of Innovation" },
        { id: "Unclassified", name: "Unclassified" },
      ],
      selectedId || "Unclassified",
      prefix,
    );
  }

  function appendApplicationCriticality(document, form, selectedId, prefix) {
    appendApplicationSelect(
      document,
      form,
      "Criticality",
      "criticality",
      [
        { id: "low", name: "low" },
        { id: "medium", name: "medium" },
        { id: "high", name: "high" },
      ],
      selectedId || "medium",
      prefix,
    );
  }

  function appendApplicationPersonalDataHandling(document, form, selectedId, prefix) {
    appendApplicationSelect(
      document,
      form,
      "Personal Data Handling",
      "personalDataHandling",
      [
        { id: "Yes", name: "Yes" },
        { id: "No", name: "No" },
        { id: "Unknown", name: "Unknown" },
      ],
      selectedId || "Unknown",
      prefix,
    );
  }

  function appendApplicationSensitiveBusinessDataHandling(document, form, selectedId, prefix) {
    appendApplicationSelect(
      document,
      form,
      "Sensitive Business Data Handling",
      "sensitiveBusinessDataHandling",
      [
        { id: "Yes", name: "Yes" },
        { id: "No", name: "No" },
        { id: "Unknown", name: "Unknown" },
      ],
      selectedId || "Unknown",
      prefix,
    );
  }

  function appendApplicationInformationStatus(document, form, selectedId, prefix) {
    appendApplicationSelect(
      document,
      form,
      "Information Status",
      "informationStatus",
      [
        { id: "Draft", name: "Draft" },
        { id: "Verified", name: "Verified" },
        { id: "Needs Review", name: "Needs Review" },
      ],
      selectedId || "Draft",
      prefix,
    );
  }

  function getApplicationFormInput(form, name) {
    return findAllFormFields(form).find((field) => field.name === name);
  }

  function findAllFormFields(node) {
    const fields = ["INPUT", "TEXTAREA", "SELECT"].includes(node.tagName) ? [node] : [];
    for (const child of node.children || []) {
      fields.push(...findAllFormFields(child));
    }
    return fields;
  }

  function readApplicationForm(form) {
    return {
      name: getApplicationFormInput(form, "name").value,
      description: getApplicationFormInput(form, "description").value,
      aliases: parseAliases(getApplicationFormInput(form, "aliases").value),
      applicationUrl: getApplicationFormInput(form, "applicationUrl").value,
      diagnosticUrl: getApplicationFormInput(form, "diagnosticUrl").value,
      businessOwnerName: getApplicationFormInput(form, "businessOwnerName").value,
      businessOwnerEmail: getApplicationFormInput(form, "businessOwnerEmail").value,
      techOwnerName: getApplicationFormInput(form, "techOwnerName").value,
      techOwnerEmail: getApplicationFormInput(form, "techOwnerEmail").value,
      vendorId: getApplicationFormInput(form, "vendorId").value,
      departmentId: getApplicationFormInput(form, "departmentId").value,
      businessAreaId: getApplicationFormInput(form, "businessAreaId").value,
      lifecycleStatus: getApplicationFormInput(form, "lifecycleStatus").value,
      plannedDate: getApplicationFormInput(form, "plannedDate").value,
      retirementDate: getApplicationFormInput(form, "retirementDate").value,
      businessFit: getApplicationFormInput(form, "businessFit").value,
      techFit: getApplicationFormInput(form, "techFit").value,
      pace: getApplicationFormInput(form, "pace").value,
      criticality: getApplicationFormInput(form, "criticality").value,
      personalDataHandling: getApplicationFormInput(form, "personalDataHandling").value,
      sensitiveBusinessDataHandling: getApplicationFormInput(form, "sensitiveBusinessDataHandling").value,
      informationStatus: getApplicationFormInput(form, "informationStatus").value,
      lastVerificationDate: getApplicationFormInput(form, "lastVerificationDate").value,
    };
  }

  function appendFormSection(document, form, title) {
    const section = makeElement(document, "div", { className: "form-section" });
    appendTextBlock(document, section, "h4", "form-section__title", title);
    const grid = makeElement(document, "div", { className: "form-grid" });
    section.appendChild(grid);
    form.appendChild(section);
    return grid;
  }

  function appendApplicationFormFields(document, form, catalog, application, prefix, catalogApi) {
    const general = appendFormSection(document, form, "General Information");
    appendApplicationField(document, general, "Application Name", "name", application.name, { prefix, required: true });
    appendApplicationField(document, general, "Alias", "aliases", formatAliases(application.aliases), { prefix });
    appendApplicationSelect(document, general, "Vendor", "vendorId", catalog.vendors, application.vendorId, prefix);
    appendApplicationSelect(
      document,
      general,
      "Department",
      "departmentId",
      catalog.departments,
      application.departmentId,
      prefix,
    );
    appendApplicationSelect(
      document,
      general,
      "Business Area",
      "businessAreaId",
      catalog.businessAreas,
      application.businessAreaId,
      prefix,
    );
    appendApplicationField(document, general, "Application URL", "applicationUrl", application.applicationUrl, { prefix });
    appendApplicationField(document, general, "Diagnostic URL", "diagnosticUrl", application.diagnosticUrl, { prefix });
    appendApplicationField(document, general, "Description", "description", application.description, {
      prefix,
      required: true,
      multiline: true,
      full: true,
    });

    const lifecycle = appendFormSection(document, form, "Lifecycle & Status");
    appendApplicationLifecycleStatus(document, lifecycle, application.lifecycleStatus, prefix);
    appendApplicationCriticality(document, lifecycle, application.criticality, prefix);
    appendApplicationInformationStatus(document, lifecycle, application.informationStatus, prefix);
    appendApplicationField(document, lifecycle, "Planned Date", "plannedDate", application.plannedDate, {
      prefix,
      type: "date",
    });
    appendApplicationField(document, lifecycle, "Retirement Date", "retirementDate", application.retirementDate, {
      prefix,
      type: "date",
    });
    appendApplicationField(
      document,
      lifecycle,
      "Last Verification Date",
      "lastVerificationDate",
      application.lastVerificationDate,
      { prefix, type: "date" },
    );

    const strategic = appendFormSection(document, form, "Strategic Assessment");
    appendApplicationBusinessFit(document, strategic, application.businessFit, prefix);
    appendApplicationTechFit(document, strategic, application.techFit, prefix);
    appendApplicationPace(document, strategic, application.pace, prefix);
    if (catalogApi && typeof catalogApi.deriveTimeClassification === "function") {
      const businessFitInput = getApplicationFormInput(strategic, "businessFit");
      const techFitInput = getApplicationFormInput(strategic, "techFit");
      const preview = makeElement(document, "div", { className: "time-preview" });
      appendTextBlock(document, preview, "span", "time-preview__label", "TIME Classification");
      const value = makeElement(document, "span", { className: "time-preview__value" });
      preview.appendChild(value);
      const updatePreview = function updateTimePreview() {
        const band = catalogApi.deriveBusinessFitBand(Number(businessFitInput.value));
        const time = catalogApi.deriveTimeClassification(band, techFitInput.value) || "Unclassified";
        value.textContent = time;
        value.className = `time-preview__value badge badge--time badge--time-${String(time).toLowerCase()}`;
      };
      updatePreview();
      businessFitInput.addEventListener("change", updatePreview);
      techFitInput.addEventListener("change", updatePreview);
      strategic.appendChild(preview);
    }

    const stakeholders = appendFormSection(document, form, "Stakeholders");
    appendApplicationField(document, stakeholders, "Business Owner Name", "businessOwnerName", application.businessOwnerName, {
      prefix,
      required: true,
    });
    appendApplicationField(
      document,
      stakeholders,
      "Business Owner Email",
      "businessOwnerEmail",
      application.businessOwnerEmail,
      { prefix, type: "email" },
    );
    appendApplicationField(document, stakeholders, "Tech Owner Name", "techOwnerName", application.techOwnerName, {
      prefix,
      required: true,
    });
    appendApplicationField(document, stakeholders, "Tech Owner Email", "techOwnerEmail", application.techOwnerEmail, {
      prefix,
      type: "email",
    });

    const compliance = appendFormSection(document, form, "Data & Compliance");
    appendApplicationPersonalDataHandling(document, compliance, application.personalDataHandling, prefix);
    appendApplicationSensitiveBusinessDataHandling(document, compliance, application.sensitiveBusinessDataHandling, prefix);
    appendTextBlock(
      document,
      compliance,
      "p",
      "form-note",
      "Set data handling to Yes when the application manages personal or sensitive business data so compliance reviews stay accurate.",
    );
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

  function overviewPercent(count, total) {
    return total > 0 ? Math.round((count / total) * 100) : 0;
  }

  const OVERVIEW_CARD = "bg-white p-lg border border-solid border-outline-variant rounded shadow-card";

  const OVERVIEW_TIME_META = {
    Invest: { textClass: "text-emerald-600", fill: "time-invest" },
    Tolerate: { textClass: "text-amber-600", fill: "time-tolerate" },
    Migrate: { textClass: "text-blue-600", fill: "time-migrate" },
    Eliminate: { textClass: "text-rose-600", fill: "time-eliminate" },
  };

  const OVERVIEW_TIER_META = {
    high: { label: "TIER 1", className: "bg-rose-100 text-rose-800" },
    medium: { label: "TIER 2", className: "bg-amber-100 text-amber-800" },
    low: { label: "TIER 3", className: "bg-sky-100 text-sky-800" },
  };

  function appendProgressBar(document, parent, fillClass, pct, trackClass) {
    const track = makeElement(document, "div", {
      className: `w-full bg-surface-container rounded-full overflow-hidden ${trackClass || "h-1.5"}`,
    });
    track.appendChild(
      makeElement(document, "div", {
        className: `h-full ${fillClass}`,
        attributes: { style: `width: ${pct}%` },
      }),
    );
    parent.appendChild(track);
    return track;
  }

  function appendOverviewTotalCard(document, parent, total, verifiedPct) {
    const card = makeElement(document, "div", {
      className: `${OVERVIEW_CARD} lg:col-span-1 flex flex-col justify-between`,
    });
    appendTextBlock(document, card, "span", "text-label-caps uppercase text-outline", "Total Applications");
    const body = makeElement(document, "div", { className: "mt-md" });
    const value = makeElement(document, "div", { className: "flex items-baseline gap-xs" });
    appendTextBlock(document, value, "span", "text-display-lg text-primary", String(total));
    appendTextBlock(document, value, "span", "sr-only", "Applications");
    body.appendChild(value);
    appendTextBlock(document, body, "span", "text-data-mono text-emerald-600", `${verifiedPct}% verified`);
    card.appendChild(body);
    parent.appendChild(card);
  }

  function appendOverviewTimeCard(document, parent, entries, total) {
    const card = makeElement(document, "div", {
      className: `${OVERVIEW_CARD} lg:col-span-4 grid grid-cols-2 md:grid-cols-4 gap-md`,
    });
    entries.forEach(function buildTimeCell(entry, index) {
      const name = entry[0];
      const count = entry[1];
      const meta = OVERVIEW_TIME_META[name] || { textClass: "text-on-surface-variant", fill: "time-tolerate" };
      const pct = overviewPercent(count, total);
      const cell = makeElement(document, "div", {
        className: "pr-md",
        attributes: index < entries.length - 1 ? { style: "border-right: 1px solid #c5c6ce" } : undefined,
      });
      appendTextBlock(document, cell, "span", `text-label-caps uppercase ${meta.textClass}`, name);
      const value = makeElement(document, "div", { className: "flex items-baseline gap-xs mt-sm" });
      appendTextBlock(document, value, "span", "text-display-lg text-primary", String(count));
      appendTextBlock(document, value, "span", "text-body-sm text-on-surface-variant", `${pct}%`);
      cell.appendChild(value);
      appendProgressBar(document, cell, meta.fill, pct, "h-1.5 mt-md");
      card.appendChild(cell);
    });
    parent.appendChild(card);
  }

  function appendOverviewQuality(document, parent, summary) {
    const card = makeElement(document, "div", { className: `${OVERVIEW_CARD} lg:col-span-1` });
    const head = makeElement(document, "div", { className: "flex justify-between items-start mb-lg" });
    appendTextBlock(document, head, "h3", "text-title-sm text-primary", "Catalog Quality Measure");
    card.appendChild(head);

    const verified = summary.catalogQuality.verified;
    const pct = overviewPercent(verified.count, verified.total);
    const donutWrap = makeElement(document, "div", { className: "flex flex-col items-center justify-center py-md" });
    const donut = makeElement(document, "div", {
      className: "flex items-center justify-center rounded-full",
      attributes: {
        style: `width:150px;height:150px;background:conic-gradient(#041632 ${pct}%, #efedf0 ${pct}% 100%)`,
      },
    });
    const hole = makeElement(document, "div", {
      className: "flex flex-col items-center justify-center rounded-full bg-white",
      attributes: { style: "width:112px;height:112px" },
    });
    appendTextBlock(document, hole, "span", "text-display-lg text-primary", `${pct}%`);
    appendTextBlock(document, hole, "span", "text-label-caps uppercase text-on-surface-variant", "Verified");
    donut.appendChild(hole);
    donutWrap.appendChild(donut);
    card.appendChild(donutWrap);

    const list = makeElement(document, "div", { className: "space-y-sm mt-sm" });
    Object.values(summary.catalogQuality).forEach(function buildQualityRow(measure) {
      const measurePct = overviewPercent(measure.count, measure.total);
      const block = makeElement(document, "div", { className: "space-y-xs" });
      const line = makeElement(document, "div", { className: "flex justify-between items-center text-body-sm" });
      appendTextBlock(document, line, "span", "sr-only", measure.label);
      appendTextBlock(document, line, "span", "text-on-surface-variant", measure.text);
      block.appendChild(line);
      appendProgressBar(document, block, "bg-primary", measurePct, "h-1");
      list.appendChild(block);
    });
    card.appendChild(list);
    parent.appendChild(card);
  }

  function appendOverviewDistribution(document, parent, businessAreaCounts) {
    const card = makeElement(document, "div", { className: `${OVERVIEW_CARD} lg:col-span-2 flex flex-col` });
    const head = makeElement(document, "div", { className: "flex justify-between items-center mb-lg" });
    appendTextBlock(document, head, "h3", "text-title-sm text-primary", "Portfolio Distribution by Business Area");
    card.appendChild(head);

    const entries = Object.entries(businessAreaCounts);
    const max = entries.reduce((acc, entry) => Math.max(acc, entry[1]), 0) || 1;
    const list = makeElement(document, "div", { className: "space-y-lg flex-1" });
    entries.forEach(function buildBar(entry) {
      const name = entry[0];
      const count = entry[1];
      const block = makeElement(document, "div", { className: "space-y-xs" });
      const line = makeElement(document, "div", { className: "flex justify-between text-body-sm" });
      appendTextBlock(document, line, "span", "text-on-surface", name);
      appendTextBlock(document, line, "span", "text-on-surface-variant", `${count} Apps`);
      block.appendChild(line);
      const track = makeElement(document, "div", {
        className: "h-8 w-full bg-surface-container-low rounded overflow-hidden",
      });
      const fill = makeElement(document, "div", {
        className: "h-full bg-primary flex items-center px-md text-white text-label-caps",
        attributes: { style: `width: ${overviewPercent(count, max)}%` },
      });
      appendTextBlock(document, fill, "span", "", String(count));
      track.appendChild(fill);
      block.appendChild(track);
      list.appendChild(block);
    });
    if (entries.length === 0) {
      appendTextBlock(document, list, "p", "text-body-sm text-on-surface-variant", "No business areas registered yet.");
    }
    card.appendChild(list);
    parent.appendChild(card);
  }

  function overviewGapDimension(application) {
    if (application.pace === "Unclassified") {
      return "Unclassified PACE";
    }
    if (application.personalDataHandling === "Unknown" || application.sensitiveBusinessDataHandling === "Unknown") {
      return "Data Handling";
    }
    if (application.informationStatus === "Needs Review") {
      return "Needs Review";
    }
    return "";
  }

  function appendOverviewGaps(document, parent, applications, businessAreas) {
    const areaNames = {};
    for (const area of businessAreas) {
      areaNames[area.id] = area.name;
    }
    const gaps = applications.filter((application) => overviewGapDimension(application) !== "");

    const card = makeElement(document, "div", {
      className: "bg-white border border-solid border-outline-variant rounded overflow-hidden",
    });
    const head = makeElement(document, "div", {
      className: "p-lg bg-surface-container-low",
      attributes: { style: "border-bottom: 1px solid #c5c6ce" },
    });
    appendTextBlock(document, head, "h3", "text-title-sm text-primary", "Strategic Quality Gaps");
    appendTextBlock(
      document,
      head,
      "p",
      "text-body-sm text-on-surface-variant",
      "Applications requiring PACE classification or data-handling review.",
    );
    card.appendChild(head);

    if (gaps.length === 0) {
      appendTextBlock(document, card, "p", "p-lg text-body-sm text-on-surface-variant", "No outstanding quality gaps.");
      parent.appendChild(card);
      return;
    }

    const table = makeElement(document, "table", { className: "w-full text-left" });
    const thead = makeElement(document, "thead", { className: "bg-surface-container-highest" });
    const headRow = makeElement(document, "tr", {});
    for (const label of ["Application Name", "Area", "Criticality", "Missing Dimension", "Owner"]) {
      appendTextBlock(document, headRow, "th", "p-md text-label-caps uppercase text-on-surface-variant", label);
    }
    thead.appendChild(headRow);
    table.appendChild(thead);

    const tbody = makeElement(document, "tbody", {});
    gaps.forEach(function buildGapRow(application, index) {
      const tier = OVERVIEW_TIER_META[application.criticality] || OVERVIEW_TIER_META.medium;
      const row = makeElement(document, "tr", {
        className: index % 2 === 1 ? "bg-surface-container-low" : "",
      });
      appendTextBlock(document, row, "td", "p-md text-body-md text-primary", application.name);
      appendTextBlock(
        document,
        row,
        "td",
        "p-md text-body-sm text-on-surface-variant",
        areaNames[application.businessAreaId] || "\u2014",
      );
      const tierCell = makeElement(document, "td", { className: "p-md" });
      appendTextBlock(document, tierCell, "span", `px-sm py-xs text-[11px] rounded ${tier.className}`, tier.label);
      row.appendChild(tierCell);
      appendTextBlock(document, row, "td", "p-md text-body-sm text-error", overviewGapDimension(application));
      appendTextBlock(
        document,
        row,
        "td",
        "p-md text-body-sm text-on-surface-variant",
        application.businessOwnerName || "\u2014",
      );
      tbody.appendChild(row);
    });
    table.appendChild(tbody);
    card.appendChild(table);
    parent.appendChild(card);
  }

  function appendOverviewFilter(document, form, labelText, name, options, selectedValue) {
    const fieldId = `overview-filter-${name}`;
    const label = makeElement(document, "label", {
      className: "overview-filter__label",
      text: labelText,
      attributes: { for: fieldId },
    });
    const select = makeElement(document, "select", {
      id: fieldId,
      name,
      value: selectedValue || "",
    });
    select.appendChild(makeElement(document, "option", { text: "All", value: "", attributes: { value: "" } }));
    for (const optionConfig of options) {
      const option = makeElement(document, "option", {
        text: optionConfig.name,
        value: optionConfig.id,
        attributes: { value: optionConfig.id },
      });
      if (optionConfig.id === selectedValue) {
        option.selected = true;
      }
      select.appendChild(option);
    }
    form.append(label, select);
    return select;
  }

  function renderOverview(document, catalog, catalogApi, filters, setFilters) {
    const summary = catalogApi.createExecutivePortfolioSummary(catalog, filters);
    const filteredCount = summary.filteredApplications.length;
    const verified = summary.catalogQuality.verified;
    const verifiedPct = overviewPercent(verified.count, verified.total);

    const section = makeElement(document, "section", {
      className: "space-y-lg",
      attributes: { id: "overview" },
    });

    const kpiRow = makeElement(document, "div", {
      className: "grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-md",
    });
    appendOverviewTotalCard(document, kpiRow, summary.totalApplications, verifiedPct);
    appendOverviewTimeCard(document, kpiRow, Object.entries(summary.counts.timeClassification), filteredCount);
    section.appendChild(kpiRow);

    const insightRow = makeElement(document, "div", {
      className: "grid grid-cols-1 lg:grid-cols-3 gap-lg",
    });
    appendOverviewQuality(document, insightRow, summary);
    appendOverviewDistribution(document, insightRow, summary.counts.businessArea);
    section.appendChild(insightRow);

    appendOverviewGaps(document, section, summary.filteredApplications, catalog.businessAreas || []);

    return section;
  }

  function renderApplications(document, catalog, catalogApi, apiClient, storage, rerender, applications, filters, setFilters) {
    const activeFilters = filters || {};
    const applyFilters = typeof setFilters === "function" ? setFilters : function noop() {};
    const baseList = applications || catalog.applications;
    const infoFilter = activeFilters.informationStatus || "";
    const searchTerm = String(activeFilters.search || "").trim().toLowerCase();
    const filteredList = baseList.filter(function matchFilters(application) {
      if (infoFilter && application.informationStatus !== infoFilter) {
        return false;
      }
      if (searchTerm) {
        const haystack = `${application.name} ${formatAliases(application.aliases)}`.toLowerCase();
        if (!haystack.includes(searchTerm)) {
          return false;
        }
      }
      return true;
    });

    const total = catalog.applications.length;
    const needsReview = catalog.applications.filter((item) => item.informationStatus === "Needs Review").length;
    const verified = catalog.applications.filter((item) => item.informationStatus === "Verified").length;
    const investCount = catalog.applications.filter((item) => item.timeClassification === "Invest").length;
    const retiring = catalog.applications.filter(
      (item) => item.lifecycleStatus === "retiring" || item.lifecycleStatus === "retired",
    ).length;
    const verifiedPct = total > 0 ? Math.round((verified / total) * 100) : 0;
    const retiringPct = total > 0 ? Math.round((retiring / total) * 100) : 0;

    const section = makeElement(document, "section", {
      className: "content-section catalog",
      attributes: { id: "applications" },
    });

    const header = makeElement(document, "div", { className: "catalog-header" });
    const headerCopy = makeElement(document, "div", { className: "catalog-header__copy" });
    appendTextBlock(document, headerCopy, "h2", "section-title", "Application Catalog");
    appendTextBlock(
      document,
      headerCopy,
      "p",
      "lede",
      "Monitor and manage the enterprise application portfolio alignment and strategic value.",
    );
    header.appendChild(headerCopy);
    const headerStats = makeElement(document, "div", { className: "catalog-header__stats" });
    const statTotal = makeElement(document, "div", { className: "stat-card" });
    appendTextBlock(document, statTotal, "p", "stat-card__label", "Total Apps");
    appendTextBlock(document, statTotal, "p", "stat-card__value", String(total));
    const statReview = makeElement(document, "div", { className: "stat-card" });
    appendTextBlock(document, statReview, "p", "stat-card__label", "Need Review");
    appendTextBlock(document, statReview, "p", "stat-card__value stat-card__value--alert", String(needsReview));
    headerStats.append(statTotal, statReview);
    header.appendChild(headerStats);
    section.appendChild(header);

    const status = makeElement(document, "p", {
      className: "master-data-status application-status",
      attributes: { role: "status" },
    });
    section.appendChild(status);

    const toolbar = makeElement(document, "div", { className: "catalog-toolbar" });
    const filterGroup = makeElement(document, "div", { className: "catalog-toolbar__filters" });
    appendTextBlock(document, filterGroup, "span", "catalog-toolbar__label", "Quick Filters:");
    const businessAreaSelect = makeElement(document, "select", {
      name: "filterBusinessArea",
      className: "catalog-select",
    });
    businessAreaSelect.appendChild(
      makeElement(document, "option", { text: "All Business Areas", value: "", attributes: { value: "" } }),
    );
    for (const area of catalog.businessAreas) {
      const option = makeElement(document, "option", {
        text: area.name,
        value: area.id,
        attributes: { value: area.id },
      });
      if (area.id === activeFilters.businessAreaId) {
        option.selected = true;
      }
      businessAreaSelect.appendChild(option);
    }
    const infoSelect = makeElement(document, "select", {
      name: "filterInformationStatus",
      className: "catalog-select",
    });
    infoSelect.appendChild(
      makeElement(document, "option", { text: "All Information Status", value: "", attributes: { value: "" } }),
    );
    for (const statusName of ["Draft", "Verified", "Needs Review"]) {
      const option = makeElement(document, "option", {
        text: statusName,
        value: statusName,
        attributes: { value: statusName },
      });
      if (statusName === activeFilters.informationStatus) {
        option.selected = true;
      }
      infoSelect.appendChild(option);
    }
    const searchInput = makeElement(document, "input", {
      name: "filterSearch",
      type: "search",
      value: activeFilters.search || "",
      className: "catalog-search",
      attributes: { placeholder: "Search applications...", "aria-label": "Search applications" },
    });
    filterGroup.append(businessAreaSelect, infoSelect, searchInput);
    const applyButton = makeElement(document, "button", {
      className: "catalog-apply",
      type: "button",
      text: "Apply Filters",
    });
    applyButton.addEventListener("click", function onApply() {
      applyFilters({
        ...activeFilters,
        businessAreaId: businessAreaSelect.value || "",
        informationStatus: infoSelect.value || "",
        search: searchInput.value || "",
      });
    });
    const clearButton = makeElement(document, "button", {
      className: "catalog-clear",
      type: "button",
      text: "Clear",
    });
    clearButton.addEventListener("click", function onClear() {
      applyFilters({});
    });
    filterGroup.append(applyButton, clearButton);
    toolbar.appendChild(filterGroup);
    const toolbarActions = makeElement(document, "div", { className: "catalog-toolbar__actions" });
    const addButton = makeElement(document, "button", {
      className: "catalog-add",
      type: "button",
      text: "Add Application",
    });
    const exportButton = makeElement(document, "button", {
      className: "catalog-export",
      type: "button",
      text: "Export CSV",
    });
    toolbarActions.append(addButton, exportButton);
    toolbar.appendChild(toolbarActions);
    section.appendChild(toolbar);

    appendTextBlock(
      document,
      section,
      "p",
      "application-count",
      `Showing ${filteredList.length} of ${total} Applications`,
    );

    const createModal = makeElement(document, "div", { className: "modal" });
    createModal.hidden = true;
    const createModalCard = makeElement(document, "div", { className: "modal__card modal__card--app" });
    const createModalHead = makeElement(document, "div", { className: "modal__head" });
    appendTextBlock(document, createModalHead, "h3", "modal__title", "Add Application");
    const createModalClose = makeElement(document, "button", {
      className: "modal__close",
      type: "button",
      text: "\u00d7",
      attributes: { "aria-label": "Close" },
    });
    createModalClose.addEventListener("click", function onCloseCreate() {
      createModal.hidden = true;
      createModal.className = "modal";
    });
    createModalHead.appendChild(createModalClose);
    const createForm = makeElement(document, "form", {
      className: "edit-form application-form application-form--create",
    });
    appendApplicationFormFields(
      document,
      createForm,
      catalog,
      {
        name: "",
        description: "",
        aliases: [],
        applicationUrl: "",
        diagnosticUrl: "",
        businessOwnerName: "",
        businessOwnerEmail: "",
        techOwnerName: "",
        techOwnerEmail: "",
        vendorId: catalog.vendors[0] ? catalog.vendors[0].id : "",
        departmentId: catalog.departments[0] ? catalog.departments[0].id : "",
        businessAreaId: catalog.businessAreas[0] ? catalog.businessAreas[0].id : "",
        lifecycleStatus: "active",
        plannedDate: "",
        retirementDate: "",
        businessFit: 3,
        techFit: "medium",
        pace: "Unclassified",
        criticality: "medium",
        personalDataHandling: "Unknown",
        sensitiveBusinessDataHandling: "Unknown",
        informationStatus: "Draft",
        lastVerificationDate: "",
      },
      "application-create",
      catalogApi,
    );
    const createButton = makeElement(document, "button", { type: "submit", text: "Add Application" });
    createForm.appendChild(createButton);
    createForm.addEventListener("submit", function onCreate(event) {
      if (event && typeof event.preventDefault === "function") {
        event.preventDefault();
      }
      return apiClient
        .createApplication(readApplicationForm(createForm))
        .then(function onCreated(created) {
          catalog.applications.push(created);
          rerender(catalog);
        })
        .catch(function onError(error) {
          status.textContent = error.message;
        });
    });
    createModalCard.append(createModalHead, createForm);
    createModal.appendChild(createModalCard);
    addButton.addEventListener("click", function onOpenCreate() {
      createModal.hidden = false;
      createModal.className = "modal modal--open";
    });
    section.appendChild(createModal);

    const tableWrap = makeElement(document, "div", { className: "catalog-table" });
    const tableHead = makeElement(document, "div", { className: "catalog-row catalog-row--head" });
    for (const label of ["Application", "Vendor", "TIME", "PACE", "Lifecycle", "Criticality", "Status", ""]) {
      appendTextBlock(document, tableHead, "span", "catalog-cell catalog-cell--head", label);
    }
    tableWrap.appendChild(tableHead);
    const listBody = makeElement(document, "div", { className: "catalog-body" });
    tableWrap.appendChild(listBody);
    const pager = makeElement(document, "div", { className: "catalog-pager" });
    tableWrap.appendChild(pager);
    section.appendChild(tableWrap);

    function buildRow(application) {
      const vendor = findById(catalog.vendors, application.vendorId);
      const card = makeElement(document, "article", { className: "application-card" });

      const nameCell = makeElement(document, "div", { className: "catalog-cell catalog-cell--name" });
      appendTextBlock(document, nameCell, "span", "catalog-cell__name", application.name);
      appendTextBlock(document, nameCell, "span", "catalog-cell__alias", formatAliases(application.aliases) || "No alias");
      const vendorCell = makeElement(document, "div", { className: "catalog-cell" });
      appendTextBlock(document, vendorCell, "span", "catalog-cell__text", vendor ? vendor.name : "Unknown");
      const timeCell = makeElement(document, "div", { className: "catalog-cell" });
      timeCell.appendChild(makeBadge(document, "time", application.timeClassification));
      const paceCell = makeElement(document, "div", { className: "catalog-cell" });
      appendTextBlock(document, paceCell, "span", "catalog-cell__pace", application.pace);
      const lifecycleCell = makeElement(document, "div", { className: "catalog-cell" });
      lifecycleCell.appendChild(makeBadge(document, "lifecycle", application.lifecycleStatus));
      const criticalityCell = makeElement(document, "div", { className: "catalog-cell" });
      const critWrap = makeElement(document, "span", { className: "crit" });
      const critSlug = String(application.criticality)
        .toLowerCase()
        .replace(/[^a-z0-9]+/g, "-");
      critWrap.appendChild(makeElement(document, "span", { className: `crit__dot crit__dot--${critSlug}` }));
      appendTextBlock(document, critWrap, "span", "crit__label", application.criticality);
      criticalityCell.appendChild(critWrap);
      const statusCell = makeElement(document, "div", { className: "catalog-cell" });
      statusCell.appendChild(makeBadge(document, "info", application.informationStatus));

      const actionsCell = makeElement(document, "div", { className: "catalog-cell catalog-cell--actions" });
      const detailsModal = makeElement(document, "div", { className: "modal" });
      detailsModal.hidden = true;
      const detailsCard = makeElement(document, "div", { className: "modal__card modal__card--app" });
      const detailsHead = makeElement(document, "div", { className: "modal__head" });
      appendTextBlock(document, detailsHead, "h3", "modal__title", application.name);
      const detailsClose = makeElement(document, "button", {
        className: "modal__close",
        type: "button",
        text: "\u00d7",
        attributes: { "aria-label": "Close" },
      });
      detailsClose.addEventListener("click", function onCloseDetails() {
        detailsModal.hidden = true;
        detailsModal.className = "modal";
      });
      detailsHead.appendChild(detailsClose);
      detailsCard.appendChild(detailsHead);

      const form = makeElement(document, "form", { className: "edit-form application-form" });
      appendApplicationFormFields(document, form, catalog, application, `application-${application.id}`, catalogApi);
      const saveButton = makeElement(document, "button", { type: "submit", text: "Save" });
      form.appendChild(saveButton);
      form.addEventListener("submit", function onSubmit(event) {
        if (event && typeof event.preventDefault === "function") {
          event.preventDefault();
        }
        return apiClient
          .updateApplication(application.id, readApplicationForm(form))
          .then(function onUpdated(updated) {
            Object.assign(application, updated);
            rerender(catalog);
          })
          .catch(function onError(error) {
            status.textContent = error.message;
          });
      });
      const deleteButton = makeElement(document, "button", {
        className: "master-data-delete application-delete",
        type: "button",
        text: "Delete",
      });
      deleteButton.addEventListener("click", function onDelete() {
        return apiClient
          .deleteApplication(application.id)
          .then(function onDeleted() {
            const index = catalog.applications.indexOf(application);
            if (index !== -1) {
              catalog.applications.splice(index, 1);
            }
            rerender(catalog);
          })
          .catch(function onError(error) {
            status.textContent = error.message;
          });
      });
      detailsCard.append(form, deleteButton);
      detailsModal.appendChild(detailsCard);
      detailsModal.addEventListener("click", function onBackdrop(event) {
        if (event && event.target === detailsModal) {
          detailsModal.hidden = true;
          detailsModal.className = "modal";
        }
      });

      const editButton = makeElement(document, "button", {
        className: "catalog-row__edit",
        type: "button",
        text: "Edit",
        attributes: { "aria-label": `Edit ${application.name}` },
      });
      editButton.addEventListener("click", function onOpenDetails() {
        detailsModal.hidden = false;
        detailsModal.className = "modal modal--open";
      });
      actionsCell.append(editButton, detailsModal);

      card.append(nameCell, vendorCell, timeCell, paceCell, lifecycleCell, criticalityCell, statusCell, actionsCell);
      return card;
    }

    let pageSize = 10;
    let currentPage = 1;

    function renderRows() {
      const pageCount = Math.max(1, Math.ceil(filteredList.length / pageSize));
      if (currentPage > pageCount) {
        currentPage = pageCount;
      }
      const start = (currentPage - 1) * pageSize;
      const pageItems = filteredList.slice(start, start + pageSize);
      if (pageItems.length === 0) {
        const empty = makeElement(document, "p", { className: "catalog-empty", text: "No applications match the current filters." });
        listBody.replaceChildren(empty);
      } else {
        listBody.replaceChildren(...pageItems.map(buildRow));
      }

      const info = makeElement(document, "span", {
        className: "catalog-pager__info",
        text:
          filteredList.length === 0
            ? "No applications"
            : `Showing ${start + 1}\u2013${Math.min(start + pageSize, filteredList.length)} of ${filteredList.length}`,
      });
      const controls = makeElement(document, "div", { className: "catalog-pager__controls" });
      const sizeSelect = makeElement(document, "select", { className: "catalog-pager__size", attributes: { "aria-label": "Rows per page" } });
      for (const size of [10, 20, 50, 100]) {
        const option = makeElement(document, "option", {
          text: `${size} / page`,
          value: String(size),
          attributes: { value: String(size) },
        });
        if (size === pageSize) {
          option.selected = true;
        }
        sizeSelect.appendChild(option);
      }
      sizeSelect.addEventListener("change", function onSize() {
        pageSize = Number(sizeSelect.value) || 10;
        currentPage = 1;
        renderRows();
      });
      const prev = makeElement(document, "button", { className: "catalog-pager__btn", type: "button", text: "Prev" });
      prev.addEventListener("click", function onPrev() {
        if (currentPage > 1) {
          currentPage -= 1;
          renderRows();
        }
      });
      const pageLabel = makeElement(document, "span", { className: "catalog-pager__page", text: `Page ${currentPage} of ${pageCount}` });
      const next = makeElement(document, "button", { className: "catalog-pager__btn", type: "button", text: "Next" });
      next.addEventListener("click", function onNext() {
        if (currentPage < pageCount) {
          currentPage += 1;
          renderRows();
        }
      });
      controls.append(sizeSelect, prev, pageLabel, next);
      pager.replaceChildren(info, controls);
    }

    exportButton.addEventListener("click", function onExport() {
      try {
        const headerRow = [
          "Name",
          "Aliases",
          "Vendor",
          "Business Area",
          "Lifecycle",
          "TIME",
          "PACE",
          "Criticality",
          "Information Status",
        ];
        const rows = [headerRow];
        for (const application of filteredList) {
          const vendor = findById(catalog.vendors, application.vendorId);
          const area = findById(catalog.businessAreas, application.businessAreaId);
          rows.push([
            application.name,
            formatAliases(application.aliases),
            vendor ? vendor.name : "",
            area ? area.name : "",
            application.lifecycleStatus,
            application.timeClassification,
            application.pace,
            application.criticality,
            application.informationStatus,
          ]);
        }
        const csv = rows
          .map((row) => row.map((cell) => `"${String(cell).replace(/"/g, '""')}"`).join(","))
          .join("\r\n");
        const view = document.defaultView;
        if (!view || typeof view.Blob === "undefined" || !view.URL) {
          return;
        }
        const blob = new view.Blob([csv], { type: "text/csv;charset=utf-8;" });
        const url = view.URL.createObjectURL(blob);
        const link = document.createElement("a");
        link.setAttribute("href", url);
        link.setAttribute("download", "application-catalog.csv");
        link.click();
        view.URL.revokeObjectURL(url);
      } catch (error) {
        status.textContent = "Export failed.";
      }
    });

    renderRows();

    const kpis = makeElement(document, "div", { className: "catalog-kpis" });
    const focusCard = makeElement(document, "div", { className: "kpi-card kpi-card--focus" });
    appendTextBlock(document, focusCard, "p", "kpi-card__label", "Strategic Focus");
    appendTextBlock(document, focusCard, "p", "kpi-card__headline", "Invest & Modernize");
    appendTextBlock(document, focusCard, "p", "kpi-card__metric", `${investCount} of ${total}`);
    appendTextBlock(document, focusCard, "p", "kpi-card__note", "Applications flagged to invest and modernize.");
    const riskCard = makeElement(document, "div", { className: "kpi-card kpi-card--risk" });
    appendTextBlock(document, riskCard, "p", "kpi-card__label", "Lifecycle Risk");
    appendTextBlock(document, riskCard, "p", "kpi-card__headline", "Retiring Portfolio");
    const riskBar = makeElement(document, "div", { className: "kpi-bar" });
    const riskFill = makeElement(document, "div", { className: "kpi-bar__fill" });
    riskFill.setAttribute("style", `width: ${retiringPct}%`);
    riskBar.appendChild(riskFill);
    riskCard.appendChild(riskBar);
    appendTextBlock(document, riskCard, "p", "kpi-card__metric", `${retiring} of ${total}`);
    appendTextBlock(document, riskCard, "p", "kpi-card__note", "Applications retiring or already retired.");
    const qualityCard = makeElement(document, "div", { className: "kpi-card kpi-card--quality" });
    appendTextBlock(document, qualityCard, "p", "kpi-card__label", "Data Quality");
    appendTextBlock(document, qualityCard, "p", "kpi-card__headline", `${verifiedPct}% Verified`);
    appendTextBlock(document, qualityCard, "p", "kpi-card__metric", `${verified} of ${total}`);
    appendTextBlock(document, qualityCard, "p", "kpi-card__note", "Records verified in the catalog.");
    kpis.append(focusCard, riskCard, qualityCard);
    section.appendChild(kpis);

    return section;
  }

  function renderMasterData(document, catalog, catalogApi, apiClient, storage, rerender, config) {
    const section = makeElement(document, "section", {
      className: "content-section content-section--compact",
      attributes: { id: config.id },
    });

    const head = makeElement(document, "div", { className: "master-head" });
    const searchInput = makeElement(document, "input", {
      className: "md-search",
      type: "search",
      attributes: { placeholder: `Search ${config.title}...`, "aria-label": `Search ${config.title}` },
    });
    const addButton = makeElement(document, "button", {
      className: "catalog-add",
      type: "button",
      text: `Add ${config.singular}`,
    });
    head.append(searchInput, addButton);
    section.appendChild(head);

    const status = makeElement(document, "p", {
      className: "master-data-status",
      attributes: { role: "status" },
    });
    section.appendChild(status);

    function countUsage(record) {
      if (!config.usageField) {
        return 0;
      }
      return catalog.applications.filter((application) => application[config.usageField] === record.id).length;
    }

    function buildGeneralCard() {
      const card = makeElement(document, "div", { className: "form-section" });
      appendTextBlock(document, card, "h4", "form-section__title", "General Information");
      const grid = makeElement(document, "div", { className: "form-grid" });
      card.appendChild(grid);
      return { card, grid };
    }

    function appendNameField(grid, fieldId, value) {
      const field = makeElement(document, "div", { className: "form-field form-field--full" });
      const label = makeElement(document, "label", {
        className: "form-field__label",
        text: `${config.singular} Name`,
        attributes: fieldId ? { for: fieldId } : undefined,
      });
      const input = makeElement(document, "input", {
        id: fieldId,
        name: "name",
        type: "text",
        value: value || "",
        attributes: { autocomplete: "off", "aria-label": `${config.singular} name` },
      });
      field.append(label, input);
      grid.appendChild(field);
      return input;
    }

    function appendInternalToggle(grid, checked) {
      const field = makeElement(document, "div", { className: "form-field form-field--full" });
      const label = makeElement(document, "label", { className: "master-data-check" });
      const input = makeElement(document, "input", { name: "isInternal", type: "checkbox", checked });
      label.append(input, document.createTextNode("Internal Vendor"));
      field.appendChild(label);
      grid.appendChild(field);
      return input;
    }

    function buildUsageCard(usageCount) {
      const card = makeElement(document, "div", { className: "usage-card" });
      appendTextBlock(document, card, "h4", "form-section__title", "Usage & Impact");
      const box = makeElement(document, "div", { className: "usage-card__box" });
      appendTextBlock(document, box, "p", "usage-card__label", "Applications Linked");
      appendTextBlock(document, box, "p", "usage-card__value", String(usageCount));
      card.appendChild(box);
      appendTextBlock(
        document,
        card,
        "p",
        "usage-card__note",
        usageCount > 0
          ? `${config.singular} is referenced by ${usageCount} application${usageCount === 1 ? "" : "s"} and cannot be deleted while in use.`
          : `${config.singular} is not linked to any application and can be safely deleted.`,
      );
      return card;
    }

    const createModal = makeElement(document, "div", { className: "modal" });
    createModal.hidden = true;
    const createModalCard = makeElement(document, "div", { className: "modal__card modal__card--md" });
    const createModalHead = makeElement(document, "div", { className: "modal__head" });
    appendTextBlock(document, createModalHead, "h3", "modal__title", `Add ${config.singular}`);
    const createModalClose = makeElement(document, "button", {
      className: "modal__close",
      type: "button",
      text: "\u00d7",
      attributes: { "aria-label": "Close" },
    });
    createModalClose.addEventListener("click", function onCloseCreate() {
      createModal.hidden = true;
      createModal.className = "modal";
    });
    createModalHead.appendChild(createModalClose);

    const createForm = makeElement(document, "form", { className: "md-form" });
    const createGeneral = buildGeneralCard();
    const createName = appendNameField(createGeneral.grid, `${config.id}-create-name`, "");
    let createInternal = null;
    if (config.kind === "vendor") {
      createInternal = appendInternalToggle(createGeneral.grid, false);
    }
    createForm.appendChild(createGeneral.card);
    const createButton = makeElement(document, "button", {
      className: "md-form__save",
      type: "submit",
      text: `Add ${config.singular}`,
    });
    createForm.appendChild(createButton);
    createForm.addEventListener("submit", function onSubmit(event) {
      if (event && typeof event.preventDefault === "function") {
        event.preventDefault();
      }
      return config
        .create({
          name: createName.value,
          isInternal: createInternal ? createInternal.checked === true : undefined,
        })
        .then(function onCreated(created) {
          config.records(catalog).push(created);
          rerender(catalog);
        })
        .catch(function onError(error) {
          status.textContent = error.message;
        });
    });
    createModalCard.append(createModalHead, createForm);
    createModal.appendChild(createModalCard);
    createModal.addEventListener("click", function onCreateBackdrop(event) {
      if (event && event.target === createModal) {
        createModal.hidden = true;
        createModal.className = "modal";
      }
    });
    addButton.addEventListener("click", function onOpenCreate() {
      createModal.hidden = false;
      createModal.className = "modal modal--open";
    });
    section.appendChild(createModal);

    const records = config.records(catalog);
    const usageValues = records.map((record) => countUsage(record));
    const maxUsage = Math.max(1, ...usageValues);
    const total = records.length;
    const referenced = usageValues.filter((count) => count > 0).length;
    const unreferenced = total - referenced;
    const totalLinks = usageValues.reduce((sum, count) => sum + count, 0);
    const avgApps = total > 0 ? (totalLinks / total).toFixed(1) : "0.0";

    const stats = makeElement(document, "div", { className: "md-stats" });
    function appendStat(label, value) {
      const card = makeElement(document, "div", { className: "md-stat" });
      appendTextBlock(document, card, "p", "md-stat__label", label);
      appendTextBlock(document, card, "p", "md-stat__value", String(value));
      stats.appendChild(card);
    }
    appendStat(`Total ${config.title}`, total);
    appendStat("In Use", referenced);
    appendStat(`Avg Apps / ${config.singular}`, avgApps);
    appendStat("Free To Delete", unreferenced);
    section.appendChild(stats);

    const tableWrap = makeElement(document, "div", {
      className: config.kind === "vendor" ? "md-table md-table--vendor" : "md-table",
    });
    const headRow = makeElement(document, "div", { className: "md-row md-row--head" });
    const columns =
      config.kind === "vendor"
        ? [`${config.singular} Name`, "Type", "Apps Linked", ""]
        : [`${config.singular} Name`, "Apps Linked", ""];
    for (const label of columns) {
      appendTextBlock(document, headRow, "span", "md-cell md-cell--head", label);
    }
    tableWrap.appendChild(headRow);
    const listBody = makeElement(document, "ul", { className: "master-list master-list--editable md-body" });
    tableWrap.appendChild(listBody);
    const pager = makeElement(document, "div", { className: "md-pager" });
    tableWrap.appendChild(pager);
    section.appendChild(tableWrap);

    function buildRow(record) {
      const usageCount = countUsage(record);
      const item = makeElement(document, "li", { className: "master-list__item md-row" });

      const nameCell = makeElement(document, "div", { className: "md-cell md-cell--name" });
      appendTextBlock(document, nameCell, "span", "master-list__name", record.name);
      item.appendChild(nameCell);

      if (config.kind === "vendor") {
        const typeCell = makeElement(document, "div", { className: "md-cell" });
        typeCell.appendChild(
          makeElement(document, "span", {
            className: record.isInternal ? "md-type md-type--internal" : "md-type md-type--external",
            text: config.describe ? config.describe(record) : "",
          }),
        );
        item.appendChild(typeCell);
      }

      const usageCell = makeElement(document, "div", { className: "md-cell md-cell--usage" });
      appendTextBlock(
        document,
        usageCell,
        "span",
        "md-usage__count",
        `${usageCount} application${usageCount === 1 ? "" : "s"}`,
      );
      const bar = makeElement(document, "div", { className: "md-usage__bar" });
      const fill = makeElement(document, "div", { className: "md-usage__fill" });
      fill.setAttribute("style", `width: ${Math.round((usageCount / maxUsage) * 100)}%`);
      bar.appendChild(fill);
      usageCell.appendChild(bar);
      item.appendChild(usageCell);

      const editForm = makeElement(document, "form", { className: "md-form" });
      const editGeneral = buildGeneralCard();
      const editName = appendNameField(editGeneral.grid, "", record.name);
      let editInternal = null;
      if (config.kind === "vendor") {
        editInternal = appendInternalToggle(editGeneral.grid, record.isInternal === true);
      }
      editForm.appendChild(editGeneral.card);
      const saveButton = makeElement(document, "button", {
        className: "md-form__save",
        type: "submit",
        text: "Save Changes",
      });
      editForm.appendChild(saveButton);
      editForm.addEventListener("submit", function onEdit(event) {
        if (event && typeof event.preventDefault === "function") {
          event.preventDefault();
        }
        return config
          .update(record.id, {
            name: editName.value,
            isInternal: editInternal ? editInternal.checked === true : undefined,
          })
          .then(function onUpdated(updated) {
            const existing = config.records(catalog).find((candidate) => candidate.id === record.id);
            if (existing) {
              Object.assign(existing, updated);
            }
            rerender(catalog);
          })
          .catch(function onError(error) {
            status.textContent = error.message;
          });
      });

      const deleteButton = makeElement(document, "button", {
        className: "md-icon-btn md-icon-btn--danger",
        type: "button",
        text: "Delete",
        attributes: {
          "aria-label": `Delete ${config.singular}`,
          title: usageCount > 0 ? "Referenced by applications" : "Delete",
        },
      });
      deleteButton.addEventListener("click", function onDelete() {
        return config
          .delete(record.id)
          .then(function onDeleted() {
            const records = config.records(catalog);
            const index = records.findIndex((candidate) => candidate.id === record.id);
            if (index !== -1) {
              records.splice(index, 1);
            }
            rerender(catalog);
          })
          .catch(function onError(error) {
            status.textContent = error.message;
          });
      });

      const editButton = makeElement(document, "button", {
        className: "md-icon-btn md-icon-btn--edit",
        type: "button",
        text: "Edit",
        attributes: { "aria-label": `Edit ${config.singular}`, title: "Edit" },
      });

      const modal = makeElement(document, "div", { className: "modal" });
      modal.hidden = true;
      const modalCard = makeElement(document, "div", { className: "modal__card modal__card--md" });
      const modalHead = makeElement(document, "div", { className: "modal__head" });
      appendTextBlock(document, modalHead, "h3", "modal__title", `Edit ${config.singular}: ${record.name}`);
      const modalClose = makeElement(document, "button", {
        className: "modal__close",
        type: "button",
        text: "\u00d7",
        attributes: { "aria-label": "Close" },
      });
      function closeModal() {
        modal.hidden = true;
        modal.className = "modal";
      }
      function openModal() {
        modal.hidden = false;
        modal.className = "modal modal--open";
      }
      modalClose.addEventListener("click", closeModal);
      modalHead.appendChild(modalClose);
      const modalBody = makeElement(document, "div", { className: "md-modal__body" });
      const modalMain = makeElement(document, "div", { className: "md-modal__main" });
      modalMain.appendChild(editForm);
      const modalSide = makeElement(document, "div", { className: "md-modal__side" });
      modalSide.appendChild(buildUsageCard(usageCount));
      modalBody.append(modalMain, modalSide);
      modalCard.append(modalHead, modalBody);
      modal.appendChild(modalCard);
      modal.addEventListener("click", function onBackdrop(event) {
        if (event && event.target === modal) {
          closeModal();
        }
      });
      editButton.addEventListener("click", openModal);

      const actionsCell = makeElement(document, "div", { className: "md-cell md-cell--actions" });
      actionsCell.append(editButton, deleteButton, modal);
      item.appendChild(actionsCell);
      return item;
    }

    let pageSize = 10;
    let currentPage = 1;
    let searchTerm = "";

    function renderRows() {
      const term = searchTerm.trim().toLowerCase();
      const filtered = term
        ? records.filter((record) => record.name.toLowerCase().includes(term))
        : records;
      const pageCount = Math.max(1, Math.ceil(filtered.length / pageSize));
      if (currentPage > pageCount) {
        currentPage = pageCount;
      }
      const start = (currentPage - 1) * pageSize;
      const pageItems = filtered.slice(start, start + pageSize);
      if (pageItems.length === 0) {
        listBody.replaceChildren(
          makeElement(document, "li", {
            className: "md-empty",
            text: `No ${config.title.toLowerCase()} match your search.`,
          }),
        );
      } else {
        listBody.replaceChildren(...pageItems.map(buildRow));
      }

      const info = makeElement(document, "span", {
        className: "md-pager__info",
        text:
          filtered.length === 0
            ? "No records"
            : `Showing ${start + 1}\u2013${Math.min(start + pageSize, filtered.length)} of ${filtered.length} records`,
      });
      const controls = makeElement(document, "div", { className: "md-pager__controls" });
      const prev = makeElement(document, "button", { className: "catalog-pager__btn", type: "button", text: "Prev" });
      prev.addEventListener("click", function onPrev() {
        if (currentPage > 1) {
          currentPage -= 1;
          renderRows();
        }
      });
      const pageLabel = makeElement(document, "span", { className: "catalog-pager__page", text: `Page ${currentPage} of ${pageCount}` });
      const next = makeElement(document, "button", { className: "catalog-pager__btn", type: "button", text: "Next" });
      next.addEventListener("click", function onNext() {
        if (currentPage < pageCount) {
          currentPage += 1;
          renderRows();
        }
      });
      controls.append(prev, pageLabel, next);
      pager.replaceChildren(info, controls);
    }

    searchInput.addEventListener("input", function onSearch() {
      searchTerm = searchInput.value || "";
      currentPage = 1;
      renderRows();
    });

    renderRows();

    const banner = makeElement(document, "div", { className: "md-banner" });
    appendTextBlock(document, banner, "span", "md-banner__icon", "\u24D8");
    const bannerBody = makeElement(document, "div", { className: "md-banner__body" });
    appendTextBlock(document, bannerBody, "h4", "md-banner__title", "Referenced Master Data Rule");
    appendTextBlock(
      document,
      bannerBody,
      "p",
      "md-banner__text",
      "Master Data records linked to one or more applications cannot be deleted, to preserve referential integrity. To remove a record, first reassign its dependencies in the Application Catalog.",
    );
    banner.appendChild(bannerBody);
    section.appendChild(banner);

    return section;
  }

  function renderAnalyticsMatrix(document, catalog) {
    const section = makeElement(document, "section", {
      className: "analytics",
      attributes: { id: "analytics" },
    });
    appendTextBlock(document, section, "h2", "section-title", "Analytics \u2014 TIME Matrix");
    appendTextBlock(
      document,
      section,
      "p",
      "lede",
      "Applications plotted by Business Fit and Technical Fit into the four TIME quadrants.",
    );
    const grid = makeElement(document, "div", { className: "time-matrix" });
    const quadrants = [
      { name: "Tolerate", slug: "tolerate" },
      { name: "Invest", slug: "invest" },
      { name: "Eliminate", slug: "eliminate" },
      { name: "Migrate", slug: "migrate" },
    ];
    for (const quadrant of quadrants) {
      const cell = makeElement(document, "div", { className: `quadrant quadrant--${quadrant.slug}` });
      const head = makeElement(document, "div", { className: "quadrant__head" });
      appendTextBlock(document, head, "span", "quadrant__name", quadrant.name);
      const matches = catalog.applications.filter(
        (application) => application.timeClassification === quadrant.name,
      );
      appendTextBlock(document, head, "span", "quadrant__count", String(matches.length));
      cell.appendChild(head);
      const chips = makeElement(document, "div", { className: "quadrant__chips" });
      for (const application of matches) {
        appendTextBlock(document, chips, "span", "chip", application.name);
      }
      cell.appendChild(chips);
      grid.appendChild(cell);
    }
    section.appendChild(grid);
    return section;
  }

  const ROLE_OPTIONS = ["VIEWER", "EDITOR", "ADMIN"];

  function formatRoleLabel(role) {
    const value = String(role || "").toUpperCase();
    return value ? value.charAt(0) + value.slice(1).toLowerCase() : "";
  }

  function formatLoginMethodLabel(loginMethod) {
    return String(loginMethod || "").toUpperCase() === "LOCAL" ? "Local Login" : "SSO";
  }

  function formatAccessScopeSummary(user) {
    const departmentCount = (user.scopedDepartmentIds || []).length;
    const businessAreaCount = (user.scopedBusinessAreaIds || []).length;
    if (departmentCount === 0 && businessAreaCount === 0) {
      return "No scope assigned";
    }
    const parts = [];
    if (departmentCount > 0) {
      parts.push(`${departmentCount} Department${departmentCount === 1 ? "" : "s"}`);
    }
    if (businessAreaCount > 0) {
      parts.push(`${businessAreaCount} Business Area${businessAreaCount === 1 ? "" : "s"}`);
    }
    return parts.join(" \u00b7 ");
  }

  function renderUsersAdmin(document, apiClient, currentUser, users, departments, businessAreas, applications, vendors) {
    const section = makeElement(document, "section", {
      className: "content-section content-section--compact",
      attributes: { id: "users" },
    });

    const header = makeElement(document, "div", { className: "view__header" });
    appendTextBlock(document, header, "h2", "section-title", "User Management");
    appendTextBlock(
      document,
      header,
      "p",
      "lede",
      "Manage who can access the portfolio, at what level, and which organizational units they can see. Only Admins can view this screen.",
    );
    section.appendChild(header);

    const status = makeElement(document, "p", {
      className: "master-data-status",
      attributes: { role: "status", "aria-live": "polite" },
    });

    const table = makeElement(document, "table", { className: "users-table" });
    const thead = makeElement(document, "thead", {});
    const headRow = makeElement(document, "tr", {});
    for (const heading of ["Name / Email", "Login Method", "Role", "Access Scope", "Edit Permissions"]) {
      appendTextBlock(document, headRow, "th", "", heading);
    }
    thead.appendChild(headRow);
    table.appendChild(thead);

    const tbody = makeElement(document, "tbody", {});
    for (const user of users) {
      const row = makeElement(document, "tr", { className: "users-table__row" });

      const identityCell = makeElement(document, "td", {});
      appendTextBlock(document, identityCell, "p", "users-table__name", user.name || "");
      appendTextBlock(document, identityCell, "p", "users-table__email", user.email || "");
      row.appendChild(identityCell);

      const methodCell = makeElement(document, "td", {});
      appendTextBlock(document, methodCell, "span", "badge", formatLoginMethodLabel(user.loginMethod));
      row.appendChild(methodCell);

      const roleCell = makeElement(document, "td", {});
      const isSelf = currentUser && currentUser.email
        && String(currentUser.email).toLowerCase() === String(user.email || "").toLowerCase();
      if (isSelf) {
        appendTextBlock(document, roleCell, "span", "badge users-table__role-self", formatRoleLabel(user.role));
        appendTextBlock(document, roleCell, "span", "users-table__hint", "You");
      } else {
        const select = makeElement(document, "select", {
          className: "users-table__role-select",
          value: user.role,
          attributes: { "aria-label": `Role for ${user.name || user.email}` },
        });
        for (const roleOption of ROLE_OPTIONS) {
          const option = makeElement(document, "option", {
            text: formatRoleLabel(roleOption),
            value: roleOption,
            attributes: { value: roleOption },
          });
          if (roleOption === String(user.role || "").toUpperCase()) {
            option.selected = true;
          }
          select.appendChild(option);
        }
        select.addEventListener("change", function onRoleChange() {
          const nextRole = select.value;
          const previousRole = user.role;
          select.disabled = true;
          return apiClient
            .updateCatalogUserRole(user.id, nextRole)
            .then(function onUpdated(updated) {
              user.role = updated && updated.role ? updated.role : nextRole;
              status.textContent = `${user.name || user.email} is now ${formatRoleLabel(user.role)}.`;
              select.disabled = false;
            })
            .catch(function onError(error) {
              status.textContent = error.message;
              select.value = previousRole;
              select.disabled = false;
            });
        });
        roleCell.appendChild(select);
      }
      row.appendChild(roleCell);

      const scopeCell = makeElement(document, "td", {});
      renderAccessScopeCell(document, apiClient, scopeCell, status, user, departments, businessAreas, isSelf);
      row.appendChild(scopeCell);

      const editPermissionsCell = makeElement(document, "td", {});
      renderEditPermissionsCell(
        document,
        apiClient,
        editPermissionsCell,
        status,
        user,
        buildEditableRecordGroups(applications, vendors, departments, businessAreas),
      );
      row.appendChild(editPermissionsCell);

      tbody.appendChild(row);
    }
    table.appendChild(tbody);

    section.append(table, status);
    return section;
  }

  function renderAccessScopeCell(document, apiClient, scopeCell, status, user, departments, businessAreas, isSelf) {
    const summary = makeElement(document, "span", {
      className: "users-table__scope-summary",
      text: formatAccessScopeSummary(user),
    });
    scopeCell.appendChild(summary);

    if (isSelf) {
      // An Admin always sees the full catalog regardless of Access Scope, so
      // editing their own scope is meaningless — show the summary only.
      return;
    }

    const editor = makeElement(document, "div", { className: "users-table__scope-editor" });
    editor.hidden = true;

    const departmentChecks = appendScopeChecklist(
      document,
      editor,
      "Departments",
      departments || [],
      user.scopedDepartmentIds || [],
    );
    const businessAreaChecks = appendScopeChecklist(
      document,
      editor,
      "Business Areas",
      businessAreas || [],
      user.scopedBusinessAreaIds || [],
    );

    const actions = makeElement(document, "div", { className: "users-table__scope-actions" });
    const saveButton = makeElement(document, "button", {
      className: "users-table__scope-save",
      text: "Save Scope",
      type: "button",
    });
    saveButton.addEventListener("click", function onSaveScope() {
      const departmentIds = departmentChecks.filter((check) => check.checked).map((check) => check.value);
      const businessAreaIds = businessAreaChecks.filter((check) => check.checked).map((check) => check.value);
      saveButton.disabled = true;
      return apiClient
        .updateCatalogUserAccessScope(user.id, { departmentIds, businessAreaIds })
        .then(function onSaved(updated) {
          user.scopedDepartmentIds = (updated && updated.scopedDepartmentIds) || departmentIds;
          user.scopedBusinessAreaIds = (updated && updated.scopedBusinessAreaIds) || businessAreaIds;
          summary.textContent = formatAccessScopeSummary(user);
          status.textContent = `Access scope updated for ${user.name || user.email}.`;
          editor.hidden = true;
          saveButton.disabled = false;
        })
        .catch(function onError(error) {
          status.textContent = error.message;
          saveButton.disabled = false;
        });
    });
    actions.appendChild(saveButton);
    editor.appendChild(actions);

    const toggle = makeElement(document, "button", {
      className: "users-table__scope-edit",
      text: "Edit scope",
      type: "button",
    });
    toggle.addEventListener("click", function onToggleEditor() {
      editor.hidden = !editor.hidden;
    });
    scopeCell.append(toggle, editor);
  }

  function appendScopeChecklist(document, editor, legend, options, selectedIds) {
    const group = makeElement(document, "fieldset", { className: "users-table__scope-group" });
    appendTextBlock(document, group, "legend", "users-table__scope-legend", legend);
    const selected = new Set((selectedIds || []).map(String));
    const checks = [];
    for (const option of options) {
      const label = makeElement(document, "label", { className: "users-table__scope-option" });
      const checkbox = makeElement(document, "input", {
        type: "checkbox",
        value: option.id,
        checked: selected.has(String(option.id)),
        attributes: { "aria-label": option.name || "" },
      });
      label.append(checkbox, makeElement(document, "span", { text: option.name || "" }));
      group.appendChild(label);
      checks.push(checkbox);
    }
    editor.appendChild(group);
    return checks;
  }

  function buildEditableRecordGroups(applications, vendors, departments, businessAreas) {
    return [
      { type: "APPLICATION", legend: "Applications", records: applications || [] },
      { type: "VENDOR", legend: "Vendors", records: vendors || [] },
      { type: "DEPARTMENT", legend: "Departments", records: departments || [] },
      { type: "BUSINESS_AREA", legend: "Business Areas", records: businessAreas || [] },
    ];
  }

  function renderEditPermissionsCell(document, apiClient, cell, status, user, recordGroups) {
    const isEditor = String(user.role || "").toUpperCase() === "EDITOR";
    const summary = makeElement(document, "span", {
      className: "users-table__perm-summary",
      text: isEditor ? "Manage permissions" : "Editor Role required",
    });
    cell.appendChild(summary);

    if (!isEditor) {
      // Edit Permission is a per-record grant layered on the Editor Role, so it
      // only applies to Editors. Viewers and Admins are handled by their Role.
      return;
    }

    const editor = makeElement(document, "div", { className: "users-table__perm-editor" });
    editor.hidden = true;

    let loaded = false;
    const grantedKeys = new Set();

    function keyOf(recordType, recordId) {
      return `${recordType}:${recordId}`;
    }

    function updateSummary() {
      const count = grantedKeys.size;
      summary.textContent = count === 0 ? "No records granted" : `${count} record${count === 1 ? "" : "s"} granted`;
    }

    function buildChecklists() {
      for (const group of recordGroups) {
        const fieldset = makeElement(document, "fieldset", { className: "users-table__perm-group" });
        appendTextBlock(document, fieldset, "legend", "users-table__perm-legend", group.legend);
        for (const record of group.records) {
          const label = makeElement(document, "label", { className: "users-table__perm-option" });
          const checkbox = makeElement(document, "input", {
            type: "checkbox",
            value: record.id,
            checked: grantedKeys.has(keyOf(group.type, record.id)),
            attributes: { "aria-label": `${group.legend}: ${record.name || ""}` },
          });
          checkbox.addEventListener("change", function onToggleGrant() {
            const shouldGrant = checkbox.checked;
            checkbox.disabled = true;
            const action = shouldGrant
              ? apiClient.grantCatalogUserEditPermission(user.id, { recordType: group.type, recordId: record.id })
              : apiClient.revokeCatalogUserEditPermission(user.id, group.type, record.id);
            return action
              .then(function onDone() {
                if (shouldGrant) {
                  grantedKeys.add(keyOf(group.type, record.id));
                } else {
                  grantedKeys.delete(keyOf(group.type, record.id));
                }
                updateSummary();
                status.textContent = `Edit permission ${shouldGrant ? "granted" : "revoked"} for ${
                  record.name || "record"
                } (${user.name || user.email}).`;
                checkbox.disabled = false;
              })
              .catch(function onError(error) {
                checkbox.checked = !shouldGrant;
                status.textContent = error.message;
                checkbox.disabled = false;
              });
          });
          label.append(checkbox, makeElement(document, "span", { text: record.name || "" }));
          fieldset.appendChild(label);
        }
        editor.appendChild(fieldset);
      }
    }

    const toggle = makeElement(document, "button", {
      className: "users-table__perm-edit",
      text: "Edit permissions",
      type: "button",
    });
    toggle.addEventListener("click", function onToggleEditor() {
      if (!loaded) {
        toggle.disabled = true;
        return apiClient
          .listCatalogUserEditPermissions(user.id)
          .then(function onLoaded(grants) {
            for (const grant of grants || []) {
              grantedKeys.add(keyOf(grant.recordType, grant.recordId));
            }
            loaded = true;
            updateSummary();
            buildChecklists();
            editor.hidden = false;
            toggle.disabled = false;
          })
          .catch(function onError(error) {
            status.textContent = error.message;
            toggle.disabled = false;
          });
      }
      editor.hidden = !editor.hidden;
      return undefined;
    });

    cell.append(toggle, editor);
  }

  let activeView = "dashboard";

  function renderApp(options) {
    const document = options.document;
    const storage = options.storage;
    const catalogApi = options.catalogApi;
    const apiClient = options.apiClient;
    const root = options.root || document.getElementById("app");
    const catalog = options.catalog || catalogApi.createInitialCatalog();
    const filters = options.filters || {};
    const currentUser = options.currentUser || null;
    const users = options.users || [];
    const isAdmin = currentUser && String(currentUser.role || "").toUpperCase() === "ADMIN";
    const summary = catalogApi.createExecutivePortfolioSummary(catalog, filters);

    function rerender(nextCatalog) {
      renderApp({ document, storage, catalogApi, apiClient, root, catalog: nextCatalog, filters, currentUser, users });
    }

    function setFilters(nextFilters) {
      renderApp({ document, storage, catalogApi, apiClient, root, catalog, filters: nextFilters, currentUser, users });
    }

    const overviewSection = renderOverview(document, catalog, catalogApi, filters, setFilters);
    const applicationsSection = renderApplications(
      document,
      catalog,
      catalogApi,
      apiClient,
      storage,
      rerender,
      summary.filteredApplications,
      filters,
      setFilters,
    );
    const vendorsSection = renderMasterData(document, catalog, catalogApi, apiClient, storage, rerender, {
      id: "vendors",
      title: "Vendors",
      singular: "Vendor",
      kind: "vendor",
      usageField: "vendorId",
      records: (currentCatalog) => currentCatalog.vendors,
      create: (input) => apiClient.createVendor(input),
      update: (id, input) => apiClient.updateVendor(id, input),
      delete: (id) => apiClient.deleteVendor(id),
      describe: catalogApi.getVendorDisplayType,
    });
    const departmentsSection = renderMasterData(document, catalog, catalogApi, apiClient, storage, rerender, {
      id: "departments",
      title: "Departments",
      singular: "Department",
      usageField: "departmentId",
      records: (currentCatalog) => currentCatalog.departments,
      create: (input) => apiClient.createDepartment(input),
      update: (id, input) => apiClient.updateDepartment(id, input),
      delete: (id) => apiClient.deleteDepartment(id),
    });
    const businessAreasSection = renderMasterData(document, catalog, catalogApi, apiClient, storage, rerender, {
      id: "business-areas",
      title: "Business Areas",
      singular: "Business Area",
      usageField: "businessAreaId",
      records: (currentCatalog) => currentCatalog.businessAreas,
      create: (input) => apiClient.createBusinessArea(input),
      update: (id, input) => apiClient.updateBusinessArea(id, input),
      delete: (id) => apiClient.deleteBusinessArea(id),
    });
    const analyticsSection = renderAnalyticsMatrix(document, catalog);

    const views = [];
    const navItems = [];
    function activate(viewKey) {
      activeView = viewKey;
      for (const view of views) {
        const on = view.dataset && view.dataset.view === viewKey;
        view.className = on ? "view view--active" : "view view--hidden";
        view.hidden = !on;
      }
      for (const navItem of navItems) {
        const on = navItem.dataset && navItem.dataset.view === viewKey;
        navItem.className = on
          ? "portfolio-nav__item portfolio-nav__item--active"
          : "portfolio-nav__item";
      }
    }
    function makeView(viewKey, children) {
      const on = activeView === viewKey;
      const view = makeElement(document, "section", {
        className: on ? "view view--active" : "view view--hidden",
        attributes: { "data-view": viewKey },
      });
      view.dataset.view = viewKey;
      if (!on) {
        view.hidden = true;
      }
      view.append(...children);
      views.push(view);
      return view;
    }

    const NAV_VIEWS = [
      { key: "dashboard", label: "Dashboard" },
      { key: "catalog", label: "Application Catalog" },
      { key: "matrix", label: "Analytics Matrix" },
      { key: "master", label: "Master Data" },
    ];
    if (isAdmin) {
      NAV_VIEWS.push({ key: "users", label: "User Management" });
    }
    const nav = makeElement(document, "nav", {
      className: "portfolio-nav",
      attributes: { "aria-label": "Primary navigation" },
    });
    for (const navView of NAV_VIEWS) {
      const on = activeView === navView.key;
      const item = makeElement(document, "a", {
        className: on ? "portfolio-nav__item portfolio-nav__item--active" : "portfolio-nav__item",
        text: navView.label,
        attributes: { href: "#", role: "button", "data-view": navView.key },
      });
      item.dataset.view = navView.key;
      item.addEventListener("click", function onNav(event) {
        if (event && typeof event.preventDefault === "function") {
          event.preventDefault();
        }
        activate(navView.key);
      });
      navItems.push(item);
      nav.appendChild(item);
    }

    const masterTabs = makeElement(document, "div", {
      className: "tabbar",
      attributes: { role: "tablist" },
    });
    const masterPanels = [
      { label: "Vendors", panel: vendorsSection },
      { label: "Departments", panel: departmentsSection },
      { label: "Business Areas", panel: businessAreasSection },
    ];
    const masterTabButtons = [];
    function activateMasterTab(index) {
      for (let i = 0; i < masterPanels.length; i += 1) {
        const on = i === index;
        masterPanels[i].panel.hidden = !on;
        masterPanels[i].panel.className = on
          ? "content-section content-section--compact master-panel master-panel--active"
          : "content-section content-section--compact master-panel master-panel--hidden";
        masterTabButtons[i].className = on ? "tab tab--active" : "tab";
      }
    }
    masterPanels.forEach(function buildTab(entry, index) {
      const on = index === 0;
      const tab = makeElement(document, "button", {
        className: on ? "tab tab--active" : "tab",
        type: "button",
        text: entry.label,
      });
      tab.addEventListener("click", function onTab() {
        activateMasterTab(index);
      });
      masterTabButtons.push(tab);
      masterTabs.appendChild(tab);
      entry.panel.className = on
        ? "content-section content-section--compact master-panel master-panel--active"
        : "content-section content-section--compact master-panel master-panel--hidden";
      if (!on) {
        entry.panel.hidden = true;
      }
    });
    const masterHeader = makeElement(document, "div", { className: "view__header" });
    appendTextBlock(document, masterHeader, "h2", "section-title", "Master Data Management");
    appendTextBlock(
      document,
      masterHeader,
      "p",
      "lede",
      "Centralized ledger for architectural metadata and global taxonomies.",
    );

    const dashboardHeader = makeElement(document, "div", {
      className: "view__header flex flex-wrap justify-between items-end gap-md",
    });
    const dashboardHeading = makeElement(document, "div", {});
    appendTextBlock(document, dashboardHeading, "h2", "section-title", "Executive Overview");
    appendTextBlock(
      document,
      dashboardHeading,
      "p",
      "lede",
      "Real-time strategic indicators for the application portfolio.",
    );
    dashboardHeader.appendChild(dashboardHeading);
    const dashboardChip = makeElement(document, "div", {
      className:
        "hidden md:flex items-center gap-xs bg-white px-sm py-xs border border-solid border-outline-variant rounded text-label-caps uppercase text-on-surface-variant",
    });
    appendTextBlock(document, dashboardChip, "span", "", "Local-first catalog");
    dashboardHeader.appendChild(dashboardChip);

    const dashboardView = makeView("dashboard", [dashboardHeader, overviewSection]);
    const catalogView = makeView("catalog", [applicationsSection]);
    const matrixView = makeView("matrix", [analyticsSection]);
    const masterView = makeView("master", [
      masterHeader,
      masterTabs,
      vendorsSection,
      departmentsSection,
      businessAreasSection,
    ]);
    const contentViews = [dashboardView, catalogView, matrixView, masterView];
    if (isAdmin) {
      const usersSection = renderUsersAdmin(
        document,
        apiClient,
        currentUser,
        users,
        catalog.departments || [],
        catalog.businessAreas || [],
        catalog.applications || [],
        catalog.vendors || [],
      );
      contentViews.push(makeView("users", [usersSection]));
    }

    const sidebar = makeElement(document, "aside", { className: "app-sidebar" });
    const brand = makeElement(document, "div", { className: "app-brand" });
    appendTextBlock(document, brand, "span", "app-brand__mark", "AP");
    appendTextBlock(document, brand, "span", "app-brand__name", "Application Portfolio");
    sidebar.append(brand, nav);

    const content = makeElement(document, "div", { className: "app-content" });
    content.append(...contentViews);

    root.replaceChildren(sidebar, content);
    return { root, catalog };
  }

  function init(root) {
    const catalogApi = root.ApplicationPortfolioCatalog;
    const apiClient = root.ApplicationPortfolioApiClient;
    const document = root.document;
    const storage = root.localStorage;
    if (!catalogApi || !apiClient || !document) {
      return null;
    }

    const loadCurrentUser =
      typeof apiClient.getCurrentUser === "function"
        ? apiClient.getCurrentUser().catch(function onMeError() {
            return null;
          })
        : Promise.resolve(null);

    return Promise.all([
      apiClient.listVendors(),
      apiClient.listDepartments(),
      apiClient.listBusinessAreas(),
      apiClient.listApplications(),
      loadCurrentUser,
    ])
      .then(function onLoaded([vendors, departments, businessAreas, applications, currentUser]) {
        const catalog = { vendors, departments, businessAreas, applications };
        const isAdmin = currentUser && String(currentUser.role || "").toUpperCase() === "ADMIN";
        const usersPromise =
          isAdmin && typeof apiClient.listCatalogUsers === "function"
            ? apiClient.listCatalogUsers().catch(function onUsersError() {
                return [];
              })
            : Promise.resolve([]);
        return usersPromise.then(function onUsers(users) {
          return renderApp({ document, storage, catalogApi, apiClient, catalog, currentUser, users });
        });
      })
      .catch(function onLoadError(error) {
        const root2 = document.getElementById("app");
        if (root2) {
          root2.textContent = `Unable to load the application portfolio: ${error.message}`;
        }
        return null;
      });
  }

  return { init, renderApp };
});
