package com.stickersync2;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialiser les composants
        ViewPager2 viewPager2 = findViewById(R.id.viewPager2);
        TabLayout tabLayout = findViewById(R.id.tabLayout);

        // Créer l'adapter pour le ViewPager2
        StickerPagerAdapter adapter = new StickerPagerAdapter(getSupportFragmentManager(), getLifecycle());

        // Associer l'adapter à la view pager
        viewPager2.setAdapter(adapter);

        // Configurer le mediator pour synchroniser les onglets avec la view pager
        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Tous");
                    break;
                case 1:
                    tab.setText("WhatsApp");
                    break;
                case 2:
                    tab.setText("TikTok");
                    break;
                case 3:
                    tab.setText("Snapchat");
                    break;
                case 4:
                    tab.setText("Facebook");
                    break;
                case 5:
                    tab.setText("Autres");
                    break;
            }
        }).attach();
    }

    // Adapter qui crée des fragments en fonction de la position
    public static class StickerPagerAdapter extends FragmentStatePagerAdapter {
        private final FragmentManager fragmentManager;

        public StickerPagerAdapter(@NonNull FragmentManager fm, @NonNull Lifecycle lifecycle) {
            super(fm, lifecycle);
            this.fragmentManager = fm;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new AllStickersFragment();
                case 1:
                    return new WhatsAppStickersFragment();
                case 2:
                    return new TikTokStickersFragment();
                case 3:
                    return new SnapchatStickersFragment();
                case 4:
                    return new FacebookStickersFragment();
                case 5:
                    return new OthersStickersFragment();
                default:
                    return new AllStickersFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 6;
        }
    }
}