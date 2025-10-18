# CODEBASE_REPORT

Tanggal: 2025-10-16

Ringkasan singkat
- Tujuan: laporan terpusat yang merangkum semua kode penting di proyek ini, menjelaskan peran file/komponen, menandai apakah menggunakan mock/local storage atau koneksi online (HTTP/WebSocket), mencatat dependensi jaringan yang masih ada, dan memberikan rekomendasi serta langkah verifikasi.
- Scope: file-file di `app/src/main/java/...`, layout penting, utilitas, dokumentasi terkait WebSocket/network, dan pengaturan dependensi Gradle.

1) Struktur dan komponen utama

- UI (Activities / Fragments)
  - `MainActivity.kt` — titik masuk utama, men-setup bottom navigation dan fragment host.
  - `HomeFragment.kt` — daftar produk, search, product grid. Sekarang menggunakan `MockDataProvider.getProducts()` untuk content.
  - `CartFragment.kt` — menampilkan isi keranjang (mengambil dari `CartManager`), `MockDataProvider.seedCart()` dipanggil untuk sample data.
  - `ProductDetailActivity.kt` — menampilkan detail produk dan review (sumber review via `ReviewManager`/SharedPreferences).
  - `ShippingTrackingActivity.kt` — peta/track pengiriman. Saat ini menggunakan OSMDroid `MapView` dan mock WebSocket messages via Handler/Runnable. Tidak membuat koneksi WebSocket nyata.
  - Banyak activity/fragment lain (Profile, OrderHistory, Voucher, Wishlist, Notification, Payment, OrderSummary, dll.) yang mengonsumsi repositori/manager yang sekarang menggunakan dummy/mock data or SharedPreferences.

- Data & Domain
  - `mock/MockDataProvider.kt` — sumber data contoh: products, user, notifications, vouchers; helper `seedCart()`.
  - `data/DummyDatabase.kt` — in-memory "database" aplikasi: users, orders, wishlist. Repositories sekarang mendelegasikan ke sini untuk offline/demo mode.
  - `data/UserRepository.kt` — sekarang menggunakan `DummyDatabase` untuk register/login/profile updates.
  - `data/OrderRepository.kt` — menggunakan `DummyDatabase` untuk list/create order.
  - `data/WishlistRepository.kt` — menggunakan `DummyDatabase`.
  - `cart/CartManager.kt` — pengelola keranjang lokal (add/remove/getTotal) yang disimpan di memori/SharedPreferences.

- Utilities
  <!-- - `network/ApiClient.kt` — OkHttp-based helper (BASE_URL, httpClient, buildGet/buildPostForm). Saat ini bersifat legacy/unused oleh alur utama, tetapi masih ada jika ingin re-enable network mode.
  - `util/OsrmUtil.kt` — semula melakukan HTTP request ke OSRM; telah diubah untuk melakukan interpolasi rute lokal sehingga tidak memanggil jaringan. -->
  - `util/ReviewManager.kt`, `util/AuthManager.kt`, `wishlist/WishlistManager.kt` — menggunakan SharedPreferences + Gson, bukan web API.

- Layouts & resources
  - `app/src/main/res/layout/*` — layouts untuk semua layar: `activity_shipping.xml`, `fragment_home.xml`, `activity_product_detail.xml`, `fragment_cart.xml`, dll.
  - Drawables & assets: ikon, vector assets, dan gambar sample digunakan oleh mock provider.

- Node mock server (opsional)
  - `server.js`, `package.json`, `package-lock.json` — skrip Node untuk mock WebSocket server jika Anda ingin demo WebSocket eksternal. Tidak diperlukan untuk mode mock lokal di aplikasi.

2) Status koneksi online vs mock (per-file/komponen penting)

- Fully mocked/local (tidak memerlukan koneksi online):
  - `MockDataProvider.kt` — produk, user, voucher, notifikasi sample.
  - `DummyDatabase.kt` — in-memory user/order/wishlist.
  - `CartManager.kt`, `ReviewManager.kt`, `WishlistManager.kt`, `AuthManager.kt` — SharedPreferences/Gson-based local storage.
  - `ShippingTrackingActivity.kt` — simulates WebSocket messages; `OsrmUtil.matchRoute()` performs interpolation locally.
  - UI screens reading from repositories now get data from `DummyDatabase`.

<!-- - Legacy / reference (tidak dipanggil saat ini):
  - `network/ApiClient.kt` — masih ada OkHttp imports and BASE_URL; not used by repositories after refactor.
  - `gradle/libs.versions.toml` — lists `okhttp` version.
  - `app/build.gradle.kts` — still includes `implementation(libs.okhttp)`.
  - `WEBSOCKET_SETUP_GUIDE.md`, parts of `SHIPPING_TRACKING_README.md` — docs that describe WebSocket setup and example URLs.
  - `server.js` (node) and `package.json` — local mock server (only needed if you want to run a separate server) -->

3) Dependencies network & other notable libs
- OkHttp (`com.squareup.okhttp3:okhttp:4.12.0`) — present in Gradle but not used by main runtime flows currently.
- Gson — used for JSON parsing in managers and mock utils.
- OSMDroid (`org.osmdroid:osmdroid-android:6.1.17`) — used for map rendering in `ShippingTrackingActivity`.
- Firebase BOM — present in Gradle (analytics) but not critical for offline demo flows.
- Kotlin coroutines (`kotlinx-coroutines-android`) — used across Activities for background tasks and simulated delays.

4) Rekomendasi pembersihan dan opsi re-enable network

