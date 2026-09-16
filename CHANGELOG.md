# Compose Recomposition Auditor Changelog

## [Unreleased]

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