package com.example.myapplication;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;

import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import net.daum.mf.map.api.CalloutBalloonAdapter;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.xml.parsers.ParserConfigurationException;

import static android.util.TypedValue.COMPLEX_UNIT_SP;

public class MainActivity extends AppCompatActivity implements MapView.CurrentLocationEventListener, MapView.MapViewEventListener, MapView.POIItemEventListener{
    GpsTracker gpsTracker;
    private static final String LOG_TAG = "MainActivity";
    private MapView mapView;
    private ViewGroup mapViewContainer;
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    Toolbar main_toolbar;
    SearchByAddress search_result;
    String current_address;
    MapPOIItem[] marker;
    private SearchView mSearchView;
    MyCarInfo carInfo = MyCarInfo.getInstance();

    class CustomCalloutBalloonAdapter implements CalloutBalloonAdapter {    //???????????? api??? ????????? ????????? - ?????? ?????? ??????
        private final View mCalloutBalloon;

        public CustomCalloutBalloonAdapter() {
            mCalloutBalloon = getLayoutInflater().inflate(R.layout.custom_balloon, null);
        }

        @Override
        public View getCalloutBalloon(MapPOIItem poiItem) {
            //((ImageView) mCalloutBalloon.findViewById(R.id.badge)).setImageResource(R.drawable.ic_launcher);
            ChargeStationInfo marker = new ChargeStationInfo();
            for(int i =0 ; i < search_result.getStation_size(); i++){
                if(search_result.getStations()[i].getCsNm().equals(poiItem.getItemName())){
                    marker = search_result.getStations()[i];
                    break;
                }
            }
            int[] total = new int[10];
            int[] able = new int[10];
            int[] fast_slow = new int[10];  //1 ?????? 2 ??????
            for(int i = 0; i < marker.getMachines_size(); i++){
                total[marker.getMachines()[i].getCpTp() - 1]++;
                if(marker.getMachines()[i].getCpStat() == 1){
                    able[marker.getMachines()[i].getCpTp() - 1]++;
                }
                if(marker.getMachines()[i].getChargeTp() == 1){
                    fast_slow[marker.getMachines()[i].getCpTp() - 1] = 1;
                }
                if(marker.getMachines()[i].getChargeTp() == 2){
                    fast_slow[marker.getMachines()[i].getCpTp() - 1] = 2;
                }
            }
            ((TextView) mCalloutBalloon.findViewById(R.id.balloon_title)).setText(marker.getCsNm());

            String[] temp_zip = new String[10];
            String[] type = {"B??????(5???)","C??????(5???)", "BC??????(5???)","BC??????(7???)", "DC?????????","AC3???", "DC??????","DC?????????+DC??????", "DC?????????+AC3???","DC?????????+DC??????+AC3???"};
            for(int i = 0; i < 10; i++) {
                String temp = "";
                if (fast_slow[i] == 1) {
                    temp += "?????? ";
                } else {
                    temp += "?????? ";
                }
                temp += type[i]+" ??? " + total[i] + "??? ??? " + able[i] + "??? ??????";
                temp_zip[i] = temp;
            }

            while(true) {
                if (able[9] >= 1) {
                    ((TextView) mCalloutBalloon.findViewById(R.id.balloon_cpTp10)).setText(temp_zip[9]);
                    ((TextView) mCalloutBalloon.findViewById(R.id.balloon_cpTp10)).setTextSize(COMPLEX_UNIT_SP, 10);
                } else {
                    ((TextView) mCalloutBalloon.findViewById(R.id.balloon_cpTp10)).setText("");
                    ((TextView) mCalloutBalloon.findViewById(R.id.balloon_cpTp10)).setTextSize(COMPLEX_UNIT_SP, 0);
                }
                if (able[8] >= 1) {
                    ((TextView) mCalloutBalloon.findViewById(R.id.balloon_cpTp9)).setText(temp_zip[8]);
                    ((TextView) mCalloutBalloon.findViewById(R.id.balloon_cpTp9)).setTextSize(COMPLEX_UNIT_SP, 10);
                } else {
                    ((TextView) mCalloutBalloon.findViewById(R.id.balloon_cpTp9)).setText("");
                    ((TextView) mCalloutBalloon.findViewById(R.id.balloon_cpTp9)).setTextSize(COMPLEX_UNIT_SP, 0);
                }
                if (able[7] >= 1) {
                    ((TextView) mCalloutBalloon.findViewById(R.id.balloon_cpTp8)).setText(temp_zip[7]);
                    ((TextView) mCalloutBalloon.findViewById(R.id.balloon_cpTp8)).setTextSize(COMPLEX_UNIT_SP, 10);
                } else {
                    ((TextView) mCalloutBalloon.findViewById(R.id.balloon_cpTp8)).setText("");
                    ((TextView) mCalloutBalloon.findViewById(R.id.balloon_cpTp8)).setTextSize(COMPLEX_UNIT_SP, 0);
                }
                if (able[6] >= 1) {
                    ((TextView) mCalloutBalloon.findViewById(R.id.balloon_cpTp7)).setText(temp_zip[6]);
                    ((TextView) mCalloutBalloon.findViewById(R.id.balloon_cpTp7)).setTextSize(COMPLEX_UNIT_SP, 10);
                } else {
                    ((TextView) mCalloutBalloon.findViewById(R.id.balloon_cpTp7)).setText("");
                    ((TextView) mCalloutBalloon.findViewById(R.id.balloon_cpTp7)).setTextSize(COMPLEX_UNIT_SP, 0);
                }
                if (able[5] >= 1) {
                    ((TextView) mCalloutBalloon.findViewById(R.id.balloon_cpTp6)).setText(temp_zip[5]);
                    ((TextView) mCalloutBalloon.findViewById(R.id.balloon_cpTp6)).setTextSize(COMPLEX_UNIT_SP, 10);
                } else {
                    ((TextView) mCalloutBalloon.findViewById(R.id.balloon_cpTp6)).setText("");
                    ((TextView) mCalloutBalloon.findViewById(R.id.balloon_cpTp6)).setTextSize(COMPLEX_UNIT_SP, 0);
                }
                if (able[4] >= 1) {
                    ((TextView) mCalloutBalloon.findViewById(R.id.balloon_cpTp5)).setText(temp_zip[4]);
                    ((TextView) mCalloutBalloon.findViewById(R.id.balloon_cpTp5)).setTextSize(COMPLEX_UNIT_SP, 10);
                } else {
                    ((TextView) mCalloutBalloon.findViewById(R.id.balloon_cpTp5)).setText("");
                    ((TextView) mCalloutBalloon.findViewById(R.id.balloon_cpTp5)).setTextSize(COMPLEX_UNIT_SP, 0);
                }
                if (able[3] >= 1) {
                    ((TextView) mCalloutBalloon.findViewById(R.id.balloon_cpTp4)).setText(temp_zip[3]);
                    ((TextView) mCalloutBalloon.findViewById(R.id.balloon_cpTp4)).setTextSize(COMPLEX_UNIT_SP, 10);
                } else {
                    ((TextView) mCalloutBalloon.findViewById(R.id.balloon_cpTp4)).setText("");
                    ((TextView) mCalloutBalloon.findViewById(R.id.balloon_cpTp4)).setTextSize(COMPLEX_UNIT_SP, 0);
                }
                if (able[2] >= 1) {
                    ((TextView) mCalloutBalloon.findViewById(R.id.balloon_cpTp3)).setText(temp_zip[2]);
                    ((TextView) mCalloutBalloon.findViewById(R.id.balloon_cpTp3)).setTextSize(COMPLEX_UNIT_SP, 10);
                } else {
                    ((TextView) mCalloutBalloon.findViewById(R.id.balloon_cpTp3)).setText("");
                    ((TextView) mCalloutBalloon.findViewById(R.id.balloon_cpTp3)).setTextSize(COMPLEX_UNIT_SP, 0);
                }
                if (able[1] >= 1) {
                    ((TextView) mCalloutBalloon.findViewById(R.id.balloon_cpTp2)).setText(temp_zip[1]);
                    ((TextView) mCalloutBalloon.findViewById(R.id.balloon_cpTp2)).setTextSize(COMPLEX_UNIT_SP, 10);
                } else {
                    ((TextView) mCalloutBalloon.findViewById(R.id.balloon_cpTp2)).setText("");
                    ((TextView) mCalloutBalloon.findViewById(R.id.balloon_cpTp2)).setTextSize(COMPLEX_UNIT_SP, 0);
                }
                if (able[0] >= 1) {
                    ((TextView) mCalloutBalloon.findViewById(R.id.balloon_cpTp1)).setText(temp_zip[0]);
                    ((TextView) mCalloutBalloon.findViewById(R.id.balloon_cpTp1)).setTextSize(COMPLEX_UNIT_SP, 10);
                } else {
                    ((TextView) mCalloutBalloon.findViewById(R.id.balloon_cpTp1)).setText("");
                    ((TextView) mCalloutBalloon.findViewById(R.id.balloon_cpTp1)).setTextSize(COMPLEX_UNIT_SP, 0);
                }
                break;
            }

            return mCalloutBalloon;

        }

