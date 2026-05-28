# MKLoRa LW009-SM Pro Android SDK

Native Android SDK and demo app for LW009-SM Pro devices. Supports BLE scanning, connection, protocol parameter read/write, device-initiated disconnect notifications, parking detection configuration, LoRaWAN settings, beacon filter rules, device log export, Nordic DFU firmware updates, and slave-module firmware upgrades.

Cross-platform reference (same protocol): [MKLoRa-LW009-SM-Flutter](https://github.com/MKLoRa/MKLoRa-LW009-SM-Flutter).

---

## Requirements

| Item | Description |
|------|-------------|
| Android Studio | 3.6+ (8.x recommended) |
| minSdk | 28 |
| compileSdk | 35 |
| Device | Physical device required (emulators do not support BLE) |

---

## Project Structure

```
LW009_SM_Pro_Android/
├── lw009/               # Demo app (scan, connect, configure, DFU, full UI)
├── mokosupport/         # BLE SDK module (primary integration dependency)
│   ├── MoKoSupport.java           # Connect, send commands, event callbacks
│   ├── MokoBleScanner.java        # Scanning
│   ├── OrderTaskAssembler.java    # Read/write task assembly (API entry)
│   └── entity/ParamsKeyEnum.java  # Protocol parameter keys
```

Communication has three stages: **scan → connect → command exchange**. The SDK reports connection status and command results via **EventBus** (you can switch to another bus in `MoKoSupport`).

---

## Integrating the SDK

### 1. Add the module

Copy `mokosupport` into your project root and add to `settings.gradle`:

```gradle
include ':lw009', ':mokosupport'
```

In the app module `build.gradle`:

```gradle
dependencies {
    implementation project(path: ':mokosupport')
}
```

### 2. Initialize

Initialize in `Application.onCreate()` or your first Activity:

```java
MoKoSupport.getInstance().init(getApplicationContext());
```

### 3. Permissions

`mokosupport` declares base BLE permissions in its `AndroidManifest.xml`. On Android 6.0+, scanning requires **runtime location permission**; on Android 12+, also request `BLUETOOTH_SCAN` and `BLUETOOTH_CONNECT`.

```java
// Example: request location (required for scanning)
if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {
    ActivityCompat.requestPermissions(this,
            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
            REQUEST_CODE_LOCATION);
}
```

### 4. Register EventBus

Connection status, command results, and Notify data are delivered via EventBus. Register in your Activity/Fragment:

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EventBus.getDefault().register(this);
}

@Override
protected void onDestroy() {
    EventBus.getDefault().unregister(this);
    super.onDestroy();
}
```

---

## 1. Scanning for Devices

### Core classes

| Class | Description |
|-------|-------------|
| `MokoBleScanner` | Start/stop scanning |
| `MokoScanDeviceCallback` | Scan started, per-device callback, scan stopped |
| `DeviceInfoParseable` | Advertisement parser interface; demo impl: `AdvInfoAnalysisImpl` |

Scan filters by Service Data UUID: `0000aa13-...` (`OrderServices.SERVICE_ADV`).

### Code example

```java
MokoBleScanner scanner = new MokoBleScanner();
AdvInfoAnalysisImpl parser = new AdvInfoAnalysisImpl();

scanner.startScanDevice(new MokoScanDeviceCallback() {
    @Override
    public void onStartScan() {
        // Clear list, refresh UI
    }

    @Override
    public void onScanDevice(DeviceInfo deviceInfo) {
        AdvInfo adv = parser.parseDeviceInfo(deviceInfo);
        if (adv == null) return;
        // adv.mac / adv.name / adv.rssi
        // adv.verifyEnable (password required?)
        // adv.lowPower / adv.connectable
    }

    @Override
    public void onStopScan() {
        // Stop animation, etc.
    }
});

