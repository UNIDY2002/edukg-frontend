package com.java.sunxun.ui;

import android.annotation.SuppressLint;
import android.content.Context;
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
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.java.sunxun.R;
import com.java.sunxun.databinding.FragmentUserTestProblemsBinding;
import com.java.sunxun.models.Problem;
import com.java.sunxun.network.ApplicationNetwork;
import com.java.sunxun.network.NetworkHandler;
import com.java.sunxun.network.PlatformNetwork;
import com.java.sunxun.utils.Share;

import java.util.*;
import java.util.stream.Collectors;

public class UserTestProblemsFragment extends Fragment {

    @Nullable
    FragmentUserTestProblemsBinding binding;

    private static final String[] alphabet = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N"};

    private String name;

    private String correctness;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentUserTestProblemsBinding.inflate(inflater, container, false);
        binding.userTestProblemsReturnIcon.setOnClickListener(view -> NavHostFragment.findNavController(this).navigateUp());

        UserTestAdapter adapter = new UserTestAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        binding.userTestRecyclerView.setLayoutManager(layoutManager);
        binding.userTestRecyclerView.setAdapter(adapter);
        new PagerSnapHelper().attachToRecyclerView(binding.userTestRecyclerView);

        Bundle bundle = getArguments();
        if (bundle != null) {
            name = bundle.getString("name");
            if (name != null) {
                PlatformNetwork.relatedProblems(name, new NetworkHandler<ArrayList<Problem>>(this) {
                    @Override
                    public void onSuccess(ArrayList<Problem> problems) {
                        if (problems.isEmpty()) {
                            Snackbar.make(binding.userTestRecyclerView, R.string.no_problems_found, Snackbar.LENGTH_SHORT).show();
                            binding.userTestRecyclerView.setVisibility(View.GONE);
                        }
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
                        binding.userTestShareButton.setVisibility(View.VISIBLE);
                        binding.userTestShareButton.setOnClickListener(v -> {
                            try {
                                int currentPos = layoutManager.findFirstVisibleItemPosition();
                                Context context = getContext();
                                if (currentPos != RecyclerView.NO_POSITION && context != null) {
                                    Share.share(context, adapter.getShareText(currentPos));
                                }
                            } catch (Exception e) {
                                Snackbar.make(v, R.string.network_error, Snackbar.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        Snackbar.make(binding.userTestRecyclerView, R.string.network_error, Snackbar.LENGTH_SHORT).show();
                        binding.userTestRecyclerView.setVisibility(View.GONE);
                    }
                });
            }
        }

        return binding.getRoot();
    }

    private class UserTestAdapter extends RecyclerView.Adapter<UserTestAdapter.ViewHolder> {

        private class UserProblem {
            String question;
            String[] options;
            int answerId;
            int selectedId = -1;

            UserProblem(String question, String[] options, int answerId) {
                this.question = question;
                this.options = options;
                this.answerId = answerId;
            }
        }

        private List<UserProblem> problems = new ArrayList<>();

        private class ViewHolder extends RecyclerView.ViewHolder {
            TextView question;
            LinearLayout options;
            MaterialButton nextProblem;
            TextView summaryTitle;
            PieChart pieChart;

            public ViewHolder(@NonNull View view) {
                super(view);
                question = view.findViewById(R.id.user_test_question);
                options = view.findViewById(R.id.user_test_options);
                nextProblem = view.findViewById(R.id.user_test_next_problem_button);
                summaryTitle = view.findViewById(R.id.user_test_summary_title);
                pieChart = view.findViewById(R.id.user_test_summary_pie_chart);
            }
        }

        public void setProblems(ArrayList<Problem> problems) {
            this.problems = problems.stream().map(problem -> {
                Pair<String[], Integer> options = problem.genRandomOptions(alphabet.length);
                return new UserProblem(problem.getQuestion(), options.first, options.second);
            }).collect(Collectors.toList());
            notifyDataSetChanged();
        }

        // Problem: 0, Loading: 1, Summary: 2
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                    viewType == 0
                            ? R.layout.item_user_test_slide
                            : viewType == 1
                            ? R.layout.item_user_test_loading_slide
                            : R.layout.item_user_test_summary_slide,
                    parent,
                    false
            ));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (problems.isEmpty()) return;
            if (position == problems.size()) {
                List<PieEntry> entries = new ArrayList<>();
                int correct = 0;
                int wrong = 0;
                for (UserProblem problem : problems) {
                    if (problem.answerId == problem.selectedId) correct++;
                    else wrong++;
                }
                entries.add(new PieEntry(correct));
                entries.add(new PieEntry(wrong));
                PieDataSet dataSet = new PieDataSet(entries, "");
                dataSet.setColors(0xff00ff00, 0xffff0000);
                dataSet.setValueFormatter(new DefaultValueFormatter(0));
                dataSet.setValueTextSize(16);
                PieData pieData = new PieData(dataSet);
                holder.pieChart.setDrawCenterText(true);
                holder.pieChart.setCenterTextSize(20);
                holder.pieChart.getLegend().setEnabled(false);
                holder.pieChart.setCenterText(correctness = String.format(Locale.getDefault(), "%d", Math.round(correct * 100.0 / (correct + wrong))) + "%");
                holder.pieChart.setData(pieData);
                holder.pieChart.getDescription().setEnabled(false);
                holder.pieChart.animateY(1500);
                holder.pieChart.setTouchEnabled(false);
                holder.summaryTitle.setText(getString(R.string.user_test_congratulations, correctness));
                return;
            }
            UserProblem problem = problems.get(position);
            holder.question.setText(getString(R.string.user_test_question, position + 1, problem.question));
            holder.options.removeAllViews();
            for (int i = 0; i < problem.options.length; i++) {
                String option = problem.options[i];
                @SuppressLint("InflateParams")
                View view = getLayoutInflater().inflate(R.layout.item_user_test_option, null);
                view.setEnabled(problem.selectedId == -1);
                TextView textView = view.findViewById(R.id.user_test_option_text);
                String optionText = getString(R.string.user_test_option, alphabet[i], option);
                textView.setText(optionText);
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
                    notifyItemInserted(position + 1);
                    ApplicationNetwork.uploadTestResult(name, j == problem.answerId, new NetworkHandler<Boolean>(UserTestProblemsFragment.this) {
                        @Override
                        public void onSuccess(Boolean result) {

                        }

                        @Override
                        public void onError(Exception e) {

                        }
                    });
                });
                holder.options.addView(view);
            }
            holder.nextProblem.setVisibility(problem.selectedId == -1 ? View.GONE : View.VISIBLE);
            holder.nextProblem.setText(position == problems.size() - 1 ? R.string.see_result : R.string.next_problem);
            holder.nextProblem.setOnClickListener(v -> {
                if (binding != null) {
                    RecyclerView.LayoutManager manager = binding.userTestRecyclerView.getLayoutManager();
                    if (manager != null) {
                        binding.userTestRecyclerView.smoothScrollToPosition(position + 1);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            int lastAnsweredIndex = -1;
            for (int i = 0; i < problems.size(); i++) {
                if (problems.get(i).selectedId != -1) lastAnsweredIndex = i;
            }
            return lastAnsweredIndex + 2;
        }

        @Override
        public int getItemViewType(int position) {
            return problems.isEmpty() ? 1 : position == problems.size() ? 2 : 0;
        }

        public String getShareText(int position) {
            if (position < problems.size()) {
                UserProblem problem = problems.get(position);
                Context context = getContext();
                if (context == null) throw new NullPointerException();
                StringBuilder shareOptions = new StringBuilder();
                for (int i = 0; i < problem.options.length; i++) {
                    shareOptions.append(getString(R.string.user_test_option, alphabet[i], problem.options[i]));
                    shareOptions.append('\n');
                }
                return getString(R.string.user_test_share_template, problem.question, shareOptions);
            } else {
                return getString(R.string.user_test_share_all_template, problems.size(), name, correctness);
            }
        }
    }
}