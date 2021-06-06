package com.example.myapplication;

import android.app.Activity;
import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.Window;

import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;


import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import net.daum.mf.map.api.CalloutBalloonAdapter;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
    class CustomCalloutBalloonAdapter implements CalloutBalloonAdapter {
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
            int[] fast_slow = new int[10];  //1 완속 2 급속
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
            String[] type = {"B타입(5핀)","C타입(5핀)", "BC타입(5핀)","BC타입(7핀)", "DC차데모","AC3상", "DC콤보","DC차데모+DC콤보", "DC차데모+AC3상","DC차데모+DC콤보+AC3상"};
            for(int i = 0; i < 10; i++) {
                String temp = "";
                if (fast_slow[i] == 1) {
                    temp += "급속 ";
                } else {
                    temp += "완속 ";
                }
                temp += type[i]+" 총 " + total[i] + "개 중 " + able[i] + "개 작동";
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getHashKey();
        setContentView(R.layout.activity_main);

        search_result = null;
        mSearchView = findViewById(R.id.searchView);

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Log.d("search = ", s);
                try {
                    search(s);
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
                if(latilongi[0] == 0.0){    //만약 주소->좌표 변환이 안되면 그 지역 충전소를 중심으로 설정
                    if(search_result.getStation_size() == 0){//API에서도 검색이 안되면 종료
                        return false;
                    }
                    mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(search_result.getStations()[0].getLat(), search_result.getStations()[0].getLongi()), true);
                }
                else{//중심으로 이동
                    mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(latilongi[0], latilongi[1]), true);
                }
                //확대 정도 설정
                mapView.setZoomLevel(5,true);
                //검색 지역에 마커 추가
                marker = new MapPOIItem[search_result.getStation_size()];
                for(int i = 0; i <search_result.getStation_size(); i++){
                    marker[i] = new MapPOIItem();
                    marker[i].setShowCalloutBalloonOnTouch(false);  //말풍선 안보이게 하기
                    marker[i].setItemName(search_result.getStations()[i].getCsNm());    //충전소 명칭을 이름으로 표시
                    marker[i].setTag(i);
                    Log.d("station get", "" + search_result.getStations()[i].getLat());
                    Log.d("station get", "" + search_result.getStations()[i].getLongi());
                    marker[i].setMapPoint(MapPoint.mapPointWithGeoCoord(search_result.getStations()[i].getLat(), search_result.getStations()[i].getLongi()));
                    marker[i].setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
                    marker[i].setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
//                    mapView.addPOIItem(marker[i]);
                }

                mapView.addPOIItems(marker);

                InputMethodManager manager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);


                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        if (checkLocationServicesStatus()) {
            checkRunTimePermission();
        } else {
            showDialogForLocationServiceSetting();
        }

        /***************************************************/

        //AVD에서 돌릴땐 여기서 부터
        gpsTracker = new GpsTracker(MainActivity.this);
        double lati = gpsTracker.getLatitude();
        double longi = gpsTracker.getLongitude();

        current_address = getCurrentAddress(lati,longi);
        Toast myToast = Toast.makeText(this.getApplicationContext(),current_address, Toast.LENGTH_SHORT);
        myToast.show();     //현재 위치를 주소로 변환 앞에 '대한민국 '을 제거하고 사용해야함 뒤에 '동' 단위도 없애야 할듯 범위가 너무 작음

        String[] cut_address = current_address.split(" ");

        try {
            search(cut_address[1]);   //현재 위치 기반으로 '시' 단위 까지 지도에 표시하기 구는 너무 작고 시단위로 해도 주변에 충전소가 많지 않다
            //search(cut_address[1] +" " + cut_address[2]);         //현재 위치 기반으로 '구' 단위 까지 지도에 표시하기
        } catch (SAXException e) {                                  //'구' 단위여서 구의 경계에 있으면 옆동네의 충전소가 보이지 않는다
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
//        mapView.setCalloutBalloonAdapter(new CustomCalloutBalloonAdapter());    //커스텀 말풍선 세팅
        mapView.setPOIItemEventListener(this);  //마커 클릭했을 때 행동 가능하게 리스너 동록
        ViewGroup mapViewContainer = (ViewGroup) findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);
        mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(lati, longi), true);

        marker = new MapPOIItem[search_result.getStation_size()];
        for(int i = 0; i <search_result.getStation_size(); i++){
            marker[i] = new MapPOIItem();
            marker[i].setShowCalloutBalloonOnTouch(false);  //말풍선 안보이게 하기
            marker[i].setItemName(search_result.getStations()[i].getCsNm());    //충전소 명칭을 이름으로 표시
            marker[i].setTag(i);
            Log.d("station get", "" + search_result.getStations()[i].getLat());
            Log.d("station get", "" + search_result.getStations()[i].getLongi());
            marker[i].setMapPoint(MapPoint.mapPointWithGeoCoord(search_result.getStations()[i].getLat(), search_result.getStations()[i].getLongi()));
            marker[i].setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
            marker[i].setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
//            mapView.addPOIItem(marker[i]);
        }

        mapView.addPOIItems(marker);

        //여기까지 주석


