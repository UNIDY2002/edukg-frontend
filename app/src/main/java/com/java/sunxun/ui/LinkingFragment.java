package com.java.sunxun.ui;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.*;
import android.widget.PopupMenu;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.material.snackbar.Snackbar;
import com.java.sunxun.R;
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

        binding.linkingStateToggleGroup.check(R.id.linking_edit_icon);
        binding.linkingStateToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == R.id.linking_edit_icon) {
                binding.linkingAnswerField.setVisibility(View.GONE);
                binding.linkingQuestionField.setVisibility(View.VISIBLE);
                binding.linkingQuestionInput.setEnabled(true);
            } else {
                Editable editable = binding.linkingQuestionInput.getText();
                if (editable == null) return;
                Context context = getContext();
                if (context == null) return;
                Subject subject = Subject.fromName(context, binding.linkingSubjectText.getText().toString());
                if (subject == null) return;
                binding.linkingQuestionInput.setEnabled(false);
                Snackbar.make(binding.linkingQuestionField, R.string.querying, Snackbar.LENGTH_SHORT).show();
                PlatformNetwork.linking(subject, editable.toString(), new NetworkHandler<Linking>(this) {
                    @Override
                    public void onSuccess(Linking result) {
                        if (binding.linkingStateToggleGroup.getCheckedButtonId() != R.id.linking_start_icon) return;
                        Snackbar.make(binding.linkingQuestionField, R.string.query_done, Snackbar.LENGTH_SHORT).show();
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
                        binding.linkingQuestionField.setVisibility(View.GONE);
                        binding.linkingAnswerField.setVisibility(View.VISIBLE);
                        binding.linkingAnswerField.setText(spannableString);
                    }

                    @Override
                    public void onError(Exception e) {
                        Snackbar.make(binding.linkingQuestionField, R.string.query_error, Snackbar.LENGTH_SHORT).show();
                    }
                });
            }
        });

        binding.linkingSubjectText.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(getContext(), view);
            popupMenu.getMenuInflater().inflate(R.menu.subject_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                binding.linkingSubjectText.setText(menuItem.getTitle());
                return true;
            });
            popupMenu.show();
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}