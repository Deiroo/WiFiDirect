package com.example.jsonsocket;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.TextView;

import com.example.jsonsocket.interfaces.IPeerListReceivers;
import com.example.wifidirectp2p.MainActivity;

public class WifiDirectReceiver extends BroadcastReceiver {
    private WifiP2pManager p2pManager;
    private WifiP2pManager.Channel channel;
    private WifiP2pManager.PeerListListener peerListListener;
    private WifiP2pManager.ConnectionInfoListener connectionInfoListener;
    private TextView connectionStatus;

    public WifiDirectReceiver(WifiP2pManager p2pManager, WifiP2pManager.Channel channel,
                              WifiP2pManager.PeerListListener peerListListener,
                              WifiP2pManager.ConnectionInfoListener connectionInfoListener,
                              TextView connectionStatus) {
        this.p2pManager = p2pManager;
        this.channel = channel;
        //Deshabilitado, ver como habilitarlo luego
        this.peerListListener = peerListListener;
        this.connectionStatus = connectionStatus;
        this.connectionInfoListener = connectionInfoListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)){
            //checr si wifi esta habilitadp
        }else if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action))
        {
            //call Wifip2pmanager.requestpeers() obtener la lista de los actuales pares
            if(p2pManager != null){
                //Interfaz, probar luego si funciona
                p2pManager.requestPeers(channel, peerListListener);
            } else {
                Log.e("MANAGER NULO", "EL MANAGER NO ESTÁ INICIALIZADO");
            }
        } else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action))
        {
            //responde a las nuevas conexiones o desconexiones
            if(p2pManager != null){
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                if(networkInfo.isConnected()){
                    //Interfaz, probar luego si funciona
                    p2pManager.requestConnectionInfo(channel, connectionInfoListener);
                } else{
                    //Interfaz, probar luego si funciona
                    //.setText("No se ha podido conectar");
                    if(connectionStatus!=null) {
                        connectionStatus.setText("No se ha podido conectar");
                    }
                }
            }
        }

    }
}
