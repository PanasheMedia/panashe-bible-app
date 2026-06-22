# Panashe Bible App — Web Parity Tracker

Track visual and behavioral parity between the Compose Multiplatform app and the live web version.

## Convention
- `[x]` — matches web
- `[~]` — partial / close but not pixel-perfect
- `[ ]` — not started
- `[—]` — not applicable

---

## §1 Shell (Header / Navigation)

- `[x]` Header sticky, 1px bottom divider
- `[x]` Wordmark "The Bible" (serif, 20sp)
- `[x]` Primary tabs: Daily, Scripture, Communion
- `[x]` Active tab underline (2px ink)
- `[x]` Search icon on all widths
- `[x]` Settings "Aa" button on all widths
- `[x]` Theme toggle (moon/sun) on all widths
- `[x]` Dark mode persistence (AppSettings + system follow)
- `[x]` Bottom tabs (mobile) with soft active bg
- `[~]` Footer: Church / About / Privacy Policy links
- `[ ]` Backdrop-filter blur on header (stretch — platform-limited)

## §2 Daily Screen

- `[x]` Daily hero (h1 "Daily Reading" + intro paragraph)
- `[x]` Eyebrow with formatted date
- `[x]` Single passage card (was two cards, now consolidated)
- `[x]` Reference heading (serif, 20sp semibold)
- `[x]` Chapter intro text (muted, 14sp)
- `[x]` Scripture verses rendered serif 18sp
- `[x]` Action buttons: Read full chapter, Copy passage, Browse the Bible
- `[x]` Button divider line (1px line)

## §3 Reader (Bible Screen)

- `[x]` Sticky toolbar: Book | Chapter | Translation
- `[x]` Translation info cell (KJVA badge, opens translation info dialog)
- `[x]` Chapter header (book name + chapter number, serif 52sp)
- `[x]` Eyebrow for section (Old/New Testament)
- `[x]` Jump-to-verse picker
- `[x]` Verse rendering with verse-number superscript (accent)
- `[x]` Line-by-line reader preference
- `[x]` Chapter nav (Previous / Next)
- `[x]` Audio playback button with PlayIcon
- `[x]` TTS: play/stop (platform-specific implementation)

## §4 Communion

- `[x]` Communion hero (h1 "Daily Communion" + intro)
- `[x]` Accent top-border on top card
- `[x]` Top card and tab card joined (shared border radius, no gap)
- `[x]` Segmented tabs: Read / Offer
- `[~]` Offering form (cascading dropdowns — preserved from before)
- `[x]` Reddit-style thread layout (post + comments)
- `[x]` Thread connector lines (vertical + horizontal, hover accent)
- `[x]` Archive grid (FlowRow responsive layout)
- `[x]` Archive items with hover effects
- `[x]` "Today" badge on archive item
- `[x]` Archive detail dialog (migrated to PanasheDialog)

## §5 Dialogs (PanasheDialog)

- `[x]` PanasheDialog scaffold: top-anchored, serif header, close-X, scrim, spring entrance
- `[x]` Search dialog migrated to PanasheDialog
- `[x]` Settings dialog migrated to PanasheDialog
- `[x]` Book picker dialog migrated to PanasheDialog
- `[x]` Chapter picker dialog migrated to PanasheDialog
- `[x]` Archive detail dialog migrated to PanasheDialog

## §6 Polish

- `[ ]` Card entrance stagger animation (not yet)
- `[x]` Loading pulse animation (alpha pulse on LoadingText)
- `[ ]` Toast notification with slide-up (in progress — existing Surface toast)
- `[ ]` Button press-scale on tap (modifier stubbed)
- `[ ]` Archive item enter animation (staggered)

## Audio

- `[x]` TTS engine interface (expect/actual for Android/iOS)
- `[x]` Play/pause button on chapter header
- `[ ]` Audio spinner during loading
- `[ ]` Settings: voice selector
- `[ ]` Settings: speed control (.8x, 1x, 1.2x)

---

## Build Targets

| Surface    | Android | iOS |
|------------|---------|-----|
| Daily      | ✓ build | —   |
| Bible      | ✓ build | —   |
| Communion  | ✓ build | —   |
| Search     | ✓ build | —   |
| Settings   | ✓ build | —   |

*✓ build = compiles clean. Visual verification against web pending.*
