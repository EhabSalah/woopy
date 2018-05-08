package woopy.domain.com.woopy;

/*-------------------------------

    - woopy -

    Created by cubycode @2017
    All Rights reserved

-------------------------------*/

import android.*;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.facebook.internal.CallbackManagerImpl.RequestCodeOffset.Login;


public class Wizard extends AppCompatActivity {

    /* Views */
    public static ImageView dotsImg;
    ViewPager viewPager;


    /* Variables */
    Timer timer;
    int page = 0;
    MarshMallowPermission mmp = new MarshMallowPermission(this);



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wizard);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Hide ActionBar
        getSupportActionBar().hide();



        // ViewPager for the wizard
        dotsImg = findViewById(R.id.dotsImg);
        viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(new CustomPagerAdapter(this));
        PageListener pageListener = new PageListener();
        viewPager.setOnPageChangeListener(pageListener);


        // TIMER TO ANIMATE THE WIZARD
        timer = new Timer(); // At this line a new Thread will be created
        timer.scheduleAtFixedRate(new ChangeWizardScreen(), 0,  3000);




        //Check if Location service is permitted
        if (!mmp.checkPermissionForLocation()) {
            mmp.requestPermissionForLocation();
        } else {
            Log.i("log-", "LOCATION IS PERMITTED!");
        }






        // MARK: - FACEBOOK LOGIN BUTTON ------------------------------------------------------------------
        Button fbButt = findViewById(R.id.facebookButt);
        fbButt.setTypeface(Configs.titSemibold);
        fbButt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder alert  = new AlertDialog.Builder(Wizard.this);
                alert.setTitle("Do you accept our Terms of Service?")
                        .setIcon(R.drawable.logo)
                        .setItems(new CharSequence[] {
                                        "Yes",
                                        "Read Terms of Service"
                        }, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {


                                    // SIGN IN WITH FACEBOOK
                                    case 0:
                                        List<String> permissions = Arrays.asList("public_profile", "email");
                                        Configs.showPD("Please wait...", Wizard.this);

                                        ParseFacebookUtils.logInWithReadPermissionsInBackground(Wizard.this, permissions, new LogInCallback() {
                                            @Override
                                            public void done(ParseUser user, ParseException e) {
                                                if (user == null) {
                                                    Log.i("log-", "Uh oh. The user cancelled the Facebook login.");
                                                    Configs.hidePD();

                                                } else if (user.isNew()) {
                                                    getUserDetailsFromFB();

                                                } else {
                                                    Log.i("log-", "RETURNING User logged in through Facebook!");
                                                    Configs.hidePD();
                                                    startActivity(new Intent(Wizard.this, Home.class));
                                        }}});
                                        break;



                                    // OPEN TERMS OF SERVICE
                                    case 1:
                                        startActivity(new Intent(Wizard.this, TermsOfUse.class));
                                        break;

                        }}})
                        .setNegativeButton("Cancel", null);
                alert.create().show();


        }});




        // This code generates a KeyHash that you'll have to copy from your Logcat console and paste it into Key Hashes field in the 'Settings' section of your Facebook Android App
        try {
            @SuppressLint("PackageManagerGetSignatures") PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.i("log-", "keyhash: " + Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
            } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException ignored) {}




        // MARK: - SIGN UP BUTTON ------------------------------------
        Button signupButt = findViewById(R.id.signupButt);
        signupButt.setTypeface(Configs.titRegular);
        signupButt.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View view) {
              startActivity(new Intent(Wizard.this, Login.class));
         }});




        // MARK: - TERMS OF SERVICE BUTTON ------------------------------------
        Button tosButt = findViewById(R.id.tosButt);
        tosButt.setTypeface(Configs.titRegular);
        tosButt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Wizard.this, TermsOfUse.class));
        }});




        // MARK: - DISMISS BUTTON ------------------------------------
        Button dismissButt = findViewById(R.id.wDismissButt);
        dismissButt.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View view) {
                finish();
          }});


    } // end onCreate()










    // MARK: - FACEBOOK GRAPH REQUEST --------------------------------------------------------
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }


    void getUserDetailsFromFB() {
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(),new GraphRequest.GraphJSONObjectCallback(){
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                String facebookID = "";
                String name = "";
                String email = "";
                String username = "";

                try{
                    name = object.getString("name");
                    email = object.getString("email");
                    facebookID = object.getString("id");


                    String[] one = name.toLowerCase().split(" ");
                    for (String word : one) { username += word; }
                    Log.i("log-", "USERNAME: " + username + "\n");
                    Log.i("log-", "email: " + email + "\n");
                    Log.i("log-", "name: " + name + "\n");

                } catch(JSONException e){ e.printStackTrace(); }


                // SAVE NEW USER IN YOUR PARSE DASHBOARD -> USER CLASS
                final String finalFacebookID = facebookID;
                final String finalUsername = username;
                final String finalEmail = email;
                final String finalName = name;

                final ParseUser currUser = ParseUser.getCurrentUser();
                currUser.put(Configs.USER_USERNAME, finalUsername);
                if (finalEmail != null) { currUser.put(Configs.USER_EMAIL, finalEmail);
                } else { currUser.put(Configs.USER_EMAIL, facebookID + "@facebook.com"); }
                currUser.put(Configs.USER_FULLNAME, finalName);
                currUser.put(Configs.USER_IS_REPORTED, false);
                List<String> hasBlocked = new ArrayList<String>();
                currUser.put(Configs.USER_HAS_BLOCKED, hasBlocked);

                currUser.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        Log.i("log-", "NEW USER signed up and logged in through Facebook...");


                        // Get and Save avatar from Facebook
                        new Timer().schedule(new TimerTask() {
                            @Override public void run() {
                                try {
                                    URL imageURL = new URL("https://graph.facebook.com/" + finalFacebookID + "/picture?type=large");
                                    Bitmap avatarBm = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());

                                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                    avatarBm.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                                    byte[] byteArray = stream.toByteArray();
                                    ParseFile imageFile = new ParseFile("image.jpg", byteArray);
                                    currUser.put(Configs.USER_AVATAR, imageFile);
                                    currUser.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException error) {
                                            Log.i("log-", "... AND AVATAR SAVED!");
                                            Configs.hidePD();
                                            startActivity(new Intent(Wizard.this, Home.class));
                                        }});
                                } catch (IOException error) { error.printStackTrace(); }

                            }}, 1000); // 1 second


                    }}); // end saveInBackground

            }}); // end graphRequest


        Bundle parameters = new Bundle();
        parameters.putString("fields", "email, name, picture.type(large)");
        request.setParameters(parameters);
        request.executeAsync();
    }
    // END FACEBOOK GRAPH REQUEST --------------------------------------------------------------------







    // CHANGE WIZARD SCREEN TIMER TASK ---------------------------------------------
    class ChangeWizardScreen extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                public void run() {
                    if (page > 2) {
                        timer.cancel();
                    } else {
                        viewPager.setCurrentItem(page++);
        }}});}
    }


    // MARK: - WIZARD PAGE LISTENER (CHANGE DOTS IMAGE ON SWIPE)
    private class PageListener extends ViewPager.SimpleOnPageChangeListener {
        public void onPageSelected(int position) {
            dotsImg = findViewById(R.id.dotsImg);
            Log.i("log-", "PAGE SELECTED: " + position);
            switch (position) {
                case 0: dotsImg.setImageResource(R.drawable.dots1); break;
                case 1: dotsImg.setImageResource(R.drawable.dots2); break;
                case 2: dotsImg.setImageResource(R.drawable.dots3); break;
            }
        }
    }



}//@end
