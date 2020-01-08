package cn.edu.caztc.bluetoothcar.activity;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Iterator;
import java.util.Set;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.edu.caztc.bluetoothcar.R;
import cn.edu.caztc.bluetoothcar.util.BluetoothOperationCallback;
import cn.edu.caztc.bluetoothcar.util.BluetoothOperator;
import cn.edu.caztc.bluetoothcar.util.SharedPreferencesUtil;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    //常量声明区
    private static final String TAG = "蓝牙" + MainActivity.class.getSimpleName();

    //UI控件成员变量声明区
    @BindView(R.id.tv_ConnectState)
    TextView tvConnectState;
    @BindView(R.id.tv_CurrentSpeed)
    TextView tvCurrentSpeed;
    @BindView(R.id.chk_GravitySensor)
    CheckBox chkGravitySensor;
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

    //普通成员变量声明区
    // Bluetooth
    private boolean hasEnabled = false;
    private BluetoothOperator mBluetoothOp;
    private BluetoothAdapter mBluetoothAdapter;
    private SensorManager sensorManager;
    private Context mContext = this;
    private AlertDialog.Builder builder;
    private ProgressDialog progressDialog;
    private String qianjin, houtui, zuozhuan, youzhuan, tingzhi;
    private Set<BluetoothDevice> devices = null;
    private String[] blueToothDeviceName;
    private String[] blueToothDeviceMAC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(1);
        getWindow().setFlags(1024, 1024);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mBluetoothOp = new BluetoothOperator(this);
        mBluetoothOp.registerCallback(callback);
        if (mBluetoothOp.isEnabled())
            hasEnabled = true;

        //初始化蓝牙适配器
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //获取已经保存过的设备信息
        devices = mBluetoothAdapter.getBondedDevices();
        if (devices.size() > 0) {
            for (Iterator<BluetoothDevice> iterator = devices.iterator(); iterator.hasNext(); ) {
                BluetoothDevice bluetoothDevice = (BluetoothDevice) iterator.next();
                Log.d(TAG, "onCreate: " + bluetoothDevice.getName() + " " + bluetoothDevice.getAddress());
            }
        }

        init();
    }


    /**
     * 初始化数据
     */
    public void init() {

        qianjin = SharedPreferencesUtil.getInstance(mContext).getSP("qianjin", "ONA");
        houtui = SharedPreferencesUtil.getInstance(mContext).getSP("houtui", "ONB");
        zuozhuan = SharedPreferencesUtil.getInstance(mContext).getSP("zuozhuan", "ONC");
        youzhuan = SharedPreferencesUtil.getInstance(mContext).getSP("youzhuan", "OND");
        tingzhi = SharedPreferencesUtil.getInstance(mContext).getSP("tingzhi", "ONF");

        sensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));


        btnForward.setOnTouchListener(this.touchListener);
        btnBack.setOnTouchListener(this.touchListener);
        btnTurnLeft.setOnTouchListener(this.touchListener);
        btnTurnRight.setOnTouchListener(this.touchListener);
        btnStop.setOnTouchListener(this.touchListener);

    }

    /**
     * 传感器监听
     *
     * @param sensorEvent
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float[] weizhi;
        weizhi = sensorEvent.values;
        int j = -1;
        float f3 = -weizhi[0];
        float f1 = -weizhi[1];
        float f2 = -weizhi[2];
        if (4.0F * (f3 * f3 + f1 * f1) >= f2 * f2) {
            int i;
            for (j = 90 - Math.round((float) Math.atan2(-f1, f3) * 57.29578F); ; j -= 360) {
                i = j;
                if (j < 360) {
                    break;
                }
            }
            for (; ; ) {
                j = i;
                if (i >= 0) {
                    break;
                }
                i += 360;
            }
        }
        if (this.chkGravitySensor.isChecked()) {
            Log.d(TAG, "onSensorChanged: 第1个数：" + weizhi[0] + "第2个数：" + weizhi[1] + "第3个数：" + weizhi[2]);
//            paramSensorEvent = new Message();
//            paramSensorEvent.what = 888;
//            paramSensorEvent.obj = Integer.valueOf(j);
//            this.mHandler.sendMessage(paramSensorEvent);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

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
                        mBluetoothOp.write(qianjin.getBytes());
                        break;
                    case R.id.btn_Back:
                        mBluetoothOp.write(houtui.getBytes());
                        break;
                    case R.id.btn_TurnLeft:
                        mBluetoothOp.write(zuozhuan.getBytes());
                        break;
                    case R.id.btn_TurnRight:
                        mBluetoothOp.write(youzhuan.getBytes());
                        break;
                    case R.id.btn_Stop:
                        mBluetoothOp.write(tingzhi.getBytes());
                        break;
                }
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP || motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
                Log.d(TAG, "onTouch: UP");
                //弹起或者取消事件
                mBluetoothOp.write(tingzhi.getBytes());
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
    private void showList() {
        devices = mBluetoothAdapter.getBondedDevices();
        if (devices.size() > 0) {
            blueToothDeviceName = new String[devices.size()];
            blueToothDeviceMAC = new String[devices.size()];
            int i = 0;
            for (Iterator<BluetoothDevice> iterator = devices.iterator(); iterator.hasNext(); ) {
                BluetoothDevice bluetoothDevice = (BluetoothDevice) iterator.next();
                Log.d(TAG, "onCreate: " + bluetoothDevice.getName() + " " + bluetoothDevice.getAddress());
                blueToothDeviceName[i] = bluetoothDevice.getName();
                blueToothDeviceMAC[i] = bluetoothDevice.getAddress();
                i++;
            }
            builder = new AlertDialog.Builder(this).setIcon(R.mipmap.ic_icon_foreground)
                    .setTitle("已连接过的蓝牙ble")
                    .setItems(blueToothDeviceName, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(MainActivity.this, "你点击的内容为： " + blueToothDeviceMAC[i], Toast.LENGTH_LONG).show();
                            mBluetoothOp.connect(blueToothDeviceMAC[i]);
                        }
                    });
            builder.create().show();
        } else {
            Toast.makeText(this, "你没有保存蓝牙好么", Toast.LENGTH_SHORT).show();
        }

    }


    /**
     * 输出设置列表列表 dialog
     */
    private void showSettingList() {
        final String[] setting = {"设置前进按钮", "设置后退按钮", "设置左转按钮", "设置右转按钮", "设置停止按钮"};
        builder = new AlertDialog.Builder(this).setIcon(R.mipmap.ic_icon_foreground)
                .setTitle("设置指令")
                .setItems(setting, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        showInput(i + 1);
                    }
                });
        builder.create().show();
    }

    /**
     * 一个输入框的 dialog
     */
    private void showInput(int type) {
        final EditText editText = new EditText(this);
        switch (type) {
            case 1:
                builder = new AlertDialog.Builder(this).setTitle("设置前进指令").setView(editText)
                        .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (editText.getText().toString().equals("")) {
                                    Toast.makeText(MainActivity.this, "你输入的值为空", Toast.LENGTH_SHORT).show();
                                } else {
                                    qianjin = editText.getText().toString();
                                    SharedPreferencesUtil.getInstance(mContext).putSP("qianjin", editText.getText().toString());
                                }
                            }
                        });
                builder.create().show();
                break;
            case 2:
                builder = new AlertDialog.Builder(this).setTitle("设置后退指令").setView(editText)
                        .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (editText.getText().toString().equals("")) {
                                    Toast.makeText(MainActivity.this, "你输入的值为空", Toast.LENGTH_SHORT).show();
                                } else {
                                    houtui = editText.getText().toString();
                                    SharedPreferencesUtil.getInstance(mContext).putSP("houtui", editText.getText().toString());
                                }
                            }
                        });
                builder.create().show();
                break;
            case 3:
                builder = new AlertDialog.Builder(this).setTitle("设置左转指令").setView(editText)
                        .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (editText.getText().toString().equals("")) {
                                    Toast.makeText(MainActivity.this, "你输入的值为空", Toast.LENGTH_SHORT).show();
                                } else {
                                    zuozhuan = editText.getText().toString();
                                    SharedPreferencesUtil.getInstance(mContext).putSP("zuozhuan", editText.getText().toString());
                                }
                            }
                        });
                builder.create().show();
                break;
            case 4:
                builder = new AlertDialog.Builder(this).setTitle("设置右转指令").setView(editText)
                        .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (editText.getText().toString().equals("")) {
                                    Toast.makeText(MainActivity.this, "你输入的值为空", Toast.LENGTH_SHORT).show();
                                } else {
                                    youzhuan = editText.getText().toString();
                                    SharedPreferencesUtil.getInstance(mContext).putSP("youzhuan", editText.getText().toString());
                                }
                            }
                        });
                builder.create().show();
                break;
            case 5:
                builder = new AlertDialog.Builder(this).setTitle("设置停止指令").setView(editText)
                        .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (editText.getText().toString().equals("")) {
                                    Toast.makeText(MainActivity.this, "你输入的值为空", Toast.LENGTH_SHORT).show();
                                    tingzhi = "ONF";
                                } else {
                                    tingzhi = editText.getText().toString();
                                    SharedPreferencesUtil.getInstance(mContext).putSP("tingzhi", editText.getText().toString());
                                }
                            }
                        });
                builder.create().show();
                break;
        }

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
        this.sensorManager.registerListener(this, this.sensorManager.getDefaultSensor(1), 1);
    }

    //退出当前Activity或者跳转到新Activity时被调用
    public void onStop() {
        super.onStop();
        this.sensorManager.unregisterListener(this);
    }


    @OnClick({R.id.btn_Connect, R.id.btn_Left, R.id.btn_Center, R.id.btn_Right, R.id.btn_LeftRotate, R.id.btn_Outfire, R.id.btn_Accelerate, R.id.btn_RightRotate, R.id.btn_Whistle, R.id.btn_Moderate, R.id.btn_setting})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_Connect:
                showList();
                break;
            case R.id.btn_Left:
                Toast.makeText(mContext, "111", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_Center:
                break;
            case R.id.btn_Right:
                break;
            case R.id.btn_LeftRotate:
                break;
            case R.id.btn_Outfire:
                break;
            case R.id.btn_Accelerate:
                break;
            case R.id.btn_RightRotate:
                break;
            case R.id.btn_Whistle:
                break;
            case R.id.btn_Moderate:
                break;
            case R.id.btn_setting:
                showSettingList();
                break;
            default:
                break;
        }
    }
}
