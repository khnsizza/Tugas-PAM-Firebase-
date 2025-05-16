package com.example.pam6.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pam6.R;
import com.example.pam6.adapters.ContactAdapter;
import com.example.pam6.firebase.FirebaseDatabaseHandler;
import com.example.pam6.models.ContactEntity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class ContactActivity extends AppCompatActivity implements FirebaseDatabaseHandler.FirebaseCallbacks {

    private static final String TAG = "ContactActivity";
    private List<ContactEntity> contactList;
    private ContactAdapter contactAdapter;
    private FirebaseAuth mAuth;
    private FirebaseDatabaseHandler firebaseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        firebaseHandler = new FirebaseDatabaseHandler();

        contactList = new ArrayList<>();
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        contactAdapter = new ContactAdapter(this, contactList, this);
        recyclerView.setAdapter(contactAdapter);

        FloatingActionButton addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(ContactActivity.this, DetailContactActivity.class);
            startActivity(intent);
        });

        loadContactsFromFirebase();
    }

    private void loadContactsFromFirebase() {
        firebaseHandler.getAllContacts(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setSubtitle("Login as: " + currentUser.getEmail());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            mAuth.signOut();

            com.google.android.gms.auth.api.signin.GoogleSignInClient googleSignInClient =
                    com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(this,
                            new com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
                                    com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
                            ).build()
                    );

            googleSignInClient.signOut().addOnCompleteListener(this, task -> {
                Intent intent = new Intent(ContactActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });

            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onContactsLoaded(List<ContactEntity> contacts) {
        contactList.clear();
        contactList.addAll(contacts);
        contactAdapter.notifyDataSetChanged();
        Log.d(TAG, "Contacts loaded from Firebase: " + contacts.size());
    }

    @Override
    public void onContactAdded(ContactEntity contact) {
        loadContactsFromFirebase();
    }

    @Override
    public void onContactUpdated(ContactEntity contact) {
        loadContactsFromFirebase();
    }

    @Override
    public void onContactDeleted(String deletedFirebaseKey) {
        loadContactsFromFirebase();
        Toast.makeText(this, "Contact deleted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(String message) {
        Toast.makeText(this, "Firebase error: " + message, Toast.LENGTH_SHORT).show();
        Log.e(TAG, "Firebase error: " + message);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadContactsFromFirebase();
    }
}






