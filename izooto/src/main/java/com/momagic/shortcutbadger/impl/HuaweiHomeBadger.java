package com.momagic.shortcutbadger.impl;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import com.momagic.shortcutbadger.Badger;
import com.momagic.shortcutbadger.ShortcutBadgerException;

import java.util.Arrays;
import java.util.List;

public class HuaweiHomeBadger implements Badger {

    @Override
    @SuppressWarnings("NewApi")
    @SuppressLint("NewApi")
    public void executeBadge(Context context, ComponentName componentName, int badgeCount) throws ShortcutBadgerException {
        Bundle localBundle = new Bundle();
        localBundle.putString("package", context.getPackageName());
        localBundle.putString("class", componentName.getClassName());
        localBundle.putInt("badgenumber", badgeCount);
        context.getContentResolver().call(Uri.parse("content://com.huawei.android.launcher.settings/badge/"), "change_badge", null, localBundle);
    }

    @Override
    public List<String> getSupportLaunchers() {
        return Arrays.asList(
                "com.huawei.android.launcher"
        );
    }
}
