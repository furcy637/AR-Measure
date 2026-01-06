# AR-MEASURE: Artırılmış Gerçeklik ile Mesafe ve Alan Ölçüm Uygulaması

## Proje Bilgileri
- **Ad Soyad:** Furkan Dokuzoğlu
- **Numara:** 221307080
- **Bölüm:** Bilişim Sistemleri Mühendisliği
- **Danışman:** Öğr. Gör. Yavuz Selim Fatihoğlu

## Proje Açıklaması
Bu uygulama, ARCore teknolojisini kullanarak gerçek dünya üzerinde mesafe ve alan ölçümü yapmanızı sağlar. Kamera aracılığıyla yüzeyleri tespit eder ve dokunarak nokta ekleyerek ölçüm yapabilirsiniz.

## Özellikler
- **Yüzey Tespiti (Plane Detection):** ARCore ile yatay ve dikey yüzeyleri otomatik tespit eder
- **Mesafe Ölçümü:** İki nokta arasındaki mesafeyi cm veya m cinsinden ölçer
- **Alan Ölçümü:** 3 veya daha fazla nokta ile çokgen alanını hesaplar
- **Kolay Kullanım:** Dokunarak nokta ekleme ve temizleme

## Kullanılan Teknolojiler
- Android Studio
- Kotlin
- Google ARCore SDK
- SceneView (ARCore görselleştirme kütüphanesi)
- ViewBinding

## Sistem Gereksinimleri
- Android 7.0 (API 24) veya üzeri
- ARCore destekli cihaz
- Kamera ve hareket sensörleri

## Kurulum

### 1. Android Studio'da Projeyi Açın
1. Android Studio'yu açın
2. "Open" seçeneği ile proje klasörünü açın
3. Gradle sync'in tamamlanmasını bekleyin

### 2. Cihaz Hazırlığı
1. ARCore destekli bir Android cihaz bağlayın
2. USB hata ayıklamayı (USB Debugging) etkinleştirin
3. Cihazda "Google Play Services for AR" uygulamasının yüklü olduğundan emin olun

### 3. Uygulamayı Çalıştırın
1. Android Studio'da "Run" butonuna tıklayın
2. Cihazınızı seçin
3. Uygulama yüklenecek ve açılacaktır

## Kullanım Kılavuzu

### Mesafe Ölçümü
1. Uygulama açıldığında varsayılan mod "Mesafe" modudur
2. Telefonu yavaşça hareket ettirerek yüzeyin taranmasını bekleyin
3. "Yüzey bulundu!" mesajı görünce, ilk noktayı eklemek için ekrana dokunun
4. İkinci noktayı eklemek için tekrar dokunun
5. Mesafe otomatik olarak hesaplanacak ve gösterilecektir

### Alan Ölçümü
1. "Mod: Mesafe" butonuna tıklayarak "Mod: Alan" moduna geçin
2. En az 3 nokta ekleyin (daha fazla da ekleyebilirsiniz)
3. "Alan Hesapla" butonuna tıklayın
4. Alan sonucu cm² veya m² olarak gösterilecektir

### Temizleme
- "Temizle" butonuna tıklayarak tüm noktaları silebilir ve yeni bir ölçüme başlayabilirsiniz

## Proje Yapısı

```
app/
├── src/main/
│   ├── java/com/example/ar_measure/
│   │   └── MainActivity.kt          # Ana aktivite - tüm uygulama mantığı
│   ├── res/
│   │   ├── layout/
│   │   │   └── activity_main.xml    # Arayüz tasarımı
│   │   └── values/
│   │       └── strings.xml          # Uygulama metinleri
│   └── AndroidManifest.xml          # Uygulama izinleri ve yapılandırması
└── build.gradle.kts                 # Bağımlılıklar
```

## Kod Açıklaması

### MainActivity.kt
Ana aktivite dosyası aşağıdaki işlevleri içerir:

1. **setupARScene()**: AR sahnesini yapılandırır
   - Yüzey tespitini etkinleştirir
   - Dokunma olaylarını dinler

2. **addPoint()**: Yeni nokta ekler
   - Anchor oluşturur ve listeye ekler
   - Mesafe modunda 2 nokta ile sınırlar

3. **calculateAndShowDistance()**: Mesafe hesaplar
   - Öklid mesafesi formülü kullanır: `sqrt((x2-x1)² + (y2-y1)² + (z2-z1)²)`

4. **calculateAndShowArea()**: Alan hesaplar
   - Shoelace formülü kullanır (çokgen alanı hesaplama)

## Formüller

### Öklid Mesafesi (3D)
```
mesafe = √[(x₂-x₁)² + (y₂-y₁)² + (z₂-z₁)²]
```

### Shoelace Formülü (Çokgen Alanı)
```
Alan = |Σ(xᵢ × zⱼ - xⱼ × zᵢ)| / 2
```
Burada j = (i + 1) mod n (son nokta için ilk noktaya döner)

## Sorun Giderme

### "ARCore yüklü değil" hatası
- Google Play Store'dan "Google Play Services for AR" uygulamasını yükleyin

### Yüzey tespit edilmiyor
- Daha iyi aydınlatılmış bir ortamda deneyin
- Telefonu daha yavaş hareket ettirin
- Düz ve desenli yüzeylerde daha iyi çalışır

### Uygulama çöküyor
- Cihazınızın ARCore destekleyip desteklemediğini kontrol edin
- Android sürümünüzün 7.0 veya üzeri olduğundan emin olun

## Lisans
Bu proje, Kocaeli Üniversitesi Bilişim Sistemleri Mühendisliği Bölümü bitirme projesi olarak geliştirilmiştir.

## İletişim
- **E-posta:** 221307080@kocaeli.edu.tr
- **Telefon:** +90 555 880 7275

