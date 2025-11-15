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

public class LengthConversionActivity extends AppCompatActivity {

    private enum LengthUnit {
        METER(R.id.length_meter_input, R.id.length_meter_info, R.string.unit_symbol_length_meter, R.string.unit_info_length_meter, 1d),
        NANOMETER(R.id.length_nanometer_input, R.id.length_nanometer_info, R.string.unit_symbol_length_nanometer, R.string.unit_info_length_nanometer, 1e-9d),
        MICRON(R.id.length_micron_input, R.id.length_micron_info, R.string.unit_symbol_length_micron, R.string.unit_info_length_micron, 1e-6d),
        MILLIMETER(R.id.length_millimeter_input, R.id.length_millimeter_info, R.string.unit_symbol_length_millimeter, R.string.unit_info_length_millimeter, 1e-3d),
        CENTIMETER(R.id.length_centimeter_input, R.id.length_centimeter_info, R.string.unit_symbol_length_centimeter, R.string.unit_info_length_centimeter, 1e-2d),
        DECIMETER(R.id.length_decimeter_input, R.id.length_decimeter_info, R.string.unit_symbol_length_decimeter, R.string.unit_info_length_decimeter, 1e-1d),
        KILOMETER(R.id.length_kilometer_input, R.id.length_kilometer_info, R.string.unit_symbol_length_kilometer, R.string.unit_info_length_kilometer, 1_000d),
        ANGSTROM(R.id.length_angstrom_input, R.id.length_angstrom_info, R.string.unit_symbol_length_angstrom, R.string.unit_info_length_angstrom, 1e-10d),
        LEAGUE(R.id.length_league_input, R.id.length_league_info, R.string.unit_symbol_length_league, R.string.unit_info_length_league, 4_828.032d),
        MILE(R.id.length_mile_input, R.id.length_mile_info, R.string.unit_symbol_length_mile, R.string.unit_info_length_mile, 1_609.344d),
        FURLONG(R.id.length_furlong_input, R.id.length_furlong_info, R.string.unit_symbol_length_furlong, R.string.unit_info_length_furlong, 201.168d),
        CHAIN(R.id.length_chain_input, R.id.length_chain_info, R.string.unit_symbol_length_chain, R.string.unit_info_length_chain, 20.1168d),
        ROD(R.id.length_rod_input, R.id.length_rod_info, R.string.unit_symbol_length_rod, R.string.unit_info_length_rod, 5.0292d),
        YARD(R.id.length_yard_input, R.id.length_yard_info, R.string.unit_symbol_length_yard, R.string.unit_info_length_yard, 0.9144d),
        CUBIT(R.id.length_cubit_input, R.id.length_cubit_info, R.string.unit_symbol_length_cubit, R.string.unit_info_length_cubit, 0.4572d),
        FOOT(R.id.length_foot_input, R.id.length_foot_info, R.string.unit_symbol_length_foot, R.string.unit_info_length_foot, 0.3048d),
        SPAN(R.id.length_span_input, R.id.length_span_info, R.string.unit_symbol_length_span, R.string.unit_info_length_span, 0.2286d),
        HAND(R.id.length_hand_input, R.id.length_hand_info, R.string.unit_symbol_length_hand, R.string.unit_info_length_hand, 0.1016d),
        PALM(R.id.length_palm_input, R.id.length_palm_info, R.string.unit_symbol_length_palm, R.string.unit_info_length_palm, 0.0762d),
        INCH(R.id.length_inch_input, R.id.length_inch_info, R.string.unit_symbol_length_inch, R.string.unit_info_length_inch, 0.0254d),
        LINE(R.id.length_line_input, R.id.length_line_info, R.string.unit_symbol_length_line, R.string.unit_info_length_line, 0.0021166666666666666d),
        CALIBER(R.id.length_caliber_input, R.id.length_caliber_info, R.string.unit_symbol_length_caliber, R.string.unit_info_length_caliber, 0.000254d),
        MIL(R.id.length_mil_input, R.id.length_mil_info, R.string.unit_symbol_length_mil, R.string.unit_info_length_mil, 0.0000254d),
        NAUTICAL_LEAGUE(R.id.length_nautical_league_input, R.id.length_nautical_league_info, R.string.unit_symbol_length_nautical_league, R.string.unit_info_length_nautical_league, 5_556d),
        INTERNATIONAL_NAUTICAL_MILE(R.id.length_international_nautical_mile_input, R.id.length_international_nautical_mile_info, R.string.unit_symbol_length_nautical_mile_international, R.string.unit_info_length_nautical_mile_international, 1_852d),
        CABLE_LENGTH(R.id.length_cable_length_input, R.id.length_cable_length_info, R.string.unit_symbol_length_cable_length, R.string.unit_info_length_cable_length, 185.2d),
        FATHOM(R.id.length_fathom_input, R.id.length_fathom_info, R.string.unit_symbol_length_fathom, R.string.unit_info_length_fathom, 1.8288d),
        US_NAUTICAL_MILE(R.id.length_us_nautical_mile_input, R.id.length_us_nautical_mile_info, R.string.unit_symbol_length_nautical_mile_us, R.string.unit_info_length_nautical_mile_us, 1_853.248d),
        UK_NAUTICAL_MILE(R.id.length_uk_nautical_mile_input, R.id.length_uk_nautical_mile_info, R.string.unit_symbol_length_nautical_mile_uk, R.string.unit_info_length_nautical_mile_uk, 1_853.184d);

        private final int inputId;
        private final int infoId;
        private final int symbolResId;
        private final int infoResId;
        private final double toMeterFactor;

        LengthUnit(@IdRes int inputId,
                   @IdRes int infoId,
                   @StringRes int symbolResId,
                   @StringRes int infoResId,
                   double toMeterFactor) {
            this.inputId = inputId;
            this.infoId = infoId;
            this.symbolResId = symbolResId;
            this.infoResId = infoResId;
            this.toMeterFactor = toMeterFactor;
        }

        double toMeter(double value) {
            return value * toMeterFactor;
        }

        double fromMeter(double meterValue) {
            return meterValue / toMeterFactor;
        }
    }

    private final EnumMap<LengthUnit, TextInputEditText> inputs = new EnumMap<>(LengthUnit.class);

    @Nullable
    private BannerAdView bannerAdView;
    private View mainContentView;
    private boolean isUpdating = false;
    private final Locale numberLocale = Locale.getDefault();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_length_conversion);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.length_screen_title);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        mainContentView = findViewById(R.id.length_root);
        bannerAdView = findViewById(R.id.ad_container_view);

        for (LengthUnit unit : LengthUnit.values()) {
            TextInputEditText editText = findViewById(unit.inputId);
            inputs.put(unit, editText);
            editText.addTextChangedListener(new LengthTextWatcher(unit));
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

    private class LengthTextWatcher implements TextWatcher {

        private final LengthUnit unit;

        LengthTextWatcher(@NonNull LengthUnit unit) {
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

    private void clearOtherFields(@NonNull LengthUnit source) {
        isUpdating = true;
        try {
            for (LengthUnit unit : LengthUnit.values()) {
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

    private void updateAllUnits(@NonNull LengthUnit source, double value, int baseFractionDigits) {
        isUpdating = true;
        try {
            double meterValue = source.toMeter(value);
            for (LengthUnit unit : LengthUnit.values()) {
                if (unit == source) {
                    continue;
                }
                TextInputEditText editText = inputs.get(unit);
                if (editText == null) {
                    continue;
                }
                double converted = unit.fromMeter(meterValue);
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
