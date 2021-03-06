package cn.edu.caztc.bluetoothcar.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Iterator;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.edu.caztc.bluetoothcar.R;
import cn.edu.caztc.bluetoothcar.util.BluetoothOperationCallback;
import cn.edu.caztc.bluetoothcar.util.BluetoothOperator;
import cn.edu.caztc.bluetoothcar.util.SharedPreferencesUtil;

public class MainActivity extends AppCompatActivity {

    //常量声明区
    private static final String TAG = "蓝牙" + MainActivity.class.getSimpleName();

    //UI控件成员变量声明区
    @BindView(R.id.tv_ConnectState)
    TextView tvConnectState;
    @BindView(R.id.tv_CurrentSpeed)
    TextView tvCurrentSpeed;
    @BindView(R.id.btn_Connect)
    Button btnConnect;
    @BindView(R.id.iv_Icon)
    ImageView ivIcon;
    @BindView(R.id.tv_SchoolName)
    TextView tvSchoolName;
    @BindView(R.id.tv_URL)
    TextView tvURL;
    @BindView(R.id.btn_Forward)
    Button btnForward;
    @BindView(R.id.btn_TurnLeft)
    Button btnTurnLeft;
    @BindView(R.id.btn_Stop)
    Button btnStop;
    @BindView(R.id.btn_TurnRight)
    Button btnTurnRight;
    @BindView(R.id.btn_Back)
    Button btnBack;
    @BindView(R.id.btn_Left)
    Button btnLeft;
    @BindView(R.id.btn_Center)
    Button btnCenter;
    @BindView(R.id.btn_Right)
    Button btnRight;
    @BindView(R.id.btn_LeftRotate)
    Button btnLeftRotate;
    @BindView(R.id.btn_Outfire)
    Button btnOutfire;
    @BindView(R.id.btn_Accelerate)
    Button btnAccelerate;
    @BindView(R.id.btn_RightRotate)
    Button btnRightRotate;
    @BindView(R.id.btn_Whistle)
    Button btnWhistle;
    @BindView(R.id.btn_Moderate)
    Button btnModerate;
    @BindView(R.id.btn_setting)
    Button btnSetting;
    @BindView(R.id.btn_AutopilotMode)
    Button btnAutopilotMode;
    @BindView(R.id.btn_TrackingMode)
    Button btnTrackingMode;
    @BindView(R.id.btn_ExitMode)
    Button btnExitMode;
    @BindView(R.id.btn_tio)
    Button btnTio;

    //普通成员变量声明区
    private Context mContext = this;
    private boolean hasEnabled = false;
    private BluetoothOperator mBluetoothOp;
    private BluetoothAdapter mBluetoothAdapter;
    private SensorManager mSensorManager;
    private AlertDialog.Builder mBuilder;

