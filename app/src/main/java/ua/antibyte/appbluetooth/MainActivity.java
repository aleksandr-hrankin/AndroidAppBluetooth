package ua.antibyte.appbluetooth;

import android.Manifest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "TAG_MAIN";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_LOCATION = 1;

    private TextView tvResult;
    private Button btnStartScan;
    private Button btnStopScan;

    private BluetoothManager btManager;
    private BluetoothAdapter btAdapter;
    private BluetoothLeScanner btScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initFields();
        setOnClickListeners();
        enableBluetooth();
        enableLocation();

        btnStopScan.setVisibility(View.INVISIBLE);
    }

    private void initViews() {
        tvResult = findViewById(R.id.tv_main_result);
        btnStartScan = findViewById(R.id.btn_main_scan_start);
        btnStopScan = findViewById(R.id.btn_main_scan_stop);
    }

    private void initFields() {
        btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        btScanner = btAdapter.getBluetoothLeScanner();
    }

    private void setOnClickListeners() {
        btnStartScan.setOnClickListener(onClickBtnStartScan());
        btnStopScan.setOnClickListener(onClickBtnStopScan());
    }

    private View.OnClickListener onClickBtnStartScan() {
        return v -> {
            tvResult.setText("Start Scanning \n");
            btnStartScan.setVisibility(View.INVISIBLE);
            btnStopScan.setVisibility(View.VISIBLE);
            AsyncTask.execute(() -> btScanner.startScan(getScanFilters(), getScanSettings(), getScanCallback()));
        };
    }

    private View.OnClickListener onClickBtnStopScan() {
        return v -> {
            tvResult.append("Stopped Scanning \n");
            btnStartScan.setVisibility(View.VISIBLE);
            btnStopScan.setVisibility(View.INVISIBLE);
            AsyncTask.execute(() -> btScanner.stopScan(getScanCallback()));
        };
    }

    private void enableBluetooth() {
        if (btAdapter != null && !btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
    }

    private void enableLocation() {
        String permission = null;
        if (Build.VERSION.SDK_INT >= 23 && Build.VERSION.SDK_INT < 29) {
            permission = Manifest.permission.ACCESS_COARSE_LOCATION;
        }
        if (Build.VERSION.SDK_INT >= 29) {
            permission = Manifest.permission.ACCESS_FINE_LOCATION;
        }
        if (permission != null) {
            if (this.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                showAlertDialogNeedLocation(permission);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Coarse location permission granted");
            } else {
                showAlertDialogFunctionalityLimited();
            }
        }
    }

    private List<ScanFilter> getScanFilters() {
        ScanFilter filter = new ScanFilter.Builder()
                .setDeviceName(null)
                .build();
        List<ScanFilter> filters = new ArrayList<>();
        filters.add(filter);
        return filters;
    }

    private ScanSettings getScanSettings() {
        return new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
                .setReportDelay(0L)
                .build();
    }

    private ScanCallback getScanCallback() {
        return new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                tvResult.append("--- " + result.getDevice().getName() + " --- \n");
                tvResult.append("device address: " + result.getDevice().getAddress() + "\n");
                tvResult.append("device type: " + result.getDevice().getType() + "\n");
                tvResult.append("rssi: " + result.getRssi() + "\n");
                tvResult.append("\n");
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                Log.e(TAG, "work");
            }

            @Override
            public void onScanFailed(int errorCode) {
                Log.e(TAG, "Can't scan. ErrorCode: " + errorCode);
            }
        };
    }

    private void showAlertDialogNeedLocation(String permission) {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("This app needs location access");
        dialogBuilder.setMessage("Please grant location access so this app can detect peripherals.");
        dialogBuilder.setPositiveButton("Ok", null);
        dialogBuilder.setOnDismissListener(dialog -> requestPermissions(new String[]{permission}, PERMISSION_REQUEST_LOCATION));
        dialogBuilder.show();
    }

    private void showAlertDialogFunctionalityLimited() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Functionality limited");
        builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
        builder.setPositiveButton("OK", null);
        builder.setOnDismissListener(dialog -> {
        });
        builder.show();
    }
}
