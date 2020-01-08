package cn.edu.caztc.bluetoothcar.util;

public class BluetoothOperationCallback {
    public void onConnect(int err, String desc) {}
    public void onDataRecv(int err, String desc, byte[] data, int len) {}
}
