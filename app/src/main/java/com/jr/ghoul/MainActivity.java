package com.jr.ghoul;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.pm.PackageInfo;
import android.content.res.Resources.NotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.OnSuccessListener;
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

    InstallStateUpdatedListener installStateUpdatedListener = new InstallStateUpdatedListener() {
        public void onStateUpdate(InstallState installState) {
            try {
                if (installState.installStatus() == InstallStatus.DOWNLOADED) {
                    MainActivity.this.popupSnackbarForCompleteUpdate();
                } else if (installState.installStatus() != InstallStatus.INSTALLED) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("InstallStateUpdatedListener: state: ");
                    sb.append(installState.installStatus());
                    Log.i("MainActivity", sb.toString());
                } else if (MainActivity.this.mAppUpdateManager != null) {
                    MainActivity.this.mAppUpdateManager.unregisterListener(MainActivity.this.installStateUpdatedListener);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    private boolean isDeleteClick;
    /* access modifiers changed from: private */
    public ItemlistAdapter itemlistAdapter;
    /* access modifiers changed from: private */
    public RelativeLayout listLayout;
    AppUpdateManager mAppUpdateManager;
    /* access modifiers changed from: private */
    public RelativeLayout noappsfoundLayout;
    /* access modifiers changed from: private */
    public ProgressBar progressBar;
    private RecyclerView recycler_view;
    private Button rescan_now;
    Button scan_now;
    /* access modifiers changed from: private */
    public RelativeLayout scan_ui;
    private ImageView shareIcon;

    /* renamed from: com.chinaappsremover.ui.MainActivity$AppUninstalledreceiver */
    class AppUninstalledreceiver extends BroadcastReceiver {
        AppUninstalledreceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            MainActivity.this.getApps();
        }
    }

    /* renamed from: com.chinaappsremover.ui.MainActivity$GetAppsAsync */
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

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_launcher);
        this.progressBar = findViewById(R.id.progressBar);
        this.shareIcon = findViewById(R.id.shareIcon);
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
        checkAppUpdate();
        this.shareIcon.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                MainActivity.this.shareApp();
            }
        });
    }

    private void checkAppUpdate() {
        try {
            this.mAppUpdateManager = AppUpdateManagerFactory.create(this);
            this.mAppUpdateManager.registerListener(this.installStateUpdatedListener);
            this.mAppUpdateManager.getAppUpdateInfo().addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>() {
                public void onSuccess(AppUpdateInfo appUpdateInfo) {
                    if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                            && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                        try {
                            MainActivity.this.mAppUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.FLEXIBLE, MainActivity.this, 201);
                        } catch (SendIntentException e) {
                            e.printStackTrace();
                        }
                    } else if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                        MainActivity.this.popupSnackbarForCompleteUpdate();
                    } else {
                        Log.e("MainActivity", "checkForAppUpdateAvailability: something else");
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i == 201 && i2 != -1) {
            Log.e("onActivityResult", "onActivityResult: app download failed");
        }
    }

    /* access modifiers changed from: private */
    public void popupSnackbarForCompleteUpdate() {
        try {
            Snackbar make = Snackbar.make(findViewById(R.id.scan_ui), "An update has just been downloaded.", Snackbar.LENGTH_SHORT);
            make.setAction("RESTART", new OnClickListener() {
                public void onClick(View view) {
                    if (MainActivity.this.mAppUpdateManager != null) {
                        MainActivity.this.mAppUpdateManager.completeUpdate();
                    }
                }
            });
            make.setActionTextColor(getResources().getColor(R.color.green));
            make.show();
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: private */
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

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int itemId = menuItem.getItemId();
        if (itemId == R.id.contact_us) {
            openUrl(getResources().getString(R.string.contact_us_url));
            return true;
        } else if (itemId == R.id.privacy_policy) {
            openUrl(getResources().getString(R.string.privacy_policy_url));
            return true;
        } else if (itemId != R.id.rate_us) {
            return super.onOptionsItemSelected(menuItem);
        } else {
            rateUs();
            return true;
        }
    }

    private void openUrl(String str) {
        startActivity(new Intent("android.intent.action.VIEW", Uri.parse(str)));
    }

    private void rateUs() {
        StringBuilder sb = new StringBuilder();
        sb.append("market://details?id=");
        sb.append(getPackageName());
        String str = "android.intent.action.VIEW";
        Intent intent = new Intent(str, Uri.parse(sb.toString()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException unused) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append("http://play.google.com/store/apps/details?id=");
            sb2.append(getPackageName());
            startActivity(new Intent(str, Uri.parse(sb2.toString())));
        }
    }

    /* access modifiers changed from: private */
    public void shareApp() {
        try {
            Intent intent = new Intent("android.intent.action.SEND");
            intent.setType("text/plain");
            intent.putExtra("android.intent.extra.SUBJECT", getResources().getString(R.string.app_name));
            StringBuilder sb = new StringBuilder();
            sb.append("\nKonnichiwa, I am using Ghoul app to get rid of Chinese apps. Try it by clicking\n\n");
            sb.append("https://play.google.com/store/apps/details?id=");
            sb.append(getPackageName());
            sb.append("\n\n");
            intent.putExtra("android.intent.extra.TEXT", sb.toString());
            startActivity(Intent.createChooser(intent, "Pick one"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean isSystemPackage(PackageInfo packageInfo) {
        return (packageInfo.applicationInfo.flags & 1) != 0;
    }

    /* access modifiers changed from: private */
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
}
