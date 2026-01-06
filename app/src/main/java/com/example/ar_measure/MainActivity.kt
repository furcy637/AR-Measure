package com.example.ar_measure

import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ar_measure.databinding.ActivityMainBinding
import com.google.ar.core.Anchor
import com.google.ar.core.Config
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import io.github.sceneview.ar.node.AnchorNode
import java.util.Locale
import kotlin.math.sqrt
import kotlin.math.abs

/**
 * AR-MEASURE MainActivity
 *
 * Bu sınıf uygulamanın ana ekranını yönetir.
 * Kullanıcı ekrana dokunarak noktalar ekleyebilir ve
 * bu noktalar arasındaki mesafeyi veya oluşan alanı ölçebilir.
 *
 * Hazırlayan: Furkan Dokuzoğlu - 221307080
 */
class MainActivity : AppCompatActivity() {

    // ViewBinding - Layout dosyasındaki görünümlere kolay erişim sağlar
    private lateinit var binding: ActivityMainBinding

    // Dokunma olaylarını algılamak için kullanılan GestureDetector
    private lateinit var gestureDetector: GestureDetector

    // Eklenen anchor noktalarını saklayan liste
    // Anchor: AR dünyasında sabit bir konum noktası
    private val anchorList = mutableListOf<Anchor>()

    // AnchorNode listesi - sahneye eklenen düğümler
    private val nodeList = mutableListOf<AnchorNode>()

    // Mevcut ölçüm modu - true: Mesafe, false: Alan
    private var isDistanceMode = true

