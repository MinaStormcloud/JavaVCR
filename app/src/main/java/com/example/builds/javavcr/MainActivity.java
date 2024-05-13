package com.example.builds.javavcr;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import android.media.*;
import android.widget.Toast;
import java.io.IOException;
import android.support.v4.app.ActivityCompat;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import java.util.Random;
import android.net.Uri;
import android.hardware.Camera;
import android.view.SurfaceHolder;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

    private MediaRecorder recorder;
    private Button record;
    private Button stop;
    private Button play;
    private Button stopPlayback;
    private  VideoView videoView;
    private Camera camera;
    private SurfaceHolder surfaceHolder;

    String filePath = null;
    Random random ;
    String RandomAudioFileName = "0123456789";

    public static final int RequestPermissionCode = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        record = (Button)findViewById(R.id.record);
        stop = (Button)findViewById(R.id.stop);
        play = (Button)findViewById(R.id.play);
        stopPlayback = (Button)findViewById(R.id.stopPlayback);
        videoView = (VideoView)findViewById(R.id.videoView);

        record.setEnabled(true);
        stop.setEnabled(false);
        play.setEnabled(false);
        stopPlayback.setEnabled(false);
        random = new Random();

        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(checkPermission()) {

                    filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + CreateRandomAudioFileName(6) + "_Video.mp4";
                    surfaceCreated(surfaceHolder);
                    MediaRecorderReady();

                    try {
                        recorder.prepare();
                        recorder.start();
                    } catch (IllegalStateException e) {

                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    stop.setEnabled(true);
                    play.setEnabled(false);
                    record.setEnabled(false);
                    stopPlayback.setEnabled(false);

                    Toast.makeText(MainActivity.this, "Recording started", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    requestPermission();
                }
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                camera.stopPreview();
                camera.release();
                recorder.stop();
                record.setEnabled(true);
                stop.setEnabled(false);
                play.setEnabled(true);
                stopPlayback.setEnabled(false);
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse(filePath);
                videoView.setVideoURI(uri);
                videoView.start();
                record.setEnabled(false);
                stop.setEnabled(false);
                play.setEnabled(false);
                stopPlayback.setEnabled(true);

                videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {

                        mediaPlayer.reset();
                        stopPlayback.setEnabled(false);
                        record.setEnabled(true);
                        play.setEnabled(true);
                    }
                });
            }
        });

        stopPlayback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stop.setEnabled(false);
                play.setEnabled(false);
                stopPlayback.setEnabled(false);
                record.setEnabled(true);

                if(videoView != null){
                    videoView.stopPlayback();
                    recorder.release();
                }
            }
        });
    }

    public void surfaceCreated(SurfaceHolder holder) {
        int cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        try{
            camera = Camera.open(cameraId);
        }catch (RuntimeException ex){
            ex.printStackTrace();
        }

        Camera.Parameters parameters;
        parameters = camera.getParameters();

        camera.setDisplayOrientation(90);

        try{
            camera.setPreviewDisplay(holder);
            camera.startPreview();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

        @Override
    protected void onDestroy() // clean up the recorder
    {
        super.onDestroy();

        if (recorder != null)
        {
            recorder.release();
            recorder = null;
        }
    }

    public void MediaRecorderReady() {
        recorder = new MediaRecorder();
        camera.unlock();
        recorder.setCamera(camera);

        recorder.setOrientationHint(90);
        recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setVideoSize(640, 480);
        recorder.setVideoFrameRate(16);
        recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        recorder.setVideoEncodingBitRate((3000000));
        recorder.setAudioEncodingBitRate(16 * 44100);
        recorder.setAudioSamplingRate(44100);
        recorder.setPreviewDisplay(videoView.getHolder().getSurface());  //Displays the preview sideways and stretched
        recorder.setOutputFile(filePath);
    }

    public String CreateRandomAudioFileName(int string){
        StringBuilder stringBuilder = new StringBuilder( string );
        int i = 0 ;
        while(i < string ) {
            stringBuilder.append(RandomAudioFileName.charAt(random.nextInt(RandomAudioFileName.length())));
            i++ ;
        }
        return stringBuilder.toString();
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]
                {READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, CAMERA, RECORD_AUDIO}, RequestPermissionCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length> 0) {
                    boolean WriteStoragePermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean RecordPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean ReadStoragePermission = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    boolean CameraPermission = grantResults[3] == PackageManager.PERMISSION_GRANTED;

                    if (WriteStoragePermission && RecordPermission && ReadStoragePermission && CameraPermission) {
                        Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this,"Permission Denied",Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    public boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        int result2 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        int result3 = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED &&
                result2 == PackageManager.PERMISSION_GRANTED && result3 == PackageManager.PERMISSION_GRANTED;
    }
}
