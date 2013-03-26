package com.advback.ledflashlight;

/**
 * This is the main flashlight class for the application. It 
 * monitors the state of the application, and adjusts the LEDs
 * when necessary, either by a user event, or by one of the 
 * built in safeguards for the battery.
 * @author Jeremy Cerise
 * @version 1.4.5
 */

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

public class FlashLight extends Activity implements OnClickListener {

	private ImageButton toggle;

	private String ns = Context.NOTIFICATION_SERVICE;
	private NotificationManager mNotificationManager;
	private Notification notification;
	private int notificationId = 424206;

	private int totalTime;
	//these are just identifiers for the handlers
	protected static final int SECONDPASSEDIDENTIFIER = 0x1337;
	protected static final int TOGGLELEDIDENTIFIER = 0x1338;

	protected boolean running = false;
	protected int secondsPassed = 0;
	protected int secondsTotal;

	int level; // percentage, or -1 for unknown
	boolean lowBatt; //did we shut down due to low battery?
	private int lowBatteryThreshold; //the battery level to shut down at

	Thread countDownThread;

	//handles the low battery state, if enabled
	Handler threadHandler = new Handler() {
		// @Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case TOGGLELEDIDENTIFIER:
				if (lowBatt) {
					FlashlightLED led;
					try {
						led = new FlashlightLED();
						toggleLED(led);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			super.handleMessage(msg);
		}
	};

	//handles the timer, if enabled
	Handler timerUpdateHandler = new Handler() {
		// @Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SECONDPASSEDIDENTIFIER:
				if (running) {
					secondsPassed++;
					if (secondsPassed % 60 == 0) {
						totalTime = totalTime - 1;

						if (Prefs.getNotify(FlashLight.this)) {
							Context context = getApplicationContext();
							CharSequence contentTitle = "LED FlashLight";
							CharSequence contentText = "LEDs will turn off in "
									+ totalTime + " minutes.";
							Intent notificationIntent = new Intent(
									FlashLight.this, FlashLight.class);
							PendingIntent contentIntent = PendingIntent
									.getActivity(FlashLight.this, 0,
											notificationIntent, 0);

							notification.setLatestEventInfo(context,
									contentTitle, contentText, contentIntent);
							mNotificationManager.notify(notificationId,
									notification);
						}
					}
					if (secondsPassed == secondsTotal) {
						FlashlightLED led;
						try {
							led = new FlashlightLED();
							toggleLED(led);
							running = false;
							secondsPassed = 0;
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
			super.handleMessage(msg);
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		toggle = (ImageButton) findViewById(R.id.toggle_button);
		toggle.setOnClickListener(this);
		mNotificationManager = (NotificationManager) getSystemService(ns);
		
		//initialize our broadcast receiver to monitor the battery
		monitorBatteryState();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.settings_option:
			startActivity(new Intent(this, Prefs.class));
			return true;
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.toggle_button:
			try {
				FlashlightLED led = new FlashlightLED();
				totalTime = Integer.parseInt(Prefs.getTimer(FlashLight.this));
				secondsTotal = totalTime * 60;

				 lowBatteryThreshold = Integer.parseInt(Prefs
						 .getThreshold(FlashLight.this));

				if (Prefs.getNotify(FlashLight.this)) {
					//set up the notification to be displayed
					int icon = R.drawable.flashlight_small;
					CharSequence tickerText = "LEDs are active";
					long when = System.currentTimeMillis();

					notification = new Notification(icon, tickerText, when);

					Context context = getApplicationContext();
					CharSequence contentTitle = "LED FlashLight";
					CharSequence contentText;
					if (Prefs.getTimeout(FlashLight.this)) {
						contentText = "LEDs will turn OFF in " + totalTime
								+ " minutes";
					} else {
						contentText = "LEDs are currently ON";
					}
					Intent notificationIntent = new Intent(FlashLight.this,
							FlashLight.class);
					notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
							| Intent.FLAG_ACTIVITY_SINGLE_TOP);

					PendingIntent contentIntent = PendingIntent.getActivity(
							FlashLight.this, 0, notificationIntent, 0);

					notification.setLatestEventInfo(context, contentTitle,
							contentText, contentIntent);

					if (Prefs.getBlink(FlashLight.this)) {
						//set up the blinking notification LED
						notification.ledARGB = 0xff0000ff;
						notification.ledOnMS = 300;
						notification.ledOffMS = 1000;
						notification.flags |= Notification.FLAG_SHOW_LIGHTS;
					}
				}

				toggleLED(led);
				break;
			} catch (Exception e) {
				try {
					throw new Exception("LEDs could not be initialized");
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}

		}
	}

	/**
	 * Sets up a BroadcastReciever to continually monitor the battery levels of
	 * the device. It translates these results into a percent remaining value.
	 */
	private void monitorBatteryState() {
		//set up a new broadcast receiver to monitor the batter levels
		BroadcastReceiver battReceiver = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {

				context.unregisterReceiver(this);
				int rawlevel = intent.getIntExtra("level", -1);
				int scale = intent.getIntExtra("scale", -1);
				if (rawlevel >= 0 && scale > 0) {
					level = (rawlevel * 100) / scale;
				}
			}
		};
		IntentFilter battFilter = new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(battReceiver, battFilter);
	}

	/**
	 * Checks to make sure the battery has not dropped below the set threshold
	 */
	private void checkBatteryStatus() {
		if (level < lowBatteryThreshold) {
			lowBatt = true;
		} else {
			lowBatt = false;
		}
	}

	/**
	 * Toggles the LED on or off, and if enabled, displays a notification and
	 * blinks the notification LED. If enabled, will also start the timer to
	 * automatically turn out the light.
	 * 
	 * @param led
	 *            The DroidLED to use
	 */
	public void toggleLED(FlashlightLED led) {
		//if the LEDs are already on
		if (led.isEnabled()) {
			led.enable(!led.isEnabled());
			//change the button image
			toggle.setImageResource(R.drawable.flashlight);
			//if notification is enabled
			if (Prefs.getNotify(FlashLight.this)) {
				mNotificationManager.cancel(notificationId);
			}
			//kill the thread, and reset all timers
			running = false;
			secondsPassed = 0;
			totalTime = 0;
			//if killed due to low battery
			if (lowBatt) {
				Toast toast = Toast
						.makeText(
								this,
								"Battery is lower than specified threshold. LEDs disabled...",
								Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
				lowBatt = false;
			}

		} else {
			led.enable(!led.isEnabled());
			//change the button image
			toggle.setImageResource(R.drawable.flashlight_lit);
			//if notification is enabled
			if (Prefs.getNotify(FlashLight.this)) {
				mNotificationManager.notify(notificationId, notification);
			}
			//if the timer is enabled, fire up the thread
			if (Prefs.getTimeout(FlashLight.this)
					|| Prefs.getBattThreshold(FlashLight.this)) {
				countDownThread = new Thread(new SecondCountDownRunner());
				countDownThread.start();
				running = true;
			}
		}
	}

	/**
	 * Toggles the LED on or off without any notification. Used for the widget.
	 * 
	 * @param led
	 *            The DroidLED to use
	 */
	public void toggleLedWidget(FlashlightLED led) {
		led.enable(!led.isEnabled());
	}

	/**
	 * Keeps track of how much time has passed using a thread. If time is up, or
	 * the LEDs are disabled for any reason, the thread is killed
	 * @author Jeremy
	 *
	 */
	class SecondCountDownRunner implements Runnable {
		// @Override
		public void run() {
			FlashlightLED led;
			try {
				led = new FlashlightLED();
				while (!Thread.currentThread().isInterrupted()) {
					// check to see if the LED is off or should be off
					if (!running || !led.isEnabled() || lowBatt) {
						// if the battery is low
						if (lowBatt) {
							Message m = new Message();
							m.what = FlashLight.TOGGLELEDIDENTIFIER;
							threadHandler.sendMessage(m);
						}
						return;
					}
					//make a call back to our handlers
					Message m = new Message();
					m.what = FlashLight.SECONDPASSEDIDENTIFIER;
					timerUpdateHandler.sendMessage(m);
					//if the user has the battery monitor enabled
					if (Prefs.getBattThreshold(FlashLight.this)) {
						checkBatteryStatus();
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}
}
