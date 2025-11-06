package com.example.prm_be.utils;

import android.content.Intent;
import android.util.Log;

import com.example.prm_be.data.FirebaseRepo;
import com.example.prm_be.data.models.User;
import com.example.prm_be.ui.admin.AdminDashboardActivity;
import com.example.prm_be.ui.discovery.HomeActivity;
import com.example.prm_be.ui.staff.StaffHomeActivity;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Utility class để kiểm tra và điều hướng theo role
 * Chức năng:
 * - Kiểm tra role của user trước khi vào màn hình
 * - Điều hướng user đến màn hình phù hợp với role
 * - Chặn user không có quyền truy cập màn hình
 */
public class RoleGuard {
    private static final String TAG = "RoleGuard";

    /**
     * Kiểm tra role và điều hướng đến màn hình phù hợp
     * @param activity Activity hiện tại (thường là LoginActivity hoặc SplashActivity)
     * @param user User object từ Firestore
     */
    public static void navigateByRole(AppCompatActivity activity, User user) {
        if (user == null) {
            Log.w(TAG, "User is null, navigating to HomeActivity");
            activity.startActivity(new Intent(activity, HomeActivity.class));
            activity.finish();
            return;
        }

        String role = user.getRole();
        String status = user.getStatus();

        // Check if user is disabled
        if (status != null && "disabled".equals(status.toLowerCase())) {
            Log.w(TAG, "User is disabled, staying on login screen");
            FirebaseRepo.getInstance().logout();
            return;
        }

        // Navigate based on role
        if (role == null || role.isEmpty() || "user".equals(role.toLowerCase())) {
            Log.d(TAG, "Navigating to HomeActivity (user role)");
            activity.startActivity(new Intent(activity, HomeActivity.class));
            activity.finish();
        } else if ("staff".equals(role.toLowerCase())) {
            Log.d(TAG, "Navigating to StaffHomeActivity (staff role)");
            activity.startActivity(new Intent(activity, StaffHomeActivity.class));
            activity.finish();
        } else if ("admin".equals(role.toLowerCase())) {
            Log.d(TAG, "Navigating to AdminDashboardActivity (admin role)");
            activity.startActivity(new Intent(activity, AdminDashboardActivity.class));
            activity.finish();
        } else {
            Log.w(TAG, "Unknown role: " + role + ", navigating to HomeActivity");
            activity.startActivity(new Intent(activity, HomeActivity.class));
            activity.finish();
        }
    }

