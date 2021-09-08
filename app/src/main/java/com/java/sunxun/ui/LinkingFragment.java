package com.java.sunxun.ui;

import android.content.Context;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.material.snackbar.Snackbar;
import com.java.sunxun.R;
import com.java.sunxun.data.LinkingViewModel;
import com.java.sunxun.databinding.FragmentLinkingBinding;
import com.java.sunxun.models.Linking;
import com.java.sunxun.models.SearchResult;
import com.java.sunxun.models.Subject;
import com.java.sunxun.network.NetworkHandler;
import com.java.sunxun.network.PlatformNetwork;
import com.java.sunxun.utils.Components;

import java.util.ArrayList;
import java.util.Optional;

public class LinkingFragment extends Fragment {

    @Nullable
    FragmentLinkingBinding binding;

    private LinkingViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(LinkingViewModel.class);
        binding = FragmentLinkingBinding.inflate(inflater, container, false);
        binding.linkingAnswerField.setMovementMethod(LinkMovementMethod.getInstance());
        binding.linkingReturnIcon.setOnClickListener(view -> NavHostFragment.findNavController(this).navigateUp());

        binding.linkingStateToggleGroup.check(R.id.linking_edit_icon);
        binding.linkingStateToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == R.id.linking_edit_icon) {
                if (viewModel.getMode().getValue() == LinkingViewModel.Mode.EDIT) return;
                viewModel.setMode(LinkingViewModel.Mode.EDIT);
            } else {
                // Validate
                if (viewModel.getMode().getValue() != LinkingViewModel.Mode.EDIT) return;
                Editable editable = binding.linkingQuestionInput.getText();
                if (editable == null) return;
                Context context = getContext();
                if (context == null) return;
                Subject subject = Subject.fromName(context, binding.linkingSubjectText.getText().toString());
                if (subject == null) return;
                viewModel.setMode(LinkingViewModel.Mode.RUNNING);
                Snackbar.make(binding.linkingQuestionField, R.string.querying, Snackbar.LENGTH_SHORT).show();

                // Search
                PlatformNetwork.linking(subject, editable.toString(), new NetworkHandler<Linking>(this) {
                    @Override
                    public void onSuccess(Linking result) {
                        if (viewModel.getMode().getValue() != LinkingViewModel.Mode.RUNNING) return;
                        Snackbar.make(binding.linkingQuestionField, R.string.query_done, Snackbar.LENGTH_SHORT).show();
                        SpannableString spannableString = new SpannableString(result.getContext());
                        result.getLinkingResults().forEach(linkingResult -> spannableString.setSpan(
                                new ClickableSpan() {
                                    @Override
                                    public void onClick(@NonNull View widget) {
                                        PlatformNetwork.searchInstance(subject, linkingResult.getEntity(), new NetworkHandler<ArrayList<SearchResult>>(widget) {
                                            @Override
                                            public void onSuccess(ArrayList<SearchResult> result) {
                                                Bundle bundle = new Bundle();
                                                bundle.putInt("subject", subject.ordinal());
                                                bundle.putString("name", linkingResult.getEntity());
                                                bundle.putString("uri", linkingResult.getEntityUri());
                                                Optional<SearchResult> target = result.stream().filter(searchResult -> searchResult.getLabel().equals(linkingResult.getEntity())).findFirst();
                                                if (!target.isPresent()) throw new NullPointerException();
                                                bundle.putString("category", target.get().getCategory());
                                                NavHostFragment.findNavController(LinkingFragment.this).navigate(R.id.nav_detail, bundle);
                                            }

                                            @Override
                                            public void onError(Exception e) {
                                                Snackbar.make(widget, R.string.do_not_support_linking, Snackbar.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                },
                                linkingResult.getStartIndex(),
                                linkingResult.getEndIndex() + 1,
                                Spanned.SPAN_INCLUSIVE_INCLUSIVE
                        ));
                        viewModel.setMode(LinkingViewModel.Mode.DONE);
                        viewModel.setResult(spannableString);
                    }

                    @Override
                    public void onError(Exception e) {
                        Snackbar.make(binding.linkingQuestionField, R.string.query_error, Snackbar.LENGTH_SHORT).show();
                    }
                });
            }
        });

        Components.bindSubjectSelectorToViewModel(this, viewModel, binding.linkingSubjectText);

        viewModel.getMode().observe(getViewLifecycleOwner(), mode -> {
            switch (mode) {
                case EDIT: {
                    binding.linkingAnswerField.setVisibility(View.GONE);
                    binding.linkingQuestionField.setVisibility(View.VISIBLE);
                    binding.linkingQuestionInput.setEnabled(true);
                    break;
                }
                case RUNNING: {
                    binding.linkingAnswerField.setVisibility(View.GONE);
                    binding.linkingQuestionField.setVisibility(View.VISIBLE);
                    binding.linkingQuestionInput.setEnabled(false);
                    break;
                }
                case DONE: {
                    binding.linkingAnswerField.setVisibility(View.VISIBLE);
                    binding.linkingQuestionField.setVisibility(View.GONE);
                    break;
                }
            }
        });

        viewModel.getResult().observe(getViewLifecycleOwner(), result -> binding.linkingAnswerField.setText(result));


        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}