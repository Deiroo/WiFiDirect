package com.example.wifidirectp2p;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.example.jsonsocket.WifiDirectConnector;
import com.example.jsonsocket.WifiDirectReceiver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    TextView connectionStatus, messageTextView;
    Button aSwitch, discoverButton;
    ListView listView;
    EditText typeMsg;
    ImageButton sendButton;

//    WifiP2pManager manager;
//    WifiP2pManager.Channel channel;

    BroadcastReceiver receiver;
    IntentFilter intentFilter;

//    List<WifiP2pDevice> peers = new ArrayList<>();
//    String[] deviceNameArray;
//    WifiP2pDevice[] deviceArray;

    //Socket socket;

//    ServerClass serverClass;
//    ClientClass clientClass;

    boolean isHost;

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
//                manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
//                    @Override
//                    public void onSuccess() {
//                        try{
//                            connectionStatus.setText("Se está buscando dispositivos");
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(int reason) {
//                        connectionStatus.setText("No se realizó el descubrimiento");
//                    }
//                });
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final WifiP2pDevice device = wifiDirectConnector.getDeviceArray()[position];
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;

                wifiDirectConnector.connectDevice(connectionStatus, config, device);

//                manager.connect(channel, config, new WifiP2pManager.ActionListener() {
//                    @Override
//                    public void onSuccess() {
//                        connectionStatus.setText("Dispositivo conectado: " + device.deviceName);
//                    }
//
//                    @Override
//                    public void onFailure(int reason) {
//                        connectionStatus.setText("No se pudo realizar la conexión");
//
//                    }
//                });
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String msg = typeMsg.getText().toString();
                wifiDirectConnector.sendMessage(msg);
//                ExecutorService executorService = Executors.newSingleThreadExecutor();
//                executorService.execute(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (msg != null && isHost){
//                            serverClass.write(msg.getBytes());
//                        } else if (msg != null && !isHost){
//                            clientClass.write(msg.getBytes());
//                        }
//                    }
//                });
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

        //seteamos el manager de wifip2p con el contexto del sistema
        wifiDirectConnector.setManager((WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE));

        if (wifiDirectConnector.getManager() != null) {
            wifiDirectConnector.setChannel(wifiDirectConnector.getManager().initialize(this, getMainLooper(), null));
        }

        //llama al construcot de WifiDirectReceiver
        //receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        receiver = new WifiDirectReceiver(wifiDirectConnector.getManager(), wifiDirectConnector.getChannel(),
                wifiDirectConnector.getPeerListListener(), wifiDirectConnector.getConnectionInfoListener(), connectionStatus);

        wifiDirectConnector.initPeerListListener(listView, connectionStatus, getApplicationContext());
        wifiDirectConnector.initConnectionInfoListener(connectionStatus,messageTextView);

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

    }

//    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
//        @Override
//        public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
//            Collection<WifiP2pDevice> listDevices = wifiP2pDeviceList.getDeviceList();
//            if(!listDevices.equals(peers)){
//                peers.clear();
//                peers.addAll(wifiP2pDeviceList.getDeviceList());
//
//                deviceNameArray = new String[wifiP2pDeviceList.getDeviceList().size()];
//                deviceArray = new WifiP2pDevice[wifiP2pDeviceList.getDeviceList().size()];
//
//                int index = 0;
//                for(WifiP2pDevice device : wifiP2pDeviceList.getDeviceList()){
//                    deviceNameArray[index] = device.deviceName;
//                    deviceArray[index] = device;
//                    ++index;
//                }
//
//                ArrayList<String> listNameArray = new ArrayList<String>();
//                for (String name : deviceNameArray)
//                    if (name != null) {
//                        listNameArray.add(name);
//                    }
//                ArrayList<WifiP2pDevice> listDeviceArray = new ArrayList<WifiP2pDevice>();
//                for (WifiP2pDevice device : deviceArray)
//                    if (device != null) {
//                        listDeviceArray.add(device);
//                    }
//                deviceNameArray = listNameArray.toArray(new String[listNameArray.size()]);
//                deviceArray = listDeviceArray.toArray(new WifiP2pDevice[listDeviceArray.size()]);
//
//                ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, deviceNameArray);
//                listView.setAdapter(adapter);
//
//                if(peers.size() == 0){
//                    connectionStatus.setText("No se encontraron dispositivos");
//                    return;
//                }
//
//            }
//        }
//    };

//    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
//        @Override
//        public void onConnectionInfoAvailable(WifiP2pInfo info) {
//            final InetAddress groupOwnerAddress = info.groupOwnerAddress;
//            if(info.groupFormed && info.isGroupOwner){
//                connectionStatus.setText("Anfitrión");
//                isHost = true;
//                serverClass = new ServerClass();
//                serverClass.start();
//            } else if(info.groupFormed){
//                connectionStatus.setText("Cliente");
//                isHost = false;
//                clientClass = new ClientClass(groupOwnerAddress);
//                clientClass.start();
//            }
//        }
//    };

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

//    public class ServerClass extends Thread{
//        ServerSocket serverSocket;
//        private InputStream inputStream;
//        private OutputStream outputStream;
//
//        public void write(byte[] bytes){
//            try {
//                outputStream.write(bytes);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        @Override
//        public void run(){
//            try {
//                serverSocket = new ServerSocket(8888);
//                socket = serverSocket.accept();
//                inputStream = socket.getInputStream();
//                outputStream = socket.getOutputStream();
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            ExecutorService executorService = Executors.newSingleThreadExecutor();
//            final Handler handler = new Handler(Looper.getMainLooper());
//
//            executorService.execute(new Runnable() {
//                @Override
//                public void run() {
//                    final byte[] buffer = new byte[1024];
//                    int bytes;
//
//                    while (socket !=null){
//                        try {
//                            bytes = inputStream.read(buffer);
//                            if (bytes>0){
//                                final int finalBytes = bytes;
//                                handler.post(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        String tempMsg = new String(buffer, 0, finalBytes);
//                                        messageTextView.setText(tempMsg);
//                                    }
//                                });
//                            }
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            });
//
//
//        }
//    }

//    public class ClientClass extends Thread{
//        String hostAdd;
//        private InputStream inputStream;
//        private OutputStream outputStream;
//
//        public ClientClass(InetAddress hostAddress){
//            hostAdd = hostAddress.getHostAddress();
//            socket = new Socket();
//        }
//
//        public void write(byte[] bytes){
//            try {
//                outputStream.write(bytes);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        @Override
//        public void run(){
//            try {
//                socket.connect(new InetSocketAddress(hostAdd, 8888), 500);
//                inputStream = socket.getInputStream();
//                outputStream = socket.getOutputStream();
//
//                ExecutorService executor = Executors.newSingleThreadExecutor();
//                final Handler handler = new Handler(Looper.getMainLooper());
//
//                executor.execute(new Runnable() {
//                    @Override
//                    public void run() {
//                        final byte[] buffer = new byte[1024];
//                        int bytes;
//
//                        while (socket != null){
//                            try {
//                                bytes = inputStream.read(buffer);
//                                if(bytes>0){
//                                    final int finalBytes = bytes;
//                                    handler.post(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            String tempMSG = new String(buffer, 0, finalBytes);
//                                            messageTextView.setText(tempMSG);
//                                        }
//                                    });
//                                }
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                });
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
}