//        setFilter(new String[]{"BC타입(5핀)"});
    }

    private void getHashKey(){
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

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면

            boolean check_result = true;


            // 모든 퍼미션을 허용했는지 체크합니다.

            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }


            if (check_result) {

                //위치 값을 가져올 수 있음
                ;
            } else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    Toast.makeText(MainActivity.this, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                    finish();


                } else {

                    Toast.makeText(MainActivity.this, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_LONG).show();

                }
            }

        }
    }

    void checkRunTimePermission() {

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)


            // 3.  위치 값을 가져올 수 있음


        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(MainActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);


            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }
        }
    }

    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
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

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
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

    @Override   //툴바에 메뉴xml 연결
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override   //툴바 클릭용 https://www.hanumoka.net/2017/10/28/android-20171028-android-toolbar/ 참고 공사중
    public boolean onOptionsItemSelected(MenuItem item) {
        //return super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.menu_search:
                // User chose the "Settings" item, show the app settings UI...
                Toast.makeText(getApplicationContext(), "환경설정 버튼 클릭됨", Toast.LENGTH_LONG).show();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                Toast.makeText(getApplicationContext(), "나머지 버튼 클릭됨", Toast.LENGTH_LONG).show();
                return super.onOptionsItemSelected(item);

        }
    }

    public void clickBtn(View view){    //긴급상황 액티비티로 전환

        setFilter(new String[]{"DC콤보"});
//        Log.d("zoomlevel = ", mapView.getZoomLevelFloat()+"");
        Intent intent = new Intent(this, Emergency.class);
        startActivity(intent);
    }

    /*
    * search를 하면 search_result에 값이 갱신됨 (추가 아님!!!)
    * 그것의 stations 배열의 값을 하나 고르고 거기서 get을 통해 필요한 정보를
    * 가져오면 됨 ex)  search_result.getStations()[3].getLongi()
    * 아니면
    * */
    void search(String input) throws SAXException, ParserConfigurationException, ParseException, IOException, ExecutionException, InterruptedException {
        SearchByAddress result = new SearchByAddress();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        Log.d("api_before", "this-"+input);

        SearchByAddress finalResult = result;
        Future<SearchByAddress> future = executor.submit(() -> {
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
            Toast.makeText(MainActivity.this, "API에 검색 결과가 없습니다!", Toast.LENGTH_LONG).show();
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

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }

        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        }

        Address address = addresses.get(0);
        return address.getAddressLine(0).toString()+"\n";

    }

    public double[] AddressTodouble( String s) {

        //지오코더... GPS를 주소로 변환
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
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return new double[]{0.0, 0.0};
        }

        Address address = addresses.get(0);
        double[] re = new double[2];
        re[0] = address.getLatitude();
        re[1] = address.getLongitude();
        return re;
    }

    public void setFilter(String[] selects){
        String[] type = {"B타입(5핀)","C타입(5핀)", "BC타입(5핀)","BC타입(7핀)", "DC차데모","AC3상", "DC콤보","DC차데모+DC콤보", "DC차데모+AC3상","DC차데모+DC콤보+AC3상"};
        int[] selects_int = new int[selects.length];
        for(int i = 0 ; i < selects_int.length;i++){    //타입을 정수로 바꿈
            for(int j = 0; j < 10; j++){
                if(selects[i].equals(type[j])){
                    selects_int[i] = j + 1;
                    break;
                }
            }
        }
        List<List<Integer>> station_n = new ArrayList<>();
        for(int i = 0 ; i < selects_int.length; i++){
            List<Integer> row = new ArrayList<>();
            for(int j=0; j < search_result.getStation_size(); j++){
                if(search_result.getStations()[j].chargeType(selects_int[i]) == true){
                    row.add(j);
                }
            }
            station_n.add(row);
        }
        List<Integer> clean_station_list = new ArrayList<>();   //중복 제거된 버전으로바꾸기
        for(int i = 0 ; i < station_n.size(); i++){
            for(int j=0; j < station_n.get(i).size(); j++){
                if(!clean_station_list.contains(station_n.get(i).get(j))){
                    clean_station_list.add(station_n.get(i).get(j));
                }
            }
        }

//        mapView.removeAllPOIItems();
        marker = new MapPOIItem[clean_station_list.size()];
        for(int i = 0; i <clean_station_list.size(); i++){
            int temp = clean_station_list.get(i);
            marker[i] = new MapPOIItem();
            marker[i].setItemName(search_result.getStations()[i].getCsNm());    //충전소 명칭을 이름으로 표시
            marker[i].setTag(i);
            marker[i].setShowCalloutBalloonOnTouch(true);
            marker[i].setMapPoint(MapPoint.mapPointWithGeoCoord(search_result.getStations()[temp].getLat(), search_result.getStations()[temp].getLongi()));
            marker[i].setMarkerType(MapPOIItem.MarkerType.YellowPin); // 기본으로 제공하는 BluePin 마커 모양.
            marker[i].setSelectedMarkerType(MapPOIItem.MarkerType.BluePin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
            mapView.addPOIItem(marker[i]);
        }

//        mapView.addPOIItems(marker);

    }

    @Override
    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) { //마커를 터치하면 충전기 등 정보가 나오게 하자
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
        String[] type = {"B타입(5핀)","C타입(5핀)", "BC타입(5핀)","BC타입(7핀)", "DC차데모","AC3상", "DC콤보","DC차데모+DC콤보", "DC차데모+AC3상","DC차데모+DC콤보+AC3상"};
        String line = "\n";

        for(int i = 0;i < now.getMachines_size(); i++){
            total[now.getMachines()[i].getCpTp()-1]++;
            if(now.getMachines()[i].getCpStat() == 1){
                able[now.getMachines()[i].getCpTp()-1]++;
            }
        }
        for(int i = 0;i < 10; i++){
            temp[i] = "type " + type[i] + " total "+total[i]+" able "+able[i]+"\n";
            if(able[i] != 0){
                line += temp[i];
            }
        }

        Intent it = new Intent(MainActivity.this, CustomNotiActivity.class);
        it.putExtra("station_name", now.getCsNm());
        it.putExtra("charger_info", line);
        it.putExtra("kakao", url);
        startActivity(it);


//        String temp = "there is ";
//        for(int i = 0;i < search_result.getStation_size(); i++){
//            if(mapPOIItem.getItemName().equals(search_result.getStations()[i].getCsNm())){
//                temp += search_result.getStations()[i].machines_size + " stations possible is ";
//                int able = 0;
//                for(int j = 0; j < search_result.getStations()[i].machines_size; j++){
//                    if(search_result.getStations()[i].machines[j].getCpStat() == 1){
//                        able++;
//                    }
//                }
//                temp += able + "";
//                break;
//            }
//        }
//
//        Toast myToast = Toast.makeText(getApplicationContext(), temp, Toast.LENGTH_SHORT);
//        myToast.show();
//        setFilter(new String[]{"DC콤보"});
    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {

    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {
//        View dialogView = getLayoutInflater().inflate(R.layout.custom_noti, null);
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setView(dialogView);
//        builder.setPositiveButton( "길찾기", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                Toast myToast = Toast.makeText(getApplicationContext(), "check", Toast.LENGTH_SHORT);
//                myToast.show();
//                String url = "kakaomap://route?sp="+ gpsTracker.getLatitude() +","+gpsTracker.getLongitude();
//                url += "&ep="+ mapPOIItem.getMapPoint().getMapPointGeoCoord().latitude+","+ mapPOIItem.getMapPoint().getMapPointGeoCoord().longitude+"&by=CAR";
//                Intent it = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//                startActivity(it);
//
//            }
//        });
//        builder.setNeutralButton( "주변 정보", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                Toast myToast = Toast.makeText(getApplicationContext(), "check", Toast.LENGTH_SHORT);
//                myToast.show();
//            }
//        });
//
//        AlertDialog alertDialog = builder.create();
//        alertDialog.show();

        setFilter(new String[]{"DC콤보"});


//        String url = "kakaomap://route?sp="+ gpsTracker.getLatitude() +","+gpsTracker.getLongitude();
//        url += "&ep="+ mapPOIItem.getMapPoint().getMapPointGeoCoord().latitude+","+ mapPOIItem.getMapPoint().getMapPointGeoCoord().longitude+"&by=CAR";
//        Intent it = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//        startActivity(it);
    }

    @Override
    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {

    }
}