package ahmux.nutritionpoint.retrofit;


import androidx.annotation.Keep;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Query;

/**
 * Created by dhanushka.
 */
@Keep
public interface APIInterface {


    //    @Headers({
//            "Authorization: bearer 1HB3zN86dOQuUPQ6TtdQ3K5wRwFZWqO9SdUMNQb_Za9PKTIFAoPgkTitAe-hZhr3aXFmcYI3lfUzgjl1UCxU1XPa071q4OkAMVoz_iuNhEOaRpbuadXGxzgZtW3NmD629RoCMzcrDcsvTEFoMLYBKaIc4Qe9MsEZBHpbC9gZFvze_ZwNhGqoq-NGjOmBdryDwMonxh2ez-1jzwkH57pi0r10Nqi4kem0nzDMVlozlGYou99jj2c-TwF6omGzedWU0u6uDNAU92Ak2T8E5HDP_OTLMzumvn6VWdf9KRGjvB8",
//            "Content-Type: application/x-www-form-urlencoded"
//    })

    @GET("place/nearbysearch/json")
    Call<NearByPlacesBean> getNearByPlaces(@Query("location") String location, @Query("radius") int radius, @Query("type") String type, @Query("key") String key);

}
