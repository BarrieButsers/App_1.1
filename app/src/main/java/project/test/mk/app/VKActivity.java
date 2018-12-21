package project.test.mk.app;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.ObjectInputStream;

public class VKActivity extends AppCompatActivity {

    private TextView textRadius, textGeschw, textGMax;
    private BTManager btManager;
    private BTMsgHandler btMsgHandler;

    double geschw, gmax;

    private SharedPreferences pref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vk);

        textRadius = (TextView)findViewById(R.id.text_radius);
        textGeschw = (TextView)findViewById(R.id.text_geschw);
        textGMax = (TextView)findViewById(R.id.text_gmax);

        pref = getSharedPreferences("KeyValues", 0);
        gmax = getGmax();

        btMsgHandler = new BTMsgHandler() {
            @Override
            void receiveMessage(String msg) {
                if (msg.charAt(0)== 'A'){
                    String temp = msg.substring(1);
                    geschw = Double.parseDouble(temp);
                }
                textRadius.setText(radiusBerechnen(geschw, gmax)+"");
            }

            @Override
            void receiveConnectStatus(boolean isConnected) {
                if (isConnected == false){
                    try {
                        btManager.connect(getAddress());
                    }catch (Exception e){
                        Toast.makeText(getApplicationContext(), "ReConn Error", Toast.LENGTH_LONG).show();

                    }
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



    }
    private double getGmax(){
        try {
            double d = (double)pref.getFloat("gmax",0);
            if (d != 0){
                return d;
            }else{
                Toast.makeText(getApplicationContext(), "gmax read error", Toast.LENGTH_LONG).show();
                return 0;
            }
        } catch(Exception e){
            Toast.makeText(getApplicationContext(), "gmax read error", Toast.LENGTH_LONG).show();
            return 0;
        }
    }

    public double radiusBerechnen (double v, double a){
        double r;
        r = (v * v) / a;
        return r;
    }

    private String getAddress(){
        try {
            String s1 = pref.getString("Address", null);
            s1 = s1.substring(s1.length() - 17);
            return s1;
        } catch (Exception e){
            Toast.makeText(getApplicationContext(), "getAddress error", Toast.LENGTH_LONG).show();
            return null;
        }
    }
}