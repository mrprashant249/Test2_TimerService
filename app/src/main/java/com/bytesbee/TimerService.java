package com.bytesbee;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class TimerService extends Service {

    public static String str_receiver = "com.bytesbee.receiver";

    private Handler mHandler = new Handler();
    Calendar calendar;
    SimpleDateFormat simpleDateFormat;
    String strDate;
    Date date_current, date_diff;
    SharedPreferences mpref;
    SharedPreferences.Editor mEditor;

    private Timer mTimer = null;
    public static final long NOTIFY_INTERVAL = 1000;
    Intent intent;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mpref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mEditor = mpref.edit();
        calendar = Calendar.getInstance();
        simpleDateFormat = new SimpleDateFormat("mm:ss");

        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new TimeDisplayTimerTask(), 5, NOTIFY_INTERVAL);
        intent = new Intent(str_receiver);
    }


    class TimeDisplayTimerTask extends TimerTask {

        @Override
        public void run() {
            mHandler.post(new Runnable() {

                @Override
                public void run() {

                    calendar = Calendar.getInstance();
                    simpleDateFormat = new SimpleDateFormat("mm:ss");
                    strDate = simpleDateFormat.format(calendar.getTime());
                    Log.e("strDate", strDate);
                    twoDatesBetweenTime();

                }

            });
        }

    }

    public String twoDatesBetweenTime() {
        try {
            date_current = simpleDateFormat.parse(strDate);
        } catch (Exception e) {
        }

        try {
            date_diff = simpleDateFormat.parse(mpref.getString("data", ""));
        } catch (Exception e) {
        }
        try {
            long diff = date_current.getTime() - date_diff.getTime();
            int int_hours = Integer.valueOf(mpref.getString("hours", ""));

            long int_timer = TimeUnit.MINUTES.toMillis(int_hours);
            long long_hours = int_timer - diff;
            long diffSeconds2 = long_hours / 1000 % 60;
            long diffMinutes2 = long_hours / (60 * 1000) % 60;

            if (long_hours > 0) {
                String str_testing = String.format(Locale.getDefault(), "%02d:%02d", diffMinutes2, diffSeconds2);
                Log.e("TIME", str_testing);
                fn_update(str_testing);
                showCustomNotification(str_testing);
            } else {
                mEditor.putBoolean("finish", true).commit();
                mTimer.cancel();
            }
        }catch (Exception e){
            mTimer.cancel();
            mTimer.purge();
        }
        return "";

    }

    @SuppressWarnings("deprecation")
    private void showCustomNotification(String time) {
        final int NOTIFICATION_ID = 1;
        final int icon = R.mipmap.ic_launcher;
        final long when = System.currentTimeMillis();

        try {
            final String strTicker = "You are on the break";
            final String strContextText = "Time left : " + time;

            final Intent cIntent = new Intent("KEY_MY_PUSH_OPEN");
            final PendingIntent pContentIntent = PendingIntent.getBroadcast(getApplicationContext(), 0 /*just for testing*/, cIntent, PendingIntent.FLAG_CANCEL_CURRENT);

            try {
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(getApplicationContext())
                                .setShowWhen(true)
                                .setWhen(when)
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), icon))
                                .setContentTitle(strTicker)
                                .setTicker(strTicker)
                                .setContentText(strContextText)
                                .setContentIntent(pContentIntent)
                                .setAutoCancel(true);

                NotificationCompat.BigTextStyle bigStyle = new NotificationCompat.BigTextStyle();
                bigStyle.setBigContentTitle(strTicker);
                bigStyle.bigText(strTicker);
                mBuilder.setStyle(bigStyle);

                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
            } catch (Exception e) {
                System.out.println("Prashant error " + e.getMessage());
            }
        } catch (Exception e) {
            System.out.println("Prashant error " + e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("Service finish","Finish");
    }

    private void fn_update(String str_time){
        intent.putExtra("time",str_time);
        sendBroadcast(intent);
    }
}
