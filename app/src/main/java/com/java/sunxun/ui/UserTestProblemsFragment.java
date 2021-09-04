package com.java.sunxun.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.java.sunxun.R;
import com.java.sunxun.databinding.FragmentUserTestProblemsBinding;
import com.java.sunxun.models.Problem;
import com.java.sunxun.network.NetworkHandler;
import com.java.sunxun.network.PlatformNetwork;
import com.java.sunxun.utils.Share;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class UserTestProblemsFragment extends Fragment {

    @Nullable
    FragmentUserTestProblemsBinding binding;

    private static final String[] alphabet = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N"};

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentUserTestProblemsBinding.inflate(inflater, container, false);
        binding.userTestProblemsReturnIcon.setOnClickListener(view -> NavHostFragment.findNavController(this).navigateUp());

        UserTestAdapter adapter = new UserTestAdapter();
        binding.userTestRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.userTestRecyclerView.setAdapter(adapter);
        new PagerSnapHelper().attachToRecyclerView(binding.userTestRecyclerView);

        Bundle bundle = getArguments();
        if (bundle != null) {
            PlatformNetwork.relatedProblems(bundle.getString("name", ""), new NetworkHandler<ArrayList<Problem>>(this) {
                @Override
                public void onSuccess(ArrayList<Problem> problems) {
                    binding.userTestCorrectnessIndicator.setColumnCount(problems.size());
                    for (int i = 0; i < problems.size(); i++) {
                        View view = new View(binding.userTestCorrectnessIndicator.getContext());
                        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams(GridLayout.spec(GridLayout.UNDEFINED), GridLayout.spec(GridLayout.UNDEFINED, 1f));
                        layoutParams.width = 0;
                        view.setLayoutParams(layoutParams);
                        binding.userTestCorrectnessIndicator.addView(view);
                    }
                    Collections.shuffle(problems);
                    adapter.setProblems(problems);
                }

                @Override
                public void onError(Exception e) {

                }
            });
        }

        return binding.getRoot();
    }

    private class UserTestAdapter extends RecyclerView.Adapter<UserTestAdapter.ViewHolder> {

        private class UserProblem {
            String question;
            String[] options;
            Integer answerId;
            Integer selectedId = -1;

            UserProblem(String question, String[] options, Integer answerId) {
                this.question = question;
                this.options = options;
                this.answerId = answerId;
            }
        }

        private List<UserProblem> problems = new ArrayList<>();

        private class ViewHolder extends RecyclerView.ViewHolder {
            TextView question;
            LinearLayout options;
            View share;

            public ViewHolder(@NonNull View view) {
                super(view);
                question = view.findViewById(R.id.user_test_question);
                options = view.findViewById(R.id.user_test_options);
                share = view.findViewById(R.id.user_test_share_button);
            }
        }

        public void setProblems(ArrayList<Problem> problems) {
            this.problems = problems.stream().map(problem -> {
                Pair<String[], Integer> options = problem.genRandomOptions(alphabet.length);
                return new UserProblem(problem.getQuestion(), options.first, options.second);
            }).collect(Collectors.toList());
            notifyDataSetChanged();
        }

        // Problem: 0, Loading: 1
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                    viewType == 0 ? R.layout.item_user_test_slide : R.layout.item_user_test_loading_slide, parent, false
            ));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (problems.isEmpty()) return;
            UserProblem problem = problems.get(position);
            holder.question.setText(getString(R.string.user_test_question, position + 1, problem.question));
            holder.options.removeAllViews();
            StringBuilder shareOptions = new StringBuilder();
            for (int i = 0; i < problem.options.length; i++) {
                String option = problem.options[i];
                @SuppressLint("InflateParams")
                View view = getLayoutInflater().inflate(R.layout.item_user_test_option, null);
                view.setEnabled(problem.selectedId == -1);
                TextView textView = view.findViewById(R.id.user_test_option_text);
                String optionText = getString(R.string.user_test_option, alphabet[i], option);
                textView.setText(optionText);
                shareOptions.append(optionText);
                shareOptions.append('\n');
                if (problem.selectedId != -1 && i == problem.answerId)
                    textView.setTextColor(getResources().getColor(R.color.green, null));
                else if (i == problem.selectedId)
                    textView.setTextColor(getResources().getColor(R.color.red, null));
                final int j = i;
                view.setOnClickListener(v -> {
                    if (binding != null)
                        binding.userTestCorrectnessIndicator.getChildAt(position).setBackgroundColor(getResources().getColor(
                                j == problem.answerId ? R.color.green : R.color.red, null
                        ));
                    for (int k = 0; k < holder.options.getChildCount(); k++) {
                        holder.options.getChildAt(k).setEnabled(false);
                    }
                    problem.selectedId = j;
                    notifyItemChanged(position);
                });
                holder.options.addView(view);
            }
            holder.share.setOnClickListener(v -> Share.share(v.getContext(), getString(R.string.user_test_share_template, problem.question, shareOptions)));
        }

        @Override
        public int getItemCount() {
            return problems.isEmpty() ? 1 : problems.size();
        }

        @Override
        public int getItemViewType(int position) {
            return problems.isEmpty() ? 1 : 0;
        }
    }
}