package cn.edu.caztc.bluetoothcar.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import cn.edu.caztc.bluetoothcar.R;
import cn.edu.caztc.bluetoothcar.application.Constants;
import cn.edu.caztc.bluetoothcar.media.IjkVideoView;
import cn.edu.caztc.bluetoothcar.util.SharedPreferencesUtil;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

import static cn.edu.caztc.bluetoothcar.media.IRenderView.AR_MATCH_PARENT;
import static cn.edu.caztc.bluetoothcar.media.IjkVideoView.RENDER_TEXTURE_VIEW;
import static cn.edu.caztc.bluetoothcar.media.IjkVideoView.RTP_JPEG_PARSE_PACKET_METHOD_FILL;

public class Main2Activity extends AppCompatActivity {
    private static final String TAG = "Main2Activity";

    private Context mContext = this;

    private Button mTakePictureButton;
    private Button mRecordVideoButton;
    private Button mSetVideoRotationButton;

    private Button mSetting;
    private Button mSendShang;
    private Button mSendxia;

    private AlertDialog.Builder mBuilder;

//    private Button mSendDataButton;
//    private Button mSetVrModeButton;

    private ProgressBar mProgressBar;

    /* 预览设置 */
    // 渲染视图，不需要更改
    private static final int VIDEO_VIEW_RENDER = RENDER_TEXTURE_VIEW;
    // 拉伸方式，根据需要选择等比例拉伸或者全屏拉伸等
    private static final int VIDEO_VIEW_ASPECT = AR_MATCH_PARENT;
    // JPEG解析方式，默认使用填充方式（即网络数据包丢失，则用上一帧数据补上），可以改为DROP（丢失数据包则丢掉整帧，网络不好不要使用），ORIGIN（原始方式，不要使用）
    private static final int RTP_JPEG_PARSE_PACKET_METHOD = RTP_JPEG_PARSE_PACKET_METHOD_FILL;
    // 重连等待间隔，单位ms
    private static final int RECONNECT_INTERVAL = 500;

    private String mVideoPath;
    private IjkVideoView mVideoView;

    // 状态
    private boolean recording = false;
    private static int videoRotationDegree = 90;