        @Override
        public View getPressedCalloutBalloon(MapPOIItem poiItem) {
            return mCalloutBalloon;
//            return null;
        }

    }

    @Override
    public void onRestart(){    //'????????????'?????? ????????? ??? ?????? ????????????
        super.onRestart();
        if(carInfo.cpTp == 0){  //?????? ?????? ????????? ?????? ?????????
            resetFilter();
            return;
        }
        setFilter(new String[]{forFilter(carInfo.cpTp)});
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getHashKey();
        setContentView(R.layout.activity_main);

        search_result = null;
        mSearchView = findViewById(R.id.searchView);

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {    //????????? ????????????
//                if(s.equals("??????")){   //?????? ?????? ?????????
//                    resetFilter();
//                    return true;
//                }

                Log.d("search = ", s);
                try {
                    search(s);  //?????? ????????? api ??????
                } catch (SAXException e) {
                    e.printStackTrace();
                    return false;
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                    return false;
                } catch (ParseException e) {
                    e.printStackTrace();
                    return false;
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    return false;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return false;
                }

                double[] latilongi = AddressTodouble(s);
                if(latilongi[0] == 0.0){    //?????? ??????->?????? ????????? ????????? ??? ?????? ???????????? ???????????? ??????
                    if(search_result.getStation_size() == 0){//API????????? ????????? ????????? ??????
                        return false;
                    }
                    mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(search_result.getStations()[0].getLat(), search_result.getStations()[0].getLongi()), true);
                }
                else{//???????????? ??????
                    mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(latilongi[0], latilongi[1]), true);
                }
                //?????? ?????? ??????
                mapView.setZoomLevel(5,true);
                //?????? ????????? ?????? ??????
                marker = new MapPOIItem[search_result.getStation_size()];
                for(int i = 0; i <search_result.getStation_size(); i++){
                    marker[i] = new MapPOIItem();
                    marker[i].setShowCalloutBalloonOnTouch(false);  //????????? ???????????? ??????
                    marker[i].setItemName(search_result.getStations()[i].getCsNm());    //????????? ????????? ???????????? ??????
                    marker[i].setTag(i);
                    Log.d("station get", "" + search_result.getStations()[i].getLat());
                    Log.d("station get", "" + search_result.getStations()[i].getLongi());
                    marker[i].setMapPoint(MapPoint.mapPointWithGeoCoord(search_result.getStations()[i].getLat(), search_result.getStations()[i].getLongi()));
                    marker[i].setMarkerType(MapPOIItem.MarkerType.BluePin); // ???????????? ???????????? BluePin ?????? ??????.
                    marker[i].setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // ????????? ???????????????, ???????????? ???????????? RedPin ?????? ??????.
//                    mapView.addPOIItem(marker[i]);
                }

