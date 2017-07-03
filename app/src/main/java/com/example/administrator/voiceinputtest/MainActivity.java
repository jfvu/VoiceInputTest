package com.example.administrator.voiceinputtest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import java.util.List;

public class MainActivity extends AppCompatActivity{
    /**
     * 语音识别
     */
    private EditText etText;
    private Button btnStartSpeak;

    /**
     * 语音合成
     */
    private EditText etHeCheng;
    private Button btnStartHeCheng;

    //有动画效果
    private RecognizerDialog iatDialog;
    //无动画效果
    private SpeechRecognizer mIat;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //语音对象初始化（id为注册码）
        SpeechUtility.createUtility(MainActivity.this, SpeechConstant.APPID +"=594a0ea2");
        initView();
    }

    private void initView() {
        etText = (EditText) findViewById(R.id.et_1);
        btnStartSpeak = (Button) findViewById(R.id.but1);

        //开始语音识别
        btnStartSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 有交互动画的语音识别器
                iatDialog = new RecognizerDialog(MainActivity.this, mInitListener);
                //1.创建SpeechRecognizer对象(没有交互动画的语音识别器)，第2个参数：本地听写时传InitListener
                //                mIat = SpeechRecognizer.createRecognizer(MainActivity.this, mInitListener);
                //                // 2.设置听写参数
                //                mIat.setParameter(SpeechConstant.DOMAIN, "iat"); // domain:域名
                //                mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
                //                mIat.setParameter(SpeechConstant.ACCENT, "mandarin"); // mandarin:普通话
                //保存音频文件到本地（有需要的话）   仅支持pcm和wav
                //mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/mIat.wav");
                //        mIat.startListening(mRecognizerListener);//

                iatDialog.setListener(new RecognizerDialogListener() {
                    String resultJson = "[";//放置在外边做类的变量则报错，会造成json格式不对（？）

                    @Override
                    public void onResult(RecognizerResult recognizerResult, boolean isLast) {
                        System.out.println("-----------------   onResult   -----------------");
                        if (!isLast) {
                            resultJson += recognizerResult.getResultString() + ",";
                        } else {
                            resultJson += recognizerResult.getResultString() + "]";
                        }

                        if (isLast) {
                            //解析语音识别后返回的json格式的结果
                            Gson gson = new Gson();
                            List<DictationResult> resultList = gson.fromJson(resultJson,
                                    new TypeToken<List<DictationResult>>() {
                                    }.getType());
                            String result = "";
                            for (int i = 0; i < resultList.size() - 1; i++) {
                                result += resultList.get(i).toString();
                            }
                            etText.setText(result);
                            //获取焦点
                            etText.requestFocus();
                            //将光标定位到文字最后，以便修改
                            etText.setSelection(result.length());
                        }
                    }

                    @Override
                    public void onError(SpeechError speechError) {
                        //自动生成的方法存根
                        speechError.getPlainDescription(true);
                    }
                });
                //开始听写，需将sdk中的assets文件下的文件夹拷入项目的assets文件夹下（没有的话自己新建）
                iatDialog.show();
            }
        });

        //语音合成
        etHeCheng = (EditText) findViewById(R.id.et_2);
        btnStartHeCheng = (Button) findViewById(R.id.but2);
        btnStartHeCheng.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startHeCheng(etHeCheng.getText().toString());

            }
        });
    }


    /**
     * 用于SpeechRecognizer（无交互动画）对象的监听回调
     */
    private RecognizerListener mRecognizerListener = new RecognizerListener() {
        @Override
        public void onVolumeChanged(int i, byte[] bytes) {

        }

        @Override
        public void onBeginOfSpeech() {

        }

        @Override
        public void onEndOfSpeech() {

        }

        @Override
        public void onResult(RecognizerResult recognizerResult, boolean b) {
            Log.i(TAG, recognizerResult.toString());
        }

        @Override
        public void onError(SpeechError speechError) {

        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }
    };

    public static final String TAG = "MainActivity";
    private InitListener mInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                Toast.makeText(MainActivity.this, "初始化失败，错误码：" + code, Toast.LENGTH_SHORT).show();
            }
        }
    };

    /**
     * 开始科大讯飞的合成语音
     */
    private void startHeCheng(String recordResult) {
        //1.创建SpeechSynthesizer对象, 第二个参数：本地合成时传InitListener
        SpeechSynthesizer mTts = SpeechSynthesizer.createSynthesizer(MainActivity.this, null);

        /**
         2.合成参数设置，详见《科大讯飞MSC API手册(Android)》SpeechSynthesizer 类
         *
         */

        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);

        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); //设置云端
        mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");//设置发音人
        mTts.setParameter(SpeechConstant.SPEED, "50");//设置语速
        //设置合成音调
        mTts.setParameter(SpeechConstant.PITCH, "50");
        mTts.setParameter(SpeechConstant.VOLUME, "80");//设置音量，范围0~100
        mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");
        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        //        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        //        boolean isSuccess = mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/tts2.wav");
        //        Toast.makeText(MainActivity.this, "语音合成 保存音频到本地：\n" + isSuccess, Toast.LENGTH_LONG).show();
        //3.开始合成
        int code = mTts.startSpeaking(recordResult, mSynListener);

        if (code != ErrorCode.SUCCESS) {
            if (code == ErrorCode.ERROR_COMPONENT_NOT_INSTALLED) {
                //上面的语音配置对象为初始化时：
                Toast.makeText(MainActivity.this, "语音组件未安装", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MainActivity.this, "语音合成失败,错误码: " + code, Toast.LENGTH_LONG).show();
            }
        }

    }

    //合成监听器
    private SynthesizerListener mSynListener = new SynthesizerListener() {
        //会话结束回调接口，没有错误时，error为null
        public void onCompleted(SpeechError error) {
        }

        //缓冲进度回调
        //percent为缓冲进度0~100，beginPos为缓冲音频在文本中开始位置，endPos表示缓冲音频在文本中结束位置，info为附加信息。
        public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
        }

        //开始播放
        public void onSpeakBegin() {
        }

        //暂停播放
        public void onSpeakPaused() {
        }

        //播放进度回调
        //percent为播放进度0~100,beginPos为播放音频在文本中开始位置，endPos表示播放音频在文本中结束位置.
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
        }

        //恢复播放回调接口
        public void onSpeakResumed() {
        }

        //会话事件回调接口
        public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
        }
    };

}