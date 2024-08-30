package com.example.eyekeep.bluetooth;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.eyekeep.service.SendEmergencySituation;

import java.util.UUID;

public class BLEService extends Service {
    private BluetoothGatt bluetoothGatt;
    private final SendEmergencySituation sendEmergencySituation = new SendEmergencySituation();

    private static final String CHANNEL_ID = "BLEServiceChannel";

    private final UUID READ_ONLY_UUID = UUID.fromString("00002a01-0000-1000-8000-00805f9b34fb");

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(1, getNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        BluetoothDevice device = intent.getParcelableExtra("BLE_DEVICE");
        if (device != null) {
            connectToBLEDevice(device);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bluetoothGatt != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification getNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("BLE Service")
                .setContentText("BLE 연결 유지 중")
                //.setSmallIcon(R.drawable.ic_bluetooth_connected)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "BLE Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    private void connectToBLEDevice(BluetoothDevice device) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        bluetoothGatt = device.connectGatt(this, false, new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    if (ActivityCompat.checkSelfPermission(BLEService.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    Log.i("BLEService", "Connected to BLE device: " + device.getName());
                    gatt.discoverServices();
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.e("BLEService", "Disconnected from BLE device: " + device.getName());
                    stopSelf(); // 연결이 끊어지면 서비스도 중지
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.i("BluetoothParingActivity", "서비스 검색 성공. 발견된 서비스와 특성을 나열합니다:");

                    for (BluetoothGattService service : gatt.getServices()) {
                        UUID serviceUUID = service.getUuid();
                        Log.i("BluetoothParingActivity", "서비스 UUID: " + serviceUUID.toString());

                        for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                            UUID characteristicUUID = characteristic.getUuid();
                            Log.i("BluetoothParingActivity", "  └ 특성 UUID: " + characteristicUUID.toString());

                            // 특성의 속성을 확인하여 읽기, 쓰기, 알림 가능 여부를 출력
                            int properties = characteristic.getProperties();
                            StringBuilder propertiesString = new StringBuilder("    └ 속성: ");
                            if ((properties & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                                propertiesString.append("읽기 ");
                            }
                            if ((properties & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                                propertiesString.append("쓰기 ");
                            }
                            if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                                propertiesString.append("알림 ");
                                setCharacteristicNotification(gatt, characteristic, true); // 알림 활성화
                            }
                            if ((properties & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
                                propertiesString.append("지시 ");
                                setCharacteristicNotification(gatt, characteristic, true); // 지시 활성화
                            }
                            Log.i("BluetoothParingActivity", propertiesString.toString());
                        }
                    }
                } else {
                    Log.e("BluetoothParingActivity", "서비스 검색 실패. 상태 코드: " + status);
                }
            }

            // 특성에 대한 알림을 활성화하는 메서드
            private void setCharacteristicNotification(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, boolean enabled) {
                if (ActivityCompat.checkSelfPermission(BLEService.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                gatt.setCharacteristicNotification(characteristic, enabled);
                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(READ_ONLY_UUID);
                if (descriptor != null) {
                    descriptor.setValue(enabled ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                    gatt.writeDescriptor(descriptor);
                }
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                // 알림을 통해 데이터 수신
                byte[] data = characteristic.getValue();
                String receivedMessage = new String(data);
                Log.i("BluetoothParingActivity", "수신된 메시지: " + receivedMessage);
                if (receivedMessage.equals("Q")) {
                    sendEmergencySituation.sendEmergencySituation();
                }
            }
        });
    }
}