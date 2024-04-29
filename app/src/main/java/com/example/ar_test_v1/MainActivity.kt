package com.example.ar_test_v1

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.arcore.LightEstimationMode
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.PlacementMode
import io.github.sceneview.math.Position


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    lateinit var sceneView: ArSceneView
    lateinit var placeButton: ImageButton
    lateinit var unAnchorButton: ImageButton
    lateinit var tooltipBtn: ImageButton
    lateinit var tooltipText: TextView
    lateinit var mapButton: ImageButton
    lateinit var mapView: MapView
    lateinit var googleMap: GoogleMap
    private lateinit var modelNode: ArModelNode
    lateinit var textModel: TextView
    var isMapVisible = false
    var isTooltipVisible = false

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)



        sceneView = findViewById<ArSceneView?>(R.id.sceneView).apply {
            this.lightEstimationMode = LightEstimationMode.ENVIRONMENTAL_HDR_NO_REFLECTIONS
        }

        placeButton = findViewById(R.id.place)
        unAnchorButton = findViewById(R.id.resetAnchor)
        textModel = findViewById(R.id.text)
        mapButton = findViewById(R.id.mapButton)
        tooltipText = findViewById(R.id.tooltipText)
        tooltipBtn = findViewById(R.id.tooltipBtn)

        placeButton.setOnClickListener {
            placeModel()
            textModel.text = generateRandomSentence()
            placeButton.isGone = true
            unAnchorButton.isGone = false
        }

        unAnchorButton.setOnClickListener {
            unAnchorModel()
            placeModel()
            textModel.text = generateRandomSentence()
            placeButton.isGone = false
            unAnchorButton.isGone = true
        }

        tooltipBtn.setOnClickListener {
            if (isTooltipVisible) {
                hideTooltip()
            } else {
                showTooltip()
            }

        }


        mapButton.setOnClickListener {
            if (isMapVisible) {
                placeModel()
                hideMap()
                sceneView.addChild(modelNode)
                textModel.isGone = false
            } else {
                unAnchorModel()
                showMap()
                sceneView.removeChild(modelNode)
                textModel.isGone = true

            }
        }




        modelNode = ArModelNode(PlacementMode.INSTANT).apply {
            loadModelGlbAsync(
                glbFileLocation = "models/scream-figure.glb",
                scaleToUnits = 1f,
                centerOrigin = Position(-0.5f)
            ) {
                sceneView.planeRenderer.isVisible = true
                val targetPosition = Position(0f, 0f, 1f)
                lookAt(targetPosition)
            }
        }
        sceneView.addChild(modelNode)

    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
        sceneView.destroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    private fun placeModel() {
        modelNode.anchor()
        sceneView.planeRenderer.isVisible = false


        if (modelNode.parent == null) {
            sceneView.addChild(modelNode)
        }


    }

    private fun unAnchorModel() {
        sceneView.removeChild(modelNode)
        sceneView.planeRenderer.isVisible = true
        placeModel()

    }

    private fun generateRandomSentence(): String {
        val sentences = listOf(
            "Did you know Munchs died in January 1944? " +
                    "He is buried in Vår Frelsers Gravlund (Our Savior's Cemetery) in Oslo.",
            "Did you know that Edvard Munch was born in December 1863 in Løten kommune? " +
                    "The Family relocated to Oslo in 1864.",
            "Did you know Both Munchs mother and sister died from tuberculosis? several of his later paintings deal with the tragic theme," +
                    " including Death in the Sickroom from 1893.",
            "Did you know Munch often painted several versions of his artworks? \n" +
                    "In example Scream exists in four different versions.",
            "Did you know Munchs Scream is norway’s most expensive painting? It was last sold in 2012 for 120.000.000 USD."


        )
        return sentences.random()
    }

    private fun showMap() {
        mapView.visibility = View.VISIBLE
        isMapVisible = true
    }

    private fun hideMap() {
        mapView.visibility = View.GONE
        isMapVisible = false
    }

    private fun hideTooltip() {
        tooltipText.visibility = View.GONE
        isTooltipVisible = false
    }

    private fun showTooltip() {
        tooltipText.visibility = View.VISIBLE
        isTooltipVisible = true


    }

    override fun onMapReady(map: GoogleMap) {
        val screamPerson = BitmapFactory.decodeResource(resources, R.drawable.screamface)
        val munch = BitmapFactory.decodeResource(resources, R.drawable.munchicon)
        val screamIcon = BitmapDescriptorFactory.fromBitmap(screamPerson)
        val munchIcon = BitmapDescriptorFactory.fromBitmap(munch)
        googleMap = map
        val oslo = LatLng(59.9061, 10.7556)
        googleMap.addMarker(
            MarkerOptions()
                .position(oslo)
                .title("Munch museum")
                .icon(munchIcon)
        )
        val personOnTheWay = LatLng(59.907829702, 10.737830382)

        googleMap.addMarker(
            MarkerOptions()
                .position(personOnTheWay)
                .title("person on the way")
                .icon(screamIcon)
        )
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(personOnTheWay, 13f))
        hideMap()
    }
}
