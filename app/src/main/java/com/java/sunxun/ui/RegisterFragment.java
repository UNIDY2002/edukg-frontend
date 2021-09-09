package com.java.sunxun.ui;

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
import com.java.sunxun.databinding.FragmentRegisterBinding;
import com.java.sunxun.exceptions.ApplicationRegisterCollisionException;
import com.java.sunxun.models.User;
import com.java.sunxun.network.NetworkHandler;

public class RegisterFragment extends Fragment {

    @Nullable
    private FragmentRegisterBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);

        TextWatcher onTextChanged = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Editable username = binding.registerUsernameInput.getText();
                Editable password = binding.registerPasswordInput.getText();
                Editable confirm = binding.registerConfirmInput.getText();
                binding.registerActionButton.setEnabled(
                        username != null && !username.toString().isEmpty() &&
                                password != null && !password.toString().isEmpty() &&
                                confirm != null && confirm.toString().equals(password.toString())
                );
            }
        };

        binding.registerUsernameInput.addTextChangedListener(onTextChanged);
        binding.registerPasswordInput.addTextChangedListener(onTextChanged);
        binding.registerConfirmInput.addTextChangedListener(onTextChanged);

        binding.registerActionButton.setOnClickListener(view -> {
            Editable username = binding.registerUsernameInput.getText();
            Editable password = binding.registerPasswordInput.getText();
            if (username != null && password != null) {
                Snackbar.make(view, R.string.register_processing, Snackbar.LENGTH_SHORT).show();
                User.register(username.toString(), password.toString(), new NetworkHandler<User>(this) {
                    @Override
                    public void onSuccess(User user) {
                        Snackbar.make(view, R.string.register_succeed, Snackbar.LENGTH_SHORT).show();
                        view.getContext().getSharedPreferences("credentials", Context.MODE_PRIVATE)
                                .edit()
                                .putString("username", user.getUsername())
                                .putString("password", user.getPassword())
                                .apply();
                        NavHostFragment.findNavController(RegisterFragment.this).navigateUp();
                        NavHostFragment.findNavController(RegisterFragment.this).navigateUp();
                    }

                    @Override
                    public void onError(Exception e) {
                        if (e instanceof ApplicationRegisterCollisionException) {
                            Snackbar.make(view, R.string.register_collide, Snackbar.LENGTH_SHORT).show();
                        } else {
                            e.printStackTrace();
                            Snackbar.make(view, R.string.register_failure, Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        binding.registerBack.setOnClickListener(view -> NavHostFragment.findNavController(this).navigateUp());

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}