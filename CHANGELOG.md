# Compose Recomposition Auditor Changelog

## [Unreleased]
### Added
- `UnstableLambdaCaptureInspection`: flags lambdas inside `@Composable` functions that capture a local `var` without `remember`, since Compose can't track changes to a plain captured variable

### Known limitations
- `UnstableLambdaCaptureInspection` only covers local `var`s declared inside the composable function itself — captures of class properties, top-level `var`s, or `var` function parameters are not yet detected
- Only `by remember { ... }` is recognized as a safe delegate; `rememberUpdatedState` is not yet recognized and will still be flagged
- One warning is reported per captured variable name per lambda, even if referenced multiple times inside it

## [0.0.1]
### Added
- Initial scaffold from the IntelliJ Platform Plugin Template, targeting Android Studio (Quail 4 | 2026.1.4)
- `StabilityInferencer`: pure-Kotlin stability inference engine reproducing (a conservative subset of) the Compose compiler's rules, fully unit-tested
- `TypeDescriptorExtractor`: bridges real Kotlin types (via the K2 Analysis API) to the stability model
- `UnstableComposableParameterInspection`: flags `@Composable` function parameters with unstable or unknown-stability types
- Quick-fix: convert `List`/`Map`/`Set` parameters to their `kotlinx.collections.immutable` equivalents, adding the import and Gradle dependency automatically
- Quick-fix: annotate a class with `@Stable` when it has a `var` property causing instability

[Unreleased]: https://github.com/Godark14/compose-recomposition-auditor/compare/v0.0.1...HEAD
[0.0.1]: https://github.com/Godark14/compose-recomposition-auditor/commits/v0.0.1