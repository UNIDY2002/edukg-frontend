package com.java.sunxun.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.navigation.fragment.NavHostFragment;
import com.java.sunxun.R;
import com.java.sunxun.databinding.FragmentUserTestWelcomeBinding;

public class UserTestWelcomeFragment extends Fragment {

    @Nullable
    FragmentUserTestWelcomeBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentUserTestWelcomeBinding.inflate(inflater, container, false);
        binding.userTestWelcomeReturnIcon.setOnClickListener(view -> NavHostFragment.findNavController(this).navigateUp());
        binding.userTestWelcomeStart.setOnClickListener(view -> NavHostFragment.findNavController(this).navigate(R.id.nav_user_test_problems));
        binding.userTestWelcomeNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                binding.userTestWelcomeStart.setEnabled(!s.toString().isEmpty());
            }
        });
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}