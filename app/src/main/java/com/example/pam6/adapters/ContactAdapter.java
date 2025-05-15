package com.example.pam6.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pam6.models.ContactEntity;
import com.example.pam6.firebase.FirebaseDatabaseHandler;
import com.example.pam6.R;
import com.example.pam6.activities.ViewContactActivity;

import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {
    private static final String TAG = "ContactAdapter";
    private Context context;
    private List<ContactEntity> contactList;
    private FirebaseDatabaseHandler firebaseHandler;
    private FirebaseDatabaseHandler.FirebaseCallbacks callbacks;

    public ContactAdapter(Context context, List<ContactEntity> contactList,
                          FirebaseDatabaseHandler.FirebaseCallbacks callbacks) {
        this.context = context;
        this.contactList = contactList;
        this.callbacks = callbacks;
        this.firebaseHandler = new FirebaseDatabaseHandler();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ContactEntity contact = contactList.get(position);

        if (contact.getName() != null && !contact.getName().isEmpty()) {
            holder.tvInitial.setText(contact.getName().substring(0, 1).toUpperCase());
        } else {
            holder.tvInitial.setText("?");
        }

        holder.tvName.setText(contact.getName());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ViewContactActivity.class);
            intent.putExtra("firebaseKey", contact.getFirebaseKey());
            intent.putExtra("name", contact.getName());
            intent.putExtra("phone", contact.getPhone());
            intent.putExtra("email", contact.getEmail());
            intent.putExtra("group", contact.getGroup());

            Log.d(TAG, "Clicking on contact: " + contact.getFirebaseKey() + ", " + contact.getName());
            context.startActivity(intent);
        });

        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Hapus Kontak")
                    .setMessage("Yakin ingin menghapus \"" + contact.getName() + "\"?")
                    .setPositiveButton("Ya", (dialog, which) -> {
                        final int contactPosition = holder.getAdapterPosition();
                        final ContactEntity contactToDelete = contactList.get(contactPosition);

                        firebaseHandler.deleteContact(contactToDelete, new FirebaseDatabaseHandler.FirebaseCallbacks() {
                            @Override
                            public void onContactsLoaded(List<ContactEntity> contacts) {}

                            @Override
                            public void onContactAdded(ContactEntity contact) {}

                            @Override
                            public void onContactUpdated(ContactEntity contact) {}

                            @Override
                            public void onContactDeleted(String deletedFirebaseKey) {  // Ganti int jadi String
                                if (contactPosition != RecyclerView.NO_POSITION && contactPosition < contactList.size()) {
                                    contactList.remove(contactPosition);
                                    notifyItemRemoved(contactPosition);
                                    notifyItemRangeChanged(contactPosition, contactList.size());
                                    Toast.makeText(context, "Kontak berhasil dihapus", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onError(String message) {
                                Toast.makeText(context, "Error deleting contact: " + message, Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Error deleting contact from Firebase: " + message);
                            }
                        });
                    })
                    .setNegativeButton("Batal", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public void updateContactList(List<ContactEntity> newContacts) {
        this.contactList.clear();
        this.contactList.addAll(newContacts);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvInitial, tvName;
        ImageView btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvInitial = itemView.findViewById(R.id.tv_initial);
            tvName = itemView.findViewById(R.id.tv_name);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}



















