package awesome.team.perapera;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gumb on 12/19/14. This class controls the sound recording and playing, including the
 * record button.
 */

interface RecordingStoppedEventListener {
    public void recordingStopped();
}

public class MediaControl {

    private static String mFileName = Environment.getExternalStorageDirectory().
            getAbsolutePath() + "/perapera/audioRecordTest.3gp";

    /// Sound code
    // Helper to control recording of sound.
    public boolean isRecording;
    public boolean isPlaying;
    public MediaRecorder mRecorder;
    List<RecordingStoppedEventListener> listeners = new ArrayList<>();
    private MediaPlayer mPlayer;
    private View recordBtn;
    private View listenBtn;


    public MediaControl(View recordButton, View listenButton) {
        isPlaying = false;
        isRecording = false;
        recordBtn = recordButton;
        listenBtn = listenButton;
    }

    public void addListener(RecordingStoppedEventListener toAdd) {
        listeners.add(toAdd);
    }

    public void notifyListeners() {
        // Notify everybody that may be interested.
        for (RecordingStoppedEventListener hl : listeners)
            hl.recordingStopped();
    }

    /* Start or stop recording. */
    public void onRecord(boolean start) {
        if (start)
            startRecording();
        else
            stopRecording();
    }

    /* Start or stop playing. */
    public void onPlay(boolean start) {
        if (start)
            startPlaying();
        else
            stopPlaying();
    }

    /* Change button and start playing the sound file. */
    private void startPlaying() {
        mPlayer = new MediaPlayer();

        // change button when sound ends as well
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                recordBtn.setActivated(false);
                recordBtn.setEnabled(true);
                listenBtn.setEnabled(true);
                isPlaying = false;
                isRecording = false;
            }
        });
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
            isPlaying = true;
        } catch (IOException e) {
            Log.e("AudioRecordTest", e.getMessage());
        }
    }

    /* Release the media player. */
    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
        isPlaying = false;
    }

    /* Create new media recorder, set encoding and start recording. */
    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mRecorder.setMaxDuration(30000); // 10 seconds
        mRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED)
                    stopRecording();
            }
        });
        try {
            mRecorder.prepare();
            mRecorder.start();
            isRecording = true;
        } catch (IOException e) {
            Log.e("AudioRecordTest", e.getMessage());
        }
    }

    /* Stop and release the recording. */
    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
        isRecording = false;
        notifyListeners();
    }

}
