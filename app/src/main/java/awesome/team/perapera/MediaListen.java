package awesome.team.perapera;

import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;

/**
 * Created by gumb on 12/19/14. This class controls the sound recording and playing, including the
 * record button.
 */
public class MediaListen {
    private String fileName;

    // Helper to control recording of sound.
    private boolean isPlaying;
    private MediaPlayer mPlayer;

    public MediaListen() {
        isPlaying = false;
    }

    /* Start or stop playing. */
    public void playPause(String fileName) {
        this.fileName = fileName;
        if (isPlaying)
            stopPlaying();
        else
            startPlaying();
    }

    /* Spawn MediaPlayer, choose what happens on completion and change button and start playing the
    sound file. */
    public void startPlaying() {
        stopPlaying();
        mPlayer = new MediaPlayer();
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if (mPlayer != null) {
                    mPlayer.release();
                    mPlayer = null;
                }
                isPlaying = false;
            }
        });
        try {
            mPlayer.setDataSource(fileName);
            mPlayer.prepare();
            mPlayer.start();
            isPlaying = true;
        } catch (IOException e) {
            Log.e("AudioRecordTest", e.getMessage());
        }
    }

    /* Release the media player. */
    public void stopPlaying() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
        isPlaying = false;
    }
}
