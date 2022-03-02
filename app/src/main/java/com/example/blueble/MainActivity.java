package com.example.blueble;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private BluetoothDevice bluetoothDevice;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattService bluetoothGattService;
    private BluetoothGattCharacteristic bluetoothGattCharacteristic;
    //bluetoothDevice是dervices中选中的一项 bluetoothDevice=dervices.get(i);
    private List<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();//存放扫描结果
    private Handler handler = new Handler();//import android.os.Handler;
    private static final String TAG = "leo";
    private Spinner spin_devPath = null;
    String str1 = "HC-08";
    String str2;
    TextView view1;
    CheckBox check1,check2,check3,check4,check5;
    byte mode = 1;
    int set1=0,set2=0,set3=0;
    byte[] senddatas;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //获取buletoothAdapter并打开蓝牙
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 0);  // 弹对话框的形式提示用户开启蓝牙
        }

        //动态开启位置权限
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)

                == PackageManager.PERMISSION_GRANTED){
            Toast.makeText(MainActivity.this,"搜索回调权限已开启", Toast.LENGTH_SHORT).show();

        }else{
            Toast.makeText(MainActivity.this,"搜索回调权限未开启",Toast.LENGTH_SHORT).show();

            ActivityCompat.requestPermissions(MainActivity.this,

                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        }

        spin_devPath = (Spinner)findViewById(R.id.spin_one);
        String[] arr = { "模式一", "模式二", "模式三",
                "模式四", "模式五"};
        // 创建ArrayAdapter对象
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, arr);
        // 为Spinner设置Adapter
        spin_devPath.setAdapter(adapter);
        spin_devPath.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String content = parent.getItemAtPosition(position).toString();
                Toast.makeText(MainActivity.this, "选择的模式是：" + content,
                        Toast.LENGTH_SHORT).show();
                if(content.equals("模式一"))
                    mode = 1;
                else if(content.equals("模式二"))
                    mode = 2;
                else if(content.equals("模式三"))
                    mode = 3;
                else if(content.equals("模式四"))
                    mode = 4;
                else if(content.equals("模式五"))
                    mode = 5;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        view1 = findViewById(R.id.view1);
        senddatas =new byte[5];
        senddatas[0]=0;
        senddatas[1]=0;
        senddatas[2]=0;
        senddatas[3]=0;
        senddatas[4]=0;
        check1 = findViewById(R.id.check1);
        check1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if(isChecked)
                    senddatas[1] = 1;
                else
                    senddatas[1] = 0;
            }
        });
        check2 = findViewById(R.id.check2);
        check2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if(isChecked)
                    senddatas[2] = 1;
                else
                    senddatas[2] = 0;
            }
        });
        check3 = findViewById(R.id.check3);
        check3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if(isChecked)
                    senddatas[3] = 1;
                else
                    senddatas[3] = 0;
            }
        });
        check4 = findViewById(R.id.check4);
        check4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if(isChecked)
                    senddatas[4] = 1;
                else
                    senddatas[4] = 0;
            }
        });


        //注册按钮
        Button btn1 = findViewById(R.id.btn1);
        //注册按钮点击事件并定义回调函数
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view1.setText("连接中。。。");
                Log.e(TAG, "onClick: ");
                handler.postDelayed(runnable, 10000);
                bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
                bluetoothLeScanner.startScan(scanCallback);//android5.0把扫描方法单独弄成一个对象了（alt+enter添加），扫描结果储存在devices数组中。最好在startScan()前调用stopScan()。

            }
        });
        Button btn3 = findViewById(R.id.btn3);
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                senddatas[0] = mode;
                bluetoothGattCharacteristic.setValue(senddatas);
                bluetoothGatt.writeCharacteristic(bluetoothGattCharacteristic);
            }
        });

    }

    //startScan()回调函数
    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult results) {
            Log.e(TAG, "回调");
            super.onScanResult(callbackType, results);
            BluetoothDevice device = results.getDevice();
            if (!devices.contains(device)) {  //判断是否已经添加
                devices.add(device);//也可以添加devices.getName()到列表，这里省略            }
                str2 = device.getName();
                if(str1.equals(str2))
                {
                    Log.e(TAG, "This is "+device.getName());
                    bluetoothDevice = device;
                }
                // callbackType：回调类型
                // result：扫描的结果，不包括传统蓝牙        }
            }
        }
    };

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            bluetoothLeScanner.stopScan(scanCallback);
            Log.e(TAG, "扫描结束");
            //选择bluetoothDevice后配置回调函数
            bluetoothGatt=bluetoothDevice.connectGatt(MainActivity.this, false, new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState){
                    super.onConnectionStateChange(gatt, status, newState);
                    if (newState == BluetoothProfile.STATE_CONNECTED) {//状态变为 已连接
                        Log.e(TAG, "成功建立连接");
                    }
                    bluetoothGatt.discoverServices();//连接成功，开始搜索服务，一定要调用此方法，否则获取不到服务
                    if (newState == BluetoothGatt.STATE_DISCONNECTED) { //状态变为 未连接
                        Toast.makeText(MainActivity.this, "连接断开", Toast.LENGTH_LONG).show();
                    }
                    return;
                }
                public void onServicesDiscovered(BluetoothGatt gatt, final int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {   //找到服务了
                        Log.e(TAG, "找到服务了");
                        //用此函数接收数据
                        super.onServicesDiscovered(gatt, status);
                        String service_UUID = "0000ffe0-0000-1000-8000-00805f9b34fb";//已知服务
                        String characteristic_UUID = "0000ffe1-0000-1000-8000-00805f9b34fb";//已知特征
                        //List<BluetoothGattService> gattServices = bluetoothGatt.getServices();
                        bluetoothGattService = bluetoothGatt.getService(UUID.fromString(service_UUID));//通过UUID找到服务
                        if(bluetoothGattService==null)
                            Log.e(TAG, "kkkkkkkkkkkkkkkkkkkk");
                        //List<BluetoothGattCharacteristic> gattCharacteristics = bluetoothGattService.getCharacteristics();
                        bluetoothGattCharacteristic = bluetoothGattService.getCharacteristic(UUID.fromString(characteristic_UUID));//找到服务后在通过UUID找到特征
                        if (bluetoothGattCharacteristic != null) {
                            gatt.setCharacteristicNotification(bluetoothGattCharacteristic, true);//启用onCharacteristicChanged(），用于接收数据
                            Log.e(TAG, "连接成功");
                            view1.setText("已连接");
                            //Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_LONG).show();
                        }
                        else {
                            Log.e(TAG, "发现服务失败");
                            //Toast.makeText(MainActivity.this, "发现服务失败", Toast.LENGTH_LONG).show();
                            return;
                        }
                    } else {
                        Log.e(TAG, "没找到服务");
                    }

                }
                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                    super.onCharacteristicChanged(gatt, characteristic);
                    //发现服务后的响应函数
                    byte[] bytesreceive = characteristic.getValue();
                    //Log.e(TAG, String.valueOf(bytesreceive[0]));
                    Log.e(TAG,bytesreceive[0]+" "+bytesreceive[1]+" "+bytesreceive[2]+" "+bytesreceive[4]);
                }
            });

        }
    };


}