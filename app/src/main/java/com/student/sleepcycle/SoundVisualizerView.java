package com.student.sleepcycle;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import kotlin.jvm.functions.Function0;

public class SoundVisualizerView extends View {

    private Paint amplitudePaint;
    private int drawingWidth;
    private int drawingHeight;
    private List<Integer> drawingAmplitudes;
    private boolean isReplaying;
    private int replayingPosition;
    public Function0<Integer> onRequestCurrentAmplitude;
    private TextView db_display; // dB 값을 표시할 TextView

    private float sum_dB = 0; // dB 값의 합계를 저장하는 변수
    private int sampleCount = 0; // dB 값 샘플 수를 저장하는 변수

    // 평균 dB 값을 반환하는 메서드
    public float getAveragedB() {
        if (sampleCount == 0) {
            return 0; // 샘플이 없는 경우 0을 반환
        } else {
            return Math.round((sum_dB / sampleCount) * 10.0f) / 10.0f;
        }
    }

    public float getdB(){
        int currentAmplitude = onRequestCurrentAmplitude != null ? onRequestCurrentAmplitude.invoke() : 0;
        float dbValue = amplitudeToDb(currentAmplitude);

        return dbValue;
    }

    private Runnable visualizeRepeatAction = new Runnable() {
        @Override
        public void run() {
            if (!isReplaying) {
                int currentAmplitude = onRequestCurrentAmplitude != null ? onRequestCurrentAmplitude.invoke() : 0;
                drawingAmplitudes.add(0, currentAmplitude);

                // dB 값을 계산하여 db_display TextView에 표시
                float dbValue = amplitudeToDb(currentAmplitude);
                sum_dB += dbValue; // dB 값을 합계에 누적
                sampleCount++; // 샘플 수 증가
                db_display.setText(String.format(Locale.getDefault(), "%.2f dB", dbValue));
            } else {
                replayingPosition++;
            }
            invalidate();
            getHandler().postDelayed(this, 250);//핸들러 값의 지연시간 2.5
        }
    };

    //생성자
    public SoundVisualizerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        amplitudePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        amplitudePaint.setColor(getContext().getColor(R.color.orance));
        amplitudePaint.setStrokeWidth(LINE_WIDTH);
        amplitudePaint.setStrokeCap(Paint.Cap.ROUND);

        drawingAmplitudes = new ArrayList<>();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        drawingWidth = w;
        drawingHeight = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float centerY = drawingHeight / 2f;
        float offsetX = drawingWidth;

        for (int amplitude : drawingAmplitudes)
        {
            float lineLength = amplitude / MAX_AMPLITUDE * drawingHeight * 0.8f;
            offsetX -= LINE_SPACE;

            if (offsetX < 0) break;

            canvas.drawLine(
                    offsetX,
                    centerY - lineLength / 2f,
                    offsetX,
                    centerY + lineLength / 2f,
                    amplitudePaint
            );
        }
        // dB 값을 TextView에 표시
        if (db_display != null && onRequestCurrentAmplitude != null) {
            int currentAmplitude = onRequestCurrentAmplitude.invoke();
            String dbText = String.format(Locale.getDefault(), "%.2f dB", amplitudeToDb(currentAmplitude));
            db_display.setText(dbText);
        }
    }//onDraw 종료

    // 추가: dB 값을 표시할 TextView 설정
    public void setDbDisplayTextView(TextView textView) {
        db_display = textView;
    }


    public void startVisualizing(boolean isReplaying) {
        this.isReplaying = isReplaying;
        if (getHandler() != null) {
            getHandler().post(visualizeRepeatAction);
            Log.i(TAG, "startVisualizing: 정상적인 실행");
        }
        else
            Log.i(TAG, "startVisualizing: 실패");
    }

    public void stopVisualizing() {
        replayingPosition = 0;
        getHandler().removeCallbacks(visualizeRepeatAction);
    }

    public void clearVisualization() {
        drawingAmplitudes.clear();
        invalidate();
    }

    //주어진 진폭 값을 dB로 변환하여 반환
    private float amplitudeToDb(int amplitude) {
        if (amplitude > 0) {
            return 20.0f * (float) Math.log10(amplitude);
        } else {
            return 0.0f;
        }
    }

    public void setOnRequestCurrentAmplitude(Function0<Integer> onRequestCurrentAmplitude) {
        this.onRequestCurrentAmplitude = onRequestCurrentAmplitude;
    }

    // dB 값을 누적하고 반환하는 메서드
    public float accumulateAndSaveDbValue(int amplitude) {
        float dbValue = amplitudeToDb(amplitude);
        sum_dB += dbValue;
        return dbValue;
    }

    // 누적된 dB 값을 반환하는 메서드
    public float getAccumulatedDbValue() {
        return sum_dB;
    }

    private static final float LINE_WIDTH = 10f;
    private static final float LINE_SPACE = 15f;
    private static final float MAX_AMPLITUDE = (float) Short.MAX_VALUE;
    private static final long ACTION_INTERVAL = 20L;
}
