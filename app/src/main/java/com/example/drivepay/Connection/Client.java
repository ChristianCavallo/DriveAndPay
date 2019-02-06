/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.drivepay.Connection;

import android.os.Handler;
import android.os.Looper;

import org.apache.commons.lang3.SerializationUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

import SerializedObjects.Command;
import SerializedObjects.CommandsEnum;

/**
 *
 * @author Christian
 */
public class Client extends Thread {

    private final String ip;
    private final Integer port;
    private final AES encryption;

    private Socket sock;

    private ArrayList<OnCommandReceivedListener> listeners= new ArrayList<>();

    public Client(String ip, Integer port) {

        this.ip = ip;
        this.port = port;
        this.encryption = new AES("FB26E3BE8631A6A5");
        autoconnect();
    }

    private void autoconnect() {
        final Client t = this;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (sock == null) {
                        System.out.println("Non connesso al server!");
                        try {
                            sock = new Socket(ip, port);
                            sock.setKeepAlive(true);
                            new Thread(t).start();
                            System.out.println("Connessione Stabilita!");
                        } catch (IOException ex) {

                        }
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }


            }
        }).start();

    }

    @Override
    public void run() {

        try {
          SendCommand(new Command(CommandsEnum.CommandType.HANDSHAKE_REQUEST, null));
          DataInputStream inData = new DataInputStream(this.sock.getInputStream());
            while (true) {
                byte[] data;

                int count = inData.readInt();
                data = new byte[count];
                inData.readFully(data);

                if (count == -1) {
                    this.sock.getInputStream().close();
                    this.sock.close();
                    break;
                }

                data = encryption.decryptAES(data);
                Command cmd = SerializationUtils.deserialize(data);
                executeCommand(cmd);
                notifyListeners(cmd);
                System.out.println("Ho ricevuto un messaggio: " + cmd.toString());

            }
        } catch (IOException e) {
            System.out.println("Errore nel test client socket: " + e.getMessage());
        } catch (Exception ex) {
            System.out.println("Errore nel test client socket: " + ex.getMessage());
        }
        try {
            this.sock.close();
        } catch (Exception e) {

        }

        this.sock = null;
        Thread.currentThread().interrupt();
    }

    public void notifyListeners(Command c) {
        final Command cmd = c;
        Handler mainHandler = new Handler(Looper.getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                if (listeners.size() > 0) {
                    for (Iterator iterator = listeners.iterator(); iterator.hasNext(); ) {
                        OnCommandReceivedListener next = (OnCommandReceivedListener) iterator.next();
                        if (next != null) {
                            next.OnCommandReceivedCallback(cmd);
                        } else {
                            listeners.remove(next);
                        }
                    }
                }
            }
        };
        mainHandler.post(myRunnable);
    }
    public void RecoverConnection(){
        while(true){
            try {
                if (this.sock != null) {
                    this.sock.close();
                }
                Thread.sleep(1000);
                this.sock = new Socket(this.ip, this.port);
                this.sock.setKeepAlive(true);
                new Thread(this).start();
                break;
            } catch (IOException ex) {
                System.out.println("Errore di connessione: " + ex.getMessage());

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Thread.currentThread().interrupt();
    }

    public void SendCommand(Command cmd) {
        try {
            DataOutputStream out = new DataOutputStream(sock.getOutputStream());
            byte[] data = SerializationUtils.serialize(cmd);
            data = encryption.encryptAES(data);
            out.writeInt(data.length);
            out.write(data);
            out.flush();
            System.out.println("Ho inviato " + data.length + " bytes al server: " + cmd.toString());
        } catch (IOException ex) {
            System.out.println(ex);
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    public void SendCommandAsync(final Command cmd, OnCommandReceivedListener listener){
        if (this.sock == null) {
            System.out.println("Non sono connesso!");
            return;
        }

        if (!this.sock.isConnected() || this.sock.isClosed()) {
            return;
        }

        new Thread(new Runnable() {

            @Override
            public void run() {

                SendCommand(cmd);
            }
        }).start();
        registerListenerByCommand(listener);

    }

    private void registerListenerByCommand(OnCommandReceivedListener mListener)
    {
        if(!listeners.contains(mListener)){
            listeners.add(mListener);
        }
    }

    public void UnregisterListener(OnCommandReceivedListener listener){
        listeners.remove(listener);
    }

    public void executeCommand(Command cmd) {

        switch (cmd.getType()) {
            case HANDSHAKE_RESPONSE:
                System.out.println("Handshaked");
                break;
            default:
                // System.out.println("Received an unknown command: " + cmd.toString());

        }

    }

}
