package com.kodex.yandexmap_2

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PointF
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import androidx.core.app.ActivityCompat
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.*
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.search.*
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.Error
import com.yandex.runtime.image.ImageProvider
import com.yandex.runtime.network.NetworkError
import com.yandex.runtime.network.RemoteError

class MainActivity : AppCompatActivity(), UserLocationObjectListener, Session.SearchListener, CameraListener {

    private lateinit var mapview:MapView
    private lateinit var probkiBut: Button
    private lateinit var locationMapKit: UserLocationLayer
    lateinit var searchEdit: EditText
    lateinit var searchManager: SearchManager
    lateinit var searchSession: Session

    private fun sumbitQery(query: String){
        searchSession = searchManager.submit(
            query,
            VisibleRegionUtils.toPolygon(mapview.map.visibleRegion),
            SearchOptions(),
            this)
    }
    //private lateinit var trafficButton: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapKitFactory.setApiKey("e7de9a94-591a-4275-9b43-76b8a66f2b4b")
        MapKitFactory.initialize(this)

        setContentView(R.layout.activity_main)
        mapview = findViewById(R.id.mapview)
        probkiBut = findViewById(R.id.probkiBut)

        val mapKit: MapKit = mapKit()
        requestLocationPermission()

         showProbki(mapKit)
        //showLocationMapKit(mapKit)

        showSearchFactory()

    }

    private fun mapKit(): MapKit {
        mapview.map.move(CameraPosition(Point(45.408830,36.949551),
            14.0f, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 12f), null)
        val mapKit: MapKit = MapKitFactory.getInstance()
        return mapKit
    }

    private fun showSearchFactory() {
        SearchFactory.initialize(this)
        searchManager = SearchFactory.getInstance()
            .createSearchManager(SearchManagerType.COMBINED)
        mapview.map.addCameraListener(this)
        searchEdit = findViewById(R.id.search_edit)
        searchEdit.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                sumbitQery(searchEdit.text.toString())
            }
            false
        }
    }

    private fun showLocationMapKit(mapKit: MapKit) {
        locationMapKit = mapKit.createUserLocationLayer(mapview.mapWindow)
        locationMapKit.isVisible = true
        locationMapKit.setObjectListener(this)
    }

    private fun showProbki(mapKit: MapKit) {
        val probki = mapKit.createTrafficLayer(mapview.mapWindow)
        probki.isTrafficVisible = false

        probkiBut.setOnClickListener {
            probki.isTrafficVisible = probki.isTrafficVisible == false
        }
    }

    private fun requestLocationPermission(){
        if(ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION), 0)
            return
        }
    }

    override fun onStop() {
        mapview.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onStart() {
        mapview.onStart()
        MapKitFactory.getInstance().onStart()
        super.onStart()
    }

    override fun onObjectAdded(userLocationView: UserLocationView) {
        locationMapKit.setAnchor(
            PointF((mapview.width() * 0.5).toFloat(), (mapview.height()*0.5).toFloat()),
            PointF((mapview.width() * 0.5).toFloat(), (mapview.height()*0.83).toFloat()),
        )
        userLocationView.arrow.setIcon(ImageProvider.fromResource(this,R.drawable.strelka_neon))
        val picIcon = userLocationView.pin.useCompositeIcon()
        picIcon.setIcon("icon", ImageProvider.fromResource(this, R.drawable.transparent_blue),
            IconStyle().setAnchor(PointF(0f, 0f)).setRotationType(RotationType.NO_ROTATION).setZIndex(0f).setScale(1f)
        )
        picIcon.setIcon("pin", ImageProvider.fromResource(this, R.drawable.transpondent_red_blaack),
            IconStyle().setAnchor(PointF(0.5f,05f)).setRotationType(RotationType.ROTATE).setZIndex(1f).setScale(0.5f))
        userLocationView.accuracyCircle.fillColor = Color.BLUE and -0x66000001
    }

    override fun onObjectRemoved(p0: UserLocationView) {

    }

    override fun onObjectUpdated(p0: UserLocationView, p1: ObjectEvent) {

    }

    override fun onSearchResponse(response: Response) {
        val mapObjects:MapObjectCollection = mapview.map.mapObjects
        for(searchResult in response.collection.children){
            val resultLocation = searchResult.obj!!.geometry[0].point!!
            if(response!=null){
                mapObjects.addPlacemark(resultLocation,
                    ImageProvider.fromResource(this, R.drawable.transparent_blue))
            }
        }
    }

    override fun onSearchError(error: Error) {
      var errorMessage = "Неизвестная ошибка!"
        if (error is RemoteError){
            errorMessage = "Беспроводная ошибка!"
        }else if(error is NetworkError){
            errorMessage = "Нет интернета"
        }
    }

    override fun onCameraPositionChanged(
        map: Map,
        cameraPosition: CameraPosition,
        cameraUpdateReason: CameraUpdateReason,
        finished: Boolean,
    ) {
        if (finished){
            sumbitQery(searchEdit.text.toString())
        }
    }
}