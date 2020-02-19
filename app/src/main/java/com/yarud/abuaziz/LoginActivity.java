package com.yarud.abuaziz;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.internal.ImageRequest;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.yarud.abuaziz.models.ModelUser;
import com.yarud.abuaziz.utils.DBHandler;
import com.yarud.abuaziz.utils.HandlerServer;
import com.yarud.abuaziz.utils.ResponServer;
import com.yarud.abuaziz.utils.ServiceAddress;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "LoginActivity";
    private DBHandler dbHandler;
    private LoginButton signInFacebook;
    private Button button_google, button_facebook;
    private CallbackManager callbackManager;
    private ProgressBar login_progressBar;
    private String ID_LOGIN, NAMA, EMAIL, SUMBER_LOGIN;
    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        initObjects();
        configure();
    }

    private void initObjects() {
        dbHandler = new DBHandler(this);
        login_progressBar = findViewById(R.id.login_progressBar);
        button_facebook = findViewById(R.id.button_facebook);
        button_facebook.setOnClickListener(this);
        signInFacebook = findViewById(R.id.signInFacebook);
        signInFacebook.setPermissions(Arrays.asList("email","public_profile"));
        callbackManager = CallbackManager.Factory.create();

        button_google = findViewById(R.id.button_google);
        button_google.setOnClickListener(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();
    }

    private void configure() {
        dbHandler.deleteDB();

    }

    @Override
    public void onClick(View v) {
        if (isNetworkAvailable()){
            switch (v.getId()){
                case R.id.button_facebook:
                    signInFacebook.performClick();
                    hubungkanAkunKeFacebook();
                    break;
                case R.id.button_google:
                    hubungkanAkunKeGoogle();
                    break;
            }
            login_progressBar.setVisibility(View.VISIBLE);
            button_facebook.setVisibility(View.GONE);
            button_google.setVisibility(View.GONE);
        } else {
            Toast.makeText(this, "Periksa koneksi internet anda", Toast.LENGTH_SHORT).show();
        }
    }

    private void hubungkanAkunKeGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void hubungkanAkunKeFacebook() {
        signInFacebook.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                login_progressBar.setVisibility(View.VISIBLE);
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                if (response != null){
                                    Log.e(TAG, "onCompleted: " + response);
                                    try {
                                        ID_LOGIN = object.getString("id");
                                        NAMA = object.getString("name");
                                        EMAIL = "Facebook Akun";
                                        SUMBER_LOGIN = "FACEBOOK";
                                        Log.e(TAG, "onCompleted: " + ID_LOGIN + " " + NAMA + " " + EMAIL + " " + SUMBER_LOGIN);
                                        dbHandler.addUser(new ModelUser(1, SUMBER_LOGIN, ID_LOGIN, NAMA, EMAIL));
                                        checkLocalDB();
                                    } catch (JSONException e) {
                                        Toast.makeText(LoginActivity.this, "Gagal login dengan akun Facebook", Toast.LENGTH_SHORT).show();
                                        Log.e(TAG, "onCompleted: hubungkanAkunKeFacebook: " + e);
                                        e.printStackTrace();
                                        login_progressBar.setVisibility(View.GONE);
                                        button_facebook.setVisibility(View.VISIBLE);
                                        button_google.setVisibility(View.VISIBLE);
                                    }
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields","id, name, email");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                Log.e(TAG, "onCancel: hubungkanAkunKeFacebook");
                login_progressBar.setVisibility(View.GONE);
                button_facebook.setVisibility(View.VISIBLE);
                button_google.setVisibility(View.VISIBLE);
                Toast.makeText(LoginActivity.this, "Anda membatalkan login menggunakan Facebook akun", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Log.e(TAG, "onError: " + error.toString());
                login_progressBar.setVisibility(View.GONE);
                button_facebook.setVisibility(View.VISIBLE);
                button_google.setVisibility(View.VISIBLE);
            }
        });
    }

    private void checkLocalDB(){
        ArrayList<HashMap<String, String>> userDB = dbHandler.getUser(1);
        String ID_LOGINDB = null;
        for (Map<String, String> map : userDB){
            ID_LOGINDB = map.get("id_login");
        }
        if (ID_LOGINDB != null){
            final Uri photo;
            int dimensionPixelSize = getResources()
                    .getDimensionPixelSize(com.facebook.R.dimen.com_facebook_profilepictureview_preset_size_large);
            if (SUMBER_LOGIN.equals("FACEBOOK")) {
                photo = ImageRequest.getProfilePictureUri(ID_LOGIN, dimensionPixelSize, dimensionPixelSize);
            } else {
                FirebaseUser currentUser = mAuth.getCurrentUser();
                assert currentUser != null;
                photo = currentUser.getPhotoUrl();
            }
            saveDataToServer(photo);
        } else {
            Toast.makeText(this, "Gagal masuk hubungi Developer", Toast.LENGTH_SHORT).show();
            login_progressBar.setVisibility(View.GONE);
            button_facebook.setVisibility(View.VISIBLE);
            button_google.setVisibility(View.VISIBLE);
        }
    }

    private void saveDataToServer(Uri photo){
        Log.e(TAG, "saveDataToServer: "+ photo);
        List<String> list = new ArrayList<>();
        list.add(SUMBER_LOGIN);
        list.add(ID_LOGIN);
        list.add(NAMA);
        list.add(EMAIL);
        list.add(String.valueOf(photo));
        HandlerServer handlerServer = new HandlerServer(this, ServiceAddress.TAMBAHUSER);
        synchronized (this){
            handlerServer.sendDataToServer(new ResponServer() {
                @Override
                public void gagal(String result) {
                    Log.e(TAG, "gagal: " + result);
                    login_progressBar.setVisibility(View.GONE);
                    button_facebook.setVisibility(View.VISIBLE);
                    button_google.setVisibility(View.VISIBLE);
                }

                @Override
                public void berhasil(JSONArray jsonArray) {
                    Log.e(TAG, "berhasil: " + jsonArray);
                    login_progressBar.setVisibility(View.GONE);
                    button_facebook.setVisibility(View.VISIBLE);
                    button_google.setVisibility(View.VISIBLE);
                    finish();
                }
            }, list);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Log.e(TAG, "onActivityResult: GOOGLE");
            // GOOGLE
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                assert account != null;
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
                login_progressBar.setVisibility(View.GONE);
                button_facebook.setVisibility(View.VISIBLE);
                button_google.setVisibility(View.VISIBLE);
            }
        } else {
            //FACEBOOK
            callbackManager.onActivityResult(requestCode,resultCode,data);
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        login_progressBar.setVisibility(View.VISIBLE);
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            assert user != null;
                            ID_LOGIN = user.getUid();
                            NAMA = user.getDisplayName();
                            EMAIL = user.getEmail();
                            SUMBER_LOGIN = "GOOGLE";
                            dbHandler.addUser(new ModelUser(1, SUMBER_LOGIN, ID_LOGIN, NAMA, EMAIL));
                            checkLocalDB();
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Gagal masuk dengan akun Google", Toast.LENGTH_SHORT).show();
                            login_progressBar.setVisibility(View.GONE);
                            button_facebook.setVisibility(View.VISIBLE);
                            button_google.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = null;
        if (connectivityManager != null) {
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
