package ahmux.nutritionpoint;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import ahmux.nutritionpoint.adapter.PoiInfoAdapter;
import ahmux.nutritionpoint.retrofit.APIClient;
import ahmux.nutritionpoint.retrofit.APIInterface;
import ahmux.nutritionpoint.retrofit.NearByPlacesBean;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;

public class MapActivity extends AppCompatActivity implements LocationListener{

    private String mCurrentCity = "";
    public GoogleMap gMap;
    public LocationManager locationManager;
    public Location location = null, curentLocation = null, selectedLocation = null;
    public GoogleApiClient mGoogleApiClient = null;
    public Handler addressHandler;
    public String selectedAddress = "";
    public String selectedAddressTitle = "";
    public AddressResultReceiver mResultReceiver;
    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private LocationRequest mLocationRequest;
    private double fusedLatitude = 0.0;
    private double fusedLongitude = 0.0;
    private double sendLatitude = 0.0;
    private double sendLongitude = 0.0;
    private PoiInfoAdapter mAdapter;
    private static final int PERMISSION_REQUEST_CODE = 200;

    public static final String RECEIVER = "com.google.android.gms.location.sample.locationaddress.RECEIVER";
    public static final String RESULT_DATA_KEY = "com.google.android.gms.location.sample.locationaddress.RESULT_DATA_KEY";
    public static final int SUCCESS_RESULT = 0;
    public static final String LOCATION_DATA_EXTRA = "com.google.android.gms.location.sample.locationaddress.LOCATION_DATA_EXTRA";

    public static final double DEFAULT_CLIENT_LOC_LAT = 6.9345468;
    public static final double DEFAULT_CLIENT_LOC_LON = 79.8442926;
    public static int MAPZOOMLEVEL = 18;
    public static int PIN_LOCATION_REQUEST = 50;
    public static final String FROM_ADDRESS = "FROMADDRESS";
    public static final String CALLER_ADDRESS = "CALLERADDRESS";

    PlacesClient placesClient;
    private String userCode;
    private String groupId;
    private String circleUserId;
    private boolean isJustSelect;
    private boolean isFirstLoad;
    private AutocompleteSupportFragment autocompleteFragment;

    RelativeLayout ivBack, rlMap;
    TextView tvContent, tvRight, etSearch;
    ListView lvLocation, listView;
    LinearLayout loSearch, layoutLocation, loSuggestList, loAddressFragment;
    MapView mapView;
    ImageView ivPin;

