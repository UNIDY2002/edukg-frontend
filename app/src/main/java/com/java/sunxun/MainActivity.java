package com.java.sunxun;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.snackbar.Snackbar;
import com.java.sunxun.databinding.ActivityMainBinding;
import com.java.sunxun.models.User;
import com.java.sunxun.network.NetworkHandler;
import com.java.sunxun.network.PlatformNetwork;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_home).build();

        TextView drawerUsernameText = binding.navView.getHeaderView(0).findViewById(R.id.drawer_username_text);
        drawerUsernameText.setText(User.currentUser.getUsername());
        User.addOnUsernameChangedListener(drawerUsernameText::setText);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            NavigationUI.setupWithNavController(binding.navView, navHostFragment.getNavController());
            drawerUsernameText.setOnClickListener(view -> {
                binding.mainDrawer.close();
                navHostFragment.getNavController().navigate(R.id.nav_login);
            });
        }

        PlatformNetwork.login("15800148446", "nmsl5201314", new NetworkHandler<String>(this) {
            @Override
            public void onSuccess(String result) {
                Snackbar.make(binding.navHostFragment, "Login successful, ID = " + result, Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }

}