A. Minimal / safe (recommended if you may re-enable network later)
- Keep `okhttp` dependency but mark `ApiClient.kt` as legacy (rename to `ApiClient.legacy.kt` OR keep header comment). This avoids build breaks and keeps a simple rollback path.
- Update `SHIPPING_TRACKING_README.md` to clearly say mock mode is default and move instructions to "Advanced / Optional".
- Move `server.js` and `WEBSOCKET_SETUP_GUIDE.md` to `extras/` or `docs/` so they don't clutter main docs.

B. Moderate (reduce dependencies)
- Rename `ApiClient.kt` to `ApiClient.legacy.kt` and remove import of `okhttp` from `app/build.gradle.kts` and `gradle/libs.versions.toml`.
- Run `./gradlew assembleDebug` and fix any references that still import `okhttp3`.

C. Aggressive (remove network traces)
- Delete `ApiClient.legacy.kt`, remove `server.js`, and remove WebSocket guides. Update docs to emphasize mock-only.
- Run `assembleDebug` and test all flows.

5) Verifikasi & checklist (apa yang harus dicek setelah pembersihan)
- [ ] Run `./gradlew assembleDebug` — ensure clean compile.
- [ ] Search repo for `okhttp` / `okhttp3` / `ApiClient` / `WEBSOCKET_URL` — ensure no compile-time imports remain.
- [ ] Launch app on device/emulator and navigate: Home → Product → Cart → ShippingTracking; verify mock data and simulated tracking behave as expected.
- [ ] If removed `okhttp`, ensure any removed files are archived in git branch or backup.

6) Cara re-enable WebSocket / network (ringkas)
1. Restore `ApiClient.kt` (or rename `ApiClient.legacy.kt` back to `ApiClient.kt`).
2. Ensure `okhttp` dependency exists in Gradle (add `implementation("com.squareup.okhttp3:okhttp:4.12.0")`).
3. Implement WebSocket client (OkHttp) or other network client in `ShippingTrackingActivity` or a dedicated service; feed received JSON strings into existing `handleWebSocketMessage()` method.
4. Update README with server URL and message format.

# CODEBASE_REPORT

Tanggal: 2025-10-17

Daftar file dan deskripsi singkat (hanya kode/file; tanpa saran atau rekomendasi):

1) UI — Activities & Fragments
- `MainActivity.kt` — Entry point yang mengatur bottom navigation dan host fragment.
- `HomeFragment.kt` — Tampilan daftar produk dan pencarian; konsumsi data dari `MockDataProvider`.
- `CartFragment.kt` — Tampilan isi keranjang; menggunakan `CartManager` untuk data keranjang.
- `ProductDetailActivity.kt` — Layar detail produk, gambar, deskripsi, dan review.
- `ShippingTrackingActivity.kt` — Layar peta dan tracking; menangani pesan simulasi untuk menggerakkan marker.
- `ProfileActivity.kt` / `EditProfileActivity.kt` — Tampilan dan penyuntingan profil pengguna.

2) Data & Repositori
- `mock/MockDataProvider.kt` — Penyedia data contoh: produk, user, voucher, notifikasi.
- `data/DummyDatabase.kt` — In-memory storage untuk users, orders, wishlist, dan operasi CRUD sederhana.
- `data/UserRepository.kt` — Abstraksi akses user yang berinteraksi dengan `DummyDatabase`.
- `data/OrderRepository.kt` — Manajemen order yang berinteraksi dengan `DummyDatabase`.
- `data/WishlistRepository.kt` — Manajemen wishlist yang berinteraksi dengan `DummyDatabase`.

3) Manager & penyimpanan lokal
- `cart/CartManager.kt` — Logika penambahan/penghapusan item dan perhitungan total keranjang.
- `wishlist/WishlistManager.kt` — Penyimpanan dan pembacaan wishlist via SharedPreferences + Gson.
- `util/ReviewManager.kt` — Penyimpanan review produk ke SharedPreferences.
- `util/AuthManager.kt` — Penyimpanan dan pembacaan session/user profile di SharedPreferences (serialisasi JSON).

4) Utilities peta / routing / tracking
- `util/OsrmUtil.kt` — Utilitas rute dan interpolasi titik (generasi rute lokal/interpolasi).
- (bagian map dalam) `ShippingTrackingActivity.kt` — Penggunaan OSMDroid `MapView`, `Marker`, `Polyline` untuk render peta dan rute.

5) Network (legacy / fallback)
- `network/ApiClient.kt` — Wrapper berbasis OkHttp untuk panggilan HTTP (BASE_URL, helper request) — tersedia sebagai file di codebase.

6) Layouts & resources
- Layout XML utama: `app/src/main/res/layout/activity_shipping.xml`, `fragment_home.xml`, `activity_product_detail.xml`, `fragment_cart.xml`, dan layout lain untuk setiap screen.
- Drawables & image assets: ikon aplikasi, kategori, dan gambar produk sample.

7) Node mock server (opsional)
- `server.js`, `package.json`, `package-lock.json` — Node-based mock WebSocket server script yang ada di repository.

8) Dependensi yang terlihat di codebase
- OSMDroid — digunakan untuk peta.
- Gson — digunakan untuk serialisasi/deserialisasi JSON di SharedPreferences/managers.
- Kotlin Coroutines — digunakan untuk operasi latar belakang dan simulasi delay.
- OkHttp — ada file wrapper (`network/ApiClient.kt`) dan dependency entri di konfigurasi proyek.