// Stop scanning (call before connecting)
scanner.stopScanDevice();
```

### Advertisement fields (`AdvInfoAnalysisImpl`)

Parsed from Service Data UUID `0000aa13`:

- `verifyEnable` — connection password enabled (`true` → call `setPassword` after connect)
- `lowPower` — low-power indicator
- `mac`, `name`, `rssi`, `connectable`, `txPower`

---

## 2. Connecting to a Device

### Connect

Only the device **MAC address** is required (from scan result `adv.mac`):

```java
// Stop scanning before connecting
scanner.stopScanDevice();
MoKoSupport.getInstance().connDevice(mac);
```

### Connection status (EventBus)

```java
@Subscribe(threadMode = ThreadMode.MAIN)
public void onConnectStatusEvent(ConnectStatusEvent event) {
    String action = event.getAction();
    if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
        // GATT disconnected (failed connect, link lost, manual disconnect, etc.)
    }
    if (MokoConstants.ACTION_DISCOVER_SUCCESS.equals(action)) {
        // Service discovery done; commands can be sent
    }
}
```

### Password verification

If advertisement has `verifyEnable == true`, send the password after service discovery:

```java
if (MokoConstants.ACTION_DISCOVER_SUCCESS.equals(action)) {
    MoKoSupport.getInstance().sendOrder(OrderTaskAssembler.setPassword("123456"));
}
```

Parse the result in `ACTION_ORDER_RESULT` for `OrderCHAR.CHAR_PASSWORD`: `value[4] == 1` success, `0` failure — then call `disConnectBle()`.

Devices without password can proceed to your UI right after `ACTION_DISCOVER_SUCCESS`.

### Manual disconnect

```java
MoKoSupport.getInstance().disConnectBle();
```

---

## 3. Reading and Writing Parameters

### Task queue

All reads/writes are wrapped as `OrderTask`, created by `OrderTaskAssembler`, and sent via `sendOrder` **in queue order**. Default timeout per task is 3 seconds.

```java
// Single task
MoKoSupport.getInstance().sendOrder(OrderTaskAssembler.getLoraRegion());

// Multiple tasks (executed in order)
List<OrderTask> tasks = new ArrayList<>();
tasks.add(OrderTaskAssembler.getTimeZone());
tasks.add(OrderTaskAssembler.getAdvName());
MoKoSupport.getInstance().sendOrder(tasks.toArray(new OrderTask[]{}));
```

See `OrderTaskAssembler.java` for the full list of `getXxx` / `setXxx` methods (device info, LoRa, parking detection, beacon filters, battery, etc.).

### Protocol frame format

Parameter channel (`CHAR_PARAMS`) frame layout:

```
ED [flag] [cmd] [len] [data...]
```

| Field | Description |
|-------|-------------|
| `0xED` | Frame header |
| `flag` | `0x00` read, `0x01` write |
| `cmd` | 1 byte, maps to `ParamsKeyEnum` (e.g. `KEY_LORA_REGION` = `0x91`) |
| `len` | Payload length |
| `data` | Payload; for write ACK, `data[0] == 1` means success |

Some long read responses (e.g. `KEY_FILTER_NAME_RULES`) use a multi-packet header `0xEE`; the SDK reassembles them in `MoKoSupport.orderResponseValid`.

### Command results (EventBus)

```java
@Subscribe(threadMode = ThreadMode.MAIN)
public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
    String action = event.getAction();
    OrderTaskResponse response = event.getResponse();

    if (MokoConstants.ACTION_ORDER_TIMEOUT.equals(action)) {
        // Timeout; check response.orderCHAR for which task
    }
    if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
        // All queued tasks finished
    }
    if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
        OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
        byte[] value = response.responseValue;
        // Parse value ...
    }
    if (MokoConstants.ACTION_CURRENT_DATA.equals(action)) {
        // Device-initiated Notify (disconnect, log data, slave notify, etc.)
    }
}
```

### Parsing read responses (generic template)

```java
if (MokoConstants.ACTION_ORDER_RESULT.equals(action)
        && (OrderCHAR) response.orderCHAR == OrderCHAR.CHAR_PARAMS) {
    byte[] value = response.responseValue;
    if (value.length < 4) return;

    int header = value[0] & 0xFF;   // 0xED
    int flag = value[1] & 0xFF;     // 0x00 = read
    int cmd = value[2] & 0xFF;
    if (header != 0xED) return;

    ParamsKeyEnum key = ParamsKeyEnum.fromParamKey(cmd);
    int length = value[3] & 0xFF;
    if (flag == 0x00 && key != null && length > 0) {
        byte[] payload = Arrays.copyOfRange(value, 4, 4 + length);
        switch (key) {
            case KEY_LORA_REGION:
                int region = payload[0] & 0xFF;
                break;
            case KEY_LORA_MODE:
                int mode = payload[0] & 0xFF; // 1=ABP, 2=OTAA
                break;
            case KEY_ADV_NAME:
                String name = new String(payload);
                break;
            case KEY_PARKING_DETECTION_MODE:
                int parkingMode = payload[0] & 0xFF;
                break;
            // ...
        }
    }
}
```

### Parsing write responses

```java
if (flag == 0x01 && key == ParamsKeyEnum.KEY_TIME_ZONE) {
    int result = value[4] & 0xFF;  // 1 = success
}
```

### Example 1: Read LoRa region

```java
MoKoSupport.getInstance().sendOrder(OrderTaskAssembler.getLoraRegion());

