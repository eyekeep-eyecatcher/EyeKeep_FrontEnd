package com.example.eyekeep.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.eyekeep.MainChildActivity;
import com.example.eyekeep.R;
import com.example.eyekeep.service.SendEmergencySituation;

import java.util.ArrayList;
import java.util.UUID;

public class BluetoothParingActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> newDevicesArrayAdapter;
    private ArrayList<BluetoothDevice> newDevicesList = new ArrayList<>();
    private ListView newDevicesListView;
    private Handler mainThreadHandler = new Handler();
    private BluetoothGatt bluetoothGatt;
    private final SendEmergencySituation sendEmergencySituation = new SendEmergencySituation();

    private final UUID READ_ONLY_UUID = UUID.fromString("00002a01-0000-1000-8000-00805f9b34fb");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_paring);



        // BluetoothTestActivity로 이동하는 버튼 클릭 리스너 설정
        AppCompatButton testActivityButton = findViewById(R.id.button_test_activity);
        AppCompatButton scanButton = findViewById(R.id.button_scan);
        AppCompatButton disconnectButton = findViewById(R.id.btn_disconnect);

        testActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BluetoothParingActivity.this, MainChildActivity.class);
                startActivity(intent);
            }
        });

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doDiscovery();
            }
        });

        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnectFromBLEDevice();
            }
        });


        // 블루투스 어댑터 초기화
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "블루투스가 활성화되지 않았거나 지원되지 않습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // ListView 및 ArrayAdapter 초기화
        newDevicesArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        newDevicesListView = findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(newDevicesArrayAdapter);

        // 새로 발견된 장치 클릭 리스너
        newDevicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
                if (ActivityCompat.checkSelfPermission(BluetoothParingActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                bluetoothAdapter.cancelDiscovery();
                // 선택한 장치의 정보를 가져와 BLE 연결 시도
                BluetoothDevice device = newDevicesList.get(arg2);
                connectToBLEDevice(device);
            }
        });

        // 블루투스 장치 발견을 위한 BroadcastReceiver 등록
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);

        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver, filter);

        // 위치 권한 확인 및 요청 (Android 12 이상)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, 1);
            }
        }
        // Disconnect 버튼 클릭 리스너 설정
    }

    private void disconnectFromBLEDevice() {
        if (bluetoothGatt != null) {
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    bluetoothGatt.disconnect(); // BLE 연결 해제
                    bluetoothGatt.close(); // GATT 객체 닫기
                    bluetoothGatt = null;
                    showMessage(this, "BLE 연결이 해제되었습니다.");
                    Log.i("BluetoothParingActivity", "BLE 연결이 성공적으로 해제되었습니다.");
                } else {
                    Log.w("BluetoothParingActivity", "BLUETOOTH_CONNECT 권한이 없습니다. BluetoothGatt를 해제할 수 없습니다.");
                }
            } catch (SecurityException e) {
                Log.e("BluetoothParingActivity", "BluetoothGatt 해제 중 SecurityException 발생", e);
            }
        } else {
            showMessage(this, "연결된 BLE 장치가 없습니다.");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 부여되었을 경우 블루투스 기능 초기화
                doDiscovery();
            } else {
                // 권한이 거부되었을 경우
                Toast.makeText(this, "Bluetooth 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // BroadcastReceiver 해제
        unregisterReceiver(receiver);

        // BluetoothGatt 해제
        if (bluetoothGatt != null) {
            try {
                // 권한 확인
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    bluetoothGatt.close();
                } else {
                    Log.w("BluetoothParingActivity", "BLUETOOTH_CONNECT 권한이 없습니다. BluetoothGatt를 닫을 수 없습니다.");
                }
            } catch (SecurityException e) {
                Log.e("BluetoothParingActivity", "BluetoothGatt를 닫는 중 SecurityException 발생", e);
            } finally {
                bluetoothGatt = null;
            }
        }
    }

    private void doDiscovery() {
        setTitle("장치 검색 중...");
        Log.d("BluetoothParingActivity", "Bluetooth 검색 시작됨");
        newDevicesArrayAdapter.clear();
        newDevicesList.clear();

        // 블루투스 활성화 상태 확인
        if (!bluetoothAdapter.isEnabled()) {
            showMessage(this, "블루투스가 활성화되어 있지 않습니다.");
            Log.d("BluetoothParingActivity", "Bluetooth 비활성화 상태");
            return;
        }

        // 권한 확인
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.d("BluetoothParingActivity", "Bluetooth 권한이 부족합니다.");
            showMessage(this, "Bluetooth 권한이 부족합니다.");
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, 1);
            return;
        }

        // 이미 검색 중인지 확인
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        // 검색 시작
        boolean isDiscovering = bluetoothAdapter.startDiscovery();
        Log.d("BluetoothParingActivity", "Bluetooth 검색 상태: " + isDiscovering);
        if (!isDiscovering) {
            showMessage(this, "기기 검색 시작 실패");
        } else {
            showMessage(this, "기기 검색을 시작했습니다.");
        }
    }

    // showMessage 메서드를 추가하여 메시지를 쉽게 출력
    private void showMessage(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    private void connectToBLEDevice(BluetoothDevice device) {
        Intent serviceIntent = new Intent(this, BLEService.class);
        serviceIntent.putExtra("BLE_DEVICE", device);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

//    private void connectToBLEDevice(BluetoothDevice device) {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
//
//        // BLE 연결 시도
//        bluetoothGatt = device.connectGatt(this, false, new BluetoothGattCallback() {
//
//            @Override
//            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
//                if (newState == BluetoothProfile.STATE_CONNECTED) {
//                    if (ActivityCompat.checkSelfPermission(BluetoothParingActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                        return;
//                    }
//                    Log.i("BluetoothParingActivity", "Connected to BLE device: " + device.getName());
//
//                    // 서비스 검색 시작
//                    boolean serviceDiscoveryStarted = gatt.discoverServices();
//                    if (serviceDiscoveryStarted) {
//                        Log.i("BluetoothParingActivity", "서비스 검색을 시작했습니다.");
//                    } else {
//                        Log.e("BluetoothParingActivity", "서비스 검색을 시작하지 못했습니다.");
//                    }
//                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
//                    Log.e("BluetoothParingActivity", "Disconnected from BLE device: " + device.getName() +
//                            " | Status: " + status + " | State: " + newState);
//                    runOnUiThread(() -> Toast.makeText(BluetoothParingActivity.this, "BLE 장치와의 연결이 해제되었습니다.", Toast.LENGTH_SHORT).show());
//                }
//            }
//
//            @Override
//            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
//                if (status == BluetoothGatt.GATT_SUCCESS) {
//                    Log.i("BluetoothParingActivity", "서비스 검색 성공. 발견된 서비스와 특성을 나열합니다:");
//
//                    for (BluetoothGattService service : gatt.getServices()) {
//                        UUID serviceUUID = service.getUuid();
//                        Log.i("BluetoothParingActivity", "서비스 UUID: " + serviceUUID.toString());
//
//                        for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
//                            UUID characteristicUUID = characteristic.getUuid();
//                            Log.i("BluetoothParingActivity", "  └ 특성 UUID: " + characteristicUUID.toString());
//
//                            // 특성의 속성을 확인하여 읽기, 쓰기, 알림 가능 여부를 출력
//                            int properties = characteristic.getProperties();
//                            StringBuilder propertiesString = new StringBuilder("    └ 속성: ");
//                            if ((properties & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
//                                propertiesString.append("읽기 ");
//                            }
//                            if ((properties & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
//                                propertiesString.append("쓰기 ");
//                            }
//                            if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
//                                propertiesString.append("알림 ");
//                                setCharacteristicNotification(gatt, characteristic, true); // 알림 활성화
//                            }
//                            if ((properties & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
//                                propertiesString.append("지시 ");
//                                setCharacteristicNotification(gatt, characteristic, true); // 지시 활성화
//                            }
//                            Log.i("BluetoothParingActivity", propertiesString.toString());
//                        }
//                    }
//                } else {
//                    Log.e("BluetoothParingActivity", "서비스 검색 실패. 상태 코드: " + status);
//                }
//            }
//
//            // 특성에 대한 알림을 활성화하는 메서드
//            private void setCharacteristicNotification(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, boolean enabled) {
//                if (ActivityCompat.checkSelfPermission(BluetoothParingActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                    return;
//                }
//                gatt.setCharacteristicNotification(characteristic, enabled);
//                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(READ_ONLY_UUID);
//                if (descriptor != null) {
//                    descriptor.setValue(enabled ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
//                    gatt.writeDescriptor(descriptor);
//                }
//            }
//
//            @Override
//            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
//                // 알림을 통해 데이터 수신
//                byte[] data = characteristic.getValue();
//                String receivedMessage = new String(data);
//                Log.i("BluetoothParingActivity", "수신된 메시지: " + receivedMessage);
//                if (receivedMessage.equals("Q")) {
//                    sendEmergencySituation.sendEmergencySituation();
//                }
//
//            }
//
//        });
//
//
//
//
//    // 연결 대기 시간을 연장 (예: 20초)
//        new Handler().postDelayed(() -> {
//            if (bluetoothGatt != null) {
//                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                    return;
//                }
//                BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
//                if (bluetoothManager != null && bluetoothManager.getConnectionState(device, BluetoothProfile.GATT) == BluetoothProfile.STATE_DISCONNECTED) {
//                    Log.d("BluetoothPairingActivity", "Connection timeout, disconnecting...");
//                    bluetoothGatt.close();
//                    bluetoothGatt = null;
//                    Toast.makeText(this, "연결 시간이 초과되었습니다.", Toast.LENGTH_SHORT).show();
//                }
//            }
//        }, 60000); // 60초 대기
//    }


    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("BluetoothParingActivity", "BroadcastReceiver 동작: " + action);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (ActivityCompat.checkSelfPermission(BluetoothParingActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                Log.d("BluetoothParingActivity", "기기 발견됨: " + device.getName());
                if (device != null) {
                    if (!newDevicesList.contains(device)) {
                        newDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                        newDevicesList.add(device);
                    }
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setTitle("검색 완료");
                Log.d("BluetoothParingActivity", "Bluetooth 검색 완료");
                if (newDevicesArrayAdapter.getCount() == 0) {
                    newDevicesArrayAdapter.add("장치를 찾을 수 없습니다.");
                }
                showMessage(context, "기기 검색이 완료되었습니다.");
            }
        }
    };
}