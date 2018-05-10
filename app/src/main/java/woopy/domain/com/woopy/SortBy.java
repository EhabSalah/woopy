package woopy.domain.com.woopy;

/*-------------------------------

    - woopy -

    Created by cubycode @2017
    All Rights reserved

-------------------------------*/

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
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

import java.util.ArrayList;
import java.util.List;

public class SortBy extends AppCompatActivity {

    /* Views */
    ListView sortByListView;




    /* Variables */
    List<String> sortByArr = new ArrayList<String>();
    String selectedSort = "";





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sort_by);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Hide ActionBar
        getSupportActionBar().hide();


        // Init sortBy Array
        sortByArr.add("Recent");
        sortByArr.add("Lowest Price");
        sortByArr.add("Highest Price");
        sortByArr.add("New");
        sortByArr.add("Used");
        sortByArr.add("Most Liked");



        // Init ListView and set its adapter
        sortByListView = findViewById(R.id.sbListView);
        sortByListView.setAdapter(new ListAdapter(SortBy.this, sortByArr));
        sortByListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id){
                selectedSort = sortByArr.get(position);
                Log.i("log-", "SELECTED SORT BY: " + selectedSort);
        }});




        // MARK: - DONE BUTTON ------------------------------------
        Button doneButt = findViewById(R.id.sbDoneButt);
        doneButt.setTypeface(Configs.titSemibold);
        doneButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!selectedSort.matches("")) {
                    Configs.sortBy = selectedSort;
                    finish();
                } else {
                    Configs.simpleAlert(getResources().getString(R.string.alert_Select_an_option_or_tap_Cancel), SortBy.this);
        }}});



        // MARK: - CANCEL BUTTON ------------------------------------
        Button cancButt = findViewById(R.id.sbCancelButt);
        cancButt.setTypeface(Configs.titSemibold);
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
            nameTxt.setText(sortByArr.get(position));

            return cell;
        }

        @Override public int getCount() { return sortByArr.size(); }
        @Override public Object getItem(int position) { return sortByArr.get(position); }
        @Override public long getItemId(int position) { return position; }
    }



}//@end
