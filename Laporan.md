- Tujuan; Tujuan dari proyek ini adalah supaya membantu penjualan UMKM dalam pemesanan secara online agar lebih mempermudah dalam memanajemen list pemesanan dari pada menggunakan secara manual melalui Whatsapp.

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
- `network/ApiClient.kt` — Wrapper berbasis OkHttp untuk panggilan HTTP (BASE_URL, helper request).

6) Layouts & resources
- Layout XML utama: `app/src/main/res/layout/activity_shipping.xml`, `fragment_home.xml`, `activity_product_detail.xml`, `fragment_cart.xml`, dan layout lain untuk setiap screen.
- Drawables & image assets: ikon aplikasi, kategori, dan gambar produk sample.

7) Node mock server (opsional)
- `server.js`, `package.json`, `package-lock.json` — Node-based mock WebSocket server script yang ada di repository.

8) Dependensi
- OSMDroid — digunakan untuk peta.
- Gson — digunakan untuk serialisasi/deserialisasi JSON di SharedPreferences/managers.
- Kotlin Coroutines — digunakan untuk operasi latar belakang dan simulasi delay.
- OkHttp — ada file wrapper (`network/ApiClient.kt`) dan dependency entri di konfigurasi proyek.