                mapView.addPOIItems(marker);

                if(carInfo.cpTp != 0){
                    setFilter(new String[]{forFilter(carInfo.cpTp)});
                }

                //?????? ????????? ?????? ??? ???????????? ????????? ?????????
                InputMethodManager manager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);


                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        if (checkLocationServicesStatus()) {    //?????? ??????
            checkRunTimePermission();
        } else {
            showDialogForLocationServiceSetting();
        }

        //?????? ?????? ?????? ????????? ??? ???????????? ??????
        Button MyCarInfoButton = findViewById(R.id.F_button4);
        MyCarInfoButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MyCarInfoActivity.class));
            }
        });
        
        
        /****************************************************************/
        //AVD????????? ???????????? API??? ?????? ??????!!!
        //AVD?????? ????????? ????????? ??????

        gpsTracker = new GpsTracker(MainActivity.this);
        double lati = gpsTracker.getLatitude();
        double longi = gpsTracker.getLongitude();

        current_address = getCurrentAddress(lati,longi);
        Toast myToast = Toast.makeText(this.getApplicationContext(),current_address, Toast.LENGTH_SHORT);
        myToast.show();     //?????? ????????? ????????? ?????? ?????? '???????????? '??? ???????????? ??????????????? ?????? '???' ????????? ????????? ?????? ????????? ?????? ??????

        String[] cut_address = current_address.split(" ");

        try {
            search(cut_address[1]);   //?????? ?????? ???????????? '???' ?????? ?????? ????????? ???????????? ?????? ?????? ?????? ???????????? ?????? ????????? ???????????? ?????? ??????
            //search(cut_address[1] +" " + cut_address[2]);         //?????? ?????? ???????????? '???' ?????? ?????? ????????? ????????????
        } catch (SAXException e) {                                  //'???' ???????????? ?????? ????????? ????????? ???????????? ???????????? ????????? ?????????
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        mapView = new MapView(this);
//        mapView.setCalloutBalloonAdapter(new CustomCalloutBalloonAdapter());    //????????? ????????? ??????
        mapView.setPOIItemEventListener(this);  //?????? ???????????? ??? ?????? ???????????? ????????? ??????
        ViewGroup mapViewContainer = (ViewGroup) findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);
        mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(lati, longi), true);

        marker = new MapPOIItem[search_result.getStation_size()];
        for(int i = 0; i <search_result.getStation_size(); i++){
            marker[i] = new MapPOIItem();
            marker[i].setShowCalloutBalloonOnTouch(false);  //????????? ???????????? ??????
            marker[i].setItemName(search_result.getStations()[i].getCsNm());    //????????? ????????? ???????????? ??????
            marker[i].setTag(i);
            Log.d("station get", "" + search_result.getStations()[i].getLat());
            Log.d("station get", "" + search_result.getStations()[i].getLongi());
            marker[i].setMapPoint(MapPoint.mapPointWithGeoCoord(search_result.getStations()[i].getLat(), search_result.getStations()[i].getLongi()));
            marker[i].setMarkerType(MapPOIItem.MarkerType.BluePin); // ???????????? ???????????? BluePin ?????? ??????.
            marker[i].setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // ????????? ???????????????, ???????????? ???????????? RedPin ?????? ??????.
//            mapView.addPOIItem(marker[i]);
        }

        mapView.addPOIItems(marker);

        //???????????? ??????
        /************************************************************/
        //?????? ????????? ?????? ?????? ??????
        if(carInfo.cpTp != 0) {
            setFilter(new String[]{forFilter(carInfo.cpTp)});
        }
