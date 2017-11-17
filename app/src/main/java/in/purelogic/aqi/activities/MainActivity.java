package in.purelogic.aqi.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
//import android.content.SharedPreferences;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.format.DateFormat;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mxn.soul.flowingdrawer_core.ElasticDrawer;
import com.mxn.soul.flowingdrawer_core.FlowingDrawer;
import com.wang.avi.AVLoadingIndicatorView;
import com.yalantis.phoenix.PullToRefreshView;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cz.msebera.android.httpclient.Header;
import es.dmoral.toasty.Toasty;
import in.purelogic.aqi.Models.OutdoorDataModel;
import in.purelogic.aqi.Palette;
import in.purelogic.aqi.R;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LocationListener {
    // Base URL
    final String url = "https://www.facebook.com/aqiindia/";
    //final static String SENSOR_OUTDOOR_URL = "http://api.airvisual.com/v2/nearest_city?";
   // final static String KEY = "kbLpQXHgWm7PkczZM";
    final static String SENSOR_OUTDOOR_URL = "http://api.airpollutionapi.com/1.0/aqi?";
    final static String APPID = "sm3u7f6d6rckdv6l5q3concsbb";
    public static final String outdoorPrefs = "outdoorPrefs";
    public static final String placeName = "placename";
    public static final String aqi = "aqi";
    public static final String pm25 = "pm25";
    public static final String temp = "temp";
    public static final String humid = "humid";
    public static final String time = "time";
    public static final String picture1 = "picture1";
    public static final String picture2 = "picture2";
    public static final String picture3 = "picture3";
    public static final String message = "message";

    SharedPreferences outdoorSharedpreferences;

    String myPlaceNow;
    String myPlaceNowSmall;

    @BindView(R.id.drawer_layout)
    FlowingDrawer mDrawer;
    @BindView(R.id.btnLocations)
    ImageButton btnLocation;
    @BindView(R.id.btnNotification)
    ImageButton btnNotify;
    @BindView(R.id.btnWhatAqi)
    ImageButton btnWhatAqi;
    @BindView(R.id.btnBlog)
    ImageButton btnBlog;
    @BindView(R.id.btnWebsite)
    ImageButton btnWebsite;
    @BindView(R.id.btnAboutUs)
    ImageButton btnAboutUs;
    @BindView(R.id.tvDate)
    TextView tvDate;
    @BindView(R.id.tvPlace)
    TextView tvPlace;
    @BindView(R.id.tvClock)
    TextView tvClock;
    @BindView(R.id.tvAqi)
    TextView tvAqi;
    @BindView(R.id.tvAqiComment)
    TextView tvAqiComment;
    @BindView(R.id.tvLastRefresh)
    TextView tvLastRefresh;
    @BindView(R.id.pull_to_refresh)
    PullToRefreshView mPullToRefreshView;
    @BindView(R.id.btnFacebook)
    ImageButton btnFacebook;
    @BindView(R.id.tvCurrentLocation)
    TextView tvCurrentLocation;
    @BindView(R.id.myCardView)
    CardView locationCard;
    @BindView(R.id.avi)
    AVLoadingIndicatorView avi;
    @BindView(R.id.chart)
    RadarChart chart;
    @BindView(R.id.ivMenu)
    ImageView ivMenu;

    Animation fade;
    MediaPlayer mp;


    //SharedPreferences locationSharedPreferences;
    // public static final String myLocationPrefs = "locationPrefs" ;
    // public static final String latPrefs = "latitude";
    //public static final String lngPrefs = "longitude";
    // public static final String aqiPrefs = "aqiValue";
    // TODO: Declare a LocationManager and a LocationListener here:
    LocationManager mLocationManager;
    LocationListener mLocationListener;
    double latitude, longitude;
    String NETWORK_LOCATION_PROVIDER = LocationManager.NETWORK_PROVIDER;
    final long MIN_TIME = 0;        // Time between location updates (5000 milliseconds or 5 seconds)
    final float MIN_DISTANCE = 0;  // Distance between location updates (1000m or 1km)
    public static boolean gps_enabled = false;
    public static boolean network_enabled = false;
    final int REQUEST_CODE = 1;

    @Override
    protected void onStart() {
        super.onStart();
        Log.e("onStart", "Called");
    }

    private void CheckOldAndroidVersion() {
        if (android.os.Build.VERSION.SDK_INT <= 22) {
            Log.e("sdkLess22", " right");
            LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            try {
                gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                Log.d("gps", "status: " + gps_enabled);
            } catch (Exception ex) {
                Log.e("gps", ex.toString());
            }
            try {
                network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                Log.d("network", "status: " + gps_enabled);
            } catch (Exception ex) {
                Log.e("network", ex.toString());
            }
            if (!gps_enabled && !network_enabled) {
                displayPromptForEnablingGPS(this);
            }
        } else {
            Log.e("sdkGreater22", " right");
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE);
                Log.e("permGrantedInOnresume", "right");
            } else {
                displayPromptForEnablingGPS(this);
                Log.e("permGrantedInOnresume", "right");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("onResume", "Called");
        CheckOldAndroidVersion();
        getAqiForCurrentLocation();
        Log.e("getWeatherForCurrentLoc", "Called from onResume");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        //ToDo: for the sake of the Radar
        //******************************************
        //************************************************************
        //************************************************************
        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(140, 0));
        entries.add(new Entry(250, 1));
        entries.add(new Entry(320, 2));
        entries.add(new Entry(470, 3));
        entries.add(new Entry(360, 4));


       // ArrayList<Entry> entries2 = new ArrayList<>();
      //  entries2.add(new Entry(10, 0));
      //  entries2.add(new Entry(50, 1));
      //  entries2.add(new Entry(60, 2));
      //  entries2.add(new Entry(30, 3));
      //  entries2.add(new Entry(40, 4));
      //  entries2.add(new Entry(80, 5));

        RadarDataSet dataset_comp1 = new RadarDataSet(entries, "Today's Reading");

      //  RadarDataSet dataset_comp2 = new RadarDataSet(entries2, "Overall Readings");

        dataset_comp1.setColor(Color.RED);
        dataset_comp1.setDrawFilled(true);

     //   dataset_comp2.setColor(Color.RED);
      //  dataset_comp2.setDrawFilled(true);


        ArrayList<RadarDataSet> dataSets = new ArrayList<RadarDataSet>();
        dataSets.add(dataset_comp1);
       // dataSets.add(dataset_comp2);

        ArrayList<String> labels = new ArrayList<String>();
        labels.add("JUL");
        labels.add("SEP");
        labels.add("OCT");
        labels.add("NOV");
        labels.add("DEC");

        RadarData data = new RadarData(labels, dataSets);
        chart.setData(data);
        //String description = "Pollutants highest Components";
        //  chart.setDescription(description);
        chart.setWebLineWidthInner(1);
        //chart.setDescriptionColor(Color.RED);
        //chart.setSkipWebLineCount(10);
        chart.invalidate();
        chart.animate();

        //************************************************************
        //*********************Chart Ended Here***************************************
        //************************************************************


        //TODO:Typeface for Texts
        //Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/moodyrock.ttf");
        //   Typeface tf2 = Typeface.createFromAsset(getAssets(),"fonts/arizona.ttf");
        //   Typeface tf3 = Typeface.createFromAsset(getAssets(),"fonts/grand_hotel.otf");
        Typeface tfRobotoBlack = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Black.ttf");
        Typeface tfRobotoBlackItalic = Typeface.createFromAsset(getAssets(), "fonts/Roboto-BlackItalic.ttf");
        Typeface tfRobotoBold = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Bold.ttf");
        Typeface tfRobotoBoldCondensed = Typeface.createFromAsset(getAssets(), "fonts/Roboto-BoldCondensed.ttf");
        //   Typeface tfRobotoBoldItalic = Typeface.createFromAsset(getAssets(),"fonts/Roboto-BoldItalic.ttf");
        fade = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade);
        mp = MediaPlayer.create(getApplicationContext(), R.raw.btnclick);
        tvPlace.setTypeface(tfRobotoBoldCondensed, Typeface.BOLD);
        tvDate.setTypeface(tfRobotoBlack, Typeface.NORMAL);
        tvClock.setTypeface(tfRobotoBlack);
        tvAqi.setTypeface(tfRobotoBoldCondensed);
        tvAqiComment.setTypeface(tfRobotoBlackItalic);
        tvLastRefresh.setTypeface(tfRobotoBlackItalic);
        tvCurrentLocation.setTypeface(tfRobotoBold);
        tvCurrentLocation.setTextSize(18);
        tvCurrentLocation.setAnimation(fade);
        Date date = Calendar.getInstance().getTime();
        String dayOfTheWeek = (String) DateFormat.format("EEEE", date); // Thursday
        String day = (String) DateFormat.format("dd", date); // 20
        String monthString = (String) DateFormat.format("MMM", date); // Jun

        tvDate.setText(dayOfTheWeek + ", " + monthString + " " + day);
        locationCard.setCardBackgroundColor(Color.TRANSPARENT);
        locationCard.setCardElevation(4.0f);
        outdoorSharedpreferences = getSharedPreferences(outdoorPrefs, Context.MODE_PRIVATE);

        //Todo: Elastic Drawer to view Settings
        mDrawer.setTouchMode(ElasticDrawer.TOUCH_MODE_BEZEL);
        mDrawer.setOnDrawerStateChangeListener(new ElasticDrawer.OnDrawerStateChangeListener() {
            @Override
            public void onDrawerStateChange(int oldState, int newState) {
                if (newState == ElasticDrawer.STATE_CLOSED) {
                    Log.i("MainActivity", "Drawer STATE_CLOSED");
                    //   Toast.makeText(MainActivity.this, "onDrawerStateChange ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onDrawerSlide(float openRatio, int offsetPixels) {
                // Log.i("MainActivity", "openRatio=" + openRatio + " ,offsetPixels=" + offsetPixels);
                //  Toast.makeText(MainActivity.this, "onDrawerSlide ", Toast.LENGTH_SHORT).show();
            }
        });
        //Todo: Pull to refresh view
        mPullToRefreshView.setOnRefreshListener(new PullToRefreshView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPullToRefreshView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPullToRefreshView.setRefreshing(false);
                        Toast.makeText(MainActivity.this, "Refreshing..", Toast.LENGTH_SHORT).show();
                        getAqiForCurrentLocation();
                        Date date = Calendar.getInstance().getTime();
                        String dayOfTheWeek = (String) DateFormat.format("EEEE", date); // Thursday
                        String day = (String) DateFormat.format("dd", date); // 20
                        String monthString = (String) DateFormat.format("MMM", date); // Jun
                        String hourString = (String) DateFormat.format("HH", date); // Jun
                        String minuteString = (String) DateFormat.format("mm", date);
                        tvLastRefresh.setText(hourString + ":" + minuteString + " " + dayOfTheWeek + ", " + monthString + " " + day); // we are
                    }
                }, 900);
            }
        });


    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isActivated()) {
            Toasty.info(MainActivity.this, "drawer is activated", Toast.LENGTH_SHORT, false).show();
            mDrawer.closeMenu(true);
        } else {
            super.onBackPressed();
            Toasty.error(MainActivity.this, "Closed", Toast.LENGTH_SHORT, false).show();
        }
    }


    // TODO: Add getAqiForCurrentLocation() here:
    private void getAqiForCurrentLocation() {
        avi.show();
        Log.e("getAQI", "Called");
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                Log.d("onLocationChanged", "called");
                latitude = location.getLatitude();
                longitude = location.getLongitude();

                String latReq = Double.toString(latitude);
                String lonReq = Double.toString(longitude);

                Log.e("onLocationChanged", "Latitude =" + latitude + " longitude =" + longitude);
                if (latitude != 0.0 && longitude != 0.0) {
                    Log.e("location", "location Achieved");
                    new FindMe(MainActivity.this).execute();
                    RequestParams params = new RequestParams();
                    params.put("lat", latReq);
                    params.put("lon", lonReq);
                   // params.put("key", KEY);
                    params.put("APPID", APPID);
                    letsDoSomeNetworkingOutdoor(params);
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d("onProviderDisabled", "called");
            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE);
            return;
        }

        mLocationManager.requestLocationUpdates(NETWORK_LOCATION_PROVIDER, MIN_TIME, MIN_DISTANCE, mLocationListener);
    }


    //TODO: Location Listeners
    @Override
    public void onLocationChanged(Location location) {
        //double lat =  location.getLatitude();
        // double lng = location.getLongitude();
        /*
        SharedPreferences.Editor editor = locationSharedPreferences.edit();
        editor.putString(latPrefs, lat+"");
        editor.putString(lngPrefs, lng+"");
        //editor.putString(aqiPrefs, e);
        editor.commit();
        */
        Log.e("onLocationChanged", "Called");
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    //TODO: AsyncTask to fetch user location
    private class FindMe extends AsyncTask<Void, Void, String> {
        private Context appContext;

        private FindMe(Context appContext) {
            this.appContext = appContext;
        }

        @Override
        protected String doInBackground(Void... voids) {
            Log.e("doInBackground", "Called");
            Geocoder geocoder = new Geocoder(appContext, Locale.getDefault());
            List<Address> addresses;

            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
                if (addresses != null && addresses.size() > 0) {
                    Log.e("Adress", addresses.toString());
                    Address address = addresses.get(0);
                    // String cityName = address.getAddressLine(0);
                    StringBuilder strReturnedAddress = new StringBuilder("");

                    for (int i = 0; i <= 2; i++) {
                        strReturnedAddress.append(address.getAddressLine(i)).append(",");
                    }
                    String strAdd = strReturnedAddress.toString();
                    // String state = addresses.get(0).getAdminArea();
                    String city = addresses.get(0).getLocality();
                    String country = addresses.get(0).getCountryName();
                    String knownName = addresses.get(0).getFeatureName();
                    //  String full = address.getAddressLine(0);
                    Log.e("cityName", city);
                    // tvPlace.setText(cityName+" "+stateName);
                    if (knownName != null) {
                        myPlaceNow = knownName + ", " + city + ", " + country;
                        myPlaceNowSmall = knownName + ", " + city;
                        if (myPlaceNow.length() > 33) {
                            return myPlaceNowSmall.trim();
                        } else {
                            return myPlaceNow.trim();
                        }

                    }
                    Log.e("knownName", "is empty");
                    return strAdd;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return "Re-locating..";
            }
            return null;
        }

        protected void onPostExecute(String result) {
            tvPlace.setText(result);
            tvCurrentLocation.setText(result);
            avi.hide();
        }


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getAqiForCurrentLocation();
                Log.e("getWeatherForCurrentLoc", "Called from onRequest");
            } else {
                Log.d("aqi", "OnRequestPermission:permission Failed");
                // Toasty.error(this, "App needs Permision to fetch AQI", Toast.LENGTH_SHORT).show();
                //tvCurrentLocation.setText("Enable Permission");
                // tvPlace.setText("GPS Off");
            }
        }
    }

    public static void displayPromptForEnablingGPS(final Activity activity) {
        LocationManager lm = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;
        final String message = "Do you want open GPS setting?";
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            Log.e("gps", ex.toString());
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            Log.e("network", ex.toString());
        }
        if (!gps_enabled && !network_enabled) {


            builder.setMessage(message)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int id) {
                                    activity.startActivity(new Intent(action));
                                    d.dismiss();
                                }
                            })
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int id) {
                                    d.cancel();
                                }
                            });
            builder.create().show();
        }
    }

    //ToDO: Managing Clicks
    @OnClick(R.id.btnLocations)
    void locationButton() {
        mp.start();
        //btnLocation.startAnimation(fade);
        Intent loc = new Intent(MainActivity.this, SavedLocations.class);
        mDrawer.closeMenu();
        startActivity(loc);
    }

    @OnClick(R.id.btnNotification)
    void notificationbtn() {
        Toast.makeText(this, "btnNotification", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.btnWhatAqi)
    void whatsAQIbtn() {
        Toast.makeText(this, "What's AQI ?", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.btnBlog)
    void blogBtn() {
        // btnBlog.setAnimation(fade);
        Toasty.info(MainActivity.this, "Blog Redirecting .. ", Toast.LENGTH_SHORT, false).show();
        Intent blog = new Intent(MainActivity.this, Blog.class);
        startActivity(blog);
        mp.start();
        // mDrawer.closeMenu();
    }

    @OnClick(R.id.btnWebsite)
    void ourWebBtn() {
        btnWebsite.setAnimation(fade);
        mp.start();
        Toasty.info(MainActivity.this, "Webpage Redirecting .. ", Toast.LENGTH_SHORT, false).show();
        mp.start();
        //btnWebsite.setAnimation(fade);
        String url = "http://aqi.in/";
        Intent web = new Intent(Intent.ACTION_VIEW);
        // mDrawer.closeMenu();
        web.setData(Uri.parse(url));
        startActivity(web);


    }

    @OnClick(R.id.btnAboutUs)
    void aboutUsBtn() {
        mp.start();
        //btnAboutUs.startAnimation(fade);
        Intent aboutUs = new Intent(MainActivity.this, AboutUs.class);
        //  mDrawer.closeMenu();
        startActivity(aboutUs);
    }

    @OnClick(R.id.btnFacebook)
    void facebookBtn() {
        mp.start();
        //btnFacebook.setAnimation(fade);
        // mDrawer.closeMenu();
        Toasty.info(MainActivity.this, "Facebook Redirecting", Toast.LENGTH_SHORT).show();
        startActivity(newFacebookIntent(getPackageManager(), url));
    }

    @OnClick(R.id.ivMenu)
    void openMenu(View v){
        if(!mDrawer.isActivated()) {
            ivMenu.setAnimation(fade);
            mDrawer.openMenu();
        }

    }

    @OnClick(R.id.myCardView)
    void goToMaps() {
        Intent mapIntent = new Intent(MainActivity.this, MapsActivity.class);
        if (latitude != 0 && longitude != 0) {
            mapIntent.putExtra("latitude", latitude);
            mapIntent.putExtra("longitude", longitude);
            mapIntent.putExtra("aqi", "22");
            mapIntent.putExtra("location", myPlaceNow);
        }
        startActivity(mapIntent);
    }

    //facebook re-directing
    public static Intent newFacebookIntent(PackageManager pm, String url) {
        Uri uri = Uri.parse(url);
        try {
            ApplicationInfo applicationInfo = pm.getApplicationInfo("com.facebook.katana", 0);
            if (applicationInfo.enabled) {
                uri = Uri.parse("fb://facewebmodal/f?href=" + url);
                Log.d("facebookredirecting", "welldone");
            }
        } catch (PackageManager.NameNotFoundException ignored) {
            Log.d("facebookredirecting", "badme");
        }
        return new Intent(Intent.ACTION_VIEW, uri);
    }


    //Todo onNoNeedGettingDataFromServer
    private void getSavedValues() {
        String mPlaceName = outdoorSharedpreferences.getString(placeName, "NA");
        int mAqi = outdoorSharedpreferences.getInt(aqi, 0);
        int mPm25 = outdoorSharedpreferences.getInt(pm25, 0);
        double mTemperature = outdoorSharedpreferences.getFloat(temp, 0.0f);
        int mHumidity = outdoorSharedpreferences.getInt(humid, 0);
        String mTime = outdoorSharedpreferences.getString(time, "NA");
        OutdoorDataModel outdoorData = new OutdoorDataModel(mPlaceName, mAqi, mPm25, mTemperature, mHumidity, mTime);
        updateOutdoorUi(outdoorData);
        Toast.makeText(MainActivity.this, "Already have The latest Data", Toast.LENGTH_SHORT).show();
    }


    private void letsDoSomeNetworkingOutdoor(RequestParams requestParams) {
        avi.show();

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(SENSOR_OUTDOOR_URL, requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
               // OutdoorDataModel outdoorData = OutdoorDataModel.fromJson(response);
                if (response != null) {
                    Log.d("json ", "success : " + response.toString());
                }
                //if (outdoorData == null || outdoorData.getmPm25() == 0) {
                // getSavedValues();
                avi.setVisibility(View.INVISIBLE);
                return;
                // }
                // boolean saved = saveOutdoorValues(outdoorData);
                // if(saved){
                //     updateOutdoorUi(outdoorData);
                // }
                //  avi.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, e, errorResponse);
                //getSavedValues();
                avi.setVisibility(View.INVISIBLE);
                if (errorResponse != null) {
                    Log.d("json ", "failed : " + errorResponse.toString());
                }
            }
        });
    }

    private boolean saveOutdoorValues(OutdoorDataModel outdoorData) {
        SharedPreferences.Editor editor = outdoorSharedpreferences.edit();
        editor.putString(placeName, outdoorData.getmPlaceName());
        editor.putInt(aqi, outdoorData.getmAqi());
        editor.putInt(pm25, outdoorData.getmPm25());
        editor.putFloat(temp, (float) outdoorData.getmTemperature());
        editor.putInt(humid, outdoorData.getmHumidity());
        editor.putString(time, outdoorData.getmTimeStamp());
        return editor.commit();
    }

    private void updateOutdoorUi(OutdoorDataModel outdoorData) {
        Palette palette = new Palette();
        // outdoorCardView.setBackgroundColor(palette.getTxtColor(this, palette.getConditionAqi(outdoorData.getmAqi())));
        //  outdoorCardText.setText(palette.getConditionString(outdoorData.getmAqi()));
        //  ivPm25SignOutdoor.setImageResource(outdoorData.getmPm25Drawable());
        //  outdoorMeter.speedTo(outdoorData.getmAqi(), 1000);
        //  tvOutdoorPM25.setText(String.format(Locale.getDefault(), "%d", outdoorData.getmPm25()));
        //  tvOutdoorPM25.setTextColor(palette.getTxtColor(this, palette.getConditionPm25(outdoorData.getmPm25())));
        //  tvOutdoorTemp.setText(String.format(Locale.getDefault(), "%1$,.1f", outdoorData.getmTemperature()));
        //   tvOutdoorHumid.setText(String.format(Locale.getDefault(), "%d", outdoorData.getmHumidity()));
        // tvOutdoorTime.setText( outdoorData.getmTimeStamp());
    }

    //For Menu to Override method
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@Nullable MenuItem item) {
        return true;
    }


}

