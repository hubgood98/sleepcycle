package com.student.sleepcycle;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class fl_noise extends Fragment {

    private MediaPlayer bird_sound, wave_sound, bonfire_sound,lot_rain_sound, s_river_sound;
    private SeekBar bird_seek, wavepool_seek, bonfire_seek,rain_seek,s_river_seek;
    private FloatingActionButton fabButton;

    //notification을 위한 전역변수
    private NotificationManagerCompat notificationManager;
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "fl_noise_sound";

    //값 저장을 위한 변수
    SharedPreferences sharedPreferences; // 멤버 변수로 선언


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fl_noise, container, false);
        Log.i("fl_noise", "onCreateView 시작");


        // SharedPreferences 객체를 가져오기
        sharedPreferences = getActivity().getSharedPreferences("fl_noise_sound", Context.MODE_PRIVATE);

        bird_seek = view.findViewById(R.id.bird_seek);
        wavepool_seek = view.findViewById(R.id.wavepool_seek);
        bonfire_seek = view.findViewById(R.id.bonfire_seek);
        rain_seek = view.findViewById(R.id.rain_seek);
        s_river_seek = view.findViewById(R.id.s_river_seek);
        bonfire_seek = view.findViewById(R.id.bonfire_seek);
        fabButton = view.findViewById(R.id.fab_button);

        //사운드 초기화
        bird_sound = MediaPlayer.create(getActivity(), R.raw.bird_sound);
        wave_sound = MediaPlayer.create(getActivity(), R.raw.wavepool_sound);
        bonfire_sound = MediaPlayer.create(getActivity(), R.raw.bonfire_sound);
        lot_rain_sound = MediaPlayer.create(getActivity(), R.raw.lot_rain);
        s_river_sound = MediaPlayer.create(getActivity(), R.raw.smallriver);

        bird_sound.setLooping(true);
        wave_sound.setLooping(true);
        bonfire_sound.setLooping(true);
        lot_rain_sound.setLooping(true);
        s_river_sound.setLooping(true);

        bird_sound.setVolume(0, 0);
        wave_sound.setVolume(0, 0);
        bonfire_sound.setVolume(0, 0);
        lot_rain_sound.setVolume(0, 0);
        s_river_sound.setVolume(0, 0);

        // 저장된 값으로 SeekBar 설정
        bird_seek.setProgress(Integer.parseInt(sharedPreferences.getString("bird_seek", "0")));
        wavepool_seek.setProgress(Integer.parseInt(sharedPreferences.getString("wavepool_seek", "0")));
        bonfire_seek.setProgress(Integer.parseInt(sharedPreferences.getString("bonfire_seek", "0")));
        rain_seek.setProgress(Integer.parseInt(sharedPreferences.getString("rain_seek", "0")));
        s_river_seek.setProgress(Integer.parseInt(sharedPreferences.getString("s_river_seek", "0")));

        setSoundVolume(bird_seek, bird_sound);
        setSoundVolume(wavepool_seek, wave_sound);
        setSoundVolume(bonfire_seek, bonfire_sound);
        setSoundVolume(rain_seek, lot_rain_sound);
        setSoundVolume(s_river_seek, s_river_sound);

        // NotificationManagerCompat 초기화
        notificationManager = NotificationManagerCompat.from(requireActivity());

        // Notification 채널 생성
        createNotificationChannel();

        startSound(bird_sound);
        startSound(wave_sound);
        startSound(bonfire_sound);
        startSound(lot_rain_sound);
        startSound(s_river_sound);

        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent serviceIntent = new Intent(getActivity(), BackgroundSoundService.class);
                int[] soundResources = {R.raw.bird_sound, R.raw.wavepool_sound, R.raw.bonfire_sound, R.raw.lot_rain, R.raw.smallriver}; // 여러 소리 리소스를 배열로 정의
                serviceIntent.putExtra("soundResources", soundResources);
                serviceIntent.putExtra("birdVolume", bird_seek.getProgress());
                serviceIntent.putExtra("wavepoolVolume", wavepool_seek.getProgress());
                serviceIntent.putExtra("bonfireVolume", bonfire_seek.getProgress());
                serviceIntent.putExtra("lot_rainVolume", rain_seek.getProgress());
                serviceIntent.putExtra("s_riverVolume", s_river_seek.getProgress());
                requireActivity().startService(serviceIntent);
                showNotification();
            }
        });

        fabButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // Service를 종료하는 Intent 생성
                Intent serviceIntent = new Intent(getActivity(), BackgroundSoundService.class);
                requireActivity().stopService(serviceIntent);

                notificationManager.cancel(NOTIFICATION_ID);


                return true;
            }
        });

        return view;
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) requireActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }




    private void setSoundVolume(final SeekBar seekBar, final MediaPlayer sound) {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (sound != null) {
                    if (sound.isPlaying()) {
                        // Convert SeekBar's progress to a range between 0.0 to 1.0
                        float volume = progress / 100.0f;

                        String key = String.valueOf(seekBar.getId());
                        saveValue(key, Float.toString(volume));

                        sound.setVolume(volume, volume);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    //액티비티가 재개될 때
    @Override
    public void onResume() {
        super.onResume();
        Log.i("생명주기","onResume()");

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i("생명주기","onPause()");

        // SeekBar 값 저장
        saveValue("bird_seek", String.valueOf(bird_seek.getProgress()));
        saveValue("wavepool_seek", String.valueOf(wavepool_seek.getProgress()));
        saveValue("bonfire_seek", String.valueOf(bonfire_seek.getProgress()));
        saveValue("rain_seek", String.valueOf(rain_seek.getProgress()));
        saveValue("s_river_seek", String.valueOf(s_river_seek.getProgress()));

        releaseMedia(bird_sound);
        releaseMedia(wave_sound);
        releaseMedia(bonfire_sound);
        releaseMedia(s_river_sound);
        releaseMedia(lot_rain_sound);
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    //앱이 종료될 때
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("생명주기","onDestroy()");

    }

    //음악 시작을 위한 모듈
    private void startSound(MediaPlayer sound) {
        if (sound != null) {
            sound.start();
        }
        else sound.release();
    }

    //음악 정지을 위한 모듈
    private void pauseSound(MediaPlayer sound) {
        if (sound != null && sound.isPlaying()) {
            sound.pause();
        }
    }

    //음악 객체를 해제(null로 만듬)
    private void releaseMedia(MediaPlayer sound) {
        if (sound != null && sound.isPlaying()) {
            sound.stop(); // 현재 재생중인 MediaPlayer를 중지합니다.
        }
        sound.release();
    }

    // 앱이 실행될 때 Notification 채널 생성 - Android 8.0 (API 레벨 26) 이상부터는 Notification에 채널을 사용하여 알림을 그룹화하고 관리하게 변경함
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Sleep Cycle의 잔잔한 소음",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }
    }

    // Notification을 표시하는 메서드
    private void showNotification() {
        createNotificationChannel();

        // Notification 생성
        Notification notification = new NotificationCompat.Builder(getActivity(), CHANNEL_ID)
                .setContentTitle("white noise setting")
                .setContentText("설정된 음악이 실행중입니다.")
                .setSmallIcon(R.mipmap.app_icon)
                .setVibrate(new long[]{0,200,200,200})  // 진동 패턴 설정 - 배열의 짝수 인덱스 : 대기시간, 홀수 인덱스 : 진동 시간
                .setAutoCancel(true)  // 통지 클릭 시 통지 삭제
                .setOngoing(true)  // 사용자가 삭제하지 못하도록 설정
                .build();

        // Notification을 노출
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        notificationManager.notify(NOTIFICATION_ID, notification);
    }



    // 값을 SharedPreferences에 저장하는 메서드
    private void saveValue(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

}
