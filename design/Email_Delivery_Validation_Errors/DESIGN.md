---
name: Strategic Enterprise Ledger
colors:
  surface: '#f9f9ff'
  surface-dim: '#d3daea'
  surface-bright: '#f9f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f0f3ff'
  surface-container: '#e7eefe'
  surface-container-high: '#e2e8f8'
  surface-container-highest: '#dce2f3'
  on-surface: '#151c27'
  on-surface-variant: '#40484f'
  inverse-surface: '#2a313d'
  inverse-on-surface: '#ebf1ff'
  outline: '#717880'
  outline-variant: '#c0c7d0'
  surface-tint: '#106493'
  primary: '#00496f'
  on-primary: '#ffffff'
  primary-container: '#0c6291'
  on-primary-container: '#b2dbff'
  inverse-primary: '#90cdff'
  secondary: '#4f5e7e'
  on-secondary: '#ffffff'
  secondary-container: '#cadaff'
  on-secondary-container: '#505f7f'
  tertiary: '#434647'
  on-tertiary: '#ffffff'
  tertiary-container: '#5b5d5f'
  on-tertiary-container: '#d5d6d8'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#cbe6ff'
  primary-fixed-dim: '#90cdff'
  on-primary-fixed: '#001e30'
  on-primary-fixed-variant: '#004b72'
  secondary-fixed: '#d7e2ff'
  secondary-fixed-dim: '#b7c7eb'
  on-secondary-fixed: '#091b37'
  on-secondary-fixed-variant: '#374765'
  tertiary-fixed: '#e1e2e4'
  tertiary-fixed-dim: '#c5c6c8'
  on-tertiary-fixed: '#191c1e'
  on-tertiary-fixed-variant: '#444749'
  background: '#f9f9ff'
  on-background: '#151c27'
  surface-variant: '#dce2f3'
typography:
  headline-lg:
    fontFamily: Inter
    fontSize: 30px
    fontWeight: '600'
    lineHeight: 38px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: -0.01em
  headline-sm:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 26px
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  body-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 18px
  label-md:
    fontFamily: Inter
    fontSize: 13px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.01em
  label-sm:
    fontFamily: Inter
    fontSize: 11px
    fontWeight: '600'
    lineHeight: 14px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  container-max: 1280px
  gutter: 24px
  margin-page: 32px
  panel-padding: 24px
  stack-sm: 8px
  stack-md: 16px
  stack-lg: 32px
---

## Brand & Style

The design system is engineered for high-stakes enterprise administration, specifically tailored for the Email Delivery module. The brand personality is rooted in reliability, transparency, and technical precision. It prioritizes a "calm-tech" aesthetic that reduces cognitive load when managing complex configurations and high-volume data.

The style is **Corporate / Modern**, characterized by a systematic approach to whitespace, a rigorous adherence to grid structures, and a refined use of depth. The interface utilizes a "contained-fluidity" approach, where information is grouped into distinct, elevated panels to provide a clear sense of hierarchy and focus.

**Key Visual Principles:**
- **Data-Dense but Readable:** High information density achieved through tight vertical rhythm and precise typography.
- **Trustworthy Professionalism:** A palette dominated by stable blues and high-clarity neutrals.
- **Functional Clarity:** Every visual element—from a shadow to a border—serves to delineate functional boundaries or state changes.

## Colors

The color architecture of this design system distinguishes between global navigation and modular action. 

- **Primary (#0c6291):** Used for active states, primary action buttons, and focused interactions within the Email Delivery module. It is a lighter, more vibrant blue than the global navigation to draw the eye to core configuration tasks.
- **Secondary (#1b2b48):** Reserved for global navigation, structural headers, and high-level organizational elements. It provides a grounded, "heavy" anchor to the interface.
- **Backgrounds:** The primary page background is `#f4f5f7`, creating a subtle contrast against the pure white (`#ffffff`) used for administrative panels and cards.
- **Semantic Palette:** Status-driven colors for email health and delivery reports. Use these exclusively for feedback loops (success, warning, error) and status indicators.

## Typography

This design system uses **Inter** exclusively to leverage its exceptional legibility in data-heavy environments. The typographic scale is built on a modular scale that favors clarity and vertical alignment.

- **Headlines:** Used for page titles and section headers within administrative cards. Use `headline-lg` for primary page headers and `headline-sm` for card titles.
- **Body:** `body-md` is the standard for form inputs, descriptions, and list items. 
- **Labels:** `label-md` is used for form field labels. `label-sm` (uppercase) is reserved for small status badges and metadata headers to provide contrast against standard body text.

## Layout & Spacing

The layout utilizes a **Fixed Grid** philosophy for desktop views to ensure readability of long-form configurations, transitioning to a fluid model for smaller viewports.

- **Grid System:** A 12-column grid with a 24px gutter. 
- **Two-Column Pattern:** For the Email Delivery module, use a 2:1 ratio (8 columns for primary configuration/content, 4 columns for supplemental stats or secondary settings).
- **Responsive Behavior:** 
  - **Desktop (1024px+):** Two-column layout with fixed 32px page margins.
  - **Tablet (768px - 1023px):** Columns stack vertically. Sidebars move beneath primary panels.
  - **Mobile (<767px):** Page margins reduce to 16px. All panels span 100% width.

## Elevation & Depth

Visual hierarchy is established through **Ambient Shadows** and tonal layering. This system avoids harsh borders in favor of soft, tinted shadows that suggest elevation without cluttering the UI.

- **Surface Levels:** The canvas is `#f4f5f7`. All interactive panels and cards sit at a +1 elevation level using `#ffffff`.
- **Shadow Profile:** Use a soft navy-tinted shadow for panels. 
  - *Shadow Token:* `0px 4px 12px rgba(27, 43, 72, 0.08)`.
- **Interactive Depth:** On hover, primary cards can transition to a +2 elevation (`rgba(27, 43, 72, 0.12)`) to indicate interactivity.
- **Flat Elements:** Use 1px borders (`#e5e7eb`) for internal card dividers and table rows to maintain structure without adding depth.

## Shapes

The shape language is "Rounded," balancing the professional nature of the ledger with a modern, approachable feel.

- **Panels & Cards:** 12px (`rounded-lg`) corner radius for all primary containers.
- **Inputs & Buttons:** 8px (`rounded-md`) radius to provide a precise, clickable appearance.
- **Status Pills:** Fully rounded (pill-shaped) to distinguish them from functional buttons.

## Components

### Card-Based Forms
Group related settings (e.g., "SMTP Configuration," "Tracking Pixel Settings") into white panels with 24px internal padding. Use a `headline-sm` title at the top of each card followed by a 1px divider.

### Status Pills
Used for email delivery states (e.g., Sent, Bounced, Queued).
- **Background:** Low-opacity version of semantic colors (10% alpha).
- **Text:** High-contrast version of semantic colors.
- **Style:** `label-sm` typography, pill-shaped, 4px vertical / 12px horizontal padding.

### Buttons
- **Primary:** Background `#0c6291`, white text.
- **Secondary:** Transparent background, `#0c6291` border and text.
- **Ghost:** No border, `#1b2b48` text, subtle grey hover.

### Input Fields
- **Default:** White background, `#d1d5db` border, 8px radius.
- **Focus:** 2px ring of `#0c6291` with 4px offset.
- **Label:** `label-md` positioned above the input with a 4px gap.

### Data Lists
For email logs, use a borderless table style with 1px horizontal dividers. High-density row height (48px). Use `body-sm` for secondary data like timestamps and ID strings.