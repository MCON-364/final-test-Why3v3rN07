package edu.touro.las.mcon364.final_test;

/**
 * A single bid submitted to an auction tracker.
 *
 * @param bidderId       identifier of the bidder
 * @param amount         bid amount in whole cents (higher is better)
 * @param timestampNanos nanosecond timestamp when the bid was recorded
 */
public record BidEntry(String bidderId, int amount, long timestampNanos)
        implements Comparable<BidEntry> {

    /**
     * Natural ordering: descending by amount (highest first),
     * then ascending by bidderId as a stable tie-break,
     * then ascending by timestampNanos to keep every entry unique in the set.
     */
    @Override
    public int compareTo(BidEntry other) {
        int c = Integer.compare(other.amount, this.amount); // descending
        if (c != 0) return c;
        c = this.bidderId.compareTo(other.bidderId);
        if (c != 0) return c;
        return Long.compare(this.timestampNanos, other.timestampNanos);
    }
}

