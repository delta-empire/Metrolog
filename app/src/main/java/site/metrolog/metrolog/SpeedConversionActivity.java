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

public class SpeedConversionActivity extends AppCompatActivity {

    private enum Unit {
        METER_PER_SECOND,
        KILOMETER_PER_SECOND,
        KILOMETER_PER_HOUR,
        METER_PER_MINUTE,
        MILE_PER_SECOND,
        MILE_PER_HOUR,
        FOOT_PER_SECOND,
        FOOT_PER_MINUTE,
        KNOT,
        NAUTICAL_MILE_PER_HOUR
    }

    private static final double METERS_PER_KILOMETER = 1000d;
    private static final double SECONDS_PER_HOUR = 3600d;
    private static final double SECONDS_PER_MINUTE = 60d;
    private static final double METERS_PER_MILE = 1609.344d;
    private static final double METERS_PER_FOOT = 0.3048d;
    private static final double METERS_PER_NAUTICAL_MILE = 1852d;

    private TextInputEditText metersPerSecondInput;
    private TextInputEditText kilometersPerSecondInput;
    private TextInputEditText kilometersPerHourInput;
    private TextInputEditText metersPerMinuteInput;
    private TextInputEditText milesPerSecondInput;
    private TextInputEditText milesPerHourInput;
    private TextInputEditText feetPerSecondInput;
    private TextInputEditText feetPerMinuteInput;
    private TextInputEditText knotsInput;
    private TextInputEditText nauticalMilesPerHourInput;

    @Nullable
    private BannerAdView bannerAdView;
    private View mainContentView;