    private Set<BluetoothDevice> devicesSet = null;
    //命令名称
    private String[] commandNameArr = {"forward", "back", "turn_letf", "turn_right", "stop", "left", "center", "right",
            "left_rotate", "right_rotate", "outfire", "whistle", "accelerate", "moderate", "autopilotmode", "trackingmode", "exitmode"};
    //初始命令
    private String[] firstCommandArr = {"ONA", "ONB", "ONC", "OND", "ONF", "left", "center", "right",
            "1", "2", "3", "4", "5", "6", "$4WD,MODE31#", "$4WD,MODE21#", "$4WD,MODE30#"};
    private String[] commandArr = new String[17];
    private String[] blueToothDeviceNameArr;
    private String[] blueToothDeviceMACArr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //打开蓝牙
        mBluetoothOp = new BluetoothOperator(this);
        mBluetoothOp.registerCallback(callback);
        if (mBluetoothOp.isEnabled())
            hasEnabled = true;
        //初始化蓝牙适配器
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        init();
    }


    /**
     * 初始化数据,添加ontouch监听
     */
    public void init() {
        //重力传感器获取服务
        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));

        for (int i = 0; i < commandNameArr.length; i++) {
            commandArr[i] = SharedPreferencesUtil.getInstance(mContext).getSP(commandNameArr[i], firstCommandArr[i]);
        }


        btnForward.setOnTouchListener(this.touchListener);
        btnBack.setOnTouchListener(this.touchListener);
        btnTurnLeft.setOnTouchListener(this.touchListener);
        btnTurnRight.setOnTouchListener(this.touchListener);
        btnStop.setOnTouchListener(this.touchListener);

    }



    /**
     * 相关按钮单击事件
     *
     * @param view
     */
    @OnClick({R.id.btn_Connect, R.id.btn_Left, R.id.btn_Center, R.id.btn_Right, R.id.btn_LeftRotate, R.id.btn_Outfire, R.id.btn_Accelerate, R.id.btn_RightRotate, R.id.btn_Whistle, R.id.btn_Moderate, R.id.btn_setting, R.id.btn_AutopilotMode, R.id.btn_TrackingMode, R.id.btn_ExitMode,R.id.btn_tio})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_Connect:
                showPairedBluetoothList();
                break;
            case R.id.btn_Left:
                mBluetoothOp.write(commandArr[5].getBytes());
                break;
            case R.id.btn_Center:
                mBluetoothOp.write(commandArr[6].getBytes());
                break;
            case R.id.btn_Right:
                mBluetoothOp.write(commandArr[7].getBytes());
                break;
            case R.id.btn_LeftRotate:
                mBluetoothOp.write(commandArr[8].getBytes());
                break;
            case R.id.btn_Outfire:
                mBluetoothOp.write(commandArr[10].getBytes());
                break;
            case R.id.btn_Accelerate:
                mBluetoothOp.write(commandArr[12].getBytes());
                break;
            case R.id.btn_RightRotate:
                mBluetoothOp.write(commandArr[9].getBytes());
                break;
            case R.id.btn_Whistle:
                mBluetoothOp.write(commandArr[11].getBytes());
                break;
            case R.id.btn_Moderate:
                mBluetoothOp.write(commandArr[13].getBytes());
                break;
            case R.id.btn_AutopilotMode:
                mBluetoothOp.write(commandArr[14].getBytes());
                break;
            case R.id.btn_TrackingMode:
                mBluetoothOp.write(commandArr[15].getBytes());
                break;
            case R.id.btn_ExitMode:
                mBluetoothOp.write(commandArr[16].getBytes());
                break;
            case R.id.btn_setting:
                showSettingList();
                break;
            case R.id.btn_tio:
                Intent intent =new Intent(this,Main2Activity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
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
                    case R.id.btn_Forward:
                        mBluetoothOp.write(commandArr[0].getBytes());
                        break;
                    case R.id.btn_Back:
                        mBluetoothOp.write(commandArr[1].getBytes());
                        break;
                    case R.id.btn_TurnLeft:
                        mBluetoothOp.write(commandArr[2].getBytes());
                        break;
                    case R.id.btn_TurnRight:
                        mBluetoothOp.write(commandArr[3].getBytes());
                        break;
                    case R.id.btn_Stop:
                        mBluetoothOp.write(commandArr[4].getBytes());
                        break;
                }
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP || motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
                Log.d(TAG, "onTouch: UP");
                //弹起或者取消事件
                mBluetoothOp.write(commandArr[4].getBytes());
            }
            return false;    // return false表示系统会继续处理
        }
    };


    /**
     * 蓝牙回调
     */
    private BluetoothOperationCallback callback = new BluetoothOperationCallback() {

        @Override
        public void onConnect(int err, String desc) {
            if (err == 0) {
                Log.d(TAG, "蓝牙连接成功");

                tvConnectState.setText(getResources().getString(R.string.main_connected));
            }
        }

        @Override
        public void onDataRecv(int err, String desc, byte[] data, int len) {
            Log.d(TAG, "onDataRecv: " + err);
            if (err == 0) {
                String str = new String(data);
                Log.d(TAG, "\n 蓝牙接收数据:" + new String(data, 0, len));
            }
        }
    };


    /**
     * 已配对蓝牙列表 dialog
     */
    private void showPairedBluetoothList() {
        //获取已经保存过的设备信息
        devicesSet = mBluetoothAdapter.getBondedDevices();
        if (devicesSet.size() > 0) {
            blueToothDeviceNameArr = new String[devicesSet.size()];
            blueToothDeviceMACArr = new String[devicesSet.size()];
            int i = 0;
            for (Iterator<BluetoothDevice> iterator = devicesSet.iterator(); iterator.hasNext(); ) {
                BluetoothDevice bluetoothDevice = (BluetoothDevice) iterator.next();
                Log.d(TAG, "onCreate: " + bluetoothDevice.getName() + " " + bluetoothDevice.getAddress());
                blueToothDeviceNameArr[i] = bluetoothDevice.getName();
                blueToothDeviceMACArr[i] = bluetoothDevice.getAddress();
                i++;
            }
            mBuilder = new AlertDialog.Builder(this).setIcon(R.mipmap.ic_icon_foreground)
                    .setTitle("已连接过的蓝牙ble")
                    .setItems(blueToothDeviceNameArr, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(MainActivity.this, "你点击的内容为： " + blueToothDeviceMACArr[i], Toast.LENGTH_LONG).show();
                            mBluetoothOp.connect(blueToothDeviceMACArr[i]);
                        }
                    });
            mBuilder.create().show();
        } else {
            Toast.makeText(this, "你没有保存蓝牙好么", Toast.LENGTH_SHORT).show();
        }

    }


    /**
     * 输出设置列表列表 dialog
     */
    private void showSettingList() {
        final String[] setting = {"设置前进按钮", "设置后退按钮", "设置左转按钮", "设置右转按钮", "设置停止按钮", "设置左按钮", "设置中按钮",
                "设置右按钮", "设置左旋按钮", "设置右旋按钮", "设置灭火按钮", "设置鸣笛按钮", "设置加速按钮", "设置减速按钮", "设置自动计时模式按钮", "设置循迹模式按钮", "设置模式退出按钮"};
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
                            Toast.makeText(MainActivity.this, "你输入的值为空", Toast.LENGTH_SHORT).show();
                        } else {
                            commandArr[type] = editText.getText().toString();
                            SharedPreferencesUtil.getInstance(mContext).putSP(commandNameArr[type], commandArr[type]);
                        }
                    }
                });
        mBuilder.create().show();
    }


    //Activity被覆盖到下面或者锁屏时被调用
    @Override
    protected void onPause() {
        super.onPause();
    }

    //退出当前Activity时被调用,调用之后Activity就结束了
    @Override
    protected void onDestroy() {
        super.onDestroy();

        mBluetoothOp.unregisterCallback(callback);
        if (!hasEnabled)
            mBluetoothOp.disable();
        mBluetoothOp.destroy();
    }

    //Activity创建或者从被覆盖、后台重新回到前台时被调用
    protected void onResume() {
        super.onResume();
        mBluetoothOp.enable();
    }

    //退出当前Activity或者跳转到新Activity时被调用
    public void onStop() {
        super.onStop();
    }


}
