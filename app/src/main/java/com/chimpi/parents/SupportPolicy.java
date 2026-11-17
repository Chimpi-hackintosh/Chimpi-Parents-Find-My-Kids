package com.chimpi.parents;

import java.time.Instant;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class SupportPolicy {
    static final String MINIMUM = "1.3.1-preview.1";
    static final String AVAILABLE = "v1.3.1-preview.1";
    static final Instant CUTOFF = Instant.parse("2026-11-04T00:00:00Z");
    private static final Pattern VERSION = Pattern.compile(
        "^v?(\\d+)\\.(\\d+)\\.(\\d+)(?:-([A-Za-z0-9.-]+))?(?:\\+[A-Za-z0-9.-]+)?$");

    private SupportPolicy() {}

    static boolean isUnsupported(String installed) {
        Version current = parse(installed);
        Version minimum = parse(MINIMUM);
        return current != null && minimum != null && compare(current, minimum) < 0;
    }

    static String language(String languageTag) {
        String value = languageTag == null ? "" : languageTag.replace('_', '-');
        int dash = value.indexOf('-');
        String code = (dash < 0 ? value : value.substring(0, dash)).toLowerCase(Locale.ROOT);
        return "pt".equals(code) ? "pt" : "en";
    }

    static Notice notice(String app, String installed, Instant now, String languageTag) {
        if (!isUnsupported(installed)) return null;
        boolean after = !now.isBefore(CUTOFF);
        boolean pt = "pt".equals(language(languageTag));
        String title;
        String body;
        if (pt) {
            title = after ? "Sua versão não recebe mais atualizações de segurança" : "Sua versão está prestes a não ser suportada";
            body = after
                ? "As versões antigas do " + app + " não recebem atualizações de segurança desde 4 de novembro de 2026 às 00h UTC"
                : "As versões antigas do " + app + " deixarão de receber atualizações de segurança em 4 de novembro de 2026 às 00h UTC";
            body += "\n\nSoftware sem correções de segurança pode ficar exposto a falhas — atualize pela página oficial do projeto"
                + "\n\nInstalada: " + installed + " · Mínima suportada: " + MINIMUM + " · Disponível: " + AVAILABLE
                + "\n\nEste aviso não afirma infecção e não instala nada automaticamente";
        } else {
            title = after ? "Your version no longer receives security updates" : "Your version is about to lose support";
            body = after
                ? "Older versions of " + app + " have not received security updates since 4 November 2026 at 00:00 UTC"
                : "Older versions of " + app + " will stop receiving security updates on 4 November 2026 at 00:00 UTC";
            body += "\n\nSoftware without security fixes may remain exposed to vulnerabilities — update from the official project page"
                + "\n\nInstalled: " + installed + " · Minimum supported: " + MINIMUM + " · Available: " + AVAILABLE
                + "\n\nThis notice does not claim an infection and does not install anything automatically";
        }
        return new Notice(title, body, after, pt);
    }

    private static Version parse(String value) {
        if (value == null || value.length() > 100) return null;
        Matcher matcher = VERSION.matcher(value);
        if (!matcher.matches()) return null;
        try {
            return new Version(
                Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3)),
                matcher.group(4));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static int compare(Version left, Version right) {
        int core = Integer.compare(left.major, right.major);
        if (core == 0) core = Integer.compare(left.minor, right.minor);
        if (core == 0) core = Integer.compare(left.patch, right.patch);
        if (core != 0) return core;
        if (left.pre == null && right.pre == null) return 0;
        if (left.pre == null) return 1;
        if (right.pre == null) return -1;
        String[] a = left.pre.split("\\.");
        String[] b = right.pre.split("\\.");
        for (int index = 0; index < Math.min(a.length, b.length); index++) {
            if (a[index].equals(b[index])) continue;
            boolean an = a[index].matches("\\d+");
            boolean bn = b[index].matches("\\d+");
            if (an && bn) return Long.compare(Long.parseLong(a[index]), Long.parseLong(b[index]));
            if (an != bn) return an ? -1 : 1;
            return a[index].compareTo(b[index]);
        }
        return Integer.compare(a.length, b.length);
    }

    private static final class Version {
        final int major;
        final int minor;
        final int patch;
        final String pre;

        Version(int major, int minor, int patch, String pre) {
            this.major = major;
            this.minor = minor;
            this.patch = patch;
            this.pre = pre;
        }
    }

    static final class Notice {
        final String title;
        final String body;
        final boolean afterCutoff;
        final boolean portuguese;

        Notice(String title, String body, boolean afterCutoff, boolean portuguese) {
            this.title = title;
            this.body = body;
            this.afterCutoff = afterCutoff;
            this.portuguese = portuguese;
        }
    }
}
