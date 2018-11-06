package iitdh.sonusourav.InstiMessage.ui.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

import iitdh.sonusourav.InstiMessage.R;
import iitdh.sonusourav.InstiMessage.utils.PreferenceManager;


public class SplashActivity extends AppCompatActivity {

    public static String TAG= SplashActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_splash);
        PreferenceManager splashPrefManager = new PreferenceManager(this);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        }




        ImageView splash = findViewById(R.id.splash_iv);

        if (splash != null) {
            Glide.with(this)
                    .asGif()
                    .load(R.drawable.gif_splash)
                    .into(splash)

            ;
        }


        if (splashPrefManager.isLoggedIn()) {
            Log.d(TAG, "Splash:onCreate: isLoggedIn=true");
            String splashEmail = splashPrefManager.getPrefEmail();
            String splashPassword = splashPrefManager.getPrefPassword();


            assert splashEmail != null;
            assert splashPassword != null;

            (firebaseAuth.signInWithEmailAndPassword(splashEmail, splashPassword))
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) {

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        final Intent mainIntent = new Intent(SplashActivity.this,UserListingActivity.class);
                                        SplashActivity.this.startActivity(mainIntent);
                                        SplashActivity.this.finish();
                                    }
                                }, 1000);


                            } else {
                                Log.e("SharedPreference Error", Objects.requireNonNull(task.getException()).toString());
                                Log.d(TAG, "SharedPreference: isLoggedIn=false");

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        final Intent mainIntent = new Intent(SplashActivity.this, Login.class);
                                        SplashActivity.this.startActivity(mainIntent);
                                        SplashActivity.this.finish();
                                    }
                                }, 1000);
                                Toast.makeText(SplashActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();

                            }
                        }
                    });

        } else {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    final Intent mainIntent = new Intent(SplashActivity.this, IntroScreen.class);
                    SplashActivity.this.startActivity(mainIntent);
                    SplashActivity.this.finish();
                }
            }, 4600);

            FirebaseUser user=firebaseAuth.getCurrentUser();
            if(user!=null){
                Log.d("User is not null","true");
            }

            Log.d(TAG, "SharedPreference: isLoggedIn=false");

        }
    }
}
