package com.jr.ghoul;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jr.ghoul.adapter.ItemlistAdapter;
import com.jr.ghoul.listener.OnItemClickListener;
import com.jr.ghoul.wrapper.AppInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnItemClickListener {
    private final int RC_APP_UPDATE = 201;
    public List<AppInfo> appInfos = new ArrayList<AppInfo>();
    public TextView app_found_count;
    public ItemlistAdapter itemlistAdapter;
    public RelativeLayout listLayout;
    public RelativeLayout noappsfoundLayout;
    public ProgressBar progressBar;
    public RelativeLayout scan_ui;
    Button scan_now;
    private boolean isDeleteClick;
    private Button rescan_now;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_launcher);
        this.progressBar = findViewById(R.id.progressBar);
        this.scan_now = findViewById(R.id.scan_now);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        this.listLayout = findViewById(R.id.listLayout);
        this.scan_ui = findViewById(R.id.scan_ui);
        this.noappsfoundLayout = findViewById(R.id.noappsfoundLayout);
        this.app_found_count = findViewById(R.id.app_found_count);
        this.rescan_now = findViewById(R.id.rescan_now);
        this.itemlistAdapter = new ItemlistAdapter(this, this.appInfos, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(this.itemlistAdapter);
        ((TextView) findViewById(R.id.scan_now_txt)).setText(Html.fromHtml(getResources().getString(R.string.clickscantext)));
        this.scan_now.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                MainActivity.this.getApps();
            }
        });
        this.rescan_now.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                MainActivity.this.getApps();
            }
        });
    }

    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i == 201 && i2 != -1) {
            Log.e("onActivityResult", "onActivityResult: app download failed");
        }
    }

    public void getApps() {
        new GetAppsAsync().execute();
    }

    public void onResume() {
        super.onResume();
        if (this.isDeleteClick) {
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    MainActivity.this.getApps();
                }
            }, 1500);
        }
        this.isDeleteClick = false;
    }

    public void onItemClick(int i) {
        this.isDeleteClick = true;
    }

    private boolean isSystemPackage(PackageInfo packageInfo) {
        return (packageInfo.applicationInfo.flags & 1) != 0;
    }

    public List<AppInfo> getInstalledApps() {
        List<AppInfo> arrayList = new ArrayList<AppInfo>();
        List<PackageInfo> installedPackages = getPackageManager().getInstalledPackages(0);
        for (int i = 0; i < installedPackages.size(); i++) {
            PackageInfo packageInfo = installedPackages.get(i);
            if (!isSystemPackage(packageInfo)) {
                AppInfo appInfo = new AppInfo();
                appInfo.appName = packageInfo.applicationInfo.loadLabel(getPackageManager()).toString();
                appInfo.packageName = packageInfo.packageName;
                appInfo.versionName = packageInfo.versionName;
                appInfo.versionCode = packageInfo.versionCode;
                appInfo.icon = packageInfo.applicationInfo.loadIcon(getPackageManager());
                long length = new File(packageInfo.applicationInfo.publicSourceDir).length() / 1048576;
                appInfo.size = length + " MB";
                arrayList.add(appInfo);
            }
        }
        return arrayList;
    }

    class AppUninstalledreceiver extends BroadcastReceiver {
        AppUninstalledreceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            MainActivity.this.getApps();
        }
    }

    class GetAppsAsync extends AsyncTask<Void, Void, List<AppInfo>> {
        GetAppsAsync() {
        }


        public void onPreExecute() {
            super.onPreExecute();
            MainActivity.this.progressBar.setVisibility(View.VISIBLE);
            MainActivity.this.scan_ui.setVisibility(View.GONE);
        }

        public List<AppInfo> doInBackground(Void... voidArr) {
            return GhoulApp.getInstance().getDbHelper().isExist(MainActivity.this.getInstalledApps(), MainActivity.this.appInfos);
        }


        public void onPostExecute(List<AppInfo> list) {
            super.onPostExecute(list);
            MainActivity.this.progressBar.setVisibility(View.GONE);
            MainActivity.this.scan_ui.setVisibility(View.GONE);
            if (list.size() > 0) {
                MainActivity.this.listLayout.setVisibility(View.VISIBLE);
                MainActivity.this.noappsfoundLayout.setVisibility(View.GONE);
                MainActivity.this.app_found_count.setText(Html.fromHtml(MainActivity.this.getResources().getString(R.string.app_found_count, Integer.valueOf(list.size()))));
            } else {
                MainActivity.this.listLayout.setVisibility(View.GONE);
                MainActivity.this.noappsfoundLayout.setVisibility(View.VISIBLE);
            }
            MainActivity.this.itemlistAdapter.notifyDataSetChanged();
            if (list.size() == 0) {
                Collections.sort(MainActivity.this.appInfos, new Comparator<AppInfo>() {
                    public int compare(AppInfo appInfo, AppInfo appInfo2) {
                        return appInfo.appName.compareToIgnoreCase(appInfo2.appName);
                    }
                });
            }
        }
    }


}
