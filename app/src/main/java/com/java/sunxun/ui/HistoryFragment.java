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
import com.java.sunxun.R;
import com.java.sunxun.databinding.FragmentHistoryBinding;
import com.java.sunxun.models.History;

import java.util.ArrayList;
import java.util.List;

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
        if (!adapter.loaded) adapter.updateData(new ArrayList<>());
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private static class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        boolean loaded = false;

        private final List<HistoryItem> data = new ArrayList<>();

        private static class ViewHolder extends RecyclerView.ViewHolder {
            TextView sectionHeader;
            View container;
            TextView name;
            TextView date;

            public ViewHolder(@NonNull View view) {
                super(view);
                sectionHeader = view.findViewById(R.id.history_section_header_text);
                container = view.findViewById(R.id.history_item_container);
                name = view.findViewById(R.id.history_item_name);
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
                holder.name.setText(item.name);
                holder.date.setText(item.date);
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
            data.add(null);
            data.add(new HistoryItem("李白", "今天"));
            data.add(new HistoryItem("杜甫", "今天"));
            data.add(null);
            data.add(new HistoryItem("苏轼", "昨天"));
            data.add(new HistoryItem("白居易", "昨天"));
            notifyDataSetChanged();
            loaded = true;
        }

        private static class HistoryItem {
            String name;
            String date;

            HistoryItem(String name, String date) {
                this.name = name;
                this.date = date;
            }
        }
    }
}