    // Yüzey bulundu mu? (Plane detection)
    private var planeFound = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Layout dosyasını bağla
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // GestureDetector'ı oluştur - ekrana dokunmayı algılar
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                // Tek dokunma algılandı
                onSingleTap(e)
                return true
            }
        })

        // AR sahnesini ayarla
        setupARScene()

        // Buton tıklama olaylarını ayarla
        setupButtons()
    }

    /**
     * AR sahnesini ayarlayan fonksiyon
     * Plane detection (yüzey tespiti) ve dokunma olaylarını yapılandırır
     */
    private fun setupARScene() {
        val sceneView = binding.arSceneView

        // ARCore oturumunu yapılandır
        sceneView.configureSession { _, config ->
            // Yatay ve dikey yüzeyleri tespit et
            config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
            // Işık tahminini etkinleştir
            config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
        }

        // Her frame güncellendiğinde çalışır - yüzey kontrolü yapar
        sceneView.onSessionUpdated = { session, _ ->
            // Yüzey bulundu mu kontrol et
            if (!planeFound) {
                // Tespit edilen yüzeyleri al
                val planes = session.getAllTrackables(Plane::class.java)
                for (plane in planes) {
                    if (plane.trackingState == TrackingState.TRACKING) {
                        // Yüzey bulundu!
                        planeFound = true
                        runOnUiThread {
                            binding.infoText.text = "Yüzey bulundu! Nokta eklemek için ekrana dokunun."
                        }
                        break
                    }
                }
            }
        }

        // Ekrana dokunulduğunda çalışır
        // SceneView'e tıklama olayı ekle
        sceneView.setOnTouchListener { _, event ->
            // GestureDetector'a olayı ilet
            gestureDetector.onTouchEvent(event)
            true
        }
    }

    /**
     * EKRANA NOKTA OLUŞTURMA
     * Ekrana dokunulduğunda AR sahnesinde bir nokta oluşturur
     */
    private fun onSingleTap(event: MotionEvent) {
        // Yüzey bulunmamışsa işlem yapma
        if (!planeFound) {
            return
        }

        // AR oturumunu al
        val session = binding.arSceneView.session ?: return
        val frame = session.update()

        // Ekran üzerindeki dokunma noktasından gerçek dünyaya ışın at (hit test)
        val hitResultList = frame.hitTest(event.x, event.y)

        // Hit test sonuçlarını kontrol et
        for (hitResult in hitResultList) {
            val trackable = hitResult.trackable

            // Sadece yüzeye dokunmaları kabul et
            if (trackable is Plane && trackable.isPoseInPolygon(hitResult.hitPose)) {
                // Dokunulan noktada anchor (sabit nokta) oluştur
                val anchor = hitResult.createAnchor()
                addPoint(anchor)
                break
            }
        }
    }

    /**
     * Buton ayarları
     */
    private fun setupButtons() {
        // Mod değiştirme butonu
        binding.modeButton.setOnClickListener {
            // Modu değiştir
            isDistanceMode = !isDistanceMode

            // Buton yazısını güncelle
            if (isDistanceMode) {
                binding.modeButton.text = "Mod: Mesafe"
                binding.calculateAreaButton.visibility = View.GONE
            } else {
                binding.modeButton.text = "Mod: Alan"
                binding.calculateAreaButton.visibility = View.VISIBLE
            }

            // Önceki ölçümleri temizle
            clearAllPoints()

            // Kullanıcıya bilgi ver
            val modeName = if (isDistanceMode) "Mesafe" else "Alan"
            Toast.makeText(this, "$modeName modu seçildi", Toast.LENGTH_SHORT).show()
        }

        // Temizle butonu
        binding.clearButton.setOnClickListener {
            clearAllPoints()
            Toast.makeText(this, "Tüm noktalar temizlendi", Toast.LENGTH_SHORT).show()
        }

        // Alan hesapla butonu
        binding.calculateAreaButton.setOnClickListener {
            calculateAndShowArea()
        }
    }

   /**
     * Nokta ekleme fonksiyonu
     *
     * @param anchor Eklenen nokta
     */
    private fun addPoint(anchor: Anchor) {
        // Mesafe modunda 2'den fazla nokta eklenmesini engelle
        if (isDistanceMode && anchorList.size >= 2) {
            Toast.makeText(this, "Mesafe modunda sadece 2 nokta eklenebilir. Önce temizleyin.", Toast.LENGTH_SHORT).show()
            return
        }

        // Anchor'u listeye ekle
        anchorList.add(anchor)

        // Anchor pozisyonunu al
        val pose = anchor.pose
        val x = pose.tx()
        val y = pose.ty()
        val z = pose.tz()

        // AnchorNode oluştur ve sahneye ekle
        val anchorNode = AnchorNode(
            engine = binding.arSceneView.engine,
            anchor = anchor
        )

        binding.arSceneView.addChildNode(anchorNode)
        nodeList.add(anchorNode)

        // Overlay view'ı güncelle - noktaları ekranda göster
        updateOverlayView()

        // Kullanıcıya bilgi ver - hangi nokta eklendi
        val pointNumber = anchorList.size
        Toast.makeText(
            this,
            "Nokta $pointNumber eklendi\nKonum: X=${String.format(Locale.getDefault(), "%.2f", x)}m, Y=${String.format(Locale.getDefault(), "%.2f", y)}m, Z=${String.format(Locale.getDefault(), "%.2f", z)}m",
            Toast.LENGTH_SHORT
        ).show()

        // Mesafe modunda ve 2 nokta varsa mesafeyi hesapla
        if (isDistanceMode && anchorList.size == 2) {
            calculateAndShowDistance()
        }

        // Bilgi metnini güncelle
        updateInfoText()
    }


    /**
     * Overlay view'ı günceller - 3D noktaları 2D ekran koordinatlarına çevirir
     */
    private fun updateOverlayView() {
        val screenPoints = mutableListOf<Pair<Float, Float>>()

        // Her anchor için ekran koordinatlarını hesapla
        for (anchor in anchorList) {
            val pose = anchor.pose

            // ARCore kamerası ve projeksiyon kullanarak ekran koordinatlarını hesapla
            val session = binding.arSceneView.session
            val frame = session?.update()

            if (frame != null) {
                val camera = frame.camera
                val viewMatrix = FloatArray(16)
                val projectionMatrix = FloatArray(16)

                camera.getViewMatrix(viewMatrix, 0)
                camera.getProjectionMatrix(projectionMatrix, 0, 0.1f, 100f)

                // 3D dünya koordinatları
                val worldPos = floatArrayOf(pose.tx(), pose.ty(), pose.tz(), 1f)

                // View matris ile çarp
                val viewPos = FloatArray(4)
                android.opengl.Matrix.multiplyMV(viewPos, 0, viewMatrix, 0, worldPos, 0)

                // Projeksiyon matris ile çarp
                val clipPos = FloatArray(4)
                android.opengl.Matrix.multiplyMV(clipPos, 0, projectionMatrix, 0, viewPos, 0)

                // Normalize edilmiş cihaz koordinatlarına çevir (NDC)
                if (clipPos[3] != 0f) {
                    val ndcX = clipPos[0] / clipPos[3]
                    val ndcY = clipPos[1] / clipPos[3]

                    // Normalize cihaz koordinatlarını ekran koordinatlarına çevir
                    val screenWidth = binding.pointOverlayView.width
                    val screenHeight = binding.pointOverlayView.height

                    val screenX = (ndcX + 1f) * 0.5f * screenWidth
                    val screenY = (1f - ndcY) * 0.5f * screenHeight  // Y eksen ters

                    screenPoints.add(Pair(screenX, screenY))
                }
            }
        }

        // Overlay view'ı güncelle
        binding.pointOverlayView.updatePoints(screenPoints)
    }

    /**
     * İki nokta arasındaki mesafeyi hesaplar ve gösterir
     */
    private fun calculateAndShowDistance() {
        if (anchorList.size < 2) return

        // İlk iki anchor'un pozisyonlarını al
        val pose1 = anchorList[0].pose
        val pose2 = anchorList[1].pose

        // 3D pozisyonları al (x, y, z)
        val x1 = pose1.tx()
        val y1 = pose1.ty()
        val z1 = pose1.tz()

        val x2 = pose2.tx()
        val y2 = pose2.ty()
        val z2 = pose2.tz()

        // Mesafeyi hesapla (Öklid mesafesi formülü)
        // Formül: sqrt((x2-x1)² + (y2-y1)² + (z2-z1)²)
        val dx = x2 - x1
        val dy = y2 - y1
        val dz = z2 - z1
        val distance = sqrt(dx * dx + dy * dy + dz * dz)

        // Sonucu göster
        showMeasurement("Mesafe: ${formatDistance(distance)}")
    }

    /**
     * Alan hesaplar ve gösterir (Shoelace formülü kullanarak)
     *
     * Shoelace formülü, 2D düzlemde bir çokgenin alanını hesaplar.
     * Biz XZ düzlemini (yatay yüzey) kullanıyoruz.
     */
    private fun calculateAndShowArea() {
        if (anchorList.size < 3) {
            Toast.makeText(this, "Alan hesaplamak için en az 3 nokta gerekli", Toast.LENGTH_SHORT).show()
            return
        }

        // Noktaların pozisyonlarını al
        val positions = anchorList.map { anchor ->
            val pose = anchor.pose
            floatArrayOf(pose.tx(), pose.ty(), pose.tz())
        }

        // Shoelace formülü ile alanı hesapla
        // XZ düzlemini kullanıyoruz (yatay yüzey için)
        var area = 0f
        val n = positions.size

        for (i in 0 until n) {
            val j = (i + 1) % n // Sonraki nokta indeksi (son için ilk noktaya döner)

            // X ve Z koordinatlarını kullan
            val xi = positions[i][0]  // X koordinatı
            val zi = positions[i][2]  // Z koordinatı
            val xj = positions[j][0]
            val zj = positions[j][2]

            area += xi * zj
            area -= xj * zi
        }

        area = abs(area) / 2f

        // Sonucu göster
        showMeasurement("Alan: ${formatArea(area)}")
    }

    /**
     * Ölçüm sonucunu ekranda gösterir
    */
    private fun showMeasurement(text: String) {
        binding.measurementText.text = text
        binding.measurementText.visibility = View.VISIBLE
    }

    /**
     * Bilgi metnini günceller
     */
    private fun updateInfoText() {
        val pointCount = anchorList.size

        binding.infoText.text = when {
            !planeFound -> "Yüzey taranıyor... Telefonu yavaşça hareket ettirin."
            isDistanceMode && pointCount == 0 -> "Mesafe modu: İlk noktayı eklemek için dokunun."
            isDistanceMode && pointCount == 1 -> "Mesafe modu: İkinci noktayı eklemek için dokunun."
            isDistanceMode && pointCount == 2 -> "Mesafe ölçüldü! Yeni ölçüm için 'Temizle' butonuna basın."
            !isDistanceMode && pointCount < 3 -> "Alan modu: $pointCount nokta. En az 3 nokta gerekli."
            !isDistanceMode -> "Alan modu: $pointCount nokta. 'Alan Hesapla' butonuna basın."
            else -> "$pointCount nokta eklendi."
        }
    }

    /**
     * Tüm noktaları temizler
     */
    private fun clearAllPoints() {
        // Tüm node'ları sahneden kaldır
        for (node in nodeList) {
            node.parent = null
        }
        nodeList.clear()


        // Tüm anchor'ları temizle
        for (anchor in anchorList) {
            anchor.detach()
        }
        anchorList.clear()

        // Overlay view'ı temizle
        binding.pointOverlayView.clearPoints()

        // Ölçüm metnini gizle
        binding.measurementText.visibility = View.GONE

        // Bilgi metnini güncelle
        updateInfoText()
    }

    /**
     * Mesafeyi formatlar (cm veya m olarak)
     */
    private fun formatDistance(distanceMeters: Float): String {
        return if (distanceMeters < 1f) {
            // 1 metreden küçükse santimetre olarak göster
            String.format(Locale.getDefault(), "%.1f cm", distanceMeters * 100)
        } else {
            // 1 metre veya daha büyükse metre olarak göster
            String.format(Locale.getDefault(), "%.2f m", distanceMeters)
        }
    }

    /**
     * Alanı formatlar (cm² veya m² olarak)
       */
    private fun formatArea(areaSquareMeters: Float): String {
        return if (areaSquareMeters < 1f) {
            // 1 metrekareden küçükse santimetrekare olarak göster
            String.format(Locale.getDefault(), "%.1f cm²", areaSquareMeters * 10000)
        } else {
            // 1 metrekare veya daha büyükse metrekare olarak göster
            String.format(Locale.getDefault(), "%.2f m²", areaSquareMeters)
        }
    }

    override fun onResume() {
        super.onResume()
        // AR oturumunu devam ettir (lifecycle yönetimi ARSceneView tarafından otomatik yapılır)
    }

    override fun onPause() {
        super.onPause()
        // AR oturumunu duraklat (lifecycle yönetimi ARSceneView tarafından otomatik yapılır)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Tüm anchor'ları temizle
        clearAllPoints()
    }
}

