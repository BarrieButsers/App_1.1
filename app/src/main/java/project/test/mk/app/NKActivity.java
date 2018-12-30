package project.test.mk.app;

import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.math.BigDecimal;

public class NKActivity extends AppCompatActivity implements SensorEventListener {

    private TextView txt_aktA, txt_maxA, txt_aktV, txt_maxV, txt_BT;
    private Button btn;

    private SharedPreferences pref;

    private BTManager btManager;
    private BTMsgHandler btMsgHandler;

    private Sensor sensor;
    private SensorManager sensorManager;

    private double vAkt, vMax, aAkt, aMax;

    private boolean messungLauft;

    private ActionBar actionBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nk);

        txt_maxA = (TextView)findViewById(R.id.textv_NKmaxA);
        txt_aktA = (TextView)findViewById(R.id.textv_NKaktA);
        txt_maxV = (TextView)findViewById(R.id.textv_NKmaxV);
        txt_aktV = (TextView)findViewById(R.id.textv_NKaktV);
        txt_BT = (TextView)findViewById(R.id.textv_NKBT);
        btn = (Button)findViewById(R.id.btn_NK);

        pref = getSharedPreferences("Profil", MODE_PRIVATE);

        actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);

        btMsgHandler = new BTMsgHandler() {
            @Override
            void receiveMessage(String msg) {
                if (msg.charAt(0) == 'B'){
                    vAkt = Double.parseDouble(msg.substring(1));
                    txt_aktV.setText("km/h: "+vAkt);
                }

            }

            @Override
            void receiveConnectStatus(boolean isConnected) {
                if (isConnected){
                    txt_BT.setText("Connected");
                }else{
                    txt_BT.setText("No Connection");
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
        getAMax();

        messungLauft = false;
        btn.setText("push to start");
        sensorManager = (SensorManager) getSystemService (SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener( this, sensor, SensorManager.SENSOR_DELAY_GAME);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!messungLauft){
                    messungLauft = true;
                    btn.setText("push to stop");
                }else{
                    messungLauft = false;
                    btn.setText("push to start");
                    txt_aktA.setText("aAkt");
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        btManager.cancel();
        Intent i = new Intent(this, MenuActivity.class);
        startActivity(i);
        return true;
    }

    private void btConn(){
        SharedPreferences btPref = getSharedPreferences("BTAddress", MODE_PRIVATE);
        String address = btPref.getString("Address", null);
        try {
            btManager.connect(address.substring(address.length() - 17));
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), "btconn error", Toast.LENGTH_LONG).show();

        }
    }

    private void berechneVMax(double vAktuell, double aAktuell){
        vMax = Math.sqrt((Math.pow(vAktuell / 3.6,2) / aAktuell)*aMax);
        txt_maxV.setText("VMax: "+vMax*3.6+" km/h");

    }

    private void getAMax(){
        SharedPreferences pref = getSharedPreferences("BTAddress", MODE_PRIVATE);
        String s = pref.getString("GMax", null);
        if (s != null) {
            aMax = Double.parseDouble(s);
            txt_maxA.setText("GMax: " + roundAndFormat(aMax / 9.81, 2));
        }


    }

    //SENSOR
    @Override
    public void onSensorChanged(SensorEvent event) {
        double x = event.values[0];
        double y = event.values[1];
        double z = event.values[2];

        if (messungLauft){
            aAkt = Math.abs(x);
            String s1 = roundAndFormat(aAkt / 9.81, 4);
            txt_aktA.setText(s1);
            berechneVMax(vAkt, aAkt);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public String roundAndFormat(final double value, final int frac) {
        final java.text.NumberFormat nf = java.text.NumberFormat.getInstance();
        nf.setMaximumFractionDigits(frac);
        return nf.format(new BigDecimal(value));
    }
}
