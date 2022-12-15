package com.example.supperapp;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 9;
    private Button phone, google, email;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private AuthUI.IdpConfig provider, googleprovider, emailprovider;

    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        phone = findViewById(R.id.firebaseAuthantication);
        google = findViewById(R.id.googleFirebaseAuthantication);
        email = findViewById(R.id.emailFirebaseAuthantication);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        phone.setOnClickListener(v -> startprovider());

        google.setOnClickListener(v -> {

            GoogleSignInClient signInClient;
            GoogleSignInOptions signInOptions;
            String idToken;

            idToken = getResources().getString(R.string.default_web_client_id);
            signInOptions = new GoogleSignInOptions
                    .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(idToken)
                    .requestEmail().build();

            signInClient = GoogleSignIn.getClient(this, signInOptions);
            Intent intent = signInClient.getSignInIntent();
            mLauncher.launch(intent);
//            Intent intent = signInClient.getSignInIntent();
//            mLauncher.launch(intent);
        });

        email.setOnClickListener(v -> {
            List<AuthUI.IdpConfig> providerss = Collections.singletonList(
                    new AuthUI.IdpConfig.EmailBuilder().build());
            Intent intent = AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providerss)
                    .build();
            uiLauncher.launch(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        user = auth.getCurrentUser();
        if (user != null){
            Intent intent = new Intent(MainActivity.this, SuperNavigation.class);
            startActivity(intent);
            finish();
        }
    }

    private void emailAuthantication() {
        emailprovider = new AuthUI.IdpConfig.EmailBuilder().build();
        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(Collections.singletonList(emailprovider)).build(), REQUEST_CODE);
    }

    private void googleAuthantication() {
        googleprovider = new AuthUI.IdpConfig.GoogleBuilder().build();
        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(Collections.singletonList(googleprovider)).build(), REQUEST_CODE);
    }

    private void startprovider() {
        provider = new AuthUI.IdpConfig.PhoneBuilder().build();
        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(Collections.singletonList(provider)).build(), REQUEST_CODE);
    }



    ActivityResultLauncher<Intent> uiLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            new ActivityResultCallback<FirebaseAuthUIAuthenticationResult>() {
                @Override
                public void onActivityResult(FirebaseAuthUIAuthenticationResult result) {
                    if (result.getResultCode() == RESULT_OK){
                        Intent intent = new Intent(getBaseContext(), SplashScreen.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                }
            }
    );

    ActivityResultLauncher<Intent> mLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK){
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            verifyGoogleAccount(account.getIdToken());
                        } catch (ApiException e) {
                            Toast.makeText(getBaseContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
    );

    private void verifyGoogleAccount(String idToken) {
        AuthCredential credential;
        credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential).addOnCompleteListener(this, task ->{
           if (task.isSuccessful()){
               Intent intent = new Intent(MainActivity.this, SuperNavigation.class);
               startActivity(intent);
               finish();
           }else {
               Toast.makeText(MainActivity.this, "Error"+task.getException(), Toast.LENGTH_SHORT).show();
           }
        });
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        GoogleSignInOptions signInOptions;
        GoogleSignInClient signInClient;

        signInOptions = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        signInClient = GoogleSignIn.getClient(this, signInOptions);
        signInClient.signOut();

        auth.signOut();


        return super.onOptionsItemSelected(item);
    }
}