    static APIInterface apiInterface;
    String Google_geo_key ="AIzaSyAMIpojAcaBMWHgtV-2tEf5wlySn1Uui7s";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        init(savedInstanceState);
    }
    public void init(Bundle savedInstanceState) {
        ivBack = findViewById(R.id.iv_back);
        rlMap = findViewById(R.id.rl_map);
        etSearch = findViewById(R.id.et_search);
        tvRight = findViewById(R.id.tv_right);
        tvContent = findViewById(R.id.tv_content);
        lvLocation = findViewById(R.id.lv_location);
        listView = findViewById(R.id.list_view);
        loSearch = findViewById(R.id.lo_search);
        layoutLocation = findViewById(R.id.layout_location);
        loSuggestList = findViewById(R.id.lo_suggest_list);
        loAddressFragment = findViewById(R.id.lo_address_fragment);
        mapView = (MapView) findViewById(R.id.map_view);
        ivPin = findViewById(R.id.iv_pin);

        new APIClient(MapActivity.this, MapActivity.this);
        //apiInterface = RetrofitClient.getInstance().create(APIInterface.class);
        apiInterface = APIClient.getClient().create(APIInterface.class);

        eventHandler(savedInstanceState);
    }

    public void eventHandler(Bundle savedInstanceState){
        tvContent.setText("Location");
        ivBack.setOnClickListener(v -> {
            //SoftInputManager.hideSoftKeyboard(etSearch);
            finish();
        });

        isFirstLoad = true;
        if (checkPlayServices()) {
            startFusedLocation();
            registerRequestUpdate(this);
        }
        autocompleteFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        Places.initialize(this, Google_geo_key);
        placesClient = Places.createClient(this);
        tvRight.setVisibility(View.VISIBLE);
        tvRight.setText("Navigate");
        tvRight.setTextSize(17);
        tvRight.setOnClickListener(v -> {
           //share location;
            jumpToGoogleMap();
        });
        initGoogleMap(savedInstanceState);
        this.addressHandler = new Handler();
        this.mResultReceiver = new AddressResultReceiver(this.addressHandler);
        loSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loSearch.setVisibility(View.GONE);
                loAddressFragment.setVisibility(View.VISIBLE);
                loSuggestList.setVisibility(View.GONE);
                rlMap.setVisibility(View.GONE);
                setPlaceBox();
            }
        });
        setPlaceBox();
    }

    private void initGoogleMap(Bundle savedInstanceState) {
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(new OnMapReadyCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onMapReady(GoogleMap mMap) {
                gMap = mMap;

                gMap.setMyLocationEnabled(true);
                gMap.getUiSettings().setMyLocationButtonEnabled(false);
                gMap.getUiSettings().setRotateGesturesEnabled(false);

                moveCameraToMyLocation(null);

                gMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                    @Override
                    public void onCameraIdle() {
                        com.google.android.gms.maps.model.LatLng centerLoc = gMap.getCameraPosition().target;
                        curentLocation = new Location("Center");
                        curentLocation.setLatitude(centerLoc.latitude);
                        curentLocation.setLongitude(centerLoc.longitude);
                        sendLatitude = curentLocation.getLatitude();
                        sendLongitude = curentLocation.getLongitude();
                        if (curentLocation != null && mGoogleApiClient.isConnected()) {
                            startIntentService();
                        }
                    }
                });

                gMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
                    @Override
                    public void onCameraMoveStarted(int i) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                etSearch.setText("fetching...");
                                selectedAddress = "";
                            }
                        });
                    }
                });

                gMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(com.google.android.gms.maps.model.LatLng point) {
                        MarkerOptions marker = new MarkerOptions().position(new LatLng(point.latitude, point.longitude)).title("Location Selected");
                        gMap.clear();
                        selectedLocation = new Location("Center");
                        selectedLocation.setLatitude(point.latitude);
                        selectedLocation.setLongitude(point.longitude);
                        moveCameraToSelectedLocation(selectedLocation);
                        getNearByPlaces(sendLatitude, sendLongitude);
                    }
                });
            }
        });
    }

    private void setPlaceBox() {
        if (!Places.isInitialized()) {
            Places.initialize(this, Google_geo_key);
        }
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.ADDRESS));
        //autocompleteFragment.setCountry("LK");
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                loSearch.setVisibility(View.VISIBLE);
                loAddressFragment.setVisibility(View.GONE);
                loSuggestList.setVisibility(View.VISIBLE);
                rlMap.setVisibility(View.VISIBLE);
                curentLocation = new Location("Center");
                curentLocation.setLatitude(place.getLatLng().latitude);
                curentLocation.setLongitude(place.getLatLng().longitude);
                selectedAddressTitle = place.getAddress();
                if (curentLocation != null && mGoogleApiClient.isConnected()) {
                    startIntentService();
                }
                gMap.clear();
                moveCameraToSelectedLocation(curentLocation);
                getNearByPlaces(place.getLatLng().latitude, place.getLatLng().longitude);
            }

            @Override
            public void onError(Status status) {
                Log.e("TAG", status.getStatusMessage());
            }
        });
    }

    public void getNearByPlaces(double lat, double lon) {
        Call<NearByPlacesBean> call = apiInterface.getNearByPlaces((lat + "," + lon), 1500, "restaurant", Google_geo_key);
        call.enqueue(new Callback<NearByPlacesBean>() {
            @Override
            public void onResponse(Call<NearByPlacesBean> call, retrofit2.Response<NearByPlacesBean> response) {

                if (response.isSuccessful()) {

                    mAdapter = new PoiInfoAdapter(MapActivity.this, Arrays.asList(response.body() != null ? response.body().getResults() : new NearByPlacesBean.Result[0]));
                    mAdapter.setOnChoiceListener((position, item) -> {
                        mAdapter.setSelectUid(item.getPlaceId());
                        Location location = new Location("loc");
                        location.setLatitude(item.getGeometry().getLocation().getLat());
                        location.setLongitude(item.getGeometry().getLocation().getLng());
                        selectedAddressTitle = item.getName();
                        gMap.clear();
                        moveCameraToSelectedLocation(location);
                    });
                    listView.setAdapter(mAdapter);
                    
                }
               

            }

            @Override
            public void onFailure(Call<NearByPlacesBean> call, Throwable t) {

                call.cancel();
            }
        });
    }


    public void moveCameraToSelectedLocation(Location location) {
        if (gMap != null && location != null) {
            sendLatitude = location.getLatitude();
            sendLongitude = location.getLongitude();
            gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), MAPZOOMLEVEL));
        }
    }

    protected void startIntentService() {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(RECEIVER, mResultReceiver);
        intent.putExtra(FROM_ADDRESS, "PICKUP");
        intent.putExtra(LOCATION_DATA_EXTRA, curentLocation);
        startService(intent);
    }

    public void moveCameraToMyLocation(View view) {
        getMyLocation();
        if ((gMap != null) && (location != null)) {
            if (isFirstLoad) {
                getNearByPlaces(location.getLatitude(), location.getLongitude());
                isFirstLoad = false;
            }
            if (view == null) {
                gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), MAPZOOMLEVEL));
            } else {
                gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), MAPZOOMLEVEL));
            }
        } else {
            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(DEFAULT_CLIENT_LOC_LAT, DEFAULT_CLIENT_LOC_LON), MAPZOOMLEVEL));
        }
    }

    public void getMyLocation() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (fusedLatitude != 0.0) {
            location = new Location("loc");
            location.setLatitude(fusedLatitude);
            location.setLongitude(fusedLongitude);
        }
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                Toast.makeText(getApplicationContext(),
                                "This device is supported. Please download google play services", Toast.LENGTH_LONG)
                        .show();
            } else {
                Toast.makeText(getApplicationContext(),
                                "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }

    public void startFusedLocation() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnectionSuspended(int cause) {
                        }

                        @Override
                        public void onConnected(Bundle connectionHint) {

                        }
                    }).addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {

                        @Override
                        public void onConnectionFailed(ConnectionResult result) {

                        }
                    }).build();
            mGoogleApiClient.connect();
        } else {
            mGoogleApiClient.connect();
        }
    }

    public void stopFusedLocation() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    public void registerRequestUpdate(final LocationListener listener) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000); // every second
        // mLocationRequest.setNumUpdates(50);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, listener);
                } catch (SecurityException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                    if (!isGoogleApiClientConnected()) {
                        mGoogleApiClient.connect();
                    }
                    registerRequestUpdate(listener);
                }
            }
        }, 200);
    }

    public boolean isGoogleApiClientConnected() {
        return mGoogleApiClient != null && mGoogleApiClient.isConnected();
    }

    public void setFusedLatitude(double lat) {
        fusedLatitude = lat;
    }

    public void setFusedLongitude(double lon) {
        fusedLongitude = lon;
    }

    public double getFusedLatitude() {
        return fusedLatitude;
    }

    public double getFusedLongitude() {
        return fusedLongitude;
    }



    private TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (!isEmpty(s)) {
                layoutLocation.setVisibility(View.GONE);
                lvLocation.setVisibility(View.VISIBLE);
            } else {
                layoutLocation.setVisibility(View.VISIBLE);
                lvLocation.setVisibility(View.GONE);
            }

        }
    };

    public static boolean isEmpty(Object obj) {
        if (null == obj) {
            return true;
        }
        if (obj instanceof String) {
            if ("NULL".equals(obj.toString().trim().toUpperCase())
                    || "".equals(obj.toString().trim())) {
                return true;
            } else {
                return false;
            }
        } else if (obj instanceof Collection) {
            return ((Collection) obj).size() == 0;
        } else if (obj instanceof Map) {
            return ((Map) obj).size() == 0;
        } else if (obj.getClass().isArray()) {
            return Array.getLength(obj) == 0;
        } else {
            try {
                return isEmpty(obj.toString());
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    @Override
    public void onStop() {
        stopFusedLocation();
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopFusedLocation();
        try {
            if (mapView != null) {
                mapView.onDestroy();
            }
        } catch (Exception e) {
            //System.out.println(Config.TAG + "SelectOnMapActivity onDestroy mapView error: " + e.getMessage());
        }
    }

    public void displayAddressOutput(final String address) {
        runOnUiThread(new Runnable() {
            public void run() {
                //tvAddress.setText(address);
            }
        });
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public void onLocationChanged(Location loc) {
        setFusedLatitude(loc.getLatitude());
        setFusedLongitude(loc.getLongitude());
        if (location == null) {
            moveCameraToMyLocation(null);
        }
    }

    @SuppressLint("ParcelCreator")
    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            if (resultCode == SUCCESS_RESULT) {
                String address = resultData.getString(RESULT_DATA_KEY);
                String callee = resultData.getString(CALLER_ADDRESS);

                try {
                    if (callee.equals("PICKUP")) {
                        displayAddressOutput(address);
                        selectedAddress = address;
                        etSearch.setText(selectedAddress);
                    } else {
                        //displayDropAddressOutput(address);
                    }
                } catch (Exception e) {
                    //System.out.println(Config.TAG + "SelectOnMapActivity  AddressResultReceiver error: " + e.getMessage());
                }
            } else {
                // show error
            }
        }
    }


    private void jumpToGoogleMap() {
        if (isAppInstalled(this, "com.google.android.apps.maps")) {
            Toast.makeText(this, "Please install google maps", Toast.LENGTH_SHORT).show();

            return;
        }
        try {
            String strUri = "http://maps.google.com/maps?q=loc:" + sendLatitude + "," + sendLongitude + "," + selectedAddress;
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(strUri));
            intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "Please install google maps", Toast.LENGTH_SHORT).show();

            return;
        }
    }

    public static boolean isAppInstalled(Context context, String packgname) {
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(packgname, 0);
        } catch (PackageManager.NameNotFoundException e) {
            packageInfo = null;
            e.printStackTrace();
        }
        if (packageInfo == null) {
            return false;
        } else {
            return true;
        }
    }
}
