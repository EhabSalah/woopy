package woopy.domain.com.woopy;

/*-------------------------------

    - woopy -

    Created by cubycode @2017
    All Rights reserved

-------------------------------*/

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Debug;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.share.internal.VideoUploader;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class SellEditItem extends AppCompatActivity implements LocationListener {

    /* Views */
    Button categoriesButt, newButt, usedButt, deleteAdButt;
    TextView titleTxt;
    EditText adTitleTxt, priceTxt, descriptionTxt;
    ImageView image1, image2, image3, videoThumb;


    /* Variables */
    ParseObject adObj;
    int pictureTag = 0;
    Location currentLocation;
    LocationManager locationManager;
    MarshMallowPermission mmp = new MarshMallowPermission(this);
    String categoryName = "";
    String condition = "";
    List<ParseObject>categoriesArray;
    RelativeLayout categoriesLayout;





    // ON CREATE() ------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sell_edit_item);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Hide ActionBar
        getSupportActionBar().hide();



        // Init views
        categoriesButt = findViewById(R.id.sellCategoriesButt);
        categoriesButt.setTypeface(Configs.titSemibold);
        newButt = findViewById(R.id.sellNewButt);
        newButt.setTypeface(Configs.titSemibold);
        usedButt = findViewById(R.id.sellUsedButt);
        usedButt.setTypeface(Configs.titSemibold);
        deleteAdButt = findViewById(R.id.sellDeleteAdButt);
        deleteAdButt.setTypeface(Configs.titSemibold);

        titleTxt = findViewById(R.id.sellTitleTxt);
        titleTxt.setTypeface(Configs.titSemibold);
        priceTxt = findViewById(R.id.sellPriceTxt);
        priceTxt.setTypeface(Configs.titRegular);
        descriptionTxt = findViewById(R.id.sellDescriptionTxt);
        descriptionTxt.setTypeface(Configs.titRegular);
        adTitleTxt = findViewById(R.id.sellAdTitleTxt);
        adTitleTxt.setTypeface(Configs.titRegular);

        image1 = findViewById(R.id.sellAdImg1);
        image2 = findViewById(R.id.sellAdImg2);
        image3 = findViewById(R.id.sellAdImg3);
        videoThumb = findViewById(R.id.sellAdVideoImg);

        categoriesLayout = findViewById(R.id.sellCategoriesLayout);
        hideCategoriesLayout();


        // Check if Location service is permitted
        if (!mmp.checkPermissionForLocation()) {
            mmp.requestPermissionForLocation();
        } else { getCurrentLocation(); }


        // Call query
        queryCategories();




        // Get objectID for adObj
        Bundle extras = getIntent().getExtras();
        String objectID = extras.getString("objectID");
        Log.i("log-", "OBJECT ID: " + objectID);


        // YOU'RE EDITING AN ITEM ------------------
        if (objectID != null) {

            adObj = ParseObject.createWithoutData(Configs.ADS_CLASS_NAME, objectID);
            try { adObj.fetchIfNeeded().getParseObject(Configs.ADS_CLASS_NAME);

                titleTxt.setText(R.string.alert_Edit_item);
                deleteAdButt.setVisibility(View.VISIBLE);

                // Call query
                showAdDetails();

            } catch (ParseException e) { e.printStackTrace(); }



        // YOU'RE SELLING A NEW ITEM ---------------
        } else {
            adObj = new ParseObject(Configs.ADS_CLASS_NAME);
            titleTxt.setText(R.string.alert_Sell_an_item);
            deleteAdButt.setVisibility(View.INVISIBLE);

            // Set default variables
            condition = "New";
        }



        // MARK: - BACK BUTTON ------------------------------------
        Button backButt = findViewById(R.id.sellBackButt);
        backButt.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              finish();
         }});




        // MARK: - CHOOSE CATEGORY BUTTON ------------------------------------
        categoriesButt.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              showCategoriesLayout();
        }});


        // MARK: - DONE CATEGORY BUTTON ------------------------------------
        Button catDoneButt = findViewById(R.id.sellCategDoneButt);
        catDoneButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideCategoriesLayout();
        }});



        // MARK: - NEW BUTTON ------------------------------------
        newButt.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              condition = "New";
              newButt.setBackgroundColor(Color.parseColor("#777777"));
              usedButt.setBackgroundColor(Color.parseColor("#bababa"));
        }});

        // MARK: - USED BUTTON ------------------------------------
        usedButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                condition = "Used";
                newButt.setBackgroundColor(Color.parseColor("#bababa"));
                usedButt.setBackgroundColor(Color.parseColor("#777777"));
        }});




        // MARK: - UPLOAD IMAGE 1 ----------------------------------------------------------------
        image1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pictureTag = 1;
                showAlertForImage();
         }});

        // MARK: - UPLOAD IMAGE 2 ----------------------------------------------------------------
        image2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pictureTag = 2;
                showAlertForImage();
        }});

        // MARK: - UPLOAD IMAGE 3 ----------------------------------------------------------------
        image3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pictureTag = 3;
                showAlertForImage();
        }});



        // MARK: - UPLOAD VIDEO ----------------------------------------------------------------
        videoThumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pictureTag = 4;
                Log.i("log-", "PICTURE TAG: " + pictureTag);

                AlertDialog.Builder alert = new AlertDialog.Builder(SellEditItem.this);
                alert.setMessage(R.string.alert_Select_source)
                        .setTitle(R.string.app_name)
                        .setPositiveButton(R.string.alert_Take_Video, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (!mmp.checkPermissionForCamera()) {
                                    mmp.requestPermissionForCamera();
                                } else {
                                    openVideoCamera();
                        }}})

                        .setNegativeButton(R.string.alert_Pick_from_Gallery, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (!mmp.checkPermissionForReadExternalStorage()) {
                                    mmp.requestPermissionForReadExternalStorage();
                                } else {
                                    openVideoGallery();
                        }}})

                        .setNeutralButton(R.string.alert_cancel, null)
                        .setIcon(R.drawable.logo);
                alert.create().show();
        }});





        // MARK: - CHOOSE LOCATION BUTTON ------------------------------------
        Button chooseLocationButt = findViewById(R.id.sellChooseLocationButt);
        chooseLocationButt.setTypeface(Configs.titSemibold);
        chooseLocationButt.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              startActivity(new Intent(SellEditItem.this, MapScreen.class));
        }});







        // MARK: - SUBMIT AD BUTTON -----------------------------------------------------------------
        Button submitAdButt = findViewById(R.id.sellSubmitButt);
        submitAdButt.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              ParseUser currentUser = ParseUser.getCurrentUser();

              ParseGeoPoint userGP = null;
              if (Configs.chosenLocation == null) {
                  userGP = new ParseGeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
              } else {
                  userGP = new ParseGeoPoint(Configs.chosenLocation.getLatitude(), Configs.chosenLocation.getLongitude());
              }

              dismissKeyboard();

              // Console log
              Log.i("log-", "USER GEOPOINT: " + userGP.getLatitude() + " -- " + userGP.getLongitude());


              // You haven't filled all required the fields
              if (image1.getDrawable() == null || adTitleTxt.getText().toString().matches("") || condition.matches("")
                      || descriptionTxt.getText().toString().matches("") || categoryName.matches("") || priceTxt.getText().toString().matches("")
                      ){
                  Configs.simpleAlert(R.string.alert_You_must_make_sure_youve_inserted_the_following_details+"\n"+R.string.alert_Category+"\n"+R.string.alert_Ad_Title+"\n"+R.string.alert_Condition_of_the_item+"\n"+R.string.alert_A_description+"\n"+R.string.alert_First_image, SellEditItem.this);


              // You can submit your Ad!
              } else {
                  Configs.showPD(getResources().getString(R.string.alert_Submitting_ad), SellEditItem.this);

                  adObj.put(Configs.ADS_SELLER_POINTER, currentUser);
                  adObj.put(Configs.ADS_TITLE, adTitleTxt.getText().toString());
                  adObj.put(Configs.ADS_CATEGORY, categoryName);
                  adObj.put(Configs.ADS_CONDITION, condition);
                  adObj.put(Configs.ADS_DESCRIPTION, descriptionTxt.getText().toString());
                  adObj.put(Configs.ADS_LOCATION, userGP);
                  adObj.put(Configs.ADS_PRICE, Integer.parseInt(priceTxt.getText().toString()) );
                  adObj.put(Configs.ADS_CURRENCY, Configs.CURRENCY);
                  adObj.put(Configs.ADS_LIKES, 0);
                  adObj.put(Configs.ADS_COMMENTS, 0);
                  adObj.put(Configs.ADS_IS_REPORTED, false);
                  List<String>empty = new ArrayList<>();
                  adObj.put(Configs.ADS_LIKED_BY, empty);

                  // Add keywords
                  List<String> keywords = new ArrayList<String>();
                  String[] a = adTitleTxt.getText().toString().toLowerCase().split(" ");
                  String[] b = descriptionTxt.getText().toString().toLowerCase().split(" ");
                  keywords.addAll(Arrays.asList(a));
                  keywords.addAll(Arrays.asList(b));
                  keywords.add(condition.toLowerCase());
                  keywords.add("@" + currentUser.getString(Configs.USER_USERNAME).toLowerCase());
                  adObj.put(Configs.ADS_KEYWORDS, keywords);

                  // Save video
                  if (videoURI != null) {
                      ParseFile videoFile = new ParseFile ("video.mp4", convertVideoToBytes(videoURI));
                      adObj.put(Configs.ADS_VIDEO, videoFile);

                      // Save thumbnail
                      Bitmap bm = ((BitmapDrawable) videoThumb.getDrawable()).getBitmap();
                      ByteArrayOutputStream st = new ByteArrayOutputStream();
                      bm.compress(Bitmap.CompressFormat.JPEG, 100, st);
                      byte[] byteArr = st.toByteArray();
                      ParseFile thumbFile = new ParseFile("thumb.jpg", byteArr);
                      adObj.put(Configs.ADS_VIDEO_THUMBNAIL, thumbFile);
                  }


                  // Saving block
                  adObj.saveInBackground(new SaveCallback() {
                      @Override
                      public void done(ParseException e) {
                          if (e == null) {

                              // Save image1
                              Configs.saveParseImage(image1, adObj, Configs.ADS_IMAGE1);

                              // Save image2
                              if (image2.getDrawable() != null) {
                                  ImageView img2 = findViewById(R.id.sellAdImg2);
                                  Configs.saveParseImage(img2, adObj, Configs.ADS_IMAGE2);
                              }

                              // Save image3
                              if (image3.getDrawable() != null) {
                                  ImageView img3 = findViewById(R.id.sellAdImg3);
                                  Configs.saveParseImage(img3, adObj, Configs.ADS_IMAGE3);
                              }

                              // Save images now
                              adObj.saveInBackground(new SaveCallback() {
                                  @Override
                                  public void done(ParseException e) {
                                      if (e == null) {
                                          Configs.hidePD();

                                          // Reset choosenLocation
                                          Configs.chosenLocation = null;

                                          // Fire an alert
                                          AlertDialog.Builder alert = new AlertDialog.Builder(SellEditItem.this);
                                          alert.setMessage(R.string.alert_Your_Ad_has_been_successfully_posted)
                                                  .setTitle(R.string.app_name)
                                                  .setPositiveButton(R.string.btn_OK, new DialogInterface.OnClickListener() {
                                                      @Override
                                                      public void onClick(DialogInterface dialogInterface, int i) {
                                                          startActivity(new Intent(SellEditItem.this, Home.class));
                                                      }
                                                  })
                                                  .setCancelable(false)
                                                  .setIcon(R.drawable.logo);
                                          alert.create().show();

                                      // error
                                      } else {
                                          Configs.hidePD();
                                          Configs.simpleAlert(e.getMessage(), SellEditItem.this);
                              }}});


                          // error on saving
                          } else {
                              Configs.hidePD();
                              Configs.simpleAlert(e.getMessage(), SellEditItem.this);
                  }}});


              } // en IF

         }});





        // MARK: - DELETE AD BUTTON
        deleteAdButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder alert = new AlertDialog.Builder(SellEditItem.this);
                alert.setMessage(R.string.alert_Are_you_sure_you_want_to_delete_this_item)
                    .setTitle(R.string.app_name)
                    .setPositiveButton(R.string.alert_Delete_Item, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            deleteAdInOtherClasses();
                    }})

                    .setNegativeButton(R.string.alert_cancel, null)
                    .setIcon(R.drawable.logo);
                alert.create().show();

        }});



    }// end onCreate()








    // MARK: - DELETE AD IN OTHER CLASSES ------------------------------------------------------
    void deleteAdInOtherClasses() {

        // Delete adPointer in Chats class
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Configs.CHATS_CLASS_NAME);
        query.whereEqualTo(Configs.CHATS_AD_POINTER, adObj);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException error) {
                if (error == null) {
                    for (int i = 0; i<objects.size(); i++) {
                        ParseObject obj = objects.get(i);
                        obj.deleteInBackground();
                    }
        }}});


        // Delete adPointer in Comments class
        ParseQuery<ParseObject> query2 = ParseQuery.getQuery(Configs.COMMENTS_CLASS_NAME);
        query2.whereEqualTo(Configs.COMMENTS_AD_POINTER, adObj);
        query2.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException error) {
                if (error == null) {
                    for (int i = 0; i<objects.size(); i++) {
                        ParseObject obj = objects.get(i);
                        obj.deleteInBackground();
                    }
        }}});

        // Delete adPointer in InBox class
        ParseQuery<ParseObject> query3 = ParseQuery.getQuery(Configs.INBOX_CLASS_NAME);
        query3.whereEqualTo(Configs.INBOX_AD_POINTER, adObj);
        query3.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException error) {
                if (error == null) {
                    for (int i = 0; i<objects.size(); i++) {
                        ParseObject obj = objects.get(i);
                        obj.deleteInBackground();
                    }
        }}});

        // Delete adPointer in Likes class
        ParseQuery<ParseObject> query4 = ParseQuery.getQuery(Configs.LIKES_CLASS_NAME);
        query4.whereEqualTo(Configs.LIKES_AD_LIKED, adObj);
        query4.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException error) {
                if (error == null) {
                    for (int i = 0; i<objects.size(); i++) {
                        ParseObject obj = objects.get(i);
                        obj.deleteInBackground();
                    }
        }}});


        // Lastly, delete the Ad
        adObj.deleteInBackground(new DeleteCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(SellEditItem.this);
                    alert.setMessage(R.string.alert_Your_Item_has_been_deleted)
                            .setTitle(R.string.app_name)
                            .setPositiveButton(R.string.btn_OK, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    finish();
                            }})
                            .setCancelable(false)
                            .setIcon(R.drawable.logo);
                    alert.create().show();
                } else {
                    Configs.simpleAlert(e.getMessage(), SellEditItem.this);
                }
        }});

    }







    // MARK: - SHOW AD's DETAILS ------------------------------------------------------------------
    void showAdDetails() {
        // Get image1
        Configs.getParseImage(image1, adObj, Configs.ADS_IMAGE1);

        // Get image2
        Configs.getParseImage(image2, adObj, Configs.ADS_IMAGE2);

        // Get image3
        Configs.getParseImage(image3, adObj, Configs.ADS_IMAGE3);

        // Get video thumbnail
        if (adObj.getParseFile(Configs.ADS_VIDEO) != null) {
            Configs.getParseImage(videoThumb, adObj, Configs.ADS_VIDEO_THUMBNAIL);
        }

        // Get Category
        categoriesButt.setText(adObj.getString(Configs.ADS_CATEGORY));
        categoryName = adObj.getString(Configs.ADS_CATEGORY);

        // Get Title
        adTitleTxt.setText(adObj.getString(Configs.ADS_TITLE));

        // Get Price
        priceTxt.setText(String.valueOf(adObj.getNumber(Configs.ADS_PRICE)));

        // Get condition
        condition = adObj.getString(Configs.ADS_CONDITION);
        if (condition.matches("New")) {
            newButt.setBackgroundColor(Color.parseColor("#777777"));
            usedButt.setBackgroundColor(Color.parseColor("#bababa"));

        } else {
            newButt.setBackgroundColor(Color.parseColor("#bababa"));
            usedButt.setBackgroundColor(Color.parseColor("#777777"));
        }

        // Get description
        descriptionTxt.setText(adObj.getString(Configs.ADS_DESCRIPTION));

        Log.i("log-", "VIDEO URI ON CREATE(): " + videoURI);
    }







    // MARK: - SHOW ALERT FOR UPLOADING IMAGES -----------------------------------------------------
    void showAlertForImage() {
        Log.i("log-", "PICTURE TAG: " + pictureTag);
        AlertDialog.Builder alert = new AlertDialog.Builder(SellEditItem.this);
        alert.setMessage(R.string.alert_Select_source)
                .setTitle(R.string.app_name)
                .setPositiveButton(R.string.alert_Take_Picture, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (!mmp.checkPermissionForCamera()) {
                            mmp.requestPermissionForCamera();
                        } else {
                            openCamera();
                        }
                    }})

                .setNegativeButton(R.string.alert_Pick_from_Gallery, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (!mmp.checkPermissionForReadExternalStorage()) {
                            mmp.requestPermissionForReadExternalStorage();
                        } else {
                            openGallery();
                        }
                    }})

                .setNeutralButton(R.string.alert_cancel, null)
                .setIcon(R.drawable.logo);
        alert.create().show();
    }







    // IMAGE/VIDEO HANDLING METHODS ------------------------------------------------------------------------
    int CAMERA = 0;
    int GALLERY = 1;
    int VIDEO_CAMERA = 2;
    int VIDEO_GALLERY = 3;
    String videoPath = null;
    Uri videoURI = null;
    Uri imageURI;
    File file;


    // OPEN CAMERA
    public void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        file = new File(Environment.getExternalStorageDirectory(), "image.jpg");
        imageURI = FileProvider.getUriForFile(getApplicationContext(), getPackageName() + ".provider", file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageURI);
        startActivityForResult(intent, CAMERA);
    }



    // OPEN GALLERY
    public void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.alert_Select_Image)), GALLERY);
    }


    // OPEN VIDEO CAMERA
    public void openVideoCamera() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, Configs.MAXIMUM_DURATION_VIDEO);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
        startActivityForResult(intent, VIDEO_CAMERA);
    }

    // OPEN VIDEO GALLERY
    public void openVideoGallery() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, Configs.MAXIMUM_DURATION_VIDEO);
        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.alert_Select_Video)), VIDEO_GALLERY);
    }





    // IMAGE/VIDEO PICKED DELEGATE ------------------------------
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            Bitmap bm = null;


            // Image from Camera
            if (requestCode == CAMERA) {
                try {
                    File f = file;
                    ExifInterface exif = new ExifInterface(f.getPath());
                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

                    int angle = 0;
                    if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                        angle = 90;
                    } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                        angle = 180;
                    } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                        angle = 270;
                    }
                    Log.i("log-", "ORIENTATION: " + orientation);

                    Matrix mat = new Matrix();
                    mat.postRotate(angle);

                    Bitmap bmp = BitmapFactory.decodeStream(new FileInputStream(f), null, null);
                    bm = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), mat, true);
                } catch (IOException | OutOfMemoryError e) { Log.i("log-", e.getMessage()); }



            // Image from Gallery
            } else if (requestCode == GALLERY) {
                try { bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                } catch (IOException e) { e.printStackTrace(); }




            // Video from Camera or Gallery
            } else if (requestCode == VIDEO_CAMERA || requestCode == VIDEO_GALLERY) {
                videoURI = data.getData();
                videoPath = getRealPathFromURI(videoURI);
                Log.i("log-", "VIDEO PATH: " + videoPath);
                Log.i("log-", "VIDEO URI: " + videoURI);

                // Check video duration
                MediaPlayer mp = MediaPlayer.create(this, videoURI);
                int videoDuration = mp.getDuration();
                mp.release();
                Log.i("log-", "VIDEO DURATION: " + videoDuration);

                // Video duration is within the allowed seconds
                if (videoDuration < Configs.MAXIMUM_DURATION_VIDEO*1100) {
                    // Set video thumbnail
                    Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Images.Thumbnails.MINI_KIND);
                    videoThumb.setImageBitmap(thumbnail);

                // Video exceeds the maximum allowed duration
                } else {
                    Configs.simpleAlert(R.string.alert_Your_video_is_longer_than +
                            String.valueOf(Configs.MAXIMUM_DURATION_VIDEO) +
                            R.string.alert_seconds_Please_choose_or_take_a_shorter_video, SellEditItem.this);

                    // Reset variables and image
                    videoPath = null;
                    videoURI = null;
                    videoThumb.setImageResource(R.drawable.add_video_image);
                }

            }



            // Set images based on the pictureTag
            if (bm != null) {
                Bitmap scaledBm = Configs.scaleBitmapToMaxSize(600, bm);

                int bmBytes = scaledBm.getByteCount();
                Log.i("log-", "BITMAP SIZE IN MB: " + bmBytes/1024);


                if (pictureTag == 1) { image1.setImageBitmap(scaledBm);
                } else if (pictureTag == 2) { image2.setImageBitmap(scaledBm);
                } else if (pictureTag == 3) { image3.setImageBitmap(scaledBm); }
            }

        }
    }
    //---------------------------------------------------------------------------------------------






    // GET VIDEO PATH AS A STRING -------------------------------------
    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }



    // CONVERT VIDEO TO BYTES -----------------------------------
    private byte[] convertVideoToBytes(Uri uri){
        byte[] videoBytes = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            FileInputStream fis = new FileInputStream(new File(getRealPathFromURI(uri)));

            byte[] buf = new byte[1024];
            int n;
            while (-1 != (n = fis.read(buf)))
                baos.write(buf, 0, n);

            videoBytes = baos.toByteArray();
        } catch (FileNotFoundException e) { e.printStackTrace();
        } catch (IOException e) { e.printStackTrace(); }
        return videoBytes;
    }






    // MARK: - QUERY CATEGORIES ------------------------------------------------------------
    void queryCategories() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Configs.CATEGORIES_CLASS_NAME);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException error) {
                if (error == null) {
                    categoriesArray = objects;

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
                                cell = inflater.inflate(R.layout.cell_category_sortby, null);
                            }

                            // Get Parse object
                            ParseObject cObj = categoriesArray.get(position);

                            TextView catTxt = cell.findViewById(R.id.cCatSortTxt);
                            catTxt.setTypeface(Configs.titRegular);
                            catTxt.setText(cObj.getString(Configs.CATEGORIES_CATEGORY));

                        return cell;
                        }

                        @Override public int getCount() {return categoriesArray.size(); }
                        @Override public Object getItem(int position) { return categoriesArray.get(position);}
                        @Override public long getItemId(int position) { return position; }
                    }

                    // Init ListView and set its adapter
                    ListView aList = findViewById(R.id.sellCategListView);
                    aList.setAdapter(new ListAdapter(SellEditItem.this, categoriesArray));
                    aList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                            ParseObject cObj = categoriesArray.get(position);
                            categoriesButt.setText(cObj.getString(Configs.CATEGORIES_CATEGORY));
                            categoryName = cObj.getString(Configs.CATEGORIES_CATEGORY);
                    }});


                // Error in query
                } else {
                    Configs.hidePD();
                    Configs.simpleAlert(error.getMessage(), SellEditItem.this);
        }}});

    }



    // MARK: - SHOW/HIDE CATEGORIES LAYOUT
    void showCategoriesLayout() {
        ViewGroup.MarginLayoutParams marginParams = new ViewGroup.MarginLayoutParams(categoriesLayout.getLayoutParams());
        marginParams.setMargins(0, 0, 0, 0);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(marginParams);
        categoriesLayout.setLayoutParams(layoutParams);
    }


    void hideCategoriesLayout() {
        ViewGroup.MarginLayoutParams marginParams = new ViewGroup.MarginLayoutParams(categoriesLayout.getLayoutParams());
        marginParams.setMargins(0, 2000, 0, 0);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(marginParams);
        categoriesLayout.setLayoutParams(layoutParams);
    }








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
            Log.i("log-", "CURRENT LOCATION FOUND! " + currentLocation.getLatitude());


        // Try to get current Location one more time
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
            Log.i("log-", "CURRENT LOCATION FOUND! " + currentLocation.getLatitude());

        // NO GPS location found!
        } else {
            Configs.simpleAlert(getResources().getString(R.string.alert_Failed_to_get_your_Location)+"\n"+getResources().getString(R.string.alert_Go_into), SellEditItem.this);

            // Set New York City as default currentLocation
            currentLocation = new Location("dummyprovider");
            currentLocation.setLatitude(Configs.DEFAULT_LOCATION.latitude) ;
            currentLocation.setLongitude(Configs.DEFAULT_LOCATION.longitude);
        }
    }

    @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
    @Override public void onProviderEnabled(String provider) {}
    @Override public void onProviderDisabled(String provider) {}






    // MARK: - DISMISS KEYBOARD
    public void dismissKeyboard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        assert imm != null;
        imm.hideSoftInputFromWindow(titleTxt.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(priceTxt.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(descriptionTxt.getWindowToken(), 0);
    }




}//@end
