# Shipping Tracking (Mock Mode by default)

## Deskripsi
Fitur tracking pengiriman yang menampilkan peta dengan mobil box yang berjalan sesuai rute. Aplikasi ini menggunakan mock/simulasi data secara default sehingga tidak memerlukan server WebSocket untuk testing UI atau demo.

## Fitur Utama
- **Peta Real-time**: Menggunakan OpenStreetMap untuk menampilkan peta
- **Tracking Mobil Box**: Menampilkan posisi mobil box yang bergerak sesuai rute
- **WebSocket Connection**: Koneksi real-time untuk update posisi driver
- **Informasi Driver**: Menampilkan detail driver, rating, dan informasi kendaraan
- **ETA (Estimated Time of Arrival)**: Estimasi waktu kedatangan
- **Action Buttons**: Tombol untuk telepon dan chat dengan driver

## Komponen yang Digunakan

### Layout (activity_shipping.xml)
- `MapView`: Peta OpenStreetMap
- `MaterialCardView`: Card informasi driver di bagian bawah
- `ProgressBar`: Indikator loading
- `TextView`: Status koneksi dan informasi rute

### Activity (ShippingTrackingActivity.kt)
- **Mode Default**: Mock data/simulasi (tidak menggunakan WebSocket)
  - ShippingTrackingActivity menghasilkan update lokasi, status, dan route secara lokal untuk keperluan demo.
- **Optional (enable WebSocket)**: ---------------
- **Map Controller**: Mengontrol peta dan marker
- **Location Updates**: Update posisi driver secara real-time
- **Route Rendering**: Menampilkan rute dengan polyline

## Dependencies yang Ditambahkan
```kotlin
// OpenStreetMap
implementation("org.osmdroid:osmdroid-android:6.1.17")

// WebSocket
implementation("com.squareup.okhttp3:okhttp:4.12.0")
```

## Permissions yang Diperlukan
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

## Cara Penggunaan

### 1. Setup WebSocket Server
Pastikan WebSocket server Anda mengirim data dalam format JSON:

```json
{
  "type": "location_update",
  "latitude": -6.2088,
  "longitude": 106.8456,
  "driver_name": "Ahmad Supriadi",
  "eta": "5 menit",
  "vehicle_info": "B 1234 ABC\nMerah - Box Truck"
}
```

### Opsional: mengaktifkan WebSocket (untuk production/testing dengan server)
Secara default aplikasi menggunakan mock data. Jika Anda ingin menghubungkan ke server WebSocket nyata:

1. Perbarui `ShippingTrackingActivity.kt` untuk menggunakan URL server Anda dan implementasikan WebSocket client (OkHttp) atau revert ke versi WebSocket asli.
2. Pastikan server mengirim data dalam format JSON seperti dijelaskan di bagian "Format Data WebSocket".

### 3. Jalankan Activity
```kotlin
val intent = Intent(this, ShippingTrackingActivity::class.java)
startActivity(intent)
```

## Format Data WebSocket

### Location Update
```json
{
  "type": "location_update",
  "latitude": -6.2088,
  "longitude": 106.8456,
  "driver_name": "Ahmad Supriadi",
  "eta": "5 menit",
  "vehicle_info": "B 1234 ABC\nMerah - Box Truck"
}
```

### Route Update
```json
{
  "type": "route_update",
  "route": [
    {"lat": -6.2088, "lng": 106.8456},
    {"lat": -6.2090, "lng": 106.8460},
    {"lat": -6.2095, "lng": 106.8465}
  ]
}
```

### Status Update
```json
{
  "type": "status_update",
  "status": "in_transit",
  "message": "Driver sedang dalam perjalanan"
}
```

## Customization

### Mengubah Warna Rute
```kotlin
routePolyline = Polyline().apply {
    setPoints(route)
    color = Color.GREEN  // Ubah warna rute
    width = 8f          // Ubah ketebalan garis
}
```

### Mengubah Marker Driver
```kotlin
driverMarker?.icon = ContextCompat.getDrawable(this, R.drawable.your_custom_icon)
```

### Mengubah Zoom Level
```kotlin
mapController.setZoom(15.0)  // Ubah level zoom
```

## Troubleshooting

### 1. Peta Tidak Muncul
- Pastikan internet connection aktif
- Check permission INTERNET dan ACCESS_NETWORK_STATE

### 2. WebSocket Tidak Terhubung
- Pastikan URL WebSocket benar
- Check server WebSocket berjalan
- Pastikan network security config mengizinkan cleartext traffic

### 3. Location Permission
- Pastikan user memberikan permission ACCESS_FINE_LOCATION
- Handle permission request dengan proper

## Testing
Mock mode berjalan otomatis dan tidak memerlukan setup server. Jalankan activity `ShippingTrackingActivity` dan Anda akan melihat update mock yang muncul secara periodik (lokasi, status, route).

Jika Anda ingin menguji WebSocket live, ikuti langkah di bagian "Opsional: mengaktifkan WebSocket".

## Catatan Penting
- Pastikan WebSocket server mendukung CORS jika diperlukan
- Gunakan HTTPS/WSS untuk production
- Implement proper error handling untuk network issues
- Consider battery optimization untuk real-time updates

## Legacy / Network helper
The project currently runs in mock mode by default. A small network helper `ApiClient.kt` remains in the codebase at `app/src/main/java/com/example/appprojek/network/ApiClient.kt`.

- Purpose: convenience OkHttp wrapper used when connecting to a real backend.
- Current status: legacy/unused by default (repositories use an in-memory DummyDatabase). Keep this file if you plan to re-enable network mode later. To make the intent explicit you may rename it to `ApiClient.legacy.kt`.

See `WEBSOCKET_CODE_REPORT.md` in the repo root for a full status report and recommended cleanup steps.
