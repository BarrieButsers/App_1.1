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
    private TextView txt_btStat;
    private Spinner spinner_profil;

    private BTMsgHandler btMsgHandler;
    private BTManager btManager;

    private SharedPreferences prefKeyValues, prefProfil;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        btn1 = (Button)findViewById(R.id.btn_MenuVK);
        btn2 = (Button)findViewById(R.id.btn_MenuNK);
        btn3 = (Button)findViewById(R.id.button3);
        btnSettings = (Button)findViewById(R.id.btn_MenuKalibrierung);
        txt_btStat = (TextView)findViewById(R.id.txtv_MenuBTStat);
        spinner_profil = (Spinner)findViewById(R.id.spinner_MenuProfil);

        prefKeyValues = getSharedPreferences("KeyValues", MODE_PRIVATE);
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
                    txt_btStat.setText("Connected");
                }else{
                    txt_btStat.setText("No Connection");
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
        spinner_profil.setSelection(getProfilPos());
        getProfil(spinner_profil.getSelectedItemId());
        spinner_profil.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                getProfil(id);
                setProfilpos(position);
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
        btManager.cancel();
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void openVK(){
        btManager.cancel();
        Intent intent = new Intent(this, VKActivity.class);
        startActivity(intent);
    }

    private void openNK(){
        btManager.cancel();
        Intent intent = new Intent(this, NKActivity.class);
        startActivity(intent);

    }

    private void openKalibrierung(){
        btManager.cancel();
        Intent intent = new Intent(this, KalibrierungActivity.class);
        startActivity(intent);
    }



    //Get-Set Methoden

    private String getAddress(){
        return prefKeyValues.getString("Address", null);
    }

    private void setProfilpos(int pos){
        SharedPreferences.Editor edit = prefKeyValues.edit();
        edit.putInt("ProfilPos", pos);
        edit.apply();
    }

    private int getProfilPos(){
        return prefKeyValues.getInt("ProfilPos",0);
    }

    private void getProfil(long key){
        Gson gson = new Gson();
        String json = prefProfil.getString(String.valueOf(key), null);
        Profil profil = gson.fromJson(json, Profil.class);
        if (profil != null) {
            double d = profil.getgMax();
            SharedPreferences.Editor editor = prefKeyValues.edit();
            editor.putString("GMax", ""+d);
            editor.commit();
        }else{
            Toast.makeText(getApplicationContext(), "Profil nicht kalibriert", Toast.LENGTH_LONG).show();

        }

    }


}
