# Testing Strategy

This plan tracks the work needed to reach good enough coverage for MinistryLogbook. Anything that has already been completed must be checked off, and future testing work should update these checkboxes as it lands.

## Coverage Goal

- [ ] Generate coverage reports locally and in CI.
- [ ] Reach 70-80% line coverage for non-UI Kotlin application logic.
- [ ] Prioritize branch coverage for date, time, report, goal, backup, and persistence rules.
- [ ] Exclude low-value generated or mostly declarative files from coverage gates.
- [ ] Treat every bug fix as requiring a regression test.
- [ ] Treat every new business rule as requiring a JVM unit test.
- [ ] Treat every Room schema migration as requiring a migration test.

## JVM Unit Tests

Use `app/src/test` for fast tests that cover business logic, view models, utilities, and isolated repository behavior.

- [x] Cover `HomeTimeCalculator` month, year, boundary, empty-data, and partial-data cases.
- [x] Cover `Time` arithmetic, formatting, zero values, and invalid or edge-case inputs.
- [x] Cover `LocalDateExtensions` month boundaries, leap years, and locale-sensitive behavior where relevant.
- [x] Cover `EntryExtensions` aggregation, filtering, sorting, and report-related calculations.
- [x] Cover `HomeViewModel` add, edit, delete, load, and state-update flows.
- [x] Cover `EntryDetailsViewModel` validation, save, delete, and navigation/state behavior.
- [x] Add `IntroViewModel` tests for onboarding state and setup completion.
- [x] Add `SettingsViewModel` tests for name, goal, role, reminder, and preference updates.
- [x] Add isolated repository tests with fake DAOs or fake services where Android APIs are not required.

## Persistence Tests

Use focused DAO, repository, and migration tests to protect stored user data.

- [x] Cover `EntryDao` insert, update, delete, range queries, and ordering.
- [x] Cover `BibleStudyDao` insert, update, delete, and active/inactive query behavior.
- [x] Cover `MonthlyInformationDao` insert, update, delete, and month-specific queries.
- [x] Cover `EntryRepository` integration with a real in-memory Room database.
- [x] Cover `BibleStudyRepository` integration with a real in-memory Room database.
- [x] Cover `MonthlyInformationRepository` integration with a real in-memory Room database.
- [x] Keep migration tests for every schema version step.
- [ ] Add migration tests whenever a schema file changes.

## Backup, Restore, And Android Services

Use instrumented tests for behavior that depends on Android APIs, storage, receivers, notifications, or URI handling.

- [x] Cover backup export with populated data.
- [x] Cover backup export with empty data.
- [x] Cover backup import from current-version data.
- [x] Cover backup import from older-version data when supported.
- [x] Cover malformed backup data and verify failure behavior is user-safe.
- [x] Cover backup round trips: export, clear database, import, verify restored records.
- [x] Cover `ReminderManager` scheduling, cancellation, and replacement behavior.
- [x] Cover receiver behavior where it changes reminders, backup state, or visible user behavior.
- [x] Cover URI and file-sharing behavior used by report or backup flows.

## Compose UI And Flow Tests

Use Compose instrumented tests for user-visible flows and critical interactions. Do not chase line coverage for every layout-only composable.

- [x] Cover app launch smoke behavior.
- [x] Cover onboarding flow from welcome through setup completion.
- [x] Cover adding a ministry entry from the home screen.
- [x] Cover editing an existing entry.
- [x] Cover deleting an existing entry.
- [x] Cover Bible study add, edit, and remove flows.
- [x] Cover settings updates for name and goal.
- [x] Cover the report/share screen rendering expected month totals.
- [x] Cover important accessibility labels, button enabled states, and click behavior for reusable components.
- [x] Avoid fragile visual assertions unless they protect a known regression-prone component.

## Coverage Tooling

- [x] Add a Kotlin coverage plugin, preferably Kover unless Android-specific JaCoCo reporting is required.
- [x] Add a local coverage task for JVM unit tests: `./gradlew :app:koverHtmlReportDebug` or `./gradlew :app:createDebugUnitTestCoverageReport`.
- [x] Add a local coverage task or documented command for instrumented coverage: `./gradlew :app:createDebugAndroidTestCoverageReport`.
- [ ] Add coverage report generation to CI.
- [ ] Publish coverage reports as CI artifacts.
- [ ] Fail CI when non-UI logic coverage drops below the agreed threshold.
- [x] Document the exact local commands for running unit tests, instrumented tests, and coverage reports.

## Suggested Coverage Exclusions

- [ ] Exclude `**/BuildConfig.*`.
- [ ] Exclude `**/R.*`.
- [ ] Exclude generated Room and KSP output.
- [ ] Exclude Compose preview-only code.
- [ ] Exclude `ui/theme` files unless they contain meaningful behavior.
- [ ] Exclude mostly declarative layout files from strict coverage gates.
- [ ] Exclude `MainActivity` unless it contains real application logic.

## CI Gate

- [ ] Run lint or static checks.
- [ ] Run JVM unit tests.
- [ ] Run instrumented tests on an emulator or managed device.
- [ ] Generate coverage reports.
- [ ] Enforce the agreed non-UI logic coverage threshold.
- [ ] Require new or changed business logic to include tests before merging.

## First High-Value Additions

- [x] Add or complete `SettingsViewModelTest`.
- [x] Add or complete `IntroViewModelTest`.
- [x] Add DAO tests for entries, Bible studies, and monthly information.
- [x] Add repository integration tests using an in-memory Room database.
- [x] Add malformed and older-data backup import tests.
- [x] Add one full Compose flow covering onboarding, entry creation, and report rendering.
