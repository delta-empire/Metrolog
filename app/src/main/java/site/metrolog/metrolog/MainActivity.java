package site.metrolog.metrolog;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.yandex.mobile.ads.banner.BannerAdEventListener;
import com.yandex.mobile.ads.banner.BannerAdSize;
import com.yandex.mobile.ads.banner.BannerAdView;
import com.yandex.mobile.ads.common.AdRequest;
import com.yandex.mobile.ads.common.AdRequestError;
import com.yandex.mobile.ads.common.ImpressionData;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    @Nullable
    private BannerAdView bannerAdView;
    private View mainContentView;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            } else if (itemId == R.id.nav_version) {
                showVersionDialog();
            } else if (itemId == R.id.nav_settings) {
                Toast.makeText(this, R.string.settings_placeholder, Toast.LENGTH_SHORT).show();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        RecyclerView recyclerView = findViewById(R.id.main_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<Integer> options = Arrays.asList(
                R.string.option_scale_signal,
                R.string.option_units
        );
        recyclerView.setAdapter(new OptionsAdapter(options, this::handleOptionClick));

        mainContentView = findViewById(R.id.main);
        bannerAdView = findViewById(R.id.ad_container_view);

        mainContentView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mainContentView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        BannerAdSize size = getStickyAdSize();
                        loadStickyBanner(size);
                    }
                }
        );

        ViewCompat.setOnApplyWindowInsetsListener(mainContentView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @NonNull
    private BannerAdSize getStickyAdSize() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int adWidthPx = mainContentView.getWidth();
        if (adWidthPx == 0) {
            adWidthPx = dm.widthPixels;
        }
        int adWidthDp = Math.round(adWidthPx / dm.density);
        return BannerAdSize.stickySize(this, adWidthDp);
    }

    private void loadStickyBanner(@NonNull BannerAdSize adSize) {
        if (bannerAdView == null) return;

        bannerAdView.setAdSize(adSize);

        bannerAdView.setAdUnitId("demo-banner-yandex"); // <-- тестовый ID

        bannerAdView.setBannerAdEventListener(new BannerAdEventListener() {
            @Override public void onAdLoaded() {
                if (isDestroyed() && bannerAdView != null) bannerAdView.destroy();
            }
            @Override public void onAdFailedToLoad(@NonNull AdRequestError error) {

            }
            @Override public void onAdClicked() { }
            @Override public void onLeftApplication() { }
            @Override public void onReturnedToApplication() { }
            @Override public void onImpression(@Nullable ImpressionData data) { }
        });

        AdRequest request = new AdRequest.Builder().build();
        bannerAdView.loadAd(request);
    }

    private void showVersionDialog() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.version_dialog_message)
                .setPositiveButton(R.string.ok, (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (bannerAdView != null) {
            bannerAdView.destroy();
            bannerAdView = null;
        }
        super.onDestroy();
    }

    private void handleOptionClick(@StringRes int optionResId) {
        if (optionResId == R.string.option_scale_signal) {
            startActivity(new Intent(this, ScaleSignalActivity.class));
        } else if (optionResId == R.string.option_units) {
            startActivity(new Intent(this, UnitsActivity.class));
        }
    }

    private static class OptionsAdapter extends RecyclerView.Adapter<OptionsAdapter.OptionViewHolder> {

        private final List<Integer> items;
        private final OnOptionClickListener listener;

        OptionsAdapter(List<Integer> items, OnOptionClickListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public OptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.item_main_option, parent, false);
            return new OptionViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull OptionViewHolder holder, int position) {
            holder.bind(items.get(position), listener);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class OptionViewHolder extends RecyclerView.ViewHolder {
            private final TextView titleView;

            OptionViewHolder(@NonNull View itemView) {
                super(itemView);
                titleView = itemView.findViewById(R.id.option_title);
            }

            void bind(@StringRes int titleResId, OnOptionClickListener listener) {
                titleView.setText(titleResId);
                itemView.setOnClickListener(v -> listener.onOptionClick(titleResId));
            }
        }

        interface OnOptionClickListener {
            void onOptionClick(@StringRes int optionResId);
        }
    }
}