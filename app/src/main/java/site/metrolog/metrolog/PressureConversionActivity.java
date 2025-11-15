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

public class PressureConversionActivity extends AppCompatActivity {

    private enum PressureUnit {
        PASCAL(R.id.pressure_pascal_input, R.id.pressure_pascal_info, R.string.pressure_symbol_pascal, R.string.pressure_info_pascal, 1d),
        KGF_CM2(R.id.pressure_kgf_cm2_input, R.id.pressure_kgf_cm2_info, R.string.pressure_symbol_kgf_cm2, R.string.pressure_info_kgf_cm2, 98066.5d),
        KGF_M2(R.id.pressure_kgf_m2_input, R.id.pressure_kgf_m2_info, R.string.pressure_symbol_kgf_m2, R.string.pressure_info_kgf_m2, 9.80665d),
        MILLIBAR(R.id.pressure_millibar_input, R.id.pressure_millibar_info, R.string.pressure_symbol_millibar, R.string.pressure_info_millibar, 100d),
        BAR(R.id.pressure_bar_input, R.id.pressure_bar_info, R.string.pressure_symbol_bar, R.string.pressure_info_bar, 100_000d),
        KILOPASCAL(R.id.pressure_kpa_input, R.id.pressure_kpa_info, R.string.pressure_symbol_kpa, R.string.pressure_info_kpa, 1_000d),
        MEGAPASCAL(R.id.pressure_mpa_input, R.id.pressure_mpa_info, R.string.pressure_symbol_mpa, R.string.pressure_info_mpa, 1_000_000d),
        NEWTON_M2(R.id.pressure_newton_m2_input, R.id.pressure_newton_m2_info, R.string.pressure_symbol_n_m2, R.string.pressure_info_n_m2, 1d),
        KILONEWTON_M2(R.id.pressure_kilonewton_m2_input, R.id.pressure_kilonewton_m2_info, R.string.pressure_symbol_kn_m2, R.string.pressure_info_kn_m2, 1_000d),
        MEGANEWTON_M2(R.id.pressure_meganewton_m2_input, R.id.pressure_meganewton_m2_info, R.string.pressure_symbol_mn_m2, R.string.pressure_info_mn_m2, 1_000_000d),
        NEWTON_CM2(R.id.pressure_newton_cm2_input, R.id.pressure_newton_cm2_info, R.string.pressure_symbol_n_cm2, R.string.pressure_info_n_cm2, 10_000d),
        NEWTON_MM2(R.id.pressure_newton_mm2_input, R.id.pressure_newton_mm2_info, R.string.pressure_symbol_n_mm2, R.string.pressure_info_n_mm2, 1_000_000d),
        PHYSICAL_ATMOSPHERE(R.id.pressure_physical_atmosphere_input, R.id.pressure_physical_atmosphere_info, R.string.pressure_symbol_atm_physical, R.string.pressure_info_atm_physical, 101_325d),
        TECHNICAL_ATMOSPHERE(R.id.pressure_technical_atmosphere_input, R.id.pressure_technical_atmosphere_info, R.string.pressure_symbol_atm_technical, R.string.pressure_info_atm_technical, 98_066.5d),
        MM_H2O(R.id.pressure_mm_h2o_input, R.id.pressure_mm_h2o_info, R.string.pressure_symbol_mm_h2o, R.string.pressure_info_mm_h2o, 9.80665d),
        CM_H2O(R.id.pressure_cm_h2o_input, R.id.pressure_cm_h2o_info, R.string.pressure_symbol_cm_h2o, R.string.pressure_info_cm_h2o, 98.0665d),
        M_H2O(R.id.pressure_m_h2o_input, R.id.pressure_m_h2o_info, R.string.pressure_symbol_m_h2o, R.string.pressure_info_m_h2o, 9_806.65d),
        IN_H2O(R.id.pressure_in_h2o_input, R.id.pressure_in_h2o_info, R.string.pressure_symbol_in_h2o, R.string.pressure_info_in_h2o, 249.08891d),
        FT_H2O(R.id.pressure_ft_h2o_input, R.id.pressure_ft_h2o_info, R.string.pressure_symbol_ft_h2o, R.string.pressure_info_ft_h2o, 2_989.06692d),
        MM_HG(R.id.pressure_mm_hg_input, R.id.pressure_mm_hg_info, R.string.pressure_symbol_mm_hg, R.string.pressure_info_mm_hg, 133.322387415d),
        CM_HG(R.id.pressure_cm_hg_input, R.id.pressure_cm_hg_info, R.string.pressure_symbol_cm_hg, R.string.pressure_info_cm_hg, 1_333.22387415d),
        IN_HG(R.id.pressure_in_hg_input, R.id.pressure_in_hg_info, R.string.pressure_symbol_in_hg, R.string.pressure_info_in_hg, 3_386.38815789d),
        KSI(R.id.pressure_ksi_input, R.id.pressure_ksi_info, R.string.pressure_symbol_ksi, R.string.pressure_info_ksi, 6_894_757.29316836d),
        PSI(R.id.pressure_psi_input, R.id.pressure_psi_info, R.string.pressure_symbol_psi, R.string.pressure_info_psi, 6_894.75729316836d),
        PSF(R.id.pressure_psf_input, R.id.pressure_psf_info, R.string.pressure_symbol_psf, R.string.pressure_info_psf, 47.88025898033584d),
        TSI_US(R.id.pressure_tsi_us_input, R.id.pressure_tsi_us_info, R.string.pressure_symbol_tsi_us, R.string.pressure_info_tsi_us, 13_789_514.586336723d),
        TSF_US(R.id.pressure_tsf_us_input, R.id.pressure_tsf_us_info, R.string.pressure_symbol_tsf_us, R.string.pressure_info_tsf_us, 95_760.51796067167d),
        TSI_UK(R.id.pressure_tsi_uk_input, R.id.pressure_tsi_uk_info, R.string.pressure_symbol_tsi_uk, R.string.pressure_info_tsi_uk, 15_444_256.336697128d),
        TSF_UK(R.id.pressure_tsf_uk_input, R.id.pressure_tsf_uk_info, R.string.pressure_symbol_tsf_uk, R.string.pressure_info_tsf_uk, 107_251.78011595226d);

