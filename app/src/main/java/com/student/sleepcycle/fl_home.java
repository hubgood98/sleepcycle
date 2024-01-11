package com.student.sleepcycle;


import androidx.fragment.app.Fragment;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.provider.AlarmClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class fl_home extends Fragment implements View.OnClickListener {


    int[] ids = {R.id.c_txt1, R.id.c_txt2, R.id.c_txt3, R.id.c_txt4, R.id.c_txt5};//TextView id값 배열로 저장
    int[][] times = {{1, 45}, {3, 15}, {4, 45}, {6, 15}, {7, 45}};
    LocalDateTime crn_time = LocalDateTime.now(); //현재 시간을 가져오는 변수
    int crn_hour = crn_time.getHour();//현재 시간에서 시간을 구함
    int crn_min = crn_time.getMinute();//현재 시간에서 분을 구함
    SharedPreferences sharedPreferences; // 멤버 변수로 선언

    Handler time_handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            crn_time = LocalDateTime.now();//현재 시간을 가져옴
            crn_hour = crn_time.getHour();//시간에
            crn_min = crn_time.getMinute();//분에
            time_handler.postDelayed(this, 60000); //1분 후에 다시 호출시킴
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fl_home, container, false);



        for (int i = 0; i < ids.length; i++) {
            TextView textView = view.findViewById(ids[i]);
            textView.setText(getTime(times[i][0], times[i][1]));
            textView.setOnClickListener(this);
        }

        // AlarmManager 객체 생성
        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        // 1분마다 crn_time 갱신
        time_handler.postDelayed(runnable, 60000);

        return view;
    }

    public void onClick(View v) {
        int id = v.getId();
        try {
            if (id == R.id.c_txt1) {
                add_alarm(1, 45);
            } else if (id == R.id.c_txt2) {
                add_alarm(3, 15);
            } else if (id == R.id.c_txt3) {
                add_alarm(4, 45);
            } else if (id == R.id.c_txt4) {
                add_alarm(6, 15);
            } else if (id == R.id.c_txt5) {
                add_alarm(7, 45);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "오류가 발생했습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.i("오류내용",e.getMessage());
        }
    }

    //현재 시간값에 일정값을 더하여 텍스트 값을 재 설정
    private String getTime(int hour,int min) {
        LocalDateTime set_time = crn_time.plusHours(hour).plusMinutes(min);//현재 시간에 시간과 분을 더함
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm"); //형식지정
        return set_time.format(formatter);
    }

    //일정 값을 더하여 알람 설정해주는 모듈
    private void add_alarm(int hour,int min) {
        LocalDateTime alarm_time = crn_time.plusHours(hour).plusMinutes(min); // 현재 시간에서 일정 시간만큼 더해진 알람 시간

        // alarm_time을 문자열로 변환
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm a");
        String alarmTime = alarm_time.format(formatter);

        // a TextView에 alarmTimeString 설정
        saveValue("getup",alarmTime);

        Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);//인텐트를 사용하여 Android 기본 시계 앱으로 전환
        intent.putExtra(AlarmClock.EXTRA_HOUR, alarm_time.getHour()); // 알람 시간 설정
        intent.putExtra(AlarmClock.EXTRA_MINUTES, alarm_time.getMinute());

        if (intent.resolveActivity(getActivity().getPackageManager()) != null)
        {
            startActivity(intent);
            Toast.makeText(getActivity(), "알람이 설정되었습니다.", Toast.LENGTH_SHORT).show();
        } else {
            Log.e("MainActivity", "시계 앱이 설치되어 있지 않습니다.");
            Toast.makeText(getActivity(), "시계 앱이 설치되어 있지 않습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    //값 저장을 쉽게 하기 위한 모듈
    private void saveValue(String key, String value) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }
}