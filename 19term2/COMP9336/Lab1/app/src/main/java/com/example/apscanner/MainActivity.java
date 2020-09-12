package com.example.apscanner;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiInfo;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.net.wifi.WifiManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements SignInDiaglog.SignInDialogListener {
    ScanResult wifiMessage;
    WifiManager wifiManager;
    public int i = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button scanButton = findViewById(R.id.button);
        // get from stackoverflow
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION }, 1);
        scanButton.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        findWifi(v);
                    }
                }
        );
    }

    public void checkWifi() {
        // create a receiver for wifi state change
        BroadcastReceiver wifiInfoReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (wifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                    NetworkInfo networkInfo =
                            intent.getParcelableExtra(wifiManager.EXTRA_NETWORK_INFO);
                    if (networkInfo.isConnected()) {
                        TextView textView = findViewById(R.id.debugMsg);
                        textView.setText("Connected");
                        WifiInfo wifiInfo =
                                intent.getParcelableExtra(wifiManager.EXTRA_WIFI_INFO);
                        if (wifiInfo == null) {
                            Toast toast =
                                    Toast.makeText(getApplicationContext(),
                                            "Oh!", Toast.LENGTH_LONG);
                            toast.show();
                        } else {
                            String BSSID = wifiInfo.getBSSID();
                            String SSID = wifiInfo.getSSID();
                            int ipInt = wifiInfo.getIpAddress();
                            int[] ipIntArray = new int[4];
                            ipIntArray[0] = ipInt >> 24 & 0xff;
                            ipIntArray[1] = ipInt >> 16 & 0xff;
                            ipIntArray[2] = ipInt >> 8 & 0xff;
                            ipIntArray[3] = ipInt & 0xff;
                            String ipAddr = ipIntArray[3]+"."
                                    +ipIntArray[2]+"."
                                    +ipIntArray[1]+"."
                                    +ipIntArray[0];
                            if (BSSID != null) {
                                TextView tx = findViewById(R.id.debugMsg);
                                tx.setText(ipAddr);
                                Toast toast =
                                        Toast.makeText(
                                                getApplicationContext(),
                                                SSID+":"+ ipAddr,
                                                Toast.LENGTH_LONG);
                                toast.show();
                            }
                        }
                    } else {
                        TextView textView = findViewById(R.id.debugMsg);
                        textView.setText("Not Connected");
                    }
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(wifiManager.NETWORK_STATE_CHANGED_ACTION);
        getApplicationContext().registerReceiver(wifiInfoReceiver, intentFilter);

    }

    public void findWifi(View v) {
        // a list of scan result
        // deprecated api?
        this.wifiManager = (WifiManager)getApplicationContext().
                getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            if (wifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLING) {
                wifiManager.setWifiEnabled(true);
            }
        }
        // borrow from wifi overview
        BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                boolean success = intent.getBooleanExtra(
                        WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (success) {
                    scanSuccess(wifiManager);
                } else {
                    scanFailure();
                }
            }

        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        getApplicationContext().registerReceiver(wifiScanReceiver, intentFilter);
        boolean success = wifiManager.startScan();
        if (!success) {
            scanFailure();
        }
    }
    private List<ScanResult> getStrongWifi(HashMap<String, ScanResult> wifis) {
        List<ScanResult> res = new ArrayList<>();
        List<ScanResult> output = new ArrayList<>();
        ScanResult temp;
        for(Map.Entry<String, ScanResult> entry: wifis.entrySet()) {
            temp = entry.getValue();
            res.add(temp);
        }
        Collections.sort(res, new Comparator<ScanResult>() {
            @Override
            public int compare(ScanResult o1, ScanResult o2) {
                return o2.level - o1.level;
            }
        });
        for(int i = 0; i < 4; i++) {
            output.add(res.get(i));
        }
        return output;
    }

    private void scanSuccess(final WifiManager wifiManager) {
        final TextView textView = findViewById(R.id.debugMsg);
        List<ScanResult> scanResults = wifiManager.getScanResults();
        HashMap<String, ScanResult> resultDict = new HashMap<>();
        List<ScanResult> strongWifi;
        String name;
        ScanResult strongestWifi;
        // index is the ssid of wifi, value is the wifi whose level is highest
        for(int i = 0; i < scanResults.size(); i++) {
            name = scanResults.get(i).SSID;
            strongestWifi = scanResults.get(i);
            // already in the dictionary
            if (resultDict.containsKey(name)) {
                if (strongestWifi.level > resultDict.get(name).level) {
                    resultDict.put(name, strongestWifi);
                }
            } else {
                resultDict.put(name, strongestWifi);
            }
        }
        // get top 4 strong level wifi, return 4 items array list
        strongWifi = getStrongWifi(resultDict);
        textView.setText("Scan success:\t"+scanResults.size());
        // Construct the data source
        // Create the adapter to convert the array to views
        WifiDataAdaptor adapter = new WifiDataAdaptor(this, strongWifi);
        // Attach the adapter to a ListView
        ListView listView = findViewById(R.id.wifiMsg);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //list view item click listener work here
                ScanResult wifiMsg = (ScanResult) parent.getItemAtPosition(position);
                //
                wifiMessage = wifiMsg;
                String SSID = wifiMsg.SSID;
                WifiConfiguration wifiConfiguration;
                if (wifiMsg.capabilities.contains("WPA")) {
                    // input username & password for access to wifi
                    showDialog();
                } else {
                    // open wifi
                    wifiConfiguration = new WifiConfiguration();
                    wifiConfiguration.SSID = "\""+SSID+"\"";
                    int netId = wifiManager.addNetwork(wifiConfiguration);
                    wifiManager.disconnect();
                    wifiManager.enableNetwork(netId, true);
                    wifiManager.reconnect();
                    checkWifi();
                }
            }
        });
    }
    // implements interface
    public ScanResult getScanResult(){
        return this.wifiMessage;
    }
    private void showDialog() {
        DialogFragment signInDialog = new SignInDiaglog();
        signInDialog.show(getSupportFragmentManager(), "SignIn");
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private WifiConfiguration
    createEAPConfiguration(String netSSID, String netIdent, String netPasswd) {
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        WifiEnterpriseConfig enterpriseConfig = new WifiEnterpriseConfig();
        wifiConfiguration.SSID = "\"" + netSSID + "\"";
        // for EAP, borrow from stack overflow
        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X);
        enterpriseConfig.setIdentity(netIdent);
        enterpriseConfig.setPassword(netPasswd);
        enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.PEAP);
        wifiConfiguration.enterpriseConfig = enterpriseConfig;
        return wifiConfiguration;
    }

    private WifiConfiguration
    createWPAConfiguration(String netSSID, String netPasswd) {
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = "\"" + netSSID + "\"";
        // for WAP
        wifiConfiguration.preSharedKey = "\"" + netPasswd + "\"";
        return wifiConfiguration;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onDialogPositiveClick(DialogFragment dialogFragment, ScanResult scanResult) {
        Dialog dialogView = dialogFragment.getDialog();
        String netPasswd = ((EditText)dialogView.findViewById(R.id.password)).
                getText().toString();
        String netIdent = ((EditText)dialogView.findViewById(R.id.username)).
                getText().toString();
        String netSSID = scanResult.SSID;
        String encroMode = scanResult.capabilities;

        WifiConfiguration wifiConfiguration;
        if (encroMode.contains("EAP")) {
            wifiConfiguration = createEAPConfiguration(netSSID, netIdent, netPasswd);;
        } else {
            wifiConfiguration = createWPAConfiguration(netSSID, netPasswd);
        }
        // remove other wifi configuration
        List<WifiConfiguration> wifiConfList = wifiManager.getConfiguredNetworks();
        for(WifiConfiguration i : wifiConfList) {
            wifiManager.removeNetwork(i.networkId);
        }
        int netId = wifiManager.addNetwork(wifiConfiguration);
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();
        checkWifi();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialogFragment) {
        // do nothing
    }

    private void scanFailure() {
        TextView textView = findViewById(R.id.debugMsg);
        textView.setText("Scan fail");
    }
}
