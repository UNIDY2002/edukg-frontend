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
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.material.snackbar.Snackbar;
import com.java.sunxun.R;
import com.java.sunxun.databinding.FragmentModifyPasswordBinding;
import com.java.sunxun.exceptions.InvalidCodeException;
import com.java.sunxun.models.User;
import com.java.sunxun.network.ApplicationNetwork;
import com.java.sunxun.network.NetworkHandler;

public class ModifyPasswordFragment extends Fragment {

    @Nullable
    FragmentModifyPasswordBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentModifyPasswordBinding.inflate(inflater, container, false);

        TextWatcher onTextChanged = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Editable prev = binding.modifyPrevInput.getText();
                Editable next = binding.modifyNextInput.getText();
                Editable confirm = binding.modifyConfirmInput.getText();
                binding.modifyActionButton.setEnabled(
                        prev != null && !prev.toString().isEmpty() &&
                                next != null && !next.toString().isEmpty() &&
                                confirm != null && confirm.toString().equals(next.toString())
                );
            }
        };

        binding.modifyPrevInput.addTextChangedListener(onTextChanged);
        binding.modifyNextInput.addTextChangedListener(onTextChanged);
        binding.modifyConfirmInput.addTextChangedListener(onTextChanged);

        binding.modifyActionButton.setOnClickListener(view -> {
            Editable prev = binding.modifyPrevInput.getText();
            Editable next = binding.modifyNextInput.getText();
            if (prev != null && next != null) {
                Snackbar.make(view, R.string.please_wait, Snackbar.LENGTH_SHORT).show();
                ApplicationNetwork.modifyPassword(User.currentUser.getUsername(), prev.toString(), next.toString(), new NetworkHandler<Boolean>(this) {
                    @Override
                    public void onSuccess(Boolean result) {
                        Snackbar.make(view, R.string.modify_succeed, Snackbar.LENGTH_SHORT).show();
                        view.getContext().getSharedPreferences("credentials", Context.MODE_PRIVATE)
                                .edit()
                                .putString("password", next.toString())
                                .apply();
                        NavHostFragment.findNavController(ModifyPasswordFragment.this).navigateUp();
                    }

                    @Override
                    public void onError(Exception e) {
                        @StringRes int message = R.string.modify_failure;
                        if (e instanceof InvalidCodeException) {
                            switch (((InvalidCodeException) e).code) {
                                case "2": {
                                    message = R.string.modify_prev_wrong;
                                    break;
                                }
                                case "3": {
                                    message = R.string.modify_not_change;
                                    break;
                                }
                            }
                        }
                        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
                    }
                });
            }
        });

        binding.modifyBack.setOnClickListener(view -> NavHostFragment.findNavController(this).navigateUp());

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}