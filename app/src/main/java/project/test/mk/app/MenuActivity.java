package project.test.mk.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.*;

import java.util.ArrayList;

public class MenuActivity extends AppCompatActivity {

    private Button btn1, btn2, btn3, btnSettings;
    private TextView textbtStat;

    private BTMsgHandler btMsgHandler;
    private BTManager btManager;

    private ArrayList listPerm;
    //private ArrayList listTemp;

    //Startup
    private static boolean startup = true;
    private String s1;

    private SharedPreferences pref;




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (startup == true){
            try {
                pref = getSharedPreferences("KeyValues", 0);
                s1 = pref.getString("Address", null);
            } catch (Exception ex) {
                Toast.makeText(getApplicationContext(), "BT Starup Error", Toast.LENGTH_LONG).show();
            }
            if(s1 != null) {
                ArrayList a1 = btManager.getPairedDeviceInfos();
                for (int i = 0; i < a1.size(); i++) {
                    if (a1.get(i).toString().equals(s1)) {
                        String address = s1.substring(s1.length() - 17);
                        btManager.connect(address);
                    }
                }
            }
        }
        startup = false;


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        btn1 = (Button)findViewById(R.id.button1);
        btn2 = (Button)findViewById(R.id.button2);
        btn3 = (Button)findViewById(R.id.button3);
        btnSettings = (Button)findViewById(R.id.buttonSettings);
        textbtStat = (TextView)findViewById(R.id.textViewConnStat);



        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openKalibrierung();
            }
        });

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openVK();
            }
        });

        btMsgHandler = new BTMsgHandler() {
            @Override
            void receiveMessage(String msg) {

            }

            @Override
            void receiveConnectStatus(boolean isConnected) {
                if (isConnected){
                    textbtStat.setText("Connected");
                }else{
                    textbtStat.setText("failed");
                }
            }

            @Override
            void handleException(Exception e) {

            }
        };

        try {
            btManager = new BTManager(this, btMsgHandler);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();

        }

        btconn();


/*
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!prefs.getBoolean("onlyonce", false)) {
            // <---- run your one time code here


            // mark once runned.
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("onlyonce", true);
            editor.commit();
        }

    }
*/




}

    /*
    public void getBTList(){
        try {
            // create a new file with an ObjectOutputStream
            FileOutputStream out = new FileOutputStream("test.txt");
            ObjectOutputStream oout = new ObjectOutputStream(out);

            // write something in the file
            oout.writeObject(s);
            oout.writeObject(i);

            // close the stream
            oout.close();

            // create an ObjectInputStream for the file we created before
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream("test.txt"));

            // read and print what we wrote before
            System.out.println("" + (String) ois.readObject());
            System.out.println("" + ois.readObject());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private void startupBTCheck(){
        ArrayList listTemp = btManager.getPairedDeviceInfos();
        if (listPerm != null){
            for (int i = 0; i<listPerm.size(); i++){
                String s1 = listPerm.get(i).toString();
                for (int o = 0; o<listPerm.size(); o++){
                    String s2 = listTemp.get(o).toString();
                    if (s1.equals(s2)){
                        String a = s1.substring(s1.length() - 17);
                        //textViewBTStatus.setText("Connecting...");
                        btManager.connect(a);
                    }
                }
            }
        }


    }

    private void getListPerm(){
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream("BTConnections.txt"));
            listPerm.add(ois.readObject());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
*/

    //Bluetooth

    private void btconn(){
        try{
            btManager.connect(getAddress());
        }catch(Exception e){
            Toast.makeText(getApplicationContext(), "BT Conn error", Toast.LENGTH_LONG).show();
        }
    }

    //BT-Settings Button

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.btbutton_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.item1:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    //Open Activity Methoden

    private void openSettings(){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void openVK(){
        Intent intent = new Intent(this, VKActivity.class);
        startActivity(intent);
    }

    private void openNK(){

    }

    private void openKalibrierung(){
        Intent intent = new Intent(this, KalibrierungActivity.class);
        startActivity(intent);
    }

    //Get-Set Methoden

    private String getAddress(){
        return pref.getString("Address", null);
    }


}
