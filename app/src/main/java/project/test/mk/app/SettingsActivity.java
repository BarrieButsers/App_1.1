package project.test.mk.app;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity {

    private BTManager btManager;
    private BTMsgHandler btMsgHandler;

    private TextView textViewBTStatus;
    private ListView btList;

    private ArrayList listTemp;
    private ArrayList listPerm;
    public static String address;

    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

       // dropdown = (Spinner)findViewById(R.id.dropdown);
        textViewBTStatus = (TextView)findViewById(R.id.textView_BTStatus);
        btList = (ListView)findViewById(R.id.list_BTDevice);

        pref = getSharedPreferences("KeyValues", 0);

        btMsgHandler = new BTMsgHandler() {
            @Override
            void receiveMessage(String msg) {

            }

            @Override
            void receiveConnectStatus(boolean isConnected) {
                if (isConnected){
                    textViewBTStatus.setText("Connected");
                    textViewBTStatus.setTextColor(0xFF3cc305);
                }else{
                    textViewBTStatus.setText("Connection failed");
                    textViewBTStatus.setTextColor(0xFFe02406);
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

/*
        //DropDown Menü
        ArrayList list = btManager.getPairedDeviceInfos();
        ArrayAdapter dropdownAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, list);
        dropdownAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdown.setAdapter(dropdownAdapter);

        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String info = parent.getItemAtPosition(position).toString();
                address = info.substring(info.length() - 17);
                btManager.connect(address);
                textViewBTStatus.setText("Connecting ...");

            } // to close the onItemSelected
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });
*/
        listTemp = btManager.getPairedDeviceInfos();
        if (listTemp.size()>0) {
            final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, listTemp);
            btList.setAdapter(adapter);
            btList.setOnItemClickListener(myListClickListener); //Method called when the device from the list is clicked
        } else {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }

    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener()
    {
        public void onItemClick (AdapterView av, View v, int arg2, long arg3)
        {
            // Get the device MAC address, the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            writeListPerm(info);
            address = info.substring(info.length() - 17);
            textViewBTStatus.setText("Connecting...");
            btManager.connect(address);
        }
    };

    // Write to BTConnections.txt
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