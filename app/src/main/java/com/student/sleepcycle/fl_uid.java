package com.student.sleepcycle;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

import java.util.Calendar;

public class fl_uid extends Fragment {
    TextView birt_val,sex_val;
    SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fl_uid, container, false);

        sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        sex_val = view.findViewById(R.id.sex_val);
        birt_val = view.findViewById(R.id.birt_val);
        birt_val.setText(sharedPreferences.getString("birt_data", "설정해주세요!"));

        birt_val.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
        return view;
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                // 날짜 선택 시 호출되는 콜백 메서드
                String selectedDate =  year + "년 " + (month + 1) + "월 " + dayOfMonth + "일"; // month는 0부터 시작하므로 +1
                birt_val.setText(selectedDate);
                saveValue("birt_data",selectedDate);
                saveValue("birt_year",Integer.toString(year));

            }
        }, year, month, dayOfMonth);

        datePickerDialog.show();
    }

    // 값을 SharedPreferences에 저장하는 메서드
    private void saveValue(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

}


