package com.klinker.android.messaging_sliding.scheduled;

import android.app.*;
import android.content.*;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemLongClickListener;
import com.klinker.android.messaging_donate.MainActivity;
import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_donate.settings.DrawerArrayAdapter;
import com.klinker.android.messaging_donate.settings.GetHelpSettingsActivity;
import com.klinker.android.messaging_donate.settings.OtherAppsSettingsActivity;
import com.klinker.android.messaging_donate.settings.SettingsPagerActivity;
import com.klinker.android.messaging_donate.utils.IOUtil;
import com.klinker.android.messaging_sliding.mass_text.MassTextActivity;
import com.klinker.android.messaging_sliding.scheduled.scheduled.ScheduledDataSource;
import com.klinker.android.messaging_sliding.scheduled.scheduled.ScheduledMessage;
import com.klinker.android.messaging_sliding.templates.TemplateActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class ScheduledSms extends Activity {

    public final static String EXTRA_NUMBER = "com.klinker.android.messaging_sliding.NUMBER";
    public final static String EXTRA_DATE = "com.klinker.android.messaging_sliding.DATE";
    public final static String EXTRA_REPEAT = "com.klinker.android.messaging_sliding.REPEAT";
    public final static String EXTRA_MESSAGE = "com.klinker.android.messaging_sliding.MESSAGE";

    public static Context context;
    public ListView sms;
    public Button addNew;
    public SharedPreferences sharedPrefs;
    public ArrayList<ScheduledMessage> text;

    private String[] linkItems;
    private String[] otherItems;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private LinearLayout mDrawer;
    private ActionBarDrawerToggle mDrawerToggle;

    private Activity activity;

    @Override
    protected void onResume() {
        super.onResume();
        text = IOUtil.readScheduled(this);

        SchedulesArrayAdapter adapter = new SchedulesArrayAdapter(this, text);
        sms.setAdapter(adapter);
        sms.setStackFromBottom(false);
    }

    @Override
    public void onStop() {
        super.onStop();
        ScheduledService.scheduleNextAlarm(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scheduled_sms);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(5);

        sms = (ListView) findViewById(R.id.smsListView);
        addNew = (Button) findViewById(R.id.addNewButton);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        context = this;

        text = IOUtil.readScheduled(this);

        SchedulesArrayAdapter adapter = new SchedulesArrayAdapter(this, text);
        sms.setAdapter(adapter);
        sms.setStackFromBottom(false);

        if (sharedPrefs.getBoolean("override_lang", false)) {
            String languageToLoad = "en";
            Locale locale = new Locale(languageToLoad);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        } else {
            String languageToLoad = Resources.getSystem().getConfiguration().locale.getLanguage();
            Locale locale = new Locale(languageToLoad);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }

        sms.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           final int arg2, long arg3) {
                new AlertDialog.Builder(context)
                        .setMessage(context.getResources().getString(R.string.delete_scheduled_sms))
                        .setPositiveButton(context.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                ScheduledDataSource dataSource = new ScheduledDataSource(context);
                                dataSource.open();
                                dataSource.deleteMessage(text.get(arg2));
                                dataSource.close();

                                onResume();
                            }
                        }).setNegativeButton(context.getResources().getString(R.string.cancel), null).show();
                return true;
            }
        });

        sms.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    final int pos, long arg3) {
                Intent intent = new Intent(context, NewScheduledSms.class);
                intent.putExtra(EXTRA_NUMBER, text.get(pos).address);
                intent.putExtra(EXTRA_DATE, text.get(pos).date);
                intent.putExtra(EXTRA_REPEAT, text.get(pos).repetition);
                intent.putExtra(EXTRA_MESSAGE, text.get(pos).body);

                startActivity(intent);
                overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
            }
        });

        addNew.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(context, NewScheduledSms.class);

                intent.putExtra(EXTRA_NUMBER, "");
                intent.putExtra(EXTRA_DATE, "");
                intent.putExtra(EXTRA_REPEAT, "0");
                intent.putExtra(EXTRA_MESSAGE, "");

                startActivity(intent);
                overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
            }

        });
        linkItems = new String[]{getResources().getString(R.string.theme_settings),
                getResources().getString(R.string.notification_settings),
                getResources().getString(R.string.popup_settings),
                getResources().getString(R.string.slideover_settings),
                getResources().getString(R.string.text_settings),
                getResources().getString(R.string.conversation_settings),
                getResources().getString(R.string.mms_settings),
                getResources().getString(R.string.google_voice_settings),
                getResources().getString(R.string.security_settings),
                getResources().getString(R.string.advanced_settings)};

        otherItems = new String[]{getResources().getString(R.string.quick_templates),
                getResources().getString(R.string.scheduled_sms),
                getResources().getString(R.string.mass_sms),
                getResources().getString(R.string.get_help),
                getResources().getString(R.string.other_apps),
                getResources().getString(R.string.rate_it)};

        DrawerArrayAdapter.current = 1;
        SettingsPagerActivity.settingsLinksActive = false;
        SettingsPagerActivity.inOtherLinks = true;

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.links_list);
        mDrawer = (LinearLayout) findViewById(R.id.drawer);

        Spinner spinner = (Spinner) findViewById(R.id.spinner);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,
                R.array.drawer_spinner_array, R.layout.drawer_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinner.setAdapter(adapter2);

        spinner.setSelection(1);

        spinner.setOnItemSelectedListener(new SpinnerClickListener());

        // Set the adapter for the list view
        mDrawerList.setAdapter(new DrawerArrayAdapter(this,
                new ArrayList<String>(Arrays.asList(otherItems))));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        );

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        activity = this;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private class SpinnerClickListener implements Spinner.OnItemSelectedListener {
        @Override
        // sets the string repetition to whatever is choosen from the spinner
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {
            // An item was selected. You can retrieve the selected item using
            // parent.getItemAtPosition(pos)
            String selected = parent.getItemAtPosition(pos).toString();

            if (selected.equals(getResources().getStringArray(R.array.drawer_spinner_array)[0])) {
                mDrawerList.setAdapter(new DrawerArrayAdapter(activity,
                        new ArrayList<String>(Arrays.asList(linkItems))));
                mDrawerList.invalidate();
                SettingsPagerActivity.settingsLinksActive = true;
            } else {
                mDrawerList.setAdapter(new DrawerArrayAdapter(activity,
                        new ArrayList<String>(Arrays.asList(otherItems))));
                mDrawerList.invalidate();
                SettingsPagerActivity.settingsLinksActive = false;
            }


        }

        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {

            // TODO: Make this smoother
            // TODO: Add the other settings options for not switching viewpager
            final Context context = getApplicationContext();
            final Intent intent;

            if (SettingsPagerActivity.settingsLinksActive) {
                mDrawerLayout.closeDrawer(mDrawer);

                final int mPositition = position;

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        Intent mIntent = new Intent(context, SettingsPagerActivity.class);
                        mIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        mIntent.putExtra("page_number", mPositition);
                        startActivity(mIntent);
                        overridePendingTransition(0, 0);
                    }
                }, 200);
            } else {
                mDrawerLayout.closeDrawer(mDrawer);

                switch (position) {
                    case 0:
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent mIntent = new Intent(context, TemplateActivity.class);
                                mIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(mIntent);
                                overridePendingTransition(0, 0);
                                //overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
                            }
                        }, 200);
                        break;

                    case 1:
                        /*
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent mIntent = new Intent(context, ScheduledSms.class);
                                mIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(mIntent);
                                overridePendingTransition(0,0);
                                //overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
                            }
                        }, 100);*/
                        break;

                    case 2:
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent mIntent = new Intent(context, MassTextActivity.class);
                                mIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(mIntent);
                                //overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
                                overridePendingTransition(0, 0);
                            }
                        }, 200);

                        break;

                    case 3:
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent mIntent = new Intent(context, GetHelpSettingsActivity.class);
                                mIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                startActivity(mIntent);
                                overridePendingTransition(0, 0);
                                //overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
                            }
                        }, 200);

                        break;

                    case 4:
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent mIntent = new Intent(context, OtherAppsSettingsActivity.class);
                                mIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                startActivity(mIntent);
                                overridePendingTransition(0, 0);
                                //overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
                            }
                        }, 200);

                        break;

                    case 5:
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
                                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);

                                try {
                                    startActivity(goToMarket);
                                } catch (ActivityNotFoundException e) {
                                    Toast.makeText(context, "Couldn't launch the market", Toast.LENGTH_SHORT).show();
                                }
                                overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
                            }
                        }, 200);

                        //overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
                        //mDrawerLayout.closeDrawer(mDrawer);
                        break;
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Pass the event to ActionBarDrawerToggle, if it returns
                // true, then it has handled the app icon touch event
                if (mDrawerToggle.onOptionsItemSelected(item)) {

                    return true;
                }

                // Handle your other action bar items...

                return super.onOptionsItemSelected(item);

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
        overridePendingTransition(R.anim.activity_slide_in_left, R.anim.activity_slide_out_right);
    }
}