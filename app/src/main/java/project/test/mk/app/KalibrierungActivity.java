package project.test.mk.app;

import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;

public class KalibrierungActivity extends AppCompatActivity implements SensorEventListener {


    private Sensor sensor;
    private SensorManager sensorManager;
    private TextView textMaxG, xText, text10, text11;
    private Button btnSave;

    private SharedPreferences pref;

    int count = 0;
    double x = 0;
    double gAdd = 0;
    double gMax = 0;
    ArrayList<Double> gDurchsArray = new ArrayList<Double>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kalibrierung);

        textMaxG = (TextView)findViewById(R.id.textMaxG);
        xText = (TextView)findViewById(R.id.xText);
        text10 = (TextView)findViewById(R.id.textView10);
        text11 = (TextView)findViewById(R.id.textView11);
        btnSave = (Button)findViewById(R.id.btn_save);

        pref = getSharedPreferences("KeyValues", 0);

        gDurchsArray.add(0.0);
        sensorManager = (SensorManager) getSystemService (SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener( this, sensor, SensorManager.SENSOR_DELAY_GAME);


        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                   writePref(gMax);
                } catch (Exception ex) {
                    Toast.makeText(getApplicationContext(), "write error", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        double x = event.values[0];
        double y = event.values[1];
        double z = event.values[2];
        xText.setText(""+ this.x +"C: "+count);
        count++;
        gMax = kaliGmax(x,y,z);
        textMaxG.setText(""+gMax);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private double kaliGmax(double x, double y, double z){

        double gDurchs;

        double x1= (Math.abs(x))/9.81;
        gAdd = gAdd + x1;
        if (count >= 10){   // !! TIMER EINBAUEN !!
            gDurchs = gAdd / count;
            gDurchsArray.add(gDurchs);
            count = 0;
            gDurchs = 0;
            gAdd = 0;

        }
        gMax = Collections.max(gDurchsArray);
        return gMax;

    }

    private void writePref(double gmax){
        SharedPreferences.Editor editor = pref.edit();
        Float f = (float)gmax;
        editor.putFloat("gmax",f);
        editor.commit();
        gDurchsArray.clear();
        gMax = 0;

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
    */
}

