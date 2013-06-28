/*
 * Copyright (C) 2012 Sven Dawitz for the CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.statusbar.toggles;

import android.app.AlertDialog;
import android.app.Profile;
import android.app.ProfileManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.android.server.ProfileManagerService;
import com.android.systemui.R;

import java.util.UUID;

public class ProfileToggle extends BaseToggle {
   
    Profile mChosenProfile;
    ProfileManager mProfileManager;
    ProfileReceiver mProfileReceiver;
	
	@Override
    public void init(Context c, int style) {
        super.init(c, style);
		mProfileReceiver = new ProfileReceiver();
        mProfileReceiver.registerSelf();
        mProfileManager = (ProfileManager) mContext.getSystemService(Context.PROFILE_SERVICE);
        update();
		scheduleViewUpdate();
    }
	
	@Override
    public void onClick(View v) {
        createProfileDialog();
        collapseStatusBar();
        dismissKeyguard();
    }

    @Override
    public boolean onLongClick(View v) {
		Intent intent = new Intent("android.settings.PROFILES_SETTINGS");
        collapseStatusBar();
        dismissKeyguard();
        startActivity(intent);
        return super.onLongClick(v);
    }

    protected void update() {
        setIcon(R.drawable.ic_qs_profiles);
        setLabel(mProfileManager.getActiveProfile().getName());
        super.updateView();
    }
	
	private void createProfileDialog() {
        final ProfileManager profileManager = (ProfileManager) mContext
                .getSystemService(Context.PROFILE_SERVICE);

        final Profile[] profiles = profileManager.getProfiles();
        UUID activeProfile = profileManager.getActiveProfile().getUuid();
        final CharSequence[] names = new CharSequence[profiles.length];

        int i = 0;
        int checkedItem = 0;

        for (Profile profile : profiles) {
            if (profile.getUuid().equals(activeProfile)) {
                checkedItem = i;
                mChosenProfile = profile;
            }
            names[i++] = profile.getName();
        }

        final AlertDialog.Builder ab = new AlertDialog.Builder(mContext);

        AlertDialog dialog = ab
                .setTitle(R.string.quick_settings_profile_label)
                .setSingleChoiceItems(names, checkedItem, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which < 0)
                            return;
                        mChosenProfile = profiles[which];
                    }
                })
                .setPositiveButton(com.android.internal.R.string.yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                profileManager.setActiveProfile(mChosenProfile.getUuid());
                            }
                        })
                .setNegativeButton(com.android.internal.R.string.no,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
        dialog.show();
    }
	
	private class ProfileReceiver extends BroadcastReceiver {
        private boolean mIsRegistered;

        public ProfileReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
			update();
        }

        private void registerSelf() {
            if (!mIsRegistered) {
                mIsRegistered = true;
                IntentFilter filter = new IntentFilter();
                filter.addAction(ProfileManagerService.INTENT_ACTION_PROFILE_SELECTED);
                mContext.registerReceiver(mProfileReceiver, filter);
            }
        }

        private void unregisterSelf() {
            if (mIsRegistered) {
                mIsRegistered = false;
                mContext.unregisterReceiver(this);
            }
        }
    }
}