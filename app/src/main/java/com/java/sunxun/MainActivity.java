package com.java.sunxun;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.iflytek.cloud.SpeechUtility;
import com.java.sunxun.dao.DetailCacheDB;
import com.java.sunxun.dao.SearchHistoryDB;
import com.java.sunxun.dao.TestHistoryDB;
import com.java.sunxun.databinding.ActivityMainBinding;
import com.java.sunxun.models.User;
import com.java.sunxun.network.ApplicationNetwork;
import com.java.sunxun.network.NetworkHandler;
import com.java.sunxun.utils.GlideEngine;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.listener.OnResultCallbackListener;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
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
        drawerUsernameText.setText(User.isVisitor() ? getString(R.string.please_login) : User.currentUser.getUsername());
        User.addOnUsernameChangedListener(name -> drawerUsernameText.setText(User.isVisitor() ? getString(R.string.please_login) : name));

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

            // 设置抽屉栏头像
            ImageView avatarImage = binding.navView.getHeaderView(0).findViewById(R.id.drawer_avatar_image);
            avatarImage.setOnClickListener(v -> {
                        if (User.isVisitor()) {
                            binding.mainDrawer.close();
                            navHostFragment.getNavController().navigate(R.id.nav_login);
                            return;
                        }
                        PictureSelector.create(this)
                                .openGallery(PictureMimeType.ofImage())
                                .imageEngine(GlideEngine.createGlideEngine())
                                .selectionMode(PictureConfig.SINGLE)
                                .isWeChatStyle(true)
                                .isCamera(true)
                                .isEnableCrop(true)
                                .withAspectRatio(1, 1)
                                .forResult(new OnResultCallbackListener<LocalMedia>() {
                                    @Override
                                    public void onResult(List<LocalMedia> result) {
                                        try {
                                            String path = result.get(0).getAndroidQToPath();
                                            String imageBase64;
                                            InputStream inputStream = null;
                                            try {
                                                inputStream = new FileInputStream(path);
                                                byte[] bytes = new byte[inputStream.available()];
                                                if (inputStream.read(bytes) == -1) throw new RuntimeException();
                                                imageBase64 = Base64.encodeToString(bytes, Base64.DEFAULT);
                                            } finally {
                                                try {
                                                    if (inputStream != null) inputStream.close();
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            String finalImageBase64 = imageBase64;
                                            ApplicationNetwork.modifyProfile(imageBase64, new NetworkHandler<Boolean>(MainActivity.this) {
                                                @Override
                                                public void onSuccess(Boolean result) {
                                                    Snackbar.make(binding.navView, R.string.avatar_upload_succeed, Snackbar.LENGTH_SHORT).show();
                                                    User.currentUser.setAvatar(finalImageBase64);
                                                    Glide.with(MainActivity.this)
                                                            .load(Base64.decode(finalImageBase64, Base64.DEFAULT))
                                                            .into(avatarImage);
                                                }

                                                @Override
                                                public void onError(Exception e) {
                                                    Snackbar.make(binding.navView, R.string.avatar_upload_fail, Snackbar.LENGTH_SHORT).show();
                                                    e.printStackTrace();
                                                }
                                            });
                                        } catch (Exception e) {
                                            Snackbar.make(binding.navView, R.string.avatar_upload_fail, Snackbar.LENGTH_SHORT).show();
                                            e.printStackTrace();
                                        }

                                    }

                                    @Override
                                    public void onCancel() {

                                    }
                                });
                    }
            );

            User.addOnAvatarChangedListener(result -> {
                if (result == null) {
                    avatarImage.setImageResource(R.drawable.avatar);
                } else {
                    Glide.with(MainActivity.this)
                            .load(Base64.decode(result, Base64.DEFAULT))
                            .into(avatarImage);
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
        ApplicationNetwork.getId(new NetworkHandler<String>(this) {
            @Override
            public void onSuccess(String result) {

            }

            @Override
            public void onError(Exception e) {
                Snackbar.make(binding.navView, R.string.login_edukg_failed, Snackbar.LENGTH_SHORT).show();
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

        // 初始化语音识别
        SpeechUtility.createUtility(this, "appid=9c3f825b");
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }

}