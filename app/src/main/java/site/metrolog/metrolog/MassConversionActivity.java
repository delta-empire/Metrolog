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

public class MassConversionActivity extends AppCompatActivity {

    private enum MassUnit {
        KILOGRAM(R.id.mass_kilogram_input, R.id.mass_kilogram_info, R.string.unit_symbol_mass_kilogram, R.string.unit_info_mass_kilogram, 1d),
        MILLIGRAM(R.id.mass_milligram_input, R.id.mass_milligram_info, R.string.unit_symbol_mass_milligram, R.string.unit_info_mass_milligram, 1e-6d),
        GRAM(R.id.mass_gram_input, R.id.mass_gram_info, R.string.unit_symbol_mass_gram, R.string.unit_info_mass_gram, 1e-3d),
        CENTNER(R.id.mass_centner_input, R.id.mass_centner_info, R.string.unit_symbol_mass_centner, R.string.unit_info_mass_centner, 100d),
        TONNE(R.id.mass_tonne_input, R.id.mass_tonne_info, R.string.unit_symbol_mass_tonne, R.string.unit_info_mass_tonne, 1_000d),
        CARAT(R.id.mass_carat_input, R.id.mass_carat_info, R.string.unit_symbol_mass_carat, R.string.unit_info_mass_carat, 0.0002d),
        NEWTON(R.id.mass_newton_input, R.id.mass_newton_info, R.string.unit_symbol_mass_newton, R.string.unit_info_mass_newton, 0.1019716213d),
        STONE(R.id.mass_stone_input, R.id.mass_stone_info, R.string.unit_symbol_mass_stone, R.string.unit_info_mass_stone, 6.35029318d),
        POUND(R.id.mass_pound_input, R.id.mass_pound_info, R.string.unit_symbol_mass_pound, R.string.unit_info_mass_pound, 0.45359237d),
        OUNCE(R.id.mass_ounce_input, R.id.mass_ounce_info, R.string.unit_symbol_mass_ounce, R.string.unit_info_mass_ounce, 0.028349523125d),
        DRAM_AV(R.id.mass_dram_av_input, R.id.mass_dram_av_info, R.string.unit_symbol_mass_dram_av, R.string.unit_info_mass_dram_av, 0.0017718451953125d),
        GRAIN(R.id.mass_grain_input, R.id.mass_grain_info, R.string.unit_symbol_mass_grain, R.string.unit_info_mass_grain, 0.00006479891d),
        SHORT_TON(R.id.mass_short_ton_input, R.id.mass_short_ton_info, R.string.unit_symbol_mass_short_ton, R.string.unit_info_mass_short_ton, 907.18474d),
        SHORT_CWT(R.id.mass_short_cwt_input, R.id.mass_short_cwt_info, R.string.unit_symbol_mass_short_cwt, R.string.unit_info_mass_short_cwt, 45.359237d),
        LONG_TON(R.id.mass_long_ton_input, R.id.mass_long_ton_info, R.string.unit_symbol_mass_long_ton, R.string.unit_info_mass_long_ton, 1_016.0469088d),
        LONG_CWT(R.id.mass_long_cwt_input, R.id.mass_long_cwt_info, R.string.unit_symbol_mass_long_cwt, R.string.unit_info_mass_long_cwt, 50.80234544d),
        TROY_POUND(R.id.mass_troy_pound_input, R.id.mass_troy_pound_info, R.string.unit_symbol_mass_troy_pound, R.string.unit_info_mass_troy_pound, 0.3732417216d),
        TROY_OUNCE(R.id.mass_troy_ounce_input, R.id.mass_troy_ounce_info, R.string.unit_symbol_mass_troy_ounce, R.string.unit_info_mass_troy_ounce, 0.0311034768d),
        PENNYWEIGHT(R.id.mass_pennyweight_input, R.id.mass_pennyweight_info, R.string.unit_symbol_mass_pennyweight, R.string.unit_info_mass_pennyweight, 0.00155517384d),
        MITE(R.id.mass_mite_input, R.id.mass_mite_info, R.string.unit_symbol_mass_mite, R.string.unit_info_mass_mite, 0.000003239945d),
        TROY_DRAM(R.id.mass_troy_dram_input, R.id.mass_troy_dram_info, R.string.unit_symbol_mass_troy_dram, R.string.unit_info_mass_troy_dram, 0.0038879346d);

        private final int inputId;
        private final int infoId;
        private final int symbolResId;
        private final int infoResId;
        private final double toKilogramFactor;

        MassUnit(@IdRes int inputId,
                 @IdRes int infoId,
                 @StringRes int symbolResId,
                 @StringRes int infoResId,
                 double toKilogramFactor) {
            this.inputId = inputId;
            this.infoId = infoId;
            this.symbolResId = symbolResId;
            this.infoResId = infoResId;
            this.toKilogramFactor = toKilogramFactor;
        }

        double toKilogram(double value) {
            return value * toKilogramFactor;
        }

        double fromKilogram(double kilogramValue) {
            return kilogramValue / toKilogramFactor;
        }
    }

    private final EnumMap<MassUnit, TextInputEditText> inputs = new EnumMap<>(MassUnit.class);

    @Nullable
    private BannerAdView bannerAdView;
    private View mainContentView;
    private boolean isUpdating = false;
    private final Locale numberLocale = Locale.getDefault();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mass_conversion);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.mass_screen_title);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        mainContentView = findViewById(R.id.mass_root);
        bannerAdView = findViewById(R.id.ad_container_view);

        for (MassUnit unit : MassUnit.values()) {
            TextInputEditText editText = findViewById(unit.inputId);
            inputs.put(unit, editText);
            editText.addTextChangedListener(new MassTextWatcher(unit));
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

    private class MassTextWatcher implements TextWatcher {

        private final MassUnit unit;

        MassTextWatcher(@NonNull MassUnit unit) {
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

    private void clearOtherFields(@NonNull MassUnit source) {
        isUpdating = true;
        try {
            for (MassUnit unit : MassUnit.values()) {
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

    private void updateAllUnits(@NonNull MassUnit source, double value, int baseFractionDigits) {
        isUpdating = true;
        try {
            double kilogramValue = source.toKilogram(value);
            for (MassUnit unit : MassUnit.values()) {
                if (unit == source) {
                    continue;
                }
                TextInputEditText editText = inputs.get(unit);
                if (editText == null) {
                    continue;
                }
                double converted = unit.fromKilogram(kilogramValue);
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
