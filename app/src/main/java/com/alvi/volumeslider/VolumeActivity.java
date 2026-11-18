package com.alvi.volumeslider;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class VolumeActivity extends Activity {

    private static final long AUTO_CLOSE_MS = 4000;

    private AudioManager am;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable closer = new Runnable() {
        @Override
        public void run() {
            finish();
        }
    };
    private final List<SeekBar> bars = new ArrayList<>();
    private final List<Integer> streams = new ArrayList<>();

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        am = (AudioManager) getSystemService(AUDIO_SERVICE);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(0x66000000);
        root.setOnClickListener(v -> finish());
        root.setOnApplyWindowInsetsListener((v, insets) -> {
            v.setPadding(0, 0, 0, insets.getSystemWindowInsetBottom());
            return insets;
        });

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(20), dp(16), dp(20), dp(16));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0xF2262A31);
        bg.setCornerRadius(dp(24));
        card.setBackground(bg);
        card.setOnClickListener(v -> { });

        addRow(card, "Media", AudioManager.STREAM_MUSIC);
        addRow(card, "Ringtone", AudioManager.STREAM_RING);
        addRow(card, "Alarm", AudioManager.STREAM_ALARM);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM);
        lp.setMargins(dp(12), 0, dp(12), dp(16));
        root.addView(card, lp);
        setContentView(root);
    }

    private void addRow(LinearLayout parent, String label, final int stream) {
        TextView tv = new TextView(this);
        tv.setText(label);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(14);
        tv.setPadding(0, dp(8), 0, 0);
        parent.addView(tv);

        SeekBar bar = new SeekBar(this);
        bar.setMax(am.getStreamMaxVolume(stream));
        bar.setProgress(am.getStreamVolume(stream));
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar s, int progress, boolean fromUser) {
                if (!fromUser) return;
                try {
                    am.setStreamVolume(stream, progress, 0);
                } catch (SecurityException e) {
                    // Do Not Disturb policy can block some streams
                    s.setProgress(am.getStreamVolume(stream));
                }
                kick();
            }

            @Override
            public void onStartTrackingTouch(SeekBar s) {
                kick();
            }

            @Override
            public void onStopTrackingTouch(SeekBar s) {
                kick();
            }
        });
        parent.addView(bar, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        bars.add(bar);
        streams.add(stream);
    }

    private void refresh() {
        for (int i = 0; i < bars.size(); i++) {
            bars.get(i).setProgress(am.getStreamVolume(streams.get(i)));
        }
    }

    private void kick() {
        handler.removeCallbacks(closer);
        handler.postDelayed(closer, AUTO_CLOSE_MS);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
        kick();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(closer);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            am.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    keyCode == KeyEvent.KEYCODE_VOLUME_UP
                            ? AudioManager.ADJUST_RAISE
                            : AudioManager.ADJUST_LOWER,
                    0);
            refresh();
            kick();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }
}
