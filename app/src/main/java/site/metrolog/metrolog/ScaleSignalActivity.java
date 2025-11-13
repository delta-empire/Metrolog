package site.metrolog.metrolog;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class ScaleSignalActivity extends AppCompatActivity {

    private enum Source {
        SCALE,
        SIGNAL
    }

    private enum ScaleType {
        LINEAR(0),
        LINEAR_DESC(1),
        QUADRATIC(2),
        QUADRATIC_DESC(3),
        ROOT(4),
        ROOT_DESC(5);

        private final int position;

        ScaleType(int position) {
            this.position = position;
        }

        @NonNull
        static ScaleType fromPosition(int position) {
            for (ScaleType type : values()) {
                if (type.position == position) {
                    return type;
                }
            }
            return LINEAR;
        }
    }

    private BannerAdView bannerAdView;
    private View mainContentView;

    private Spinner scaleTypeSpinner;
    private TextInputEditText scaleStartInput;
    private TextInputEditText scaleEndInput;
    private TextInputEditText scaleValueInput;
    private TextInputEditText signalStartInput;
    private TextInputEditText signalEndInput;
    private TextInputEditText signalValueInput;

    private boolean isUpdatingScaleValue = false;
    private boolean isUpdatingSignalValue = false;
    private Source lastChangedSource = Source.SCALE;

    private DecimalFormat decimalFormat;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_scale_signal);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        decimalFormat = createDecimalFormat();

        mainContentView = findViewById(R.id.scale_signal_root);
        bannerAdView = findViewById(R.id.ad_container_view);

        scaleTypeSpinner = findViewById(R.id.scale_type_spinner);
        scaleStartInput = findViewById(R.id.scale_start_input);
        scaleEndInput = findViewById(R.id.scale_end_input);
        scaleValueInput = findViewById(R.id.scale_value_input);
        signalStartInput = findViewById(R.id.signal_start_input);
        signalEndInput = findViewById(R.id.signal_end_input);
        signalValueInput = findViewById(R.id.signal_value_input);

        setupSpinner();
        setDefaultValues();
        setupWatchers();
        setupInsets();
        loadAdWhenReady();

        recalculate();
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.scale_types,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        scaleTypeSpinner.setAdapter(adapter);
        scaleTypeSpinner.setSelection(ScaleType.LINEAR.position);
        scaleTypeSpinner.setOnItemSelectedListener(new SimpleItemSelectedListener(() -> recalculate()));
    }

    private void setDefaultValues() {
        setEditTextValue(scaleStartInput, "0");
        setEditTextValue(scaleEndInput, "100");
        setEditTextValue(scaleValueInput, "50");
        setEditTextValue(signalValueInput, "12");
        setEditTextValue(signalStartInput, "4");
        setEditTextValue(signalEndInput, "20");
    }

    private void setupWatchers() {
        scaleValueInput.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                if (isUpdatingScaleValue) {
                    return;
                }
                lastChangedSource = Source.SCALE;
                recalculate();
            }
        });

        signalValueInput.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                if (isUpdatingSignalValue) {
                    return;
                }
                lastChangedSource = Source.SIGNAL;
                recalculate();
            }
        });

        TextWatcher parametersWatcher = new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                recalculate();
            }
        };

        scaleStartInput.addTextChangedListener(parametersWatcher);
        scaleEndInput.addTextChangedListener(parametersWatcher);
        signalStartInput.addTextChangedListener(parametersWatcher);
        signalEndInput.addTextChangedListener(parametersWatcher);
    }

    private void setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(mainContentView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loadAdWhenReady() {
        mainContentView.getViewTreeObserver().addOnGlobalLayoutListener(new android.view.ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mainContentView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                BannerAdSize adSize = getStickyAdSize();
                loadStickyBanner(adSize);
            }
        });
    }

    private void recalculate() {
        if (lastChangedSource == Source.SIGNAL) {
            calculateScaleValue();
        } else {
            calculateSignalValue();
        }
    }

    private void calculateSignalValue() {
        Double scs = parseDouble(scaleStartInput);
        Double sce = parseDouble(scaleEndInput);
        Double scv = parseDouble(scaleValueInput);
        Double sgs = parseDouble(signalStartInput);
        Double sge = parseDouble(signalEndInput);

        if (scs == null || sce == null || scv == null || sgs == null || sge == null) {
            updateSignalValue(null);
            return;
        }

        double denominator = sce - scs;
        if (denominator == 0d) {
            updateSignalValue(null);
            return;
        }

        double normalized = (scv - scs) / denominator;
        ScaleType scaleType = ScaleType.fromPosition(scaleTypeSpinner.getSelectedItemPosition());
        Double result = computeSignalValue(scaleType, normalized, sgs, sge);
        updateSignalValue(result);
    }

    private void calculateScaleValue() {
        Double scs = parseDouble(scaleStartInput);
        Double sce = parseDouble(scaleEndInput);
        Double sgs = parseDouble(signalStartInput);
        Double sge = parseDouble(signalEndInput);
        Double sgv = parseDouble(signalValueInput);

        if (scs == null || sce == null || sgs == null || sge == null || sgv == null) {
            updateScaleValue(null);
            return;
        }

        double denominator = sge - sgs;
        double reverseDenominator = sgs - sge;
        ScaleType scaleType = ScaleType.fromPosition(scaleTypeSpinner.getSelectedItemPosition());

        Double result;
        switch (scaleType) {
            case LINEAR:
                if (denominator == 0d) {
                    result = null;
                    break;
                }
                result = ((sgv - sgs) / denominator) * (sce - scs) + scs;
                break;
            case LINEAR_DESC:
                if (reverseDenominator == 0d) {
                    result = null;
                    break;
                }
                result = ((sgv - sge) / reverseDenominator) * (sce - scs) + scs;
                break;
            case QUADRATIC:
                if (denominator == 0d) {
                    result = null;
                    break;
                }
                double ratioQuadratic = (sgv - sgs) / denominator;
                if (ratioQuadratic < 0d) {
                    result = null;
                    break;
                }
                result = Math.sqrt(ratioQuadratic) * (sce - scs) + scs;
                break;
            case QUADRATIC_DESC:
                if (reverseDenominator == 0d) {
                    result = null;
                    break;
                }
                double ratioQuadraticDesc = (sgv - sge) / reverseDenominator;
                if (ratioQuadraticDesc < 0d) {
                    result = null;
                    break;
                }
                result = Math.sqrt(ratioQuadraticDesc) * (sce - scs) + scs;
                break;
            case ROOT:
                if (denominator == 0d) {
                    result = null;
                    break;
                }
                double ratioRoot = (sgv - sgs) / denominator;
                result = Math.pow(ratioRoot, 2) * (sce - scs) + scs;
                break;
            case ROOT_DESC:
                if (reverseDenominator == 0d) {
                    result = null;
                    break;
                }
                double ratioRootDesc = (sgv - sge) / reverseDenominator;
                result = Math.pow(ratioRootDesc, 2) * (sce - scs) + scs;
                break;
            default:
                result = null;
                break;
        }

        updateScaleValue(result);
    }

    @Nullable
    private Double computeSignalValue(@NonNull ScaleType scaleType, double normalized, double sgs, double sge) {
        switch (scaleType) {
            case LINEAR:
                return normalized * (sge - sgs) + sgs;
            case LINEAR_DESC:
                return normalized * (sgs - sge) + sge;
            case QUADRATIC:
                return Math.pow(normalized, 2) * (sge - sgs) + sgs;
            case QUADRATIC_DESC:
                return Math.pow(normalized, 2) * (sgs - sge) + sge;
            case ROOT:
                if (normalized < 0d) {
                    return null;
                }
                return Math.sqrt(normalized) * (sge - sgs) + sgs;
            case ROOT_DESC:
                if (normalized < 0d) {
                    return null;
                }
                return Math.sqrt(normalized) * (sgs - sge) + sge;
            default:
                return null;
        }
    }

    private void updateSignalValue(@Nullable Double value) {
        isUpdatingSignalValue = true;
        if (value == null || Double.isNaN(value) || Double.isInfinite(value)) {
            signalValueInput.setText("");
        } else {
            signalValueInput.setText(decimalFormat.format(value));
        }
        isUpdatingSignalValue = false;
    }

    private void updateScaleValue(@Nullable Double value) {
        isUpdatingScaleValue = true;
        if (value == null || Double.isNaN(value) || Double.isInfinite(value)) {
            scaleValueInput.setText("");
        } else {
            scaleValueInput.setText(decimalFormat.format(value));
        }
        isUpdatingScaleValue = false;
    }

    @Nullable
    private Double parseDouble(@NonNull TextInputEditText editText) {
        CharSequence text = editText.getText();
        if (text == null) {
            return null;
        }
        String raw = text.toString().trim();
        if (raw.isEmpty()) {
            return null;
        }
        try {
            Number number = decimalFormat.parse(raw);
            if (number != null) {
                return number.doubleValue();
            }
        } catch (ParseException ignored) {
        }
        try {
            return Double.parseDouble(raw.replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void setEditTextValue(@NonNull TextInputEditText editText, @NonNull String value) {
        editText.setText(value);
        editText.setSelection(editText.getText() != null ? editText.getText().length() : 0);
    }

    private BannerAdSize getStickyAdSize() {
        int adWidthPx = mainContentView.getWidth();
        if (adWidthPx == 0) {
            adWidthPx = getResources().getDisplayMetrics().widthPixels;
        }
        int adWidthDp = Math.round(adWidthPx / getResources().getDisplayMetrics().density);
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
            public void onAdFailedToLoad(@NonNull AdRequestError error) {
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
            public void onImpression(@Nullable ImpressionData data) {
            }
        });

        AdRequest request = new AdRequest.Builder().build();
        bannerAdView.loadAd(request);
    }

    private void destroyBanner() {
        if (bannerAdView != null) {
            bannerAdView.destroy();
            bannerAdView = null;
        }
    }

    @Override
    protected void onDestroy() {
        destroyBanner();
        super.onDestroy();
    }

    private DecimalFormat createDecimalFormat() {
        DecimalFormat format = (DecimalFormat) NumberFormat.getNumberInstance(Locale.getDefault());
        format.setMaximumFractionDigits(3);
        format.setMinimumFractionDigits(0);
        format.setGroupingUsed(false);
        format.setRoundingMode(java.math.RoundingMode.HALF_UP);
        return format;
    }

    private static class SimpleItemSelectedListener implements AdapterView.OnItemSelectedListener {

        private final Runnable onItemSelected;
        private boolean isInitialized = false;

        SimpleItemSelectedListener(@NonNull Runnable onItemSelected) {
            this.onItemSelected = onItemSelected;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (isInitialized) {
                onItemSelected.run();
            } else {
                isInitialized = true;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }
}
