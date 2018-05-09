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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ReportAdOrUser extends AppCompatActivity {

    /* Views */
    TextView titleTxt;
    ListView reportListView;



    /* Variables */
    ParseObject adObj;
    ParseUser userObj;
    String reportType = "";
    List<String>reportArray = new ArrayList<String>();






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_ad_or_user);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Hide ActionBar
        getSupportActionBar().hide();



        // Init views
        titleTxt = findViewById(R.id.repTitleTxt);
        titleTxt.setTypeface(Configs.titSemibold);



        // Get extras from previous .java
        Bundle extras = getIntent().getExtras();
        assert extras != null;
        String adObjectID = extras.getString("adObjectID");
        String userObjectID = extras.getString("userObjectID");
        reportType = extras.getString("reportType");

        // Set title Txt
        titleTxt.setText("Report " + reportType);


        // REPORT AN AD ------------------------------------------
        if (reportType.matches("User")) {
            userObj = (ParseUser) ParseUser.createWithoutData(Configs.USER_CLASS_NAME, userObjectID);
            try { userObj.fetchIfNeeded().getParseUser(Configs.USER_CLASS_NAME);

                reportArray = new ArrayList<String>(Arrays.asList(Configs.reportUserOptions));

            } catch (ParseException e) { e.printStackTrace(); }


        // REPORT A USER ------------------------------------------
        } else {
            adObj = ParseObject.createWithoutData(Configs.ADS_CLASS_NAME, adObjectID);
            try { adObj.fetchIfNeeded().getParseObject(Configs.ADS_CLASS_NAME);

                reportArray = new ArrayList<String>(Arrays.asList(Configs.reportAdOptions));

            } catch (ParseException e) { e.printStackTrace(); }
        }



        // Init ListView and set its adapter
        reportListView = findViewById(R.id.repListView);
        reportListView.setAdapter(new ListAdapter(ReportAdOrUser.this, reportArray));
        reportListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, final int position, long id) {

                AlertDialog.Builder alert = new AlertDialog.Builder(ReportAdOrUser.this);
                alert.setMessage(R.string.alert_Are_you_sure_you_want_to_report_this + reportType + R.string.alert_for_the_following_reason+"\n" + reportArray.get(position) + "?")
                        .setTitle(R.string.app_name)
                        .setPositiveButton(R.string.alert_Yes_Im_sure, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                Configs.showPD(getResources().getString(R.string.alert_please_wait), ReportAdOrUser.this);

                                // REPORT THE AD --------------------------------------------------
                                if (reportType.matches("Ad")) {

                                    adObj.put(Configs.ADS_IS_REPORTED, true);
                                    adObj.put(Configs.ADS_REPORT_MESSAGE, reportArray.get(position));

                                    // Saving block
                                    adObj.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if (e == null) {
                                                Configs.hidePD();

                                                AlertDialog.Builder alert = new AlertDialog.Builder(ReportAdOrUser.this);
                                                alert.setMessage(R.string.alert_Thanks_for_reporting_this_Ad_We_will_review_it_within_24h)
                                                    .setTitle(R.string.app_name)
                                                    .setPositiveButton(R.string.btn_OK, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            // Go back to Home
                                                            startActivity(new Intent(ReportAdOrUser.this, Home.class));
                                                    }})
                                                    .setIcon(R.drawable.logo);
                                                alert.create().show();

                                            } else {
                                                Configs.hidePD();
                                                Configs.simpleAlert(e.getMessage(), ReportAdOrUser.this);
                                    }}});




                                // REPORT A USER --------------------------------------------------
                                } else {
                                    HashMap<String, Object> params = new HashMap<String, Object>();
                                    params.put("userId", userObj.getObjectId());
                                    params.put("reportMessage", reportArray.get(position));

                                    ParseCloud.callFunctionInBackground("reportUser", params, new FunctionCallback<ParseUser>() {
                                        public void done(ParseUser user, ParseException error) {
                                            if (error == null) {
                                                Configs.hidePD();

                                                AlertDialog.Builder alert = new AlertDialog.Builder(ReportAdOrUser.this);
                                                alert.setMessage(R.string.alert_Thanks_for_reporting_this_User_we_ll_check_it_out_within_24hours)
                                                    .setTitle(R.string.app_name)
                                                    .setPositiveButton(R.string.btn_OK, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            // Go back to Home
                                                            startActivity(new Intent(ReportAdOrUser.this, Home.class));
                                                        }})
                                                    .setIcon(R.drawable.logo);
                                                alert.create().show();


                                                // Query all Ads posted by this User...
                                            ParseQuery<ParseObject> query = ParseQuery.getQuery(Configs.ADS_CLASS_NAME);
                                                query.whereEqualTo(Configs.ADS_SELLER_POINTER, userObj);
                                                query.findInBackground(new FindCallback<ParseObject>() {
                                                    public void done(List<ParseObject> objects, ParseException e) {
                                                        if (e == null) {
                                                            // ...and report Ads them one by one
                                                            for (int i = 0; i < objects.size(); i++) {
                                                                ParseObject adObj = new ParseObject(Configs.ADS_CLASS_NAME);
                                                                adObj = objects.get(i);
                                                                adObj.put(Configs.ADS_IS_REPORTED, true);
                                                                adObj.put(Configs.ADS_REPORT_MESSAGE, "**Automatically reported after reporting the its Seller**");
                                                                adObj.saveInBackground();
                                                }}}});



                                            // Error in Cloud Code
                                            } else {
                                                Configs.hidePD();
                                                Configs.simpleAlert(error.getMessage(), ReportAdOrUser.this);
                                    }}
                                    });

                                }

                        }})

                        .setNegativeButton(R.string.alert_No, null)
                        .setIcon(R.drawable.logo);
                    alert.create().show();

        }});






        // MARK: - CANCEL BUTTON ------------------------------------
        Button cancButt = findViewById(R.id.repCancelButt);
        cancButt.setTypeface(Configs.titRegular);
        cancButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
        }});



    }// end onCreate()






    // CUSTOM LIST ADAPTER
    public class ListAdapter extends BaseAdapter {
        private Context context;
        public ListAdapter(Context context, List<String> objects) {
            super();
            this.context = context;
        }

        // CONFIGURE CELL
        @Override
        public View getView(int position, View cell, ViewGroup parent) {
            if (cell == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                assert inflater != null;
                cell = inflater.inflate(R.layout.cell_category_sortby, null);
            }

            TextView nameTxt = cell.findViewById(R.id.cCatSortTxt);
            nameTxt.setTypeface(Configs.titRegular);
            nameTxt.setText(reportArray.get(position));

        return cell;
        }

        @Override public int getCount() { return reportArray.size(); }
        @Override public Object getItem(int position) { return reportArray.get(position); }
        @Override public long getItemId(int position) { return position; }
    }



}//@end
