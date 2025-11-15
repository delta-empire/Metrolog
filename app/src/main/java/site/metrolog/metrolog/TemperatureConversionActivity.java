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

public class TemperatureConversionActivity extends AppCompatActivity {

    private enum Unit {
        KELVIN,
        CELSIUS,
        FAHRENHEIT,
        RANKINE,
        REAUMUR
    }

    private TextInputEditText kelvinInput;
    private TextInputEditText celsiusInput;
    private TextInputEditText fahrenheitInput;
    private TextInputEditText rankineInput;
    private TextInputEditText reaumurInput;

    @Nullable
    private BannerAdView bannerAdView;
    private View mainContentView;

    private boolean isUpdating = false;
    private final Locale numberLocale = Locale.getDefault();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_temperature_conversion);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.temperature_screen_title);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        mainContentView = findViewById(R.id.temperature_root);
        bannerAdView = findViewById(R.id.ad_container_view);

        kelvinInput = findViewById(R.id.temperature_kelvin_input);
        celsiusInput = findViewById(R.id.temperature_celsius_input);
        fahrenheitInput = findViewById(R.id.temperature_fahrenheit_input);
        rankineInput = findViewById(R.id.temperature_rankine_input);
        reaumurInput = findViewById(R.id.temperature_reaumur_input);

        setupInfoButtons();
        setupWatchers();
        setupInsets();
        loadAdWhenReady();
    }

    private void setupInfoButtons() {
        configureInfoButton(R.id.temperature_kelvin_info, R.string.unit_symbol_kelvin, R.string.unit_info_kelvin);
        configureInfoButton(R.id.temperature_celsius_info, R.string.unit_symbol_celsius, R.string.unit_info_celsius);
        configureInfoButton(R.id.temperature_fahrenheit_info, R.string.unit_symbol_fahrenheit, R.string.unit_info_fahrenheit);
        configureInfoButton(R.id.temperature_rankine_info, R.string.unit_symbol_rankine, R.string.unit_info_rankine);
        configureInfoButton(R.id.temperature_reaumur_info, R.string.unit_symbol_reaumur, R.string.unit_info_reaumur);
    }

    private void configureInfoButton(int viewId, int symbolResId, int descriptionResId) {
        View view = findViewById(viewId);
        view.setOnClickListener(v -> showInfoDialog(symbolResId, descriptionResId));
    }

    private void setupWatchers() {
        kelvinInput.addTextChangedListener(new ConversionWatcher(Unit.KELVIN));
        celsiusInput.addTextChangedListener(new ConversionWatcher(Unit.CELSIUS));
        fahrenheitInput.addTextChangedListener(new ConversionWatcher(Unit.FAHRENHEIT));
        rankineInput.addTextChangedListener(new ConversionWatcher(Unit.RANKINE));
        reaumurInput.addTextChangedListener(new ConversionWatcher(Unit.REAUMUR));
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
            if (source != Unit.KELVIN) {
                setTextIfChanged(kelvinInput, "");
            }
            if (source != Unit.CELSIUS) {
                setTextIfChanged(celsiusInput, "");
            }
            if (source != Unit.FAHRENHEIT) {
                setTextIfChanged(fahrenheitInput, "");
            }
            if (source != Unit.RANKINE) {
                setTextIfChanged(rankineInput, "");
            }
            if (source != Unit.REAUMUR) {
                setTextIfChanged(reaumurInput, "");
            }
        } finally {
            isUpdating = false;
        }
    }

    private void updateAllUnits(@NonNull Unit source, double value, int fractionDigits) {
        isUpdating = true;
        try {
            double celsius = toCelsius(source, value);
            double kelvin = celsius + 273.15d;
            double fahrenheit = celsius * 9d / 5d + 32d;
            double rankine = kelvin * 9d / 5d;
            double reaumur = celsius * 4d / 5d;

            if (source != Unit.KELVIN) {
                setTextIfChanged(kelvinInput, formatValue(kelvin, fractionDigits));
            }
            if (source != Unit.CELSIUS) {
                setTextIfChanged(celsiusInput, formatValue(celsius, fractionDigits));
            }
            if (source != Unit.FAHRENHEIT) {
                setTextIfChanged(fahrenheitInput, formatValue(fahrenheit, fractionDigits));
            }
            if (source != Unit.RANKINE) {
                setTextIfChanged(rankineInput, formatValue(rankine, fractionDigits));
            }
            if (source != Unit.REAUMUR) {
                setTextIfChanged(reaumurInput, formatValue(reaumur, fractionDigits));
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

    @NonNull
    private double toCelsius(@NonNull Unit unit, double value) {
        switch (unit) {
            case KELVIN:
                return value - 273.15d;
            case CELSIUS:
                return value;
            case FAHRENHEIT:
                return (value - 32d) * 5d / 9d;
            case RANKINE:
                return (value * 5d / 9d) - 273.15d;
            case REAUMUR:
                return value * 5d / 4d;
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
