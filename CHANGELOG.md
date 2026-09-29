# Compose Recomposition Auditor Changelog

## [Unreleased]

## [2026.1.2] - 2026-09-29
### Fixed
- Added missing `optional="true"` attribute to the product-descriptor, required by JetBrains Marketplace for Freemium plugins

### Added
- Freemium pricing model: plugin is now Freemium on JetBrains Marketplace, with all existing inspections and quick-fixes remaining free forever
- **Export Stability Report** (Premium, 30-day trial): new action under Tools menu that scans the whole project and exports all detected issues (unstable parameters and lambda captures) as an HTML report and a JSON file
- `LicensingUtil`: license verification via the platform's `LicensingFacade`, gating the export feature behind an active Premium license or trial
- Refactored inspection logic into standalone `ComposableStabilityAnalyzer` and `LambdaCaptureAnalyzer`, shared between the live inspections and the batch export, guaranteeing identical detection behavior in both
- Progress bar (cancellable) shown during export on larger projects, running off the EDT to avoid freezing the IDE
- `StabilityReportCollectorTest`: integration tests covering the export pipeline end-to-end, mirroring the existing inspection test cases

### Known limitations
- Export currently reports the same two issue types as the live inspections (unstable parameters, unstable lambda captures); it does not yet aggregate historical data or compare across commits/branches


## [0.1.0] - 2026-09-19
### Added
- `UnstableLambdaCaptureInspection`: flags lambdas inside `@Composable` functions that capture a local `var` without `remember`, since Compose can't track changes to a plain captured variable
- Both inspections now skip `@Preview`-annotated composables (and multipreview variants like `@PreviewScreenSizes`), since preview functions commonly use unoptimized mock data on purpose
- Integration tests for both inspections using `BasePlatformTestCase`, covering the stability rules, the @Preview exclusion, and the remember-capture logic end-to-end
- False-positive reduction: a small whitelist of known-stable external Compose/coroutines types (`Modifier`, `State`, `MutableState`, `StateFlow`, `SharedFlow`, etc.) that are treated as stable even without a resolvable `@Stable`/`@Immutable` annotation

### Known limitations
- `UnstableLambdaCaptureInspection` only covers local `var`s declared inside the composable function itself — captures of class properties, top-level `var`s, or `var` function parameters are not yet detected
- Only `by remember { ... }` is recognized as a safe delegate for local `var`s. Recognizing `rememberUpdatedState` here is a non-goal: it returns a read-only `State<T>` and cannot delegate a `var`, so it doesn't apply to this inspection by construction
- One warning is reported per captured variable name per lambda, even if referenced multiple times inside it

## [0.0.1]
### Added
- Initial scaffold from the IntelliJ Platform Plugin Template, targeting Android Studio (Quail 4 | 2026.1.4)
- `StabilityInferencer`: pure-Kotlin stability inference engine reproducing (a conservative subset of) the Compose compiler's rules, fully unit-tested
- `TypeDescriptorExtractor`: bridges real Kotlin types (via the K2 Analysis API) to the stability model
- `UnstableComposableParameterInspection`: flags `@Composable` function parameters with unstable or unknown-stability types
- Quick-fix: convert `List`/`Map`/`Set` parameters to their `kotlinx.collections.immutable` equivalents, adding the import and Gradle dependency automatically
- Quick-fix: annotate a class with `@Stable` when it has a `var` property causing instability

[Unreleased]: https://github.com/Godark14/compose-recomposition-auditor/compare/v2026.1.2...HEAD
[2026.1.2]: https://github.com/Godark14/compose-recomposition-auditor/compare/v0.1.0...v2026.1.2
[0.1.0]: https://github.com/Godark14/compose-recomposition-auditor/compare/v0.0.1...v0.1.0
[0.0.1]: https://github.com/Godark14/compose-recomposition-auditor/commits/v0.0.1