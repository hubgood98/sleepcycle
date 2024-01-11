package com.student.sleepcycle;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;
import android.icu.text.SimpleDateFormat;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.util.Date;
import java.util.Locale;
import java.util.Calendar;


public class sleeping extends AppCompatActivity implements SensorEventListener {

    private SoundVisualizerView soundVisualizerView;
    private Button resetButton, fin_sleep;
    private RecordButton recordButton;

    private TextView db_display, crn_day,crn_time, s_val, am_pm,getup_text;

    private final String[] requiredPermissions = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    /*녹음 상태 관련 변수들*/
    private String recordingFilePath;
    private State state = State.BEFORE_RECORDING;
    private MediaRecorder recorder;
    private MediaPlayer player;

    private boolean isRecording = false;//녹음중인거 확인하는 변수
    private long recordingStartTime = 0; //녹음시간
    private float sum_dB = 0; // 누적 dB 값을 저장하는 변수

    /*흔들림 감지 관련 변수들*/
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private boolean isShaking = false;
    private int shaking_cnt = 0; //흔들림 카운트

    private int chek = 0; //종료시 체크여부 확인

    /*수면 상태를 확인하기 위한 변수*/
    private static final int SLEEP_STATE_wait = 0; //수면대기
    private static final int SLEEP_STATE_low = 1; //얕은수면
    private static final int SLEEP_STATE_deep = 2; //깊은수면
    private int crn_SleepState = SLEEP_STATE_wait; // 초기 상태는 '수면대기'로 초기화
    private long waitStartTime = 0;//'대기' 상태의 시작시간 기록
    private long deepStartTime = 0;//'깊은' 상태의 시작시간
    private long lowStartTime = 0; // '낮은' 상태의 시작시간
    private long totalwaitTime = 0; // 수면 중인 시간 누적
    private long totallowTime = 0; // 낮은 수면인 시간 누적
    private long totaldeepTime = 0; // 수면 중인 시간 누적
    SharedPreferences sharedPreferences; // 멤버 변수로 선언

    private static final float ACCELERATION_THRESHOLD = 0.5f; // 가속도 임계값 (조절 필요)
    private long sleepCheckStartTime = 0; // 가속도 체크 시작 시간

    private static final int SLEEP_STATE_CHECK_INTERVAL = 3000; // 3초 간격으로 수면 상태 확인

    private Handler sleepStateHandler = new Handler();
    private final Handler time_handler = new Handler(Looper.getMainLooper());
    private Runnable sleepStateRunnable = new Runnable() {
        @Override
        public void run() {
            long currentTime = System.currentTimeMillis();

            // 각 상태에 따라 시간을 누적
            if (isRecording) {
                switch (crn_SleepState) {
                    case SLEEP_STATE_wait:
                        totalwaitTime += (currentTime - waitStartTime);
                        waitStartTime = currentTime;
                        break;
                    case SLEEP_STATE_deep:
                        long deepDuration = currentTime - deepStartTime;
                        totaldeepTime += deepDuration;

                        if (deepDuration >= 5800) {
                            crn_SleepState = SLEEP_STATE_low;
                            lowStartTime = currentTime;
                        } else if (deepDuration >= 10000 && crn_SleepState == SLEEP_STATE_low) {
                            crn_SleepState = SLEEP_STATE_deep;
                            deepStartTime = currentTime;
                        }
                        break;
                    case SLEEP_STATE_low:
                        totallowTime += (currentTime - lowStartTime);
                        break;
                }
            }

            // 다음 확인을 예약
            sleepStateHandler.postDelayed(this, SLEEP_STATE_CHECK_INTERVAL);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sleeping);

        // SharedPreferences 객체를 가져오기
        sharedPreferences = this.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        sleepStateHandler.postDelayed(sleepStateRunnable, SLEEP_STATE_CHECK_INTERVAL);

        // 센서 관리자 초기화
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // 가속도계 센서 초기화
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        requestAudioPermission();
        initViews();
        bindViews();
        initVariables();

        String getup_time = sharedPreferences.getString("getup", "값이 없습니다");
        getup_text.setText("\uD83D\uDD14 기상시간 "+getup_time);
        soundVisualizerView.setOnRequestCurrentAmplitude(this::getCurrentAmplitude);
        soundVisualizerView.setDbDisplayTextView(db_display); // dB 값을 표시할 TextView 설정
        recordButton.updateIconWithState(state);//현재 녹음버튼 아이콘 갱신
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM, dd일", Locale.US);
        String currentDate = dateFormat.format(new Date());
        crn_day.setText(currentDate);

        time_handler.post(new Runnable() {
            @Override
            public void run() {
                updateClock();
                time_handler.postDelayed(this, 1000); // 1초마다 갱신
            }
        });//핸들러 종료

    }

