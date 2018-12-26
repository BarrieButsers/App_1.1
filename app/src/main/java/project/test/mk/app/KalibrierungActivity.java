/*



*/
package project.test.mk.app;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;

public class KalibrierungActivity extends AppCompatActivity implements SensorEventListener, AdapterView.OnItemSelectedListener {

    private EditText texte_name;
    private TextView textv_temperatur, textv_gWert, textv_messung;
    private Button btn_save;
    private Spinner spinner_profil, spinner_bedingung;
    private Switch switch_messung;

    private Sensor sensor;
    private SensorManager sensorManager;

    private BTManager btManager;
    private BTMsgHandler btMsgHandler;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private boolean messungLauft;
    private int strassenBedPos;
    private double save_temperatur;

    private double x,y,z;
    double gDurchs;
    int count = 0;
    double gAdd = 0;
    double gMax = 0;
    double save_gMax;
    ArrayList<Double> gDurchsArray = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kalibrierung);

        texte_name = (EditText)findViewById(R.id.texte_name);
        textv_gWert = (TextView)findViewById(R.id.textv_gWert);
        textv_temperatur = (TextView)findViewById(R.id.textv_temperatur);
        btn_save = (Button)findViewById(R.id.btn_save);
        spinner_bedingung = (Spinner)findViewById(R.id.spinner_straßenBedingung);
        spinner_profil = (Spinner)findViewById(R.id.spinner_profil);
        switch_messung = (Switch)findViewById(R.id.switch_messung);
        textv_messung = (TextView)findViewById(R.id.textv_messung);

        pref = getSharedPreferences("Profil", MODE_PRIVATE);

        //BT Verbindung

        btMsgHandler = new BTMsgHandler() {
            @Override
            void receiveMessage(String msg) {
                if (msg.charAt(0) == 'E'){
                    save_temperatur = Double.parseDouble(msg.substring(1));
                }

            }

            @Override
            void receiveConnectStatus(boolean isConnected) {

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



        // Switch_Messung
        switch_messung.setChecked(false);
        switch_messung.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    messungLauft = true;

                }else{
                    messungLauft = false;
                    textv_messung.setText("keine akt Daten");
                    gMax = 0;
                    gDurchs = 0;
                    gAdd = 0;
                }
            }
        });

        // Spinner Profil
        ArrayAdapter<CharSequence> adapterProfil = ArrayAdapter.createFromResource(this, R.array.profil, android.R.layout.simple_spinner_item);
        adapterProfil.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_profil.setAdapter(adapterProfil);
        spinner_profil.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setUI(getProfil());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //Spinner Bedingungen
        ArrayAdapter<CharSequence> adapterBedingung = ArrayAdapter.createFromResource(this, R.array.straßenBedingungen, android.R.layout.simple_spinner_item);
        adapterBedingung.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_bedingung.setAdapter(adapterBedingung);
        spinner_bedingung.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinnerBed(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Button Save
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfil();
            }
        });


    }// ENDE ON-CREATE


    // BT Connection aufbauen
    private void btConn(){
        SharedPreferences btPref = getSharedPreferences("BTAddress", MODE_PRIVATE);
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
         x = event.values[0];
         y = event.values[1];
         z = event.values[2];
        if (messungLauft) {
            count++;
            gMax = kaliGmax(x, y, z);

            String roundGMax = roundAndFormat(gMax / 9.81, 4);
            textv_gWert.setText(roundGMax);
            String roundMessung = roundAndFormat(Math.abs(x) / 9.81, 4);
            textv_messung.setText("akt. Messung: "+roundMessung);
        }
    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //Messung
    private double kaliGmax(double x, double y, double z) {
        double x1 = (Math.abs(x));
        gAdd = gAdd + x1;
        if (count >= 10) {
            gDurchs = gAdd / count;
            gDurchsArray.add(gDurchs);
            count = 0;
            gDurchs = 0;
            gAdd = 0;
        }
        gMax = Collections.max(gDurchsArray);
        save_gMax = gMax;
        return gMax;
    }


    //Profil
    private void saveProfil() {
        String name = texte_name.getText().toString();
        int bedingung = strassenBedPos;
        double temperatur = Double.parseDouble(textv_temperatur.getText().toString());
        double gWert = save_gMax;
        String key = spinner_profil.getSelectedItemId()+"";

        Profil p1 = new Profil(name, bedingung, temperatur, gWert);

        try {
            SharedPreferences.Editor edit = pref.edit();
            Gson gson = new Gson();
            String json = gson.toJson(p1);
            edit.putString(key, json);
            edit.commit();
            Toast.makeText(getApplicationContext(), "write successful", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "write error", Toast.LENGTH_LONG).show();
        }
    }

    private Profil getProfil(){
        String key = spinner_profil.getSelectedItemId()+"";
        Gson gson = new Gson();
        String json = pref.getString(key, "");
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
                textv_temperatur.setText("Außentemperatur: "+temperatur);
                textv_gWert.setText("Max G-Wert: "+stringgWert);
                spinner_bedingung.setSelection(sb);
            }else{
                texte_name.setText("-");
                textv_temperatur.setText("-");
                textv_gWert.setText("-");
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

    public String roundAndFormat(final double value, final int frac) {
        final java.text.NumberFormat nf = java.text.NumberFormat.getInstance();
        nf.setMaximumFractionDigits(frac);
        return nf.format(new BigDecimal(value));
    }


}
