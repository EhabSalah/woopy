package woopy.domain.com.woopy;

/*-------------------------------

    - woopy -

    Created by cubycode @2017
    All Rights reserved

-------------------------------*/

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

public class ActivityScreen extends AppCompatActivity {



    /* Variables */
    List<ParseObject>activityArray;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);
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
                startActivity(new Intent(ActivityScreen.this, Home.class));
            }});

        tab_two.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActivityScreen.this, MyLikes.class));
            }});

        tab_three.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActivityScreen.this, SellEditItem.class));
            }});

        tab_five.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActivityScreen.this, Account.class));
            }});



        // Call query
        queryActivity();


    }// end onCreate()



    // MARK: - QUERY ACTIVITY ---------------------------------------------------------------
    void queryActivity() {
        Configs.showPD(getResources().getString(R.string.alert_please_wait), ActivityScreen.this);

        ParseQuery<ParseObject> query = ParseQuery.getQuery(Configs.ACTIVITY_CLASS_NAME);
        query.whereEqualTo(Configs.ACTIVITY_CURRENT_USER, ParseUser.getCurrentUser());
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException error) {
                if (error == null) {
                    activityArray = objects;
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
                                cell = inflater.inflate(R.layout.cell_activity, null);
                            }
                            final View finalCell = cell;

                            // Get Parse object
                            final ParseObject actObj = activityArray.get(position);

                            // Get userPointer
                            actObj.getParseObject(Configs.ACTIVITY_OTHER_USER).fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                                public void done(ParseObject userPointer, ParseException e) {

                                    TextView actTxt = finalCell.findViewById(R.id.actTextTxt);
                                    actTxt.setTypeface(Configs.titRegular);
                                    actTxt.setText(actObj.getString(Configs.ACTIVITY_TEXT));

                                    // Get date
                                    TextView dateTxt = finalCell.findViewById(R.id.actDateTxt);
                                    dateTxt.setTypeface(Configs.titRegular);
                                    dateTxt.setText(Configs.timeAgoSinceDate(actObj.getCreatedAt()));

                                    // Get Avatar
                                    final ImageView avImage = finalCell.findViewById(R.id.actAvatarImg);
                                    Configs.getParseImage(avImage, userPointer, Configs.USER_AVATAR);


                                }});// end userPointer


                            return cell;
                        }

                        @Override public int getCount() { return activityArray.size(); }
                        @Override public Object getItem(int position) { return activityArray.get(position); }
                        @Override public long getItemId(int position) { return position; }
                    }

                    // Init ListView and set its adapter
                    final ListView aList = findViewById(R.id.actListView);
                    aList.setAdapter(new ListAdapter(ActivityScreen.this, activityArray));



                    // MARK: - TAP A CELL TO SEE USER'S PROFILE -----------------------------------
                    aList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                            // Get Parse object
                            ParseObject actObj = activityArray.get(position);

                            // Get userPointer
                            actObj.getParseObject(Configs.ACTIVITY_OTHER_USER).fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                                public void done(ParseObject userPointer, ParseException e) {
                                    // Pass objectID to the other Activity
                                    Intent i = new Intent(ActivityScreen.this, UserProfile.class);
                                    Bundle extras = new Bundle();
                                    extras.putString("objectID", userPointer.getObjectId());
                                    i.putExtras(extras);
                                    startActivity(i);

                            }});// end userPointer

                    }});




                    // MARK: - LONG PRESS TO DELETE AN ACTIVITY ------------------------------------
                    aList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                        @Override
                        public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {

                            ParseObject actObj = activityArray.get(i);
                            actObj.deleteInBackground(new DeleteCallback() {
                                @Override
                                public void done(ParseException e) {
                                    activityArray.remove(i);
                                    aList.invalidateViews();
                                    aList.refreshDrawableState();
                                    Toast.makeText(ActivityScreen.this, getResources().getString(R.string.toast_activity_removed), Toast.LENGTH_SHORT).show();
                            }});

                            return false;
                    }});

                // Error in query
                } else {
                    Configs.hidePD();
                    Configs.simpleAlert(error.getMessage(), ActivityScreen.this);
        }}});


    }


}//@end
