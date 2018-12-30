package project.test.mk.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

public class MenuActivity extends AppCompatActivity {

    private Button btn1, btn2, btn3, btnSettings;
    private TextView textbtStat;
    private Spinner spinner_profil;

    private BTMsgHandler btMsgHandler;
    private BTManager btManager;

    private SharedPreferences prefAddress, prefProfil;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        btn1 = (Button)findViewById(R.id.button1);
        btn2 = (Button)findViewById(R.id.button2);
        btn3 = (Button)findViewById(R.id.button3);
        btnSettings = (Button)findViewById(R.id.buttonSettings);
        textbtStat = (TextView)findViewById(R.id.textViewConnStat);
        spinner_profil = (Spinner)findViewById(R.id.spinner_MProfil);

        prefAddress = getSharedPreferences("BTAddress", MODE_PRIVATE);
        prefProfil = getSharedPreferences("Profil", MODE_PRIVATE);

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

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNK();
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
                    textbtStat.setText("No Connection");
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

        btconn(getAddress());

        ArrayAdapter<CharSequence> adapterBedingung = ArrayAdapter.createFromResource(this, R.array.profil, android.R.layout.simple_spinner_item);
        adapterBedingung.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_profil.setAdapter(adapterBedingung);
        spinner_profil.setSelection(0);
        getProfil(0);
        spinner_profil.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                getProfil(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }



    //Bluetooth

    private void btconn(String btInfo){
        try{
            String address = btInfo.substring(btInfo.length() - 17);
            btManager.connect(address);
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
        Intent intent = new Intent(this, NKActivity.class);
        startActivity(intent);

    }

    private void openKalibrierung(){
        Intent intent = new Intent(this, KalibrierungActivity.class);
        startActivity(intent);
    }



    //Get-Set Methoden

    private String getAddress(){
        return prefAddress.getString("Address", null);
    }

    private void getProfil(int key){
        Gson gson = new Gson();
        String json = prefProfil.getString(""+key, "");
        Profil profil = gson.fromJson(json, Profil.class);
        if (profil != null) {
            double d = profil.getgMax();
            SharedPreferences.Editor editor = prefAddress.edit();
            editor.putString("GMax", ""+d);
            editor.commit();
        }else{
            Toast.makeText(getApplicationContext(), "Profil keine Kalibrierung", Toast.LENGTH_LONG).show();

        }

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

}
