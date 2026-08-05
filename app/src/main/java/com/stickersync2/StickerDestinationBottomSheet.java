package com.stickersync2;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.Arrays;
import java.util.List;

public class StickerDestinationBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_STICKER = "sticker";
    private StickerEntity sticker;

    public static StickerDestinationBottomSheet newInstance(StickerEntity sticker) {
        StickerDestinationBottomSheet fragment = new StickerDestinationBottomSheet();
        Bundle args = new Bundle();
        args.putParcelable(ARG_STICKER, sticker);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            sticker = getArguments().getParcelable(ARG_STICKER);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_destination, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context ctx = requireContext();
        StickerDuplicationService duplicationService = new StickerDuplicationService(ctx);

        LinearLayout destinationContainer = view.findViewById(R.id.destination_container);
        Button closeButton = view.findViewById(R.id.closeButton);

        List<String> apps = Arrays.asList("WhatsApp", "TikTok", "Snapchat", "Facebook");

        // Remove source app from destinations
        String sourceApp = sticker != null ? sticker.getSourceApp() : "";
        for (String app : apps) {
            if (app.equalsIgnoreCase(sourceApp)) continue;

            LinearLayout row = new LinearLayout(ctx);
            row.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            rowParams.setMargins(0, 0, 0, 24);
            row.setLayoutParams(rowParams);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);

            TextView appName = new TextView(ctx);
            appName.setText(app);
            appName.setTextSize(16f);
            LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            appName.setLayoutParams(tvParams);

            Button copyBtn = new Button(ctx);
            copyBtn.setText("Copier");
            copyBtn.setOnClickListener(v -> {
                if (sticker != null && duplicationService.duplicateSticker(sticker, app)) {
                    Toast.makeText(ctx, "Sticker copié vers " + app + " ✓", Toast.LENGTH_SHORT).show();
                    dismiss();
                } else {
                    Toast.makeText(ctx, "Échec — vérifiez que " + app + " est installé", Toast.LENGTH_LONG).show();
                }
            });

            row.addView(appName);
            row.addView(copyBtn);
            destinationContainer.addView(row);
        }

        closeButton.setOnClickListener(v -> dismiss());
    }
}
