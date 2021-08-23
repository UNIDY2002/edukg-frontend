package com.java.sunxun.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.material.snackbar.Snackbar;
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
        binding.linkingAnswerField.setMovementMethod(LinkMovementMethod.getInstance());
        binding.linkingReturnIcon.setOnClickListener(view -> NavHostFragment.findNavController(this).navigateUp());
        binding.linkingSubmitButton.setOnClickListener(view -> {
            Editable editable = binding.linkingQuestionInput.getText();
            if (editable != null) {
                PlatformNetwork.linking(Subject.chinese, editable.toString(), new NetworkHandler<Linking>(view) {
                    @Override
                    public void onSuccess(Linking result) {
                        SpannableString spannableString = new SpannableString(result.getContext());
                        result.getLinkingResults().forEach(linkingResult -> spannableString.setSpan(
                                new ClickableSpan() {
                                    @Override
                                    public void onClick(@NonNull View widget) {
                                        Snackbar.make(widget, "Entity uri = " + linkingResult.getEntityUri(), Snackbar.LENGTH_LONG).show();
                                    }
                                },
                                linkingResult.getStartIndex(),
                                linkingResult.getEndIndex() + 1,
                                Spanned.SPAN_INCLUSIVE_INCLUSIVE
                        ));
                        binding.linkingAnswerField.setText(spannableString);
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