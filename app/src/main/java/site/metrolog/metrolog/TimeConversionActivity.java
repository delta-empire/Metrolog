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

public class TimeConversionActivity extends AppCompatActivity {

    private enum Unit {
        SECOND,
        NANOSECOND,
        MICROSECOND,
        MILLISECOND,
        MINUTE,
        HOUR,
        DAY,
        WEEK,
        YEAR,
        CENTURY,
        MILLENNIUM
    }

    private static final double SECONDS_IN_MINUTE = 60d;
    private static final double SECONDS_IN_HOUR = 3600d;
    private static final double SECONDS_IN_DAY = 86400d;
    private static final double SECONDS_IN_WEEK = 604800d;
    private static final double SECONDS_IN_YEAR = 31536000d;
    private static final double SECONDS_IN_CENTURY = SECONDS_IN_YEAR * 100d;
    private static final double SECONDS_IN_MILLENNIUM = SECONDS_IN_YEAR * 1000d;

    private TextInputEditText secondsInput;
    private TextInputEditText nanosecondsInput;
    private TextInputEditText microsecondsInput;
    private TextInputEditText millisecondsInput;
    private TextInputEditText minutesInput;
    private TextInputEditText hoursInput;
    private TextInputEditText daysInput;
    private TextInputEditText weeksInput;
    private TextInputEditText yearsInput;
    private TextInputEditText centuriesInput;
    private TextInputEditText millenniaInput;

    @Nullable
    private BannerAdView bannerAdView;
    private View mainContentView;

