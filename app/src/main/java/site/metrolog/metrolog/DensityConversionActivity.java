package site.metrolog.metrolog;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
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
import java.util.EnumMap;
import java.util.Locale;

public class DensityConversionActivity extends AppCompatActivity {

    private enum DensityUnit {
        KG_M3(R.id.density_kg_m3_input, R.id.density_kg_m3_info, R.string.density_symbol_kg_m3, R.string.density_info_kg_m3, 1d),
        T_M3(R.id.density_t_m3_input, R.id.density_t_m3_info, R.string.density_symbol_t_m3, R.string.density_info_t_m3, 1_000d),
        G_M3(R.id.density_g_m3_input, R.id.density_g_m3_info, R.string.density_symbol_g_m3, R.string.density_info_g_m3, 0.001d),
        MG_M3(R.id.density_mg_m3_input, R.id.density_mg_m3_info, R.string.density_symbol_mg_m3, R.string.density_info_mg_m3, 0.000001d),
        KG_L(R.id.density_kg_l_input, R.id.density_kg_l_info, R.string.density_symbol_kg_l, R.string.density_info_kg_l, 1_000d),
        G_L(R.id.density_g_l_input, R.id.density_g_l_info, R.string.density_symbol_g_l, R.string.density_info_g_l, 1d),
        G_ML(R.id.density_g_ml_input, R.id.density_g_ml_info, R.string.density_symbol_g_ml, R.string.density_info_g_ml, 1_000d),
        LB_YD3(R.id.density_lb_yd3_input, R.id.density_lb_yd3_info, R.string.density_symbol_lb_yd3, R.string.density_info_lb_yd3, 0.5932764212577829d),
        LB_FT3(R.id.density_lb_ft3_input, R.id.density_lb_ft3_info, R.string.density_symbol_lb_ft3, R.string.density_info_lb_ft3, 16.018463373960138d),
        LB_IN3(R.id.density_lb_in3_input, R.id.density_lb_in3_info, R.string.density_symbol_lb_in3, R.string.density_info_lb_in3, 27679.904710203125d),
        LB_GAL(R.id.density_lb_gal_input, R.id.density_lb_gal_info, R.string.density_symbol_lb_gal, R.string.density_info_lb_gal, 119.82642731689663d),
        LB_BU(R.id.density_lb_bu_input, R.id.density_lb_bu_info, R.string.density_symbol_lb_bu, R.string.density_info_lb_bu, 12.871859780974471d),
        OZ_YD3(R.id.density_oz_yd3_input, R.id.density_oz_yd3_info, R.string.density_symbol_oz_yd3, R.string.density_info_oz_yd3, 0.03707977632861143d),
        OZ_FT3(R.id.density_oz_ft3_input, R.id.density_oz_ft3_info, R.string.density_symbol_oz_ft3, R.string.density_info_oz_ft3, 1.0011539608725086d),
        OZ_IN3(R.id.density_oz_in3_input, R.id.density_oz_in3_info, R.string.density_symbol_oz_in3, R.string.density_info_oz_in3, 1729.9940443876953d),
        OZ_GAL(R.id.density_oz_gal_input, R.id.density_oz_gal_info, R.string.density_symbol_oz_gal, R.string.density_info_oz_gal, 7.489151707306039d),
        OZ_BU(R.id.density_oz_bu_input, R.id.density_oz_bu_info, R.string.density_symbol_oz_bu, R.string.density_info_oz_bu, 0.8044912363109045d),
        LB_GAL_UK(R.id.density_lb_gal_uk_input, R.id.density_lb_gal_uk_info, R.string.density_symbol_lb_gal_uk, R.string.density_info_lb_gal_uk, 99.7763726631017d),
        OZ_GAL_UK(R.id.density_oz_gal_uk_input, R.id.density_oz_gal_uk_info, R.string.density_symbol_oz_gal_uk, R.string.density_info_oz_gal_uk, 6.236023291443856d);

        private final int inputId;
        private final int infoId;
        private final int symbolResId;
        private final int infoResId;
        private final double toKgPerM3Factor;

        DensityUnit(@IdRes int inputId,
                    @IdRes int infoId,
                    @StringRes int symbolResId,
                    @StringRes int infoResId,
                    double toKgPerM3Factor) {
            this.inputId = inputId;
            this.infoId = infoId;
            this.symbolResId = symbolResId;
            this.infoResId = infoResId;
            this.toKgPerM3Factor = toKgPerM3Factor;
        }

