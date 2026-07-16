package com.eatool.backend.applications;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Unit coverage of the TIME Classification derivation matrix ported from
 * src/catalog.js's deriveBusinessFitBand/deriveTimeClassification.
 */
class ApplicationNormalizerTests {

    @Test
    void deriveBusinessFitBandBucketsFitScores() {
        assertThat(ApplicationNormalizer.deriveBusinessFitBand(1)).isEqualTo("low");
        assertThat(ApplicationNormalizer.deriveBusinessFitBand(2)).isEqualTo("low");
        assertThat(ApplicationNormalizer.deriveBusinessFitBand(3)).isEqualTo("medium");
        assertThat(ApplicationNormalizer.deriveBusinessFitBand(4)).isEqualTo("high");
        assertThat(ApplicationNormalizer.deriveBusinessFitBand(5)).isEqualTo("high");
    }

    @Test
    void deriveTimeClassificationMatchesTheFullMatrix() {
        assertThat(ApplicationNormalizer.deriveTimeClassification("high", "high")).isEqualTo("Invest");
        assertThat(ApplicationNormalizer.deriveTimeClassification("high", "medium")).isEqualTo("Invest");
        assertThat(ApplicationNormalizer.deriveTimeClassification("high", "low")).isEqualTo("Migrate");
        assertThat(ApplicationNormalizer.deriveTimeClassification("medium", "high")).isEqualTo("Tolerate");
        assertThat(ApplicationNormalizer.deriveTimeClassification("medium", "medium")).isEqualTo("Tolerate");
        assertThat(ApplicationNormalizer.deriveTimeClassification("medium", "low")).isEqualTo("Migrate");
        assertThat(ApplicationNormalizer.deriveTimeClassification("low", "high")).isEqualTo("Eliminate");
        assertThat(ApplicationNormalizer.deriveTimeClassification("low", "medium")).isEqualTo("Eliminate");
        assertThat(ApplicationNormalizer.deriveTimeClassification("low", "low")).isEqualTo("Eliminate");
    }
}