    private boolean isUpdating = false;
    private final Locale numberLocale = Locale.getDefault();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_speed_conversion);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.speed_screen_title);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        mainContentView = findViewById(R.id.speed_root);
        bannerAdView = findViewById(R.id.ad_container_view);

        metersPerSecondInput = findViewById(R.id.speed_mps_input);
        kilometersPerSecondInput = findViewById(R.id.speed_kmps_input);
        kilometersPerHourInput = findViewById(R.id.speed_kmph_input);
        metersPerMinuteInput = findViewById(R.id.speed_mpm_input);
        milesPerSecondInput = findViewById(R.id.speed_mileps_input);
        milesPerHourInput = findViewById(R.id.speed_mph_input);
        feetPerSecondInput = findViewById(R.id.speed_fps_input);
        feetPerMinuteInput = findViewById(R.id.speed_fpm_input);
        knotsInput = findViewById(R.id.speed_kn_input);
        nauticalMilesPerHourInput = findViewById(R.id.speed_marine_mph_input);

        setupInfoButtons();
        setupWatchers();
        setupInsets();
        loadAdWhenReady();
    }

    private void setupInfoButtons() {
        configureInfoButton(R.id.speed_mps_info, R.string.unit_symbol_speed_mps, R.string.unit_info_speed_mps);
        configureInfoButton(R.id.speed_kmps_info, R.string.unit_symbol_speed_kmps, R.string.unit_info_speed_kmps);
        configureInfoButton(R.id.speed_kmph_info, R.string.unit_symbol_speed_kmph, R.string.unit_info_speed_kmph);
        configureInfoButton(R.id.speed_mpm_info, R.string.unit_symbol_speed_mpm, R.string.unit_info_speed_mpm);
        configureInfoButton(R.id.speed_mileps_info, R.string.unit_symbol_speed_mileps, R.string.unit_info_speed_mileps);
        configureInfoButton(R.id.speed_mph_info, R.string.unit_symbol_speed_mph, R.string.unit_info_speed_mph);
        configureInfoButton(R.id.speed_fps_info, R.string.unit_symbol_speed_fps, R.string.unit_info_speed_fps);
        configureInfoButton(R.id.speed_fpm_info, R.string.unit_symbol_speed_fpm, R.string.unit_info_speed_fpm);
        configureInfoButton(R.id.speed_kn_info, R.string.unit_symbol_speed_kn, R.string.unit_info_speed_kn);
        configureInfoButton(R.id.speed_marine_mph_info, R.string.unit_symbol_speed_mph, R.string.unit_info_speed_marine_mph);
    }

    private void configureInfoButton(int viewId, int symbolResId, int descriptionResId) {
        View view = findViewById(viewId);
        view.setOnClickListener(v -> showInfoDialog(symbolResId, descriptionResId));
    }

    private void setupWatchers() {
        metersPerSecondInput.addTextChangedListener(new ConversionWatcher(Unit.METER_PER_SECOND));
        kilometersPerSecondInput.addTextChangedListener(new ConversionWatcher(Unit.KILOMETER_PER_SECOND));
        kilometersPerHourInput.addTextChangedListener(new ConversionWatcher(Unit.KILOMETER_PER_HOUR));
        metersPerMinuteInput.addTextChangedListener(new ConversionWatcher(Unit.METER_PER_MINUTE));
        milesPerSecondInput.addTextChangedListener(new ConversionWatcher(Unit.MILE_PER_SECOND));
        milesPerHourInput.addTextChangedListener(new ConversionWatcher(Unit.MILE_PER_HOUR));
        feetPerSecondInput.addTextChangedListener(new ConversionWatcher(Unit.FOOT_PER_SECOND));
        feetPerMinuteInput.addTextChangedListener(new ConversionWatcher(Unit.FOOT_PER_MINUTE));
        knotsInput.addTextChangedListener(new ConversionWatcher(Unit.KNOT));
        nauticalMilesPerHourInput.addTextChangedListener(new ConversionWatcher(Unit.NAUTICAL_MILE_PER_HOUR));
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
            if (source != Unit.METER_PER_SECOND) {
                setTextIfChanged(metersPerSecondInput, "");
            }
            if (source != Unit.KILOMETER_PER_SECOND) {
                setTextIfChanged(kilometersPerSecondInput, "");
            }
            if (source != Unit.KILOMETER_PER_HOUR) {
                setTextIfChanged(kilometersPerHourInput, "");
            }
            if (source != Unit.METER_PER_MINUTE) {
                setTextIfChanged(metersPerMinuteInput, "");
            }
            if (source != Unit.MILE_PER_SECOND) {
                setTextIfChanged(milesPerSecondInput, "");
            }
            if (source != Unit.MILE_PER_HOUR) {
                setTextIfChanged(milesPerHourInput, "");
            }
            if (source != Unit.FOOT_PER_SECOND) {
                setTextIfChanged(feetPerSecondInput, "");
            }
            if (source != Unit.FOOT_PER_MINUTE) {
                setTextIfChanged(feetPerMinuteInput, "");
            }
            if (source != Unit.KNOT) {
                setTextIfChanged(knotsInput, "");
            }
            if (source != Unit.NAUTICAL_MILE_PER_HOUR) {
                setTextIfChanged(nauticalMilesPerHourInput, "");
            }
        } finally {
            isUpdating = false;
        }
    }

    private void updateAllUnits(@NonNull Unit source, double value, int preferredFractionDigits) {
        isUpdating = true;
        try {
            double metersPerSecond = toMetersPerSecond(source, value);
            double kilometersPerSecond = metersPerSecond / METERS_PER_KILOMETER;
            double kilometersPerHour = metersPerSecond * (SECONDS_PER_HOUR / METERS_PER_KILOMETER);
            double metersPerMinute = metersPerSecond * SECONDS_PER_MINUTE;
            double milesPerSecond = metersPerSecond / METERS_PER_MILE;
            double milesPerHour = metersPerSecond * (SECONDS_PER_HOUR / METERS_PER_MILE);
            double feetPerSecond = metersPerSecond / METERS_PER_FOOT;
            double feetPerMinute = metersPerSecond * (SECONDS_PER_MINUTE / METERS_PER_FOOT);
            double knots = metersPerSecond * (SECONDS_PER_HOUR / METERS_PER_NAUTICAL_MILE);
            double nauticalMilesPerHour = knots;

            if (source != Unit.METER_PER_SECOND) {
                setTextIfChanged(metersPerSecondInput, formatValue(metersPerSecond, preferredFractionDigits));
            }
            if (source != Unit.KILOMETER_PER_SECOND) {
                setTextIfChanged(kilometersPerSecondInput, formatValue(kilometersPerSecond, preferredFractionDigits));
            }
            if (source != Unit.KILOMETER_PER_HOUR) {
                setTextIfChanged(kilometersPerHourInput, formatValue(kilometersPerHour, preferredFractionDigits));
            }
            if (source != Unit.METER_PER_MINUTE) {
                setTextIfChanged(metersPerMinuteInput, formatValue(metersPerMinute, preferredFractionDigits));
            }
            if (source != Unit.MILE_PER_SECOND) {
                setTextIfChanged(milesPerSecondInput, formatValue(milesPerSecond, preferredFractionDigits));
            }
            if (source != Unit.MILE_PER_HOUR) {
                setTextIfChanged(milesPerHourInput, formatValue(milesPerHour, preferredFractionDigits));
            }
            if (source != Unit.FOOT_PER_SECOND) {
                setTextIfChanged(feetPerSecondInput, formatValue(feetPerSecond, preferredFractionDigits));
            }
            if (source != Unit.FOOT_PER_MINUTE) {
                setTextIfChanged(feetPerMinuteInput, formatValue(feetPerMinute, preferredFractionDigits));
            }
            if (source != Unit.KNOT) {
                setTextIfChanged(knotsInput, formatValue(knots, preferredFractionDigits));
            }
            if (source != Unit.NAUTICAL_MILE_PER_HOUR) {
                setTextIfChanged(nauticalMilesPerHourInput, formatValue(nauticalMilesPerHour, preferredFractionDigits));
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

    private String formatValue(double value, int preferredFractionDigits) {
        int fractionDigits = determineFractionDigits(value, preferredFractionDigits);
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(numberLocale);
        DecimalFormat format = new DecimalFormat();
        format.setDecimalFormatSymbols(symbols);
        format.setGroupingUsed(false);
        format.setMinimumFractionDigits(fractionDigits);
        format.setMaximumFractionDigits(fractionDigits);
        format.setRoundingMode(RoundingMode.HALF_UP);
        return format.format(BigDecimal.valueOf(value));
    }

    private int determineFractionDigits(double value, int preferredFractionDigits) {
        int digits = Math.max(3, preferredFractionDigits);
        if (preferredFractionDigits > 3) {
            return digits;
        }
        BigDecimal decimal = BigDecimal.valueOf(Math.abs(value));
        String plain = decimal.stripTrailingZeros().toPlainString();
        int dotIndex = plain.indexOf('.');
        if (dotIndex < 0) {
            return digits;
        }
        String fraction = plain.substring(dotIndex + 1);
        if (fraction.isEmpty()) {
            return digits;
        }
        int leadingZeros = 0;
        while (leadingZeros < fraction.length() && fraction.charAt(leadingZeros) == '0') {
            leadingZeros++;
        }
        if (leadingZeros >= 3) {
            int required = leadingZeros + 1;
            digits = Math.max(digits, required);
            digits = Math.min(digits, 10);
        }
        return digits;
    }

    private void setTextIfChanged(@NonNull TextInputEditText editText, @NonNull String value) {
        CharSequence current = editText.getText();
        if (!TextUtils.equals(current, value)) {
            editText.setText(value);
            int length = value.length();
            editText.setSelection(length);
        }
    }

    @NonNull
    private double toMetersPerSecond(@NonNull Unit unit, double value) {
        switch (unit) {
            case METER_PER_SECOND:
                return value;
            case KILOMETER_PER_SECOND:
                return value * METERS_PER_KILOMETER;
            case KILOMETER_PER_HOUR:
                return value * (METERS_PER_KILOMETER / SECONDS_PER_HOUR);
            case METER_PER_MINUTE:
                return value / SECONDS_PER_MINUTE;
            case MILE_PER_SECOND:
                return value * METERS_PER_MILE;
            case MILE_PER_HOUR:
                return value * (METERS_PER_MILE / SECONDS_PER_HOUR);
            case FOOT_PER_SECOND:
                return value * METERS_PER_FOOT;
            case FOOT_PER_MINUTE:
                return value * METERS_PER_FOOT / SECONDS_PER_MINUTE;
            case KNOT:
                return value * (METERS_PER_NAUTICAL_MILE / SECONDS_PER_HOUR);
            case NAUTICAL_MILE_PER_HOUR:
                return value * (METERS_PER_NAUTICAL_MILE / SECONDS_PER_HOUR);
            default:
                return value;
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
