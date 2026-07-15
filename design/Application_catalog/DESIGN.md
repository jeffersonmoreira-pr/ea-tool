---
name: Strategic Enterprise Ledger
colors:
  surface: '#fbf8fb'
  surface-dim: '#dbd9dc'
  surface-bright: '#fbf8fb'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f5f3f6'
  surface-container: '#efedf0'
  surface-container-high: '#eae7ea'
  surface-container-highest: '#e4e2e5'
  on-surface: '#1b1b1e'
  on-surface-variant: '#44474d'
  inverse-surface: '#303033'
  inverse-on-surface: '#f2f0f3'
  outline: '#75777e'
  outline-variant: '#c5c6ce'
  surface-tint: '#4f5e7e'
  primary: '#041632'
  on-primary: '#ffffff'
  primary-container: '#1b2b48'
  on-primary-container: '#8393b5'
  inverse-primary: '#b7c7eb'
  secondary: '#006591'
  on-secondary: '#ffffff'
  secondary-container: '#39b8fd'
  on-secondary-container: '#004666'
  tertiary: '#211500'
  on-tertiary: '#ffffff'
  tertiary-container: '#3b2800'
  on-tertiary-container: '#ac8e5b'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#d7e2ff'
  primary-fixed-dim: '#b7c7eb'
  on-primary-fixed: '#091b37'
  on-primary-fixed-variant: '#374765'
  secondary-fixed: '#c9e6ff'
  secondary-fixed-dim: '#89ceff'
  on-secondary-fixed: '#001e2f'
  on-secondary-fixed-variant: '#004c6e'
  tertiary-fixed: '#ffdea7'
  tertiary-fixed-dim: '#e3c28a'
  on-tertiary-fixed: '#271900'
  on-tertiary-fixed-variant: '#594317'
  background: '#fbf8fb'
  on-background: '#1b1b1e'
  surface-variant: '#e4e2e5'
typography:
  display-lg:
    fontFamily: Inter
    fontSize: 30px
    fontWeight: '700'
    lineHeight: 38px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: -0.01em
  title-sm:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  body-sm:
    fontFamily: Inter
    fontSize: 13px
    fontWeight: '400'
    lineHeight: 18px
  label-caps:
    fontFamily: Inter
    fontSize: 11px
    fontWeight: '700'
    lineHeight: 16px
    letterSpacing: 0.05em
  data-mono:
    fontFamily: Inter
    fontSize: 13px
    fontWeight: '500'
    lineHeight: 18px
rounded:
  sm: 0.125rem
  DEFAULT: 0.25rem
  md: 0.375rem
  lg: 0.5rem
  xl: 0.75rem
  full: 9999px
spacing:
  base: 4px
  xs: 4px
  sm: 8px
  md: 16px
  lg: 24px
  xl: 32px
  container-max: 1440px
  gutter: 16px
---

## Brand & Style

The design system is engineered for IT leadership and enterprise architects. It prioritizes **Strategic Intelligence** over decorative flair. The brand personality is authoritative yet helpful, acting as a reliable source of truth for large-scale decision-making. 

The visual style follows a **Modern Corporate** aesthetic with a lean toward **High-Density Utility**. It utilizes a structured grid, subtle borders, and a neutral foundation to ensure that critical data—not the UI itself—remains the focus. The interface conveys efficiency through tight spacing, rigorous alignment, and a sophisticated color logic that maps directly to strategic outcomes (TIME model).

## Colors

The palette is functional and semantic. 
- **Deep Navy (#1B2B48)** serves as the anchor for navigation and global headers, establishing a professional and stable environment.
- **Semantic Indicators** are strictly tied to the TIME classification model:
  - **Success (Invest):** Emerald Green represents growth and stability.
  - **Warning (Tolerate):** Amber signals maintenance without further investment.
  - **Info (Migrate):** Sky Blue denotes movement and transition.
  - **Danger (Eliminate):** Rose indicates risk or decommissioning.
- **Backgrounds:** A soft **Neutral Gray (#F8FAFC)** provides a low-strain canvas for high-density data tables, while **White (#FFFFFF)** is reserved for interactive cards and elevated surfaces.

## Typography

This design system utilizes **Inter** for its exceptional legibility in data-heavy environments. 
- **Hierarchy:** Distinct contrast is maintained between "Labels" (small, bold, often uppercase) and "Data" (standard weight, high legibility).
- **Numerical Data:** Use tabular figures (`tnum`) for columns of numbers and financial values to ensure vertical alignment across table rows.
- **Scale:** Type sizes are slightly smaller than consumer standards to accommodate high information density without sacrificing clarity.

## Layout & Spacing

The layout uses a **Fluid Grid** with a maximum container width of 1440px to prevent excessive line lengths on ultra-wide monitors. 
- **Rhythm:** A 4px baseline grid governs all spacing. 
- **Density:** Padding within data tables should be "Compact" (8px vertical, 12px horizontal) to maximize the visible records per screen.
- **Responsiveness:** On tablet/mobile, the sidebar navigation collapses into a slim icon bar or drawer, and multi-column data tables transition into "List Cards" with key strategic indicators prioritized.

## Elevation & Depth

To maintain a clean, professional look, this design system avoids heavy shadows. 
- **Tonal Layers:** Elevation is primarily communicated through color shifts (e.g., White cards on a Gray background).
- **Subtle Outlines:** Use 1px borders (#E2E8F0) to define sections and containers. 
- **Active Elevation:** Only use a very soft, diffused shadow (0px 4px 6px -1px rgba(0,0,0,0.05)) on hovered cards or open dropdown menus to indicate interactivity without breaking the flat, structured aesthetic.

## Shapes

The shape language is **Soft (0.25rem)**. This provides a subtle modern touch that softens the "industrial" feel of dense data without appearing overly casual or playful.
- **Buttons & Inputs:** 4px (0.25rem) radius.
- **Cards & Modals:** 8px (0.5rem) radius.
- **Status Badges:** 2px or fully square to differentiate them from interactive buttons.

## Components

### Data Tables
The core component. Features includes sticky headers, alternating row stripes (zebra striping) in very faint gray, and "Sort" indicators. Strategic Classifications (TIME) should be the first column after the Application Name.

### Status Badges (Pills)
Used for **Lifecycle** and **TIME** classifications.
- **TIME Badges:** Solid background with white text for high visibility (e.g., "INVEST" in Emerald).
- **Lifecycle/PACE:** Ghost style (outlined) or subtle tint backgrounds to ensure they don't compete with the TIME status.

### Business Fit Ratings
Represented by a **5-segment bar horizontal chart** rather than stars to maintain a technical, data-driven feel. Each segment fills to represent a score of 1-5.

### Progress Bars (Catalog Quality)
Thin (4px-6px) bars used within table cells or header stats. Use the primary Navy color for the progress fill and a light gray for the track.

### Input Fields
Strictly rectangular with 1px borders. Focused state uses a 2px Deep Navy border or a Sky Blue glow to indicate active entry.

### Cards
Used for "Summary Insights" at the top of the catalog. Should include a large numeric value (Display-LG) and a small Label-Caps title.