// In callback for KEY_LORA_REGION: region 0~12 maps to AS923, AU915, EU868, etc. (see LoRaConnSettingActivity)
```

### Example 2: Write time zone (UTC+8)

```java
MoKoSupport.getInstance().sendOrder(OrderTaskAssembler.setTimeZone(8));
```

Time zone enum range is `-24` ~ `28` (half-hour step encoding; same as demo `GeneralFragment`).

### Example 3: Read/write LoRa mode (ABP / OTAA)

```java
// Read
MoKoSupport.getInstance().sendOrder(OrderTaskAssembler.getLoraUploadMode());

// Write: 1=ABP, 2=OTAA
MoKoSupport.getInstance().sendOrder(OrderTaskAssembler.setLoraUploadMode(2));
```

### Example 4: Change advertisement name

```java
MoKoSupport.getInstance().sendOrder(
        OrderTaskAssembler.setAdvName("LW009-001"));
```

### Example 5: Sync UTC time

```java
MoKoSupport.getInstance().sendOrder(OrderTaskAssembler.setTime());
```

### Example 6: Batch read device info (GATT + protocol)

```java
List<OrderTask> tasks = new ArrayList<>();
tasks.add(OrderTaskAssembler.getDeviceModel());      // GATT 0x2A24
tasks.add(OrderTaskAssembler.getSoftwareVersion());  // GATT 0x2A28
tasks.add(OrderTaskAssembler.getFirmwareVersion());  // GATT 0x2A26
tasks.add(OrderTaskAssembler.getBattery());          // protocol KEY_BATTERY_POWER
tasks.add(OrderTaskAssembler.getMacAddress());       // protocol KEY_CHIP_MAC
MoKoSupport.getInstance().sendOrder(tasks.toArray(new OrderTask[]{}));
```

For standard GATT characteristics, branch on `OrderCHAR.CHAR_MODEL_NUMBER`, etc. in `ACTION_ORDER_RESULT` and use `new String(value)`.

Some writes require a reboot to take effect:

```java
MoKoSupport.getInstance().sendOrder(OrderTaskAssembler.restart());
```

### Example 7: Parking detection settings

```java
// Read parking detection mode
MoKoSupport.getInstance().sendOrder(OrderTaskAssembler.getParkingDetectionMode());

// Write sensitivity (1~7)
MoKoSupport.getInstance().sendOrder(OrderTaskAssembler.setParkingDetectionSensitivity(5));

