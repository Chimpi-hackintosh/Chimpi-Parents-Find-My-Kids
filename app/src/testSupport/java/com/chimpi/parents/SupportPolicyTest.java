package com.chimpi.parents;

import java.time.Instant;

public final class SupportPolicyTest {
    public static void main(String[] args) {
        check(SupportPolicy.isUnsupported("1.0.0-preview.1"), "old major feature version");
        check(SupportPolicy.isUnsupported("1.3.1-preview.0"), "older preview of same version");
        check(!SupportPolicy.isUnsupported("1.3.1-preview.1"), "minimum version");
        check(!SupportPolicy.isUnsupported("1.3.1"), "stable build of minimum version");
        check(!SupportPolicy.isUnsupported("1.4.0-preview.1"), "newer version");
        check(!SupportPolicy.isUnsupported("invalid"), "invalid version");
        check("pt".equals(SupportPolicy.language("pt-BR")), "Portuguese language");
        check("en".equals(SupportPolicy.language("fr-FR")), "English fallback");
        SupportPolicy.Notice before = SupportPolicy.notice("App", "1.2.0", Instant.parse("2026-11-03T23:59:59Z"), "pt-BR");
        SupportPolicy.Notice after = SupportPolicy.notice("App", "1.2.0", Instant.parse("2026-11-04T00:00:00Z"), "pt-BR");
        SupportPolicy.Notice fallback = SupportPolicy.notice("App", "1.2.0", Instant.parse("2026-11-04T00:00:00Z"), "de-DE");
        check(before != null && !before.afterCutoff && before.title.contains("prestes"), "before cutoff");
        check(after != null && after.afterCutoff && after.title.contains("segurança"), "after cutoff");
        check(fallback != null && !fallback.portuguese && fallback.title.startsWith("Your version"), "fallback text");
        System.out.println("PASS: semantic previews, old/current/new versions, exact UTC cutoff, Portuguese and English fallback");
    }

    private static void check(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
    }
}
