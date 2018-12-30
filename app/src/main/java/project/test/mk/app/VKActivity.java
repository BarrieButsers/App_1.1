package project.test.mk.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.FileInputStream;
import java.io.ObjectInputStream;

public class VKActivity extends AppCompatActivity {

    private TextView textRadius, textGeschw, textGMax, textv_BT;

    private BTManager btManager;
    private BTMsgHandler btMsgHandler;

    private Button btnTest;

    double geschw, gmax;

    private SharedPreferences pref;
    private ActionBar actionBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vk);

        textRadius = (TextView)findViewById(R.id.text_radius);
        textGeschw = (TextView)findViewById(R.id.text_geschw);
        textGMax = (TextView)findViewById(R.id.text_gmax);
        textv_BT = (TextView)findViewById(R.id.textv_VKBT);

        pref = getSharedPreferences("KeyValues", 0);
        gmax = getGmax();

        actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);

        btMsgHandler = new BTMsgHandler() {
            @Override
            void receiveMessage(String msg) {
                if (msg.charAt(0)== 'A'){
                    String temp = msg.substring(1);
                    geschw = Double.parseDouble(temp);
                    textRadius.setText(radiusBerechnen(geschw, gmax)+"");
                }
            }

            @Override
            void receiveConnectStatus(boolean isConnected) {
                if (isConnected){
                    textv_BT.setText("Connected");
                }else{
                    textv_BT.setText("No Connection");
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

    private double getGmax(){
        try {
            SharedPreferences pref = getSharedPreferences("BTAddress", MODE_PRIVATE);
            String s = pref.getString("GMax", null);

            if (s != null){
                double d = Double.parseDouble(s);
                textGMax.setText("G Max: "+d/9.81);
                return d;
            }else if (s == null){
                Toast.makeText(getApplicationContext(), "getGmax error", Toast.LENGTH_LONG).show();
                return 0;
            }
        } catch(Exception e){
            Toast.makeText(getApplicationContext(), "getGmax error", Toast.LENGTH_LONG).show();
            return 0;
        }
        return 0;
    }

    public double radiusBerechnen (double v, double a){
        double r = Math.pow(v,2) / a;
        return r;
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
}
