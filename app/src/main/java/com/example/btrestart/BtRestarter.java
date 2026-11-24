package com.example.btrestart;

import android.bluetooth.BluetoothAdapter;

import java.util.concurrent.TimeUnit;

public class BtRestarter {

    public interface Log {
        void line(String s);
    }

    public static String stateName(int s) {
        switch (s) {
            case BluetoothAdapter.STATE_OFF: return "OFF";
            case BluetoothAdapter.STATE_TURNING_ON: return "TURNING_ON";
            case BluetoothAdapter.STATE_ON: return "ON";
            case BluetoothAdapter.STATE_TURNING_OFF: return "TURNING_OFF";
            default: return "UNKNOWN(" + s + ")";
        }
    }

    // Fallback for rooted head units
    private static String shell(String cmd) {
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", cmd});
            if (!p.waitFor(5, TimeUnit.SECONDS)) {
                p.destroy();
                return "su -c \"" + cmd + "\" -> timeout";
            }
            return "su -c \"" + cmd + "\" -> exit " + p.exitValue();
        } catch (Exception e) {
            return "su -c \"" + cmd + "\" -> " + e.getClass().getSimpleName() + ": " + e.getMessage();
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
    public static void restart(Log log) {
        BluetoothAdapter a = BluetoothAdapter.getDefaultAdapter();
        if (a == null) {
            log.line("getDefaultAdapter() = null (no Bluetooth adapter)");
            return;
        }
        try {
            log.line("State at start: " + stateName(a.getState()));

            try {
                log.line("disable() returned " + a.disable());
            } catch (Exception e) {
                log.line("disable() threw " + e);
            }
            boolean off = waitForState(a, BluetoothAdapter.STATE_OFF, 8000);
            log.line("Reached OFF: " + off + ", state now " + stateName(a.getState()));
            if (!off) {
                log.line(shell("svc bluetooth disable"));
                waitForState(a, BluetoothAdapter.STATE_OFF, 5000);
                log.line("State now " + stateName(a.getState()));
            }

            Thread.sleep(1500);

            try {
                log.line("enable() returned " + a.enable());
            } catch (Exception e) {
                log.line("enable() threw " + e);
            }
            boolean on = waitForState(a, BluetoothAdapter.STATE_ON, 15000);
            log.line("Reached ON: " + on + ", state now " + stateName(a.getState()));
            if (!on) {
                log.line(shell("svc bluetooth enable"));
                waitForState(a, BluetoothAdapter.STATE_ON, 10000);
                log.line("State now " + stateName(a.getState()));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.line("Interrupted");
        }
        log.line("-- done --");
    }
}
