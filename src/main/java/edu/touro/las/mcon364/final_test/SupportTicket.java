package edu.touro.las.mcon364.final_test;

/**
 * A completed or unresolved support ticket used for stream-based reporting.
 */
public record SupportTicket(
        int id,
        String category,
        Priority priority,
        int minutesToResolve,
        boolean resolved
) {
    public SupportTicket {
        if (id <= 0) {
            throw new IllegalArgumentException("id must be positive");
        }
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("category must be non-blank");
        }
        if (priority == null) {
            throw new IllegalArgumentException("priority must not be null");
        }
        if (minutesToResolve < 0) {
            throw new IllegalArgumentException("minutesToResolve must be non-negative");
        }
    }
}
