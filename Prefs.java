package com.advback.ledflashlight;

import android.os.Bundle;
import android.content.Context;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class Prefs extends PreferenceActivity {
	
	private static final String OPT_NOTIFY = "notify";
	private static final boolean OPT_NOTIFY_DEF = true;
	private static final String OPT_BLINK = "notify_blink";
	private static final boolean OPT_BLINK_DEF = true;
	private static final String OPT_TIMEOUT = "set_timeout";
	private static final boolean OPT_TIMEOUT_DEF = true;
	private static final String OPT_TIMER = "set_timer";
	private static final String OPT_TIMER_DEF = "5";
	private static final String OPT_BATT_THRESHOLD = "set_batt_threshold";
	private static final boolean OPT_BATT_THRESHOLD_DEF = true;
	private static final String OPT_THRESHOLD = "set_threshold";
	private static final String OPT_THRESHOLD_DEF = "50";
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
	}
	
	public static boolean getNotify(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context)
		.getBoolean(OPT_NOTIFY, OPT_NOTIFY_DEF);
	}

	public static boolean getBlink(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
		.getBoolean(OPT_BLINK, OPT_BLINK_DEF);
	}
	
	public static boolean getTimeout(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
		.getBoolean(OPT_TIMEOUT, OPT_TIMEOUT_DEF);
	}
	
	public static String getTimer(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
		.getString(OPT_TIMER, OPT_TIMER_DEF);
	}
	
	public static boolean getBattThreshold(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
		.getBoolean(OPT_BATT_THRESHOLD, OPT_BATT_THRESHOLD_DEF);
	}
	
	public static String getThreshold(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
		.getString(OPT_THRESHOLD, OPT_THRESHOLD_DEF);
	}
	
}
