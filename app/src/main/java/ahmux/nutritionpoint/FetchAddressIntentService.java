package ahmux.nutritionpoint;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/*
    @Author Dhanushka Silva
    On behalf of ICan Lanka Mobile team
    15-12-2022
 */
public class FetchAddressIntentService extends IntentService
{
    static final String TAG = "FetchAddress :-";
    protected ResultReceiver mReceiver;

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
    public FetchAddressIntentService()
    {
        super("FetchAddressThread");
    }

    protected void onHandleIntent(Intent intent)
    {
        String errorMessage = ""; //BuildConfig.FLAVOR;
        String callFrom = intent.getStringExtra(FROM_ADDRESS);
        Location location =  intent.getParcelableExtra(LOCATION_DATA_EXTRA);
        this.mReceiver =  intent.getParcelableExtra(RECEIVER);
        if (location == null)
        {
            errorMessage = "Address Not Available..!";
            deliverResultToReceiver(1, errorMessage,callFrom);
            return;
        }
        List<Address> addresses = null;
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

        } catch (IOException ioException) {
            errorMessage = "Not found";
        } catch (IllegalArgumentException illegalArgumentException) {
            errorMessage = "Not found";
        }

        if (addresses == null || addresses.size() == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = "Not found";
            }
            deliverResultToReceiver(1, errorMessage,callFrom);
            return;
        }
        Address address =  addresses.get(0);
        StringBuilder str = new StringBuilder();
        int maxsize = address.getMaxAddressLineIndex();
        if(maxsize > 0) {
            maxsize = maxsize - 1;
        }
        for (int i = 0; i <= maxsize; i++) {
            if(i == maxsize)
                str.append(address.getAddressLine(i) + " ");
            else
                str.append(address.getAddressLine(i) + ", ");
        }
        deliverResultToReceiver(SUCCESS_RESULT, str.toString(), callFrom);
    }

    private void deliverResultToReceiver(int resultCode, String message, String toCaller) {
        Bundle bundle = new Bundle();
        bundle.putString(RESULT_DATA_KEY, message);
        bundle.putString(CALLER_ADDRESS, toCaller);
        this.mReceiver.send(resultCode, bundle);
    }
}
