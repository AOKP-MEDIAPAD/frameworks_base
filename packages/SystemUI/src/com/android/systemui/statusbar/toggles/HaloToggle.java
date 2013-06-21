
package com.android.systemui.statusbar.toggles;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;;
import android.view.View;

import com.android.systemui.R;

public class HaloToggle extends StatefulToggle {

    @Override
    public void init(Context c, int style) {
        super.init(c, style);
        scheduleViewUpdate();
    }

    @Override
    protected void doEnable() {
        Settings.System.putInt(mContext.getContentResolver(),
                Settings.System.HALO_ACTIVE, 1);
    }

    @Override
    protected void doDisable() {
        Settings.System.putInt(mContext.getContentResolver(),
                Settings.System.HALO_ACTIVE, 0);
    }

    @Override
    public boolean onLongClick(View v) {
        Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        collapseStatusBar();
        dismissKeyguard();
        startActivity(intent);
        return super.onLongClick(v);
    }

    @Override
    protected void updateView() {
        boolean enabled = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.HALO_ACTIVE, 0) == 1;
        setEnabledState(enabled);
        setIcon(enabled ? R.drawable.ic_notify_halo_pressed : R.drawable.ic_notify_halo_normal);
        setLabel(enabled ? R.string.quick_settings_halo_on_label
                : R.string.quick_settings_halo_off_label);
        super.updateView();
    }

}
