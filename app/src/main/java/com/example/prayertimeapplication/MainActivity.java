// - Package & Libraries
package com.example.prayertimeapplication;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.os.Bundle;

import java.util.Calendar;

import android.os.Handler;

import java.text.DateFormat;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.text.format.Time;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;

// - Volley (HTTP) Library
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.scwang.wave.MultiWaveHeader;

import org.json.JSONException;
import org.json.JSONObject;


// - Main Class
public class MainActivity extends AppCompatActivity implements SensorEventListener {

    // - Attributes
    private static final String TAG = "tag";

    // - Cities
    private String cities[] = {
            "ADANA", "ADIYAMAN", "AFYONKARAHİSAR", "AĞRI", "AMASYA", "ANKARA", "ANTALYA", "ARTVİN", "AYDIN", "BALIKESİR",
            "BİLECİK", "BİNGÖL", "BİTLİS", "BOLU", "BURDUR", "BURSA", "ÇANAKKALE", "ÇANKIRI", "ÇORUM", "DENİZLİ",
            "DİYARBAKIR", "EDİRNE", "ELAZIĞ", "ERZİNCAN", "ERZURUM", "ESKİŞEHİR", "GAZİANTEP", "GİRESUN", "GÜMÜŞHANE", "HAKKARİ",
            "HATAY", "ISPARTA", "MERSİN", "İSTANBUL", "İZMİR", "KARS", "KASTAMONU", "KAYSERİ", "KIRKLARELİ", "KIRŞEHİR",
            "KOCAELİ", "KONYA", "KÜTAHYA", "MALATYA", "MANİSA", "KAHRAMANMARAŞ", "MARDİN", "MUĞLA", "MUŞ", "NEVŞEHİR",
            "NİĞDE", "ORDU", "RİZE", "SAKARYA", "SAMSUN", "SİİRT", "SİNOP", "SİVAS", "TEKİRDAĞ", "TOKAT",
            "TRABZON", "TUNCELİ", "ŞANLIURFA", "UŞAK", "VAN", "YOZGAT", "ZONGULDAK", "AKSARAY", "BAYBURT", "KARAMAN",
            "KIRIKKALE", "BATMAN", "ŞIRNAK", "BARTIN", "ARDAHAN", "IĞDIR", "YALOVA", "KARABÜK", "KİLİS", "OSMANİYE", "DÜZCE"
    };

    // - Toolbar
    private Toolbar mToolbar;
    private Spinner mspinner;
    private ArrayAdapter<String> dataAdapterForCities;

    // -- Thread
    private Handler hander;
    private Runnable runnable;

    // -- Current Location
    private TextView currentLocation;
    private TextView chosenLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private String city = "KONYA";
    private float currentLatitude;
    private float currentLongitude;

    // -- Date
    private Time time;
    private Date date;
    private Date today;
    private Calendar rightnow;
    private DateFormat dateFormatter;
    private SimpleDateFormat timeFormatter;
    private SimpleDateFormat mFormatter;

    // -- ProgressBar
    private String clockResult;
    private String minuteResult;
    private TextView dateValue;
    private TextView clockValue;
    private TextView minuteValue;
    private TextView directionValue;
    private ProgressBar progressBar;

    // -- Prayer Times
    private Date prayTime;
    private String fajr = "00:00";
    private String dhuhr = "00:00";
    private String asr = "00:00";
    private String maghrib = "00:00";
    private String isha = "00:00";
    private TextView fajrTime;
    private TextView dhuhrTime;
    private TextView asrTime;
    private TextView maghribTime;
    private TextView ishaTime;
    private TextView cmpPrayStr;
    private TextView cmpTime;

    // - Tag used to cancel the request
    private ProgressDialog pDialog;
    String tag_json_obj = "json_obj_req";

    // -- Compass
    private static SensorManager sensorManager;
    private static Sensor sensor;
    private ImageView qıbleCompass;
    private ImageView kaabaCompass;
    private float mGravity[] = new float[3];
    private float mGeomagnetic[] = new float[3];
    private float azimuth = 0f;
    private float currentAzimuth = 0f;

    // - Constructor
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //activity_main

        // - Toolbar
        mToolbar = findViewById(R.id.mytoolbar);
        mspinner = findViewById(R.id.mspinner);
        toolbarSpinner();
        setSupportActionBar(mToolbar);