        private final int inputId;
        private final int infoId;
        private final int symbolResId;
        private final int infoResId;
        private final double toPascalFactor;

        PressureUnit(@IdRes int inputId,
                     @IdRes int infoId,
                     @StringRes int symbolResId,
                     @StringRes int infoResId,
                     double toPascalFactor) {
            this.inputId = inputId;
            this.infoId = infoId;
            this.symbolResId = symbolResId;
            this.infoResId = infoResId;
            this.toPascalFactor = toPascalFactor;
        }

        double toPascal(double value) {
            return value * toPascalFactor;
        }

        double fromPascal(double pascalValue) {
            return pascalValue / toPascalFactor;
        }
    }

    private final EnumMap<PressureUnit, TextInputEditText> inputs = new EnumMap<>(PressureUnit.class);

    @Nullable
    private BannerAdView bannerAdView;
    private View mainContentView;
    private boolean isUpdating = false;
    private final Locale numberLocale = Locale.getDefault();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pressure_conversion);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.pressure_screen_title);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        mainContentView = findViewById(R.id.pressure_root);
        bannerAdView = findViewById(R.id.ad_container_view);

        for (PressureUnit unit : PressureUnit.values()) {
            TextInputEditText editText = findViewById(unit.inputId);
            inputs.put(unit, editText);
            editText.addTextChangedListener(new PressureTextWatcher(unit));
            configureInfoButton(unit.infoId, unit.symbolResId, unit.infoResId);
        }

        setupInsets();
        loadAdWhenReady();
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

    private class PressureTextWatcher implements TextWatcher {

        private final PressureUnit unit;

        PressureTextWatcher(@NonNull PressureUnit unit) {
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

    private void clearOtherFields(@NonNull PressureUnit source) {
        isUpdating = true;
        try {
            for (PressureUnit unit : PressureUnit.values()) {
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

    private void updateAllUnits(@NonNull PressureUnit source, double value, int baseFractionDigits) {
        isUpdating = true;
        try {
            double pascalValue = source.toPascal(value);
            for (PressureUnit unit : PressureUnit.values()) {
                if (unit == source) {
                    continue;
                }
                TextInputEditText editText = inputs.get(unit);
                if (editText == null) {
                    continue;
                }
                double converted = unit.fromPascal(pascalValue);
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
