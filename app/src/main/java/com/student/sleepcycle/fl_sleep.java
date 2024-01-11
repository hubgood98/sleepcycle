package com.student.sleepcycle;

import static android.hardware.Sensor.TYPE_AMBIENT_TEMPERATURE;
import static android.hardware.Sensor.TYPE_RELATIVE_HUMIDITY;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.Manifest;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.dinuscxj.progressbar.CircleProgressBar;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;


public class fl_sleep extends Fragment implements View.OnClickListener, CircleProgressBar.ProgressFormatter {
    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private static final String DEFAULT_PATTERN = "%d%%"; //프로그래스 기본값

    private TextView recom_sleeptime,total_count,avg_dB,slp_des;
    private String src;
    private int now_year;//현재 년도를 구함
    private int birt_year;//자신의 생일값
    private Button sleep_tracker,test_bt;
    private Handler handler = new Handler();
    //원형 진행도
    CircleProgressBar circle_score;

    // 센서 관련
    private SensorManager sensorManager;
    private Sensor lightSensor;
    private float currentLightValue = 0;
    private Sensor temperature_sen; //온도센서
    private Sensor humidity_sen; //습도센서

    //선 그래프
    private BarChart weekday_chart;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fl_sleep, container, false);

        init(view);//초기화할 값들 호출
        sleep_tracker.setOnClickListener(this);



        // SharedPreferences 객체를 가져오기
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        total_count.setText(sharedPreferences.getString("recen_slp","< 1분"));
        String dbavg = sharedPreferences.getString("dB_val","< 10dB");

        avg_dB.setText(dbavg); //dB값
        birt_year =  Integer.parseInt(sharedPreferences.getString("birt_year","1998")); //생일(년)값을 호출
        // 현재 날짜데이터 수신
        LocalDate crn_data = LocalDate.now();
        now_year = crn_data.getYear(); //현재 년도를 저장
        request_sleeptime(birt_year); //권장 수면시간 텍스트 반환모듈
        recom_sleeptime.setText("권장 수면량 : "+src);


        // 변수 값을 검색 (두 번째 매개변수는 기본값)
        String mon = sharedPreferences.getString("mon", "5");
        String tue = sharedPreferences.getString("tue", "0");
        String wed = sharedPreferences.getString("wed", "0");
        String thu = sharedPreferences.getString("thu", "6");
        String fri = sharedPreferences.getString("fri", "4");
        String sat = sharedPreferences.getString("sat", "3");
        String recen = sharedPreferences.getString("recen", "0"); //최근 정보에 사용될 값

        String recen_score = sharedPreferences.getString("recen_score", "0"); //최근 정보에 사용될 값
        int recen_v = Integer.parseInt(recen);

        // 현재 요일을 가져오기
        Calendar calendar = Calendar.getInstance();
        int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.RECORD_AUDIO)) {

            } else {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
            }
        } else {

        }


        circle_score.setProgress(Integer.parseInt(recen_score));  // 수면점수 해당 퍼센트를 적용

        ArrayList<BarEntry> entry_chart = new ArrayList<>(); // 데이터를 담을 Arraylist
        weekday_chart = (BarChart) view.findViewById(R.id.weekday_chart);
        BarData weekday_data = new BarData(); // 차트에 담길 데이터

        entry_chart.add(new BarEntry(0, Float.parseFloat(mon))); //weekday_chart에 좌표 데이터를 담는다.
        entry_chart.add(new BarEntry(1, Float.parseFloat(tue)));
        entry_chart.add(new BarEntry(2, Float.parseFloat(wed)));
        entry_chart.add(new BarEntry(3, Float.parseFloat(thu)));
        entry_chart.add(new BarEntry(4, Float.parseFloat(fri)));
        entry_chart.add(new BarEntry(5, Float.parseFloat(sat)));

        BarDataSet barDataSet = new BarDataSet(entry_chart, "수면 데이터"); // 데이터가 담긴 Arraylist 를 수면데이터로 변환한다.

        // 요일에 따라 바 차트 색상 설정
        ArrayList<Integer> colors = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            if (i == currentDayOfWeek - 2) { // 오늘 요일에 해당하는 막대의 인덱스는 (currentDayOfWeek - 2)입니다.
                colors.add(Color.parseColor("#FFA500")); // 오렌지색
            } else {
                colors.add(Color.WHITE); // 다른 요일은 파란색 또는 다른 색
            }
        }
        barDataSet.setColors(colors);
        weekday_data.addDataSet(barDataSet); // 해당 BarDataSet 을 적용될 차트에 들어갈 DataSet 에 넣는다.

        // 범례(레전드) 숨기기
        Legend legend = weekday_chart.getLegend();
        legend.setEnabled(false);

        // 막대 그래프의 Description 제거
        weekday_chart.getDescription().setEnabled(false);

        //x축 설정
        XAxis xAxis = weekday_chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // X축 라벨 위치 설정
        xAxis.setGranularity(1f); // 그리드 선 수평 거리 설정
        xAxis.setTextColor(Color.WHITE); // X축 텍스트 컬러 설정
        xAxis.setDrawGridLines(false); // 격자선 설정

        // X축 라벨 설정
        String[] labels = new String[] { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));

        //Y축 설정
        YAxis yAxis = weekday_chart.getAxisLeft(); // 왼쪽 Y축 가져오기

        // Y축에 눈금 추가 설정
        yAxis.setGranularity(2f); // 눈금 간격 설정
        yAxis.setTextColor(Color.WHITE); // 눈금 텍스트 컬러 설정
        yAxis.setDrawGridLines(false); // 그리드 라인 비활성화
        yAxis.setAxisMinimum(0f); // 최소값 설정
        yAxis.setAxisMaximum(10f); // 최대값 설정

        //차트설정
        weekday_chart.getXAxis().setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
        weekday_chart.setData(weekday_data); // 차트에 위의 DataSet 을 넣는다.
        weekday_chart.animateY(1000);
        weekday_chart.getXAxis().setDrawGridLines(false);
        weekday_chart.invalidate(); // 차트 업데이트
        weekday_chart.setTouchEnabled(false); // 차트 터치 불가능하게


        return view;
    }

    public String request_sleeptime(int a)
    {
        Log.i("request_sleeptime시작", "수신값 : "+ a);
        if(65<=now_year-a+1)
            src = "7~8시간";
        else if(18<=now_year-a+1)
            src ="7~9시간";
        else if(14<=now_year-a+1)
            src ="8~10시간";
        else if(6<=now_year-a+1)
            src ="9~11시간";
        else
            src ="10~13시간";
        return src;
    }

    @Override
    public void onClick(View v) {
        Log.i("fl_sleep", "onClick 시작");
        if (v.getId() == R.id.sleep_tracker) {
            Intent intent = new Intent(getActivity(), sleeping.class);
            startActivity(intent);
        }
    }

    @Override
    public CharSequence format(int progress, int max) {
        return String.format(DEFAULT_PATTERN, (int) ((float) progress / (float) max * 100));
    }



    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    public void init(View view)
    {
        sleep_tracker = view.findViewById(R.id.sleep_tracker);
        recom_sleeptime = view.findViewById(R.id.recom_sleeptime);
        total_count = view.findViewById(R.id.total_count); //최근수면시간 표시값
        avg_dB = view.findViewById(R.id.avg_dB);
        slp_des = view.findViewById(R.id.slp_des);
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        temperature_sen = sensorManager.getDefaultSensor(TYPE_AMBIENT_TEMPERATURE); //센서값 초기화
        humidity_sen = sensorManager.getDefaultSensor(TYPE_RELATIVE_HUMIDITY);

        circle_score = view.findViewById(R.id.circle_score);

    }
}
