package com.student.sleepcycle;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class BackgroundSoundService extends Service {
    private MediaPlayer[] mediaPlayers;
    private static final String EXTRA_SOUND_RESOURCE = "soundResource";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra("soundResources")) {
            int[] soundResources = intent.getIntArrayExtra("soundResources");
            if (soundResources != null) {
                mediaPlayers = new MediaPlayer[soundResources.length];
                int birdVolume = intent.getIntExtra("birdVolume", 0);
                int wavepoolVolume = intent.getIntExtra("wavepoolVolume", 0);
                int bonfireVolume = intent.getIntExtra("bonfireVolume", 0);
                int lot_rainVolume = intent.getIntExtra("lot_rainVolume", 0);
                int s_riverVolume = intent.getIntExtra("s_riverVolume", 0);

                for (int i = 0; i < soundResources.length; i++) {
                    mediaPlayers[i] = MediaPlayer.create(this, soundResources[i]);
                    mediaPlayers[i].setLooping(true);

                    float volume = getVolumeByIndex(i, birdVolume, wavepoolVolume, bonfireVolume,lot_rainVolume,s_riverVolume);
                    mediaPlayers[i].setVolume(volume, volume);

                    mediaPlayers[i].start();
                }
            }
        }
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        if (mediaPlayers != null) {
            for (MediaPlayer mediaPlayer : mediaPlayers) {
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                }
            }
        }
        super.onDestroy();
    }

    private float getVolumeByIndex(int index, int birdVolume, int wavepoolVolume, int bonfireVolume,int lot_rainVolume ,int s_riverVolume) {
        if (index == 0) {
            return birdVolume / 100.0f;
        } else if (index == 1) {
            return wavepoolVolume / 100.0f;
        } else if (index == 2) {
            return bonfireVolume / 100.0f;
        }else if (index == 3) {
            return lot_rainVolume / 100.0f;
        }else if (index == 4) {
            return s_riverVolume / 100.0f;
        }
        return 0.0f;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
