package com.chimpi.parents;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import java.time.Instant;
import java.util.Locale;

final class SupportNotice {
    private static final String APP = "Chimpi Parents Find My Kids";
    private static final String RELEASE = "https://github.com/Chimpi-hackintosh/Chimpi-Parents-Find-My-Kids/releases/tag/v1.1.0-preview.1";

    private SupportNotice() {}

    static void showIfRequired(Activity activity) {
        String installedVersion;
        try {
            installedVersion = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionName;
        } catch (Exception error) {
            return;
        }
        SupportPolicy.Notice notice = SupportPolicy.notice(
            APP, installedVersion, Instant.now(), Locale.getDefault().toLanguageTag());
        if (notice == null || activity.isFinishing()) return;
        AlertDialog dialog = new AlertDialog.Builder(activity)
            .setTitle(notice.title)
            .setMessage(notice.body)
            .setPositiveButton(notice.portuguese ? "Atualizar" : "Update", (ignored, which) -> {
                try {
                    activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(RELEASE)));
                } catch (RuntimeException error) {
                    // Keep the application usable when no browser can open the official page
                }
            })
            .setNegativeButton(notice.portuguese ? "Depois" : "Later", null)
            .create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
}
