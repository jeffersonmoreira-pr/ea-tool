(function attachApiClient(root, factory) {
  const api = factory();
  if (typeof module === "object" && module.exports) {
    module.exports = api;
  }
  if (root) {
    root.ApplicationPortfolioApiClient = api;
  }
})(typeof globalThis !== "undefined" ? globalThis : undefined, function createApiClient() {
  function readCookie(name) {
    const cookieSource = typeof document !== "undefined" && document ? document.cookie : "";
    const match = String(cookieSource || "").match(new RegExp("(?:^|; )" + name + "=([^;]*)"));
    return match ? decodeURIComponent(match[1]) : null;
  }

  function getFetch() {
    if (typeof fetch === "function") {
      return fetch;
    }
    if (typeof window !== "undefined" && typeof window.fetch === "function") {
      return window.fetch;
    }
    if (typeof global !== "undefined" && typeof global.fetch === "function") {
      return global.fetch;
    }
    throw new Error("This browser or environment does not support network requests.");
  }

  function readErrorMessage(response) {
    return response
      .json()
      .catch(function onParseFailure() {
        return null;
      })
      .then(function throwWithMessage(body) {
        const message = body && body.message ? body.message : "Request failed.";
        throw new Error(message);
      });
  }

  function request(method, url, body) {
    let activeFetch;
    try {
      activeFetch = getFetch();
    } catch (error) {
      return Promise.reject(error);
    }

    const headers = {};
    const options = { method, credentials: "include", headers };

    if (method !== "GET") {
      headers["X-XSRF-TOKEN"] = readCookie("XSRF-TOKEN") || "";
    }
    if (body !== undefined) {
      headers["Content-Type"] = "application/json";
      options.body = JSON.stringify(body);
    }

    return activeFetch(url, options)
      .catch(function onNetworkError() {
        throw new Error("Unable to reach the server. Please check your connection and try again.");
      })
      .then(function handleResponse(response) {
        if (response.status === 204) {
          return undefined;
        }
        if (!response.ok) {
          return readErrorMessage(response);
        }
        return response.json().catch(function onParseError() {
          throw new Error("Received an unexpected response from the server.");
        });
      });
  }

  function listDepartments() {
    return request("GET", "/api/departments");
  }
  function createDepartment(input) {
    return request("POST", "/api/departments", input);
  }
  function updateDepartment(id, input) {
    return request("PUT", `/api/departments/${encodeURIComponent(id)}`, input);
  }
  function deleteDepartment(id) {
    return request("DELETE", `/api/departments/${encodeURIComponent(id)}`);
  }

  function listVendors() {
    return request("GET", "/api/vendors");
  }
  function createVendor(input) {
    return request("POST", "/api/vendors", input);
  }
  function updateVendor(id, input) {
    return request("PUT", `/api/vendors/${encodeURIComponent(id)}`, input);
  }
  function deleteVendor(id) {
    return request("DELETE", `/api/vendors/${encodeURIComponent(id)}`);
  }

  function listBusinessAreas() {
    return request("GET", "/api/business-areas");
  }
  function createBusinessArea(input) {
    return request("POST", "/api/business-areas", input);
  }
  function updateBusinessArea(id, input) {
    return request("PUT", `/api/business-areas/${encodeURIComponent(id)}`, input);
  }
  function deleteBusinessArea(id) {
    return request("DELETE", `/api/business-areas/${encodeURIComponent(id)}`);
  }

  function listApplications() {
    return request("GET", "/api/applications");
  }
  function createApplication(input) {
    return request("POST", "/api/applications", input);
  }
  function updateApplication(id, input) {
    return request("PUT", `/api/applications/${encodeURIComponent(id)}`, input);
  }
  function deleteApplication(id) {
    return request("DELETE", `/api/applications/${encodeURIComponent(id)}`);
  }

  function getCurrentUser() {
    return request("GET", "/api/me");
  }
  function listCatalogUsers() {
    return request("GET", "/api/catalog-users");
  }
  function updateCatalogUserRole(id, role) {
    return request("PUT", `/api/catalog-users/${encodeURIComponent(id)}/role`, { role });
  }
  function createLocalUser(input) {
    return request("POST", "/api/catalog-users/local", {
      name: input && input.name,
      email: input && input.email,
      role: input && input.role,
    });
  }
  function updateCatalogUserAccessScope(id, scope) {
    return request("PUT", `/api/catalog-users/${encodeURIComponent(id)}/access-scope`, {
      departmentIds: (scope && scope.departmentIds) || [],
      businessAreaIds: (scope && scope.businessAreaIds) || [],
    });
  }
  function listCatalogUserEditPermissions(id) {
    return request("GET", `/api/catalog-users/${encodeURIComponent(id)}/edit-permissions`);
  }
  function grantCatalogUserEditPermission(id, grant) {
    return request("POST", `/api/catalog-users/${encodeURIComponent(id)}/edit-permissions`, {
      recordType: grant && grant.recordType,
      recordId: grant && grant.recordId,
    });
  }
  function revokeCatalogUserEditPermission(id, recordType, recordId) {
    return request(
      "DELETE",
      `/api/catalog-users/${encodeURIComponent(id)}/edit-permissions/${encodeURIComponent(recordType)}/${encodeURIComponent(recordId)}`,
    );
  }

  return {
    listDepartments,
    createDepartment,
    updateDepartment,
    deleteDepartment,
    listVendors,
    createVendor,
    updateVendor,
    deleteVendor,
    listBusinessAreas,
    createBusinessArea,
    updateBusinessArea,
    deleteBusinessArea,
    listApplications,
    createApplication,
    updateApplication,
    deleteApplication,
    getCurrentUser,
    listCatalogUsers,
    updateCatalogUserRole,
    createLocalUser,
    updateCatalogUserAccessScope,
    listCatalogUserEditPermissions,
    grantCatalogUserEditPermission,
    revokeCatalogUserEditPermission,
  };
});
