package com.student.sleepcycle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    private FrameLayout fl; //메인 프레임 레이아웃
    private BottomNavigationView btm_navi; //하단부에 위치한 navigation


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fl=findViewById(R.id.fl_contain);
        btm_navi = findViewById(R.id.btm_navi);

        btm_navi.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home:
                        btm_navi.setItemIconTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.nv_bt1));
                        btm_navi.setItemTextColor(ContextCompat.getColorStateList(MainActivity.this, R.color.nv_bt1));
                        changeFragment(new fl_home());
                        return true;

                    case R.id.sleep:
                        Log.i("메인", "fl_sleep버튼누름");
                        btm_navi.setItemIconTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.nv_bt2));
                        btm_navi.setItemTextColor(ContextCompat.getColorStateList(MainActivity.this, R.color.nv_bt2));
                        changeFragment(new fl_sleep());
                        return true;

                    case R.id.noise_select:
                        btm_navi.setItemIconTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.nv_bt3));
                        btm_navi.setItemTextColor(ContextCompat.getColorStateList(MainActivity.this, R.color.nv_bt3));
                        changeFragment(new fl_noise());
                        return true;

                    case R.id.uid:
                        btm_navi.setItemIconTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.nv_bt4));
                        btm_navi.setItemTextColor(ContextCompat.getColorStateList(MainActivity.this, R.color.nv_bt4));
                        changeFragment(new fl_uid());
                        return true;
                }
                return false;
            }
            });
        btm_navi.setSelectedItemId(R.id.home); //초기화를 home으로 지정
    }

    private void changeFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fl_contain, fragment)
                .commit();
    }

}

