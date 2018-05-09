package woopy.domain.com.woopy;

/*-------------------------------

    - woopy -

    Created by cubycode @2017
    All Rights reserved

-------------------------------*/

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class EditProfile extends AppCompatActivity {

    /* Views */
    EditText usernameTxt, fullnameTxt, websiteTxt, aboutTxt;
    ImageView avatarImg;
    MarshMallowPermission mmp = new MarshMallowPermission(this);
    Context ctx = EditProfile.this;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Hide ActionBar
        getSupportActionBar().hide();



        // Init views
        usernameTxt = findViewById(R.id.epUsernameTxt);
        usernameTxt.setTypeface(Configs.titRegular);
        fullnameTxt = findViewById(R.id.epFullnameTxt);
        fullnameTxt.setTypeface(Configs.titRegular);
        websiteTxt = findViewById(R.id.epWebsiteTxt);
        websiteTxt.setTypeface(Configs.titRegular);
        aboutTxt = findViewById(R.id.epAboutTxt);
        aboutTxt.setTypeface(Configs.titRegular);
        avatarImg = findViewById(R.id.epAvatarImg);



        // Call query
        showMyDetails();



        // MARK: - CHANGE AVATAR IMAGE ----------------------------------------
        avatarImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(EditProfile.this);
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
        }});





        // MARK: - SAVE PROFILE BUTTON ------------------------------------
        Button saveButt = findViewById(R.id.epSaveButt);
        saveButt.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {

              ParseUser currUser = ParseUser.getCurrentUser();

              if (usernameTxt.getText().toString().matches("") || fullnameTxt.getText().toString().matches("") ) {
                  Configs.simpleAlert(getResources().getString(R.string.alert_You_must_insert_at_least_a_username_and_your_full_name), EditProfile.this);

              } else {
                  Configs.showPD(getResources().getString(R.string.alert_please_wait), EditProfile.this);
                  dismissKeyboard();

                  currUser.put(Configs.USER_USERNAME,  usernameTxt.getText().toString());
                  currUser.put(Configs.USER_FULLNAME, fullnameTxt.getText().toString());
                  if (!websiteTxt.getText().toString().matches(""))  { currUser.put(Configs.USER_WEBSITE, websiteTxt.getText().toString()); }
                  if (!aboutTxt.getText().toString().matches("")) { currUser.put(Configs.USER_ABOUT_ME, aboutTxt.getText().toString()); }

                  // Save Avatar
                  Configs.saveParseImage(avatarImg, currUser, Configs.USER_AVATAR);

                  // Saving block
                  currUser.saveInBackground(new SaveCallback() {
                      @Override
                      public void done(ParseException e) {
                          if (e == null) {
                              Configs.hidePD();
                              Configs.simpleAlert(getResources().getString(R.string.alert_Your_Profile_has_been_updated), EditProfile.this);
                          } else {
                              Configs.hidePD();
                              Configs.simpleAlert(e.getMessage(), EditProfile.this);
                  }}});
              }
         }});






        // MARK: - RESET PASSWORD BUTTON ------------------------------------
        Button rpButt = findViewById(R.id.epResetPasswordButt);
        rpButt.setTypeface(Configs.titSemibold);
        rpButt.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              AlertDialog.Builder alert = new AlertDialog.Builder(ctx)
              .setTitle(R.string.app_name)
              .setIcon(R.drawable.logo)
              .setMessage(getResources().getString(R.string.alert_Type_the_valid_email_address_you_have_used_to_register_on_this_app));

              // Add an EditTxt
              final EditText editTxt = new EditText (EditProfile.this);
              editTxt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
              alert.setView(editTxt)
              .setNegativeButton(R.string.alert_cancel, null)
              .setPositiveButton(R.string.alert_Reset_Password, new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int whichButton) {
                      // Reset password
                      ParseUser.requestPasswordResetInBackground(editTxt.getText().toString(), new RequestPasswordResetCallback() {
                      public void done(ParseException error) {
                          if (error == null) {
                              AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                              builder.setMessage(R.string.alert_We_ve_sent_you_an_email_to_reset_your_password)
                              .setTitle(R.string.app_name)
                              .setPositiveButton(R.string.btn_OK, null);
                              AlertDialog dialog = builder.create();
                              dialog.setIcon(R.drawable.logo);
                              dialog.show();

                           } else {
                              Configs.simpleAlert(error.getMessage(), ctx);
                      }}});

              }});
              alert.show();


         }});


        // MARK: - TERMS OF SERVICE BUTTON ----------------------------------------------------------
        Button tosButt = findViewById(R.id.epTOSbutt);
        tosButt.setTypeface(Configs.titSemibold);
        tosButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ctx, TermsOfUse.class));
            }});



        // MARK: - BACK BUTTON ------------------------------------
        Button backButt = findViewById(R.id.epBackButt);
        backButt.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              finish();
         }});



    }// end onCreate()






    // MARK: - SHOW MY DETAILS ----------------------------------------------------------------
    void showMyDetails() {

        ParseUser currUser = ParseUser.getCurrentUser();

        usernameTxt.setText(currUser.getString(Configs.USER_USERNAME));
        fullnameTxt.setText(currUser.getString(Configs.USER_FULLNAME));
        if (currUser.getString(Configs.USER_WEBSITE) != null) { websiteTxt.setText(currUser.getString(Configs.USER_WEBSITE)); }
        if (currUser.getString(Configs.USER_ABOUT_ME) != null) { aboutTxt.setText(currUser.getString(Configs.USER_ABOUT_ME)); }

        // Get Avatar
        Configs.getParseImage(avatarImg, currUser, Configs.USER_AVATAR);
    }








    // IMAGE HANDLING METHODS ------------------------------------------------------------------------
    int CAMERA = 0;
    int GALLERY = 1;
    Uri imageURI;
    File file;


    // OPEN CAMERA
    public void openCamera() {
        Intent intent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
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



    // IMAGE PICKED DELEGATE -----------------------------------
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
                    if (orientation == ExifInterface.ORIENTATION_ROTATE_90) { angle = 90; }
                    else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) { angle = 180; }
                    else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) { angle = 270; }
                    Log.i("log-", "ORIENTATION: " + orientation);

                    Matrix mat = new Matrix();
                    mat.postRotate(angle);

                    Bitmap bmp = BitmapFactory.decodeStream(new FileInputStream(f), null, null);
                    bm = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), mat, true);
                }
                catch (IOException | OutOfMemoryError e) { Log.i("log-", e.getMessage()); }


                // Image from Gallery
            } else if (requestCode == GALLERY) {
                try { bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                } catch (IOException e) { e.printStackTrace(); }
            }



            // Set images based on the pictureTag
            if (bm != null) {
                Bitmap scaledBm = Configs.scaleBitmapToMaxSize(400, bm);
                avatarImg.setImageBitmap(scaledBm);
            }

        }
    }
    //---------------------------------------------------------------------------------------------







    // MARK: - DISMISS KEYBOARD
    public void dismissKeyboard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(usernameTxt.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(fullnameTxt.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(websiteTxt.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(aboutTxt.getWindowToken(), 0);
    }




}// @end
