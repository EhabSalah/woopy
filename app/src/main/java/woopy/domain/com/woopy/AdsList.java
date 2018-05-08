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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.appevents.internal.AutomaticAnalyticsLogger;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

public class AdsList extends AppCompatActivity implements LocationListener {

    /* Views */
    EditText searchTxt;
    Button cancelButt, categoryButt, sortByButt, cityCountryButt;
    TextView distanceTxt, noSearchTxt;
    RelativeLayout noResultsLayout;



    /* Variables */
    List<ParseObject>adsArray;
    String searchString;
    MarshMallowPermission mmp = new MarshMallowPermission(this);
    Location currentLocation;
    LocationManager locationManager;





    // ON START() ----------------------------------------------------------------------------------
    @Override
    protected void onStart() {
        super.onStart();

        // Get strings from Home.java
        Bundle extras = getIntent().getExtras();
        if (extras != null) { searchString = extras.getString("searchString"); }
        Log.i("log-", "SEARCH TEXT STRING: " + searchString +
                      "\nCATEGORY: " + Configs.selectedCategory);

        // Set search variables for the query
        if (searchString != null ) {
            searchTxt.setText(searchString);
            categoryButt.setText(Configs.selectedCategory);
        } else {
            searchTxt.setText(Configs.selectedCategory);
            categoryButt.setText(Configs.selectedCategory);
        }

        // Set sort By text
        sortByButt.setText(Configs.sortBy);



        // Check if Location service is permitted
        if (!mmp.checkPermissionForLocation()) {
            mmp.requestPermissionForLocation();
        } else {

            // Get ads from a chosen location
            if (Configs.chosenLocation != null) {
                currentLocation = Configs.chosenLocation;

                getCityCountryNames();

            // Get current Location
            } else {
                getCurrentLocation();
            }
        }
    }






    // ON CREATE() --------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ads_list);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Hide ActionBar
        getSupportActionBar().hide();



        // Init views
        searchTxt = findViewById(R.id.alSearchTxt);
        searchTxt.setTypeface(Configs.titRegular);
        distanceTxt = findViewById(R.id.alDistanceTxt);
        distanceTxt.setTypeface(Configs.titRegular);
        categoryButt = findViewById(R.id.alCategoryButt);
        categoryButt.setTypeface(Configs.titSemibold);
        sortByButt = findViewById(R.id.alSortByButt);
        sortByButt.setTypeface(Configs.titSemibold);
        cancelButt = findViewById(R.id.alCancelButt);
        cancelButt.setTypeface(Configs.titSemibold);
        cityCountryButt = findViewById(R.id.alCityCountryButt);
        cityCountryButt.setTypeface(Configs.titSemibold);
        noSearchTxt = findViewById(R.id.alNoSearchTxt);
        noSearchTxt.setTypeface(Configs.titSemibold);
        noResultsLayout = findViewById(R.id.alNoResultsLayout);
        noResultsLayout.setVisibility(View.INVISIBLE);





        // MARK: - SEARCH ADS BY KEYWORDS --------------------------------------------------------
        searchTxt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                    if (!searchTxt.getText().toString().matches("")) {

                        Configs.selectedCategory = "All";
                        categoryButt.setText("All");
                        searchString = searchTxt.getText().toString();
                        dismisskeyboard();

                        // Call query
                        queryAds();

                        return true;
                    }

