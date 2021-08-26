package com.java.sunxun.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.material.snackbar.Snackbar;
import com.java.sunxun.R;
import com.java.sunxun.databinding.FragmentLoginBinding;
import com.java.sunxun.models.User;
import com.java.sunxun.network.NetworkHandler;

public class LoginFragment extends Fragment {

    @Nullable
    private FragmentLoginBinding binding;

    private Fragment getThis() {
        return this;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);

        TextWatcher onTextChanged = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Editable username = binding.loginUsernameInput.getText();
                Editable password = binding.loginPasswordInput.getText();
                binding.loginActionButton.setEnabled(
                        username != null && !username.toString().isEmpty() &&
                                password != null && !password.toString().isEmpty()
                );
            }
        };

        binding.loginUsernameInput.addTextChangedListener(onTextChanged);
        binding.loginPasswordInput.addTextChangedListener(onTextChanged);

        binding.loginActionButton.setOnClickListener(view -> {
            Editable username = binding.loginUsernameInput.getText();
            Editable password = binding.loginPasswordInput.getText();
            if (username != null && password != null) {
                Snackbar.make(view, R.string.logging_in, Snackbar.LENGTH_SHORT).show();
                User.login(username.toString(), password.toString(), new NetworkHandler<User>(this) {
                    @Override
                    public void onSuccess(User user) {
                        new Thread(() -> {
                            try {
                                Thread.sleep(1000);
                                Activity activity = (Activity) view.getContext();
                                activity.runOnUiThread(() -> {
                                    activity.getSharedPreferences("credentials", Context.MODE_PRIVATE)
                                            .edit()
                                            .putString("username", user.getUsername())
                                            .putString("password", user.getPassword())
                                            .apply();
                                    NavHostFragment.findNavController(getThis()).navigateUp();
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }).start();
                    }

                    @Override
                    public void onError(Exception e) {
                        Snackbar.make(view, R.string.login_failure, Snackbar.LENGTH_LONG).show();
                    }
                });

            }
        });

        binding.loginAsVisitorText.setOnClickListener(view -> NavHostFragment.findNavController(this).navigateUp());

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}