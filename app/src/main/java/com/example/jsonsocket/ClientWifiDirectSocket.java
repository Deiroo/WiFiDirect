package com.example.jsonsocket;

import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientWifiDirectSocket extends Thread {
    //CAPAZ EL SOCKET DEBA SER COMPARTIDO
    String hostAdd;
    private InputStream inputStream;
    private OutputStream outputStream;
    Socket socket;

    TextView messageTextView = null;


    public ClientWifiDirectSocket(InetAddress hostAddress, TextView messageTextView){
        hostAdd = hostAddress.getHostAddress();
        socket = new Socket();
        this.messageTextView = messageTextView;
    }

    public void write(byte[] bytes){
        try {
            outputStream.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run(){
        try {
            socket.connect(new InetSocketAddress(hostAdd, 8888), 500);
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();

            ExecutorService executor = Executors.newSingleThreadExecutor();
            final Handler handler = new Handler(Looper.getMainLooper());

            executor.execute(new Runnable() {
                @Override
                public void run() {
                    final byte[] buffer = new byte[1024];
                    int bytes;

                    while (socket != null){
                        try {
                            bytes = inputStream.read(buffer);
                            if(bytes>0){
                                final int finalBytes = bytes;
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        String tempMSG = new String(buffer, 0, finalBytes);
                                        if(messageTextView != null) {
                                            messageTextView.setText(tempMSG);
                                        }
                                    }
                                });
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}