    private int getCurrentAmplitude() {
        if (recorder != null) {
            return recorder.getMaxAmplitude();
        } else {
            return 0;
        }
    }

    private void requestAudioPermission() {
        ActivityCompat.requestPermissions(this, requiredPermissions, REQUEST_RECORD_AUDIO_PERMISSION);
    }

    //변수 초기화모음
    private void initViews() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        soundVisualizerView = findViewById(R.id.soundVisualizerView);
        resetButton = findViewById(R.id.resetButton);
        recordButton = findViewById(R.id.recordButton);
        db_display = findViewById(R.id.db_display);
        s_val = findViewById(R.id.s_val);
        fin_sleep = findViewById(R.id.fin_sleep);
        crn_day = findViewById(R.id.crn_day);
        crn_time = findViewById(R.id.crn_time);
        am_pm = findViewById(R.id.am_pm);
        getup_text = findViewById(R.id.getup_text);

        recordingFilePath = getExternalCacheDir().getAbsolutePath() + "/recording.3gp";
        soundVisualizerView.setOnRequestCurrentAmplitude(() -> recorder != null ? recorder.getMaxAmplitude() : 0);

    }

    private void bindViews() {
        recordButton.setOnClickListener(v -> {
            switch (state) {
                case BEFORE_RECORDING:
                    startRecording();
                    break;
                case ON_RECORDING:
                    stopRecording();
                    break;
            }
            //상태에 따라 버튼 이미지 변경
            recordButton.updateIconWithState(state);
        });

        resetButton.setOnClickListener(v -> {
            String totalSleepTimeString = String.valueOf(totaldeepTime);
            s_val.setText(totalSleepTimeString);
        });

        fin_sleep.setOnClickListener(v ->
            showToast("안전한 종료를 위해 2초이상 길게 눌러주세요")
        );

        //수면 종료버튼을 눌렀을때
        fin_sleep.setOnLongClickListener(v -> {
            // 현재 시간을 녹음 시간으로 설정
            long recordingEndTime = System.currentTimeMillis();
            long recordingDuration = recordingEndTime - recordingStartTime; //총 기록시간
            float averagedB = soundVisualizerView.getAveragedB();

            saveValue("dB_val", String.valueOf(averagedB));

            int currentDayOfWeek = getCurrentDayOfWeek();
            String valueToSave; //요일값 저장

            if (currentDayOfWeek == Calendar.MONDAY) {
                valueToSave = "mon";
            } else if (currentDayOfWeek == Calendar.TUESDAY) {
                valueToSave = "tue";
            } else if (currentDayOfWeek == Calendar.WEDNESDAY) {
                valueToSave = "wed";
            } else if (currentDayOfWeek == Calendar.THURSDAY) {
                valueToSave = "thu";
            } else if (currentDayOfWeek == Calendar.FRIDAY) {
                valueToSave = "fri";
            } else if (currentDayOfWeek == Calendar.SATURDAY) {
                valueToSave = "sat";
            } else {
                valueToSave = "other"; // 다른 요일의 경우
            }
            long total_time = recordingDuration / 1000; //전체시간을 초로 변경
            saveValue(valueToSave, String.valueOf(total_time));
            saveValue("recen", String.valueOf(total_time));
            saveValue("recen_score", String.valueOf((totaldeepTime+totallowTime)/total_time/10));
            saveValue("recen_sleepv", String.valueOf(totaldeepTime+totallowTime));

                stopPlaying();
                soundVisualizerView.clearVisualization();
                state = State.BEFORE_RECORDING;

                // 다른 액티비티로 전달할 데이터를 Intent에 추가
                Intent intent = new Intent(sleeping.this, slp_result.class); // 다른 액티비티로 이동을 위한 인텐트 객체 생성
                intent.putExtra("recording_duration", recordingDuration); //putExtra(데이터 식별하는 고유한 문자열 키, 전달하려는 데이터 실제값)
                intent.putExtra("shaking_cnt", shaking_cnt);
                intent.putExtra("wait_t", totalwaitTime);
                intent.putExtra("deep_t", totaldeepTime);
                intent.putExtra("low_t", totallowTime);
                startActivity(intent);
                finish();
                return true;
        });

    }

    private void initVariables() {
        state = State.BEFORE_RECORDING;
    }

    private void startRecording() {
        recordingStartTime = System.currentTimeMillis(); //녹음 시작시간을 기록함
        isRecording = true; // 녹음 중임을 나타내는 변수, true로 설정
        crn_SleepState = SLEEP_STATE_wait; // 녹음이 시작되면 대기중 상태로 설정
        waitStartTime = recordingStartTime; // 대기중 상태 시작 시간을 녹음 startTime 기록
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(recordingFilePath);
        try {
            recorder.prepare();
            recorder.start();
            soundVisualizerView.startVisualizing(false);
            state = State.ON_RECORDING;
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 30초 후 얕은 수면으로 변경하는 핸들러 설정
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isRecording && crn_SleepState == SLEEP_STATE_wait) {
                    // 30초가 경과하고 아직 대기 중인 경우
                    crn_SleepState = SLEEP_STATE_low;
                    lowStartTime = System.currentTimeMillis(); // 얕은 수면 상태 시작 시간 갱신
                }
            }
        }, 10000); // 30,000 밀리초 (30초) 후에 실행
    }

    private void stopRecording() {
        if (recorder != null) {
            recorder.stop();
            recorder.release();
            recorder = null;
            soundVisualizerView.stopVisualizing();
            isRecording = false;//녹음상태확인
            state = State.AFTER_RECORDING;
        }
    }

    private void stopPlaying() {
        if (player != null) {
            player.release();
            player = null;
            soundVisualizerView.stopVisualizing();
            state = State.AFTER_RECORDING;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            boolean audioRecordPermissionGranted = grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED;

            if (!audioRecordPermissionGranted) {
                finish();
            }
        }
    }

    public enum State {
        BEFORE_RECORDING,
        ON_RECORDING,
        AFTER_RECORDING
    }

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 201;

    @Override
    protected void onResume() {
        super.onResume();
        // 센서 리스너 등록
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 센서 리스너 해제
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        long currentTime = System.currentTimeMillis();

        // 가속도 센서 이벤트 처리
        if (event.sensor == accelerometer) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // 가속도 값의 크기 계산
            double acceleration = Math.sqrt(x * x + y * y + z * z);

            if (event.sensor == accelerometer) {
                if (isRecording) {
                    if (crn_SleepState == SLEEP_STATE_wait) {
                        // 대기 중 상태에서 30초가 경과하면 얕은 수면 상태로 변경
                        if (currentTime - waitStartTime >= 15000) {
                            crn_SleepState = SLEEP_STATE_low;
                        }
                        // 대기 중 상태 시간 갱신
                        totalwaitTime += (currentTime - waitStartTime);
                        waitStartTime = currentTime;
                    } else if (crn_SleepState == SLEEP_STATE_deep) {
                        long deepDuration = currentTime - deepStartTime;
                        totaldeepTime += deepDuration;

                        if (deepDuration >= 18000) {
                            crn_SleepState = SLEEP_STATE_low;
                            lowStartTime = currentTime;
                        } else if (deepDuration >= 6000 && crn_SleepState == SLEEP_STATE_low) {
                            crn_SleepState = SLEEP_STATE_deep;
                            deepStartTime = currentTime;
                        }

                        // 깊은 수면 상태일 때 dB 값이 60 이상이면 얕은 수면 상태로 변경
                        if (crn_SleepState == SLEEP_STATE_deep && soundVisualizerView.getdB() >= 60) {
                            crn_SleepState = SLEEP_STATE_low;
                            lowStartTime = currentTime;
                        }
                    } else if (crn_SleepState == SLEEP_STATE_low) {
                        totallowTime += (currentTime - lowStartTime);
                        if (acceleration > ACCELERATION_THRESHOLD) {
                            crn_SleepState = SLEEP_STATE_low;
                            lowStartTime = currentTime;
                        }
                    }

                    if (acceleration > ACCELERATION_THRESHOLD) {
                        if (crn_SleepState == SLEEP_STATE_deep) {
                            crn_SleepState = SLEEP_STATE_low;
                            lowStartTime = currentTime;
                        }
                    }
                }
        }

        // 조도 센서 이벤트 처리
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            float lightIntensity = event.values[0];

            if (lightIntensity >= 45 && crn_SleepState == SLEEP_STATE_deep) {
                // 빛의 세기가 45 이상이고 현재 깊은 수면 상태이면 얕은 수면 상태로 변경
                crn_SleepState = SLEEP_STATE_low;
                lowStartTime = currentTime; // 얕은 수면 상태 시작 시간 갱신
            }
        }
        if(crn_SleepState==SLEEP_STATE_wait && currentTime - waitStartTime>=10)//얕은에서 10초
        {
            crn_SleepState = SLEEP_STATE_low;
        }
    }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // 가속도계 센서 정확도 변경 이벤트 처리 (여기서는 사용하지 않음)
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    //값 저장을 쉽게 하기 위한 모듈
    private void saveValue(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    private int getCurrentDayOfWeek() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.DAY_OF_WEEK);
    }
    //현재의 시간을 갱신해주는 메소드
    private void updateClock() {
        SimpleDateFormat Time_set = new SimpleDateFormat("HH:mm"); //시간:분 표시
        SimpleDateFormat Time_set2 = new SimpleDateFormat("a"); //am/pm 표시
        String currentTime = Time_set.format(new Date());
        String currentAP = Time_set2.format(new Date());

        // TextView에 현재 시간 표시
        crn_time.setText(currentTime);
        am_pm.setText(currentAP);
    }


}

