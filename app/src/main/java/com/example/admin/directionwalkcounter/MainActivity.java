package com.example.admin.directionwalkcounter;

// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener, SensorEventListener, TextToSpeech.OnInitListener {
    static final float BORDER_MAX = 11.0F;
    static final float BORDER_MIN = 9.5F;
    TextView mTextViewX;
    TextView mTextViewY;
    TextView mTextViewZ;
    TextView mTextViewComposite;
    TextView mTextViewCount;
    TextView mTextViewFront;
    TextView mTextViewBack;
    TextView mTextViewRight;
    TextView mTextViewLeft;
    SensorManager mSensorManager;
    Sensor mSensor;
    float mSmoothedValue = 0.0F;
    float mPreSmoothedValue = 0.0F;

    float mPreX = 0;
    float mPreY = 0;
    float mPreZ = 0;

    int mCount = 0;

    int mFrontCount = 0;
    int mBackCount = 0;
    int mRightCount = 0;
    int mLeftCount = 0;

    boolean mIsUpper = false;
    SpeakActivity speakactivity =new SpeakActivity();
    public static TextToSpeech tts;

    private static final String TAG = "TTS";

    public MainActivity() {
    }

    public void onInit(int status) {
        // TTS初期化
        if (TextToSpeech.SUCCESS == status) {
            Log.d(TAG, "initialized");
        } else {
            Log.e(TAG, "faile to initialize");
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        PackageManager packageManager = this.getPackageManager();
        if(!packageManager.hasSystemFeature("android.hardware.sensor.accelerometer")) {
            Toast.makeText(this, "加速度センサが搭載されていません", Toast.LENGTH_SHORT).show();
            this.finish();
        }

        this.mTextViewX = (TextView)this.findViewById(R.id.textview_x);
        this.mTextViewY = (TextView)this.findViewById(R.id.textview_y);
        this.mTextViewZ = (TextView)this.findViewById(R.id.textview_z);
        this.mTextViewComposite = (TextView)this.findViewById(R.id.textview_composite);
        this.mTextViewCount = (TextView)this.findViewById(R.id.textview_count);

        this.mTextViewFront = (TextView)this.findViewById(R.id.textview_front);
        this.mTextViewBack = (TextView)this.findViewById(R.id.textview_back);
        this.mTextViewRight = (TextView)this.findViewById(R.id.textview_right);
        this.mTextViewLeft = (TextView)this.findViewById(R.id.textview_left);

        Button buttonStart = (Button)this.findViewById(R.id.button_start);
        buttonStart.setOnClickListener(this);
        Button buttonStop = (Button)this.findViewById(R.id.button_stop);
        buttonStop.setOnClickListener(this);
        Button buttonReset = (Button)this.findViewById(R.id.button_reset);
        buttonReset.setOnClickListener(this);
        this.mSensorManager = (SensorManager)this.getSystemService(SENSOR_SERVICE);
        this.mSensor = this.mSensorManager.getDefaultSensor(1);

        tts = new TextToSpeech(this, this);
    }


    public void onClick(View v) {
        if(v.getId() == R.id.button_start) {
            Toast.makeText(this, "開始", Toast.LENGTH_SHORT).show();
            this.mSensorManager.registerListener(this, this.mSensor, 1);
        } else if(v.getId() == R.id.button_stop) {
            Toast.makeText(this, "停止", Toast.LENGTH_SHORT).show();
            this.mSensorManager.unregisterListener(this);

        } else if(v.getId() == R.id.button_reset) {
            Toast.makeText(this, "リセット", Toast.LENGTH_SHORT).show();
            this.mSensorManager.unregisterListener(this);
            this.mCount = 0;
            this.mTextViewCount.setText(String.valueOf(this.mCount) + "歩");
            this.mIsUpper = false;
            this.mSmoothedValue = 0.0F;

            this.mFrontCount = 0;
            this.mBackCount = 0;
            this.mRightCount = 0;
            this.mLeftCount = 0;
            this.mTextViewFront.setText("前:"+ String.valueOf(mFrontCount));
            this.mTextViewBack.setText("後:"+ String.valueOf(mBackCount));
            this.mTextViewRight.setText("右:"+ String.valueOf(mRightCount));
            this.mTextViewLeft.setText("左:"+ String.valueOf(mLeftCount));

            this.mTextViewX.setText("X軸：");
            this.mTextViewY.setText("Y軸：");
            this.mTextViewZ.setText("Z軸：");
            this.mTextViewComposite.setText("合成：");

            this.mTextViewFront.setText("前:");
            this.mTextViewBack.setText("後:");
            this.mTextViewRight.setText("右:");
            this.mTextViewLeft.setText("左:");
        }

    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent event) {
        String direction="";
        if(event.sensor.getType() == 1) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            float compositeValue = (float)Math.sqrt((double)(x * x + y * y + z * z));
            if(this.mPreSmoothedValue == 0.0F) {
                this.mSmoothedValue = compositeValue;
            } else {
                this.mSmoothedValue = compositeValue * 0.1F + this.mPreSmoothedValue * 0.9F;
            }

            this.mTextViewX.setText("X軸：" + String.valueOf(x));
            this.mTextViewY.setText("Y軸：" + String.valueOf(y));
            this.mTextViewZ.setText("Z軸：" + String.valueOf(z));
            this.mTextViewComposite.setText("合成：" + String.valueOf(this.mSmoothedValue));
            if(this.mSmoothedValue > 11.0F) {
                this.mIsUpper = true;
            }

            if(this.mSmoothedValue < 9.5F && this.mIsUpper) {
                ++this.mCount;
                this.mTextViewCount.setText(String.valueOf(this.mCount) + "歩");
                this.mIsUpper = false;

                //前後左右の判定を書く(スマホを縦に持っている状態)
                if((Math.abs(z-mPreZ))>(Math.abs(x-mPreX))) {
                    if (z < mPreZ) {
                        mFrontCount++;
                        direction="前";
                    } else if (z > mPreZ) {
                        mBackCount++;
                        direction="後ろ";
                    }
                }
                else {
                    if (x > mPreX) {
                        mRightCount++;
                        direction="右";
                    } else if (x < mPreX) {
                        mLeftCount++;
                        direction="左";
                    }
                }
                speakactivity.speechText(direction,tts);
                this.mTextViewFront.setText("前:"+ String.valueOf(mFrontCount));
                this.mTextViewBack.setText("後:"+ String.valueOf(mBackCount));
                this.mTextViewRight.setText("右:"+ String.valueOf(mRightCount));
                this.mTextViewLeft.setText("左:"+ String.valueOf(mLeftCount));

            }

            this.mPreSmoothedValue = this.mSmoothedValue;
            this.mPreX = x;
            this.mPreY = y;
            this.mPreZ = z;

        }

    }

    public void onDestroy() {
        super.onDestroy();
        this.mSensorManager.unregisterListener(this);
    }
}
