package com.klinker.android.messaging_sliding.slide_over;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.klinker.android.messaging_donate.R;

public class SlideOverSettings  extends PreferenceActivity {

    public static Context context;
    public SharedPreferences sharedPrefs;

    public boolean showAll;

    private boolean enabled;
    private boolean haptic;
    private boolean close;
    private String secAction;
    private String side;
    private int sliver;
    private int vertical;
    private int activation;
    private int breakPoint;
    private int speed;
    private int padding;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.slideover_settings);
        setTitle(R.string.slide_over);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        getOriginal();

        showAll = sharedPrefs.getBoolean("show_advanced_settings", false);

        // Inflate a "Done/Discard" custom action bar view.
        LayoutInflater inflater = (LayoutInflater) getActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View customActionBarView = inflater.inflate(
                R.layout.actionbar_custom_view_done_discard, null);
        customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        doneClick();
                        finish(); // TODO: don't just finish()!
                    }
                });
        customActionBarView.findViewById(R.id.actionbar_discard).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        discardClick();
                        finish(); // TODO: don't just finish()!
                    }
                });

        // Show the custom action bar view and hide the normal Home icon and title.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(
                ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                        | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setCustomView(customActionBarView, new ActionBar.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        Preference slideOver = findPreference("slideover_enabled");
        slideOver.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                restartHalo();
                return false;
            }
        });

        Preference side = findPreference("slideover_side");
        side.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                restartHalo();
                return true;
            }
        });

        Preference sliver = findPreference("slideover_sliver");
        sliver.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                restartHalo();
                return true;
            }
        });

        Preference alignment = findPreference("slideover_vertical");
        alignment.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                restartHalo();
                return true;
            }
        });

        Preference activation = findPreference("slideover_activation");
        activation.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                restartHalo();
                return true;
            }
        });

        Preference breakPoint = findPreference("slideover_break_point");
        breakPoint.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                restartHalo();
                return true;
            }
        });

        Preference haptic = findPreference("slideover_haptic_feedback");
        haptic.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                restartHalo();
                return true;
            }
        });

        if (!showAll) {
            getPreferenceScreen().removePreference(findPreference("slideover_break_point"));
            getPreferenceScreen().removePreference(findPreference("slideover_secondary_action"));
            getPreferenceScreen().removePreference(findPreference("slideover_haptic_feedback"));
        }
    }

    public void restartHalo() {
        Intent service = new Intent();
        service.setAction("com.klinker.android.messaging.STOP_HALO");
        sendBroadcast(service);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (sharedPrefs.getBoolean("slideover_enabled", false)) {
                    Intent service = new Intent(getApplicationContext(), com.klinker.android.messaging_sliding.slide_over.SlideOverService.class);
                    startService(service);
                }
            }
        }, 500);
    }

    public boolean doneClick()
    {
        finish();
        return true;
    }

    public boolean discardClick()
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("slideover_enabled", enabled);
        editor.putString("slideover_side", side);
        editor.putInt("slideover_sliver", sliver);
        editor.putInt("slideover_vertical", vertical);
        editor.putInt("slideover_activation", activation);
        editor.putInt("slideover_break_point", breakPoint);
        editor.putBoolean("slideover_haptic_feedback", haptic);
        editor.putInt("slideover_animation_speed", speed);
        editor.putString("slideover_secondary_action", secAction);
        editor.putBoolean("full_app_popup_close", close);
        editor.putInt("slideover_padding", padding);
        editor.commit();

        restartHalo();

        finish();
        return true;
    }

    public void getOriginal()
    {
        enabled = sharedPrefs.getBoolean("slideover_enabled", false);
        haptic = sharedPrefs.getBoolean("slideover_haptic_feedback", true);
        close = sharedPrefs.getBoolean("full_app_popup_close", true);
        secAction = sharedPrefs.getString("slideover_secondary_action", "conversations");
        side = sharedPrefs.getString("slideover_side", "left");
        sliver = sharedPrefs.getInt("slideover_sliver", 33);
        vertical = sharedPrefs.getInt("slideover_vertical", 50);
        activation = sharedPrefs.getInt("slideover_activation", 33);
        breakPoint = sharedPrefs.getInt("slideover_break_point", 33);
        speed = sharedPrefs.getInt("slideover_animation_speed", 33);
        padding = sharedPrefs.getInt("slideover_padding", 50);
    }
}