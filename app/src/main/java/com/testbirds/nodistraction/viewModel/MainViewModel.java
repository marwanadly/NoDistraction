package com.testbirds.nodistraction.viewModel;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import com.testbirds.nodistraction.helper.Constants;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.content.Context.MODE_PRIVATE;

public class MainViewModel {

    public List<ResolveInfo> getAppList(Context context) {
        Intent installedAppsIntent = new Intent(Intent.ACTION_MAIN, null);
        installedAppsIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        return context.getPackageManager().queryIntentActivities(installedAppsIntent, 0);
    }

    public int blockApp(Context context,String packageName) {
        Set<String> blockedApps = loadBlockedApps(context);
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.APP_PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (blockedApps == null) {
            blockedApps = new HashSet<>();

            blockedApps.add(packageName);
            editor.putStringSet(Constants.BLOCKED_APPS_PREF, blockedApps);
            editor.apply();
            Log.i("blockApps size", String.valueOf(blockedApps.size()));

            return Constants.APP_BLOCKED;
        } else {
            if(!blockedApps.contains(packageName)){
                blockedApps.add(packageName);
                editor.putStringSet(Constants.BLOCKED_APPS_PREF, blockedApps);
                editor.apply();
                Log.i("blockApps size", String.valueOf(blockedApps.size()));

                return Constants.APP_BLOCKED;
            }else{
                blockedApps.remove(packageName);
                editor.putStringSet(Constants.BLOCKED_APPS_PREF, blockedApps);
                editor.apply();
                Log.i("blockApps size", String.valueOf(blockedApps.size()));

                return Constants.APP_UNBLOCKED;
            }
        }
    }

    public static Set<String> loadBlockedApps(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.APP_PREF, MODE_PRIVATE);
        Set<String> blockedApps = sharedPreferences.getStringSet(Constants.BLOCKED_APPS_PREF, null);
        if(blockedApps != null){
            return new HashSet<>(blockedApps);
        }else{
            return null;
        }
    }

    public boolean checkUsageAllowPermission(Context context){
        boolean granted;
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), context.getPackageName());

        if (mode == AppOpsManager.MODE_DEFAULT) {
            granted = (context.checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED);
        } else {
            granted = (mode == AppOpsManager.MODE_ALLOWED);
        }
        return granted;
    }
}
