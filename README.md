# Compose Recomposition Auditor

![Build](https://img.shields.io/badge/build-passing-brightgreen)

<!-- Plugin description -->
Detects Jetpack Compose recomposition issues directly in the editor — before you
ever run a profiler. The plugin reproduces (a conservative subset of) the Compose
compiler's own stability inference to flag `@Composable` function parameters that
are likely to cause unnecessary recompositions, and detects lambda captures that
defeat smart recomposition entirely.

**Detected issues:**
- Mutable collection interfaces (`List`, `Map`, `Set`) passed as `@Composable` parameters
- Classes with `var` properties passed as `@Composable` parameters, without `@Stable`/`@Immutable`
- Types defined outside the current module whose stability cannot be determined
- Lambdas inside a `@Composable` function that capture a local `var` without
  `remember` — Compose cannot observe changes to a plain captured variable

**Quick-fixes:**
- **Convert to Immutable\*** — rewrites `List<T>` / `Map<K,V>` / `Set<T>` to their
  `kotlinx.collections.immutable` equivalents, adding both the import and the
  Gradle dependency automatically if missing
- **Annotate class with @Stable** — adds the `@Stable` annotation to the offending
  class declaration

More recomposition-related inspections are planned.
<!-- Plugin description end -->

## Installation

Using the IDE built-in plugin system:

1. Open **Settings/Preferences > Plugins > Marketplace**
2. Search for "Compose Recomposition Auditor"
3. Click **Install**

Manually:

1. Download the [latest release](https://github.com/Godark14/compose-recomposition-auditor/releases/latest)
2. Open **Settings/Preferences > Plugins**, click the gear icon ⚙️, then **Install plugin from disk...**
3. Select the downloaded file

## How it works

The plugin models Kotlin types through a small, PSI-independent `TypeDescriptor`
representation, and infers stability against it using the same rules the Compose
compiler applies (primitives and functions are stable; mutable collection
interfaces and classes with `var` properties are unstable; external types with
no visible source are treated as unknown/unstable by default). This separation
keeps the core inference logic unit-testable without spinning up an IDE instance,
while a thin extraction layer bridges real Kotlin PSI (via the K2 Analysis API)
to that model for use in the live inspection.

Lambda capture detection works independently: it walks the body of each
`@Composable` function, resolves every name reference inside its lambdas via
standard Kotlin reference resolution, and flags references to local `var`s that
aren't delegated through `remember`.

---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template