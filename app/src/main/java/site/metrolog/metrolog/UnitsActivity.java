package site.metrolog.metrolog;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yandex.mobile.ads.banner.BannerAdEventListener;
import com.yandex.mobile.ads.banner.BannerAdSize;
import com.yandex.mobile.ads.banner.BannerAdView;
import com.yandex.mobile.ads.common.AdRequest;
import com.yandex.mobile.ads.common.AdRequestError;
import com.yandex.mobile.ads.common.ImpressionData;

import java.util.Arrays;
import java.util.List;

public class UnitsActivity extends AppCompatActivity {

    @Nullable
    private BannerAdView bannerAdView;
    private View mainContentView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_units);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.units_screen_title);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        mainContentView = findViewById(R.id.units_root);
        bannerAdView = findViewById(R.id.ad_container_view);

        RecyclerView recyclerView = findViewById(R.id.units_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<Integer> items = Arrays.asList(
                R.string.units_option_temperature,
                R.string.units_option_time
        );
        recyclerView.setAdapter(new UnitsAdapter(items, this::handleUnitClick));

        ViewCompat.setOnApplyWindowInsetsListener(mainContentView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mainContentView.getViewTreeObserver().addOnGlobalLayoutListener(
                new android.view.ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mainContentView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        BannerAdSize adSize = getStickyAdSize();
                        loadStickyBanner(adSize);
                    }
                }
        );
    }

    private void handleUnitClick(@StringRes int optionResId) {
        if (optionResId == R.string.units_option_temperature) {
            startActivity(new Intent(this, TemperatureConversionActivity.class));
        } else if (optionResId == R.string.units_option_time) {
            startActivity(new Intent(this, TimeConversionActivity.class));
        }
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
        if (bannerAdView == null) {
            return;
        }
        bannerAdView.setAdSize(adSize);
        bannerAdView.setAdUnitId("demo-banner-yandex");
        bannerAdView.setBannerAdEventListener(new BannerAdEventListener() {
            @Override
            public void onAdLoaded() {
                if (isDestroyed() && bannerAdView != null) {
                    bannerAdView.destroy();
                }
            }

            @Override
            public void onAdFailedToLoad(@NonNull AdRequestError adRequestError) {
            }

            @Override
            public void onAdClicked() {
            }

            @Override
            public void onLeftApplication() {
            }

            @Override
            public void onReturnedToApplication() {
            }

            @Override
            public void onImpression(@Nullable ImpressionData impressionData) {
            }
        });

        AdRequest request = new AdRequest.Builder().build();
        bannerAdView.loadAd(request);
    }

    @Override
    protected void onDestroy() {
        if (bannerAdView != null) {
            bannerAdView.destroy();
            bannerAdView = null;
        }
        super.onDestroy();
    }

    private static class UnitsAdapter extends RecyclerView.Adapter<UnitsAdapter.UnitViewHolder> {

        private final List<Integer> items;
        private final OnUnitClickListener listener;

        UnitsAdapter(@NonNull List<Integer> items, @NonNull OnUnitClickListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public UnitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.item_main_option, parent, false);
            return new UnitViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull UnitViewHolder holder, int position) {
            holder.bind(items.get(position), listener);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class UnitViewHolder extends RecyclerView.ViewHolder {

            UnitViewHolder(@NonNull View itemView) {
                super(itemView);
            }

            void bind(@StringRes int titleResId, @NonNull OnUnitClickListener listener) {
                android.widget.TextView title = itemView.findViewById(R.id.option_title);
                title.setText(titleResId);
                itemView.setOnClickListener(v -> listener.onUnitClick(titleResId));
            }
        }

        interface OnUnitClickListener {
            void onUnitClick(@StringRes int optionResId);
        }
    }
}
