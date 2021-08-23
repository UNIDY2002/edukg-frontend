package com.java.sunxun.ui;

import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.navigation.fragment.NavHostFragment;
import com.java.sunxun.R;
import com.java.sunxun.databinding.FragmentQaBinding;
import com.java.sunxun.models.Answer;
import com.java.sunxun.models.Subject;
import com.java.sunxun.network.NetworkHandler;
import com.java.sunxun.network.PlatformNetwork;

public class QaFragment extends Fragment {

    @Nullable
    FragmentQaBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentQaBinding.inflate(inflater, container, false);
        binding.qaReturnIcon.setOnClickListener(view -> NavHostFragment.findNavController(this).navigateUp());
        binding.qaSubmitButton.setOnClickListener(view -> {
            Editable editable = binding.qaQuestionInput.getText();
            if (editable != null) {
                PlatformNetwork.qa(Subject.chinese, editable.toString(), new NetworkHandler<Answer>(view) {
                    @Override
                    public void onSuccess(Answer result) {
                        String ans = result.getValue();
                        if (ans.isEmpty()) {
                            binding.qaAnswerField.setText(R.string.qa_empty);
                        } else {
                            binding.qaAnswerField.setText(result.getValue());
                        }
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
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