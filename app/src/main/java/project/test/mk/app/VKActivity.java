/*

vAkt in km/h
aMax in m/s^2
 */
package project.test.mk.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;

public class VKActivity extends AppCompatActivity {

    private TextView textRadius, textGeschw, textGMax, textv_BT;

    private BTManager btManager;

    double aMax;

    private SharedPreferences pref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vk);

        textRadius = (TextView)findViewById(R.id.txtv_VKradius);
        textGeschw = (TextView)findViewById(R.id.txtv_VKgeschw);
        textGMax = (TextView)findViewById(R.id.txtv_VKgmax);
        textv_BT = (TextView)findViewById(R.id.txtv_VKBT);

        pref = getSharedPreferences("KeyValues", 0);
        aMax = getAMax();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);

        BTMsgHandler btMsgHandler = new BTMsgHandler() {
            @Override
            void receiveMessage(String msg) {
                if (msg.charAt(0) == 'B') {
                    String vString = msg.substring(1, msg.length() - 1);
                    double vAkt = Double.parseDouble(vString);
                    double radius = radiusBerechnen(vAkt, aMax);
                    setTextGeschw(vAkt);
                    setTextRadius(radius);
                }
            }


            @Override
            void receiveConnectStatus(boolean isConnected) {
                if (isConnected) {
                    textv_BT.setText("Connected");
                    textv_BT.setTextColor(Color.parseColor("#01DF01"));
                } else {
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



    }

    @Override
    public boolean onSupportNavigateUp() {
        btManager.cancel();
        Intent i = new Intent(this, MenuActivity.class);
        startActivity(i);
        return true;
    }

    private double getAMax(){
        try {
            SharedPreferences pref = getSharedPreferences("KeyValues", MODE_PRIVATE);
            String s = pref.getString("GMax", null);

            if (s != null){
                double d = Double.parseDouble(s);
                setTextGMax(d);
                return d;
            }else{
                Toast.makeText(getApplicationContext(), "getAMax error", Toast.LENGTH_LONG).show();
                return 0;
            }
        } catch(Exception e){
            Toast.makeText(getApplicationContext(), "getAMax error", Toast.LENGTH_LONG).show();
            return 0;
        }
    }

    public double radiusBerechnen (double v, double a){
        double v1 = v / 3.6;       // V in m/s umrechnen
        return Math.pow(v1,2) / a;       // Radius in Metern
    }

    private void btConn(){
        try {
            String s1 = pref.getString("Address", null);
            s1 = s1.substring(s1.length() - 17);
            btManager.connect(s1);
        } catch (Exception e){
            Toast.makeText(getApplicationContext(), "BT-Conn error", Toast.LENGTH_LONG).show();
        }
    }

    public String roundAndFormat(final double value, final int frac) {
        final java.text.NumberFormat nf = java.text.NumberFormat.getInstance();
        nf.setMaximumFractionDigits(frac);
        return nf.format(new BigDecimal(value));
    }

    private void setTextGeschw(double v){
        textGeschw.setText("akt.Geschw.:"+v+" km/h");
    }

    private void setTextRadius(double radius){
        textRadius.setText("möglicher Kurvenradius: "+roundAndFormat(radius,0)+" m");
    }

    private void setTextGMax(double gMax){
        gMax = gMax/9.81;
        textGMax.setText("Max Querbeschleunigung: "+roundAndFormat(gMax, 2));
    }
}
