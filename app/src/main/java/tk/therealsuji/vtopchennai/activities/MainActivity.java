package tk.therealsuji.vtopchennai.activities;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.fragments.AssignmentsFragment;
import tk.therealsuji.vtopchennai.fragments.HomeFragment;
import tk.therealsuji.vtopchennai.fragments.PerformanceFragment;
import tk.therealsuji.vtopchennai.fragments.ProfileFragment;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;

    static final String HOME_FRAGMENT_TAG = "HOME_FRAGMENT_TAG";
    static final String PERFORMANCE_FRAGMENT_TAG = "PERFORMANCE_FRAGMENT_TAG";
    static final String ASSIGNMENTS_FRAGMENT_TAG = "ASSIGNMENTS_FRAGMENT_TAG";
    static final String PROFILE_FRAGMENT_TAG = "PROFILE_FRAGMENT_TAG";

    ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, R.string.permission_granted, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                }
            });

    public ActivityResultLauncher<String> getRequestPermissionLauncher() {
        return this.requestPermissionLauncher;
    }

    public void hideBottomNavigationView() {
        this.bottomNavigationView.clearAnimation();
        this.bottomNavigationView.post(() -> this.bottomNavigationView.animate().translationY(bottomNavigationView.getMeasuredHeight()));

        int gestureLeft = 0;

        if (Build.VERSION.SDK_INT >= 29) {
            gestureLeft = this.getWindow().getDecorView().getRootWindowInsets().getSystemGestureInsets().left;
        }

        if (gestureLeft == 0) {
            this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    public void showBottomNavigationView() {
        this.bottomNavigationView.clearAnimation();
        this.bottomNavigationView.post(() -> this.bottomNavigationView.animate().translationY(0));

        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        this.bottomNavigationView = findViewById(R.id.bottom_navigation);

        Bundle customInsets = new Bundle();
        customInsets.putInt("systemWindowInsetLeft", 0);
        customInsets.putInt("systemWindowInsetTop", 0);
        customInsets.putInt("systemWindowInsetRight", 0);
        customInsets.putInt("systemWindowInsetBottom", 0);
        customInsets.putInt("bottomNavigationHeight", 0);

        findViewById(R.id.frame_layout_fragment_container)
                .setOnApplyWindowInsetsListener((view, windowInsets) -> {
                    int systemWindowInsetLeft = windowInsets.getSystemWindowInsetLeft();
                    int systemWindowInsetTop = windowInsets.getSystemWindowInsetTop();
                    int systemWindowInsetRight = windowInsets.getSystemWindowInsetRight();
                    int systemWindowInsetBottom = windowInsets.getSystemWindowInsetBottom();

                    customInsets.putInt("systemWindowInsetLeft", systemWindowInsetLeft);
                    customInsets.putInt("systemWindowInsetTop", systemWindowInsetTop);
                    customInsets.putInt("systemWindowInsetRight", systemWindowInsetRight);
                    customInsets.putInt("systemWindowInsetBottom", systemWindowInsetBottom);

                    getSupportFragmentManager().setFragmentResult("customInsets", customInsets);

                    // Send the bottom navigation height to all fragments when ready
                    bottomNavigationView.post(() -> {
                        customInsets.putInt("bottomNavigationHeight", bottomNavigationView.getMeasuredHeight());
                        getSupportFragmentManager().setFragmentResult("customInsets", customInsets);
                    });

                    return windowInsets;
                });

        getSupportFragmentManager().setFragmentResultListener("bottomNavigationVisibility", this, (requestKey, result) -> {
            if (result.getBoolean("isVisible")) {
                this.showBottomNavigationView();
            } else {
                this.hideBottomNavigationView();
            }
        });

        this.bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment;
            String selectedFragmentTag;

            if (item.getItemId() == R.id.item_performance) {
                selectedFragmentTag = PERFORMANCE_FRAGMENT_TAG;
                selectedFragment = getSupportFragmentManager().findFragmentByTag(selectedFragmentTag);

                if (selectedFragment == null) {
                    selectedFragment = new PerformanceFragment();
                }
            } else if (item.getItemId() == R.id.item_assignments) {
                selectedFragmentTag = ASSIGNMENTS_FRAGMENT_TAG;
                selectedFragment = getSupportFragmentManager().findFragmentByTag(selectedFragmentTag);

                if (selectedFragment == null) {
                    selectedFragment = new AssignmentsFragment();
                }
            } else if (item.getItemId() == R.id.item_profile) {
                selectedFragmentTag = PROFILE_FRAGMENT_TAG;
                selectedFragment = getSupportFragmentManager().findFragmentByTag(selectedFragmentTag);

                if (selectedFragment == null) {
                    selectedFragment = new ProfileFragment();
                }
            } else {
                selectedFragmentTag = HOME_FRAGMENT_TAG;
                selectedFragment = getSupportFragmentManager().findFragmentByTag(selectedFragmentTag);

                if (selectedFragment == null) {
                    selectedFragment = new HomeFragment();
                }
            }

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame_layout_fragment_container, selectedFragment, selectedFragmentTag)
                    .commit();

            return true;
        });

        int selectedItem = R.id.item_home;

        if (savedInstanceState != null) {
            selectedItem = savedInstanceState.getInt("selectedItem");
        }

        this.bottomNavigationView.setSelectedItemId(selectedItem);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selectedItem", this.bottomNavigationView.getSelectedItemId());
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (this.bottomNavigationView.getTranslationY() != 0) {
            this.hideBottomNavigationView();
        }
    }
}