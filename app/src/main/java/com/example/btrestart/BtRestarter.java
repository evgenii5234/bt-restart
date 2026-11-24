package com.example.btrestart;

import android.bluetooth.BluetoothAdapter;

public class BtRestarter {

    // Fallback for rooted head units
    private static boolean shell(String cmd) {
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", cmd});
            return p.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean waitForState(BluetoothAdapter a, int state, int timeoutMs)
            throws InterruptedException {
        long end = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < end) {
            if (a.getState() == state) return true;
            Thread.sleep(200);
        }
        return a.getState() == state;
    }

    /** Blocking. Call from a background thread. */
    @SuppressWarnings("deprecation")
    public static String restart() {
        BluetoothAdapter a = BluetoothAdapter.getDefaultAdapter();
        if (a == null) return "Bluetooth adapter not found";
        try {
            if (a.isEnabled()) {
                if (!a.disable()) shell("svc bluetooth disable");
                waitForState(a, BluetoothAdapter.STATE_OFF, 8000);
            }
            Thread.sleep(1500);
            if (!a.enable()) shell("svc bluetooth enable");
            boolean on = waitForState(a, BluetoothAdapter.STATE_ON, 10000);
            return on ? "Bluetooth restarted" : "Bluetooth did not turn on";
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Interrupted";
        }
    }
}
