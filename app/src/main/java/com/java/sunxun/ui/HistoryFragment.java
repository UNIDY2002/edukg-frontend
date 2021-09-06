package com.java.sunxun.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;
import com.java.sunxun.R;
import com.java.sunxun.databinding.FragmentHistoryBinding;
import com.java.sunxun.models.History;
import com.java.sunxun.models.Subject;
import com.java.sunxun.network.ApplicationNetwork;
import com.java.sunxun.network.NetworkHandler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class HistoryFragment extends Fragment {

    @Nullable
    FragmentHistoryBinding binding;

    Adapter adapter = new Adapter();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        binding.historyReturnIcon.setOnClickListener(view -> NavHostFragment.findNavController(this).navigateUp());
        binding.historyRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.historyRecyclerView.setAdapter(adapter);
        if (!adapter.loaded) ApplicationNetwork.getHistoryList(new NetworkHandler<List<History>>(this) {
            @Override
            public void onSuccess(List<History> result) {
                adapter.updateData(result);
            }

            @Override
            public void onError(Exception e) {
                Snackbar.make(binding.historyRecyclerView, R.string.network_error, Snackbar.LENGTH_SHORT).show();
            }
        });
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        boolean loaded = false;

        private final List<HistoryItem> data = new ArrayList<>();

        private class ViewHolder extends RecyclerView.ViewHolder {
            TextView sectionHeader;
            View container;
            TextView name;
            TextView category;
            TextView date;

            public ViewHolder(@NonNull View view) {
                super(view);
                sectionHeader = view.findViewById(R.id.history_section_header_text);
                container = view.findViewById(R.id.history_item_container);
                name = view.findViewById(R.id.history_item_name);
                category = view.findViewById(R.id.history_item_category);
                date = view.findViewById(R.id.history_item_date);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                    viewType == 0 ? R.layout.item_history_section_header : R.layout.item_history_item, parent, false
            ));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            @Nullable HistoryItem item = data.get(position);
            if (item == null) {
                holder.sectionHeader.setText(data.get(position + 1).date);
            } else {
                holder.name.setText(getString(R.string.dash_template, item.subject.toName(getContext()), item.name));
                holder.category.setText(item.category);
                holder.date.setText(item.date);
                holder.container.setOnClickListener(v -> {
                    Bundle bundle = new Bundle();
                    bundle.putInt("subject", item.subject.ordinal());
                    bundle.putString("name", item.name);
                    bundle.putString("uri", item.uri);
                    bundle.putString("category", item.category);
                    NavHostFragment.findNavController(HistoryFragment.this).navigate(R.id.nav_detail, bundle);
                });
            }
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        @Override
        public int getItemViewType(int position) {
            return data.get(position) == null ? 0 : 1;
        }

        public void updateData(List<History> source) {
            data.clear();
            List<HistoryItem> tempList = source.stream().map(item -> {
                Calendar calendar = Calendar.getInstance(Locale.CHINA);
                calendar.setTime(item.getTime());
                return new HistoryItem(
                        item.getSubject(),
                        item.getUri(),
                        item.getName(),
                        item.getCategory(),
                        calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.MONTH) + "-" + calendar.get(Calendar.DATE)
                );
            }).filter(historyItem -> historyItem.subject != null).collect(Collectors.toList());
            for (int i = 0; i < tempList.size(); i++) {
                if (i == 0 || !tempList.get(i - 1).date.equals(tempList.get(i).date)) data.add(null);
                data.add(tempList.get(i));
            }
            notifyDataSetChanged();
            loaded = true;
        }

        private class HistoryItem {
            Subject subject;
            String uri;
            String name;
            String category;
            String date;

            HistoryItem(Subject subject, String uri, String name, String category, String date) {
                this.subject = subject;
                this.uri = uri;
                this.name = name;
                this.category = category;
                this.date = date;
            }
        }
    }
}
