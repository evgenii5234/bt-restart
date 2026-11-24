package com.example.btrestart;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends Activity {

    private TextView logView;

    private void log(final String s) {
        runOnUiThread(() -> logView.append(s + "\n"));
    }

    private Button button(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(20);
        return b;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 32, 32, 32);
        scroll.addView(root);

        final Button restart = button("Restart Bluetooth");
        Button settings = button("Open Bluetooth settings");
        Button request = button("Ask system to turn on Bluetooth");
        Button list = button("List Bluetooth-related apps");
        Button vendor = button("Open vendor Bluetooth app (com.tw.bt)");
        Button info = button("App info: vendor Bluetooth (Force stop)");

        logView = new TextView(this);
        logView.setTextSize(16);
        logView.setTextIsSelectable(true);

        BluetoothAdapter a = BluetoothAdapter.getDefaultAdapter();
        logView.setText("Android " + android.os.Build.VERSION.RELEASE
                + " (API " + android.os.Build.VERSION.SDK_INT + ")\n"
                + "Adapter: " + (a == null ? "none" : BtRestarter.stateName(a.getState())) + "\n\n");

        restart.setOnClickListener(v -> {
            restart.setEnabled(false);
            log("Restarting...");
            new Thread(() -> {
                BtRestarter.restart(this::log);
                runOnUiThread(() -> restart.setEnabled(true));
            }).start();
        });

        settings.setOnClickListener(v -> {
            try {
                log("Opening Bluetooth settings...");
                startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
            } catch (Exception e) {
                log("Settings screen failed: " + e);
            }
        });

        request.setOnClickListener(v -> {
            try {
                log("Sending enable request...");
                startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
            } catch (Exception e) {
                log("Enable request failed: " + e);
            }
        });

        list.setOnClickListener(v -> new Thread(() -> {
            PackageManager pm = getPackageManager();
            for (PackageInfo p : pm.getInstalledPackages(0)) {
                String n = p.packageName.toLowerCase(Locale.ROOT);
                if (n.contains("blue") || n.contains(".bt") || n.contains("bt.") || n.contains("phone")) {
                    boolean sys = (p.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
                    log((sys ? "[sys] " : "[app] ") + p.packageName
                            + " (" + pm.getApplicationLabel(p.applicationInfo) + ")");
                }
            }
            log("-- end of list --");
        }).start());

        vendor.setOnClickListener(v -> {
            Intent i = getPackageManager().getLaunchIntentForPackage("com.tw.bt");
            if (i == null) {
                log("No launch intent for com.tw.bt");
                return;
            }
            try {
                log("Opening com.tw.bt...");
                startActivity(i);
            } catch (Exception e) {
                log("Launch failed: " + e);
            }
        });

        info.setOnClickListener(v -> {
            try {
                log("Opening app info for com.tw.bt...");
                startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        android.net.Uri.parse("package:com.tw.bt")));
            } catch (Exception e) {
                log("App info failed: " + e);
            }
        });

        root.addView(restart);
        root.addView(settings);
        root.addView(request);
        root.addView(list);
        root.addView(vendor);
        root.addView(info);
        root.addView(logView);
        setContentView(scroll);
    }
}
