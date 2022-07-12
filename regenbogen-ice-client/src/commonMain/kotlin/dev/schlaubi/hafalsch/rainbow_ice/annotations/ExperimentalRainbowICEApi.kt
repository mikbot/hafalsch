package dev.schlaubi.hafalsch.rainbow_ice.annotations

/**
 * Annotation marking experimental rainbow ice apis.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION)
@MustBeDocumented
@RequiresOptIn
public annotation class ExperimentalRainbowICEApi
