
## 1. Authentication & Access Management

### Login (SSO)
*   **Folder**: design/Login_SSO
*   **Purpose**: The primary entry point for all users.
*   **Value**: Ensures secure, centralized authentication using corporate credentials. It simplifies the user experience by prioritizing SSO while providing a fallback path for administrative "Local Login" accounts.

### Local Login
*   **Folder**: design/Local_Login
*   **Purpose**: A secondary login path for administrative or external accounts not integrated into the SSO provider.
*   **Value**: Provides redundancy and allows administrators to maintain access during SSO migrations or outages.

### User Management
*   **Folder**: design/User_Management
*   **Purpose**: The administrative hub for controlling who can access the portfolio and at what level.
*   **Value**: Displays a high-density table of all catalog users, their login methods, and roles (Viewer, Editor, Admin). It highlights "Access Scope" gaps to ensure data security and governance.

### Invite User Modal
*   **Folder**: design/Invite_User_Modal
*   **Purpose**: A streamlined workflow for administrators to add new "Local Login" users.
*   **Value**: Simplifies the onboarding process by capturing name, email, and initial role, then triggering an automated invitation.

### Access Scope Modal
*   **Folder**: design/Access_Scope_Assignment
*   **Purpose**: Granular control over data visibility for individual users.
*   **Value**: Allows administrators to restrict a user's view to specific Departments or Business Areas. The "No scope" warning state ensures no user accidentally sees data they shouldn't.

### Edit Permission Panel
*   **Folder**: design/Edit_Permission_Grant
*   **Purpose**: Record-level governance within Application or Master Data profiles.
*   **Value**: Overrides default role-based visibility to grant specific "Editor" rights to individual users for a particular record, ensuring data stewardship is assigned to the right experts.

---
