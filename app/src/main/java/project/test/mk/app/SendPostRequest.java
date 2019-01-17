package project.test.mk.app;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class SendPostRequest extends AsyncTask<String, Void, String> {

    private Double gMax;
    private int profilNr;

    protected void onPreExecute(){

    }

    protected String doInBackground(String... arg0) {

        try {
            String s = "https://api.thingspeak.com/update?api_key=KWIC98IKD3BM5XW0&field"+profilNr+"="+gMax;
            URL url = new URL(s);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));

            writer.flush();
            writer.close();
            os.close();

            int responseCode=conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                StringBuffer sb = new StringBuffer("");
                String line="";

                while((line = in.readLine()) != null) {

                    sb.append(line);
                    break;
                }

                in.close();
                return sb.toString();

            }
            else {
                return new String("false : "+responseCode);
            }
        }
        catch(Exception e){
            return new String("Exception: " + e.getMessage());
        }

    }

    @Override
    protected void onPostExecute(String result) {

    }

    public void setgMax(Double gMax) {
        this.gMax = gMax;
    }

    public void setProfilNr(int profilNr) {
        this.profilNr = profilNr;
    }
}
