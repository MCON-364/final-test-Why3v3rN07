package edu.touro.las.mcon364.final_test;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Homework - Concurrent Auction Tracker (ConcurrentSkipListSet + ExecutorService)
 *
 * Scenario: an online auction platform receives bid submissions from many bidders
 * at the same time. The tracker must always reflect the current top bids in
 * sorted order (highest first) and must be safe when read and written concurrently.
 *
 * This exercise practises:
 * - ConcurrentSkipListSet as the thread-safe sorted cousin of TreeSet.
 * - Why TreeSet is NOT safe for concurrent access.
 * - ExecutorService and Runnable to simulate concurrent bid submissions.
 * - AtomicInteger for a safe submission counter.
 * - Stream operations to produce a ranked snapshot from the sorted set.
 *
 * Before coding, think about:
 * - What would happen if two threads called TreeSet.add() simultaneously?
 * - ConcurrentSkipListSet keeps elements sorted by compareTo.
 *   Look at BidEntry.compareTo: which bid appears first in iteration?
 * - Each BidEntry is unique by (bidderId, amount, timestampNanos).
 *   If a bidder submits a higher bid, does the lower one disappear automatically?
 *
 * Requirements:
 * - submitBid(entry) adds a BidEntry thread-safely and counts the submission.
 * - getTopN(n) returns the top n BidEntry objects as an immutable list, highest first.
 * - getTotalBids() returns the number of times submitBid has been called.
 * - runSimulation(bidders, bidsEach) uses an ExecutorService to have each bidder
 *   submit bidsEach random bids concurrently, then shuts down the pool and waits.
 *
 * Do not use synchronized blocks. Rely on ConcurrentSkipListSet and AtomicInteger.
 */
public class ConcurrentAuctionTracker {

    // BidEntry.compareTo sorts highest amount first
    private final ConcurrentSkipListSet<BidEntry> bids = new ConcurrentSkipListSet<>();
    private final AtomicInteger totalBids = new AtomicInteger(0);

    /**
     * Adds a bid entry to the tracker thread-safely and increments the counter.
     *
     * @param entry the bid entry to add
     */
    public void submitBid(BidEntry entry) {
        bids.add(entry);
        totalBids.incrementAndGet();
    }

    /**
     * Returns the top n bids as an immutable list, highest amount first.
     *
     * @param n number of top entries to return
     * @return immutable top-n list
     */
    public List<BidEntry> getTopN(int n) {
        return bids.stream()
                .limit(n)
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Returns how many times submitBid has been called since creation.
     */
    public int getTotalBids() {
        return totalBids.get();
    }

    /**
     * Simulates concurrent bid submissions using an ExecutorService.
     *
     * Each bidder in the list submits bidsEach random bids on a separate thread.
     * Wait for all threads to finish before returning.
     *
     * @param bidders   list of bidder identifiers
     * @param bidsEach  number of random bids each bidder submits
     */
    public void runSimulation(List<String> bidders, int bidsEach)
            throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(bidders.size());
        Random random = new Random();
        for (String bidder : bidders) {
            pool.submit(() -> {
                for (int i = 0; i < bidsEach; i++) {
                    int amount = random.nextInt(10_000); // random bid 0–9999 cents
                    submitBid(new BidEntry(bidder, amount, System.nanoTime()));
                }
            });
        }
        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);
    }
}

