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
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MyLikes extends AppCompatActivity {

    /* Views */
    RelativeLayout noLikesLayout;


    /* Variables */
    List<ParseObject> likesArray;
    MarshMallowPermission mmp = new MarshMallowPermission(this);




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_likes);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Hide ActionBar
        getSupportActionBar().hide();



        // Init views
        noLikesLayout = findViewById(R.id.mlNoLikesLayout);
        noLikesLayout.setVisibility(View.INVISIBLE);


        // Init TabBar buttons
        Button tab_one = findViewById(R.id.tab_one);
        Button tab_two = findViewById(R.id.tab_two);
        Button tab_three = findViewById(R.id.tab_three);
        Button tab_four = findViewById(R.id.tab_four);
        Button tab_five = findViewById(R.id.tab_five);

        tab_one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MyLikes.this, Home.class));
            }});

        tab_three.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MyLikes.this, SellEditItem.class));
            }});

        tab_four.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MyLikes.this, ActivityScreen.class));
            }});

        tab_five.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MyLikes.this, Account.class));
            }});




        // Call query
        queryLikes();


        // Init AdMob banner
        AdView mAdView = findViewById(R.id.admobBanner);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


    }// end onCreate()




    // MARK: - QUERY ADS ------------------------------------------------------------------
    void queryLikes() {
        noLikesLayout.setVisibility(View.INVISIBLE);

        Configs.showPD(getResources().getString(R.string.alert_please_wait), MyLikes.this);

        // Launch query
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Configs.LIKES_CLASS_NAME);
        query.whereEqualTo(Configs.LIKES_CURR_USER, ParseUser.getCurrentUser());
        query.orderByDescending(Configs.LIKES_CREATED_AT);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException error) {
                if (error == null) {
                    likesArray = objects;
                    Configs.hidePD();

                    // Show/hide noLikesView
                    if (likesArray.size() == 0) { noLikesLayout.setVisibility(View.VISIBLE);
                    } else { noLikesLayout.setVisibility(View.INVISIBLE); }


                    // CUSTOM GRID ADAPTER
                    class GridAdapter extends BaseAdapter {
                        private Context context;
                        public GridAdapter(Context context, List<ParseObject> objects) {
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
                            final ParseObject likeObj = likesArray.get(position);

                            // Init cell's views
                            final ImageView aImage = finalCell.findViewById(R.id.cadImage);
                            final TextView titleTxt = finalCell.findViewById(R.id.cadAdTitleTxt);
                            final TextView priceTxt = finalCell.findViewById(R.id.cadPriceTxt);
                            final TextView likesTxt = finalCell.findViewById(R.id.cadLikesTxt);
                            final TextView commTxt = finalCell.findViewById(R.id.cadCommentsTxt);
                            final TextView dateTxt = finalCell.findViewById(R.id.cadDateTxt);
                            final ImageView avImg = finalCell.findViewById(R.id.cadAvatarImg);
                            final TextView uTxt = finalCell.findViewById(R.id.cadUsernametxt);
                            final Button likeButt =  finalCell.findViewById(R.id.cadLikeButt);
                            final Button commButt = finalCell.findViewById(R.id.cadCommentsButt);
                            final Button optionsButt = finalCell.findViewById(R.id.cadOptionsButt);




                            // Get adObj
                            likeObj.getParseObject(Configs.LIKES_AD_LIKED).fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                                public void done(final ParseObject adObj, ParseException e) {


                                    // AD HAS NOT BEEN REPORTED, SHOW IT --------------------------
                                    if (!adObj.getBoolean(Configs.ADS_IS_REPORTED)) {


                                        // Get userPointer
                                        adObj.getParseObject(Configs.ADS_SELLER_POINTER).fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                                            public void done(final ParseObject userPointer, ParseException e) {


                                                // Get Image 1
                                                Configs.getParseImage(aImage, adObj, Configs.ADS_IMAGE1);

                                                // Show Ad details on click on the image
                                                aImage.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        Intent i = new Intent(MyLikes.this, AdDetails.class);
                                                        Bundle extras = new Bundle();
                                                        extras.putString("objectID", adObj.getObjectId());
                                                        i.putExtras(extras);
                                                        startActivity(i);
                                                    }
                                                });


                                                // Get Title
                                                titleTxt.setTypeface(Configs.titRegular);
                                                titleTxt.setText(adObj.getString(Configs.ADS_TITLE));

                                                // Get Price
                                                priceTxt.setTypeface(Configs.titRegular);
                                                priceTxt.setText(adObj.getString(Configs.ADS_CURRENCY) + String.valueOf(adObj.getNumber(Configs.ADS_PRICE)));

                                                // Get likes
                                                likesTxt.setTypeface(Configs.titRegular);
                                                if (adObj.getNumber(Configs.ADS_LIKES) != null) {
                                                    likesTxt.setText(Configs.roundThousandsIntoK(adObj.getNumber(Configs.ADS_LIKES)));
                                                } else {
                                                    likesTxt.setText("0");
                                                }

                                                // Get comments
                                                commTxt.setTypeface(Configs.titRegular);
                                                if (adObj.getNumber(Configs.ADS_COMMENTS) != null) {
                                                    commTxt.setText(Configs.roundThousandsIntoK(adObj.getNumber(Configs.ADS_COMMENTS)));
                                                } else {
                                                    commTxt.setText("0");
                                                }

                                                // Get Date
                                                dateTxt.setTypeface(Configs.titRegular);
                                                dateTxt.setText(Configs.timeAgoSinceDate(adObj.getCreatedAt()));


                                                // Get Users' Avar
                                                Configs.getParseImage(avImg, userPointer, Configs.USER_AVATAR);

                                                // Get User's username
                                                uTxt.setTypeface(Configs.titRegular);
                                                uTxt.setText(userPointer.getString(Configs.USER_USERNAME));



                                                // Clip the cell layout to outline
                                                RelativeLayout cellLayout = finalCell.findViewById(R.id.cadCellLayout);
                                                cellLayout.setClipToOutline(true);


                                                // MARK: - AVATAR BUTTON ------------------------------------------------------
                                                avImg.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        // Pass objectID to the other Activity
                                                        Intent i = new Intent(MyLikes.this, UserProfile.class);
                                                        Bundle extras = new Bundle();
                                                        extras.putString("objectID", userPointer.getObjectId());
                                                        i.putExtras(extras);
                                                        startActivity(i);
                                                    }
                                                });




                                                // MARK: - COMMENTS BUTTON ------------------------------------------------------
                                                commButt.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        // Pass objectID to the other Activity
                                                        Intent i = new Intent(MyLikes.this, Comments.class);
                                                        Bundle extras = new Bundle();
                                                        extras.putString("objectID", adObj.getObjectId());
                                                        i.putExtras(extras);
                                                        startActivity(i);
                                                    }
                                                });




                                                // MARK: - AD OPTIONS BUTTON ------------------------------------
                                                optionsButt.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        AlertDialog.Builder alert = new AlertDialog.Builder(MyLikes.this);
                                                        alert.setMessage(R.string.alert_select_option)
                                                                .setTitle(R.string.app_name)

                                                                // REPORT AD
                                                                .setPositiveButton(R.string.alert_report_ad, new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                                        Intent in = new Intent(MyLikes.this, ReportAdOrUser.class);
                                                                        Bundle extras = new Bundle();
                                                                        extras.putString("adObjectID", adObj.getObjectId());
                                                                        extras.putString("reportType", "Ad");
                                                                        in.putExtras(extras);
                                                                        startActivity(in);
                                                                    }
                                                                })


                                                                // SHARE AD
                                                                .setNegativeButton(R.string.alert_share, new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                                        if (!mmp.checkPermissionForWriteExternalStorage()) {
                                                                            mmp.requestPermissionForWriteExternalStorage();
                                                                        } else {
                                                                            // Get Image
                                                                            ParseFile fileObject = adObj.getParseFile(Configs.ADS_IMAGE1);
                                                                            if (fileObject != null) {
                                                                                fileObject.getDataInBackground(new GetDataCallback() {
                                                                                    public void done(byte[] data, ParseException error) {
                                                                                        if (error == null) {
                                                                                            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                                                                                            if (bmp != null) {
                                                                                                Uri uri = Configs.getImageUri(MyLikes.this, bmp);
                                                                                                Intent intent = new Intent(Intent.ACTION_SEND);
                                                                                                intent.setType("image/jpeg");
                                                                                                intent.putExtra(Intent.EXTRA_STREAM, uri);
                                                                                                intent.putExtra(Intent.EXTRA_TEXT, "Check this out: " + adObj.getString(Configs.ADS_TITLE) + " on #woopy");
                                                                                                startActivity(Intent.createChooser(intent, "Share on..."));
                                                                            }}}});}
                                                                        }
                                                                }})


                                                                // Cancel
                                                                .setNeutralButton(R.string.alert_cancel, null)
                                                                .setIcon(R.drawable.logo);
                                                        alert.create().show();
                                                    }
                                                });


                                                // MARK: - LIKE AD BUTTON ------------------------------------
                                                likeButt.setBackgroundResource(R.drawable.liked_icon);
                                                likeButt.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        Configs.showPD(getResources().getString(R.string.alert_Removing_liked_ad), MyLikes.this);

                                                        final ParseUser currUser = ParseUser.getCurrentUser();

                                                        // UNLIKE THIS AD :(
                                                        likeObj.deleteInBackground(new DeleteCallback() {
                                                            @Override
                                                            public void done(ParseException e) {
                                                                Configs.hidePD();

                                                                // Decrement likes for the adObj
                                                                adObj.increment(Configs.ADS_LIKES, -1);

                                                                // Remove the user's objectID
                                                                List<String>likedByArr = adObj.getList(Configs.ADS_LIKED_BY);
                                                                likedByArr.remove(currUser.getObjectId());
                                                                adObj.put(Configs.ADS_LIKED_BY, likedByArr);
                                                                adObj.saveInBackground();


                                                                // Recall query
                                                                queryLikes();
                                                        }});

                                                    }});


                                            }}); // end userPointer


                                        // AD HAS BEEN REPORTED! ----------------------------------------
                                    } else {
                                        aImage.setBackgroundResource(R.drawable.report_image);
                                        titleTxt.setText("N/A");
                                        priceTxt.setText("N/A");
                                        likesTxt.setText("N/A");
                                        commTxt.setText("N/A");
                                        dateTxt.setText("N/A");
                                        avImg.setBackgroundResource(R.drawable.logo);
                                        uTxt.setText("N/A");
                                        // Disable buttons
                                        commButt.setEnabled(false);
                                        optionsButt.setEnabled(false);
                                        avImg.setEnabled(false);
                                    }


                                }});// end adObj



                        return cell;
                        }

                        @Override
                        public int getCount() { return likesArray.size(); }
                        @Override
                        public Object getItem(int position) { return likesArray.get(position); }
                        @Override
                        public long getItemId(int position) { return position; }
                    }


                    // Init GridView and set its adapter
                    GridView aGrid = findViewById(R.id.mlAdsGridView);
                    aGrid.setAdapter(new GridAdapter(MyLikes.this, likesArray));

                    // Set number of Columns accordingly to the device used
                    float scalefactor = getResources().getDisplayMetrics().density * 150; // 150 is the cell's width
                    int number = getWindowManager().getDefaultDisplay().getWidth();
                    int columns = (int) ((float) number / (float) scalefactor);
                    aGrid.setNumColumns(columns);


                    // error in query
                } else {
                    Configs.hidePD();
                    Configs.simpleAlert(error.getMessage(), MyLikes.this);
                }}});

    }







}// @end
