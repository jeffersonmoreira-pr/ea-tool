(function initAuthBar(window) {
  "use strict";

  const document = window.document;

  function readCookie(name) {
    const match = document.cookie.match(new RegExp("(?:^|; )" + name + "=([^;]*)"));
    return match ? decodeURIComponent(match[1]) : null;
  }

  function buildBar(user) {
    const bar = document.createElement("div");
    bar.className = "auth-bar";
    bar.setAttribute("role", "banner");

    const identity = document.createElement("span");
    identity.className = "auth-bar__identity";
    identity.textContent = user.name || user.email || "Signed in";
    bar.appendChild(identity);

    const logoutButton = document.createElement("button");
    logoutButton.type = "button";
    logoutButton.className = "auth-bar__logout";
    logoutButton.textContent = "Log out";
    logoutButton.addEventListener("click", function onLogout() {
      fetch("/logout", {
        method: "POST",
        headers: { "X-XSRF-TOKEN": readCookie("XSRF-TOKEN") || "" },
      }).then(function afterLogout() {
        window.location.href = "/";
      });
    });
    bar.appendChild(logoutButton);

    return bar;
  }

  function mount() {
    fetch("/api/me", { headers: { Accept: "application/json" } })
      .then(function parse(response) {
        return response.ok ? response.json() : null;
      })
      .then(function render(user) {
        if (!user) {
          return;
        }
        document.body.insertBefore(buildBar(user), document.body.firstChild);
      })
      .catch(function ignore() {
        // The auth bar is a non-critical enhancement; the catalog still works without it.
      });
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", mount);
  } else {
    mount();
  }
})(window);
