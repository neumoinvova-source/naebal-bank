package com.naebal.bank;

import android.app.Activity;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.*;

public class MainActivity extends Activity {

    NfcAdapter nfcAdapter;
    LinearLayout root;
    EditText amount;
    TextView status;
    TextView icon;
    TextView total;
    boolean waiting = false;

    int bg = Color.rgb(17, 23, 34);
    int green = Color.rgb(51, 220, 150);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        showInput();
    }

    TextView text(String value, int size) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(Color.WHITE);
        t.setGravity(Gravity.CENTER);
        return t;
    }

    void setup() {
        root = new LinearLayout(this);
        root.setOrientation(1);
        root.setGravity(Gravity.CENTER);
        root.setPadding(28, 25, 28, 25);
        root.setBackgroundColor(bg);
        setContentView(root);
    }

    void showInput() {
        waiting = false;
        setup();

        TextView title = text("НАЕБАЛ БАНК", 28);
        title.setTypeface(null, 1);
        root.addView(title);

        root.addView(text("NFC TERMINAL • SIMULATOR", 12));

        root.addView(text("СУММА ОПЕРАЦИИ", 15));

        amount = new EditText(this);
        amount.setTextColor(Color.WHITE);
        amount.setHintTextColor(Color.GRAY);
        amount.setHint("Например: 1500");
        amount.setTextSize(30);
        amount.setGravity(Gravity.CENTER);
        amount.setInputType(8194);
        root.addView(amount);

        Button confirm = new Button(this);
        confirm.setText("✓  ПОДТВЕРДИТЬ");
        root.addView(confirm);

        status = text("Терминал готов", 14);
        root.addView(status);

        confirm.setOnClickListener(v -> {
            String value = amount.getText().toString().trim();

            if (value.isEmpty() || Double.parseDouble(value) <= 0) {
                status.setText("Введите корректную сумму");
                return;
            }

            total = text(value + " ₽", 35);
            showWaiting(value);
        });

        root.addView(text("ШУТОЧНЫЙ СИМУЛЯТОР\nНЕ ПРИНИМАЕТ ПЛАТЕЖИ", 11));
    }

    void showWaiting(String value) {
        waiting = true;
        setup();

        root.addView(text("НАЕБАЛ БАНК", 26));
        root.addView(text("СУММА ОПЕРАЦИИ", 13));
        root.addView(text(value + " ₽", 40));

        icon = text("◉", 90);
        icon.setTextColor(green);
        root.addView(icon);

        status = text("ПРИЛОЖИТЕ NFC-МЕТКУ", 20);
        root.addView(status);

        root.addView(text("Ожидание обнаружения...", 13));

        Button cancel = new Button(this);
        cancel.setText("ОТМЕНА");
        root.addView(cancel);

        cancel.setOnClickListener(v -> showInput());

        AlphaAnimation anim = new AlphaAnimation(0.4f, 1f);
        anim.setDuration(1000);
        anim.setRepeatMode(AlphaAnimation.REVERSE);
        anim.setRepeatCount(AlphaAnimation.INFINITE);
        icon.startAnimation(anim);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (nfcAdapter != null && nfcAdapter.isEnabled()) {
            nfcAdapter.enableReaderMode(
                this,
                tag -> runOnUiThread(() -> onDetected(tag)),
                NfcAdapter.FLAG_READER_NFC_A |
                NfcAdapter.FLAG_READER_NFC_B |
                NfcAdapter.FLAG_READER_NFC_F |
                NfcAdapter.FLAG_READER_NFC_V,
                null
            );
        }
    }

    void onDetected(Tag tag) {
        if (!waiting) return;

        waiting = false;Vibrator vibrator =
            (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        if (vibrator != null) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    250, VibrationEffect.DEFAULT_AMPLITUDE
                )
            );
        }

        showSuccess();
    }

    void showSuccess() {
        setup();

        TextView check = text("✓", 110);
        check.setTextColor(green);
        root.addView(check);

        TextView success = text("ОБНАРУЖЕНО!", 27);
        success.setTextColor(green);
        root.addView(success);

        root.addView(text("NFC-объект обнаружен", 16));

        root.postDelayed(() -> showInput(), 2500);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (nfcAdapter != null) {
            nfcAdapter.disableReaderMode(this);
        }
    }
}