// Trigger no-parking calibration
MoKoSupport.getInstance().sendOrder(OrderTaskAssembler.setNoParkingCalibration());
```

Calibration results are pushed via `CHAR_SLAVE_NOTIFY` in `ACTION_CURRENT_DATA` (`KEY_NO_PARKING_CALIBRATION_RESULT`).

---

## 4. Disconnect Notifications

Handle two kinds of disconnect events separately.

### 4.1 BLE link disconnect (`ConnectStatusEvent`)

Triggered when the device powers off, goes out of range, connection fails, or you call `disConnectBle()`:

```java
if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
    // Close config UI, return to scan page, restart startScanDevice
}
```

### 4.2 Device-initiated disconnect Notify (`CHAR_DISCONNECTED_NOTIFY`)

After connect, the SDK enables Notify on characteristic `0000AA01`. The device may push a frame before disconnecting; receive it in `ACTION_CURRENT_DATA`:

```java
if (MokoConstants.ACTION_CURRENT_DATA.equals(action)) {
    OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
    if (orderCHAR == OrderCHAR.CHAR_DISCONNECTED_NOTIFY) {
        byte[] value = response.responseValue;
        // Fixed 5 bytes: ED 02 01 01 [type]
        if (value.length == 5
                && (value[0] & 0xFF) == 0xED
                && (value[1] & 0xFF) == 0x02
                && (value[2] & 0xFF) == 0x01
                && (value[3] & 0xFF) == 0x01) {
            int type = value[4] & 0xFF;
            // 1 = password verification timeout
            // 2 = password changed successfully (reconnect required)
            // 3 = no data exchange for 3 minutes
            // 4 = reboot successful (reconnect required)
            // 5 = factory reset successful (reconnect required)
        }
    }
}
```

`ACTION_DISCONNECTED` usually follows. The demo shows a dialog in `DeviceInfoActivity` based on `type`, then `finish()` back to the scan page.

**During DFU**, ignore disconnect dialogs (demo uses `isUpgrade` flag in `SystemInfoActivity`).

---

## 5. Device Log Export

The device pushes log text via Notify on characteristic `0000AA04` (`CHAR_LOG`). Enable log Notify, accumulate data, then save to a local file:

```java
// Start receiving logs
MoKoSupport.getInstance().enableLogNotify();

// In ACTION_CURRENT_DATA for CHAR_LOG:
String log = new String(value);
// Append to StringBuilder and save when sync stops

// Stop receiving logs
MoKoSupport.getInstance().disableLogNotify();
```

See `LogDataActivity` for the full sync, file save, and export flow (entry: **System Information → Log Data**).

---

## 6. DFU Firmware Update

The demo supports two upgrade paths.

### 6.1 Main BLE chip (Nordic DFU)

UI entry: **Device → System Information → DFU**.

The demo pulls Nordic DFU via `MKLoRaUILib` or project dependencies. If you only integrate `mokosupport`, add it in your app module, for example:

```gradle
dependencies {
    implementation 'no.nordicsemi.android:dfu:2.3.0'
}
```

Use the version that matches your successful demo build (check transitive versions in `app/build/outputs/logs/manifest-merger-*-report.txt`).

Register the service in `AndroidManifest.xml`:

```xml
<service android:name="com.moko.lw009smpro.service.DfuService" />
```

`DfuService` extends `DfuBaseService` (see `lw009/.../service/DfuService.java`).

#### Flow

1. Connected and device MAC read (`OrderTaskAssembler.getMacAddress()`)
2. User selects a **`.zip`** firmware package
3. Start DFU with MAC (no need to keep the original GATT session; device reboots when done)
4. Show progress via `DfuProgressListener`
5. Return to scan page and reconnect

#### Code example

```java
// Register listener
DfuServiceListenerHelper.registerProgressListener(context, mDfuProgressListener);

// After selecting zip
DfuServiceInitiator starter = new DfuServiceInitiator(deviceMac)
        .setKeepBond(false)
        .setForeground(false)
        .disableMtuRequest()
        .setDisableNotification(true);
starter.setZip(null, firmwareFilePath);
starter.start(context, DfuService.class);

// Listener example
private final DfuProgressListener mDfuProgressListener = new DfuProgressListenerAdapter() {
    @Override
    public void onProgressChanged(String address, int percent, float speed,
            float avgSpeed, int currentPart, int partsTotal) {
        // Progress: percent%
    }

    @Override
    public void onDfuCompleted(String deviceAddress) {
        // Success — prompt user to scan and reconnect
    }

    @Override
    public void onError(String deviceAddress, int error, int errorType, String message) {
        // Upgrade failed
    }
};

