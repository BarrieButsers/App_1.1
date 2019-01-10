/*



aAkt in m/s^2
aMax in m/s^2
vAkt in km/h
vMax in m/s
 */

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
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.anastr.speedviewlib.SpeedView;

import java.math.BigDecimal;

public class NKActivity extends AppCompatActivity implements SensorEventListener {

    private TextView txt_aktA, txt_maxA, txt_aktV, txt_maxV, txt_BT, txt_radius, txt_deltav;
    private Button btn;
    private ProgressBar progressBar;

    private SharedPreferences pref;

    private BTManager btManager;
    private BTMsgHandler btMsgHandler;

    private Sensor sensor;
    private SensorManager sensorManager;

    private double vAkt, vMax, aMax;

    private boolean messungLauft;

    private ActionBar actionBar;


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

        pref = getSharedPreferences("Profil", MODE_PRIVATE);

        actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);

        aMax = getAMax();
        messungLauft = false;
        btn.setText("push to start");

        btMsgHandler = new BTMsgHandler() {
            @Override
            void receiveMessage(String msg) {
                if (msg.charAt(0) == 'B'){
                    if (messungLauft) {
                        String s = msg.substring(1, msg.length() - 1);
                        vAkt = Double.parseDouble(s);
                        txt_aktV.setText("km/h: " + s);
                    }else{
                        txt_aktV.setText("v-Akt");
                    }
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

        sensorManager = (SensorManager) getSystemService (SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener( this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        progressBar.setMax(100);

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



    //SENSOR
    @Override
    public void onSensorChanged(SensorEvent event) {
        double x = event.values[0];
        double y = event.values[1];
        double z = event.values[2];

        if (messungLauft){
            setProgressBar(x);
            double aAkt = Math.abs(x);
            txt_aktA.setText(roundAndFormat(aAkt / 9.81, 2));
            txt_maxV.setText(roundAndFormat(berechneVMax(vAkt, aAkt), 2));      // berechne V-Max
            double radius = radiusBerechnen(vAkt, x);                                // berechne akt. Radius
            txt_radius.setText(roundAndFormat(radius, 0)+" m");
            double deltaV = (vMax*3.6) - vAkt;                                       // berechne Delta-V
            txt_deltav.setText(roundAndFormat(deltaV,0)+" km/h");
        }else{
            progressBar.setProgress(0);
            txt_aktA.setText("a-Akt");
            txt_maxV.setText("v-Max");
            txt_radius.setText("akt-Radius");
            txt_deltav.setText("Delta-v");

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public double radiusBerechnen (double geschw, double a){
        double v = geschw / 3.6;
        double r = Math.pow(v,2) / Math.abs(a);
        return r;
    }

    @Override
    public boolean onSupportNavigateUp() {
        btManager.cancel();
        Intent i = new Intent(this, MenuActivity.class);
        startActivity(i);
        return true;
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

    private double berechneVMax(double vAktuell, double aAktuell){
        vMax = Math.sqrt((Math.pow(vAktuell / 3.6,2) / aAktuell) * aMax);
        return vMax;
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
        double xx = Math.abs(x) / 9.81; // aAkt in G umrechnen
        double g = aMax / 9.81;         // aMax in G umrechnen
        double prozent = (xx/g) * 100;
        int i = (int) prozent;
        progressBar.setProgress(i);

    }
}
