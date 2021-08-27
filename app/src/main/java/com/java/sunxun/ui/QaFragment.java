package com.java.sunxun.ui;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.java.sunxun.R;
import com.java.sunxun.databinding.FragmentQaBinding;
import com.java.sunxun.models.Answer;
import com.java.sunxun.models.Subject;
import com.java.sunxun.network.NetworkHandler;
import com.java.sunxun.network.PlatformNetwork;

import java.util.ArrayList;

public class QaFragment extends Fragment {

    @Nullable
    FragmentQaBinding binding;

    ArrayList<Pair<Subject, String>> qaList = new ArrayList<>();

    // if subject is null, then the data is an answer
    private void pushToQaList(@Nullable Subject subject, String data) {
        if (binding != null) {
            qaList.add(new Pair<>(subject, data));
            QaRvAdapter adapter = (QaRvAdapter) binding.qaRecyclerView.getAdapter();
            if (adapter != null) {
                adapter.notifyItemInserted(qaList.size() - 1);
            }
            binding.qaRecyclerView.scrollToPosition(qaList.size() - 1);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentQaBinding.inflate(inflater, container, false);
        binding.qaReturnIcon.setOnClickListener(view -> NavHostFragment.findNavController(this).navigateUp());
        binding.qaSubjectText.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(getContext(), view);
            popupMenu.getMenuInflater().inflate(R.menu.subject_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                binding.qaSubjectText.setText(menuItem.getTitle());
                return true;
            });
            popupMenu.show();
        });

        binding.qaRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.qaRecyclerView.setAdapter(new QaRvAdapter());

        binding.qaSubmitButton.setOnClickListener(view -> {
            Editable editable = binding.qaQuestionInput.getText();
            if (editable == null) return;
            Context context = getContext();
            if (context == null) return;
            Subject subject = Subject.fromName(context, binding.qaSubjectText.getText().toString());
            if (subject == null) return;
            String question = editable.toString();
            pushToQaList(subject, question);
            binding.qaQuestionInput.setText("");
            PlatformNetwork.qa(subject, question, new NetworkHandler<Answer>(view) {
                @Override
                public void onSuccess(Answer result) {
                    pushToQaList(null, result.getValue());
                }

                @Override
                public void onError(Exception e) {
                    pushToQaList(null, e.toString());
                }
            });
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    class QaRvAdapter extends RecyclerView.Adapter<QaRvAdapter.ViewHolder> {

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
            holder.text.setText(qaList.get(position).second);
            if (holder.subject != null) holder.subject.setText(qaList.get(position).first.toName(getContext()));
        }

        @Override
        public int getItemCount() {
            return qaList.size();
        }

        @Override
        public int getItemViewType(int position) {
            return qaList.get(position).first == null ? 1 : 0;
        }
    }
}