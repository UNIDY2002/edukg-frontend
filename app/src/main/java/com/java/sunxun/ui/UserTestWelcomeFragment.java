package com.java.sunxun.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.flexbox.FlexboxLayout;
import com.java.sunxun.R;
import com.java.sunxun.dao.TestHistoryDB;
import com.java.sunxun.databinding.FragmentUserTestWelcomeBinding;

import java.util.List;

public class UserTestWelcomeFragment extends Fragment {

    @Nullable
    FragmentUserTestWelcomeBinding binding;

    private void updateHistory(@NonNull FlexboxLayout container) {
        try {
            List<String> history = TestHistoryDB.getInstance().getHistory();
            container.removeAllViews();
            history.subList(0, Math.min(history.size(), 11)).forEach(record -> {
                TextView textView = new TextView(getContext());
                FlexboxLayout.LayoutParams layoutParams = new FlexboxLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.topMargin = layoutParams.bottomMargin = layoutParams.leftMargin = layoutParams.rightMargin = 10;
                textView.setLayoutParams(layoutParams);
                textView.setBackgroundResource(R.drawable.shape_small_text_background);
                textView.setTextColor(getResources().getColor(R.color.black, null));
                textView.setPadding(15, 15, 15, 15);
                textView.setText(record);
                textView.setOnClickListener(v -> {
                    try {
                        TestHistoryDB.getInstance().addHistory(record);
                        updateHistory(container);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Bundle bundle = new Bundle();
                    bundle.putString("name", record);
                    NavHostFragment.findNavController(this).navigate(R.id.nav_user_test_problems, bundle);
                });
                container.addView(textView);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentUserTestWelcomeBinding.inflate(inflater, container, false);
        binding.userTestWelcomeReturnIcon.setOnClickListener(view -> NavHostFragment.findNavController(this).navigateUp());
        binding.userTestWelcomeStart.setOnClickListener(view -> {
            Editable editable = binding.userTestWelcomeNameInput.getText();
            if (editable != null) {
                try {
                    TestHistoryDB.getInstance().addHistory(editable.toString());
                    updateHistory(binding.userTestHistoryContainer);
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
        updateHistory(binding.userTestHistoryContainer);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}