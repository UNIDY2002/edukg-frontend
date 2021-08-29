package com.java.sunxun.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
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
import java.util.Arrays;

public class UserTestProblemsFragment extends Fragment {

    @Nullable
    FragmentUserTestProblemsBinding binding;

    private static final String[] alphabet = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N"};

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

            public ViewHolder(@NonNull View view) {
                super(view);
                question = view.findViewById(R.id.user_test_question);
                options = view.findViewById(R.id.user_test_options);
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
            ArrayList<String> allOptions = new ArrayList<>();
            allOptions.add(problem.getAnswer());
            allOptions.addAll(Arrays.asList(problem.getDistractions()));
            while (allOptions.size() > alphabet.length) allOptions.remove(allOptions.size() - 1);
            for (int i = 0; i < allOptions.size(); i++) {
                String option = allOptions.get(i);
                @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.item_user_test_option, null);
                ((TextView) view.findViewById(R.id.user_test_option_text)).setText(alphabet[i] + ". " + option);
                holder.options.addView(view);
            }
        }

        @Override
        public int getItemCount() {
            return problems.size();
        }
    }
}