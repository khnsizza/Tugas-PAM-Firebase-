package com.example.pam6.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pam6.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private SignInButton btnGoogleLogin;
    private TextView tvRegister, tvForgotPassword, tvTerms;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private final String TAG = "LoginActivity";
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "com.example.pam6.login";
    private static final String KEY_AUTO_LOGIN = "auto_login";

    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        // Google Sign In was successful, authenticate with Firebase
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                        firebaseAuthWithGoogle(account);
                    } catch (ApiException e) {
                        // Google Sign In failed, update UI appropriately
                        Log.w(TAG, "Google sign in failed", e);
                        Toast.makeText(this, "Google Sign-In failed: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        boolean autoLoginEnabled = sharedPreferences.getBoolean(KEY_AUTO_LOGIN, false);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize UI elements
        etEmail = findViewById(R.id.email);
        etPassword = findViewById(R.id.password);
        btnLogin = findViewById(R.id.loginButton);
        btnGoogleLogin = findViewById(R.id.btn_google_sign_in);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvTerms = findViewById(R.id.tvTerms);

        // Set text for Google Sign-In button
        btnGoogleLogin.setSize(SignInButton.SIZE_STANDARD);

        // Login with email button
        btnLogin.setOnClickListener(v -> loginWithEmail());

        // Google Sign-In button
        btnGoogleLogin.setOnClickListener(v -> signInWithGoogle());

        // Register button
        tvRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));

        // Forgot password
        tvForgotPassword.setOnClickListener(v -> {
            // You can implement password reset functionality here
            Toast.makeText(this, "Forgot password clicked", Toast.LENGTH_SHORT).show();
        });

        // Terms & Conditions
        tvTerms.setOnClickListener(v -> {
            // You can implement terms & conditions display here
            Toast.makeText(this, "Terms & Conditions clicked", Toast.LENGTH_SHORT).show();
        });

        // PERBAIKAN: Cek auto-login setelah UI sudah di-render
        if (autoLoginEnabled) {
            checkAutoLogin();
        } else {
            Log.d(TAG, "Auto-login disabled, showing login screen");
        }
    }

    // PERBAIKAN: Pindahkan logika auto-login ke method terpisah
    private void checkAutoLogin() {
        // Check if user is already logged in and auto-login is enabled
        // Try silent sign-in with Google
        GoogleSignInAccount lastAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (lastAccount != null) {
            Log.d(TAG, "Auto-login with Google account: " + lastAccount.getEmail());
            firebaseAuthWithGoogle(lastAccount);
            return;
        }

        // Check if Firebase user exists
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "Auto-login with Firebase: " + currentUser.getEmail());
            goToMain();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // PERBAIKAN: Hapus auto-login otomatis di onStart()
        // Ini menghindari login otomatis saat kembali ke Activity ini
    }

    // PERBAIKAN: Method untuk logout manual saat debugging
    public void logoutForDebugging() {
        // Sign out from Firebase
        FirebaseAuth.getInstance().signOut();

        // Sign out from Google
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            // Update shared preferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(KEY_AUTO_LOGIN, false);
            editor.apply();

            Log.d(TAG, "Logged out for debugging purposes");
        });
    }

    private void silentSignInWithGoogle() {
        Log.d(TAG, "Attempting silent sign-in with Google");
        mGoogleSignInClient.silentSignIn()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Successfully signed in with Google silently
                        GoogleSignInAccount account = task.getResult();
                        Log.d(TAG, "Silent sign-in successful: " + account.getEmail());
                        firebaseAuthWithGoogle(account);
                    } else {
                        Log.d(TAG, "Silent sign-in failed, will show login UI");
                        // Silent sign-in failed, we already showing the login UI
                    }
                });
    }

    private void loginWithEmail() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        // Show loading progress if needed
        // showProgressDialog();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    // hideProgressDialog();
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success");

                        // Enable auto-login for next time
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(KEY_AUTO_LOGIN, true);
                        editor.apply();

                        goToMain();
                    } else {
                        // If sign in fails, display a message to the user
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Authentication failed: " +
                                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signInWithGoogle() {
        // Show interactive Google Sign-In
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        // Show loading progress if needed
        // showProgressDialog();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    // hideProgressDialog();
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        Log.d(TAG, "Google sign in successful: " + user.getEmail());

                        // Enable auto-login for next time
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(KEY_AUTO_LOGIN, true);
                        editor.apply();

                        goToMain();
                    } else {
                        // If sign in fails, display a message to the user
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Authentication failed: " +
                                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void goToMain() {
        Intent intent = new Intent(this, ContactActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}



