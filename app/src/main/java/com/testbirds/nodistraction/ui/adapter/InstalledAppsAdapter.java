package com.testbirds.nodistraction.ui.adapter;

import android.content.Context;
import android.content.pm.ResolveInfo;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.testbirds.nodistraction.R;
import com.testbirds.nodistraction.viewModel.MainViewModel;

import java.util.List;
import java.util.Set;

public class InstalledAppsAdapter extends RecyclerView.Adapter<InstalledAppsAdapter.AppViewHolder> {

    private List<ResolveInfo> appList;
    private ItemClickListener mClickListener;
    private Context context;

    public InstalledAppsAdapter(Context context, List<ResolveInfo> appList) {
        this.context = context;
        this.appList = appList;
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.app_item, viewGroup, false);
        return new AppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder appViewHolder, int i) {
        Set<String> blockedApps = MainViewModel.loadBlockedApps(context);

        appViewHolder.appName.setText(appList.get(i).loadLabel(context.getPackageManager()));
        appViewHolder.appIcon.setImageDrawable(appList.get(i).loadIcon(context.getPackageManager()));
        if(blockedApps != null){
            if (blockedApps.contains(appList.get(i).activityInfo.packageName)) {
                appViewHolder.blockButton.setText(context.getResources().getString(R.string.unblock_button_text));
                appViewHolder.blockButton.setBackgroundColor(appViewHolder.blockButton.getContext().getResources().getColor(android.R.color.holo_blue_dark));
            } else {
                appViewHolder.blockButton.setText(context.getResources().getString(R.string.block_button_text));
                appViewHolder.blockButton.setBackgroundColor(appViewHolder.blockButton.getContext().getResources().getColor(android.R.color.holo_red_dark));
            }
        }

        appViewHolder.blockButton.setOnClickListener(v -> {
            mClickListener.onBlockButtonClick(v,i);
            if(blockedApps != null){
                if (blockedApps.contains(appList.get(i).activityInfo.packageName)) {
                    appViewHolder.blockButton.setText(context.getResources().getString(R.string.unblock_button_text));
                    appViewHolder.blockButton.setBackgroundColor(appViewHolder.blockButton.getContext().getResources().getColor(android.R.color.holo_blue_dark));
                } else {
                    appViewHolder.blockButton.setText(context.getResources().getString(R.string.block_button_text));
                    appViewHolder.blockButton.setBackgroundColor(appViewHolder.blockButton.getContext().getResources().getColor(android.R.color.holo_red_dark));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    class AppViewHolder extends RecyclerView.ViewHolder{

        ImageView appIcon;
        TextView appName;
        Button blockButton;

        AppViewHolder(@NonNull View appView) {
            super(appView);
            appIcon = appView.findViewById(R.id.appIcon);
            appName = appView.findViewById(R.id.appName);
            blockButton = appView.findViewById(R.id.blockButton);
        }
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onBlockButtonClick(View view, int position);
    }
}
