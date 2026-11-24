package com.example.btrestart;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(48, 48, 48, 48);

        final TextView status = new TextView(this);
        status.setTextSize(22);
        status.setGravity(Gravity.CENTER);

        final Button btn = new Button(this);
        btn.setText("Restart Bluetooth");
        btn.setTextSize(26);

        btn.setOnClickListener(v -> {
            btn.setEnabled(false);
            status.setText("Restarting...");
            new Thread(() -> {
                final String result = BtRestarter.restart();
                runOnUiThread(() -> {
                    status.setText(result);
                    btn.setEnabled(true);
                });
            }).start();
        });

        root.addView(btn);
        root.addView(status);
        setContentView(root);
    }
}