        // - Compass
        qıbleCompass = findViewById(R.id.arrow_IMG);
        kaabaCompass = findViewById(R.id.kaaba_IMG);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        // - Real Time Clock
        time = new Time();
        runnable = new Runnable() {
            @Override
            public void run() {
                time.setToNow();
                hander.postDelayed(runnable, 1000);

                // - Location
                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    // - When permisson granted
                    getLocation();
                } else {
                    // - When permisson denied
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
                }

                // - Date & Time Formatter
                timeFormatter = new SimpleDateFormat("HH:mm");
                mFormatter = new SimpleDateFormat("ss");
                dateFormatter = new SimpleDateFormat("EEEE, dd MMM");

                // - Date & Time Objects
                date = new Date();
                today = new Date();
                rightnow = Calendar.getInstance();

                // - Progress Bar Components
                dateValue = (TextView) findViewById(R.id.week_TV);
                clockValue = (TextView) findViewById(R.id.clock_TV);
                minuteValue = (TextView) findViewById(R.id.minute_TV);
                chosenLocation = (TextView) findViewById(R.id.city_TV);
                currentLocation = (TextView) findViewById(R.id.currentLocation_TV);

                // - Typefaces
                clockValue.setTypeface(ResourcesCompat.getFont(MainActivity.this, R.font.learner));
                dateValue.setTypeface(ResourcesCompat.getFont(MainActivity.this, R.font.moonlight));
                minuteValue.setTypeface(ResourcesCompat.getFont(MainActivity.this, R.font.moonlight));

                // Progress Bar Values
                updateProgressBarValue();
                chosenLocation.setText(city.toUpperCase());

                // - Set Data
                clockResult = timeFormatter.format(today);
                clockValue.setText(clockResult);
                minuteResult = mFormatter.format(date);
                minuteValue.setText(minuteResult);
                dateValue.setText(dateFormatter.format(date).toUpperCase());

            }
        };
        hander = new Handler();
        hander.postDelayed(runnable, 1000);
    }


    // - Toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }


    // - Toolbar Drop-down List
    public void toolbarSpinner() {
        dataAdapterForCities = new ArrayAdapter<String>(this, R.layout.spinner_list, cities);
        dataAdapterForCities.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mspinner.setAdapter(dataAdapterForCities);
        mspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                city = mspinner.getSelectedItem().toString();
                mToolbar.setTitle(mspinner.getSelectedItem().toString().toUpperCase());
                getData();
                // saveData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    // - Save data
    private void saveData() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("City", city);
        editor.commit();
    }

    private void loadData() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        city = prefs.getString("City", "");
        editor.commit();
    }

    // Drop-Down List Data


    // - Progress Bar Value
    private void updateProgressBarValue() {
        float progressBarValue;
        float backupBarValue;

        progressBarValue = (((rightnow.get(Calendar.HOUR_OF_DAY)) * 60) + ((rightnow.get(Calendar.MINUTE))));
        backupBarValue = progressBarValue;
        compareTime(backupBarValue);
        progressBarValue = Math.round((progressBarValue / ((float) (24 * 60))) * 100); // percent
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setProgress((int) progressBarValue);
    }

    // - Get Current Location
    private void getLocation() {
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if (location != null) {

                    try {
                        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

                        // provinence = addresses.get(0).getSubAdminArea(); // KARATAY
                        // city = addresses.get(0).getAdminArea();          // KONYA
                        // country = addresses.get(0).getCountryName();     // TURKEY

                        currentLatitude = (float) (addresses.get(0).getLatitude()); // Fd
                        currentLongitude = (float) (addresses.get(0).getLongitude()); // Ld

                        currentLocation.setTypeface(ResourcesCompat.getFont(MainActivity.this, R.font.moonlight));
                        currentLocation.setText(addresses.get(0).getAdminArea().toUpperCase());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {

                }
            }
        });
    }

    // - Compass
    @Override
    protected void onResume() {
        super.onResume();

        if(sensor != null) {
            sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);
            sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
        }
        else {
           Toast.makeText(this, "Compass Not Supported", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        final float alpha = 0.97f;
        synchronized (this) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                mGravity[0] = alpha * mGravity[0] + (1 - alpha) * sensorEvent.values[0];
                mGravity[1] = alpha * mGravity[1] + (1 - alpha) * sensorEvent.values[1];
                mGravity[2] = alpha * mGravity[2] + (1 - alpha) * sensorEvent.values[2];
            }

            if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                mGeomagnetic[0] = alpha * mGeomagnetic[0] + (1 - alpha) * sensorEvent.values[0];
                mGeomagnetic[1] = alpha * mGeomagnetic[1] + (1 - alpha) * sensorEvent.values[1];
                mGeomagnetic[2] = alpha * mGeomagnetic[2] + (1 - alpha) * sensorEvent.values[2];
            }

            float R[] = new float[9];
            float I[] = new float[9];
            double kiblat_derajat;
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimuth = (float) Math.toDegrees(orientation[0]);
                azimuth = (azimuth + 360) % 360;

                kiblat_derajat = bearing(currentLatitude, currentLongitude, 21.422487, 39.826206);
                azimuth -= kiblat_derajat;

                getDirectionString(kiblat_derajat);

                Animation anim = new RotateAnimation(-currentAzimuth, -azimuth, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                currentAzimuth = azimuth;

                anim.setDuration(500);
                anim.setRepeatCount(0);
                anim.setFillAfter(true);

                qıbleCompass.startAnimation(anim);

                Animation animkaaba = new RotateAnimation(-currentAzimuth, -azimuth, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

                animkaaba.setDuration(500);
                animkaaba.setRepeatCount(0);
                animkaaba.setFillAfter(true);

                kaabaCompass.startAnimation(animkaaba);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    // - Bearing
    protected static double bearing(double lat1, double lon1, double lat2, double lon2) {
        double longitude1 = lon1;
        double longitude2 = lon2;
        double latitude1 = Math.toRadians(lat1);
        double latitude2 = Math.toRadians(lat2);
        double longDiff = Math.toRadians(longitude2 - longitude1);
        double y = Math.sin(longDiff) * Math.cos(latitude2);
        double x = Math.cos(latitude1) * Math.sin(latitude2) - Math.sin(latitude1) * Math.cos(latitude2) * Math.cos(longDiff);

        return (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
    }
	
	
	// - Direction
	private void getDirectionString(double azimuthDegrees) {
        String where = "NW";

        if (azimuthDegrees >= 350 || azimuthDegrees <= 10)
            where = "N";
        if (azimuthDegrees < 350 && azimuthDegrees > 280)
            where = "NW";
        if (azimuthDegrees <= 280 && azimuthDegrees > 260)
            where = "W";
        if (azimuthDegrees <= 260 && azimuthDegrees > 190)
            where = "SW";
        if (azimuthDegrees <= 190 && azimuthDegrees > 170)
            where = "S";
        if (azimuthDegrees <= 170 && azimuthDegrees > 100)
            where = "SE";
        if (azimuthDegrees <= 100 && azimuthDegrees > 80)
            where = "E";
        if (azimuthDegrees <= 80 && azimuthDegrees > 10)
            where = "NE";

        directionValue = findViewById(R.id.direction_TV);
        directionValue.setText(Math.round(azimuthDegrees)+" "+where);

    }


    // - Get Data to Web Service
    public void getData() {

        // - Prayer Timing
        fajrTime = (TextView) findViewById(R.id.FajrTime_TV);
        dhuhrTime = (TextView) findViewById(R.id.DhuhrTime_TV);
        asrTime = (TextView) findViewById(R.id.AsrTime_TV);
        maghribTime = (TextView) findViewById(R.id.MaghribTime_TV);
        ishaTime = (TextView) findViewById(R.id.IshaTime_TV);

        // - Loading
        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading...");
        pDialog.show();

        // - HTTP Request
        String url = "https://api.collectapi.com/pray/all?data.city=" + (city.toLowerCase());
        RequestQueue queue = Volley.newRequestQueue(this);

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    // get data from JSON
                    fajr = response.getJSONArray("result").getJSONObject(0).get("saat").toString();
                    dhuhr = response.getJSONArray("result").getJSONObject(2).get("saat").toString();
                    asr = response.getJSONArray("result").getJSONObject(3).get("saat").toString();
                    maghrib = response.getJSONArray("result").getJSONObject(4).get("saat").toString();
                    isha = response.getJSONArray("result").getJSONObject(5).get("saat").toString();

                    // Set Data
                    fajrTime.setText(fajr);
                    dhuhrTime.setText(dhuhr);
                    asrTime.setText(asr);
                    maghribTime.setText(maghrib);
                    ishaTime.setText(isha);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                pDialog.hide();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                pDialog.hide();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("authorization", "apikey 2zkCIcZfeYT2LaDZbXm2y4:0xGpT8tlCIkIR7Y8Ku4JTj");
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(jsonObjectRequest, tag_json_obj);
    }


    // - Compare Time
    private void compareTime(float barValues) {

        // - Variables
        boolean control = false;

        // - Date & Time Formatter
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");


        try {
            cmpTime = findViewById(R.id.cmpTime_TV);
            cmpPrayStr = findViewById(R.id.compare_TV);

            String leftTime;

            // - Fajr
            prayTime = formatter.parse(fajr);
            if (((calculateMinutes(prayTime) - (barValues)) >= 1f) && ((calculateMinutes(prayTime) - (barValues)) <= 5f)) {
                control = true;
                leftTime = Integer.toString((int)(calculateMinutes(prayTime) - (barValues)));
                cmpTime.setText(leftTime);
                cmpPrayStr.setText(getResources().getText(R.string.fajrleft));
            }
            else if (((calculateMinutes(prayTime) - (barValues)) == 0f)) {
                control = true;
                cmpTime.setText("");
                cmpPrayStr.setText(getResources().getText(R.string.fajrtime));
            }

            // - Dhuhr
            prayTime = formatter.parse(dhuhr);
            if (((calculateMinutes(prayTime) - (barValues)) >= 1f) && ((calculateMinutes(prayTime) - (barValues)) <= 5f)) {
                control = true;
                leftTime = Integer.toString((int)(calculateMinutes(prayTime) - (barValues)));
                cmpTime.setText(leftTime);
                cmpPrayStr.setText(getResources().getText(R.string.dhuhrleft));

            } else if (((calculateMinutes(prayTime) - (barValues)) == 0f)) {
                control = true;
                cmpTime.setText("");
                cmpPrayStr.setText(getResources().getText(R.string.dhuhrtime));
            }

            // - Asr
            prayTime = formatter.parse(asr);
            if (((calculateMinutes(prayTime) - (barValues)) >= 1f) && ((calculateMinutes(prayTime) - (barValues)) <= 5f)) {
                control = true;
                leftTime = Integer.toString((int)(calculateMinutes(prayTime) - (barValues)));
                cmpTime.setText(leftTime);
                cmpPrayStr.setText(getResources().getText(R.string.asrleft));

            } else if (((calculateMinutes(prayTime) - (barValues)) == 0f)) {
                control = true;
                cmpTime.setText("");
                cmpPrayStr.setText(getResources().getText(R.string.asrtime));
            }

            // - Maghrib
            prayTime = formatter.parse(maghrib);
            if (((calculateMinutes(prayTime) - (barValues)) >= 1f) && ((calculateMinutes(prayTime) - (barValues)) <= 5f)) {
                control = true;
                leftTime = Integer.toString((int)(calculateMinutes(prayTime) - (barValues)));
                cmpTime.setText(leftTime);
                cmpPrayStr.setText(getResources().getText(R.string.maghribleft));

            } else if (((calculateMinutes(prayTime) - (barValues)) == 0f)) {
                control = true;
                cmpTime.setText("");
                cmpPrayStr.setText(getResources().getText(R.string.maghribtime));
            }

            // - Isha
            prayTime = formatter.parse(isha);
            if (((calculateMinutes(prayTime) - (barValues)) >= 1f) && ((calculateMinutes(prayTime) - (barValues)) <= 5f)) {
                control = true;
                leftTime = Integer.toString((int)(calculateMinutes(prayTime) - (barValues)));
                cmpTime.setText(leftTime);
                cmpPrayStr.setText(getResources().getText(R.string.ishaleft));

            } else if (((calculateMinutes(prayTime) - (barValues)) == 0f)) {
                control = true;
                cmpTime.setText("");
                cmpPrayStr.setText(getResources().getText(R.string.ishatime));
            }


            if (control != true) {
                cmpTime.setText("");
                cmpPrayStr.setText("");
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private float calculateMinutes(Date prayTime) {
        return ((prayTime.getHours() * 60) + prayTime.getMinutes());
    }

}