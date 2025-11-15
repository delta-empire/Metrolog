package site.metrolog.metrolog;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.yandex.mobile.ads.banner.BannerAdEventListener;
import com.yandex.mobile.ads.banner.BannerAdSize;
import com.yandex.mobile.ads.banner.BannerAdView;
import com.yandex.mobile.ads.common.AdRequest;
import com.yandex.mobile.ads.common.AdRequestError;
import com.yandex.mobile.ads.common.ImpressionData;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class AccelerationConversionActivity extends AppCompatActivity {

    private enum Unit {
        MPS2,
        KMPS2,
        MMPS2,
        MILEPS2,
        FOOTPS2,
        INCHPS2
    }

    private TextInputEditText mps2Input;
    private TextInputEditText kmps2Input;
    private TextInputEditText mmps2Input;
    private TextInputEditText mileps2Input;
    private TextInputEditText footps2Input;
    private TextInputEditText inchps2Input;

    @Nullable
    private BannerAdView bannerAdView;
    private View mainContentView;

    private boolean isUpdating = false;
    private final Locale numberLocale = Locale.getDefault();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_acceleration_conversion);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.acceleration_screen_title);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        mainContentView = findViewById(R.id.acceleration_root);
        bannerAdView = findViewById(R.id.ad_container_view);

        mps2Input = findViewById(R.id.acceleration_mps2_input);
        kmps2Input = findViewById(R.id.acceleration_kmps2_input);
        mmps2Input = findViewById(R.id.acceleration_mmps2_input);
        mileps2Input = findViewById(R.id.acceleration_mileps2_input);
        footps2Input = findViewById(R.id.acceleration_footps2_input);
        inchps2Input = findViewById(R.id.acceleration_inchps2_input);

        setupInfoButtons();
        setupWatchers();
        setupInsets();
        loadAdWhenReady();
    }

    private void setupInfoButtons() {
        configureInfoButton(R.id.acceleration_mps2_info, R.string.unit_symbol_acceleration_mps2, R.string.unit_info_acceleration_mps2);
        configureInfoButton(R.id.acceleration_kmps2_info, R.string.unit_symbol_acceleration_kmps2, R.string.unit_info_acceleration_kmps2);
        configureInfoButton(R.id.acceleration_mmps2_info, R.string.unit_symbol_acceleration_mmps2, R.string.unit_info_acceleration_mmps2);
        configureInfoButton(R.id.acceleration_mileps2_info, R.string.unit_symbol_acceleration_mileps2, R.string.unit_info_acceleration_mileps2);
        configureInfoButton(R.id.acceleration_footps2_info, R.string.unit_symbol_acceleration_footps2, R.string.unit_info_acceleration_footps2);
        configureInfoButton(R.id.acceleration_inchps2_info, R.string.unit_symbol_acceleration_inchps2, R.string.unit_info_acceleration_inchps2);
    }

    private void configureInfoButton(int viewId, int symbolResId, int descriptionResId) {
        View view = findViewById(viewId);
        view.setOnClickListener(v -> showInfoDialog(symbolResId, descriptionResId));
    }

    private void setupWatchers() {
        mps2Input.addTextChangedListener(new ConversionWatcher(Unit.MPS2));
        kmps2Input.addTextChangedListener(new ConversionWatcher(Unit.KMPS2));
        mmps2Input.addTextChangedListener(new ConversionWatcher(Unit.MMPS2));
        mileps2Input.addTextChangedListener(new ConversionWatcher(Unit.MILEPS2));
        footps2Input.addTextChangedListener(new ConversionWatcher(Unit.FOOTPS2));
        inchps2Input.addTextChangedListener(new ConversionWatcher(Unit.INCHPS2));
    }

    private void setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(mainContentView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loadAdWhenReady() {
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

    private void showInfoDialog(int symbolResId, int descriptionResId) {
        String message = getString(symbolResId) + "\n" + getString(descriptionResId);
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(R.string.ok, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private class ConversionWatcher implements TextWatcher {

        private final Unit unit;

        ConversionWatcher(Unit unit) {
            this.unit = unit;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (isUpdating) {
                return;
            }
            String raw = s.toString().trim();
            if (raw.isEmpty()) {
                clearOtherFields(unit);
                return;
            }
            if (isIncompleteNumber(raw)) {
                return;
            }
            Double value = parseDouble(raw);
            if (value == null) {
                clearOtherFields(unit);
                return;
            }
            int fractionDigits = Math.max(3, countFractionDigits(raw));
            updateAllUnits(unit, value, fractionDigits);
        }
    }

    private void clearOtherFields(@NonNull Unit source) {
        isUpdating = true;
        try {
            if (source != Unit.MPS2) {
                setTextIfChanged(mps2Input, "");
            }
            if (source != Unit.KMPS2) {
                setTextIfChanged(kmps2Input, "");
            }
            if (source != Unit.MMPS2) {
                setTextIfChanged(mmps2Input, "");
            }
            if (source != Unit.MILEPS2) {
                setTextIfChanged(mileps2Input, "");
            }
            if (source != Unit.FOOTPS2) {
                setTextIfChanged(footps2Input, "");
            }
            if (source != Unit.INCHPS2) {
                setTextIfChanged(inchps2Input, "");
            }
        } finally {
            isUpdating = false;
        }
    }

    private void updateAllUnits(@NonNull Unit source, double value, int fractionDigits) {
        isUpdating = true;
        try {
            double baseValue = toBase(source, value);

            if (source != Unit.MPS2) {
                setTextIfChanged(mps2Input, formatValue(fromBase(Unit.MPS2, baseValue), fractionDigits));
            }
            if (source != Unit.KMPS2) {
                setTextIfChanged(kmps2Input, formatValue(fromBase(Unit.KMPS2, baseValue), fractionDigits));
            }
            if (source != Unit.MMPS2) {
                setTextIfChanged(mmps2Input, formatValue(fromBase(Unit.MMPS2, baseValue), fractionDigits));
            }
            if (source != Unit.MILEPS2) {
                setTextIfChanged(mileps2Input, formatValue(fromBase(Unit.MILEPS2, baseValue), fractionDigits));
            }
            if (source != Unit.FOOTPS2) {
                setTextIfChanged(footps2Input, formatValue(fromBase(Unit.FOOTPS2, baseValue), fractionDigits));
            }
            if (source != Unit.INCHPS2) {
                setTextIfChanged(inchps2Input, formatValue(fromBase(Unit.INCHPS2, baseValue), fractionDigits));
            }
        } finally {
            isUpdating = false;
        }
    }

    @Nullable
    private Double parseDouble(@NonNull String value) {
        String normalized = value.replace(',', '.');
        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int countFractionDigits(@NonNull String value) {
        int pointIndex = Math.max(value.indexOf('.'), value.indexOf(','));
        if (pointIndex < 0) {
            return 0;
        }
        return value.length() - pointIndex - 1;
    }

    private boolean isIncompleteNumber(@NonNull String value) {
        return "-".equals(value)
                || ".".equals(value)
                || ",".equals(value)
                || "-.".equals(value)
                || "-,".equals(value);
    }

    private String formatValue(double value, int fractionDigits) {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(numberLocale);
        DecimalFormat format = new DecimalFormat();
        format.setDecimalFormatSymbols(symbols);
        format.setGroupingUsed(false);
        format.setMinimumFractionDigits(fractionDigits);
        format.setMaximumFractionDigits(fractionDigits);
        format.setRoundingMode(RoundingMode.HALF_UP);
        return format.format(BigDecimal.valueOf(value));
    }

    private void setTextIfChanged(@NonNull TextInputEditText editText, @NonNull String value) {
        CharSequence current = editText.getText();
        if (!TextUtils.equals(current, value)) {
            editText.setText(value);
            int length = value.length();
            editText.setSelection(length);
        }
    }

    private double toBase(@NonNull Unit unit, double value) {
        return value * getFactor(unit);
    }

    private double fromBase(@NonNull Unit unit, double baseValue) {
        return baseValue / getFactor(unit);
    }

    private double getFactor(@NonNull Unit unit) {
        switch (unit) {
            case MPS2:
                return 1d;
            case KMPS2:
                return 1000d;
            case MMPS2:
                return 0.001d;
            case MILEPS2:
                return 1609.344d;
            case FOOTPS2:
                return 0.3048d;
            case INCHPS2:
                return 0.0254d;
            default:
                return 1d;
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
}
