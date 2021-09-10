package com.java.sunxun.ui;

import android.annotation.SuppressLint;
import android.content.Context;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.java.sunxun.R;
import com.java.sunxun.databinding.FragmentUserTestRecommendedBinding;
import com.java.sunxun.models.RecommendedProblem;
import com.java.sunxun.network.ApplicationNetwork;
import com.java.sunxun.network.NetworkHandler;
import com.java.sunxun.utils.Share;

import java.util.ArrayList;
import java.util.List;

public class UserTestRecommendedFragment extends Fragment {

    @Nullable
    FragmentUserTestRecommendedBinding binding;

    private static final String[] alphabet = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N"};

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentUserTestRecommendedBinding.inflate(inflater, container, false);
        binding.recommendedProblemsReturnIcon.setOnClickListener(view -> NavHostFragment.findNavController(this).navigateUp());

        UserTestAdapter adapter = new UserTestAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        binding.recommendedRecyclerView.setLayoutManager(layoutManager);
        binding.recommendedRecyclerView.setAdapter(adapter);
        new PagerSnapHelper().attachToRecyclerView(binding.recommendedRecyclerView);

        ApplicationNetwork.getRecommendedProblem(5, new NetworkHandler<RecommendedProblem>(this) {
            @Override
            public void onSuccess(RecommendedProblem recommendedProblem) {
                adapter.addProblem(recommendedProblem);
                binding.recommendedShareButton.setVisibility(View.VISIBLE);
                binding.recommendedShareButton.setOnClickListener(v -> {
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
                Snackbar.make(binding.recommendedRecyclerView, R.string.network_error, Snackbar.LENGTH_SHORT).show();
                binding.recommendedRecyclerView.setVisibility(View.GONE);
                e.printStackTrace();
            }
        });

        return binding.getRoot();
    }

    private class UserTestAdapter extends RecyclerView.Adapter<UserTestAdapter.ViewHolder> {

        private final List<UserProblem> problems = new ArrayList<>();

        private class ViewHolder extends RecyclerView.ViewHolder {
            TextView question;
            LinearLayout options;
            MaterialButton nextProblem;
            View loading;

            public ViewHolder(@NonNull View view) {
                super(view);
                question = view.findViewById(R.id.user_test_question);
                options = view.findViewById(R.id.user_test_options);
                nextProblem = view.findViewById(R.id.user_test_next_problem_button);
                loading = view.findViewById(R.id.user_test_next_problem_loading);
            }
        }


        @NonNull
        @Override
        public UserTestAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(viewType == 0 ? R.layout.item_user_recommended_slide : R.layout.item_user_test_loading_slide, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull UserTestAdapter.ViewHolder holder, int position) {
            if (problems.isEmpty()) return;
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
                    for (int k = 0; k < holder.options.getChildCount(); k++) {
                        holder.options.getChildAt(k).setEnabled(false);
                    }
                    problem.selectedId = j;
                    notifyItemChanged(position);
                    ApplicationNetwork.uploadTestResult(problem.uri, problem.label, j == problem.answerId, new NetworkHandler<Boolean>(UserTestRecommendedFragment.this) {
                        @Override
                        public void onSuccess(Boolean result) {

                        }

                        @Override
                        public void onError(Exception e) {

                        }
                    });
                    problem.loading = true;
                    holder.loading.setVisibility(View.VISIBLE);
                    ApplicationNetwork.getRecommendedProblem(5, new NetworkHandler<RecommendedProblem>(UserTestRecommendedFragment.this) {
                        @Override
                        public void onSuccess(RecommendedProblem result) {
                            problem.loading = false;
                            holder.loading.setVisibility(View.GONE);
                            addProblem(result);
                        }

                        @Override
                        public void onError(Exception e) {
                            problem.loading = false;
                            holder.loading.setVisibility(View.GONE);
                            notifyItemChanged(position);
                            if (binding != null) {
                                Snackbar.make(binding.recommendedRecyclerView, R.string.network_error, Snackbar.LENGTH_SHORT).show();
                            }
                            e.printStackTrace();
                        }
                    });
                });
                holder.options.addView(view);
            }
            holder.loading.setVisibility(problem.loading ? View.VISIBLE : View.GONE);
            holder.nextProblem.setVisibility(position == problems.size() - 1 ? View.GONE : View.VISIBLE);
            holder.nextProblem.setOnClickListener(v -> {
                if (binding != null) {
                    RecyclerView.LayoutManager manager = binding.recommendedRecyclerView.getLayoutManager();
                    if (manager != null) {
                        binding.recommendedRecyclerView.smoothScrollToPosition(position + 1);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return Math.max(1, problems.size());
        }

        @Override
        public int getItemViewType(int position) {
            return problems.isEmpty() ? 1 : 0;
        }

        public void addProblem(RecommendedProblem problem) {
            Pair<String[], Integer> options = problem.problem.genRandomOptions(alphabet.length);
            problems.add(new UserProblem(problem.label, problem.uri, problem.problem.getQuestion(), options.first, options.second));
            notifyDataSetChanged();
        }

        public String getShareText(int position) {
            UserProblem problem = problems.get(position);
            Context context = getContext();
            if (context == null) throw new NullPointerException();
            StringBuilder shareOptions = new StringBuilder();
            for (int i = 0; i < problem.options.length; i++) {
                shareOptions.append(getString(R.string.user_test_option, alphabet[i], problem.options[i]));
                shareOptions.append('\n');
            }
            return getString(R.string.user_test_share_template, problem.question, shareOptions);
        }

        private class UserProblem {
            String label;
            String uri;
            String question;
            String[] options;
            int answerId;
            int selectedId = -1;
            boolean loading = false;

            UserProblem(String label, String uri, String question, String[] options, int answerId) {
                this.label = label;
                this.uri = uri;
                this.question = question;
                this.options = options;
                this.answerId = answerId;
            }
        }

    }
}