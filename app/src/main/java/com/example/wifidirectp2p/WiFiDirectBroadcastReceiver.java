package com.example.wifidirectp2p;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
    private WifiP2pManager p2pManager;
    private WifiP2pManager.Channel channel;
    private MainActivity activity;

    public WiFiDirectBroadcastReceiver(WifiP2pManager p2pManager, WifiP2pManager.Channel channel, MainActivity activity) {
        this.p2pManager = p2pManager;
        this.channel = channel;
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)){
            //check to see if Wi-Fi is enabled
        }else if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action))
        {
            //call Wifip2pmanager.requestpeers() to get list of current peers
            if(p2pManager != null){
                p2pManager.requestPeers(channel, activity.peerListListener);
            } else {
                Log.e("MANAGER NULO", "EL MANAGER NO ESTÁ INICIALIZADO");
            }
        } else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action))
        {
            //responde to new connection or disconnections
            if(p2pManager != null){
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                if(networkInfo.isConnected()){
                    p2pManager.requestConnectionInfo(channel,activity.connectionInfoListener);
                } else{
                    activity.connectionStatus.setText("No se ha podido conectar");
                }
            }
        }

    }
}
