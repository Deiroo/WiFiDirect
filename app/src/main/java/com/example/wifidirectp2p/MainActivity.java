package com.example.wifidirectp2p;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;

import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.example.jsonsocket.WifiDirectConnector;
import com.example.jsonsocket.WifiDirectReceiver;
import com.example.jsonsocket.jsonsEntities.JsonEntity;
import com.google.gson.Gson;


public class MainActivity extends AppCompatActivity {

    TextView connectionStatus, messageTextView;
    Button aSwitch, discoverButton;
    ListView listView;
    EditText typeMsg;
    ImageButton sendButton;

    BroadcastReceiver receiver;
    IntentFilter intentFilter;

    Gson gson = new Gson();

    WifiDirectConnector wifiDirectConnector = new WifiDirectConnector();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialWork();

        exqListener();
    }

    private void exqListener() {
        aSwitch.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                startActivityForResult(intent,1);
            }
        });

        discoverButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                wifiDirectConnector.discoverPeers(connectionStatus);

            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final WifiP2pDevice device = wifiDirectConnector.getDeviceArray()[position];
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;

                wifiDirectConnector.connectDevice(connectionStatus, config, device);

            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String msg = typeMsg.getText().toString();
                JsonEntity jsonEntity = new JsonEntity();
                jsonEntity.setMessage(msg);
                String jsonMessage = gson.toJson(jsonEntity);

                wifiDirectConnector.sendMessage(jsonMessage);

            }
        });
    }

    private void initialWork() {
        connectionStatus = findViewById(R.id.tvConnectionStatus);
        messageTextView = findViewById(R.id.tvMessage);
        aSwitch = findViewById(R.id.btnOn);
        discoverButton = findViewById(R.id.btnDiscover);
        listView = findViewById(R.id.listView);
        typeMsg = findViewById(R.id.editTextTypeMsg);
        sendButton = findViewById(R.id.sendButton);

        wifiDirectConnector.setPeerListListener(wifiDirectConnector.initPeerListListener(listView, connectionStatus, getApplicationContext()));
        wifiDirectConnector.setConnectionInfoListener(wifiDirectConnector.initConnectionInfoListener(connectionStatus,messageTextView));

        //seteamos el manager de wifip2p con el contexto del sistema
        wifiDirectConnector.setManager((WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE));

        if (wifiDirectConnector.getManager() != null) {
            wifiDirectConnector.setChannel(wifiDirectConnector.getManager().initialize(this, getMainLooper(), null));
        }

        //llama al constructor de WifiDirectReceiver
        receiver = new WifiDirectReceiver(wifiDirectConnector.getManager(), wifiDirectConnector.getChannel(),
                wifiDirectConnector.getPeerListListener(), wifiDirectConnector.getConnectionInfoListener(), connectionStatus);

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

}
