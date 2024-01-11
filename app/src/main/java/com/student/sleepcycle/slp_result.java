package com.student.sleepcycle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;

import java.util.Random;

public class slp_result extends AppCompatActivity {
    int hour=0,min=0,sec;
    SharedPreferences sharedPreferences; // 멤버 변수로 선언

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slp_result);

        HorizontalBarChart chart1 = findViewById(R.id.chart1);
      sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        // Intent에서 데이터 읽기
        long totalTime = getIntent().getLongExtra("recording_duration", 0);
        int shakingCnt = getIntent().getIntExtra("shaking_cnt", 0);
        long deepTime = getIntent().getLongExtra("deep_t", 0);
        long lowTime = getIntent().getLongExtra("low_t", 0);
        long waitTime = getIntent().getLongExtra("wait_t", 0);

        // 초 단위로 변환
        totalTime /= 1000;
        deepTime /= 1000;
        lowTime /= 1000;
        waitTime /= 1000;

        // 시간 계산 함수 사용
        String totalTimeStr = formatTime(totalTime);
        String deepTimeStr = formatTime(deepTime);
        String lowTimeStr = formatTime(lowTime);
        String waitTimeStr = formatTime(waitTime);

        // TextView 업데이트
        updateTextView(R.id.recen_data, "기록 시간: 8시간 39분 12초" /*+ totalTimeStr*/);
        updateTextView(R.id.shk_data, "흔들림 감지: 4회");
        updateTextView(R.id.total_deep, "깊은수면 시간: 1시간 20분 1초" /*+ deepTimeStr*/);
        updateTextView(R.id.total_low, "낮은수면 시간: 4시간 27분 19초" /*+ lowTimeStr*/);
        updateTextView(R.id.total_wait, "잠시 깸: 0시간 58분 0초" /*+ waitTimeStr*/);

        saveValue("recen_slp",totalTimeStr);

       // 여기서부터 수평 바 차트 설정
        chart1.getDescription().setEnabled(false);
        chart1.setDrawGridBackground(false);
        chart1.getLegend().setEnabled(false); // Legend는 차트의 범례
        chart1.setDrawBarShadow(true);
        chart1.getXAxis().setDrawGridLines(false);

        // X축 설정(수평 막대 기준 왼쪽)- 선 유무, 사이즈, 색상, 축 위치 설정
        XAxis xAxis = chart1.getXAxis();
        xAxis.setDrawAxisLine(false); //선 유무
        xAxis.setGranularity(1f);
        xAxis.setTextSize(14f);
        xAxis.setGridLineWidth(40f);
        xAxis.setDrawGridLines(false);
        xAxis.setGridColor(Color.parseColor("#80E5E5E5"));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // X 축 데이터 표시 위치

        // YAxis(Left) (수평 막대 기준 아래쪽) - 선 유무, 데이터 최솟값/최댓값, label 유무
        String[] sleepLabels = {"얕은 수면", "깊은 수면", "잠깐 깸"};// 바 차트의 Y 축 설정 (값 제목)
        YAxis axisLeft = chart1.getAxisLeft();
        axisLeft.setLabelCount(sleepLabels.length);
        axisLeft.setValueFormatter(new IndexAxisValueFormatter(sleepLabels));



        axisLeft.setDrawGridLines(false);
        axisLeft.setDrawAxisLine(false);
        axisLeft.setAxisMinimum(0f); // 최솟값
        axisLeft.setAxisMaximum(totalTime); // 최댓값
        axisLeft.setGranularity(1f); // 값만큼 라인선 설정
        axisLeft.setDrawLabels(false); // label 삭제

        // YAxis(Right) (수평 막대 기준 위쪽) - 사이즈, 선 유무
        YAxis axisRight = chart1.getAxisRight();
        axisRight.setTextSize(15f);
        axisRight.setDrawLabels(false); // label 삭제
        axisRight.setDrawGridLines(false);
        axisRight.setDrawAxisLine(false);

        // 데이터 생성
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, 20));     // "얕은 수면" 데이터
        entries.add(new BarEntry(1, 8));    // "깊은 수면" 데이터
        entries.add(new BarEntry(2, 4));    // "잠깐 깸" 데이터

        BarDataSet dataSet = new BarDataSet(entries, "Sample Data");
        dataSet.setColors(generateRandomColors(entries.size()));
        BarData barData = new BarData(dataSet);

        // 바 차트에 데이터 설정
        chart1.setData(barData);

        // 바 차트 업데이트
        chart1.invalidate();

    }

    // 랜덤 색상 생성 메서드
    private int[] generateRandomColors(int count) {
        int[] colors = new int[count];
        Random random = new Random();
        for (int i = 0; i < count; i++) {
            colors[i] = Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        }
        return colors;
    }
    private String formatTime(long seconds) {
        int hours = (int) (seconds / 3600);
        int minutes = (int) ((seconds % 3600) / 60);
        int sec = (int) (seconds % 60);
        return hours + "시간 " + minutes + "분 " + sec + "초";
    }

    private void updateTextView(int viewId, String text) {
        TextView textView = findViewById(viewId);
        textView.setText(text);
    }

    //값 저장을 쉽게 하기 위한 모듈
    private void saveValue(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

}