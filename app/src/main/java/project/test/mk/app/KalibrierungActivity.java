/*



*/
package project.test.mk.app;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;

public class KalibrierungActivity extends AppCompatActivity implements SensorEventListener, AdapterView.OnItemSelectedListener {

    private EditText texte_name;
    private TextView textv_temperatur, textv_gWert, textv_messung, textv_BT;
    private Button btn_messung;
    private Spinner spinner_profil, spinner_bedingung;

    private Sensor sensor;
    private SensorManager sensorManager;

    private BTManager btManager;
    private BTMsgHandler btMsgHandler;

    private SharedPreferences pref;

    private boolean messungLauft;
    private int strassenBedPos;
    private double aktTemperatur;

    int count = 0;
    double gAdd = 0;
    double aMax = 0;
    ArrayList<Double> gDurchsArray = new ArrayList<>();

    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kalibrierung);

        texte_name = (EditText)findViewById(R.id.txte_Kaliname);
        textv_gWert = (TextView)findViewById(R.id.txtv_KaligWert);
        textv_temperatur = (TextView)findViewById(R.id.txtv_KaliTemperatur);
        Button btn_save = (Button) findViewById(R.id.btn_Kalisave);
        spinner_bedingung = (Spinner)findViewById(R.id.spinner_KalistraßenBedingung);
        spinner_profil = (Spinner)findViewById(R.id.spinner_Kaliprofil);
        textv_messung = (TextView)findViewById(R.id.txtv_Kalimessung);
        textv_BT = (TextView)findViewById(R.id.txtv_KaliBT);
        btn_messung = (Button)findViewById(R.id.btn_Kalimessung);

        pref = getSharedPreferences("Profil", MODE_PRIVATE);

        actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);

        //BT Verbindung

        btMsgHandler = new BTMsgHandler() {
            @Override
            void receiveMessage(String msg) {
                if (msg.charAt(0) == 'E'){
                    String stringTemperatur = msg.substring(1, msg.length()-1);
                    aktTemperatur = Double.parseDouble(stringTemperatur);
                }

            }

            @Override
            void receiveConnectStatus(boolean isConnected) {
                if (isConnected){
                    textv_BT.setText("Connected");
                    textv_BT.setTextColor(Color.parseColor("#01DF01"));

                }else{
                    textv_BT.setText("No Connection");
                    textv_BT.setTextColor(Color.parseColor("#d60000"));
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

        btConn();

        // Sensor
        messungLauft = false;
        sensorManager = (SensorManager) getSystemService (SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener( this, sensor, SensorManager.SENSOR_DELAY_GAME);
        gDurchsArray.add(0.0);

        // Spinner Profil
        ArrayAdapter<CharSequence> adapterProfil = ArrayAdapter.createFromResource(this, R.array.profil, R.layout.spinner_item);
        adapterProfil.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_profil.setAdapter(adapterProfil);
        spinner_profil.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setUI(getProfil(id));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //Spinner Bedingungen
        ArrayAdapter<CharSequence> adapterBedingung = ArrayAdapter.createFromResource(this, R.array.straßenBedingungen, R.layout.spinner_item);
        adapterBedingung.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_bedingung.setAdapter(adapterBedingung);
        spinner_bedingung.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                strassenBedPos = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



        // Button Save
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendPostRequest();
                saveProfil();

            }
        });

        btn_messung.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!messungLauft){
                    messungLauft = true;
                    btn_messung.setText("Messung Stop");
                }else{
                    messungLauft = false;
                    textv_messung.setText("akt. Messung: keine Messung");
                    btn_messung.setText("Messung Start");
                }
            }
        });


    }// ENDE ON-CREATE

    @Override
    public boolean onSupportNavigateUp() {
        btManager.cancel();
        Intent i = new Intent(this, MenuActivity.class);
        startActivity(i);
        return true;
    }


    // BT Connection aufbauen
    private void btConn(){
        SharedPreferences btPref = getSharedPreferences("KeyValues", MODE_PRIVATE);
        String address = btPref.getString("Address", null);
        try {
            btManager.connect(address.substring(address.length() - 17));
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), "btconn error", Toast.LENGTH_LONG).show();

        }

    }


    // Spinner Bedingung
    private void spinnerBed(int pos){
        strassenBedPos = pos;
    }




    // Sensor
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (messungLauft) {
            double x = event.values[0];
            count++;
            aMax = kalibrierungAMax(x);
            String roundGMax = roundAndFormat(aMax / 9.81, 4);
            textv_gWert.setText("max G-Wert: "+roundGMax);
            String roundMessung = roundAndFormat(Math.abs(x) / 9.81, 4);
            textv_messung.setText("akt. Messung: "+roundMessung);
            textv_temperatur.setText("akt. Außentemperatur: "+aktTemperatur+"°C");
        }
    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //Messung
    private double kalibrierungAMax(double x) {
        double gDurchs;
        gAdd = gAdd + Math.abs(x);
        if (count == 10) {
            gDurchs = gAdd / count;
            gDurchsArray.add(gDurchs);
            count = 0;
            gDurchs = 0;
            gAdd = 0;
            aMax = Collections.max(gDurchsArray);
        }
        return aMax;
    }


    //Profil
    private void saveProfil() {
        Boolean save_success;
        String name = texte_name.getText().toString();
        long key = spinner_profil.getSelectedItemId();
        Profil p1 = new Profil(name, strassenBedPos, aktTemperatur, aMax);

        try {
            SharedPreferences.Editor edit = pref.edit();
            Gson gson = new Gson();
            String json = gson.toJson(p1);
            edit.putString(String.valueOf(key), json);
            edit.commit();
            save_success = true;
            Toast.makeText(getApplicationContext(), "write successful", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            save_success = false;
            Toast.makeText(getApplicationContext(), "write error", Toast.LENGTH_LONG).show();
        }
        if (save_success){
            aMax = 0;
            gDurchsArray.clear();
        }
    }

    private void sendPostRequest(){
        SendPostRequest sendPostRequest = new SendPostRequest();
        sendPostRequest.setProfilNr(spinner_profil.getSelectedItemPosition()+1);
        sendPostRequest.setgMax(aMax/9.81);
        sendPostRequest.execute();
    }

    private Profil getProfil(long key){
        Gson gson = new Gson();
        String json = pref.getString(String.valueOf(key), null);
        Profil profil = gson.fromJson(json, Profil.class);
       if (profil != null) {
            return profil;
       }else{
            profil = new Profil("Neues Profil",0,99,0);
            return profil;
       }
    }

    private void setUI(Profil profil){
        try {
            String name = profil.getName();
            double temperatur = profil.getTemperatur();
            double gWert = profil.getgMax();
            String stringgWert = roundAndFormat(gWert / 9.81, 4);
            int sb = profil.getStraßenbedingung();

            if (texte_name!=null) {
                texte_name.setText(name);
                textv_temperatur.setText("gespeicherte Außentemperatur: "+temperatur+"°C");
                textv_gWert.setText("gespeicherter max G-Wert: "+stringgWert);
                spinner_bedingung.setSelection(sb);
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "setUI error", Toast.LENGTH_LONG).show();

        }
    }

    // Extra Methoden
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private String roundAndFormat(final double value, final int frac) {
        final java.text.NumberFormat nf = java.text.NumberFormat.getInstance();
        nf.setMaximumFractionDigits(frac);
        return nf.format(new BigDecimal(value));
    }

}
