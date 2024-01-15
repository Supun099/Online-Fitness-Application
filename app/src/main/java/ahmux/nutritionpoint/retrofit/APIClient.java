package ahmux.nutritionpoint.retrofit;


import android.app.Activity;
import android.content.Context;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by dhanushka.
 */

public class APIClient {

    private static Retrofit retrofit = null;
    static Context context;
    static Activity activity;


    public APIClient(Context ctx, Activity act) {
        context = ctx;
        activity = act;
        // private constructor to prevent access
        // only way to access: Retrofit client = RetrofitClient.getInstance();
    }

    public static Retrofit getClient() {

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(3, TimeUnit.MINUTES)
                .readTimeout(3, TimeUnit.MINUTES)
                .writeTimeout(3, TimeUnit.MINUTES)
                .build();


        retrofit = new Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/maps/api/")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();


        return retrofit;
    }


}
