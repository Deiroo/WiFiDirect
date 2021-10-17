package com.example.jsonsocket;

import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import com.example.jsonsocket.jsonsEntities.JsonEntity;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerWifiDirectSocket extends Thread {

    Socket socket;
    ServerSocket serverSocket;
    private InputStream inputStream;
    private OutputStream outputStream;

    Gson gson = new Gson();

    TextView messageTextView = null;

    public ServerWifiDirectSocket() {
    }

    public ServerWifiDirectSocket(TextView messageTextView) {
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
            serverSocket = new ServerSocket(8888);
            socket = serverSocket.accept();
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();

        } catch (IOException e) {
            e.printStackTrace();
        }

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        final Handler handler = new Handler(Looper.getMainLooper());

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                final byte[] buffer = new byte[1024];
                int bytes;

                while (socket !=null){
                    try {
                        bytes = inputStream.read(buffer);
                        if (bytes>0){
                            final int finalBytes = bytes;
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    //ACA SE RECIBEN LOS MENSAJES DEL SERVER
                                    String tempMsg = new String(buffer, 0, finalBytes);
                                    //puede ser nulo, se puede modificar a futuro para cambiar la estructura de los mensajes
                                    //se puede convertir de texto a JSON si Fuera Necesario
                                    JsonEntity jsonEntity = gson.fromJson(tempMsg, JsonEntity.class);
                                    if (messageTextView != null) {
                                        messageTextView.setText(jsonEntity.getMessage());
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


    }
}
