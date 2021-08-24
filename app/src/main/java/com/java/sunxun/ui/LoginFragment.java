package com.java.sunxun.ui;

import android.app.Activity;
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
import com.java.sunxun.databinding.FragmentLoginBinding;
import com.java.sunxun.models.User;

public class LoginFragment extends Fragment {

    @Nullable
    FragmentLoginBinding binding;

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
                binding.loginLoading.setVisibility(View.VISIBLE);
                User user = User.login(username.toString(), password.toString());
                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                        ((Activity) view.getContext()).runOnUiThread(() -> {
                            binding.loginLoading.setVisibility(View.GONE);
                            NavHostFragment.findNavController(this).navigateUp();
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        });

        binding.loginCancelButton.setOnClickListener(view -> NavHostFragment.findNavController(this).navigateUp());

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}