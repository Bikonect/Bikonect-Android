package com.eduaraujodev.test_bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;

public class ConnectionThread extends Thread{

    BluetoothSocket btSocket = null;
    BluetoothServerSocket btServerSocket = null;
    InputStream input = null;
    OutputStream output = null;
    String btDevAddress = null;
    String myUUID = "00001101-0000-1000-8000-00805F9B34FB";
    boolean server;
    boolean running = false;
    boolean isConnected = false;

    public ConnectionThread() {
        this.server = true;
    }

    public ConnectionThread(String btDevAddress) {
        this.server = false;
        this.btDevAddress = btDevAddress;
    }

    public void run() {
        this.running = true;
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

        if(this.server) {
            try {
                btServerSocket = btAdapter.listenUsingRfcommWithServiceRecord("Super Counter", UUID.fromString(myUUID));
                btSocket = btServerSocket.accept();

                if(btSocket != null) {
                    btServerSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                toMainActivity("---N".getBytes());
            }
        } else {
            try {
                BluetoothDevice btDevice = btAdapter.getRemoteDevice(btDevAddress);
                btSocket = btDevice.createRfcommSocketToServiceRecord(UUID.fromString(myUUID));
                btAdapter.cancelDiscovery();

                if (btSocket != null) {
                    btSocket.connect();
                }

            } catch (IOException e) {
                e.printStackTrace();
                toMainActivity("---N".getBytes());
            }
        }

        if(btSocket != null) {
            this.isConnected = true;
            toMainActivity("---S".getBytes());

            try {
                input = btSocket.getInputStream();
                output = btSocket.getOutputStream();

                while(running) {
                    byte[] buffer = new byte[1024];
                    int bytes;
                    int bytesRead = -1;

                    do {
                        bytes = input.read(buffer, bytesRead+1, 1);
                        bytesRead+=bytes;
                    } while(buffer[bytesRead] != '\n');

                    toMainActivity(Arrays.copyOfRange(buffer, 0, bytesRead-1));
                }
            } catch (IOException e) {
                e.printStackTrace();
                toMainActivity("---N".getBytes());
                this.isConnected = false;
            }
        }

    }

    private void toMainActivity(byte[] data) {
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putByteArray("data", data);
        message.setData(bundle);
        MainActivity.handler.sendMessage(message);
    }

    public void write(int dado) {
        if(output != null) {
            try {
                output.write(13);
                output.write(dado);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            toMainActivity("---N".getBytes());
        }
    }

    public void cancel() {

        try {
            running = false;
            this.isConnected = false;
            btServerSocket.close();
            btSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        running = false;
        this.isConnected = false;
    }

    public boolean isConnected() {
        return this.isConnected;
    }
}