        double toKgPerM3(double value) {
            return value * toKgPerM3Factor;
        }

        double fromKgPerM3(double value) {
            return value / toKgPerM3Factor;
        }
    }

    private final EnumMap<DensityUnit, TextInputEditText> inputs = new EnumMap<>(DensityUnit.class);

    @Nullable
    private BannerAdView bannerAdView;
    private View mainContentView;
    private boolean isUpdating = false;
    private final Locale numberLocale = Locale.getDefault();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_density_conversion);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.density_screen_title);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        mainContentView = findViewById(R.id.density_root);
        bannerAdView = findViewById(R.id.ad_container_view);

        for (DensityUnit unit : DensityUnit.values()) {
            TextInputEditText editText = findViewById(unit.inputId);
            inputs.put(unit, editText);
            editText.addTextChangedListener(new DensityTextWatcher(unit));
            configureInfoButton(unit.infoId, unit.symbolResId, unit.infoResId);
        }

        setupInsets();
        loadAdWhenReady();
    }

    private void configureInfoButton(@IdRes int viewId, @StringRes int symbolResId, @StringRes int infoResId) {
        View view = findViewById(viewId);
        view.setOnClickListener(v -> showInfoDialog(symbolResId, infoResId));
    }

    private void showInfoDialog(@StringRes int symbolResId, @StringRes int infoResId) {
        String message = getString(symbolResId) + "\n" + getString(infoResId);
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(R.string.ok, null)
                .show();
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

    private class DensityTextWatcher implements TextWatcher {

        private final DensityUnit unit;

        DensityTextWatcher(@NonNull DensityUnit unit) {
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

    private void clearOtherFields(@NonNull DensityUnit source) {
        isUpdating = true;
        try {
            for (DensityUnit unit : DensityUnit.values()) {
                if (unit != source) {
                    TextInputEditText editText = inputs.get(unit);
                    if (editText != null) {
                        setTextIfChanged(editText, "");
                    }
                }
            }
        } finally {
            isUpdating = false;
        }
    }

    private void updateAllUnits(@NonNull DensityUnit source, double value, int baseFractionDigits) {
        isUpdating = true;
        try {
            double baseValue = source.toKgPerM3(value);
            for (DensityUnit unit : DensityUnit.values()) {
                if (unit == source) {
                    continue;
                }
                TextInputEditText editText = inputs.get(unit);
                if (editText == null) {
                    continue;
                }
                double converted = unit.fromKgPerM3(baseValue);
                String formatted = formatValue(converted, baseFractionDigits);
                setTextIfChanged(editText, formatted);
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

    private String formatValue(double value, int baseFractionDigits) {
        int fractionDigits = Math.max(3, baseFractionDigits);
        int firstNonZero = findFirstNonZeroFractionDigit(value);
        if (firstNonZero > 3 && firstNonZero > fractionDigits) {
            fractionDigits = Math.min(10, Math.max(fractionDigits, firstNonZero));
        }
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(numberLocale);
        DecimalFormat format = new DecimalFormat();
        format.setDecimalFormatSymbols(symbols);
        format.setGroupingUsed(false);
        format.setMinimumFractionDigits(fractionDigits);
        format.setMaximumFractionDigits(fractionDigits);
        format.setRoundingMode(RoundingMode.HALF_UP);
        return format.format(BigDecimal.valueOf(value));
    }

    private int findFirstNonZeroFractionDigit(double value) {
        BigDecimal decimal = BigDecimal.valueOf(Math.abs(value));
        BigDecimal fraction = decimal.remainder(BigDecimal.ONE);
        if (fraction.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }
        BigDecimal current = fraction;
        for (int i = 1; i <= 10; i++) {
            current = current.movePointRight(1);
            int digit = current.intValue();
            if (digit != 0) {
                return i;
            }
            current = current.remainder(BigDecimal.ONE);
        }
        return 10;
    }

    private void setTextIfChanged(@NonNull TextInputEditText editText, @NonNull String value) {
        CharSequence current = editText.getText();
        if (!TextUtils.equals(current, value)) {
            editText.setText(value);
            editText.setSelection(value.length());
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
