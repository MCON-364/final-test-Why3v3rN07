package edu.touro.las.mcon364.final_test;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

class ProductReviewAnalyzerTest {

    // ── helpers ───────────────────────────────────────────────────────────────

    /** Flat list: "electronics" x3, "books" x2, "clothing" x1, "audio" x1 */
    private List<String> sampleTags() {
        return List.of(
                "electronics", "books", "electronics", "clothing",
                "electronics", "books", "audio"
        );
    }

    // ── constructor ───────────────────────────────────────────────────────────

    @Test
    void constructorThrowsOnNullInput() {
        assertThrows(Exception.class, () -> new ProductReviewAnalyzer(null));
    }

    @Test
    void constructorDefendsAgainstExternalMutation() {
        List<String> tags = new ArrayList<>(List.of("books", "electronics"));
        ProductReviewAnalyzer analyzer = new ProductReviewAnalyzer(tags);
        tags.clear();
        // analyzer should still see 2 distinct categories
        assertEquals(2, analyzer.buildCategoryFrequencyMap().size());
    }

    // ── buildCategoryFrequencyMap ─────────────────────────────────────────────

    @Test
    void frequencyMapIsEmptyForEmptyInput() {
        assertTrue(new ProductReviewAnalyzer(List.of()).buildCategoryFrequencyMap().isEmpty());
    }

    @Test
    void frequencyMapCountsCorrectly() {
        TreeMap<String, Long> map = new ProductReviewAnalyzer(sampleTags()).buildCategoryFrequencyMap();
        assertEquals(3L, map.get("electronics"));
        assertEquals(2L, map.get("books"));
        assertEquals(1L, map.get("clothing"));
        assertEquals(1L, map.get("audio"));
    }

    @Test
    void frequencyMapIsSortedAlphabetically() {
        TreeMap<String, Long> map = new ProductReviewAnalyzer(sampleTags()).buildCategoryFrequencyMap();
        List<String> keys = new ArrayList<>(map.keySet());
        assertEquals("audio",       keys.get(0));
        assertEquals("books",       keys.get(1));
        assertEquals("clothing",    keys.get(2));
        assertEquals("electronics", keys.get(3));
    }

    @Test
    void frequencyMapReturnTypeIsTreeMap() {
        assertTrue(new ProductReviewAnalyzer(sampleTags()).buildCategoryFrequencyMap()
                instanceof TreeMap);
    }

    // ── getTopNCategories ─────────────────────────────────────────────────────

    @Test
    void topNReturnsHighestCountFirst() {
        List<String> top = new ProductReviewAnalyzer(sampleTags()).getTopNCategories(2);
        assertEquals("electronics", top.get(0));
        assertEquals("books",       top.get(1));
    }

    @Test
    void topNWithNLargerThanSizeReturnsAll() {
        List<String> top = new ProductReviewAnalyzer(sampleTags()).getTopNCategories(100);
        assertEquals(4, top.size());
    }

    @Test
    void topNWithZeroReturnsEmptyList() {
        assertTrue(new ProductReviewAnalyzer(sampleTags()).getTopNCategories(0).isEmpty());
    }

    @Test
    void topNOnEmptyInputReturnsEmptyList() {
        assertTrue(new ProductReviewAnalyzer(List.of()).getTopNCategories(3).isEmpty());
    }

    // ── getCategoriesStartingWith ─────────────────────────────────────────────

    @Test
    void categoriesStartingWithReturnsCorrectEntries() {
        List<String> result = new ProductReviewAnalyzer(sampleTags()).getCategoriesStartingWith('b');
        assertEquals(List.of("books"), result);
    }

    @Test
    void categoriesStartingWithReturnsSortedList() {
        List<String> tags = List.of("cameras", "clothing", "books", "computing", "audio");
        List<String> result = new ProductReviewAnalyzer(tags).getCategoriesStartingWith('c');
        assertEquals(List.of("cameras", "clothing", "computing"), result);
    }

    @Test
    void categoriesStartingWithReturnsEmptyWhenNoMatch() {
        List<String> result = new ProductReviewAnalyzer(sampleTags()).getCategoriesStartingWith('z');
        assertTrue(result.isEmpty());
    }

    @Test
    void categoriesStartingWithDoesNotIncludeOtherLetters() {
        List<String> result = new ProductReviewAnalyzer(sampleTags()).getCategoriesStartingWith('e');
        assertTrue(result.stream().allMatch(s -> s.startsWith("e")));
    }

    // ── getMostReviewedInRange ────────────────────────────────────────────────

    @Test
    void mostReviewedInRangeReturnsCorrectCategory() {
        // range "books" to "clothing" includes "books"(2) and "clothing"(1)
        Optional<String> result = new ProductReviewAnalyzer(sampleTags())
                .getMostReviewedInRange("books", "clothing");
        assertEquals(Optional.of("books"), result);
    }

    @Test
    void mostReviewedInRangeIsInclusiveOnBothEnds() {
        Optional<String> result = new ProductReviewAnalyzer(sampleTags())
                .getMostReviewedInRange("electronics", "electronics");
        assertEquals(Optional.of("electronics"), result);
    }

    @Test
    void mostReviewedInRangeReturnsEmptyWhenRangeHasNoCategories() {
        Optional<String> result = new ProductReviewAnalyzer(sampleTags())
                .getMostReviewedInRange("ma", "mz");
        assertEquals(Optional.empty(), result);
    }

    @Test
    void mostReviewedInRangeWithFullRangeReturnsOverallMost() {
        Optional<String> result = new ProductReviewAnalyzer(sampleTags())
                .getMostReviewedInRange("audio", "electronics");
        assertEquals(Optional.of("electronics"), result);
    }
}

