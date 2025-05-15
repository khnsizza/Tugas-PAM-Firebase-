package com.example.pam6.firebase;

import com.example.pam6.models.ContactEntity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class FirebaseDatabaseHandler {
    private final DatabaseReference contactsRef;

    public FirebaseDatabaseHandler() {
        contactsRef = FirebaseDatabase.getInstance().getReference("contacts");
    }

    public interface FirebaseCallbacks {
        void onContactsLoaded(List<ContactEntity> contacts);
        void onContactAdded(ContactEntity contact);
        void onContactUpdated(ContactEntity contact);
        void onContactDeleted(String deletedFirebaseKey);  // Ganti ke String
        void onError(String message);
    }

    public void addContact(ContactEntity contact, FirebaseCallbacks callback) {
        String key = contactsRef.push().getKey();
        if (key == null) {
            callback.onError("Unable to generate key for contact");
            return;
        }

        contact.setFirebaseKey(key);
        contactsRef.child(key).setValue(contact)
                .addOnSuccessListener(aVoid -> callback.onContactAdded(contact))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void updateContact(ContactEntity contact, FirebaseCallbacks callback) {
        if (contact.getFirebaseKey() == null || contact.getFirebaseKey().isEmpty()) {
            callback.onError("Contact key not found");
            return;
        }

        contactsRef.child(contact.getFirebaseKey()).setValue(contact)
                .addOnSuccessListener(aVoid -> callback.onContactUpdated(contact))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void deleteContact(ContactEntity contact, FirebaseCallbacks callback) {
        if (contact.getFirebaseKey() == null || contact.getFirebaseKey().isEmpty()) {
            callback.onError("Contact key not found");
            return;
        }

        contactsRef.child(contact.getFirebaseKey()).removeValue()
                .addOnSuccessListener(aVoid -> callback.onContactDeleted(contact.getFirebaseKey()))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void getAllContacts(FirebaseCallbacks callback) {
        contactsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<ContactEntity> contactList = new ArrayList<>();
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    ContactEntity contact = snapshot.getValue(ContactEntity.class);
                    if (contact != null) {
                        contact.setFirebaseKey(snapshot.getKey());
                        contactList.add(contact);
                    }
                }
                callback.onContactsLoaded(contactList);
            } else {
                callback.onError("Failed to load contacts: " + task.getException().getMessage());
            }
        });
    }
}