    private String commandNameShang, commandNameXia, commandNameStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main2);
        // handle arguments
        mVideoPath = Constants.RTSP_ADDRESS;

        // init player
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");

        mVideoView = (IjkVideoView) findViewById(R.id.video_view);
        mVideoView.setRtpJpegParsePacketMethod(RTP_JPEG_PARSE_PACKET_METHOD);
        mVideoView.setRender(VIDEO_VIEW_RENDER);
        mVideoView.setAspectRatio(VIDEO_VIEW_ASPECT);

        commandNameShang = SharedPreferencesUtil.getInstance(this).getSP("shang", "$Servo,LRR#");
        commandNameXia = SharedPreferencesUtil.getInstance(this).getSP("xia", "$Servo,LRL#");
        commandNameStop = SharedPreferencesUtil.getInstance(this).getSP("main2stop", "$Servo,LRS#");
        // 准备开始预览回调
        mVideoView.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer mp) {
                onStartPlayback();
            }
        });
        // 发生错误回调
        mVideoView.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer mp, int what, int extra) {
                stopAndRestartPlayback();
                return true;
            }
        });
        // 收到图传板数据回调
        mVideoView.setOnReceivedRtcpSrDataListener(new IMediaPlayer.OnReceivedRtcpSrDataListener() {
            @Override
            public void onReceivedRtcpSrData(IMediaPlayer mp, byte[] data) {
                // 因为数据通道是和RTCP共用，所以回传数据需要和RTCP的Sender Report区分开，需要加上自己的标志区分
                // RTCP默认每5秒发送一次Sender Report
                // 以后会封装起来，直接发送和接收数据
                Log.d(TAG, new String(data) + Arrays.toString(data));
            }
        });
        // 拍照回调
        // resultCode，<0 发生错误，=0 拍下一张照片，=1，完成拍照
        mVideoView.setOnTookPictureListener(new IMediaPlayer.OnTookPictureListener() {
            @Override
            public void onTookPicture(IMediaPlayer mp, int resultCode, String fileName) {
                if (resultCode == 1) {
                    showToast("拍照完成");
                } else if (resultCode == 0) {
                    showToast("已拍摄：" + fileName);
                } else if (resultCode < 0) {
                    showToast("拍照发生错误");
                }
            }
        });
        // 录像回调
        // resultCode，<0 发生错误，=0开始录像，否则就是成功保存录像
        mVideoView.setOnRecordVideoListener(new IMediaPlayer.OnRecordVideoListener() {
            @Override
            public void onRecordVideo(IMediaPlayer mp, final int resultCode, final String fileName) {
                Handler handler = new Handler(getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (resultCode < 0) {
                            recording = false;

                            mRecordVideoButton.setText("开始录像");
                            showToast("录像发生错误");
                        } else if (resultCode == 0) {
                            recording = true;

                            mRecordVideoButton.setText("停止录像");
                            showToast("开始录像");
                        } else {
                            mRecordVideoButton.setText("开始录像");
                            showToast("录像完成");

                            // set flag
                            recording = false;
                        }
                    }
                });
            }

        });
        // 播放完成后
        mVideoView.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(IMediaPlayer mp) {
                mVideoView.stopPlayback();
                mVideoView.release(true);
                mVideoView.stopBackgroundPlay();
            }
        });
        // prefer mVideoPath
        if (mVideoPath != null)
            mVideoView.setVideoPath(mVideoPath);
        else {
            Log.e(TAG, "Null Data Source\n");
            finish();
            return;
        }

        /* 按键 */

        mTakePictureButton = (Button) findViewById(R.id.take_picture_button);
        mTakePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto(1);
            }
        });

        mRecordVideoButton = (Button) findViewById(R.id.record_video_button);
        mRecordVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordVideo();
            }
        });

        mSetVideoRotationButton = (Button) findViewById(R.id.set_video_rotation_button);
        mSetVideoRotationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoRotationDegree += 90;
                videoRotationDegree %= 360;
                setVideoRotation(videoRotationDegree);
            }
        });

        mSetting = (Button) findViewById(R.id.btn_setting2);
        mSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSettingList();
            }
        });

        mSendShang = (Button) findViewById(R.id.send_shang);
        mSendShang.setOnTouchListener(this.touchListener);
//        mSendShang.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                sendFlyControllerData(commandNameShang.getBytes());
//            }
//        });

        mSendxia = (Button) findViewById(R.id.send_xia);
        mSendxia.setOnTouchListener(this.touchListener);
