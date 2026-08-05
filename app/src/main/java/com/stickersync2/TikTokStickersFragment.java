package com.stickersync2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class TikTokStickersFragment extends Fragment {

    private TikTokStickersViewModel viewModel;
    private StickerAdapter adapter;
    private TextView tvCount;
    private Button btnRefresh;

    public TikTokStickersFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sticker_grid, container, false);

        TextView title = view.findViewById(R.id.tvSectionTitle);
        title.setText("TikTok");

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewStickers);
        tvCount = view.findViewById(R.id.tvStickerCount);
        btnRefresh = view.findViewById(R.id.btnRefresh);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        adapter = new StickerAdapter(sticker -> {
            StickerDestinationBottomSheet sheet = StickerDestinationBottomSheet.newInstance(sticker);
            sheet.show(getParentFragmentManager(), "dest");
        });
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(TikTokStickersViewModel.class);
        viewModel.getStickers().observe(getViewLifecycleOwner(), stickers -> {
            adapter.setStickers(stickers);
            tvCount.setText(String.format("%d sticker(s)", stickers.size()));
        });

        btnRefresh.setOnClickListener(v -> viewModel.refreshStickers());
        return view;
    }
}
