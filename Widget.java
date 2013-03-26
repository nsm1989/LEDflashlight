package com.advback.ledflashlight;

/**
 * The applicaiton widget for the LED Flashlight. When the widget is used
 * to toggle the light, the battery monitor, timer, and notifications are 
 * not used. The widget will simply toggle the light on or off. If there
 * threads running or notifications present from the main application, the widget
 * will kill them all on turning the light off.
 */
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class Widget extends AppWidgetProvider {

	private static final String ACTION_WIDGET_RECEIVER = "ActionRecieverWidget";
	private FlashLight f = new FlashLight();
	private FlashlightLED led;
	private String ns = Context.NOTIFICATION_SERVICE;
	private NotificationManager mNotificationManager;
	private RemoteViews views = new RemoteViews("com.advback.ledflashlight",
			R.layout.widget);

	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		final int N = appWidgetIds.length;

		// Perform this loop procedure for each App Widget that belongs to this
		// provider
		for (int i = 0; i < N; i++) {
			int appWidgetId = appWidgetIds[i];

			// Create an Intent that targets ourself, this will force a call to
			//onReceive()
			Intent intent = new Intent(context, Widget.class);
			intent.setAction(ACTION_WIDGET_RECEIVER);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
					0, intent, 0);

			// Get the layout for the App Widget and attach an on-click listener
			// to the button
			views.setOnClickPendingIntent(R.id.toggle_button_widget,
					pendingIntent);

			// Tell the AppWidgetManager to perform an update on the current App
			// Widget
			appWidgetManager.updateAppWidget(appWidgetId, views);
		}
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		try {
			led = new FlashlightLED();
			if (led.isEnabled()) {
				f.toggleLedWidget(led);
				mNotificationManager = (NotificationManager) context
						.getSystemService(ns);
				mNotificationManager.cancel(424206);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(ACTION_WIDGET_RECEIVER)) {
			try {
				led = new FlashlightLED();
				f.toggleLedWidget(led);
				//This toggles the image on the widget, seems a little cludgy,
				//but it works. Theres probably a better way to do it.
				if (!led.isEnabled()) {
					//if there is a notification hanging around, kill it
					mNotificationManager = (NotificationManager) context
							.getSystemService(ns);
					mNotificationManager.cancel(424206);
					views.setImageViewResource(R.id.toggle_button_widget,
							R.drawable.flashlight);
					ComponentName me = new ComponentName(context, Widget.class);
					AppWidgetManager.getInstance(context).updateAppWidget(me,
							views);
				}else{
					views.setImageViewResource(R.id.toggle_button_widget,
							R.drawable.flashlight_lit);
					ComponentName me = new ComponentName(context, Widget.class);
					AppWidgetManager.getInstance(context).updateAppWidget(me,
							views);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		super.onReceive(context, intent);
	}
}