                    // No text -> No search
                } else { Configs.simpleAlert("You must type somehting!", AdsList.this); }

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
                searchString = null;
                Configs.selectedCategory = "All";
                categoryButt.setText(Configs.selectedCategory);
                cancelButt.setVisibility(View.INVISIBLE);
                dismisskeyboard();

                // Call query
                queryAds();
            }});





        // MARK: - CHATS BUTTON ------------------------------------
        Button chatButt = findViewById(R.id.alChatButt);
        chatButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ParseUser.getCurrentUser().getUsername() != null) {
                    startActivity(new Intent(AdsList.this, Chats.class));
                } else {
                    Configs.loginAlert("You must be loggen in to see your Chats. Want to login now?", AdsList.this);
                }
        }});





        // MARK: - CITY/COUNTRY BUTTON ------------------------------------
        cityCountryButt.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              Log.i("log-", "CURR LOCATION: " + currentLocation);

             // if (!distanceTxt.getText().toString().matches("loading...")) {
              if (currentLocation != null) {
                  Double lat = currentLocation.getLatitude();
                  Double lng = currentLocation.getLongitude();
                  Log.i("log-", "LATITUDE: " + lat + "\nLONGITUDE: " + lng);
                  Intent i = new Intent(AdsList.this, DistanceMap.class);
                  Bundle extras = new Bundle();
                  extras.putDouble("latitude", lat);
                  extras.putDouble("longitude", lng);
                  i.putExtras(extras);
                  startActivity(i);

              } else {
                  // Set default Location
                  Intent i = new Intent(AdsList.this, DistanceMap.class);
                  Bundle extras = new Bundle();
                  extras.putDouble("latitude", Configs.DEFAULT_LOCATION.latitude);
                  extras.putDouble("longitude", Configs.DEFAULT_LOCATION.longitude);
                  i.putExtras(extras);
                  startActivity(i);
              }
              // }
         }});





        // MARK: - CHOOSE CATEGORY BUTTON ------------------------------------
        Button catButt = findViewById(R.id.alCategoryButt);
        catButt.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              startActivity(new Intent(AdsList.this, Categories.class));
         }});



        // MARK: - SORT BY BUTTON ------------------------------------
        Button sbButt = findViewById(R.id.alSortByButt);
        sbButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AdsList.this, SortBy.class));
        }});


        // MARK: - BACK BUTTON ------------------------------------
        Button backButt = findViewById(R.id.alBackButt);
        backButt.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              finish();
         }});




        // Init AdMob banner
        AdView mAdView = findViewById(R.id.admobBanner);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


    } // end onCreate()







    // MARK: - GET CURRENT LOCATION ------------------------------------------------------
    protected void getCurrentLocation() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_LOW);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;
        String provider = locationManager.getBestProvider(criteria, true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        currentLocation = locationManager.getLastKnownLocation(provider);

        if (currentLocation != null) {

            getCityCountryNames();

        // Try to find your current Location one more time
        } else { locationManager.requestLocationUpdates(provider, 1000, 0, this); }
    }


    @Override
    public void onLocationChanged(Location location) {
        //remove location callback:
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.removeUpdates(this);
        currentLocation = location;

        if (currentLocation != null) {

            getCityCountryNames();


        // NO GPS location found!
        } else {
            Configs.simpleAlert("Failed to get your Location.\nGo into Settings and make sure Location Service is enabled!", AdsList.this);

            // Set New York City as default currentLocation
            currentLocation = new Location("provider");
            currentLocation.setLatitude(Configs.DEFAULT_LOCATION.latitude) ;
            currentLocation.setLongitude(Configs.DEFAULT_LOCATION.longitude);


            // Set distance and city labels
            String distFormatted = String.format("%.0f", Configs.distanceInMiles);
            distanceTxt.setText(distFormatted + " Mi FROM");
            cityCountryButt.setText("New York, USA");


            // Call query
            queryAds();
        }

    }

    @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
    @Override public void onProviderEnabled(String provider) {}
    @Override public void onProviderDisabled(String provider) {}






    // MARK: - GET CITY AND COUNTRY NAMES | CALL QUERY ADS -----------------------------------
    void getCityCountryNames() {
        try {
            Geocoder geocoder = new Geocoder(AdsList.this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1);
            if (Geocoder.isPresent()) {
                Address returnAddress = addresses.get(0);
                String city = returnAddress.getLocality();
                String country = returnAddress.getCountryName();

                if (city == null) {
                    city = "";
                }
                // Show City/Country
                cityCountryButt .setText(city + ", " + country );

                // Set distance
                String distFormatted = String.format("%.0f", Configs.distanceInMiles);
                distanceTxt.setText(distFormatted + " Mi FROM");


                // Call query
                queryAds();

            } else { Toast.makeText(getApplicationContext(), "Geocoder not present!", Toast.LENGTH_SHORT).show(); }
        } catch (IOException e) { Configs.simpleAlert(e.getMessage(), AdsList.this); }

    }








    // MARK: - QUERY ADS ------------------------------------------------------------------
    void queryAds() {
        noResultsLayout.setVisibility(View.INVISIBLE);

        Configs.showPD("Please wait...", AdsList.this);


        // Launch query
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Configs.ADS_CLASS_NAME);

        // query by search text
        if (searchString != null ) {
            // Get keywords
            List<String> keywords = new ArrayList<String>();
            String[] one = searchString.toLowerCase().split(" ");
            keywords.addAll(Arrays.asList(one));
            Log.d("KEYWORDS", "\n" + keywords + "\n");

            query.whereContainedIn(Configs.ADS_KEYWORDS, keywords); }

        // query by Category
        if (!Configs.selectedCategory.matches("All")) { query.whereEqualTo(Configs.ADS_CATEGORY, Configs.selectedCategory); }

        // query nearby
        ParseGeoPoint gp = new ParseGeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
        query.whereWithinMiles(Configs.ADS_LOCATION, gp, Configs.distanceInMiles);

        // query sortBy
        switch (Configs.sortBy ) {
            case "Recent": query.orderByDescending(Configs.ADS_CREATED_AT); break;
            case "Lowest Price": query.orderByAscending(Configs.ADS_PRICE); break;
            case "Highest Price": query.orderByDescending(Configs.ADS_PRICE); break;
            case "New": query.whereEqualTo(Configs.ADS_CONDITION, "New"); break;
            case "Used": query.whereEqualTo(Configs.ADS_CONDITION, "Used"); break;
            case "Most Liked": query.orderByDescending(Configs.ADS_LIKES); break;

            default:break; }


        query.whereEqualTo(Configs.ADS_IS_REPORTED, false);

        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException error) {
                if (error == null) {
                    adsArray = objects;
                    Configs.hidePD();

                    // Show/hide noResult view
                    if (adsArray.size() == 0) { noResultsLayout.setVisibility(View.VISIBLE);
                    } else { noResultsLayout.setVisibility(View.INVISIBLE); }

                    // CUSTOM GRID ADAPTER
                    class GridAdapter extends BaseAdapter {
                        private Context context;
                        private GridAdapter(Context context, List<ParseObject> objects) {
                            super();
                            this.context = context;
                        }


                        // CONFIGURE CELL
                        @Override
                        public View getView(final int position, View cell, ViewGroup parent) {
                            if (cell == null) {
                                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                assert inflater != null;
                                cell = inflater.inflate(R.layout.cell_ad, null);
                            }
                            final View finalCell = cell;


                            // Get Parse object
                            final ParseObject adObj = adsArray.get(position);

                            // Get userPointer
                            adObj.getParseObject(Configs.ADS_SELLER_POINTER).fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                                public void done(final ParseObject userPointer, ParseException e) {

                                    // Get Image 1
                                    final ImageView aImage = finalCell.findViewById(R.id.cadImage);
                                    Configs.getParseImage(aImage, adObj, Configs.ADS_IMAGE1);

                                    // Show Ad details on click on the image
                                    aImage.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent i = new Intent(AdsList.this, AdDetails.class);
                                            Bundle extras = new Bundle();
                                            extras.putString("objectID", adObj.getObjectId());
                                            i.putExtras(extras);
                                            startActivity(i);
                                    }});


                                    // Get Title
                                    TextView titleTxt = finalCell.findViewById(R.id.cadAdTitleTxt);
                                    titleTxt.setTypeface(Configs.titRegular);
                                    titleTxt.setText(adObj.getString(Configs.ADS_TITLE));

                                    // Get Price
                                    TextView priceTxt = finalCell.findViewById(R.id.cadPriceTxt);
                                    priceTxt.setTypeface(Configs.titRegular);
                                    priceTxt.setText(adObj.getString(Configs.ADS_CURRENCY) + String.valueOf(adObj.getNumber(Configs.ADS_PRICE)) );

                                    // Get likes
                                    final TextView likesTxt = finalCell.findViewById(R.id.cadLikesTxt);
                                    likesTxt.setTypeface(Configs.titRegular);
                                    if (adObj.getNumber(Configs.ADS_LIKES) != null) { likesTxt.setText(Configs.roundThousandsIntoK(adObj.getNumber(Configs.ADS_LIKES)) );
                                    } else { likesTxt.setText("0"); }

                                    // Get comments
                                    TextView commTxt = finalCell.findViewById(R.id.cadCommentsTxt);
                                    commTxt.setTypeface(Configs.titRegular);
                                    if (adObj.getNumber(Configs.ADS_COMMENTS) != null) { commTxt.setText(Configs.roundThousandsIntoK(adObj.getNumber(Configs.ADS_COMMENTS)) );
                                    } else { commTxt.setText("0"); }

                                    // Get Date
                                    TextView dateTxt = finalCell.findViewById(R.id.cadDateTxt);
                                    dateTxt.setTypeface(Configs.titRegular);
                                    dateTxt.setText(Configs.timeAgoSinceDate(adObj.getCreatedAt()));



                                    // Get Users' Avar
                                    final ImageView avImg = finalCell.findViewById(R.id.cadAvatarImg);
                                    Configs.getParseImage(avImg, userPointer, Configs.USER_AVATAR);


                                    // Get User's username
                                    TextView uTxt = finalCell.findViewById(R.id.cadUsernametxt);
                                    uTxt.setTypeface(Configs.titRegular);
                                    uTxt.setText(userPointer.getString(Configs.USER_USERNAME));


                                    // CHECK IF YOU'VE ALREADY LIKED THIS AD AND CHANGE LIKE ICON
                                    String currUserID = ParseUser.getCurrentUser().getObjectId();
                                    final Button likeButt = finalCell.findViewById(R.id.cadLikeButt);
                                    if (adObj.getList(Configs.ADS_LIKED_BY) != null) {
                                        List<String>likedByArr = adObj.getList(Configs.ADS_LIKED_BY);
                                        if (likedByArr.contains(currUserID)) {
                                            likeButt.setBackgroundResource(R.drawable.liked_icon);
                                        } else {
                                            likeButt.setBackgroundResource(R.drawable.like_butt);
                                        }
                                    } else {
                                        likeButt.setBackgroundResource(R.drawable.like_butt);
                                    }


                                    // Clip the cell layout to outline
                                    RelativeLayout cellLayout = finalCell.findViewById(R.id.cadCellLayout);
                                    cellLayout.setClipToOutline(true);




                                    // MARK: - AVATAR BUTTON ------------------------------------------------------
                                    avImg.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                            // Pass objectID to the other Activity
                                            Intent i = new Intent(AdsList.this, UserProfile.class);
                                            Bundle extras = new Bundle();
                                            extras.putString("objectID", userPointer.getObjectId());
                                            i.putExtras(extras);
                                            startActivity(i);
                                    }});



                                    // MARK: - COMMENTS BUTTON ------------------------------------------------------
                                    Button commButt = finalCell.findViewById(R.id.cadCommentsButt);
                                    commButt.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if (ParseUser.getCurrentUser().getUsername() != null) {

                                                // Pass objectID to the other Activity
                                                Intent i = new Intent(AdsList.this, Comments.class);
                                                Bundle extras = new Bundle();
                                                extras.putString("objectID", adObj.getObjectId());
                                                i.putExtras(extras);
                                                startActivity(i);
                                            } else {
                                                Configs.loginAlert("You must be logged in to see and post comments. Want to login now?", AdsList.this);
                                            }
                                    }});




                                    // MARK: - AD OPTIONS BUTTON ------------------------------------
                                    Button optionsButt = finalCell.findViewById(R.id.cadOptionsButt);
                                    optionsButt.setOnClickListener(new View.OnClickListener() {
                                      @Override
                                      public void onClick(View view) {
                                          AlertDialog.Builder alert = new AlertDialog.Builder(AdsList.this);
                                          alert.setMessage("Select option")
                                              .setTitle(R.string.app_name)

                                              // REPORT AD
                                              .setPositiveButton("Report Ad", new DialogInterface.OnClickListener() {
                                                  @Override
                                                  public void onClick(DialogInterface dialogInterface, int i) {
                                                      Intent in = new Intent(AdsList.this, ReportAdOrUser.class);
                                                      Bundle extras = new Bundle();
                                                      extras.putString("adObjectID", adObj.getObjectId());
                                                      extras.putString("reportType", "Ad");
                                                      in.putExtras(extras);
                                                      startActivity(in);
                                              }})


                                              // SHARE AD
                                              .setNegativeButton("Share", new DialogInterface.OnClickListener() {
                                                  @Override
                                                  public void onClick(DialogInterface dialogInterface, int i) {
                                                      if (!mmp.checkPermissionForWriteExternalStorage()) {
                                                          mmp.requestPermissionForWriteExternalStorage();
                                                      } else {
                                                          // Get Image
                                                          ParseFile fileObject = (ParseFile)adObj.get(Configs.ADS_IMAGE1);
                                                          if (fileObject != null ) {
                                                              fileObject.getDataInBackground(new GetDataCallback() {
                                                                  public void done(byte[] data, ParseException error) {
                                                                      if (error == null) {
                                                                          Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                                                                          if (bmp != null) {
                                                                              Uri uri = Configs.getImageUri(AdsList.this, bmp);
                                                                              Intent intent = new Intent(Intent.ACTION_SEND);
                                                                              intent.setType("image/jpeg");
                                                                              intent.putExtra(Intent.EXTRA_STREAM, uri);
                                                                              intent.putExtra(Intent.EXTRA_TEXT, "Check this out: " + adObj.getString(Configs.ADS_TITLE) + " on #woopy");
                                                                              startActivity(Intent.createChooser(intent, "Share on..."));
                                                          }}}});}
                                                      }
                                              }})


                                              // Cancel
                                              .setNeutralButton("Cancel", null)
                                              .setIcon(R.drawable.logo);
                                          alert.create().show();
                                     }});





                                    // MARK: - LIKE AD BUTTON ------------------------------------
                                    likeButt.setOnClickListener(new View.OnClickListener() {
                                      @Override
                                      public void onClick(View view) {
                                          if (ParseUser.getCurrentUser().getUsername() != null) {

                                              Configs.showPD("Please wait...", AdsList.this);

                                          final ParseUser currUser = ParseUser.getCurrentUser();

                                          // 1. CHECK IF YOU'VE ALREADY LIKED THIS AD
                                          ParseQuery<ParseObject> query = ParseQuery.getQuery(Configs.LIKES_CLASS_NAME);
                                          query.whereEqualTo(Configs.LIKES_CURR_USER, currUser);
                                          query.whereEqualTo(Configs.LIKES_AD_LIKED, adObj);
                                          query.findInBackground(new FindCallback<ParseObject>() {
                                              public void done(List<ParseObject> objects, ParseException error) {
                                                  if (error == null) {

                                                      // 2. LIKE THIS AD!
                                                      if (objects.size() == 0) {

                                                          ParseObject likeObj = new ParseObject(Configs.LIKES_CLASS_NAME);

                                                          // Save data
                                                          likeObj.put(Configs.LIKES_CURR_USER, currUser);
                                                          likeObj.put(Configs.LIKES_AD_LIKED, adObj);
                                                          likeObj.saveInBackground(new SaveCallback() {
                                                              @Override
                                                              public void done(ParseException e) {
                                                                  if (e == null){

                                                                      likeButt.setBackgroundResource(R.drawable.liked_icon);
                                                                      Configs.hidePD();

                                                                      // Increment likes for the adObj
                                                                      adObj.increment(Configs.ADS_LIKES, 1);

                                                                      // Add the user's objectID
                                                                      if (adObj.getList(Configs.ADS_LIKED_BY) != null) {
                                                                          List<String>likedByArr = adObj.getList(Configs.ADS_LIKED_BY);
                                                                          likedByArr.add(currUser.getObjectId());
                                                                          adObj.put(Configs.ADS_LIKED_BY, likedByArr);
                                                                      } else {
                                                                          List<String>likedByArr = new ArrayList<String>();
                                                                          likedByArr.add(currUser.getObjectId());
                                                                          adObj.put(Configs.ADS_LIKED_BY, likedByArr);
                                                                      }
                                                                      adObj.saveInBackground();

                                                                      // Set likes number in the cell
                                                                      int likesNr = Integer.parseInt(likesTxt.getText().toString());
                                                                      likesTxt.setText(String.valueOf(likesNr+1));


                                                                      // Send push notification
                                                                      final ParseUser sellerPointer = (ParseUser) adObj.get(Configs.ADS_SELLER_POINTER);

                                                                      final String pushMessage = "@" + currUser.getUsername() + " liked your Ad: " + adObj.getString(Configs.ADS_TITLE);

                                                                      HashMap<String, Object> params = new HashMap<String, Object>();
                                                                      params.put("someKey", sellerPointer.getObjectId());
                                                                      params.put("data", pushMessage);

                                                                      ParseCloud.callFunctionInBackground("pushAndroid", params, new FunctionCallback<String>() {
                                                                          @Override
                                                                          public void done(String object, ParseException e) {
                                                                              if (e == null) {
                                                                                  Log.i("log-", "PUSH SENT TO: " + sellerPointer.getUsername()
                                                                                          + "\nMESSAGE: "
                                                                                          + pushMessage);

                                                                              // Error in Cloud Code
                                                                              } else {
                                                                                  Configs.hidePD();
                                                                                  Configs.simpleAlert(e.getMessage(), AdsList.this);
                                                                      }}});



                                                                      // Save Activity
                                                                      ParseObject actObj = new ParseObject(Configs.ACTIVITY_CLASS_NAME);
                                                                      actObj.put(Configs.ACTIVITY_CURRENT_USER, sellerPointer);
                                                                      actObj.put(Configs.ACTIVITY_OTHER_USER, currUser);
                                                                      actObj.put(Configs.ACTIVITY_TEXT, pushMessage);
                                                                      actObj.saveInBackground();
                                                                      


                                                                  // error on saving like
                                                                  } else {
                                                                      Configs.hidePD();
                                                                      Configs.simpleAlert(e.getMessage(), AdsList.this);
                                                          }}});





                                                      // 3. UNLIKE THIS AD :(
                                                      } else {
                                                          ParseObject likeObj = new ParseObject(Configs.LIKES_CLASS_NAME);
                                                          likeObj = objects.get(0);
                                                          likeObj.deleteInBackground(new DeleteCallback() {
                                                              @Override
                                                              public void done(ParseException e) {
                                                                  if (e == null) {
                                                                      likeButt.setBackgroundResource(R.drawable.like_butt);
                                                                      Configs.hidePD();

                                                                      // Decrement likes for the adObj
                                                                      adObj.increment(Configs.ADS_LIKES, -1);

                                                                      // Remove the user's objectID
                                                                      List<String>likedByArr = adObj.getList(Configs.ADS_LIKED_BY);
                                                                      likedByArr.remove(currUser.getObjectId());
                                                                      adObj.put(Configs.ADS_LIKED_BY, likedByArr);
                                                                      adObj.saveInBackground();

                                                                      // Set likes number in the cell
                                                                      int likesNr = Integer.parseInt(likesTxt.getText().toString());
                                                                      likesTxt.setText(String.valueOf(likesNr-1));

                                                                  }}});
                                                      }


                                                  // error in query
                                                  } else {
                                                      Configs.hidePD();
                                                      Configs.simpleAlert(error.getMessage(), AdsList.this);

                                              }}});// end query for Likes



                                          // YOU'RE NOT LOGGED IN
                                          } else {  Configs.loginAlert("You must be logged in to like this Ad. Want to login now?", AdsList.this);  }

                                      }});


                            }}); // end userPointer


                            return cell;
                        }

                        @Override public int getCount() { return adsArray.size(); }
                        @Override public Object getItem(int position) { return adsArray.get(position); }
                        @Override public long getItemId(int position) { return position; }
                    }


                    // Init GridView and set its adapter
                    GridView aGrid = findViewById(R.id.alAdsGridView);
                    aGrid.setAdapter(new GridAdapter(AdsList.this, adsArray));

                    // Set number of Columns accordingly to the device used
                    float scalefactor = getResources().getDisplayMetrics().density * 150; // 150 is the cell's width
                    int number = getWindowManager().getDefaultDisplay().getWidth();
                    int columns = (int) ((float) number / (float) scalefactor);
                    aGrid.setNumColumns(columns);


                // error in query
                } else {
                    Configs.hidePD();
                    Configs.simpleAlert(error.getMessage(), AdsList.this);
        }}});

    }






    // MARK: - DISMISS KEYBOARD
    public void dismisskeyboard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchTxt.getWindowToken(), 0);
    }


} //@end
