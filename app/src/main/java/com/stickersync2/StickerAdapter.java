package com.stickersync2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StickerAdapter extends RecyclerView.Adapter<StickerAdapter.StickerViewHolder> {

    private final List<StickerEntity> stickers = new ArrayList<>();
    private final OnStickerClickListener clickListener;

    public interface OnStickerClickListener {
        void onStickerClick(StickerEntity sticker);
    }

    public StickerAdapter(OnStickerClickListener listener) {
        this.clickListener = listener;
    }

    public void setStickers(List<StickerEntity> stickers) {
        this.stickers.clear();
        this.stickers.addAll(stickers);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StickerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.grid_item, parent, false);
        return new StickerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StickerViewHolder holder, int position) {
        StickerEntity sticker = stickers.get(position);
        holder.bind(sticker);
    }

    @Override
    public int getItemCount() {
        return stickers.size();
    }

    class StickerViewHolder extends RecyclerView.ViewHolder {
        private final ImageView stickerImage;

        public StickerViewHolder(@NonNull View itemView) {
            super(itemView);
            stickerImage = itemView.findViewById(R.id.stickerImage);
            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onStickerClick(stickers.get(pos));
                }
            });
        }

        public void bind(StickerEntity sticker) {
            File file = new File(sticker.getFilePath());
            if (file.exists()) {
                Glide.with(itemView.getContext())
                        .load(file)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_delete)
                        .into(stickerImage);
            } else {
                stickerImage.setImageResource(android.R.drawable.ic_delete);
            }
        }
    }
}