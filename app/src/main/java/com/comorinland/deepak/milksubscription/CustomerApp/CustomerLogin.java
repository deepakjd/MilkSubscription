package com.comorinland.deepak.milksubscription.CustomerApp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.comorinland.deepak.milksubscription.Common.Constant;
import com.comorinland.deepak.milksubscription.Common.DownloadFromAmazonDBTask;
import com.comorinland.deepak.milksubscription.Common.DownloadFromS3Task;
import com.comorinland.deepak.milksubscription.Common.InternalFileStorageUtils;
import com.comorinland.deepak.milksubscription.Common.ResponseHandler;
import com.comorinland.deepak.milksubscription.R;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class CustomerLogin extends AppCompatActivity implements ResponseHandler
{
    private UserLoginTask mAuthTask = null;
    private ProgressDialog mProgressDialog;

    // UI references.
    private EditText mEditCustomerID;
    private EditText mEditCustomerPassword;

    // Profile references
    String mStrCustomerID;
    String mStrCustomerPassword;
    String mStrVendorID;
    String mStrPostalAddress;
    String mStrMilkDistributor;

    HashMap<String,String> mMapImageName = new HashMap<>();

    /* Pre-Signin URL String */
    String mStrSignInURL="";
    String mStrStorageKey ="";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarCustomerLogin);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Customer Login");

        toolbar.setNavigationOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                finish();
            }
        });

        // Set up the login form.
        mEditCustomerID = (EditText) findViewById(R.id.edit_customer_login_id);
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String strCustomerID = sharedPref.getString(getString(R.string.customer_id), null);
        mEditCustomerID.setText(strCustomerID);

        mEditCustomerPassword = (EditText) findViewById(R.id.edit_customer_password);

        mEditCustomerPassword.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL)
                {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button btnCustomerSignIn = (Button) findViewById(R.id.customer_sign_in_button);

        btnCustomerSignIn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                /* This will have to be deleted */
                /* Intent intentMilkman = new Intent(CustomerLogin.this, MainActivity.class);
                    startActivity(intentMilkman);
                */

                if (IsConfigurationInfoPresent() == false)
                {

                    /* The shared preferences do not have some preference information. We need to
                        get the information from the preferences for the login to be authenticated.
                    */

                    // Create thread to get data from the server.
                    mProgressDialog = new ProgressDialog(CustomerLogin.this);
                    mProgressDialog.setIndeterminate(false);
                    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

                    new DownloadFromAmazonDBTask(CustomerLogin.this,"GetCustomerInfo.php", mProgressDialog).execute(jsonBuildInfo());

                }
                else
                {
                    attemptLogin();
                }
            }
        });

        SpannableString ss = new SpannableString("Forgot Password ? Click here to get your password");

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                startActivity(new Intent(CustomerLogin.this, PasswordLost.class));
            }
        };

        ss.setSpan(clickableSpan, 40, 44, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        TextView txtVendorRegister = (TextView) findViewById(R.id.customer_link_signup);
        txtVendorRegister.setText(ss);
        txtVendorRegister.setMovementMethod(LinkMovementMethod.getInstance());

    }

    private JsonObject jsonBuildInfo()
    {
        JsonObject jsonCustomerObject = new JsonObject();

        mEditCustomerID = (EditText) findViewById(R.id.edit_customer_login_id);
        String strCustomerID = mEditCustomerID.getText().toString();

        jsonCustomerObject.addProperty("CustomerID", strCustomerID);

        return jsonCustomerObject;
    }

    /* This function is going to get the string response and store it in a string array */
    @Override
    public String HandleJsonResponse(String strResponse)
    {

        if (strResponse == null)
        {
            return Constant.RESPONSE_UNAVAILABLE;
        }

        if (strResponse.equals("QUERY_EMPTY"))
        {
            return Constant.INFO_NOT_FOUND;
        }

        if (strResponse.equals("DB_EXCEPTION"))
        {
            return Constant.DB_ERROR;
        }

        try
        {

            JSONObject dataObject = new JSONObject(strResponse);

            mStrCustomerID = dataObject.getString("CustomerID");
            mStrVendorID = dataObject.getString("VendorID");
            mStrCustomerPassword = dataObject.getString("CustomerPassword");
            mStrPostalAddress = dataObject.getString("PostalAddress");
            mStrMilkDistributor = dataObject.getString("MilkCompanyID");

            if (dataObject.has("SignInURL"))
            {
                /* The SignIn URL which has been created */
                mStrSignInURL = dataObject.getString("SignInURL");
            }

            if (dataObject.has("S3KeyZipName"))
            {
                mStrStorageKey = dataObject.getString("S3KeyZipName");
            }

            if (dataObject.has("ImageNames"))
            {
                /* Get the name of the .png image files of the milk cover */
                JSONObject jsonImageNamesObject = dataObject.getJSONObject("ImageNames");

                Iterator<String> iter = jsonImageNamesObject.keys();

                while (iter.hasNext())
                {
                    String strMilkVarietyName = iter.next();
                    String strImageName = (String) jsonImageNamesObject.get(strMilkVarietyName);
                    mMapImageName.put(strMilkVarietyName, strImageName);
                }
            }
        }
        catch (JSONException e)
        {
            return Constant.JSON_EXCEPTION;
        }

        return Constant.JSON_SUCCESS;

    }

    public void UpdateMilkInfoDisplay(String strReturnCode)
    {

        if (strReturnCode.equals(Constant.JSON_SUCCESS))
        {

            /* The registration process has been a success. We will store important customer
              related information that is going to be used throughout the app in the profile
              database.
            */

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.vendor_id), mStrVendorID);
            editor.commit();
            editor.putString(getString(R.string.customer_id), mStrCustomerID);
            editor.commit();
            editor.putString(getString(R.string.customer_password), mStrCustomerPassword);
            editor.commit();
            editor.putString(getString(R.string.city_name), mStrPostalAddress);
            editor.commit();
            editor.putString(getString(R.string.distribution_company),mStrMilkDistributor);
            editor.commit();

            if (mMapImageName.isEmpty() == Boolean.FALSE)
            {
                Set<String> setKeyNames = mMapImageName.keySet();
                Iterator<String> iter = setKeyNames.iterator();
                while (iter.hasNext()) {
                    String strMilkVarietyName = iter.next();
                    editor.putString(strMilkVarietyName, mMapImageName.get(strMilkVarietyName));
                    editor.commit();
                }
            }

        }

        /* We will try to login inspite of the status of server data transaction */
        attemptLogin();
    }


    /*
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (missing fields, short passwords), the
     * errors are presented and no actual login attempt is made.
     */

    private void attemptLogin()
    {
        if (mAuthTask != null)
        {
            return;
        }

        // Reset errors.
        mEditCustomerID.setError(null);
        mEditCustomerPassword.setError(null);

        // Store values at the time of the login attempt.
        String strCustomerID = mEditCustomerID.getText().toString();
        String strCustomerPassword = mEditCustomerPassword.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(strCustomerPassword) && !isPasswordValid(strCustomerPassword))
        {
            mEditCustomerPassword.setError(getString(R.string.error_invalid_password));
            focusView = mEditCustomerPassword;
            cancel = true;
        }

        // Check for a valid mobile number.
        if (TextUtils.isEmpty(strCustomerID))
        {
            mEditCustomerID.setError(getString(R.string.error_field_required));
            focusView = mEditCustomerPassword;
            cancel = true;
        }

        if (!isMobileIDValid(strCustomerID))
        {
            mEditCustomerID.setError(getString(R.string.error_invalid_mobile));
            focusView = mEditCustomerPassword;
            cancel = true;
        }

        if (cancel)
        {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        }
        else
        {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mAuthTask = new UserLoginTask(this, strCustomerID, strCustomerPassword);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean IsConfigurationInfoPresent()
    {
        boolean bConfigurationInfo = true;

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        bConfigurationInfo = sharedPref.contains(getString(R.string.vendor_id));

        if (bConfigurationInfo == false)
        {
            return bConfigurationInfo;
        }
        bConfigurationInfo = sharedPref.contains(getString(R.string.customer_password));

        if (bConfigurationInfo == false)
        {
            return bConfigurationInfo;
        }
        bConfigurationInfo = sharedPref.contains(getString(R.string.city_name));

        if (bConfigurationInfo == false)
        {
            return bConfigurationInfo;
        }

        bConfigurationInfo = sharedPref.contains(getString(R.string.distribution_company));

        if (bConfigurationInfo == false)
        {
            return bConfigurationInfo;
        }

        return bConfigurationInfo;
    }

    private boolean isPasswordValid(String password)
    {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    private boolean isMobileIDValid(String mobileNo)
    {
        //TODO: Replace this with your own logic
        return mobileNo.length() == 10;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean>
    {

        private final String mStrCustomerID;

        private final String mStrCustomerPassword;

        private Context mContext;

        private InternalFileStorageUtils mInternalFileStorageUtils;

        private String mStrZipFileName;

        UserLoginTask(Context context, String strCustomerID, String strPassword)
        {
            mStrCustomerID = strCustomerID;
            mStrCustomerPassword = strPassword;
            mContext = context;
        }

        @Override
        protected Boolean doInBackground(Void... params)
        {
            // TODO: attempt authentication against a network service.
            try
            {
                // Simulate network access.
                Thread.sleep(1000);
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
                String strCustomerID = sharedPref.getString(getString(R.string.customer_id), null);
                String strCustomerPassword = sharedPref.getString(getString(R.string.customer_password), null);

                if (mStrCustomerID.equals(strCustomerID) == false)
                {
                    return false;
                }

                if (mStrCustomerPassword.equals(strCustomerPassword) == false)
                {
                    return false;
                }
            }
            catch (InterruptedException e)
            {
                return false;
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success)
        {
            mAuthTask = null;

            if (!success)
            {
                mEditCustomerPassword.setError(getString(R.string.error_incorrect_password));
                mEditCustomerPassword.requestFocus();
            }
            else
            {
                if ((mStrSignInURL.isEmpty() == Boolean.FALSE) && mInternalFileStorageUtils.AreMilkVarietyPhotosAvailable(mStrZipFileName) == Boolean.FALSE)
                {
                    // If the Sign-in URL is present and the directory were the photos are stored is not present.
                    // We will then download the zip file.
                    new DownloadFromS3Task(CustomerLogin.this).execute(mStrSignInURL, mStrZipFileName);
                }

                Intent intentMilkman = new Intent(mContext, MainActivity.class);
                startActivity(intentMilkman);
            }
        }

        @Override
        protected void onCancelled()
        {
            mAuthTask = null;
        }
    }
}
