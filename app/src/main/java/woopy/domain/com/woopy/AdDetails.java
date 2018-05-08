package woopy.domain.com.woopy;

/*-------------------------------

    - woopy -

    Created by cubycode @2017
    All Rights reserved

-------------------------------*/

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Picture;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AdDetails extends AppCompatActivity {

    /* Views */
    Button likeButt;
    TextView likesTxt;

    /* Variables */
    ParseObject adObj;
    MarshMallowPermission mmp = new MarshMallowPermission(this);




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ad_details);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Hide ActionBar
        getSupportActionBar().hide();



        // Init views
        likeButt = findViewById(R.id.adLikeButt);
        likesTxt = findViewById(R.id.adLikesTxt);



        // Get objectID from previous .java
        Bundle extras = getIntent().getExtras();
        assert extras != null;
        String objectID = extras.getString("objectID");
        adObj = ParseObject.createWithoutData(Configs.ADS_CLASS_NAME, objectID);
        try { adObj.fetchIfNeeded().getParseObject(Configs.ADS_CLASS_NAME);


            // Call queries
            showAdDetails();
            queryIfYouLikedThisAd();




            // MARK: - OPTIONS BUTTON ------------------------------------
            Button opButt = findViewById(R.id.adOptionsButt);
            opButt.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                  AlertDialog.Builder alert = new AlertDialog.Builder(AdDetails.this);
                  alert.setMessage("Select option")
                      .setTitle(R.string.app_name)

                      // Report Ad
                      .setPositiveButton("Report Ad", new DialogInterface.OnClickListener() {
                          @Override
                          public void onClick(DialogInterface dialogInterface, int i) {
                              // Pass objectID to the other Activity
                              Intent in = new Intent(AdDetails.this, ReportAdOrUser.class);
                              Bundle extras = new Bundle();
                              extras.putString("adObjectID", adObj.getObjectId());
                              extras.putString("reportType", "Ad");
                              in.putExtras(extras);
                              startActivity(in);
                      }})


                      // Share Ad
                      .setNegativeButton("Share", new DialogInterface.OnClickListener() {
                              @Override
                              public void onClick(DialogInterface dialogInterface, int i) {

                              if (!mmp.checkPermissionForWriteExternalStorage()) {
                                  mmp.requestPermissionForWriteExternalStorage();
                              } else {
                                  ImageView img1 = findViewById(R.id.adImg1);
                                  Bitmap bitmap = ((BitmapDrawable) img1.getDrawable()).getBitmap();
                                  Uri uri = Configs.getImageUri(AdDetails.this, bitmap);
                                  Intent intent = new Intent(Intent.ACTION_SEND);
                                  intent.setType("image/jpeg");
                                  intent.putExtra(Intent.EXTRA_STREAM, uri);
                                  intent.putExtra(Intent.EXTRA_TEXT, "Check this out: '" + adObj.getString(Configs.ADS_TITLE) + "' on #woopy");
                                  startActivity(Intent.createChooser(intent, "Share on..."));
                              }
                      }})

                      .setNeutralButton("Cancel", null)
                      .setIcon(R.drawable.logo);
                  alert.create().show();
            }});








            // MARK: - CHAT WITH SELLER BUTTON ------------------------------------
            Button chatButt = findViewById(R.id.adChatButt);
            chatButt.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                  if (ParseUser.getCurrentUser().getUsername() != null) {

                      // Get sellerPointer
                      adObj.getParseObject(Configs.ADS_SELLER_POINTER).fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                          public void done(ParseObject sellerPointer, ParseException e) {
                              List<String> hasBlocked = sellerPointer.getList(Configs.USER_HAS_BLOCKED);

                              // Seller has blocked you
                              if (hasBlocked.contains(ParseUser.getCurrentUser().getObjectId())) {
                                  Configs.simpleAlert("Sorry, @" + sellerPointer.getString(Configs.USER_USERNAME) + " has blocked you, you can't chat with this user.", AdDetails.this);

                              } else {
                                  // Pass objectID to the other Activity
                                  Intent i = new Intent(AdDetails.this, InboxActivity.class);
                                  Bundle extras = new Bundle();
                                  extras.putString("adObjectID", adObj.getObjectId());
                                  extras.putString("sellerObjectID", sellerPointer.getObjectId());
                                  i.putExtras(extras);
                                  startActivity(i);
                      }}
                      });

                  } else {
                      Configs.loginAlert("You must be logged in to chat. Want to login now?", AdDetails.this);
                  }
             }});








            // MARK: - LIKE AD BUTTON ------------------------------------
            likeButt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (ParseUser.getCurrentUser().getUsername() != null) {

                        Configs.showPD("Please wait...", AdDetails.this);

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
                                                if (e == null) {

                                                    likeButt.setBackgroundResource(R.drawable.liked_icon);
                                                    Configs.hidePD();

                                                    // Increment likes for the adObj
                                                    adObj.increment(Configs.ADS_LIKES, 1);

                                                    // Add the user's objectID
                                                    if (adObj.getList(Configs.ADS_LIKED_BY) != null) {
                                                        List<String> likedByArr = adObj.getList(Configs.ADS_LIKED_BY);
                                                        likedByArr.add(currUser.getObjectId());
                                                        adObj.put(Configs.ADS_LIKED_BY, likedByArr);
                                                    } else {
                                                        List<String> likedByArr = new ArrayList<String>();
                                                        likedByArr.add(currUser.getObjectId());
                                                        adObj.put(Configs.ADS_LIKED_BY, likedByArr);
                                                    }
                                                    adObj.saveInBackground();

                                                    // Set likes number in the cell
                                                    int likesNr = (int) adObj.getNumber(Configs.ADS_LIKES);
                                                    likesTxt.setText(String.valueOf(likesNr));


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
                                                                Configs.simpleAlert(e.getMessage(), AdDetails.this);
                                                            }
                                                        }
                                                    });


                                                    // Save Activity
                                                    ParseObject actObj = new ParseObject(Configs.ACTIVITY_CLASS_NAME);
                                                    actObj.put(Configs.ACTIVITY_CURRENT_USER, sellerPointer);
                                                    actObj.put(Configs.ACTIVITY_OTHER_USER, currUser);
                                                    actObj.put(Configs.ACTIVITY_TEXT, pushMessage);
                                                    actObj.saveInBackground();


                                                    // error on saving like
                                                } else {
                                                    Configs.hidePD();
                                                    Configs.simpleAlert(e.getMessage(), AdDetails.this);
                                                }
                                            }
                                        });



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
                                                    List<String> likedByArr = adObj.getList(Configs.ADS_LIKED_BY);
                                                    likedByArr.remove(currUser.getObjectId());
                                                    adObj.put(Configs.ADS_LIKED_BY, likedByArr);
                                                    adObj.saveInBackground();

                                                    // Put "Like" back to the txt
                                                    likesTxt.setText("Like");

                                                }
                                            }
                                        });
                                    }


                                // error in query
                                } else {
                                    Configs.hidePD();
                                    Configs.simpleAlert(error.getMessage(), AdDetails.this);
                                }

                            }});// end query for Likes



                    // YOU'RE NOT LOGGED IN!
                    } else {
                        Configs.loginAlert("You must be logged in to like this Ad. Want to login now?", AdDetails.this);
                    }

                }});


        } catch (ParseException e) { e.printStackTrace(); }





        // MARK: - BACK BUTTON ------------------------------------
        Button backButt = findViewById(R.id.adBackButt);
        backButt.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              finish();
        }});


    }// end onCreate()





    // MARK: - QUERY IF YOU'VE LIKED THIS AD ------------------------------------------------------
    void queryIfYouLikedThisAd() {
        if (ParseUser.getCurrentUser().getUsername() != null) {

            ParseQuery<ParseObject> query = ParseQuery.getQuery(Configs.LIKES_CLASS_NAME);
            query.whereEqualTo(Configs.LIKES_CURR_USER, ParseUser.getCurrentUser());
            query.whereEqualTo(Configs.LIKES_AD_LIKED, adObj);
            query.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> objects, ParseException error) {
                    if (error == null) {
                        if (objects.size() != 0) {
                            likeButt.setBackgroundResource(R.drawable.liked_icon);
                            likesTxt.setText(String.valueOf(adObj.getNumber(Configs.ADS_LIKES)));
                        } else {
                            likeButt.setBackgroundResource(R.drawable.like_butt);
                            likesTxt.setText("Like");
                        }

                    // error
                    } else {
                        Configs.simpleAlert(error.getMessage(), AdDetails.this);
                    }
                }
            });
        }
    }





    // MARK: - SHOW AD DETAILS ---------------------------------------------------------------------
    void showAdDetails() {

        // Get Image 1
        final ImageView img1 = findViewById(R.id.adImg1);
        Configs.getParseImage(img1, adObj, Configs.ADS_IMAGE1);

        // Open image1 full screen
        img1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImageFullScreen("image1");
        }});


        // Get Image 2
        final ImageView img2 = findViewById(R.id.adImg2);
        ParseFile fileObject2 = adObj.getParseFile(Configs.ADS_IMAGE2);
        if (fileObject2 != null ) {
            fileObject2.getDataInBackground(new GetDataCallback() {
                public void done(byte[] data, ParseException error) {
                    if (error == null) {
                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                        if (bmp != null) {
                            img2.setImageBitmap(bmp);
        }}}});
        } else { img2.setImageBitmap(null); }

        // Open image2 full screen
        img2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImageFullScreen("image2");
        }});



        // Get Image 3
        final ImageView img3 = findViewById(R.id.adImg3);
        ParseFile fileObject3 = adObj.getParseFile(Configs.ADS_IMAGE3);
        if (fileObject3 != null ) {
            fileObject3.getDataInBackground(new GetDataCallback() {
                public void done(byte[] data, ParseException error) {
                    if (error == null) {
                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                        if (bmp != null) {
                            img3.setImageBitmap(bmp);
        }}}});
        } else { img3.setImageBitmap(null); }


        // Open image3 full screen
        img3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImageFullScreen("image3");
        }});





        // Get title
        TextView titleTxt = findViewById(R.id.adTitleTxt);
        titleTxt.setTypeface(Configs.titRegular);
        titleTxt.setText(adObj.getString(Configs.ADS_TITLE));

        // Get date
        TextView datetxt = findViewById(R.id.adDateTxt);
        datetxt.setTypeface(Configs.titRegular);
        datetxt.setText(Configs.timeAgoSinceDate(adObj.getCreatedAt()));

        // Get price
        TextView priceTxt = findViewById(R.id.adPriceTxt);
        priceTxt.setTypeface(Configs.titRegular);
        priceTxt.setText( "Price: " + adObj.getString(Configs.ADS_CURRENCY) + String.valueOf(adObj.getNumber(Configs.ADS_PRICE)));

        // Get condition
        TextView conditionTxt = findViewById(R.id.adConditionTxt);
        conditionTxt.setTypeface(Configs.titRegular);
        conditionTxt.setText("Condition: " + adObj.getString(Configs.ADS_CONDITION));

        // Get category
        TextView categoryTxt = findViewById(R.id.adCategoryTxt);
        categoryTxt.setTypeface(Configs.titRegular);
        categoryTxt.setText("Category: " + adObj.getString(Configs.ADS_CATEGORY));


        // Get Location (City, Country)
        ParseGeoPoint gp = new ParseGeoPoint(adObj.getParseGeoPoint(Configs.ADS_LOCATION));
        Location adLocation = new Location("dummyprovider");
        adLocation.setLatitude(gp.getLatitude());
        adLocation.setLongitude(gp.getLongitude());

        try {
            Geocoder geocoder = new Geocoder(AdDetails.this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(adLocation.getLatitude(), adLocation.getLongitude(), 1);
            if (Geocoder.isPresent()) {
                Address returnAddress = addresses.get(0);
                String city = returnAddress.getLocality();
                String country = returnAddress.getCountryName();

                if (city == null) { city = ""; }

                // Show City/Country
                TextView locationTxt = findViewById(R.id.adLocationTxt);
                locationTxt.setTypeface(Configs.titRegular);
                locationTxt.setText("Location: " + city + ", " + country);

            } else { Toast.makeText(getApplicationContext(), "Geocoder not present!", Toast.LENGTH_SHORT).show(); }
        } catch (IOException e) { Configs.simpleAlert(e.getMessage(), AdDetails.this); }



        // Get video
        Button watchVideoButt = findViewById(R.id.adWatchVideoButt);
        watchVideoButt.setTypeface(Configs.titSemibold);
        if (adObj.get(Configs.ADS_VIDEO) != null) {
            watchVideoButt.setText("Video: Watch video");
            watchVideoButt.setEnabled(true);
        } else {
            watchVideoButt.setText("Video: N/A");
            watchVideoButt.setEnabled(false);
        }

        // Show it in the WatchVideo screen
        watchVideoButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Pass objectID to the other Activity
                Intent i = new Intent(AdDetails.this, WatchVideo.class);
                Bundle extras = new Bundle();
                extras.putString("objectID", adObj.getObjectId());
                i.putExtras(extras);
                startActivity(i);
        }});


        // Get description
        TextView desctxt = findViewById(R.id.adDescriptionTxt);
        desctxt.setTypeface(Configs.titRegular);
        desctxt.setText(adObj.getString(Configs.ADS_DESCRIPTION));




        // SELLERS DETAILS -------------------------------------

        // Get userPointer
        adObj.getParseObject(Configs.ADS_SELLER_POINTER).fetchIfNeededInBackground(new GetCallback<ParseObject>() {
            public void done(final ParseObject sellerPointer, ParseException e) {

                // Get Avatar
                final ImageView avImg = findViewById(R.id.adAvatarImg);
                Configs.getParseImage(avImg, sellerPointer, Configs.USER_AVATAR);

                // TAP ON AVATAR -> SHOW USER PROFILE
                avImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Pass objectID to the other Activity
                        Intent i = new Intent(AdDetails.this, UserProfile.class);
                        Bundle extras = new Bundle();
                        extras.putString("objectID", sellerPointer.getObjectId());
                        i.putExtras(extras);
                        startActivity(i);
                }});




                // Get username
                TextView usernameTxt = findViewById(R.id.adUsernameTxt);
                usernameTxt.setTypeface(Configs.titRegular);
                usernameTxt.setText(sellerPointer.getString(Configs.USER_USERNAME));

                // Get joined
                TextView joinedTxt = findViewById(R.id.adJoinedTxt);
                joinedTxt.setTypeface(Configs.titRegular);
                joinedTxt.setText("Joined: " +Configs.timeAgoSinceDate(sellerPointer.getCreatedAt()));

                // Get verified
                TextView verifiedTxt = findViewById(R.id.adVerifiedTxt);
                verifiedTxt.setTypeface(Configs.titRegular);
                if (sellerPointer.get(Configs.USER_EMAIL_VERIFIED) != null) {
                    if (sellerPointer.getBoolean(Configs.USER_EMAIL_VERIFIED)) {
                        verifiedTxt.setText("Verified: Yes");
                    } else { verifiedTxt.setText("Verified: No"); }
                } else { verifiedTxt.setText("Verified: No"); }



                // MARK: - COMMENTS BUTTON ------------------------------------
                Button commButt = findViewById(R.id.adCommentsButt);
                commButt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (ParseUser.getCurrentUser().getUsername() != null) {
                            Intent i = new Intent(AdDetails.this, Comments.class);
                            Bundle extras = new Bundle();
                            extras.putString("objectID", adObj.getObjectId());
                            i.putExtras(extras);
                            startActivity(i);

                        } else {
                            Configs.loginAlert("You must be logged in to comment. Want to Login now?", AdDetails.this);
                        }
                }});




                // MARK: - SEND FEEDBACK BUTTON ------------------------------------
                Button feedbackButt = findViewById(R.id.adFeedbackButt);
                feedbackButt.setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View view) {
                      if (ParseUser.getCurrentUser().getUsername() != null) {

                          Intent i = new Intent(AdDetails.this, SendFeedback.class);
                          Bundle extras = new Bundle();
                          extras.putString("objectID", adObj.getObjectId());
                          extras.putString("sellerID", sellerPointer.getObjectId());
                          i.putExtras(extras);
                          startActivity(i);

                      } else {
                          Configs.loginAlert("You must be logged in to send a feedback. Want to login now?", AdDetails.this);
                      }
                }});


            }});// end sellerPointer

    }






    // OPEN TAPPED IMAGE INTO FULL SCREEN ACTIVITY
    void openImageFullScreen(String imageName) {
        Intent i = new Intent(AdDetails.this, FullScreenPreview.class);
        Bundle extras = new Bundle();
        extras.putString("imageName", imageName);
        extras.putString("objectID", adObj.getObjectId());
        i.putExtras(extras);
        startActivity(i);
    }


}//@end
