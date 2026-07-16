package com.eatool.backend.applications;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.eatool.backend.common.BadRequestException;

/**
 * Faithful port of the Application validation/derivation rules from
 * src/catalog.js (normalizeName, normalizeLifecycleInput,
 * normalizeVerificationInput, normalizeBusinessFit, normalizeTechFit,
 * normalizePace, normalizeCriticality, normalizeDataHandling,
 * normalizeInformationStatus, normalizeAliases, deriveBusinessFitBand,
 * deriveTimeClassification). Keep this authoritative logic in sync with
 * catalog.js if either side changes.
 */
public final class ApplicationNormalizer {

    private static final Pattern ISO_DATE = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
    private static final Set<String> LIFECYCLE_STATUSES = Set.of("planned", "active", "retiring", "retired");
    private static final Set<String> TECH_FITS = Set.of("low", "medium", "high");
    private static final Set<String> CRITICALITIES = Set.of("low", "medium", "high");
    private static final Set<String> DATA_HANDLING_VALUES = Set.of("Yes", "No", "Unknown");
    private static final Set<String> PACE_VALUES =
            Set.of("System of Record", "System of Differentiation", "System of Innovation", "Unclassified");

    private ApplicationNormalizer() {
    }

    public static String normalizeText(String value) {
        return value == null ? "" : value.trim();
    }

    public static String requireName(String value, String message) {
        String name = normalizeText(value);
        if (name.isEmpty()) {
            throw new BadRequestException(message);
        }
        return name;
    }

    public static List<String> normalizeAliases(List<String> aliases) {
        List<String> normalized = new ArrayList<>();
        if (aliases == null) {
            return normalized;
        }
        for (String alias : aliases) {
            String value = normalizeText(alias);
            if (!value.isEmpty()) {
                normalized.add(value);
            }
        }
        return normalized;
    }

    public static String normalizeLifecycleStatus(String value) {
        String lifecycleStatus = normalizeText(value);
        if (lifecycleStatus.isEmpty()) {
            lifecycleStatus = "active";
        }
        if (!LIFECYCLE_STATUSES.contains(lifecycleStatus)) {
            throw new BadRequestException("Lifecycle Status must be planned, active, retiring, or retired.");
        }
        return lifecycleStatus;
    }

    public static int normalizeBusinessFit(Integer value) {
        if (value == null || value < 1 || value > 5) {
            throw new BadRequestException("Business Fit must be 1, 2, 3, 4, or 5.");
        }
        return value;
    }

    public static String normalizeTechFit(String value) {
        String techFit = normalizeText(value).toLowerCase();
        if (!TECH_FITS.contains(techFit)) {
            throw new BadRequestException("Tech Fit must be low, medium, or high.");
        }
        return techFit;
    }

    public static String normalizePace(String value) {
        String pace = normalizeText(value);
        if (pace.isEmpty()) {
            pace = "Unclassified";
        }
        String legacyMatch = matchLegacyPace(pace);
        String normalized = legacyMatch != null ? legacyMatch : pace;
        if (!PACE_VALUES.contains(normalized)) {
            throw new BadRequestException(
                    "PACE Classification must be System of Record, System of Differentiation, System of Innovation, or Unclassified.");
        }
        return normalized;
    }

    private static String matchLegacyPace(String pace) {
        String legacy = pace.toLowerCase();
        return switch (legacy) {
            case "system of record" -> "System of Record";
            case "system of differentiation" -> "System of Differentiation";
            case "system of innovation" -> "System of Innovation";
            case "unclassified" -> "Unclassified";
            default -> null;
        };
    }

    public static String normalizeCriticality(String value) {
        String criticality = normalizeText(value).toLowerCase();
        if (criticality.isEmpty()) {
            criticality = "medium";
        }
        if (!CRITICALITIES.contains(criticality)) {
            throw new BadRequestException("Criticality must be low, medium, or high.");
        }
        return criticality;
    }

    public static String normalizeDataHandling(String value, String label) {
        String dataHandling = normalizeText(value);
        if (dataHandling.isEmpty()) {
            dataHandling = "Unknown";
        }
        if (!DATA_HANDLING_VALUES.contains(dataHandling)) {
            throw new BadRequestException(label + " must be Yes, No, or Unknown.");
        }
        return dataHandling;
    }

    public static String normalizeInformationStatus(String value) {
        String informationStatus = normalizeText(value);
        if (informationStatus.isEmpty()) {
            informationStatus = "Draft";
        }
        String normalized = switch (informationStatus.toLowerCase()) {
            case "draft" -> "Draft";
            case "verified" -> "Verified";
            case "needs review" -> "Needs Review";
            default -> informationStatus;
        };
        if (!Set.of("Draft", "Verified", "Needs Review").contains(normalized)) {
            throw new BadRequestException("Information Status must be Draft, Verified, or Needs Review.");
        }
        return normalized;
    }

    public static String deriveBusinessFitBand(int businessFit) {
        if (businessFit <= 2) {
            return "low";
        }
        if (businessFit == 3) {
            return "medium";
        }
        return "high";
    }

    public static String deriveTimeClassification(String businessFitBand, String techFit) {
        return switch (businessFitBand + "/" + techFit) {
            case "high/high", "high/medium" -> "Invest";
            case "high/low", "medium/low" -> "Migrate";
            case "medium/high", "medium/medium" -> "Tolerate";
            default -> "Eliminate";
        };
    }

    public static boolean isValidIsoDate(String value) {
        if (!ISO_DATE.matcher(value).matches()) {
            return false;
        }
        try {
            LocalDate.parse(value);
            return true;
        } catch (DateTimeParseException exception) {
            return false;
        }
    }

    public record LifecycleInput(String lifecycleStatus, String plannedDate, String retirementDate) {
    }

    public static LifecycleInput normalizeLifecycleInput(String rawLifecycleStatus, String rawPlannedDate, String rawRetirementDate) {
        String lifecycleStatus = normalizeLifecycleStatus(rawLifecycleStatus);
        String plannedDate = normalizeText(rawPlannedDate);
        String retirementDate = normalizeText(rawRetirementDate);
        if (lifecycleStatus.equals("planned") && plannedDate.isEmpty()) {
            throw new BadRequestException("Planned Date is required for planned Applications.");
        }
        if ((lifecycleStatus.equals("retiring") || lifecycleStatus.equals("retired")) && retirementDate.isEmpty()) {
            throw new BadRequestException("Retirement Date is required for " + lifecycleStatus + " Applications.");
        }
        if (!plannedDate.isEmpty() && !isValidIsoDate(plannedDate)) {
            throw new BadRequestException("Planned Date must be a valid date.");
        }
        if (!retirementDate.isEmpty() && !isValidIsoDate(retirementDate)) {
            throw new BadRequestException("Retirement Date must be a valid date.");
        }
        return new LifecycleInput(lifecycleStatus, plannedDate, retirementDate);
    }

    public record VerificationInput(String informationStatus, String lastVerificationDate) {
    }

    public static VerificationInput normalizeVerificationInput(String rawInformationStatus, String rawLastVerificationDate) {
        String informationStatus = normalizeInformationStatus(rawInformationStatus);
        String lastVerificationDate = normalizeText(rawLastVerificationDate);
        if (informationStatus.equals("Verified") && lastVerificationDate.isEmpty()) {
            throw new BadRequestException("Last Verification Date is required for Verified Applications.");
        }
        if (!lastVerificationDate.isEmpty() && !isValidIsoDate(lastVerificationDate)) {
            throw new BadRequestException("Last Verification Date must be a valid date.");
        }
        return new VerificationInput(informationStatus, lastVerificationDate);
    }
}