@Override
protected void onDestroy() {
    DfuServiceListenerHelper.unregisterProgressListener(context, mDfuProgressListener);
    super.onDestroy();
}
```

Notes:

- Firmware must be a valid non-empty **ZIP** file
- Call `disConnectBle()` before upgrading to avoid conflicting with normal BLE traffic
- Abort DFU if `onDeviceConnecting` retries more than 3 times (see `SystemInfoActivity`)

### 6.2 Slave module firmware (protocol upgrade)

UI entry: **System Information → Self Test → DFU**.

The slave upgrade uses protocol commands (`setTriggerSlaveUpdate`, `setSlaveUpdate`) and receives progress via `CHAR_SLAVE_NOTIFY`. See `SelfTestActivity` for the full flow.

---

## 7. Typical Flow

```
Scan page (LoRaLW009MainActivity)
  ├─ MokoBleScanner.startScanDevice
  ├─ Parse advertisements → device list
  ├─ connDevice(mac)
  ├─ [optional] setPassword
  └─ DeviceInfoActivity
       ├─ LoRa / Parking / General / Device tabs
       ├─ sendOrder read/write parameters
       ├─ LoRa: LoRaConnSettingActivity, LoRaAppSettingActivity, MessageTypeSettingsActivity
       ├─ Parking: parking detection, calibration, slave settings (ParkingFragment)
       ├─ General: BleSettingsActivity, BleFixActivity, AdvancedSettingActivity
       ├─ Filter: FilterIBeaconActivity, FilterAdvNameActivity, FilterMacAddressActivity, ...
       ├─ ACTION_CURRENT_DATA → device disconnect Notify / log data / slave notify
       ├─ ACTION_DISCONNECTED → link lost
       └─ SystemInfoActivity
            ├─ DFU (Nordic) → back to scan and reconnect
            ├─ LogDataActivity → log sync and export
            └─ SelfTestActivity → slave DFU
```

---

## 8. Core Classes Quick Reference

| Stage | Class | Role |
|-------|-------|------|
| Scan | `MokoBleScanner` | Scan control |
| Scan | `MokoScanDeviceCallback` | Scan callbacks |
| Connect | `MoKoSupport` | Connect, send commands, Bluetooth on/off |
| Comm | `OrderTaskAssembler` | Build read/write tasks |
| Event | `ConnectStatusEvent` | Connected / disconnected |
| Event | `OrderTaskResponseEvent` | Command results, Notify data |

---

## 9. Notes

1. **Permissions**: Android 6.0+ requires runtime location for scanning; Android 12+ needs `BLUETOOTH_SCAN` / `BLUETOOTH_CONNECT`.
2. **EventBus**: The SDK posts events internally. To use LiveData/RxJava instead, change `orderFinish` / `orderTimeout` / `orderResult` / `orderNotify` in `MoKoSupport`.
3. **Logging**: The SDK uses `XLog` with file output and storage permission. To disable file logging, keep only `XLog.init(config)` in `BaseApplication`.
4. **Parameters**: Each `ParamsKeyEnum` maps to `OrderTaskAssembler` methods. When adding parameters, extend `ParamsReadTask` / `ParamsWriteTask` accordingly.
5. **Demo references**: Scan/connect — `LoRaLW009MainActivity`; parameters — `LoRaConnSettingActivity`, `DeviceInfoActivity`, `LoRaFragment`, `ParkingFragment`; filters — `FilterIBeaconActivity`, `FilterMacAddressActivity`; log export — `LogDataActivity`; main DFU — `SystemInfoActivity`; slave DFU — `SelfTestActivity`.

---

## Changelog

| Date | Version | Notes |
|------|---------|-------|
| 2021.03.11 | mokosupport 1.0 | Initial release |
| — | mokosupport 4.0 | compileSdk 35, minSdk 28 |
