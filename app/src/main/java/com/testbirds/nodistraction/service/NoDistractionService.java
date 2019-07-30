package com.testbirds.nodistraction.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.testbirds.nodistraction.R;
import com.testbirds.nodistraction.ui.MainActivity;
import com.testbirds.nodistraction.viewModel.MainViewModel;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static com.testbirds.nodistraction.NoDistractionApp.CHANNEL_ID;

public class NoDistractionService extends Service {

    public static boolean SERVICE_RUNNING = false;
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("No Distraction Service")
                .setContentText("")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        checkForgroundAppIsBlocked();
        SERVICE_RUNNING = true;
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void checkForgroundAppIsBlocked() {
        disposables.add(Observable.interval(1000, 1000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    String packageName = getForegroundApp();
                    Set<String> blockedApps = MainViewModel.loadBlockedApps(NoDistractionService.this);
                    if (packageName != null && blockedApps != null) {
                        if (blockedApps.contains(packageName)) {
                            new Handler(Looper.getMainLooper()).post(() -> showCustomToast(NoDistractionService.this));
                            Intent openAppIntent = new Intent(NoDistractionService.this, MainActivity.class);
                            openAppIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(openAppIntent);
                        }
                    }
                }));
    }

    private String getForegroundApp() {

        String foregroundApp = null;
        long time = System.currentTimeMillis();

        UsageStatsManager mUsageStatsManager = (UsageStatsManager) getSystemService(Service.USAGE_STATS_SERVICE);
        UsageEvents usageEvents = mUsageStatsManager.queryEvents(time - 1000 * 3600, time);
        UsageEvents.Event event = new UsageEvents.Event();
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event);
            if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                foregroundApp = event.getPackageName();
            }
        }
        return foregroundApp;
    }

    public void showCustomToast(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View customToastView = inflater.inflate(R.layout.custom_toast, null);
        ImageView toastIcon = customToastView.findViewById(R.id.toastIcon);
        toastIcon.setImageDrawable(context.getDrawable(R.drawable.block));
        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(customToastView);
        toast.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SERVICE_RUNNING = false;
        disposables.clear();
    }
}
