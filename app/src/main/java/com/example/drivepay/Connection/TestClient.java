/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.drivepay.Connection;

import org.apache.commons.lang3.SerializationUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

import SerializedObjects.Command;
import SerializedObjects.CommandsEnum;
import SerializedObjects.UserObjects.InformazioniUtente;

/**
 *
 * @author Christian
 */
public class TestClient extends Thread {

    private final String ip;
    private final Integer port;
    private final AES encryption;

    private Socket sock;

    private ArrayList<OnCommandReceivedListener> listeners= new ArrayList<>();

    public TestClient(String ip, Integer port) {

        this.ip = ip;
        this.port = port;
        this.encryption = new AES("FB26E3BE8631A6A5");

        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            this.sock = new Socket(this.ip, this.port);
            this.sock.setKeepAlive(true);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
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
                if(listeners.size() > 0) {
                    for (Iterator iterator = listeners.iterator(); iterator.hasNext(); ) {
                        OnCommandReceivedListener next = (OnCommandReceivedListener) iterator.next();
                        if (next != null) {
                            next.OnCommandReceivedCallback(cmd);
                        } else {
                            listeners.remove(next);
                        }
                    }
                }
                System.out.println("Ho ricevuto un messaggio: " + cmd.toString());

            }
        } catch (IOException e) {
            System.out.println("Errore nel test client socket: " + e.getMessage());
        } catch (Exception ex) {
            System.out.println("Errore nel test client socket: " + ex.getMessage());
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                RecoverConnection();
            }
        }).start();
    }

    public void RecoverConnection(){
        while(true){
            try {
                Thread.sleep(5000);
                this.sock = new Socket(this.ip, this.port);
                this.sock.setKeepAlive(true);
                new Thread(this).start();
                break;
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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
        System.out.println("Sending Async command");
        new Thread(new Runnable() {

            @Override
            public void run() {

                SendCommand(cmd);
            }
        }).start();
        registerListenerByCommand(listener);
        System.out.println("Listener registered: " + listeners.size());
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
            case LOGIN_RESPONSE:
                InformazioniUtente U = (InformazioniUtente) cmd.getArg();
                break;
            case UPLOAD_DOCUMENT_RESPONSE:
                break;
            default:
                System.out.println("Received an unknown command: " + cmd.toString());

        }

    }

}