    private boolean isUpdating = false;
    private final Locale numberLocale = Locale.getDefault();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_time_conversion);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.time_screen_title);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        mainContentView = findViewById(R.id.time_root);
        bannerAdView = findViewById(R.id.ad_container_view);

        secondsInput = findViewById(R.id.time_seconds_input);
        nanosecondsInput = findViewById(R.id.time_nanoseconds_input);
        microsecondsInput = findViewById(R.id.time_microseconds_input);
        millisecondsInput = findViewById(R.id.time_milliseconds_input);
        minutesInput = findViewById(R.id.time_minutes_input);
        hoursInput = findViewById(R.id.time_hours_input);
        daysInput = findViewById(R.id.time_days_input);
        weeksInput = findViewById(R.id.time_weeks_input);
        yearsInput = findViewById(R.id.time_years_input);
        centuriesInput = findViewById(R.id.time_centuries_input);
        millenniaInput = findViewById(R.id.time_millennia_input);

        setupInfoButtons();
        setupWatchers();
        setupInsets();
        loadAdWhenReady();
    }

    private void setupInfoButtons() {
        configureInfoButton(R.id.time_seconds_info, R.string.time_symbol_seconds, R.string.time_info_seconds);
        configureInfoButton(R.id.time_nanoseconds_info, R.string.time_symbol_nanoseconds, R.string.time_info_nanoseconds);
        configureInfoButton(R.id.time_microseconds_info, R.string.time_symbol_microseconds, R.string.time_info_microseconds);
        configureInfoButton(R.id.time_milliseconds_info, R.string.time_symbol_milliseconds, R.string.time_info_milliseconds);
        configureInfoButton(R.id.time_minutes_info, R.string.time_symbol_minutes, R.string.time_info_minutes);
        configureInfoButton(R.id.time_hours_info, R.string.time_symbol_hours, R.string.time_info_hours);
        configureInfoButton(R.id.time_days_info, R.string.time_symbol_days, R.string.time_info_days);
        configureInfoButton(R.id.time_weeks_info, R.string.time_symbol_weeks, R.string.time_info_weeks);
        configureInfoButton(R.id.time_years_info, R.string.time_symbol_years, R.string.time_info_years);
        configureInfoButton(R.id.time_centuries_info, R.string.time_symbol_centuries, R.string.time_info_centuries);
        configureInfoButton(R.id.time_millennia_info, R.string.time_symbol_millennia, R.string.time_info_millennia);
    }

    private void configureInfoButton(int viewId, int symbolResId, int descriptionResId) {
        View view = findViewById(viewId);
        view.setOnClickListener(v -> showInfoDialog(symbolResId, descriptionResId));
    }

    private void setupWatchers() {
        secondsInput.addTextChangedListener(new ConversionWatcher(Unit.SECOND));
        nanosecondsInput.addTextChangedListener(new ConversionWatcher(Unit.NANOSECOND));
        microsecondsInput.addTextChangedListener(new ConversionWatcher(Unit.MICROSECOND));
        millisecondsInput.addTextChangedListener(new ConversionWatcher(Unit.MILLISECOND));
        minutesInput.addTextChangedListener(new ConversionWatcher(Unit.MINUTE));
        hoursInput.addTextChangedListener(new ConversionWatcher(Unit.HOUR));
        daysInput.addTextChangedListener(new ConversionWatcher(Unit.DAY));
        weeksInput.addTextChangedListener(new ConversionWatcher(Unit.WEEK));
        yearsInput.addTextChangedListener(new ConversionWatcher(Unit.YEAR));
        centuriesInput.addTextChangedListener(new ConversionWatcher(Unit.CENTURY));
        millenniaInput.addTextChangedListener(new ConversionWatcher(Unit.MILLENNIUM));
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
            if (source != Unit.SECOND) {
                setTextIfChanged(secondsInput, "");
            }
            if (source != Unit.NANOSECOND) {
                setTextIfChanged(nanosecondsInput, "");
            }
            if (source != Unit.MICROSECOND) {
                setTextIfChanged(microsecondsInput, "");
            }
            if (source != Unit.MILLISECOND) {
                setTextIfChanged(millisecondsInput, "");
            }
            if (source != Unit.MINUTE) {
                setTextIfChanged(minutesInput, "");
            }
            if (source != Unit.HOUR) {
                setTextIfChanged(hoursInput, "");
            }
            if (source != Unit.DAY) {
                setTextIfChanged(daysInput, "");
            }
            if (source != Unit.WEEK) {
                setTextIfChanged(weeksInput, "");
            }
            if (source != Unit.YEAR) {
                setTextIfChanged(yearsInput, "");
            }
            if (source != Unit.CENTURY) {
                setTextIfChanged(centuriesInput, "");
            }
            if (source != Unit.MILLENNIUM) {
                setTextIfChanged(millenniaInput, "");
            }
        } finally {
            isUpdating = false;
        }
    }

    private void updateAllUnits(@NonNull Unit source, double value, int baseFractionDigits) {
        isUpdating = true;
        try {
            double seconds = toSeconds(source, value);

            if (source != Unit.SECOND) {
                setTextIfChanged(secondsInput, formatValue(fromSeconds(Unit.SECOND, seconds), baseFractionDigits));
            }
            if (source != Unit.NANOSECOND) {
                setTextIfChanged(nanosecondsInput, formatValue(fromSeconds(Unit.NANOSECOND, seconds), baseFractionDigits));
            }
            if (source != Unit.MICROSECOND) {
                setTextIfChanged(microsecondsInput, formatValue(fromSeconds(Unit.MICROSECOND, seconds), baseFractionDigits));
            }
            if (source != Unit.MILLISECOND) {
                setTextIfChanged(millisecondsInput, formatValue(fromSeconds(Unit.MILLISECOND, seconds), baseFractionDigits));
            }
            if (source != Unit.MINUTE) {
                setTextIfChanged(minutesInput, formatValue(fromSeconds(Unit.MINUTE, seconds), baseFractionDigits));
            }
            if (source != Unit.HOUR) {
                setTextIfChanged(hoursInput, formatValue(fromSeconds(Unit.HOUR, seconds), baseFractionDigits));
            }
            if (source != Unit.DAY) {
                setTextIfChanged(daysInput, formatValue(fromSeconds(Unit.DAY, seconds), baseFractionDigits));
            }
            if (source != Unit.WEEK) {
                setTextIfChanged(weeksInput, formatValue(fromSeconds(Unit.WEEK, seconds), baseFractionDigits));
            }
            if (source != Unit.YEAR) {
                setTextIfChanged(yearsInput, formatValue(fromSeconds(Unit.YEAR, seconds), baseFractionDigits));
            }
            if (source != Unit.CENTURY) {
                setTextIfChanged(centuriesInput, formatValue(fromSeconds(Unit.CENTURY, seconds), baseFractionDigits));
            }
            if (source != Unit.MILLENNIUM) {
                setTextIfChanged(millenniaInput, formatValue(fromSeconds(Unit.MILLENNIUM, seconds), baseFractionDigits));
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
        int fractionDigits = determineFractionDigits(value, baseFractionDigits);
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(numberLocale);
        DecimalFormat format = new DecimalFormat();
        format.setDecimalFormatSymbols(symbols);
        format.setGroupingUsed(false);
        format.setMinimumFractionDigits(fractionDigits);
        format.setMaximumFractionDigits(fractionDigits);
        format.setRoundingMode(RoundingMode.HALF_UP);
        return format.format(BigDecimal.valueOf(value));
    }

    private int determineFractionDigits(double value, int baseFractionDigits) {
        int digits = Math.max(3, baseFractionDigits);
        BigDecimal fractional = BigDecimal.valueOf(value).remainder(BigDecimal.ONE).abs();
        if (fractional.compareTo(BigDecimal.ZERO) == 0) {
            return digits;
        }

        BigDecimal threshold = new BigDecimal("0.001");
        if (fractional.compareTo(threshold) < 0) {
            int firstNonZero = -1;
            for (int i = 1; i <= 10; i++) {
                BigDecimal scaled = fractional.movePointRight(i);
                if (scaled.compareTo(BigDecimal.ONE) >= 0) {
                    firstNonZero = i;
                    break;
                }
            }
            if (firstNonZero > 0) {
                digits = Math.max(digits, firstNonZero);
                if (digits > 10) {
                    digits = 10;
                }
            } else {
                digits = 10;
            }
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

    private double toSeconds(@NonNull Unit unit, double value) {
        switch (unit) {
            case SECOND:
                return value;
            case NANOSECOND:
                return value * 1e-9d;
            case MICROSECOND:
                return value * 1e-6d;
            case MILLISECOND:
                return value * 1e-3d;
            case MINUTE:
                return value * SECONDS_IN_MINUTE;
            case HOUR:
                return value * SECONDS_IN_HOUR;
            case DAY:
                return value * SECONDS_IN_DAY;
            case WEEK:
                return value * SECONDS_IN_WEEK;
            case YEAR:
                return value * SECONDS_IN_YEAR;
            case CENTURY:
                return value * SECONDS_IN_CENTURY;
            case MILLENNIUM:
                return value * SECONDS_IN_MILLENNIUM;
            default:
                return value;
        }
    }

    private double fromSeconds(@NonNull Unit unit, double seconds) {
        switch (unit) {
            case SECOND:
                return seconds;
            case NANOSECOND:
                return seconds * 1e9d;
            case MICROSECOND:
                return seconds * 1e6d;
            case MILLISECOND:
                return seconds * 1e3d;
            case MINUTE:
                return seconds / SECONDS_IN_MINUTE;
            case HOUR:
                return seconds / SECONDS_IN_HOUR;
            case DAY:
                return seconds / SECONDS_IN_DAY;
            case WEEK:
                return seconds / SECONDS_IN_WEEK;
            case YEAR:
                return seconds / SECONDS_IN_YEAR;
            case CENTURY:
                return seconds / SECONDS_IN_CENTURY;
            case MILLENNIUM:
                return seconds / SECONDS_IN_MILLENNIUM;
            default:
                return seconds;
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

