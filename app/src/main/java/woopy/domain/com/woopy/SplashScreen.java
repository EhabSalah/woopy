package woopy.domain.com.woopy;

/*-------------------------------

    - woopy -

    Created by cubycode @2017
    All Rights reserved

-------------------------------*/

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import java.util.Locale;

public class SplashScreen extends AppCompatActivity {

    private static int splashInterval = 2000;

    SharedPreferences.Editor editor;
    SharedPreferences prefs;
    private static Locale myLocale;
    String langPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.splash_screen);

        prefs = getSharedPreferences("CommonPrefs", MODE_PRIVATE);
        String strPref = prefs.getString("App_Language", "ar");
        if(strPref.equals("en"))
        {
            changeLang("en");
        }
        else{
            changeLang("ar");
        }

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                Intent i = new Intent(SplashScreen.this, Home.class);
                startActivity(i);
                this.finish();
            }

            private void finish() {
            }
        }, splashInterval);

    };

    public void changeLang(String lang) {
        if (lang.equalsIgnoreCase(""))
            return;
        myLocale = new Locale(lang);
        Locale.setDefault(myLocale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.locale = myLocale;
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        saveLocale(lang);

    }
    public void saveLocale(String lang)
    {
        langPref = "App_Language";
        prefs =getSharedPreferences("CommonPrefs", Activity.MODE_PRIVATE);
        editor = prefs.edit();
        editor.putString(langPref, lang);
        editor.commit();
    }

}
