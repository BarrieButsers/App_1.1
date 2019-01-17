/*



aAkt in m/s^2
aMax in m/s^2
vAkt in km/h
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
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;

public class NKActivity extends AppCompatActivity implements SensorEventListener {

    private TextView txt_aktA, txt_maxA, txt_aktV, txt_maxV, txt_BT, txt_radius, txt_deltav;
    private Button btn;
    private ProgressBar progressBar;

    private BTManager btManager;

    private double vAkt, aMax;

    private boolean messungLauft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nk);

        txt_maxA = (TextView)findViewById(R.id.txtv_NKmaxA);
        txt_aktA = (TextView)findViewById(R.id.txtv_NKaktA);
        txt_maxV = (TextView)findViewById(R.id.txtv_NKmaxV);
        txt_aktV = (TextView)findViewById(R.id.txtv_NKaktV);
        txt_BT = (TextView)findViewById(R.id.txtv_NKbt);
        txt_radius = (TextView)findViewById(R.id.txtv_NKradius);
        txt_deltav = (TextView)findViewById(R.id.txtv_NKdeltav);
        btn = (Button)findViewById(R.id.btn_NK);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);

        SharedPreferences pref = getSharedPreferences("Profil", MODE_PRIVATE);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);

        aMax = getAMax();
        messungLauft = false;
        btn.setText("push to start");

        BTMsgHandler btMsgHandler = new BTMsgHandler() {
            @Override
            void receiveMessage(String msg) {
                if (msg.charAt(0) == 'B') {
                        String s = msg.substring(1, msg.length() - 1);
                        vAkt = Double.parseDouble(s);
                        txt_aktV.setText("akt.km/h: " + s);
                }

            }

            @Override
            void receiveConnectStatus(boolean isConnected) {
                if (isConnected) {
                    txt_BT.setText("Connected");
                    txt_BT.setTextColor(Color.parseColor("#01DF01"));
                } else {
                    txt_BT.setText("No Connection");
                    txt_BT.setTextColor(Color.parseColor("#d60000"));
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

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener( this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        progressBar.setMax(100);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!messungLauft){
                    messungLauft = true;
                    btn.setText("Messung stoppen");
                }else{
                    messungLauft = false;
                    new SendPostRequest().execute();
                    btn.setText("Messung starten");
                    txt_aktA.setText("aAkt");
                }
            }
        });
    }


    //SENSOR
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (messungLauft){
            double x = event.values[0];
            double aAkt = Math.abs(x);
            setTxt_aktA(aAkt);
            setProgressBar(aAkt);
            double maxV = berechneVMax(aAkt);
            setTxt_maxV(maxV);
            double radius = radiusBerechnen(vAkt, x);  // berechne akt. Radius
            setTxt_radius(radius);
            double deltaV = maxV - vAkt;// berechne Delta-V
            setTxt_deltav(deltaV);
        }else{
            setAllTxtKeineMessung();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public boolean onSupportNavigateUp() {
        btManager.cancel();
        Intent i = new Intent(this, MenuActivity.class);
        startActivity(i);
        return true;
    }

    public double radiusBerechnen (double geschw, double a){
        double v = geschw / 3.6;
        return Math.pow(v,2) / Math.abs(a);
    }

    private void btConn(){
        SharedPreferences btPref = getSharedPreferences("KeyValues", MODE_PRIVATE);
        String address = btPref.getString("Address", null);
        try {
            btManager.connect(address.substring(address.length() - 17));
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), "btconn error", Toast.LENGTH_LONG).show();

        }
    }

    private double berechneVMax(double aAktuell){
        double vMax = Math.sqrt((Math.pow((vAkt/3.6),2)/aAktuell)*aMax);
        return vMax*3.6; // Rückgabe in km/h
    }

    private double getAMax(){
        SharedPreferences pref = getSharedPreferences("KeyValues", MODE_PRIVATE);
        String s = pref.getString("GMax", null);
        if (s != null) {
            double aMax = Double.parseDouble(s);
            txt_maxA.setText("GMax: " + roundAndFormat(aMax / 9.81, 2));
            return  aMax;
        }
        return 0;

    }

    public String roundAndFormat(final double value, final int frac) {
        final java.text.NumberFormat nf = java.text.NumberFormat.getInstance();
        nf.setMaximumFractionDigits(frac);
        return nf.format(new BigDecimal(value));
    }

    private void setProgressBar(double x){
        double gAkt = x / 9.81;             // aAkt in G umrechnen
        double gMax = aMax / 9.81;         // aMax in G umrechnen
        double prozent = (gAkt/gMax) * 100;
        int i = (int) prozent;
        progressBar.setProgress(i);

    }

    private void setTxt_deltav(double deltav){
        if (deltav > 300){
            txt_deltav.setText("+ <300 km/h");
        }else{
            txt_deltav.setText("+ "+roundAndFormat(deltav,0)+" km/h möglich");
        }
    }

    private void setTxt_maxV(double maxV){
        if(maxV > 300){
            txt_maxV.setText("Max mögliche Geschw.: +300 km/h");
        }else{
            txt_maxV.setText("Max mögliche Geschw.: " + roundAndFormat(maxV, 0)+" km/h");

        }
    }

    private void setTxt_radius(double radius){
        txt_radius.setText("akt.Radius: "+roundAndFormat(radius, 0)+" m");

    }

    private void setTxt_aktA(double aAkt){
        txt_aktA.setText("Akt.Querbeschleunigung: "+roundAndFormat(aAkt / 9.81, 2));

    }

    private void setAllTxtKeineMessung(){
        progressBar.setProgress(0);
        txt_aktA.setText("Akt.Querbeschleunigung: keine Messung");
        txt_maxV.setText("Max mögliche Geschw.: keine Messung");
        txt_radius.setText("akt.Radius: keine Messung");
        txt_deltav.setText("Messung starten");
    }

}