//        setFilter(new String[]{"BC??????(5???)"});
    }

    private void getHashKey(){  //??????????????? ????????? ????????? ????????? ??????
        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageInfo == null)
            Log.e("KeyHash", "KeyHash:null");

        for (Signature signature : packageInfo.signatures) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            } catch (NoSuchAlgorithmException e) {
                Log.e("KeyHash", "Unable to get MessageDigest. signature=" + signature, e);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {
            boolean check_result = true;
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }
            if (check_result) {
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {
                    Toast.makeText(MainActivity.this, "???????????? ?????????????????????. ?????? ?????? ???????????? ???????????? ??????????????????.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "???????????? ?????????????????????. ??????(??? ??????)?????? ???????????? ???????????? ?????????. ", Toast.LENGTH_LONG).show();
                }
            }

        }
    }

    void checkRunTimePermission() {
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
        }
        else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, REQUIRED_PERMISSIONS[0])) {
                Toast.makeText(MainActivity.this, "??? ?????? ??????????????? ?????? ?????? ????????? ???????????????.", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }
                else {
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }
        }
    }

    private void showDialogForLocationServiceSetting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("?????? ????????? ????????????");
        builder.setMessage("?????? ???????????? ???????????? ?????? ???????????? ???????????????.\n"
                + "?????? ????????? ???????????????????");
        builder.setCancelable(true);
        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case GPS_ENABLE_REQUEST_CODE:
                //???????????? GPS ?????? ???????????? ??????
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        Log.d("@@@", "onActivityResult : GPS ????????? ?????????");
                        checkRunTimePermission();
                        return;
                    }
                }
                break;
        }
    }
    
    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint mapPoint, float v) {

    }

    @Override
    public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {

    }

    @Override
    public void onCurrentLocationUpdateFailed(MapView mapView) {

    }

    @Override
    public void onCurrentLocationUpdateCancelled(MapView mapView) {

    }

    @Override
    public void onMapViewInitialized(MapView mapView) {

    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {

    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {

    }

    @Override   //????????? ??????xml ??????
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override   //?????? ????????? https://www.hanumoka.net/2017/10/28/android-20171028-android-toolbar/ ?????? - ?????? ?????? ??????
    public boolean onOptionsItemSelected(MenuItem item) {
        //return super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.menu_search:
                // User chose the "Settings" item, show the app settings UI...
                Toast.makeText(getApplicationContext(), "???????????? ?????? ?????????", Toast.LENGTH_LONG).show();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                Toast.makeText(getApplicationContext(), "????????? ?????? ?????????", Toast.LENGTH_LONG).show();
                return super.onOptionsItemSelected(item);

        }
    }

    public void clickBtn(View view){    //???????????? ??????????????? ??????
//        setFilter(new String[]{"DC??????"});
//        Log.d("zoomlevel = ", mapView.getZoomLevelFloat()+"");
        Intent intent = new Intent(this, Emergency.class);
        startActivity(intent);
    }


    /*
    * search??? ?????? search_result??? ?????? ????????? (?????? ??????!!!) - ?????? ????????? ??????
    * ????????? stations ????????? ?????? ?????? ????????? ????????? get??? ?????? ????????? ?????????
    * ???????????? ??? ex)  search_result.getStations()[3].getLongi()
    * ?????????
    * */
    void search(String input) throws SAXException, ParserConfigurationException, ParseException, IOException, ExecutionException, InterruptedException {
        SearchByAddress result = new SearchByAddress();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        Log.d("api_before", "this-"+input);

        SearchByAddress finalResult = result;
        Future<SearchByAddress> future = executor.submit(() -> {    //api??? ?????? ????????????????????? ?????? ??????
            SearchByAddress temp = finalResult;
            Log.d("api", "this-"+input);
            try {
                finalResult.XmlToStationList(finalResult.APISearch(input));
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return temp;
        });
        result = future.get();
        if(result.getStation_size() == 0){
            Toast.makeText(MainActivity.this, "API??? ?????? ????????? ????????????!", Toast.LENGTH_LONG).show();
            return;
        }
        if(search_result == null){
            search_result = result;
        }
        else{
            int new_size = search_result.getStation_size() + result.getStation_size();
            ChargeStationInfo[] new_ch = new ChargeStationInfo[new_size];
            for(int i = 0; i < new_size; i++){
                if(i < search_result.getStation_size()){
                    new_ch[i] = search_result.getStations()[i];
                }
                else{
                    new_ch[i] = result.getStations()[i - search_result.getStation_size()];
                }
            }
            search_result.setStation_size(new_size);
            search_result.setStations(new_ch);

        }
        Log.d("api_after", "this-"+input);
        Log.d("api", "lat = " + result.getStations()[0].getLat());
        Log.d("api", "longi = " + result.getStations()[0].getLongi());
    }

    public String getCurrentAddress( double latitude, double longitude) {
        //????????????... GPS??? ????????? ??????
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        } catch (IOException ioException) {
            //???????????? ??????
            Toast.makeText(this, "???????????? ????????? ????????????", Toast.LENGTH_LONG).show();
            return "???????????? ????????? ????????????";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "????????? GPS ??????", Toast.LENGTH_LONG).show();
            return "????????? GPS ??????";

        }

        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "?????? ?????????", Toast.LENGTH_LONG).show();
            return "?????? ?????????";

        }

        Address address = addresses.get(0);
        return address.getAddressLine(0).toString()+"\n";

    }

    public double[] AddressTodouble( String s) {
        //????????????... GPS??? ????????? ??????
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocationName(s,10);
        }
        catch (IOException ioException) {
        }
        catch (IllegalArgumentException illegalArgumentException) {
        }

        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "?????? ?????????", Toast.LENGTH_LONG).show();
            return new double[]{0.0, 0.0};
        }

        Address address = addresses.get(0);
        double[] re = new double[2];
        re[0] = address.getLatitude();
        re[1] = address.getLongitude();
        return re;
    }

    public String forFilter(int a){ //????????? ????????? ????????? ??? ???????????? ??????
        String[] type = {"B??????(5???)","C??????(5???)", "BC??????(5???)","BC??????(7???)", "DC?????????","AC3???", "DC??????","DC?????????+DC??????", "DC?????????+AC3???","DC?????????+DC??????+AC3???"};
        if(a == 0){
            return "";
        }
        return type[a - 1];
    }

    public void setFilter(String[] selects){    //selects??? ???????????? ?????? ?????? ???????????? ??????
        String[] type = {"B??????(5???)","C??????(5???)", "BC??????(5???)","BC??????(7???)", "DC?????????","AC3???", "DC??????","DC?????????+DC??????", "DC?????????+AC3???","DC?????????+DC??????+AC3???"};
        int[] selects_int = new int[selects.length];
        for(int i = 0 ; i < selects_int.length;i++){    //????????? ????????? ??????
            for(int j = 0; j < 10; j++){
                if(selects[i].equals(type[j])){
                    selects_int[i] = j + 1;
                    break;
                }
            }
        }
        List<List<Integer>> station_n = new ArrayList<>();  //select[i]??? ???????????? ???????????? ?????????
        for(int i = 0 ; i < selects_int.length; i++){
            List<Integer> row = new ArrayList<>();
            for(int j=0; j < search_result.getStation_size(); j++){
                if(search_result.getStations()[j].chargeType(selects_int[i]) == true){
                    row.add(j);
                }
            }
            station_n.add(row);
        }
        List<Integer> clean_station_list = new ArrayList<>();   //????????? ????????? ???????????? ?????? ?????????
        for(int i = 0 ; i < station_n.size(); i++){
            for(int j=0; j < station_n.get(i).size(); j++){
                if(!clean_station_list.contains(station_n.get(i).get(j))){
                    clean_station_list.add(station_n.get(i).get(j));
                }
            }
        }

        mapView.removeAllPOIItems();                //?????? ???????????? ?????? ??? ??????
        marker = new MapPOIItem[clean_station_list.size()];
        for(int i = 0; i <clean_station_list.size(); i++){
            int temp = clean_station_list.get(i);
            marker[i] = new MapPOIItem();
            marker[i].setShowCalloutBalloonOnTouch(false);  //????????? ???????????? ??????
            marker[i].setItemName(search_result.getStations()[i].getCsNm());    //????????? ????????? ???????????? ??????
            marker[i].setTag(i);
            marker[i].setMapPoint(MapPoint.mapPointWithGeoCoord(search_result.getStations()[temp].getLat(), search_result.getStations()[temp].getLongi()));
            marker[i].setMarkerType(MapPOIItem.MarkerType.YellowPin); // ????????? ??????????????? ??????
            marker[i].setSelectedMarkerType(MapPOIItem.MarkerType.BluePin); // ????????? ???????????????, ??????????????? ??????
            mapView.addPOIItem(marker[i]);
        }

//        mapView.addPOIItems(marker);

    }

    public void resetFilter(){
        mapView.removeAllPOIItems();            //?????? ???????????? ?????? ??? ??????

        marker = new MapPOIItem[search_result.getStation_size()];
        for(int i = 0; i <search_result.getStation_size(); i++){
            marker[i] = new MapPOIItem();
            marker[i].setShowCalloutBalloonOnTouch(false);  //????????? ???????????? ??????
            marker[i].setItemName(search_result.getStations()[i].getCsNm());    //????????? ????????? ???????????? ??????
            marker[i].setTag(i);
            Log.d("station get", "" + search_result.getStations()[i].getLat());
            Log.d("station get", "" + search_result.getStations()[i].getLongi());
            marker[i].setMapPoint(MapPoint.mapPointWithGeoCoord(search_result.getStations()[i].getLat(), search_result.getStations()[i].getLongi()));
            marker[i].setMarkerType(MapPOIItem.MarkerType.BluePin); // ????????? ?????? ???????????? ??????
            marker[i].setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // ????????? ???????????????, ?????? ???????????? ??????
//                    mapView.addPOIItem(marker[i]);
        }

        mapView.addPOIItems(marker);
//        Toast.makeText(this, "?????? ??????!", Toast.LENGTH_LONG).show();

    }

    @Override
    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) { //????????? ???????????? ????????? ??? ????????? ????????? ??????
        String url = "kakaomap://route?sp="+ gpsTracker.getLatitude() +","+gpsTracker.getLongitude();
        url += "&ep="+ mapPOIItem.getMapPoint().getMapPointGeoCoord().latitude+","+ mapPOIItem.getMapPoint().getMapPointGeoCoord().longitude+"&by=CAR";

        ChargeStationInfo now = new ChargeStationInfo();
        for(int i = 0; i < search_result.getStation_size(); i++){
            if(mapPOIItem.getItemName().equals(search_result.getStations()[i].getCsNm())){
                now = search_result.getStations()[i];
                break;
            }
        }

        String[] temp = new String[10];
        int[] total = new int[10];
        int[] able = new int[10];
        String[] type = {"B??????(5???)","C??????(5???)", "BC??????(5???)","BC??????(7???)", "DC?????????","AC3???", "DC??????","DC?????????+DC??????", "DC?????????+AC3???","DC?????????+DC??????+AC3???"};
        String line = "\n";

        for(int i = 0;i < now.getMachines_size(); i++){
            total[now.getMachines()[i].getCpTp()-1]++;
            if(now.getMachines()[i].getCpStat() == 1){
                able[now.getMachines()[i].getCpTp()-1]++;
            }
        }
        for(int i = 0;i < 10; i++){
            temp[i] = "????????? ?????? : " + type[i] + "\n   ??? "+total[i]+"??? ??? "+able[i]+"??? ?????? ??????\n";
            if(able[i] != 0){
                line += temp[i];
            }
        }

        if(line.equals("\n")){
            line = "\n?????? ?????? ????????? ????????? ??????\n";
        }
        Intent it = new Intent(MainActivity.this, CustomNotiActivity.class);    //intent??? ???????????? ????????? ????????? ??? ?????????
        it.putExtra("station_name", now.getCsNm());
        it.putExtra("charger_info", line);
        it.putExtra("kakao", url);
        startActivity(it);

    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {

    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {

    }

    @Override
    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {

    }
}