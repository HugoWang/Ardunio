package com.example.blunobasicdemo;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.TextView;

public class MyActivity extends Activity {
    private BluetoothAdapter BTAdapter = BluetoothAdapter.getDefaultAdapter();

    private final double coef_A = 0.42093;
    private final double coef_B = 6.9476;
    private final double coef_C = 0.54992;
    private final double ref_t = -45;

    private final double coef_a = 4.6112676141;
    private final double coef_b = 1.7202622463;
    private Vibrator v;

    private Integer le_rssi;
    private String le_device;

    final Handler mHandler = new Handler();


    // Create runnable for posting
    final Runnable mUpdateResults = new Runnable() {
        public void run() {
            updateResultsInUi();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        BTAdapter.startLeScan(new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                String name = device.getName();
                Log.e("MY Activity", "scan rssi: " + rssi + ", name: " + name);
                if (name.equals("BlunoV1.8")){
                    le_rssi = rssi;
                    le_device = name;
                    mHandler.post(mUpdateResults);
                }

            }
        });

    }

    private void updateResultsInUi() {
        TextView rssi_msg = (TextView) findViewById(R.id.textView3);
        rssi_msg.setText(String.valueOf(le_rssi));

        //double d = coef_b * Math.pow((le_rssi/ref_t), coef_a);
        double d = calculateAccuracy(-50, le_rssi);

        if (d > 90.f){
            v.vibrate(500);

            try {
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                r.play();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        TextView rssi_dist = (TextView) findViewById(R.id.textView7);
        rssi_dist.setText(String.valueOf(d));

        TextView rssi_dev = (TextView) findViewById(R.id.textView5);
        rssi_dev.setText(le_device);
    }

    protected static double calculateAccuracy(int txPower, double rssi) {
        if (rssi == 0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }

        double ratio = rssi*1.0/txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio,10);
        }
        else {
            double accuracy =  (0.89976)*Math.pow(ratio,7.7095) + 0.111;
            return accuracy;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unregisterReceiver(receiver);
    }

}
