package com.db.assetstore.domain.service.validation;

/**
 * Defines how attribute validation should behave for a given request.
 * <ul>
 *     <li>{@link #FULL} – enforce every configured constraint, including REQUIRED on
 *     attributes that are missing from the payload.</li>
 *     <li>{@link #PARTIAL} – skip REQUIRED constraints when an attribute is not provided
 *     (useful for PATCH flows) while still validating any supplied values.</li>
 *     <li>{@link #STRICT} – expresses an intent similar to {@code additionalProperties: false}
 *     by requiring callers to opt into strict schema enforcement. Behaviour currently matches
 *     {@link #FULL} but keeps the API explicit for flows that must reject unknown attributes.</li>
 * </ul>
 */
public enum ValidationMode {
    FULL(true, true),
    PARTIAL(false, true),
    STRICT(true, true);

    private final boolean enforceRequiredForMissing;
    private final boolean failOnUnknownAttributes;

    ValidationMode(boolean enforceRequiredForMissing,
                   boolean failOnUnknownAttributes) {
        this.enforceRequiredForMissing = enforceRequiredForMissing;
        this.failOnUnknownAttributes = failOnUnknownAttributes;
    }

    public boolean enforceRequiredForMissing() {
        return enforceRequiredForMissing;
    }

    public boolean failOnUnknownAttributes() {
        return failOnUnknownAttributes;
    }
}
