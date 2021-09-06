package com.java.sunxun;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.java.sunxun.dao.DetailCacheDB;
import com.java.sunxun.dao.SearchHistoryDB;
import com.java.sunxun.dao.TestHistoryDB;
import com.java.sunxun.databinding.ActivityMainBinding;
import com.java.sunxun.models.User;
import com.java.sunxun.network.NetworkHandler;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 绑定布局
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 绑定应用工具栏
        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_home).build();

        // 设置抽屉顶部用户名，监听用户名改变事件
        TextView drawerUsernameText = binding.navView.getHeaderView(0).findViewById(R.id.drawer_username_text);
        drawerUsernameText.setText(User.currentUser.getUsername());
        User.addOnUsernameChangedListener(drawerUsernameText::setText);

        // 绑定导航
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            NavigationUI.setupWithNavController(binding.navView, navHostFragment.getNavController());
            // 点击游客用户名时跳转登录
            drawerUsernameText.setOnClickListener(view -> {
                if (User.isVisitor()) {
                    binding.mainDrawer.close();
                    navHostFragment.getNavController().navigate(R.id.nav_login);
                }
            });
        }

        // 从本地存储加载用户名和密码
        SharedPreferences sharedPreferences = getSharedPreferences("credentials", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", User.VISITOR.getUsername());
        String password = sharedPreferences.getString("password", User.VISITOR.getPassword());

        // 监听用户名变化，控制抽屉栏中用户相关选项的显示
        User.addOnUsernameChangedListener(user -> binding.navView.getMenu().setGroupVisible(R.id.menu_user_nav_group, !Objects.equals(user, User.VISITOR.getUsername())));

        // 尝试登录
        User.login(username, password, new NetworkHandler<User>(this) {
            @Override
            public void onSuccess(User result) {

            }

            @Override
            public void onError(Exception e) {

            }
        });

        // 绑定登出事件
        binding.navView.getMenu().findItem(R.id.nav_logout).setOnMenuItemClickListener(item -> {
            User.currentUser.logout();
            getSharedPreferences("credentials", Context.MODE_PRIVATE)
                    .edit()
                    .remove("username")
                    .remove("password")
                    .apply();
            return true;
        });

        // 初始化单例
        DetailCacheDB.init(this);
        SearchHistoryDB.init(this);
        TestHistoryDB.init(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }

}