package com.testbirds.nodistraction.ui;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.testbirds.nodistraction.R;
import com.testbirds.nodistraction.helper.Constants;
import com.testbirds.nodistraction.service.NoDistractionService;
import com.testbirds.nodistraction.ui.adapter.InstalledAppsAdapter;
import com.testbirds.nodistraction.viewModel.MainViewModel;

import java.util.List;

public class MainActivity extends AppCompatActivity implements InstalledAppsAdapter.ItemClickListener {

    private MainViewModel mainViewModel = null;
    RecyclerView appRecyclerView;
    List<ResolveInfo> appsList;
    Button switchMode;
    InstalledAppsAdapter installedAppsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mainViewModel = new MainViewModel();

        appsList = mainViewModel.getAppList(this);
        setContentView(R.layout.activity_main);

        viewInitalization();
        buildRecyclerView();
    }

    private void viewInitalization() {
        appRecyclerView = findViewById(R.id.appList);
        switchMode = findViewById(R.id.activateButton);

        if (!NoDistractionService.SERVICE_RUNNING) {
            switchMode.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));
            switchMode.setText(getResources().getString(R.string.activate_no_distraction_mode));
        } else {
            switchMode.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
            switchMode.setText(getResources().getString(R.string.deactivate_no_distraction_mode));
        }

        switchMode.setOnClickListener(v -> {

            if (NoDistractionService.SERVICE_RUNNING) {
                stopService();
                switchMode.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));
                switchMode.setText(getResources().getString(R.string.activate_no_distraction_mode));
            } else {
                boolean permissionGranted = mainViewModel.checkUsageAllowPermission(this);
                if (permissionGranted) {
                    startService();
                    switchMode.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
                    switchMode.setText(getResources().getString(R.string.deactivate_no_distraction_mode));
                } else {
                    AlertDialog.Builder permissionDialog = new AlertDialog.Builder(this);
                    permissionDialog.setTitle("Permission needed")
                            .setMessage("To use this application kindly you asked to allow usage access.")
                            .setPositiveButton("Ok", (dialog, which) -> startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))).show();
                }
            }
        });
    }

    private void buildRecyclerView() {
        appRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        installedAppsAdapter = new InstalledAppsAdapter(this, appsList);
        appRecyclerView.setAdapter(installedAppsAdapter);
        installedAppsAdapter.setClickListener(this);
    }

    public void startService() {
        Intent serviceIntent = new Intent(this, NoDistractionService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    public void stopService() {
        Intent serviceIntent = new Intent(this, NoDistractionService.class);
        stopService(serviceIntent);
    }

    @Override
    public void onBlockButtonClick(View view, int position) {
        int callback = mainViewModel.blockApp(this, appsList.get(position).activityInfo.packageName);
        if (callback == Constants.APP_BLOCKED) {
            Toast.makeText(this, getResources().getString(R.string.app_blocked), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, getResources().getString(R.string.app_unblocked), Toast.LENGTH_LONG).show();
        }
        installedAppsAdapter.notifyDataSetChanged();
    }
}
