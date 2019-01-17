package project.test.mk.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity {

    private BTManager btManager;

    private TextView textViewBTStatus;

    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        textViewBTStatus = (TextView)findViewById(R.id.txtv_BTbtStatus);
        ListView btList = (ListView) findViewById(R.id.list_BTDevice);

        pref = getSharedPreferences("KeyValues", MODE_PRIVATE);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);

        BTMsgHandler btMsgHandler = new BTMsgHandler() {
            @Override
            void receiveMessage(String msg) {

            }

            @Override
            void receiveConnectStatus(boolean isConnected) {
                if (isConnected) {
                    textViewBTStatus.setText("Connected");
                    textViewBTStatus.setTextColor(Color.parseColor("#01DF01"));
                } else {
                    textViewBTStatus.setText("No Connection");
                    textViewBTStatus.setTextColor(Color.parseColor("#d60000"));
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


        ArrayList listTemp = btManager.getPairedDeviceInfos();
        if (listTemp.size()>0) {
            final ArrayAdapter adapter = new ArrayAdapter(this,R.layout.list_item, listTemp);
            btList.setAdapter(adapter);
            btList.setOnItemClickListener(myListClickListener); //Method called when the device from the list is clicked
        } else {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        btManager.cancel();
        Intent i = new Intent(this, MenuActivity.class);
        startActivity(i);
        return true;
    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener()
    {
        public void onItemClick (AdapterView av, View v, int arg2, long arg3)
        {
            String info = ((TextView) v).getText().toString();
            writeListPerm(info);
            String address = info.substring(info.length() - 17);
            textViewBTStatus.setText("Connecting...");
            btManager.connect(address);
        }
    };

    private void writeListPerm(String info){
        try {
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("Address", info);
            editor.commit();
        } catch (Exception ex) {
                ex.printStackTrace();
        }
    }
}
