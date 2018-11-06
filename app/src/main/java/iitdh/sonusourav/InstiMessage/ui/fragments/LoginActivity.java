package iitdh.sonusourav.InstiMessage.ui.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import iitdh.sonusourav.InstiMessage.R;
import iitdh.sonusourav.InstiMessage.models.User;
import iitdh.sonusourav.InstiMessage.ui.activities.RegisterActivity;
import iitdh.sonusourav.InstiMessage.ui.activities.UserListingActivity;
import iitdh.sonusourav.InstiMessage.utils.PreferenceManager;

public class LoginActivity extends AppCompatActivity {
    public FirebaseAuth loginAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference rootRef;
    private DatabaseReference userRef;
    private Button loginButton;
    private EditText loginEmail;
    private EditText loginPass;
    private SignInButton googleSignInButton;
    private CheckBox rememberMe;
    private TextView signUp;
    private ImageView loginLogo;
    private static final int RC_SIGN_IN = 234;
    private PreferenceManager loginPref;

    //Tag for the logs optional
    private static final String TAG = LoginActivity.class.getSimpleName();

    //creating a GoogleSignInClient object
    GoogleSignInClient mGoogleSignInClient;


    private String signInEmail;
    private String signInPass;
    private ProgressDialog mProgressDialog;

    private long back_pressed;

    protected void onCreate(Bundle bundle) {

        super.onCreate(bundle);
        setContentView(R.layout.activity_login);

        if (bundle != null) {
            onRestoreInstanceState(bundle);
        }

        initLogin();
        loginPref=new PreferenceManager(this);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Animation fadeIn = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.fadeout);

                loginLogo.setVisibility(View.VISIBLE);
                loginLogo.startAnimation(fadeIn);

            }
        }, 1000);




        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                signInEmail = loginEmail.getText().toString().trim();
                signInPass = loginPass.getText().toString().trim();


                if ((signInEmail == null) || signInEmail.equals("") || TextUtils.isEmpty(signInEmail) || (signInEmail.length() == 0)) {
                    Toast.makeText(getApplicationContext(), "Enter your username", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (signInPass.equals("") || (signInPass == null) || TextUtils.isEmpty(signInPass) || signInPass.length() == 0) {
                    Toast.makeText(getApplicationContext(), "Enter your password", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!validateEmail(signInEmail)) {
                    Toast.makeText(getApplicationContext(), "Invalid Email address", Toast.LENGTH_SHORT).show();
                    return;
                }

                showProgressDialog();

                (loginAuth.signInWithEmailAndPassword(signInEmail, signInPass))
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()) {

                                    checkEmailVerification();

                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Log.d("error",e.getLocalizedMessage());
                                hideProgressDialog();

                                Toast.makeText(getApplicationContext(),e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                            }


                                });

                }

                });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        //Then we will get the GoogleSignInClient object from GoogleSignIn class
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //Now we will attach a click listener to the sign_in_button
        //and inside onClick() method we are calling the signIn() method that will open
        //google sign in intent
        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });



        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));

            }
        });



        }

    @Override
    protected void onStart() {
        super.onStart();

        //if the user is already signed in
        //we will close this activity
        //and take the user to profile activity

        if (loginAuth.getCurrentUser() != null) {
            Log.d("Current User","active");
            startActivity(new Intent(this, UserListingActivity.class));
            Toast.makeText(getApplicationContext(),"Signed In as " + loginAuth.getCurrentUser().getDisplayName(),Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    public void initLogin(){

        loginLogo=findViewById(R.id.login_logo);
        loginEmail =findViewById(R.id.ed_user_name);
        loginPass =findViewById(R.id.ed_password);
        rememberMe=findViewById(R.id.remember_me_chk_box);
        loginButton=findViewById(R.id.button_login);
        googleSignInButton=findViewById(R.id.button_google_login);
        signUp=findViewById(R.id.login_tv3);
        loginEmail.setText("");
        loginPass.setText("");
        rememberMe.setChecked(false);
        loginAuth = FirebaseAuth.getInstance();
        firebaseDatabase=FirebaseDatabase.getInstance();
        rootRef=firebaseDatabase.getReference();

    }

    //validateEmailMethod
    public boolean validateEmail(String email) {

        Pattern pattern;
        Matcher matcher;
        String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(email);
        return matcher.matches();

    }


    private void checkEmailVerification(){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        assert firebaseUser != null;


            if(rememberMe.isChecked()){
                loginPref.setIsLoggedIn(true);
                loginPref.setLoginCredentials(signInEmail,signInPass);
            }
            String testEmail=loginPref.getPrefEmail();
            Log.d("LoginEmail",testEmail);
            Toast.makeText(getApplicationContext(),"Signed In successfully",Toast.LENGTH_SHORT).show();
            Intent intent=new Intent(LoginActivity.this,UserListingActivity.class);
            startActivity(intent);
            hideProgressDialog();
            finish();




    }

    public void onBackPressed() {
        if (back_pressed + 2000 > System.currentTimeMillis()) {
            moveTaskToBack(true);
        } else
            Toast.makeText(getBaseContext(), "Press once again to exit!", Toast.LENGTH_SHORT).show();
        back_pressed = System.currentTimeMillis();
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //if the requestCode is the Google Sign In code that we defined at starting
        if (requestCode == RC_SIGN_IN) {

            //Getting the GoogleSignIn Task
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                //Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);

                //authenticating with firebase
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {


        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        //getting the auth credential
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        //Now using firebase we are signing in the user here
        loginAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            final FirebaseUser user = loginAuth.getCurrentUser();
                            assert user != null;
                            String email = user.getEmail();
                            assert email != null;

                                if(loginPref.isFirstGoogleLogin()){

                                    loginPref.setIsFirstGoogleLogin(false);
                                    userRef = rootRef.getRef().child("users").push().getRef();

                                    String uid=user.getUid();
                                    String token=acct.getIdToken();

                                    userRef.setValue(new User(uid,user.getEmail(),token)).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(LoginActivity.this, " Login failed .\n Try again.", Toast.LENGTH_SHORT).show();
                                            loginPref.setIsFirstGoogleLogin(false);
                                            hideProgressDialog();

                                        }
                                    }).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            loginPref.setIsFirstGoogleLogin(false);
                                            Toast.makeText(LoginActivity.this, " Signed In as " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(LoginActivity.this, UserListingActivity.class));
                                            hideProgressDialog();
                                            finish();

                                        }
                                    });


                                }else {
                                    Toast.makeText(LoginActivity.this, " Signed In as " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(LoginActivity.this, UserListingActivity.class));

                                    hideProgressDialog();

                                }


                        } else {
                            // If sign in fails
                            hideProgressDialog();
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }

                    }
                });
    }


    //this method is called on click
    private void signIn() {
        //getting the google signIn intent
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();

        showProgressDialog();
        //starting the activity for result
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void showProgressDialog() {

        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this,R.style.MyAlertDialogStyle);
            mProgressDialog.setMessage("Logging In ....");
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCanceledOnTouchOutside(false);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
    static String encodeUserEmail(String userEmail) {
        return userEmail.replace(".", ",");
    }

}
