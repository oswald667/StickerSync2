package com.stickersync2;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {

    private static final String[] TAB_TITLES = {"Tous", "WhatsApp", "TikTok", "Snapchat", "Facebook", "Autres"};
    private static final int[] TAB_ICONS = {
        android.R.drawable.ic_menu_gallery,
        android.R.drawable.ic_menu_share,
        android.R.drawable.ic_menu_slideshow,
        android.R.drawable.ic_menu_camera,
        android.R.drawable.ic_menu_myplaces,
        android.R.drawable.ic_menu_more
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewPager2 viewPager2 = findViewById(R.id.viewPager2);
        TabLayout tabLayout = findViewById(R.id.tabLayout);

        StickerPagerAdapter adapter = new StickerPagerAdapter(this);
        viewPager2.setAdapter(adapter);
        viewPager2.setOffscreenPageLimit(5);

        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
            tab.setText(TAB_TITLES[position]);
        }).attach();
    }

    private static class StickerPagerAdapter extends FragmentStateAdapter {

        public StickerPagerAdapter(@NonNull AppCompatActivity activity) {
            super(activity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0: return new AllStickersFragment();
                case 1: return new WhatsAppStickersFragment();
                case 2: return new TikTokStickersFragment();
                case 3: return new SnapchatStickersFragment();
                case 4: return new FacebookStickersFragment();
                case 5: return new OthersStickersFragment();
                default: return new AllStickersFragment();
            }
        }

        @Override
        public int getItemCount() { return 6; }
    }
}
