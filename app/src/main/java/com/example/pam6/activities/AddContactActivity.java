package com.example.pam6.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pam6.models.ContactEntity;
import com.example.pam6.firebase.FirebaseDatabaseHandler;
import com.example.pam6.R;

import java.util.List;

public class AddContactActivity extends AppCompatActivity implements FirebaseDatabaseHandler.FirebaseCallbacks {
    private static final String TAG = "AddContactActivity";
    private FirebaseDatabaseHandler firebaseHandler;
    private EditText etName, etPhone, etEmail, etGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_contact);

        firebaseHandler = new FirebaseDatabaseHandler();

        TextView tvTitle = findViewById(R.id.tv_email);
        etName = findViewById(R.id.et_name);
        etPhone = findViewById(R.id.et_phone);
        etEmail = findViewById(R.id.et_email);
        etGroup = findViewById(R.id.et_group);
        Button btnSave = findViewById(R.id.btn_save);
        Button btnCancel = findViewById(R.id.btn_cancel);
        Button btnEdit = findViewById(R.id.btn_edit);
        TextView tvName = findViewById(R.id.tv_name);

        tvTitle.setText("Add New Contact");
        btnEdit.setVisibility(android.view.View.GONE);
        tvName.setVisibility(android.view.View.GONE);
        btnSave.setVisibility(android.view.View.VISIBLE);
        btnCancel.setVisibility(android.view.View.VISIBLE);

        etName.setEnabled(true);
        etPhone.setEnabled(true);
        etEmail.setEnabled(true);
        etGroup.setEnabled(true);

        etName.setText("");
        etPhone.setText("");
        etEmail.setText("");
        etGroup.setText("");

        btnSave.setOnClickListener(v -> {
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

            ContactEntity contact = new ContactEntity();
            contact.setName(name);
            contact.setPhone(phone);
            contact.setEmail(email);
            contact.setGroup(group);

            Log.d(TAG, "Saving new contact: " + name);
            firebaseHandler.addContact(contact, this);
        });

        btnCancel.setOnClickListener(v -> finish());
    }

    @Override
    public void onContactsLoaded(List<ContactEntity> contacts) {
        // unused
    }

    @Override
    public void onContactAdded(ContactEntity contact) {
        runOnUiThread(() -> {
            Toast.makeText(this, "Contact successfully added", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    @Override
    public void onContactUpdated(ContactEntity contact) {
        // unused
    }

    @Override
    public void onContactDeleted(String deletedFirebaseKey) {
        // unused
    }

    @Override
    public void onError(String message) {
        runOnUiThread(() -> {
            Toast.makeText(this, "Failed to add contact: " + message, Toast.LENGTH_SHORT).show();
        });
    }
}











