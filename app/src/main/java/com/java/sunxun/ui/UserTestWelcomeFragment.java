package com.java.sunxun.ui;

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
import com.java.sunxun.R;
import com.java.sunxun.databinding.FragmentUserTestWelcomeBinding;
import com.java.sunxun.utils.Share;

public class UserTestWelcomeFragment extends Fragment {

    @Nullable
    FragmentUserTestWelcomeBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentUserTestWelcomeBinding.inflate(inflater, container, false);
        binding.userTestWelcomeReturnIcon.setOnClickListener(view -> NavHostFragment.findNavController(this).navigateUp());
        binding.userTestWelcomeStart.setOnClickListener(view -> {
            Editable editable = binding.userTestWelcomeNameInput.getText();
            if (editable != null) {
                Bundle bundle = new Bundle();
                bundle.putString("name", editable.toString());
                NavHostFragment.findNavController(this).navigate(R.id.nav_user_test_problems, bundle);
            }
        });
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
        binding.userTestShareButton.setOnClickListener(view -> Share.share(this, "#eduKGInfo#\n快来看看我在 eduKGInfo 中的做题情况吧！\n我练习了 10 道 莎士比亚 的相关习题，每道题全都选择了莎士比亚的相关选项，达到了 0% 的正确率！"));
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}