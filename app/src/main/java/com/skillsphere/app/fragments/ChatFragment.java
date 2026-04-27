package com.skillsphere.app.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.skillsphere.app.R;
import com.skillsphere.app.adapters.MessageAdapter;
import com.skillsphere.app.databinding.FragmentChatBinding;
import com.skillsphere.app.models.Message;
import com.skillsphere.app.utils.Constants;
import com.skillsphere.app.utils.SessionManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {

    private static final String ARG_PROJECT_ID = "project_id";
    private String projectId;
    private FragmentChatBinding binding;
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
        sessionManager = SessionManager.getInstance(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentChatBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(requireContext(), messageList, sessionManager.getUserId());

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setStackFromEnd(true);
        binding.rvMessages.setLayoutManager(layoutManager);
        binding.rvMessages.setAdapter(messageAdapter);

        binding.btnSend.setOnClickListener(v -> sendMessage());
        listenForMessages();
    }

    private void listenForMessages() {
        if (projectId == null) return;
        chatListener = db.collection(Constants.COLLECTION_PROJECTS)
                .document(projectId)
                .collection(Constants.COLLECTION_MESSAGES)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null || !isAdded()) return;
                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            Message message = dc.getDocument().toObject(Message.class);
                            message.setId(dc.getDocument().getId());
                            messageList.add(message);
                            messageAdapter.notifyItemInserted(messageList.size() - 1);
                            scrollToBottom();
                        }
                    }
                });
    }

    private void sendMessage() {
        String text = binding.etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;
        String userId = sessionManager.getUserId();
        String senderName = sessionManager.getUserName();
        Message message = new Message(userId, senderName, text);
        binding.etMessage.setText("");
        db.collection(Constants.COLLECTION_PROJECTS)
                .document(projectId)
                .collection(Constants.COLLECTION_MESSAGES)
                .add(message)
                .addOnFailureListener(ex -> {
                    if (isAdded())
                        Toast.makeText(requireContext(), "Failed to send message", Toast.LENGTH_SHORT).show();
                });
    }

    private void scrollToBottom() {
        if (!messageList.isEmpty())
            binding.rvMessages.smoothScrollToPosition(messageList.size() - 1);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (chatListener != null) chatListener.remove();
        binding = null;
    }
}
