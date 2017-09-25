package com.eduaraujodev.test_bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private int status = 1;
    private Button teste;
    static TextView statusMessage;
    ConnectionThread connect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        teste = (Button) findViewById(R.id.teste);
        statusMessage = (TextView) findViewById(R.id.statusMessage);

        teste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                envDados();
            }
        });

        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            statusMessage.setText("Que pena! Hardware Bluetooth não está funcionando :(");
        } else {
            statusMessage.setText("Ótimo! Hardware Bluetooth está funcionando :)");
        }

        btAdapter.enable();

        connect = new ConnectionThread("20:16:10:17:35:41");
        connect.start();

        try {
            Thread.sleep(1000);
        } catch (Exception E) {
            E.printStackTrace();
        }
    }

    public static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            Bundle bundle = msg.getData();
            byte[] data = bundle.getByteArray("data");
            String dataString= new String(data);

            if(dataString.equals("---N"))
                statusMessage.setText("Ocorreu um erro durante a conexão D:");
            else if(dataString.equals("---S"))
                statusMessage.setText("Conectado :D");
            else {
                statusMessage.setText(dataString);
            }

        }
    };

    public void envDados() {
        connect.write(status);

        if (status == 0) {
            teste.setText("Destravar");
            status = 1;
        } else {
            teste.setText("Travar");
            status = 0;
        }
    }
}
