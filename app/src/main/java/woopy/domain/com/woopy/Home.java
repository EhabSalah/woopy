package woopy.domain.com.woopy;

/*-------------------------------

    - woopy -

    Created by cubycode @2017
    All Rights reserved

-------------------------------*/

import android.*;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

public class Home extends AppCompatActivity {

    /* Views */
    EditText searchTxt;
    Button cancelButt;



    /* Variables */
    List<ParseObject>categoriesArray;
    MarshMallowPermission mmp = new MarshMallowPermission(this);





    // ON START ------------------------------------------------------------
    @Override
    protected void onStart() {
        super.onStart();
        if (ParseUser.getCurrentUser().getUsername() != null) {

            // Register for Push Notifications
            ParseInstallation installation = ParseInstallation.getCurrentInstallation();


            // IMPORTANT: REPLACE "478517440140" WITH YOUR OWN GCM Sender ID
            installation.put("GCMSenderId", "478517440140");
            //--------------------------------------------------------


            installation.put("username", ParseUser.getCurrentUser().getUsername());
            installation.put("userID", ParseUser.getCurrentUser().getObjectId());
            installation.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    Log.i("log-", "REGISTERED FOR PUSH NOTIFICATIONS");
            }});
        }


        // Request Storage permission
        if(!mmp.checkPermissionForReadExternalStorage()) {
            mmp.requestPermissionForReadExternalStorage();
        }

    }







    // ON CREATE ------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Hide ActionBar
        getSupportActionBar().hide();



        // Init TabBar buttons
        Button tab_two = findViewById(R.id.tab_two);
        Button tab_three = findViewById(R.id.tab_three);
        Button tab_four = findViewById(R.id.tab_four);
        Button tab_five = findViewById(R.id.tab_five);

        tab_two.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ParseUser.getCurrentUser().getUsername() != null) {
                    startActivity(new Intent(Home.this, MyLikes.class));
                } else {
                    startActivity(new Intent(Home.this, Wizard.class));
                }
            }});

        tab_three.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ParseUser.getCurrentUser().getUsername() != null) {
                    startActivity(new Intent(Home.this, SellEditItem.class));
                } else {
                    startActivity(new Intent(Home.this, Wizard.class));
                }
            }});

        tab_four.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ParseUser.getCurrentUser().getUsername() != null) {
                    startActivity(new Intent(Home.this, ActivityScreen.class));
                } else {
                    startActivity(new Intent(Home.this, Wizard.class));
                }
            }});

        tab_five.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ParseUser.getCurrentUser().getUsername() != null) {
                    startActivity(new Intent(Home.this, Account.class));
                } else {
                    startActivity(new Intent(Home.this, Wizard.class));
                }
        }});




        // Init views
        searchTxt = findViewById(R.id.hSearchTxt);
        searchTxt.setTypeface(Configs.titRegular);
        cancelButt = findViewById(R.id.hCancelButt);
        cancelButt.setTypeface(Configs.titSemibold);

        // Check if Location service is permitted
        if (!mmp.checkPermissionForLocation()) {
            mmp.requestPermissionForLocation();
        } else {
            Log.i("log-", "LOCATION IS PERMITTED!");
        }





        // Call query
        queryCategories();





        // MARK: - SEARCH ADS BY KEYWORDS --------------------------------------------------------
        searchTxt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (!searchTxt.getText().toString().matches("")) {

                        Configs.selectedCategory = "All";
                        dismisskeyboard();

                        // Pass strings to AdsList.java
                        Intent i = new Intent(Home.this, AdsList.class);
                        Bundle extras = new Bundle();
                        extras.putString("searchString", searchTxt.getText().toString());
                        i.putExtras(extras);
                        startActivity(i);

                        return true;
                    }

                // No text -> No search
                } else { Configs.simpleAlert(getResources().getString(R.string.alert_you_must_type_somehting), Home.this); }


                return false;
        }});


        // EditText TextWatcher delegate
        searchTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                cancelButt.setVisibility(View.VISIBLE);
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                cancelButt.setVisibility(View.VISIBLE);
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });




        // MARK: - CANCEL SEARCH BUTTON ------------------------------------
        cancelButt.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              searchTxt.setText("");
              cancelButt.setVisibility(View.INVISIBLE);
              dismisskeyboard();
        }});




        // MARK: - CHATS BUTTON ------------------------------------
        Button chatButt = findViewById(R.id.hChatButt);
        chatButt.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              if (ParseUser.getCurrentUser().getUsername() != null) {
                  startActivity(new Intent(Home.this, Chats.class));
              } else {
                  Configs.loginAlert(getResources().getString(R.string.alert_You_must_be_logged_in_to_see_your_Chats_Want_to_login_now), Home.this);
              }
        }});




        // Init AdMob banner
        AdView mAdView = findViewById(R.id.admobBanner);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


    }// end onCreate()








    // MARK: - QUERY CATEGORIES ----------------------------------------------------------------------
    void queryCategories() {
        Configs.showPD(getResources().getString(R.string.alert_please_wait), Home.this);

        ParseQuery<ParseObject> query = ParseQuery.getQuery(Configs.CATEGORIES_CLASS_NAME);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException error) {
                if (error == null) {
                    categoriesArray = objects;
                    Configs.hidePD();


                    // CUSTOM GRID ADAPTER --------------------------
                    class GridAdapter extends BaseAdapter {
                        private Context context;
                        private GridAdapter(Context context, List<ParseObject> objects) {
                            super();
                            this.context = context;
                        }


                        // CONFIGURE CELL
                        @Override
                        public View getView(int position, View cell, ViewGroup parent) {
                            if (cell == null) {
                                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                assert inflater != null;
                                cell = inflater.inflate(R.layout.cell_home, null);
                            }

                            // Get Parse object
                            ParseObject cObj = categoriesArray.get(position);

                            // Get Title
                            TextView titleTxt = cell.findViewById(R.id.chCatNameTxt);
                            titleTxt.setTypeface(Configs.qsBold);
                            titleTxt.setText(cObj.getString(Configs.CATEGORIES_CATEGORY).toUpperCase());

                            // Get Image
                            final ImageView aImage = cell.findViewById(R.id.chCatImage);
                            Configs.getParseImage(aImage, cObj, Configs.CATEGORIES_IMAGE);


                        return cell;
                        }

                        @Override public int getCount() { return categoriesArray.size(); }
                        @Override public Object getItem(int position) { return categoriesArray.get(position); }
                        @Override public long getItemId(int position) { return position; }
                    }


                    // Init GridView and set its adapter
                    GridView aGrid = findViewById(R.id.hCategoriesGridView);
                    aGrid.setAdapter(new GridAdapter(Home.this, categoriesArray));

                    // Set number of Columns accordingly to the device used
                    float scalefactor = getResources().getDisplayMetrics().density * 150; // 150 is the cell's width
                    int number = getWindowManager().getDefaultDisplay().getWidth();
                    int columns = (int) ((float) number / (float) scalefactor);
                    aGrid.setNumColumns(columns);

                    // CATEGORY TAPPED -> SHOW ADS LIST
                    aGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                            ParseObject cObj = categoriesArray.get(position);

                            // Set selected Category
                            Configs.selectedCategory = cObj.getString(Configs.CATEGORIES_CATEGORY);

                            // Go to AdsList
                            startActivity(new Intent(Home.this, AdsList.class));
                    }});


                // error in query
                } else {
                    Configs.hidePD();
                    Configs.simpleAlert(error.getMessage(), Home.this);
        }}});

    }




   // MARK: - DISMISS KEYBOARD
   public void dismisskeyboard() {
       InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
       assert imm != null;
       imm.hideSoftInputFromWindow(searchTxt.getWindowToken(), 0);
   }


}// @end
