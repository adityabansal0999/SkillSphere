package com.skillsphere.app.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.skillsphere.app.R;
import com.skillsphere.app.adapters.TaskAdapter;
import com.skillsphere.app.databinding.FragmentTasksBinding;
import com.skillsphere.app.models.Task;
import com.skillsphere.app.utils.Constants;
import com.skillsphere.app.utils.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class TasksFragment extends Fragment implements TaskAdapter.OnTaskCheckedListener {

    private static final String ARG_PROJECT_ID = "project_id";
    private String projectId;
    private FragmentTasksBinding binding;
    private FirebaseFirestore db;
    private SessionManager sessionManager;
    private TaskAdapter taskAdapter;
    private List<Task> taskList;
    private ListenerRegistration tasksListener;

    public static TasksFragment newInstance(String projectId) {
        TasksFragment fragment = new TasksFragment();
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
        binding = FragmentTasksBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(requireContext(), taskList, this);

        binding.rvTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTasks.setAdapter(taskAdapter);

        binding.fabAddTask.setOnClickListener(v -> showAddTaskDialog());
        startListening();
    }

    private void startListening() {
        if (projectId == null) return;
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.tvEmpty.setVisibility(View.GONE);

        tasksListener = db.collection(Constants.COLLECTION_PROJECTS)
                .document(projectId)
                .collection(Constants.COLLECTION_TASKS)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((querySnapshot, e) -> {
                    if (!isAdded()) return;
                    binding.progressBar.setVisibility(View.GONE);
                    if (e != null) {
                        Toast.makeText(requireContext(), "Error loading tasks", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (querySnapshot != null) {
                        taskList.clear();
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            Task task = doc.toObject(Task.class);
                            task.setId(doc.getId());
                            taskList.add(task);
                        }
                        taskAdapter.notifyDataSetChanged();
                        binding.tvEmpty.setVisibility(taskList.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });
    }

    private void showAddTaskDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_task, null);
        EditText etTitle = dialogView.findViewById(R.id.et_task_title);
        EditText etDescription = dialogView.findViewById(R.id.et_task_description);
        EditText etAssignee = dialogView.findViewById(R.id.et_task_assignee);

        new AlertDialog.Builder(requireContext())
                .setTitle("Add New Task")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String description = etDescription.getText().toString().trim();
                    String assignee = etAssignee.getText().toString().trim();
                    if (TextUtils.isEmpty(title)) {
                        Toast.makeText(requireContext(), "Task title is required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    createTask(title, description, assignee);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void createTask(String title, String description, String assigneeName) {
        Task task = new Task(projectId, title, sessionManager.getUserId(), assigneeName);
        task.setDescription(description);
        task.setCreatedBy(sessionManager.getUserId());
        task.setAssignedName(assigneeName);
        task.setStatus(Constants.TASK_STATUS_PENDING);
        task.setCreatedAt(System.currentTimeMillis());

        db.collection(Constants.COLLECTION_PROJECTS)
                .document(projectId)
                .collection(Constants.COLLECTION_TASKS)
                .add(task)
                .addOnSuccessListener(docRef -> {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(), "Task added!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    if (isAdded())
                        Toast.makeText(requireContext(), "Failed to add task", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onTaskChecked(Task task, boolean isDone, int position) {
        String newStatus = isDone ? Constants.TASK_STATUS_DONE : Constants.TASK_STATUS_PENDING;
        db.collection(Constants.COLLECTION_PROJECTS)
                .document(projectId)
                .collection(Constants.COLLECTION_TASKS)
                .document(task.getId())
                .update("status", newStatus)
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        taskAdapter.notifyItemChanged(position);
                        Toast.makeText(requireContext(), "Failed to update task", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (tasksListener != null) {
            tasksListener.remove();
        }
        binding = null;
    }
}
