package com.example.jsonsocket;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WifiDirectConnector {
    //TextView connectionStatus, messageTextView;
//    Button aSwitch, discoverButton;
    //ListView listView;
//    EditText typeMsg;
//    ImageButton sendButton;

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;

    //BroadcastReceiver receiver;
    //IntentFilter intentFilter;

    private List<WifiP2pDevice> peers = new ArrayList<>();
    private String[] deviceNameArray;
    private WifiP2pDevice[] deviceArray;


    private ServerWifiDirectSocket serverClass;
    private ClientWifiDirectSocket clientClass;

    private boolean isHost;

    private WifiP2pManager.PeerListListener peerListListener;

    private WifiP2pManager.ConnectionInfoListener connectionInfoListener;

    //start getters and setters
    public WifiP2pManager getManager() {
        return manager;
    }

    public void setManager(WifiP2pManager manager) {
        this.manager = manager;
    }

    public WifiP2pManager.Channel getChannel() {
        return channel;
    }

    public void setChannel(WifiP2pManager.Channel channel) {
        this.channel = channel;
    }

    public List<WifiP2pDevice> getPeers() {
        return peers;
    }

    public void setPeers(List<WifiP2pDevice> peers) {
        this.peers = peers;
    }

    public String[] getDeviceNameArray() {
        return deviceNameArray;
    }

    public void setDeviceNameArray(String[] deviceNameArray) {
        this.deviceNameArray = deviceNameArray;
    }

    public WifiP2pDevice[] getDeviceArray() {
        return deviceArray;
    }

    public void setDeviceArray(WifiP2pDevice[] deviceArray) {
        this.deviceArray = deviceArray;
    }

    public ServerWifiDirectSocket getServerClass() {
        return serverClass;
    }

    public void setServerClass(ServerWifiDirectSocket serverClass) {
        this.serverClass = serverClass;
    }

    public ClientWifiDirectSocket getClientClass() {
        return clientClass;
    }

    public void setClientClass(ClientWifiDirectSocket clientClass) {
        this.clientClass = clientClass;
    }

    public boolean isHost() {
        return isHost;
    }

    public void setHost(boolean host) {
        isHost = host;
    }

    public WifiP2pManager.PeerListListener getPeerListListener() {
        return peerListListener;
    }

    public void setPeerListListener(WifiP2pManager.PeerListListener peerListListener) {
        this.peerListListener = peerListListener;
    }


    public WifiP2pManager.ConnectionInfoListener getConnectionInfoListener() {
        return connectionInfoListener;
    }

    public void setConnectionInfoListener(WifiP2pManager.ConnectionInfoListener connectionInfoListener) {
        this.connectionInfoListener = connectionInfoListener;
    }

    //end getters and setters

    public void discoverPeers(final TextView connectionStatus) {
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                try{
                    connectionStatus.setText("Se está buscando dispositivos");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int reason) {
                connectionStatus.setText("No se realizó el descubrimiento");
            }
        });
    }

    public void connectDevice(final TextView connectionStatus, WifiP2pConfig config, final WifiP2pDevice device) {
        manager.connect(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                connectionStatus.setText("Dispositivo conectado: " + device.deviceName);
            }

            @Override
            public void onFailure(int reason) {
                connectionStatus.setText("No se pudo realizar la conexión");

            }
        });
    }

    public void sendMessage(final String message) {
        //ACA SE ENVIAN LOS MENSAJES
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                if (message != null && isHost){
                    serverClass.write(message.getBytes());
                } else if (message != null && !isHost){
                    clientClass.write(message.getBytes());
                }
            }
        });
    }

    public WifiP2pManager.PeerListListener initPeerListListener(final ListView listView, final TextView connectionStatus, final Context applicationContext) {
        return new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
                Collection<WifiP2pDevice> listDevices = wifiP2pDeviceList.getDeviceList();
                if(!listDevices.equals(peers)){
                    peers.clear();
                    peers.addAll(wifiP2pDeviceList.getDeviceList());

                    deviceNameArray = new String[wifiP2pDeviceList.getDeviceList().size()];
                    deviceArray = new WifiP2pDevice[wifiP2pDeviceList.getDeviceList().size()];

                    int index = 0;
                    for(WifiP2pDevice device : wifiP2pDeviceList.getDeviceList()){
                        deviceNameArray[index] = device.deviceName;
                        deviceArray[index] = device;
                        ++index;
                    }

                    ArrayList<String> listNameArray = new ArrayList<String>();
                    for (String name : deviceNameArray)
                        if (name != null) {
                            listNameArray.add(name);
                        }
                    ArrayList<WifiP2pDevice> listDeviceArray = new ArrayList<WifiP2pDevice>();
                    for (WifiP2pDevice device : deviceArray)
                        if (device != null) {
                            listDeviceArray.add(device);
                        }
                    deviceNameArray = listNameArray.toArray(new String[listNameArray.size()]);
                    deviceArray = listDeviceArray.toArray(new WifiP2pDevice[listDeviceArray.size()]);

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(applicationContext, android.R.layout.simple_list_item_1, deviceNameArray);
                    if(listView!=null) {
                        listView.setAdapter(adapter);
                    }

                    if(peers.size() == 0){
                        if(connectionStatus!=null) {
                            connectionStatus.setText("No se encontraron dispositivos");
                        }
                        return;
                    }

                }
            }
        };
    }

    public WifiP2pManager.ConnectionInfoListener initConnectionInfoListener(final TextView connectionStatus, final TextView messageTextView) {
        return new WifiP2pManager.ConnectionInfoListener() {
            @Override
            public void onConnectionInfoAvailable(WifiP2pInfo info) {
//                final InetAddress groupOwnerAddress = info.groupOwnerAddress;
//                if(info.groupFormed && info.isGroupOwner){
//                    if(connectionStatus!=null) {
//                        connectionStatus.setText("Anfitrión");
//                    }
//
//                    isHost = true;
//                    serverClass = serverClassAct;
//                    serverClass.start();
//                } else if(info.groupFormed){
//                    if(connectionStatus!=null) {
//                        connectionStatus.setText("Cliente");
//                    }
//                    isHost = false;
//                    clientClass = clientClassAct;
//                    clientClass.start();
//                }
                final InetAddress groupOwnerAddress = info.groupOwnerAddress;
                if(info.groupFormed && info.isGroupOwner){
                    connectionStatus.setText("Anfitrión");
                    isHost = true;
                    serverClass = new ServerWifiDirectSocket();
                    serverClass.start();
                } else if(info.groupFormed){
                    connectionStatus.setText("Cliente");
                    isHost = false;
                    clientClass = new ClientWifiDirectSocket(groupOwnerAddress, messageTextView);
                    clientClass.start();
                }
            }
        };
    }


}
