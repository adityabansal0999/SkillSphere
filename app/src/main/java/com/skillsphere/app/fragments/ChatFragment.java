package com.example.skillsphere.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skillsphere.R;
import com.skillsphere.app.adapters.MessageAdapter;
import com.example.skillsphere.models.Message;
import com.example.skillsphere.utils.Constants;
import com.example.skillsphere.utils.SessionManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatFragment extends Fragment {

    private static final String ARG_PROJECT_ID = "project_id";

    private String projectId;
    private RecyclerView rvMessages;
    private EditText etMessage;
    private ImageButton btnSend;

    private FirebaseFirestore db;
    private SessionManager sessionManager;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private ListenerRegistration chatListener;

    public static ChatFragment newInstance(String projectId) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PROJECT_ID, projectId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            projectId = getArguments().getString(ARG_PROJECT_ID);
        }
        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvMessages = view.findViewById(R.id.rv_messages);
        etMessage = view.findViewById(R.id.et_message);
        btnSend = view.findViewById(R.id.btn_send);

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(requireContext(), messageList, sessionManager.getUserId());

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(messageAdapter);

        btnSend.setOnClickListener(v -> sendMessage());

        listenForMessages();
    }

    private void listenForMessages() {
        if (projectId == null) return;

        chatListener = db.collection(Constants.COLLECTION_PROJECTS)
                .document(projectId)
                .collection(Constants.COLLECTION_MESSAGES)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null || snapshots == null || !isAdded()) return;

                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                Message message = dc.getDocument().toObject(Message.class);
                                message.setMessageId(dc.getDocument().getId());
                                messageList.add(message);
                                messageAdapter.notifyItemInserted(messageList.size() - 1);
                                scrollToBottom();
                            }
                        }
                    }
                });
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        String userId = sessionManager.getUserId();
        String senderName = sessionManager.getUserName();

        Message message = new Message();
        message.setSenderId(userId);
        message.setSenderName(senderName);
        message.setText(text);
        message.setTimestamp(new Date());

        etMessage.setText("");

        db.collection(Constants.COLLECTION_PROJECTS)
                .document(projectId)
                .collection(Constants.COLLECTION_MESSAGES)
                .add(message)
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Failed to send message", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void scrollToBottom() {
        if (messageList.size() > 0) {
            rvMessages.smoothScrollToPosition(messageList.size() - 1);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (chatListener != null) {
            chatListener.remove();
        }
    }
}