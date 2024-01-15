package ahmux.nutritionpoint;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import ahmux.nutritionpoint.utills.CreditCardFormatTextWatcher;

public class PaymentsActivity extends AppCompatActivity {
    public EditText etCardNumber, etExpireMonth, etExpireYear, etCVV, etCardHolderName;
    public String cardNumber = "", holderName = "", eMonth = "", eYear = "", eCvv = "", clientMobile = "", cardRegisterURL = "", cardTypeInt = "";
    public int expireMonth = 0, expireYear = 0, ecvv = 0;
    public SharedPreferences clientSession;
    public WebView wvCardRespond;
    public LinearLayout loBodyView, loWebView;
    public MenuItem menuItemDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_credicard);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.green));
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));

        final Drawable upArrow = getResources().getDrawable(R.mipmap.ic_close_activity_3);
        upArrow.setColorFilter(getResources().getColor(R.color.budget_color_main), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        initialize();
        eventHandler();

        EditText etCreditCard = (EditText) findViewById(R.id.et_cardNumber);

        // the EditText here is used to compute the padding (1 EM) which depends
        // on the actual size of the Text rendered on screen taking into account:
        // the font, the text size, the user scaling on text
        CreditCardFormatTextWatcher tv = new CreditCardFormatTextWatcher(etCreditCard);
        etCreditCard.addTextChangedListener(tv);

    }


    public void initialize() {
        etCardNumber = (EditText) findViewById(R.id.et_cardNumber);
        etExpireMonth = (EditText) findViewById(R.id.et_expireMonth);
        etExpireYear = (EditText) findViewById(R.id.et_expireYear);
        etCVV = (EditText) findViewById(R.id.et_cvv);
        etCardHolderName = (EditText) findViewById(R.id.et_cardHolderName);

        loBodyView = (LinearLayout) findViewById(R.id.lo_bodyView);

    }


    public void eventHandler() {
        setTitle("Add New Card");

        etCardNumber.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
                if (etCardNumber.getText().toString().length() == 16)     //size as per your requirement
                {
                    etExpireMonth.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }
        });

        etExpireMonth.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
                if (etExpireMonth.getText().toString().length() == 2)     //size as per your requirement
                {
                    etExpireYear.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }
        });


        etExpireYear.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
                if (etExpireYear.getText().toString().length() == 2)     //size as per your requirement
                {
                    etCVV.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub

            }
        });

        etCVV.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
                if (etCVV.getText().toString().length() == 3)     //size as per your requirement
                {
                    etCardHolderName.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub

            }
        });


        etCardHolderName.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;

                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(etCardHolderName.getWindowToken(), 0);

                    doneClickEvent();

                    handled = true;
                }

                return handled;
            }
        });
    }

    public enum CardType {

        UNKNOWN,
        VISA("^4[0-9]{6,}([0-9]{3})?$"),
        MASTERCARD("^(5[1-5][0-9]{4}|677189)[0-9]{5,}$"),
        AMERICAN_EXPRESS("^3[47][0-9]{5,}$"),
        DINERS_CLUB("^3(?:0[0-5]\\d|095|6\\d{0,2}|[89]\\d{2})\\d{12,15}$"),
        DISCOVER("^6(?:011|[45][0-9]{2})[0-9]{12}$"),
        JCB("^(?:2131|1800|35\\d{3})\\d{11}$"),
        CHINA_UNION_PAY("^62[0-9]{14,17}$");

        private Pattern pattern;

        CardType() {
            this.pattern = null;
        }

        CardType(String pattern) {
            this.pattern = Pattern.compile(pattern);
        }

        public static CardType detect(String cardNumber) {

            for (CardType cardType : CardType.values()) {
                if (null == cardType.pattern)
                {
                    continue;
                }
                if (cardType.pattern.matcher(cardNumber).matches())
                {

                    //System.out.println(StaticVariables.TAG + "xxx CardType " + cardType);
                    return cardType;

                }
            }
            return UNKNOWN;
        }

    }

    public void doneClickEvent() {
        boolean status1 = false, status2 = false, status3 = false, status4 = false, status5 = false;

        if (etCardNumber.getText() != null) {
            cardNumber = etCardNumber.getText().toString().trim();
        } else {
            cardNumber = "";
        }


        if (etCardHolderName.getText() != null) {
            holderName = etCardHolderName.getText().toString().trim();
        } else {
            holderName = "";
        }


        if (etExpireMonth.getText() != null) {
            eMonth = etExpireMonth.getText().toString().trim();
        } else {
            eMonth = "";
        }


        if (etExpireYear.getText() != null) {
            eYear = etExpireYear.getText().toString().trim();
        } else {
            eYear = "";
        }

        if (etCVV.getText() != null) {
            eCvv = etCVV.getText().toString().trim();
        } else {
            eCvv = "";
        }


        if (cardNumber.equals("") || cardNumber.length() < 1) {
            etCardNumber.setError("Card no cannot be empty");
            status1 = false;
        } else {

            if(String.valueOf(CardType.detect(cardNumber)).equals("VISA")){
                cardTypeInt = "001";
            }else if(String.valueOf(CardType.detect(cardNumber)).equals("MASTERCARD")){
                cardTypeInt = "002";
            }else if(String.valueOf(CardType.detect(cardNumber)).equals("AMERICAN_EXPRESS")){
                cardTypeInt = "003";
            }else if(String.valueOf(CardType.detect(cardNumber)).equals("DISCOVER")){
                cardTypeInt = "004";
            }else if(String.valueOf(CardType.detect(cardNumber)).equals("DINERS_CLUB")){
                cardTypeInt = "005";
            }

            //System.out.println(StaticVariables.TAG + "xxx cardTypeInt " + cardTypeInt+ " cardNumber "+cardNumber);

            etCardNumber.setError(null);
            status1 = true;
        }

        if (holderName.equals("") || holderName.length() < 1) {
            etCardHolderName.setError("Name cannot be empty");
            status2 = false;
        } else {
            etCardHolderName.setError(null);
            status2 = true;
        }


        if (eMonth.equals("") || eMonth.length() < 2) {
            status3 = false;

            if (eMonth.equals("")) {
                etExpireMonth.setError("Cannot be empty");
            } else {
                etExpireMonth.setError("Need two digits");
            }
        } else {
            expireMonth = Integer.parseInt(eMonth);

            if (expireMonth >= 1 && expireMonth <= 12) {
                etExpireMonth.setError(null);
                status3 = true;
            } else {
                etExpireMonth.setError("Invalid month");
                status3 = false;
            }
        }


        if (eYear.equals("") || eYear.length() < 2) {
            status4 = false;

            if (eYear.equals("")) {
                etExpireYear.setError("Cannot be empty");
            } else {
                etExpireYear.setError("Need two digits");
            }
        } else {
            expireYear = Integer.parseInt(eYear);

            if (expireYear >= 19) {
                etExpireYear.setError(null);
                status4 = true;
            } else {
                etExpireYear.setError("Invalid year");
                status4 = false;
            }
        }

        if (eCvv.equals("") || eCvv.length() < 3) {
            status5= false;

            if (eCvv.equals("")) {
                etCVV.setError("Cannot be empty");
            } else {
                etCVV.setError("Need three digits");
            }
        } else {
            ecvv = Integer.parseInt(eCvv);
            status5= true;

        }


    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ride_schedule_menu, menu);

        menuItemDone = menu.findItem(R.id.action_done);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == R.id.action_done) {
            doneClickEvent();
        }

        return super.onOptionsItemSelected(item);
    }
}