//        mSendxia.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                sendFlyControllerData(commandNameXia.getBytes());
//            }
//        });


        /* 进度条 */
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
    }

    /**
     * 按下弹起监听
     */
    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                Log.d(TAG, "onTouch: DOWN");
                // 按下
                switch (view.getId()) {
                    case R.id.send_shang:
                        sendFlyControllerData(commandNameShang.getBytes());
                        break;
                    case R.id.send_xia:
                        sendFlyControllerData(commandNameXia.getBytes());
                        break;
                }
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP || motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
                Log.d(TAG, "onTouch: UP");
                //弹起或者取消事件
                sendFlyControllerData(commandNameStop.getBytes());
            }
            return false;    // return false表示系统会继续处理
        }
    };

    /**
     * 输出设置列表列表 dialog
     */
    private void showSettingList() {
        final String[] setting = {"设置向上按钮", "设置向下按钮","设置停止指令"};
        mBuilder = new AlertDialog.Builder(this).setIcon(R.mipmap.ic_icon_foreground)
                .setTitle("设置指令")
                .setItems(setting, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        showSetCommandInput(i);
                    }
                });
        mBuilder.create().show();
    }

    /**
     * 设置指令 dialog
     */
    private void showSetCommandInput(int type) {
        final EditText editText = new EditText(this);
        mBuilder = new AlertDialog.Builder(this).setTitle("设置指令").setView(editText)
                .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (editText.getText().toString().equals("")) {
                            Toast.makeText(Main2Activity.this, "你输入的值为空", Toast.LENGTH_SHORT).show();
                        } else {
                            if (type == 0) {
                                commandNameShang = editText.getText().toString();
                                SharedPreferencesUtil.getInstance(mContext).putSP("shang", commandNameShang);
                            } else if (type == 1) {
                                commandNameXia = editText.getText().toString();
                                SharedPreferencesUtil.getInstance(mContext).putSP("xia", commandNameXia);
                            } else if (type == 2) {
                                commandNameXia = editText.getText().toString();
                                SharedPreferencesUtil.getInstance(mContext).putSP("main2stop", commandNameStop);
                            } else{
                                Toast.makeText(mContext, "出了点问题showSetCommandInput", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
        mBuilder.create().show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        // Activity slide from left
        overridePendingTransition(
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right
        );
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (!mVideoView.isBackgroundPlayEnabled()) {
            mVideoView.stopPlayback();
            mVideoView.release(true);
            mVideoView.stopBackgroundPlay();
        } else {
            mVideoView.enterBackground();
        }
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 开启屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mVideoView.setRender(VIDEO_VIEW_RENDER);
        mVideoView.setAspectRatio(VIDEO_VIEW_ASPECT);
        mVideoView.setVideoPath(mVideoPath);
        mVideoView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 关闭屏幕常亮
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // 停止录像
        if (recording)
            mVideoView.stopRecordVideo();
    }

    ;

    @Override
    protected void onDestroy() {
        super.onDestroy();

        IjkMediaPlayer.native_profileEnd();
    }

    /**
     * 播放开始后执行
     */
    private void onStartPlayback() {
        showToast("开始预览");
        // 隐藏ProgressBar
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    /**
     * 关闭播放器并重新开始播放
     * 错误发生的时候调用
     */
    private void stopAndRestartPlayback() {
        mProgressBar.setVisibility(View.VISIBLE);

        mVideoView.post(new Runnable() {
            @Override
            public void run() {
                mVideoView.stopPlayback();
                mVideoView.release(true);
                mVideoView.stopBackgroundPlay();
            }
        });
        mVideoView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mVideoView.setRender(VIDEO_VIEW_RENDER);
                mVideoView.setAspectRatio(VIDEO_VIEW_ASPECT);
                mVideoView.setVideoPath(mVideoPath);
                mVideoView.start();
            }
        }, RECONNECT_INTERVAL);
    }

    /**
     * 发送飞控数据
     * 因为数据通道是和RTCP共用，所以数据需要和RTCP的Receive Report区分开，需要加上自己的标志区分
     * 以后会封装起来，直接发送和接收数据
     */
    public void sendFlyControllerData(byte[] data) {
        // Send
        try {
            mVideoView.sendRtcpRrData(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 拍照
     */
    private void takePhoto(int num) {
        // Take a photo
        String photoFilePath = getPhotoDirPath();
        String photoFileName = getMediaFileName();
        try {
            // 拍照参数说明
            // 1、目录路径，目录需要先创建，否则返回错误
            // 2、文件名，不需要指定扩展名
            // 3和4、保存图像的宽高，如果都是-1（不允许只有一个-1），则保存原图像大小，如果是其他，则拉伸为设定值
            // 5、连续拍照数量，连续拍照，中间不设间隔
            mVideoView.takePicture(photoFilePath, photoFileName, -1, -1, num);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 录像，目前固定保存为AVI
     * 目前MJPEG格式为YUV420P，部分手机的默认播放器不支持，只支持YUV422P，不支持直接播放和使用什么容器（比如AVI、MP4）无关
     * 以后会加入录制时转码功能，但是使用这个功能在低端手机上可能会造成卡顿，根据实际需要选择使用
     */
    private void recordVideo() {
        if (recording) {
            mVideoView.stopRecordVideo();
        } else {
            String videoFilePath = getVideoDirPath();
            String videoFileName = getMediaFileName();
            // Start to record video
            try {
                // 录像参数说明
                // 1、目录路径，目录需要先创建，否则返回错误
                // 2、文件名，目前固定给".avi"，下一个版本将自动指定扩展名，届时会有说明
                // 3和4、录像的宽高，目前不使用，保留
                mVideoView.startRecordVideo(videoFilePath, videoFileName + ".avi", -1, -1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 设置VR模式（左右分屏显示）
     */
    private void setVrMode(boolean en) {
        mVideoView.setVrMode(en);
    }

    /**
     * 软件旋转屏幕，顺时针旋转（非Sensor旋转，图传传过来的图旋转角度不变，改变的是渲染的图像角度，不影响拍照和录像）
     */
    private void setVideoRotation(int degree) {
        mVideoView.setVideoRotation(degree);
    }


    private void showToast(String s) {
        Toast.makeText(Main2Activity.this, s, Toast.LENGTH_SHORT).show();
    }






    /* 以下是Demo使用到的方法 */

    // 主目录名
    private static final String HOME_PATH_NAME = "MediaStream";

    // 照片和视频的子目录名
    private static final String PHOTO_PATH_NAME = "Image";
    private static final String VIDEO_PATH_NAME = "Movie";

    /**
     * 获取应用数据主目录
     *
     * @return 主目录路径
     */
    static public String getHomePath() {
        String homePath = null;

        try {
            String extStoragePath = Environment.getExternalStorageDirectory().getCanonicalPath();
            File homeFile = new File(extStoragePath, HOME_PATH_NAME);
            homePath = homeFile.getCanonicalPath();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return homePath;
    }

    /**
     * 获取父目录下子目录
     */
    static public String getSubDir(String parent, String dir) {
        if (parent == null)
            return null;

        String subDirPath = null;

        try {
            // 获取展开的子目录路径
            File subDirFile = new File(parent, dir);

            if (!subDirFile.exists())
                subDirFile.mkdirs();

            subDirPath = subDirFile.getCanonicalPath();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return subDirPath;
    }

    /**
     * 获取主目录下照片目录
     *
     * @return 照片目录路径
     */
    static public String getPhotoPath() {
        return getSubDir(getHomePath(), PHOTO_PATH_NAME);
    }

    /**
     * 获取主目录下视频目录
     *
     * @return 视频目录路径
     */
    static public String getVideoPath() {
        return getSubDir(getHomePath(), VIDEO_PATH_NAME);
    }

    /**
     * 获取图片目录路径
     *
     * @return 图片目录路径
     */
    static public String getPhotoDirPath() {
        String photoPath = getPhotoPath();
        if (photoPath == null)
            return null;

        // 如果文件夹不存在, 则创建
        File photoDir = new File(photoPath);
        if (!photoDir.exists()) {
            // 创建失败则返回null
            if (!photoDir.mkdirs()) return null;
        }

        return photoDir.getAbsolutePath();
    }

    /**
     * 获取视频目录路径
     *
     * @return 视频目录路径
     */
    static public String getVideoDirPath() {
        String videoPath = getVideoPath();
        if (videoPath == null)
            return null;

        // 如果文件夹不存在, 则创建
        File videoDir = new File(videoPath);
        if (!videoDir.exists()) {
            // 创建失败则返回null
            if (!videoDir.mkdirs()) return null;
        }

        return videoDir.getAbsolutePath();
    }

    /**
     * 获取媒体文件名称
     *
     * @return 媒体文件名称
     */
    static public String getMediaFileName() {
        // 由日期创建文件名
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmsss", Locale.getDefault());
        String dateString = format.format(date);
//        String photoFileName = dateString + "." + PHOTO_FILE_EXTENSION;
        String photoFileName = dateString;

        return photoFileName;
    }

}
