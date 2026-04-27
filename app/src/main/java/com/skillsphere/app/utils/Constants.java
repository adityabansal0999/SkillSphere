package com.skillsphere.app.utils;

import java.util.Arrays;
import java.util.List;

public class Constants {

    // Firestore Collection Names
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_PROJECTS = "projects";
    public static final String COLLECTION_MESSAGES = "messages";
    public static final String COLLECTION_TASKS = "tasks";
    public static final String COLLECTION_REQUESTS = "requests";
    public static final String COLLECTION_NOTIFICATIONS = "notifications";

    // Firestore Sub-collections
    public static final String SUB_COLLECTION_ITEMS = "items";

    // SharedPreferences Keys
    public static final String PREF_NAME = "SkillSpherePrefs";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_USER_NAME = "userName";
    public static final String KEY_USER_EMAIL = "userEmail";
    public static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    // Intent Extra Keys
    public static final String EXTRA_PROJECT_ID = "projectId";
    public static final String EXTRA_PROJECT_TITLE = "projectTitle";
    public static final String EXTRA_USER_ID = "userId";
    public static final String EXTRA_USER_NAME = "userName";

    // Request Types
    public static final String REQUEST_TYPE_JOIN = "join_request";
    public static final String REQUEST_TYPE_INVITE = "invite";

    // Request Status
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_ACCEPTED = "accepted";
    public static final String STATUS_REJECTED = "rejected";

    // Project Status
    public static final String PROJECT_STATUS_FORMING = "forming";
    public static final String PROJECT_STATUS_ACTIVE = "active";
    public static final String PROJECT_STATUS_COMPLETED = "completed";
    public static final String PROJECT_STATUS_CANCELLED = "cancelled";

    // Project Visibility
    public static final String VISIBILITY_PUBLIC = "public";
    public static final String VISIBILITY_PRIVATE = "private";

    // Task Status
    public static final String TASK_STATUS_PENDING = "pending";
    public static final String TASK_STATUS_IN_PROGRESS = "in_progress";
    public static final String TASK_STATUS_DONE = "done";

    // Task Priority
    public static final String PRIORITY_LOW = "low";
    public static final String PRIORITY_MEDIUM = "medium";
    public static final String PRIORITY_HIGH = "high";

    // Notification Types
    public static final String NOTIF_TYPE_JOIN_REQUEST = "join_request";
    public static final String NOTIF_TYPE_INVITE = "invite";
    public static final String NOTIF_TYPE_ACCEPTED = "accepted";
    public static final String NOTIF_TYPE_REJECTED = "rejected";
    public static final String NOTIF_TYPE_MESSAGE = "message";
    public static final String NOTIF_TYPE_TASK_ASSIGNED = "task_assigned";

    // Message Types
    public static final String MESSAGE_TYPE_TEXT = "text";
    public static final String MESSAGE_TYPE_IMAGE = "image";

    // Roles
    public static final String ROLE_LEAD = "lead";
    public static final String ROLE_MEMBER = "member";

    // Categories
    public static final List<String> CATEGORIES = Arrays.asList(
            "AI/ML",
            "Web Dev",
            "Mobile",
            "Design",
            "IoT",
            "Security",
            "Cloud",
            "Data Science"
    );

    // Skills
    public static final List<String> SKILLS_LANGUAGES = Arrays.asList(
            "Python", "JavaScript", "Java", "C++", "Kotlin",
            "Swift", "Dart", "TypeScript"
    );

    public static final List<String> SKILLS_FRAMEWORKS = Arrays.asList(
            "React Native", "Flutter", "Vue.js", "Django",
            "FastAPI", "Next.js"
    );

    public static final List<String> SKILLS_TOOLS = Arrays.asList(
            "Firebase", "AWS", "Docker", "Git", "Linux"
    );

    public static final List<String> SKILLS_AI_ML = Arrays.asList(
            "TensorFlow", "NLP", "LangChain", "OpenCV"
    );

    public static final List<String> SKILLS_DESIGN = Arrays.asList(
            "Figma", "Adobe XD", "UI/UX Research"
    );

    public static final List<String> SKILLS_DATABASES = Arrays.asList(
            "MongoDB", "PostgreSQL", "MySQL", "SQLite"
    );

    // Project Icons (Emoji)
    public static final List<String> PROJECT_ICONS = Arrays.asList(
            "🤖", "💻", "🌐", "📊", "📱", "🛡️", "☁️", "🎨",
            "🔬", "🎮", "📚", "🏥", "🚗", "🍔", "🎵", "📷"
    );

    // Avatar Colors (for random assignment)
    public static final String[] AVATAR_COLORS = {
            "av-g", "av-b", "av-p", "av-o", "av-r", "av-t"
    };

    // Time Constants
    public static final long ONE_HOUR = 60 * 60 * 1000L;
    public static final long ONE_DAY = 24 * ONE_HOUR;
    public static final long ONE_WEEK = 7 * ONE_DAY;

    // Limits
    public static final int MAX_PROJECT_MEMBERS = 10;
    public static final int MIN_PROJECT_MEMBERS = 2;
    public static final int MAX_SKILLS_PER_USER = 15;
    public static final int MIN_SKILLS_PER_USER = 3;
    public static final int MAX_MESSAGE_LENGTH = 500;
    public static final int MAX_PROJECT_TITLE_LENGTH = 50;
    public static final int MAX_DESCRIPTION_LENGTH = 300;

    // File Upload
    public static final int MAX_IMAGE_SIZE_MB = 5;
    public static final String STORAGE_PATH_PROFILE = "profile_photos/";
    public static final String STORAGE_PATH_MESSAGES = "message_images/";
}