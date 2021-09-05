package com.java.sunxun.ui;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.java.sunxun.R;
import com.java.sunxun.data.QaViewModel;
import com.java.sunxun.databinding.FragmentQaBinding;
import com.java.sunxun.models.Answer;
import com.java.sunxun.models.Subject;
import com.java.sunxun.network.NetworkHandler;
import com.java.sunxun.network.PlatformNetwork;
import com.java.sunxun.utils.Components;

import java.util.ArrayList;

public class QaFragment extends Fragment {

    @Nullable
    FragmentQaBinding binding;

    private QaViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(QaViewModel.class);
        QaRvAdapter adapter = new QaRvAdapter();

        binding = FragmentQaBinding.inflate(inflater, container, false);
        binding.qaReturnIcon.setOnClickListener(view -> NavHostFragment.findNavController(this).navigateUp());

        Components.bindSubjectSelectorToViewModel(this, viewModel, binding.qaSubjectText);

        binding.qaRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.qaRecyclerView.setAdapter(adapter);

        binding.qaQuestionInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                binding.qaSubmitButton.setEnabled(!s.toString().trim().isEmpty());
            }
        });

        binding.qaSubmitButton.setOnClickListener(view -> {
            Editable editable = binding.qaQuestionInput.getText();
            if (editable == null) return;
            Context context = getContext();
            if (context == null) return;
            Subject subject = Subject.fromName(context, binding.qaSubjectText.getText().toString());
            if (subject == null) return;
            String question = editable.toString();
            viewModel.pushToQaList(subject, question);
            binding.qaQuestionInput.setText("");
            PlatformNetwork.qa(subject, question, new NetworkHandler<Answer>(view) {
                @Override
                public void onSuccess(Answer result) {
                    if (result.getScore() < 20) {
                        viewModel.pushToQaList(null,
                                result.getPredicate().isEmpty()
                                        ? getString(R.string.qa_uncertain_without_predicate, result.getEntityName())
                                        : getString(R.string.qa_uncertain_with_predicate, result.getEntityName(), result.getPredicate())
                        );
                    }
                    viewModel.pushToQaList(null, result.getValue());
                    viewModel.setCurrentFastLink(subject, result.getEntityUri(), result.getEntityName());
                }

                @Override
                public void onError(Exception e) {
                    viewModel.pushToQaList(null, e.toString());
                }
            });
        });

        viewModel.getQaList().observe(getViewLifecycleOwner(), data -> {
            // Assume that old data is never modified.
            int prevSize = adapter.data.size();
            int currSize = data.size();
            adapter.data = data;
            adapter.notifyItemRangeInserted(prevSize, currSize - prevSize);
            binding.qaRecyclerView.scrollToPosition(data.size() - 1);
        });

        viewModel.getCurrentFastLink().observe(getViewLifecycleOwner(), data -> {
            if (data == null) {
                binding.qaFastLink.setVisibility(View.GONE);
            } else {
                binding.qaFastLink.setText(getString(R.string.qa_fast_link_hint, data.name));
                binding.qaFastLink.setOnClickListener(view -> {
                    Bundle bundle = new Bundle();
                    bundle.putInt("subject", data.subject.ordinal());
                    bundle.putString("name", data.name);
                    bundle.putString("uri", data.uri);
                    NavHostFragment.findNavController(this).navigate(R.id.nav_detail, bundle);
                });
                binding.qaFastLink.setVisibility(View.VISIBLE);
            }
        });

        viewModel.pushToQaListIfEmpty(null, getString(R.string.qa_welcome));

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    class QaRvAdapter extends RecyclerView.Adapter<QaRvAdapter.ViewHolder> {

        ArrayList<Pair<Subject, String>> data = new ArrayList<>();

        private class ViewHolder extends RecyclerView.ViewHolder {
            TextView text;

            @Nullable
            TextView subject;

            public ViewHolder(@NonNull View view) {
                super(view);
                text = view.findViewById(R.id.qa_item_text);
                subject = view.findViewById(R.id.qa_item_subject);
            }
        }

        @NonNull
        @Override
        public QaRvAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new QaRvAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                    viewType == 0 ? R.layout.item_qa_question : R.layout.item_qa_answer, parent, false
            ));
        }

        @Override
        public void onBindViewHolder(@NonNull QaRvAdapter.ViewHolder holder, int position) {
            holder.text.setText(data.get(position).second);
            if (holder.subject != null) holder.subject.setText(data.get(position).first.toName(getContext()));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        @Override
        public int getItemViewType(int position) {
            return data.get(position).first == null ? 1 : 0;
        }
    }
}