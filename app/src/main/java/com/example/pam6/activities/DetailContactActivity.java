package com.example.pam6.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;


import androidx.appcompat.app.AppCompatActivity;

import com.example.pam6.models.ContactEntity;
import com.example.pam6.firebase.FirebaseDatabaseHandler;
import com.example.pam6.R;

import java.util.List;

public class DetailContactActivity extends AppCompatActivity implements FirebaseDatabaseHandler.FirebaseCallbacks {

    private FirebaseDatabaseHandler firebaseHandler;
    private EditText etName, etPhone, etEmail, etGroup;
    private Button btnSave, btnCancel, btnEdit;
    private TextView tvName, tvTitle;
    private boolean isEditing = false;
    private String firebaseKey = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_contact);

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            finish();
        });

        firebaseHandler = new FirebaseDatabaseHandler();

        etName = findViewById(R.id.et_name);
        etPhone = findViewById(R.id.et_phone);
        etEmail = findViewById(R.id.et_email);
        etGroup = findViewById(R.id.et_group);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);
        btnEdit = findViewById(R.id.btn_edit);
        tvName = findViewById(R.id.tv_name);
        tvTitle = findViewById(R.id.tv_title);

        firebaseKey = getIntent().getStringExtra("firebaseKey");

        if (firebaseKey != null) {
            loadContactData(firebaseKey);
            setupViewMode();
        } else {
            setupNewContactMode();
        }

        btnEdit.setOnClickListener(v -> setupEditMode());

        btnCancel.setOnClickListener(v -> {
            if (firebaseKey != null) {
                loadContactData(firebaseKey);
                setupViewMode();
            } else {
                finish();
            }
        });

        btnSave.setOnClickListener(v -> saveContactData());
    }


    // Override toolbar back button action
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void loadContactData(String key) {
        firebaseHandler.getAllContacts(new FirebaseDatabaseHandler.FirebaseCallbacks() {
            @Override
            public void onContactsLoaded(List<ContactEntity> contacts) {
                for (ContactEntity contact : contacts) {
                    if (key.equals(contact.getFirebaseKey())) {
                        runOnUiThread(() -> updateUIWithContactData(contact));
                        break;
                    }
                }
            }

            @Override public void onContactAdded(ContactEntity contact) {}
            @Override public void onContactUpdated(ContactEntity contact) {}
            @Override public void onContactDeleted(String deletedFirebaseKey) {}
            @Override public void onError(String message) {
                runOnUiThread(() -> Toast.makeText(DetailContactActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void updateUIWithContactData(ContactEntity contact) {
        etName.setText(contact.getName());
        etPhone.setText(contact.getPhone());
        etEmail.setText(contact.getEmail());
        etGroup.setText(contact.getGroup());
        tvName.setText(contact.getName());
        tvTitle.setText("Contact Details");
    }

    private void setupViewMode() {
        isEditing = false;
        etName.setEnabled(false);
        etPhone.setEnabled(false);
        etEmail.setEnabled(false);
        etGroup.setEnabled(false);

        btnSave.setVisibility(Button.GONE);
        btnCancel.setVisibility(Button.GONE);
        btnEdit.setVisibility(Button.VISIBLE);
    }

    private void setupEditMode() {
        isEditing = true;
        etName.setEnabled(true);
        etPhone.setEnabled(true);
        etEmail.setEnabled(true);
        etGroup.setEnabled(true);

        btnSave.setVisibility(Button.VISIBLE);
        btnCancel.setVisibility(Button.VISIBLE);
        btnEdit.setVisibility(Button.GONE);
        tvTitle.setText("Edit Contact");
    }

    private void setupNewContactMode() {
        isEditing = true;

        etName.setEnabled(true);
        etPhone.setEnabled(true);
        etEmail.setEnabled(true);
        etGroup.setEnabled(true);

        btnSave.setVisibility(Button.VISIBLE);
        btnCancel.setVisibility(Button.VISIBLE);
        btnEdit.setVisibility(Button.GONE);
        tvTitle.setText("Add New Contact");
        tvName.setVisibility(TextView.GONE);
    }

    private void saveContactData() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String group = etGroup.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Name is required");
            return;
        }
        if (phone.isEmpty()) {
            etPhone.setError("Phone is required");
            return;
        }

        ContactEntity contact = new ContactEntity(name, phone, email, group, firebaseKey);

        if (firebaseKey == null) {
            // Add new contact
            firebaseHandler.addContact(contact, new FirebaseDatabaseHandler.FirebaseCallbacks() {
                @Override
                public void onContactAdded(ContactEntity contact) {
                    runOnUiThread(() -> {
                        Toast.makeText(DetailContactActivity.this, "Contact added", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
                @Override public void onContactsLoaded(List<ContactEntity> contacts) {}
                @Override public void onContactUpdated(ContactEntity contact) {}
                @Override public void onContactDeleted(String deletedFirebaseKey) {}
                @Override public void onError(String message) {
                    runOnUiThread(() -> Toast.makeText(DetailContactActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show());
                }
            });
        } else {
            // Update contact
            firebaseHandler.updateContact(contact, new FirebaseDatabaseHandler.FirebaseCallbacks() {
                @Override
                public void onContactUpdated(ContactEntity contact) {
                    runOnUiThread(() -> {
                        Toast.makeText(DetailContactActivity.this, "Contact updated", Toast.LENGTH_SHORT).show();
                        setupViewMode();
                        updateUIWithContactData(contact);
                    });
                }
                @Override public void onContactsLoaded(List<ContactEntity> contacts) {}
                @Override public void onContactAdded(ContactEntity contact) {}
                @Override public void onContactDeleted(String deletedFirebaseKey) {}
                @Override public void onError(String message) {
                    runOnUiThread(() -> Toast.makeText(DetailContactActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show());
                }
            });
        }
    }

    @Override
    public void onContactsLoaded(List<ContactEntity> contacts) {}

    @Override
    public void onContactAdded(ContactEntity contact) {}

    @Override
    public void onContactUpdated(ContactEntity contact) {}

    @Override
    public void onContactDeleted(String deletedFirebaseKey) {}

    @Override
    public void onError(String message) {}
}

























