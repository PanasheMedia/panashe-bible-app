# App → Web UI/UX Parity

Tracks bringing the Compose app (`composeApp`) to visual + interaction parity with
`panashe-bible-web`. Web is the source of truth. Design tokens (colors, fonts, radius)
already match; this is about shell chrome, component fidelity, interactions, and a few
structural/functional holes.

Token reference: `ui/PanasheTheme.kt`. Web reference: `panashe-bible-web/styles.css`.

## Status

- [x] **Cleanup** — removed dead web CSS (`communion-steps`, `communion-hero-cross`,
      `communion-process-note`, `communion-privacy-note`, `communion-archive-intro`,
      `cross-breathe` keyframe) and orphaned app imports after the Communion trim.
- [ ] **§1 Shell** (`App.kt`, `PanasheTheme.kt`, `AppSettings.kt`)
  - [ ] Header: 1px bottom divider only (not 4-sided box); sticky feel
  - [ ] Bottom tabs: 1px top divider only
  - [ ] Icon set (vector): Aa, moon/sun, magnifier, play, close, chevrons
  - [ ] Active-tab underline indicator
  - [ ] Search + Settings reachable at all widths (currently hidden > 700.dp)
  - [ ] Manual dark-mode toggle + persist to `AppSettings` (currently system-only)
- [ ] **§5a Shared `PanasheDialog`** — top-anchored sheet, serif header, close-X,
      blurred scrim, spring entrance. Replaces Material `AlertDialog`.
- [ ] **§2 Daily** (`ReaderScreen.kt` `DailyReadingScreen`) — add hero, collapse to one
      passage card, drop duplicated second card + repeated chapterIntro.
- [ ] **§4 Communion** (`CommunionScreen.kt`) — accent top-border joined cards;
      thread connector lines (vertical + elbow, accent on hover); archive grid + today badge + empty state.
- [ ] **§3 Reader** (`ReaderScreen.kt`) — sticky toolbar + Translation (KJVA) cell;
      **audio playback (TTS)**; two-cell chapter nav.
- [ ] **§5b Dialog internals** — migrate Search / Settings / Book / Chapter onto
      `PanasheDialog`; match contents (filter chips, shimmer, font cards, size grid, switches).
- [ ] **§6 Polish** — staggered card entrance, bottom toast, loading pulse, button press-scale.
- [ ] **Verify** — build + screenshot each surface (Android target; iOS sim screenshots
      are blank for Skia) and compare to live web.

## Toolchain notes (Intel Mac)
- iosX64 target; Kotlin 2.2.0 / Compose 1.9.0 / AGP 8.13.0; Gradle pinned to JDK 17.
- `simctl io screenshot` returns blank for Skia surfaces → verify visuals on Android.
