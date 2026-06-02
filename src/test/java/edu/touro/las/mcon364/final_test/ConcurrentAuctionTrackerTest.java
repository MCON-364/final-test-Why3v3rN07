package edu.touro.las.mcon364.final_test;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrentAuctionTrackerTest {

    // ── helpers ───────────────────────────────────────────────────────────────

    private BidEntry bid(String bidder, int amount) {
        return new BidEntry(bidder, amount, System.nanoTime());
    }

    // ── submitBid / getTotalBids ──────────────────────────────────────────────

    @Test
    void totalBidsIsZeroInitially() {
        assertEquals(0, new ConcurrentAuctionTracker().getTotalBids());
    }

    @Test
    void submitBidIncrementsTotalBids() {
        ConcurrentAuctionTracker tracker = new ConcurrentAuctionTracker();
        tracker.submitBid(bid("Alice", 500));
        tracker.submitBid(bid("Bob", 700));
        assertEquals(2, tracker.getTotalBids());
    }

    // ── getTopN ───────────────────────────────────────────────────────────────

    @Test
    void getTopNWhenEmptyReturnsEmptyList() {
        assertTrue(new ConcurrentAuctionTracker().getTopN(5).isEmpty());
    }

    @Test
    void getTopNWithZeroReturnsEmptyList() {
        ConcurrentAuctionTracker tracker = new ConcurrentAuctionTracker();
        tracker.submitBid(bid("Alice", 100));
        assertTrue(tracker.getTopN(0).isEmpty());
    }

    @Test
    void getTopNReturnsHighestBidsFirst() {
        ConcurrentAuctionTracker tracker = new ConcurrentAuctionTracker();
        tracker.submitBid(bid("Alice", 300));
        tracker.submitBid(bid("Bob",   700));
        tracker.submitBid(bid("Carol", 500));
        List<BidEntry> top = tracker.getTopN(2);
        assertEquals(2, top.size());
        assertEquals(700, top.get(0).amount());
        assertEquals(500, top.get(1).amount());
    }

    @Test
    void getTopNWithNLargerThanSizeReturnsAll() {
        ConcurrentAuctionTracker tracker = new ConcurrentAuctionTracker();
        tracker.submitBid(bid("Alice", 100));
        tracker.submitBid(bid("Bob",   200));
        assertEquals(2, tracker.getTopN(100).size());
    }

    @Test
    void getTopNIsImmutable() {
        ConcurrentAuctionTracker tracker = new ConcurrentAuctionTracker();
        tracker.submitBid(bid("Alice", 100));
        List<BidEntry> top = tracker.getTopN(1);
        assertThrows(Exception.class, top::clear);
    }

    // ── runSimulation — concurrent correctness ────────────────────────────────

    @Test
    void runSimulationSubmitsCorrectTotalCount() throws InterruptedException {
        ConcurrentAuctionTracker tracker = new ConcurrentAuctionTracker();
        List<String> bidders = List.of("Alice", "Bob", "Carol", "Dan");
        int bidsEach = 50;
        tracker.runSimulation(bidders, bidsEach);
        assertEquals(bidders.size() * bidsEach, tracker.getTotalBids());
    }

    @Test
    void runSimulationWithSingleBidderStillWorks() throws InterruptedException {
        ConcurrentAuctionTracker tracker = new ConcurrentAuctionTracker();
        tracker.runSimulation(List.of("Solo"), 10);
        assertEquals(10, tracker.getTotalBids());
    }
}

