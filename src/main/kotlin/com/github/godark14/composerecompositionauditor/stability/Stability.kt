package com.github.godark14.composerecompositionauditor.stability

enum class Stability {
    STABLE,
    UNSTABLE,
    /** Not enough information to decide — treated as UNSTABLE by the
     * Compose compiler, but kept distinct here so the inspection can
     * word its warning differently ("unknown" vs "known unstable"). */
    UNKNOWN,
}