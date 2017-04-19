package com.touhid.vehicletracker;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class Main2Activity extends AppCompatActivity {

    private AlertDialog.Builder alartBuilder = null;
    private LocationManager locationManager = null;
    double latitude = 0, longitude = 0;
    private LocationListener locationListener;
    private TelephonyManager telephonyManager;
    private int statusCounter;
    private WifiManager wifiManager = null;
    private String serverResponse ="";
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        textView = (TextView) findViewById(R.id.textView2);

        ConnectivityManager connectionManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectionManager.getActiveNetworkInfo();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        System.out.println("Internet connection is: " + isConnected);


        if (isConnected != false) {
            getGPSInformation();
        } else {
            alartBuilder = new AlertDialog.Builder(this);
            alartBuilder.setMessage("Please Active Your Internet Connection.").setCancelable(false);
            AlertDialog alertDialog = alartBuilder.create();
            alertDialog.setTitle("Alert !!!");
            alertDialog.show();
        }
    }
    public void showStatus(int status, String message){
        statusCounter += status;
        if(statusCounter >=2){statusCounter =2;}
        System.out.println("Message is: "+message);
        alartBuilder = new AlertDialog.Builder(this);
        if(statusCounter == 1){
            if (message != null && message.equals("TRUE")){
                textView.setText("");
                alartBuilder.setMessage("Your bus is under tracking press home button to continue tracking.");
                AlertDialog alertDialog = alartBuilder.create();
                alertDialog.setTitle("Status !!!");
                alertDialog.show();
            }
            else if(message != null && message.equals("FALSE")){
                textView.setText("");
                alartBuilder.setMessage("Your bus is not registered for this system use IMEI bellow to register IMEI: "+telephonyManager.getDeviceId()).setCancelable(false);
                AlertDialog alertDialog = alartBuilder.create();
                alertDialog.setTitle("Status !!!");
                alertDialog.show();
            }
            else if(message.equals(null)){
                textView.setText("");
                alartBuilder.setMessage("Can not communicate to server please contact with authority").setCancelable(false);
                AlertDialog alertDialog = alartBuilder.create();
                alertDialog.setTitle("Status !!!");
                alertDialog.show();
            }
            else {
                textView.setText("");
                alartBuilder.setMessage("Can not communicate to server please contact with authority").setCancelable(false);
                AlertDialog alertDialog = alartBuilder.create();
                alertDialog.setTitle("Status !!!");
                alertDialog.show();
            }
        }
    }
    private void getGPSInformation() {
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                System.out.println("Latitude: " + location.getLatitude() + " Longitude: " + location.getLongitude());

                String type = "checkAndInsertValue";
                new BackgroundWorker().execute(type,String.valueOf(latitude),String.valueOf(longitude));

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }

        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates("gps", 5000, 0, locationListener);
    }







    class BackgroundWorker extends AsyncTask<String, String, String> {

        public BackgroundWorker() {
            telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        }

        @Override
        protected String doInBackground(String... params) {
            String type = params[0];
            String latitude = params[1];
            String longitude = params[2];
            String checkAndInsertUrl = "http://192.168.0.100:8080/checkAndInsertValue";
            if (type.equals("checkAndInsertValue")) {
                try {

                    String IMEI = telephonyManager.getDeviceId();
                    String macAddress = wifiManager.getConnectionInfo().getMacAddress();


                    URL url = new URL(checkAndInsertUrl);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoOutput(true);

                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8"));

                    String post_data = URLEncoder.encode("IMEI","UTF-8")+"="+URLEncoder.encode(IMEI,"UTF-8")+"&"
                            +URLEncoder.encode("macAddress","UTF-8")+"="+URLEncoder.encode(macAddress,"UTF-8")+"&"
                            +URLEncoder.encode("latitude","UTF-8")+"="+URLEncoder.encode(latitude,"UTF-8")+"&"
                            +URLEncoder.encode("longitude","UTF-8")+"="+URLEncoder.encode(longitude,"UTF-8");

                    bufferedWriter.write(post_data);
                    bufferedWriter.flush();
                    bufferedWriter.close();

                    InputStream inputStream = httpURLConnection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));

                    String line = "";

                    while((line = bufferedReader.readLine()) != null){
                        serverResponse = line;
                    }
                    bufferedReader.close();
                    inputStream.close();
                    httpURLConnection.disconnect();

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(String result) {
            showStatus(1, serverResponse);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }
    }
}
