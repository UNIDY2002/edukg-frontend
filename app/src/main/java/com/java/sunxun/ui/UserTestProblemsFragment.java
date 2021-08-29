package com.java.sunxun.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.ArrayList;
import java.util.Collections;

public class UserTestProblemsFragment extends Fragment {

    @Nullable
    FragmentUserTestProblemsBinding binding;

    private static final String[] alphabet = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N"};

    private int correct = 0;
    private int wrong = 0;
    private int total = 1;

    private void redrawCorrectnessIndicator() {
        if (binding == null) return;
        binding.userTestCorrect.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, correct));
        binding.userTestWrong.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, wrong));
        binding.userTestRemaining.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, total - correct - wrong));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentUserTestProblemsBinding.inflate(inflater, container, false);
        binding.userTestProblemsReturnIcon.setOnClickListener(view -> NavHostFragment.findNavController(this).navigateUp());

        UserTestAdapter adapter = new UserTestAdapter();
        binding.userTestRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.userTestRecyclerView.setAdapter(adapter);
        new PagerSnapHelper().attachToRecyclerView(binding.userTestRecyclerView);

        Bundle bundle = getArguments();
        if (bundle != null) {
            PlatformNetwork.relatedProblems(bundle.getString("name", ""), new NetworkHandler<ArrayList<Problem>>(this) {
                @Override
                public void onSuccess(ArrayList<Problem> problems) {
                    total = problems.size();
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

        private ArrayList<Problem> problems = new ArrayList<>();

        private class ViewHolder extends RecyclerView.ViewHolder {
            TextView question;
            LinearLayout options;
            TextView page;

            public ViewHolder(@NonNull View view) {
                super(view);
                question = view.findViewById(R.id.user_test_question);
                options = view.findViewById(R.id.user_test_options);
                page = view.findViewById(R.id.user_test_page);
            }
        }

        public void setProblems(ArrayList<Problem> problems) {
            this.problems = problems;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_test_slide, parent, false));
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Problem problem = problems.get(position);
            holder.question.setText(problem.getQuestion());
            holder.options.removeAllViews();
            Pair<String[], Integer> options = problems.get(position).genRandomOptions(alphabet.length);
            TextView[] textViews = new TextView[options.first.length];
            View[] views = new View[options.first.length];
            for (int i = 0; i < options.first.length; i++) {
                String option = options.first[i];
                @SuppressLint("InflateParams")
                View view = views[i] = getLayoutInflater().inflate(R.layout.item_user_test_option, null);
                TextView textView = textViews[i] = view.findViewById(R.id.user_test_option_text);
                textView.setText(alphabet[i] + ". " + option);
                final int j = i;
                view.setOnClickListener(v -> {
                    if (j == options.second) {
                        textView.setTextColor(getResources().getColor(R.color.green, null));
                        correct++;
                    } else {
                        textView.setTextColor(getResources().getColor(R.color.red, null));
                        textViews[options.second].setTextColor(getResources().getColor(R.color.green, null));
                        wrong++;
                    }
                    redrawCorrectnessIndicator();
                    for (View it : views) it.setEnabled(false);
                });
                holder.options.addView(view);
            }
            holder.page.setText((position + 1) + "/" + problems.size());
        }

        @Override
        public int getItemCount() {
            return problems.size();
        }
    }
}