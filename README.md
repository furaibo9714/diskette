
# Diskette

<img src="assets/graphics/logo.svg" alt="Diskette logo" align="left" width="180" hspace="0" vspace="30" />

Diskette is a modern TV Shows and Movies tracking app for [Floppy](https://github.com/dannyvfilms/Floppy).

Diskette is a fork of [Showly](https://github.com/michaldrabik/showly-2.0) by [michaldrabik](https://github.com/michaldrabik), rebuilt
around a self-hosted Floppy backend instead of Trakt.tv. It is completely free of all Google services.

Releases are published on this repository and are meant to be installed and kept up to date with
[Obtainium](https://github.com/ImranR98/Obtainium).

## Screenshots

<div>
   <img src="https://github.com/user-attachments/assets/84f00049-6593-4cb6-bbe3-9cff7ffd313f" width="160" alt="Screenshot 1">
  &nbsp;&nbsp;
   <img src="https://github.com/user-attachments/assets/81dcb5a1-0db0-40bd-bba4-0bb82fbc7a4e" width="160" alt="Screenshot 2">
  &nbsp;&nbsp;
   <img src="https://github.com/user-attachments/assets/a6959690-57d0-40c9-a2e3-f02216aacc71" width="160" alt="Screenshot 3">
  &nbsp;&nbsp;
  <img src="https://github.com/user-attachments/assets/255505c5-ddc4-4ae6-b130-1ef47057e9b8" width="160" alt="Screenshot 4">
</div>

## About This Fork

This fork diverges from upstream in two ways:

1. **Trakt.tv, fully removed.** Trakt's API became partially paywalled, so this fork is now a phone
   client built exclusively for [Floppy](https://github.com/dannyvfilms/Floppy), a self-hosted
   tracker, with metadata/search/discovery served directly from TMDB instead of proxied through
   Trakt. See [`TRAKT_MIGRATION.md`](TRAKT_MIGRATION.md) for the full milestone-by-milestone
   writeup.
2. **Premium/paywall system, removed.** The upstream premium gates had no billing backend behind
   them in this fork (dead UI only) — they've been deleted and the three features they used to
   lock (app theme, collection view mode, widget theme/transparency) rebuilt as normal, always-on
   settings.

## Roadmap

Status of fork-specific work, grouped by theme. Upstream features/issues aren't tracked here.

### Trakt → Floppy migration

- [x] Metadata/Search/Discover → TMDB direct
- [x] Surface Floppy manual/custom entries (no external provider id)
- [x] Watchlist/watched sync → Floppy (auth, sync engine, import reconciliation)
- [x] Remove Trakt from the watchlist/watched write path (dual-write → Floppy-only)
- [x] External ratings strip → Floppy-sourced TMDB rating (OMDB dependency removed entirely)
- [x] Personal rating sync (show/movie/season/episode) → Floppy
- [x] Custom lists (create/rename/membership/delete) → Floppy
- [x] Hidden/Archive → Floppy
- [x] Comments feature — dropped (no Floppy equivalent, not migrated)
- [x] Background full-sync engine (app start + pull-to-refresh) rebuilt on Floppy, old Trakt engine deleted
- [x] Delete all remaining Trakt OAuth/account-linking code
- [x] Cleartext networking support for self-hosted/LAN Floppy instances
- [ ] Beyond migration: evaluate Floppy's own endpoints as replacements for local-only or TMDB-only features (not scoped yet)
  - [ ] `/api/v1/discover/` — personalized recommendations from the user's own Floppy library, a different (arguably more relevant) signal than TMDB's generic trending/popular.
  - [ ] `/api/v1/home/` — a ready-made "home feed," possibly a better fit than assembling one from separate TMDB calls.
  - [ ] `/api/v1/statistics/`, `/api/v1/statistics/overview/` — Floppy-computed stats, an alternative to `ui-statistics`' local aggregation.
  - [ ] `/api/v1/tags/` — user-defined tagging, no current Diskette equivalent at all.
  - [ ] Support other media types tracked in Floppy:
    - [ ] Manga
    - [ ] Games
    - [ ] Books
    - [ ] Comics
    - [ ] Board Games
    - [ ] Music
    - [ ] Podcasts

### Premium removal & free features

- [x] Remove premium/paywall system (module, nav actions, dead UI, gating code)
- [x] Widget settings — working Theme + Transparency pickers, persisted, live widget refresh
- [x] Collection grid view mode (My Shows/Movies, Watchlist, Hidden, List Details)
- [x] App theme — Light/Dark picker, new light Material3 palette, persists across restarts
  - [x] Fix contrast regressions surfaced by the new light palette (button text, switch "off"
        state, image-overlay title text, toolbar back arrow, button outlines)
- [ ] Dracula theme
- [x] Final verification & cleanup pass (repo-wide grep sweep, lint unused-resource pass,
      translation coverage for new strings — all clean, no orphaned resources from this work)
- [x] Compact list view mode (third view mode, cycling List → Grid → Compact on the same chip
      across all 7 collection screens)

### Metadata & data quality

- [x] Fix "-1 min" runtime on every movie/show detail screen — the details cache was considered
      fresh even when the row had only ever been populated by a Discover/search/related list
      response (TMDB's compact list shape carries no runtime), so the real `/movie|tv/{id}` fetch
      never ran. Both detail repositories now refetch when a cached row looks incomplete.
- [x] Show runtime falls back to real episode runtimes when TMDB's `episode_run_time` aggregate is
      empty (increasingly common on newer shows), and displays the true min-max spread across all
      seasons (e.g. "40-53 min") rather than a single arbitrary episode's length.
- [x] Remove the upstream AWS/S3 image fallback (`data-remote/aws`, its Retrofit/OkHttp clients,
      `ImageSource.AWS`). That bucket is maintained by the Showly project, so the fork shouldn't
      depend on it. TMDB is now the only remote image source; posters still fall back to
      backdrops, and fanarts still fall back to an episode still.

### Rebranding

- [x] Rebranding and renaming package name (`com.michaldrabik.*` → `io.github.furaibo9714.diskette`,
      app name, user agent, DB filename, backup export prefix, launcher icon resource names,
      internal `Showly*` class/view names, docs)
- [x] New app icon
- [ ] Localization sweep (update strings across all supported languages via Crowdin)

### Floppy Integration Enhancements

- [ ] Offline mode & sync queuing (queue actions locally when Floppy is unreachable)
- [ ] Floppy server health/status indicator in settings or main feed
- [ ] New onboarding flow for connecting to a self-hosted Floppy instance
- [ ] Local notifications for upcoming episodes/movies based on Floppy data

### Tech Debt & App Modernization

- [ ] Jetpack Compose migration (gradual rewrite of UI screens)
- [ ] Edge-to-edge UI, transparent navigation bars, and predictive back gestures (Android 14+)

### Open Source Sustainability

- [ ] Add "Sponsor/Donate" links (GitHub Sponsors, Ko-fi, Patreon)

### Advanced Data Management

- [ ] Smart Lists & Advanced Filtering (build custom lists based on tags, rules, and scores)
- [ ] Data Export/Backup (export local watch history to CSV/JSON for easy Floppy server migrations)

### Next-Level UI Polish

- [ ] Material You (Dynamic Colors) support for Android 12+
- [ ] Shared Element Transitions for smoother navigation between lists and details
- [ ] Additional Widgets (e.g., mini-calendar, "Continue Watching")

### Distribution

- [x] Remove all Google Play Store surface (README badge/link, in-app "Rate on Play Store"
      settings row + helper, dead Play Store review-prompt/in-app-update strings)
- [x] Remove IzzyOnDroid distribution (README badge/link and the whole `fastlane/metadata` tree:
      store descriptions, listing icon/banner, phone screenshots). GitHub releases consumed via
      Obtainium are the only distribution channel.
- [x] Remove developer social-media promotion (Settings icons, "What's New" dialog button,
      README badges/links, and the Discover-feed "Follow us on Twitter" promo card feature —
      adapter/viewmodel/case/view/install-timestamp tracking all removed)
- [ ] Publish/track releases via [Obtainium](https://github.com/ImranR98/Obtainium)

## Project Setup

1. Clone repository and open project in the latest version of Android Studio.
2. Create a `keystore.properties` file and put it in the `/app` folder.
3. Add the following properties into the `keystore.properties` file (values are not important at this moment):

   ```ini
   keyAlias=github
   keyPassword=github
   storePassword=github
   ```

4. Add your [Trakt.tv](https://trakt.tv/oauth/applications), [TMDB](https://developers.themoviedb.org/3/) API keys as
   following properties into your `local.properties` file located in the root directory of the project:

   ```ini
   traktClientId="your trakt client id"
   traktClientSecret="your trakt client secret"
   tmdbApiKey="your tmdb api key (v4)"
   ```

5. Rebuild and start the app.

## Issues & Contributions

Feel free to post problems with the app as Github [Issues](https://github.com/furaibo9714/diskette/issues).

Features ideas should be posted as new Github [Discussion](https://github.com/furaibo9714/diskette/discussions).

Pull requests are welcome. Remember about leaving a comment in the relevant issue if you are working on something.

## Dev Notes

The codebase has been around for a few years now and it grew a bit rusty.
A few things surely could be addressed:

- Overall architecture should be simplified and refactored into a more strict feature-based one
- The single responsibility principle is broken and should be refactored in a few places like some of the Use Cases
- Retrofit could be replaced in favor of Ktor Client
- Jetpack Compose migration (although there is no **_real_** benefit of it currently from end-user point of view)
- Add more unit tests to complete the suite and increase coverage

## Translations

Diskette inherits its translations from Showly. Strings added by this fork are English-only for now.
Translation contributions are welcome via pull request until the translation process is fully established.

## Contact

Issues & discussions: https://github.com/furaibo9714/diskette
