# 🚀 Panduan Setup WebSocket Server untuk Shipping Tracking

## ✅ Status Pengecekan Error

### 1. **Kode Android** ✅
- ✅ Tidak ada linter errors
- ✅ Dependencies sudah diperbaiki (menghapus duplikasi OkHttp)
- ✅ Layout XML valid
- ✅ Drawable resources valid
- ✅ Colors.xml valid

### 2. **Server WebSocket** ✅
- ✅ Syntax JavaScript valid
- ✅ Dependencies sudah didefinisikan di package.json

## 🛠️ Cara Menjalankan

### Opsi 1: Menggunakan Mock Data (Paling Mudah)
```kotlin
// Di ShippingTrackingActivity.kt, pastikan:
private const val USE_MOCK_DATA = true
```
**Keuntungan**: Langsung bisa dijalankan tanpa server

### Opsi 2: Menggunakan WebSocket Server

#### Langkah 1: Install Node.js
1. Download Node.js dari https://nodejs.org/
2. Install dengan default settings

#### Langkah 2: Setup Server
```bash
# Buka terminal di folder websocket-server
cd websocket-server

# Install dependencies
npm install

# Jalankan server
npm start
```

#### Langkah 3: Update IP Address di Android
```kotlin
// Di ShippingTrackingActivity.kt, ganti IP address:
private const val WEBSOCKET_URL = "ws://192.168.1.19:8080/tracking"
// Ganti 192.168.1.100 dengan IP komputer Anda
```

## 🔧 Troubleshooting

### Masalah 1: "Connection refused"
**Solusi**:
1. Pastikan server WebSocket sudah berjalan
2. Cek IP address di kode Android
3. Pastikan device dan komputer dalam jaringan yang sama

### Masalah 2: "Dependencies not found"
**Solusi**:
```bash
# Di Android Studio, jalankan:
./gradlew clean
./gradlew build
```

### Masalah 3: "Map not loading"
**Solusi**:
1. Pastikan internet connection aktif
2. Cek permission INTERNET di AndroidManifest.xml
3. Pastikan OSMDroid dependencies ter-install

### Masalah 4: "WebSocket connection failed"
**Solusi**:
1. Ganti ke mock data: `USE_MOCK_DATA = true`
2. Atau pastikan server berjalan di port 8080
3. Cek firewall settings

## 📱 Testing

### Test Mock Data
1. Set `USE_MOCK_DATA = true`
2. Jalankan aplikasi
3. Lihat mobil box bergerak di peta setiap 3 detik

### Test WebSocket
1. Set `USE_MOCK_DATA = false`
2. Jalankan server: `npm start`
3. Jalankan aplikasi
4. Lihat status "Terhubung" di pojok kanan atas

## 🌐 Server Endpoints

### HTTP Endpoints
- `GET /` - Status server
- `GET /api/driver` - Data driver
- `GET /api/route` - Data rute

### WebSocket Endpoint
- `ws://localhost:8080/tracking` - Real-time tracking

## 📊 Data Format

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
    {"lat": -6.2095, "lng": 106.8458}
  ]
}
```

## 🎯 Fitur yang Sudah Berfungsi

### ✅ Mock Data Mode
- Mobil box bergerak otomatis
- Update ETA setiap 3 detik
- Status berubah: picked_up → in_transit → delivered
- Rute hijau ditampilkan di peta

### ✅ WebSocket Mode
- Koneksi real-time
- Update posisi driver
- Status koneksi indicator
- Auto-reconnection

### ✅ UI Components
- Peta OpenStreetMap
- Card informasi driver
- Tombol telepon/chat
- Overlay informasi rute

## 🚨 Catatan Penting

1. **Untuk testing cepat**: Gunakan mock data (`USE_MOCK_DATA = true`)
2. **Untuk production**: Gunakan WebSocket server dengan database real
3. **IP Address**: Pastikan IP address di kode Android sesuai dengan komputer server
4. **Network**: Device dan server harus dalam jaringan yang sama

## 📞 Support

Jika ada masalah:
1. Cek log di Android Studio
2. Cek log di terminal server
3. Pastikan semua dependencies ter-install
4. Restart Android Studio dan server
