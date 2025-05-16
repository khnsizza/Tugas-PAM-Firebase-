package com.example.pam6.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pam6.models.ContactEntity;
import com.example.pam6.firebase.FirebaseDatabaseHandler;
import com.example.pam6.R;

import java.util.List;

public class ViewContactActivity extends AppCompatActivity implements FirebaseDatabaseHandler.FirebaseCallbacks {
    private static final String TAG = "ViewContactActivity";

    private TextView tvName, tvPhone, tvEmail, tvGroup;
    private Button btnHistory, btnStorage, btnEditContact;
    private ImageButton btnCall, btnSms, btnVideo;
    private ImageView backButton;

    private String firebaseKey;
    private FirebaseDatabaseHandler firebaseHandler;

    private final ActivityResultLauncher<Intent> editContactLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    boolean contactUpdated = result.getData().getBooleanExtra("contactUpdated", false);
                    Log.d(TAG, "Contact updated: " + contactUpdated);
                    if (contactUpdated) {
                        refreshContactData();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_contact);

        firebaseHandler = new FirebaseDatabaseHandler();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        tvName = findViewById(R.id.tv_name);
        tvPhone = findViewById(R.id.tv_phone);

        btnCall = findViewById(R.id.btn_call);
        btnSms = findViewById(R.id.btn_sms);
        btnVideo = findViewById(R.id.btn_video);
        btnHistory = findViewById(R.id.btn_history);
        btnStorage = findViewById(R.id.btn_storage);
        btnEditContact = findViewById(R.id.btn_edit_contact);

        backButton = findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        firebaseKey = getIntent().getStringExtra("firebaseKey");
        if (firebaseKey == null) {
            Toast.makeText(this, "Error: Contact key not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        refreshContactData();

        btnCall.setOnClickListener(v -> Toast.makeText(this, "Call button clicked", Toast.LENGTH_SHORT).show());
        btnSms.setOnClickListener(v -> Toast.makeText(this, "SMS button clicked", Toast.LENGTH_SHORT).show());
        btnVideo.setOnClickListener(v -> Toast.makeText(this, "Video call button clicked", Toast.LENGTH_SHORT).show());
        btnHistory.setOnClickListener(v -> Toast.makeText(this, "History button clicked", Toast.LENGTH_SHORT).show());
        btnStorage.setOnClickListener(v -> Toast.makeText(this, "Storage button clicked", Toast.LENGTH_SHORT).show());

        btnEditContact.setOnClickListener(v -> {
            Intent intent = new Intent(ViewContactActivity.this, DetailContactActivity.class);
            intent.putExtra("firebaseKey", firebaseKey);
            editContactLauncher.launch(intent);
        });
    }

    private void refreshContactData() {
        firebaseHandler.getAllContacts(this);
    }

    private void updateUI(ContactEntity contact) {
        runOnUiThread(() -> {
            tvName.setText(contact.getName());
            tvPhone.setText("Ponsel " + contact.getPhone());
            if (tvEmail != null) {
                tvEmail.setText("Email: " + contact.getEmail());
            }
            if (tvGroup != null) {
                tvGroup.setText("Group: " + contact.getGroup());
            }
            Log.d(TAG, "Contact data updated: " + contact.getName());
        });
    }

    @Override
    public void onContactsLoaded(List<ContactEntity> contacts) {
        for (ContactEntity contact : contacts) {
            if (firebaseKey.equals(contact.getFirebaseKey())) {
                updateUI(contact);
                break;
            }
        }
    }

    @Override public void onContactAdded(ContactEntity contact) {}

    @Override public void onContactUpdated(ContactEntity contact) {}

    @Override
    public void onContactDeleted(String deletedFirebaseKey) {
        if (deletedFirebaseKey.equals(firebaseKey)) {
            Toast.makeText(this, "This contact has been deleted", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onError(String message) {
        Toast.makeText(this, "Error refreshing contact: " + message, Toast.LENGTH_SHORT).show();
        Log.e(TAG, "Firebase error: " + message);
    }
}

