    /**
     * Kiểm tra xem user hiện tại có role hợp lệ để truy cập activity không
     * @param activity Activity cần check
     * @param requiredRole Role yêu cầu ("admin", "staff", hoặc null nếu user thường)
     * @return true nếu có quyền, false nếu không
     */
    public static boolean checkRole(AppCompatActivity activity, String requiredRole) {
        FirebaseRepo repo = FirebaseRepo.getInstance();
        
        if (!repo.isUserLoggedIn()) {
            Log.w(TAG, "User not logged in, redirecting to login");
            redirectToLogin(activity);
            return false;
        }

        // If no role required, allow access
        if (requiredRole == null || requiredRole.isEmpty()) {
            return true;
        }

        // Get current user
        com.google.firebase.auth.FirebaseUser firebaseUser = repo.getCurrentUser();
        if (firebaseUser == null) {
            redirectToLogin(activity);
            return false;
        }

        // Get user document from Firestore
        repo.getUser(firebaseUser.getUid(), new FirebaseRepo.FirebaseCallback<User>() {
            @Override
            public void onSuccess(User user) {
                String userRole = user.getRole();
                String userStatus = user.getStatus();

                // Check if disabled
                if (userStatus != null && "disabled".equals(userStatus.toLowerCase())) {
                    Log.w(TAG, "User is disabled");
                    FirebaseRepo.getInstance().logout();
                    redirectToLogin(activity);
                    return;
                }

                // Check role match
                if (userRole == null || userRole.isEmpty()) {
                    userRole = "user";
                }

                if (!requiredRole.toLowerCase().equals(userRole.toLowerCase())) {
                    Log.w(TAG, "Role mismatch. Required: " + requiredRole + ", User: " + userRole);
                    redirectToHome(activity);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error checking user role", e);
                redirectToHome(activity);
            }
        });

        return true; // Temporarily return true, async check will redirect if needed
    }

    /**
     * Check role synchronously (for immediate check)
     */
    public static boolean checkRoleSync(AppCompatActivity activity, String requiredRole) {
        FirebaseRepo repo = FirebaseRepo.getInstance();
        
        if (!repo.isUserLoggedIn()) {
            redirectToLogin(activity);
            return false;
        }

        // For sync check, we'll allow access and do async check in onCreate
        // This prevents blocking the UI thread
        return true;
    }

    private static void redirectToLogin(AppCompatActivity activity) {
        Intent intent = new Intent(activity, com.example.prm_be.ui.auth.LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    private static void redirectToHome(AppCompatActivity activity) {
        Intent intent = new Intent(activity, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    /**
     * Kiểm tra user có phải là user thường không (chặn staff/admin)
     * Dùng cho các màn hình chỉ dành cho khách hàng (HomeActivity, BookingActivity, ProfileActivity)
     * @param activity Activity cần check
     * @return true nếu là user thường, false nếu là staff/admin (sẽ redirect)
     */
    public static boolean checkUserRoleOnly(AppCompatActivity activity) {
        FirebaseRepo repo = FirebaseRepo.getInstance();
        
        if (!repo.isUserLoggedIn()) {
            redirectToLogin(activity);
            return false;
        }

        com.google.firebase.auth.FirebaseUser firebaseUser = repo.getCurrentUser();
        if (firebaseUser == null) {
            redirectToLogin(activity);
            return false;
        }

        // Get user document from Firestore
        repo.getUser(firebaseUser.getUid(), new FirebaseRepo.FirebaseCallback<User>() {
            @Override
            public void onSuccess(User user) {
                String userRole = user.getRole();
                String userStatus = user.getStatus();

                // Check if disabled
                if (userStatus != null && "disabled".equals(userStatus.toLowerCase())) {
                    Log.w(TAG, "User is disabled");
                    FirebaseRepo.getInstance().logout();
                    redirectToLogin(activity);
                    return;
                }

                // Check if user role is "user" (not staff/admin)
                if (userRole == null || userRole.isEmpty()) {
                    userRole = "user";
                }

                if (!"user".equals(userRole.toLowerCase())) {
                    Log.w(TAG, "User is not regular user, role: " + userRole + ", redirecting to appropriate dashboard");
                    // Redirect to appropriate dashboard
                    if ("staff".equals(userRole.toLowerCase())) {
                        Intent intent = new Intent(activity, StaffHomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        activity.startActivity(intent);
                        activity.finish();
                    } else if ("admin".equals(userRole.toLowerCase())) {
                        Intent intent = new Intent(activity, AdminDashboardActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        activity.startActivity(intent);
                        activity.finish();
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error checking user role", e);
                // If can't get user from Firestore, assume it's a regular user (default role)
                // Don't redirect to login, allow access to user screens
                Log.w(TAG, "Could not fetch user document, assuming regular user role");
                // Allow access, don't redirect
            }
        });

        return true; // Temporarily return true, async check will redirect if needed
    }

    /**
     * Check user role only synchronously (for immediate check in onCreate)
     */
    public static boolean checkUserRoleOnlySync(AppCompatActivity activity) {
        FirebaseRepo repo = FirebaseRepo.getInstance();
        
        if (!repo.isUserLoggedIn()) {
            redirectToLogin(activity);
            return false;
        }

        // For sync check, we'll allow access and do async check in onCreate
        // This prevents blocking the UI thread
        return true;
    }
}

