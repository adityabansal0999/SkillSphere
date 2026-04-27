package com.skillsphere.app.utils;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.skillsphere.app.models.Project;
import com.skillsphere.app.models.User;

import java.util.Arrays;
import java.util.Collections;

public class MockDataGenerator {

    public static void generateMockData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        WriteBatch batch = db.batch();

        // 1. Mock Users
        User u1 = new User("mock_user_1", "Sarah Jenkins", "sarah@university.edu");
        u1.setDepartment("Computer Science");
        u1.setYear("3rd Year");
        u1.setSkills(Arrays.asList("Java", "Android", "Firebase"));
        u1.setAppearInDiscover(true);
        u1.setBio("Passionate about mobile development and UI/UX design.");

        User u2 = new User("mock_user_2", "David Chen", "david@university.edu");
        u2.setDepartment("Artificial Intelligence");
        u2.setYear("4th Year");
        u2.setSkills(Arrays.asList("Python", "PyTorch", "Data Science"));
        u2.setAppearInDiscover(true);
        u2.setBio("Working on deep learning projects for healthcare.");

        User u3 = new User("mock_user_3", "Emily Rodriguez", "emily@university.edu");
        u3.setDepartment("Design");
        u3.setYear("2nd Year");
        u3.setSkills(Arrays.asList("Figma", "Adobe XD", "UI Design"));
        u3.setAppearInDiscover(true);
        u3.setBio("Helping teams build beautiful and intuitive interfaces.");

        batch.set(db.collection(Constants.COLLECTION_USERS).document(u1.getId()), u1);
        batch.set(db.collection(Constants.COLLECTION_USERS).document(u2.getId()), u2);
        batch.set(db.collection(Constants.COLLECTION_USERS).document(u3.getId()), u3);

        // 2. Mock Projects
        Project p1 = new Project("mock_proj_1", "HealthTrack App", "A fitness tracking application with social features.", u1.getId(), Arrays.asList("Android", "Java"), "Mobile");
        p1.setLeadName(u1.getName());
        p1.setMaxMembers(4);
        p1.setVisibility("public");
        p1.setStatus("forming");
        p1.setCreatedAt(System.currentTimeMillis() - 86400000L); // 1 day ago

        Project p2 = new Project("mock_proj_2", "AI Study Buddy", "Intelligent chatbot to help students organize study materials.", u2.getId(), Arrays.asList("Python", "NLP"), "AI/ML");
        p2.setLeadName(u2.getName());
        p2.setMaxMembers(3);
        p2.setVisibility("public");
        p2.setStatus("active");
        p2.setCreatedAt(System.currentTimeMillis() - 172800000L); // 2 days ago

        Project p3 = new Project("mock_proj_3", "Campus Food Finder", "Real-time locator for food stalls and student discounts.", u3.getId(), Arrays.asList("React Native", "Node.js"), "Web/Mobile");
        p3.setLeadName(u3.getName());
        p3.setMaxMembers(5);
        p3.setVisibility("public");
        p3.setStatus("forming");
        p3.setCreatedAt(System.currentTimeMillis());

        batch.set(db.collection(Constants.COLLECTION_PROJECTS).document(p1.getId()), p1);
        batch.set(db.collection(Constants.COLLECTION_PROJECTS).document(p2.getId()), p2);
        batch.set(db.collection(Constants.COLLECTION_PROJECTS).document(p3.getId()), p3);

        batch.commit();
    }
}
