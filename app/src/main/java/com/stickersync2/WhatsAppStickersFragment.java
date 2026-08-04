package com.stickersync2;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stickersync2.StickerEntity;
import com.stickersync2.StickerAdapter;
import com.stickersync2.StickerRepository;
import com.stickersync2.StickerDestinationBottomSheet;

public class WhatsAppStickersFragment extends Fragment {

    private WhatsAppStickersViewModel viewModel;
    private StickerAdapter adapter;
    private TextView tvCount;
    private Button btnRefresh;

    public WhatsAppStickersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                            @Nullable ViewGroup container,
                            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_whatsapp, container, false);

        // Initialiser les vues
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewStickers);
        tvCount = view.findViewById(R.id.tvStickerCount);
        btnRefresh = view.findViewById(R.id.btnRefresh);

        // Configurer le RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        adapter = new StickerAdapter(stickerClickListener);
        recyclerView.setAdapter(adapter);

        // Observer les stickers WhatsApp
        viewModel = new ViewModelProvider(this).get(WhatsAppStickersViewModel.class);
        viewModel.getStickers().observe(getViewLifecycleOwner(), stickers -> {
            adapter.setStickers(stickers);
            tvCount.setText(String.format("%d stickers", stickers.size()));
        });

        // Rafraîchir manuellement
        btnRefresh.setOnClickListener(v -> viewModel.refreshStickers());

        return view;
    }

    // Click listener interface
    private final OnStickerClickListener stickerClickListener = new OnStickerClickListener() {
        @Override
        public void onStickerClick(StickerEntity sticker) {
            // Ouvrir la feuille de partage vers une autre app
            StickerDestinationBottomSheet bottomSheet = StickerDestinationBottomSheet.newInstance(sticker);
            bottomSheet.show(getParentFragmentManager(), bottomSheet.getTag());
        }
    };
}