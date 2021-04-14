package com.example.surpa.googlemapsactivity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // SupportMapFragmentを取得し、mapを使用する準備ができたら通知を受け取る
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        // mapFragmentオブジェクトのgetMapAsync()メソッドにthisを渡し、マップの準備ができた時のコールバックに指定する
        // これでマップの準備ができた時にonMapRready()メソッドが呼び出されるようになる
        mapFragment.getMapAsync(this)
    }

    /**
     * 利用可能になったらmapを操作する
     * callbackはmapを使用する準備ができたときにトリガーされる
     * ここでマーカーや線を追加したり、リスナーを追加したり、カメラを移動したりできる
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // シドニーにマーカーを追加しカメラを移動する
        // LatLng型のオブジェクトを生成し、GoogleMap型のオブジェクトであるmMapのaddMarker()メソッドでマップにピンを立て、moveCamera()メソッドで視点をSydneyに移している
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }
}
