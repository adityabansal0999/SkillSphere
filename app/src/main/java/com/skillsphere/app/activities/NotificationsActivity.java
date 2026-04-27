package com.skillsphere.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.skillsphere.app.adapters.NotificationAdapter;
import com.skillsphere.app.databinding.ActivityNotificationsBinding;
import com.skillsphere.app.models.Notification;
import com.skillsphere.app.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity implements NotificationAdapter.OnNotificationActionListener {

    private ActivityNotificationsBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private NotificationAdapter adapter;
    private List<Notification> notifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        notifications = new ArrayList<>();
        adapter = new NotificationAdapter(this, notifications, this);

        binding.rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        binding.rvNotifications.setAdapter(adapter);

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnMarkAllRead.setOnClickListener(v -> markAllRead());

        loadNotifications();
    }

    private void loadNotifications() {
        if (auth.getCurrentUser() == null) return;
        String userId = auth.getCurrentUser().getUid();

        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection(Constants.COLLECTION_NOTIFICATIONS)
                .document(userId)
                .collection(Constants.SUB_COLLECTION_ITEMS)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    binding.progressBar.setVisibility(View.GONE);
                    notifications.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Notification notif = doc.toObject(Notification.class);
                        notif.setId(doc.getId());
                        notifications.add(notif);
                    }
                    adapter.notifyDataSetChanged();
                    if (notifications.isEmpty()) {
                        binding.tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        binding.tvEmpty.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load notifications", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onAccept(Notification notification) {
        if (notification.getRequestId() == null) return;

        // Accept: update request status + add user to project members
        String requestId = notification.getRequestId();
        db.collection(Constants.COLLECTION_REQUESTS).document(requestId).get()
                .addOnSuccessListener(doc -> {
                    String fromUserId = doc.getString("fromUserId");
                    String projectId = doc.getString("projectId");
                    if (fromUserId == null || projectId == null) return;

                    // Update request to accepted
                    db.collection(Constants.COLLECTION_REQUESTS).document(requestId)
                            .update("status", Constants.STATUS_ACCEPTED,
                                    "respondedAt", System.currentTimeMillis());

                    // Add user to project members
                    db.collection(Constants.COLLECTION_PROJECTS).document(projectId)
                            .update("members", FieldValue.arrayUnion(fromUserId));

                    // Mark notification as read
                    markNotificationRead(notification);

                    Toast.makeText(this, "Request accepted!", Toast.LENGTH_SHORT).show();
                    loadNotifications();
                });
    }

    @Override
    public void onReject(Notification notification) {
        if (notification.getRequestId() == null) return;

        db.collection(Constants.COLLECTION_REQUESTS).document(notification.getRequestId())
                .update("status", Constants.STATUS_REJECTED,
                        "respondedAt", System.currentTimeMillis())
                .addOnSuccessListener(aVoid -> {
                    markNotificationRead(notification);
                    Toast.makeText(this, "Request rejected", Toast.LENGTH_SHORT).show();
                    loadNotifications();
                });
    }

    private void markNotificationRead(Notification notification) {
        if (auth.getCurrentUser() == null) return;
        String userId = auth.getCurrentUser().getUid();
        db.collection(Constants.COLLECTION_NOTIFICATIONS)
                .document(userId)
                .collection(Constants.SUB_COLLECTION_ITEMS)
                .document(notification.getId())
                .update("isRead", true);
    }

    private void markAllRead() {
        if (auth.getCurrentUser() == null) return;
        String userId = auth.getCurrentUser().getUid();
        for (Notification notif : notifications) {
            if (!notif.isRead()) {
                db.collection(Constants.COLLECTION_NOTIFICATIONS)
                        .document(userId)
                        .collection(Constants.SUB_COLLECTION_ITEMS)
                        .document(notif.getId())
                        .update("isRead", true);
                notif.setRead(true);
            }
        }
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "All marked as read", Toast.LENGTH_SHORT).show();
    }
}
