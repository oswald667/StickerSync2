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
import androidx.fragment.app.DialogFragment;

import com.stickersync2.StickerEntity;
import com.stickersync2.ErrorHandlingService;
import com.stickersync2.StickerDuplicationService;

import java.util.ArrayList;
import java.util.List;

public class StickerDestinationBottomSheet extends DialogFragment {

    private static final String ARG_STICKER = "sticker";
    private StickerEntity sticker;
    private List<String> destinationApps = new ArrayList<>();
    private StickerDuplicationService duplicationService;
    private Context context;

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
        View view = inflater.inflate(R.layout.bottom_sheet_destination, container, false);

        context = requireContext();
        duplicationService = new StickerDuplicationService(context);

        // Setup UI
        TextView title = view.findViewById(R.id.title);
        title.setText("Copier ce sticker vers...");

        LinearLayout containerView = view.findViewById(R.id.destination_container);
        destinationApps = getAvailableDestinationApps();

        // Populate destination options
        for (String app : destinationApps) {
            View option = LayoutInflater.from(context).inflate(R.layout.destination_option, containerView, false);
            TextView appName = option.findViewById(R.id.appName);
            Button applyButton = option.findViewById(R.id.applyButton);

            appName.setText(app);

            applyButton.setOnClickListener(v -> {
                if (duplicateSticker(sticker, app)) {
                    Toast.makeText(context, "Sticker copié vers " + app, Toast.LENGTH_SHORT).show();
                    dismiss();
                } else {
                    Toast.makeText(context, "Échec de la copie", Toast.LENGTH_LONG).show();
                }
            });

            // Add to container
            containerView.addView(option);
        }

        return view;
    }

    private List<String> getAvailableDestinationApps() {
        // This would be populated dynamically based on app capabilities
        List<String> apps = new ArrayList<>();
        apps.add("WhatsApp");
        apps.add("TikTok");
        apps.add("Snapchat");
        apps.add("Facebook");
        return apps;
    }

    private boolean duplicateSticker(StickerEntity sticker, String destinationApp) {
        try {
            return duplicationService.duplicateSticker(sticker, destinationApp);
        } catch (Exception e) {
            ErrorHandlingService errorHandlingService = new ErrorHandlingService();
            errorHandlingService.logError("Duplication failed", e, ErrorHandlingService.ErrorCode.FORMAT_NOT_SUPPORTED);
            return false;
        }
    }
}