package woopy.domain.com.woopy;

/*-------------------------------

    - woopy -

    Created by cubycode @2017
    All Rights reserved
    mahmoud mohamed

-------------------------------*/

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

public class Account extends AppCompatActivity {

    /* Views */
    Button likeButt;
    TextView likesTxt;



    /* Variables */
    List<ParseObject>myAdsArray;



    // ON START ----------------------------------------------------------------------
    @Override
    protected void onStart() {
        super.onStart();
        // Call query
        getUserDetails();

    }


    // ON CREATE ----------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Hide ActionBar
        getSupportActionBar().hide();



        // Init TabBar buttons
        Button tab_one = findViewById(R.id.tab_one);
        Button tab_two = findViewById(R.id.tab_two);
        Button tab_three = findViewById(R.id.tab_three);
        Button tab_four = findViewById(R.id.tab_four);
        Button tab_five = findViewById(R.id.tab_five);

        tab_one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Account.this, Home.class));
            }});

        tab_two.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Account.this, MyLikes.class));
            }});

        tab_three.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Account.this, SellEditItem.class));
            }});

        tab_four.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Account.this, ActivityScreen.class));
            }});





        // MARK: - FEEDBACKS BUTTON ------------------------------------
        Button feedButt = findViewById(R.id.accFeedbacksButt);
        feedButt.setTypeface(Configs.titSemibold);
        feedButt.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              Intent i = new Intent(Account.this, Feedbacks.class);
              Bundle extras = new Bundle();
              extras.putString("userObjectID", ParseUser.getCurrentUser().getObjectId());
              i.putExtras(extras);
              startActivity(i);
         }});



        // MARK: - EDIT PROFILE BUTTON ------------------------------------
        Button epButt = findViewById(R.id.accEditProfileButt);
        epButt.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              startActivity(new Intent(Account.this, EditProfile.class));
        }});



        // MARK: - CHATS BUTTON ------------------------------------
        Button cButt = findViewById(R.id.accChatButt);
        cButt.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              startActivity(new Intent(Account.this, Chats.class));
        }});



        // MARK: - LOGOUT BUTTON ------------------------------------
        Button logoutButt = findViewById(R.id.accLogoutButt);
        logoutButt.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              AlertDialog.Builder alert = new AlertDialog.Builder(Account.this);
              alert.setMessage(R.string.alert_logout)
                  .setTitle(R.string.app_name)
                  .setPositiveButton(R.string.alert_confirm_logout, new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialogInterface, int i) {
                          Configs.showPD(getResources().getString(R.string.alert_loging_out), Account.this);

                          ParseUser.logOutInBackground(new LogOutCallback() {
                              @Override
                              public void done(ParseException e) {
                                  Configs.hidePD();
                                  // Go back to Home
                                  startActivity(new Intent(Account.this, Home.class));
                          }});
                  }})

                  .setNegativeButton(R.string.alert_cancel, null)
                  .setIcon(R.drawable.logo);
              alert.create().show();
         }});



    }// end onCreate()






    // MARK: - GET USER'S DETAILS ----------------------------------------------------------------
    void getUserDetails() {
        ParseUser currUser = ParseUser.getCurrentUser();

        // Get username
        TextView usernTxt = findViewById(R.id.accUsernameTxt);
        usernTxt.setTypeface(Configs.titSemibold);
        usernTxt.setText("@" + currUser.getString(Configs.USER_USERNAME));

        // Get fullname
        TextView fnTxt = findViewById(R.id.accFullNameTxt);
        fnTxt.setTypeface(Configs.titSemibold);
        fnTxt.setText("@" + currUser.getString(Configs.USER_FULLNAME));

        // Get joined since
        TextView joinedTxt = findViewById(R.id.accJoinedTxt);
        joinedTxt.setTypeface(Configs.titRegular);
        joinedTxt.setText(R.string.txt_joined_since + Configs.timeAgoSinceDate(currUser.getCreatedAt()));


        // Get verified
        TextView verifiedtxt = findViewById(R.id.accVerifiedTxt);
        verifiedtxt.setTypeface(Configs.titRegular);
        if (currUser.get(Configs.USER_EMAIL_VERIFIED) != null) {
            if (currUser.getBoolean(Configs.USER_EMAIL_VERIFIED)) {
                verifiedtxt.setText(R.string.alert_verified_yes);
            } else { verifiedtxt.setText(R.string.alert_verified_no); }
        } else { verifiedtxt.setText(R.string.alert_verified_no); }

        // Get avatar
        final ImageView avImg = findViewById(R.id.accAvatarImg);
        Configs.getParseImage(avImg, currUser, Configs.USER_AVATAR);

        // Call query
        queryMyAds();
    }






    // MARK: - QUERY MY ADS ------------------------------------------------------------------
    void queryMyAds() {
        Configs.showPD(getResources().getString(R.string.alert_please_wait), Account.this);

        ParseQuery<ParseObject> query = ParseQuery.getQuery(Configs.ADS_CLASS_NAME);
        query.whereEqualTo(Configs.ADS_SELLER_POINTER, ParseUser.getCurrentUser());
        query.orderByDescending(Configs.ADS_CREATED_AT);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException error) {
                if (error == null) {
                    myAdsArray = objects;
                    Configs.hidePD();


                    // CUSTOM LIST ADAPTER
                    class ListAdapter extends BaseAdapter {
                        private Context context;
                        public ListAdapter(Context context, List<ParseObject> objects) {
                            super();
                            this.context = context;
                        }


                        // CONFIGURE CELL
                        @Override
                        public View getView(int position, View cell, ViewGroup parent) {
                            if (cell == null) {
                                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                assert inflater != null;
                                cell = inflater.inflate(R.layout.cell_myads, null);
                            }

                            // Get Parse object
                            ParseObject adObj = myAdsArray.get(position);

                            // Get ad title
                            TextView titletxt = cell.findViewById(R.id.cmaAdTitleTxt);
                            titletxt.setTypeface(Configs.titSemibold);
                            titletxt.setText(adObj.getString(Configs.ADS_TITLE));

                            // Get ad price
                            TextView priceTxt = cell.findViewById(R.id.cmaPricetxt);
                            priceTxt.setTypeface(Configs.titRegular);
                            priceTxt.setText(adObj.getString(Configs.ADS_CURRENCY) + String.valueOf(adObj.getNumber(Configs.ADS_PRICE)) );

                            // Get date
                            TextView dateTxt = cell.findViewById(R.id.cmaDatetxt);
                            dateTxt.setTypeface(Configs.titRegular);
                            dateTxt.setText(Configs.timeAgoSinceDate(adObj.getCreatedAt()));

                            // Get Image
                            final ImageView anImage = cell.findViewById(R.id.cmaImage);
                            Configs.getParseImage(anImage, adObj, Configs.ADS_IMAGE1);


                        return cell;
                        }

                        @Override public int getCount() { return myAdsArray.size(); }
                        @Override public Object getItem(int position) { return myAdsArray.get(position); }
                        @Override public long getItemId(int position) { return position; }
                    }

                    // Init ListView and set its adapter
                    ListView aList = findViewById(R.id.myAdsListView);
                    aList.setAdapter(new ListAdapter(Account.this, myAdsArray));
                    aList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                            ParseObject adObj = myAdsArray.get(position);
                            Intent i = new Intent(Account.this, SellEditItem.class);
                            Bundle extras = new Bundle();
                            extras.putString("objectID", adObj.getObjectId());
                            i.putExtras(extras);
                            startActivity(i);
                    }});


                // Error in query
                } else {
                    Configs.hidePD();
                    Configs.simpleAlert(error.getMessage(), Account.this);
        }}});

    }






}//@end
