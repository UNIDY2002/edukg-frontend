package com.java.sunxun.ui;

import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.java.sunxun.databinding.FragmentLinkingBinding;
import com.java.sunxun.models.Linking;
import com.java.sunxun.models.Subject;
import com.java.sunxun.network.NetworkHandler;
import com.java.sunxun.network.PlatformNetwork;

public class LinkingFragment extends Fragment {

    @Nullable
    FragmentLinkingBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLinkingBinding.inflate(inflater, container, false);
        binding.linkingReturnIcon.setOnClickListener(view -> NavHostFragment.findNavController(this).navigateUp());
        binding.linkingSubmitButton.setOnClickListener(view -> {
            Editable editable = binding.linkingQuestionInput.getText();
            if (editable != null) {
                PlatformNetwork.linking(Subject.chinese, editable.toString(), new NetworkHandler<Linking>(view) {
                    @Override
                    public void onSuccess(Linking result) {
                        binding.linkingAnswerField.setText(result.getContext());
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