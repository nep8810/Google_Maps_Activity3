package com.example.surpa.googlemapsactivity

import  android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.text.format.DateFormat
import android.view.WindowManager
import android.widget.Toast
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback{
    private lateinit var mMap: GoogleMap
    private val MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 1

    // lateint修飾子をつけて、FusedLocationProviderClientクラスのオブジェクトと位置情報を受け取るためのLocationオブジェクトを宣言する
    private  lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var lastLocation:Location
    private var locationCallback : LocationCallback? = null
    private lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // 画面をスリープにしない
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        realm = Realm.getDefaultInstance()
        memoBtn.setOnClickListener{
            val intent = Intent(this, AddActivity::class.java)  // AddActivity:Memoを登録するActivity
            intent.putExtra("lat", lastLocation.latitude)
            intent.putExtra("lng", lastLocation.longitude)
            startActivity(intent)
        }
    }

    override  fun onStart(){
        super.onStart()
        if (::mMap.isInitialized){
            putsMarkers()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        checkPermission()
    }

    private  fun checkPermission(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        == PackageManager.PERMISSION_GRANTED){
            myLocationEnable()
        }else{
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)){
                // 許可を求め、拒否された場合
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION)
        }else{
            // まだ許可を求めていない場合
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION->{
                if (permissions.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    // 許可された
                    myLocationEnable()
                }else{
                    showToast("現在位置は表示できません")
                }
            }
        }
    }

    // リアルタイムで位置情報を受け取るためにリスナーを仕掛けるのは、myLocationEnable()
    private fun myLocationEnable(){
        // 赤波線でエラーが表示されるため
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        == PackageManager.PERMISSION_GRANTED){
            mMap.isMyLocationEnabled = true
            // LocationRequestオブジェクトに位置情報を受け取るためのパラメータを設定
            val locationRequest = LocationRequest().apply {
                interval = 10000  // 最多の更新時間(ミリ秒)
                fastestInterval = 5000  // 最短の更新時間(ミリ秒)
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY  // 最も高精度に設定
            }
            locationCallback = object : LocationCallback(){
                // onLocationResult()は位置情報を取得したときに呼び出されるメソッド
                override fun onLocationResult(locationResult:LocationResult?) {
                    // 位置情報をlastLocationに取得し、LatLng型にしてcurrentLatLngに代入→それをマップのmoveCamera()メソッドの引数に指定することでマップを動かす
                    if (locationResult?.lastLocation != null){
                        lastLocation = locationResult.lastLocation
                        val currentLatLng = LatLng(lastLocation.latitude,lastLocation.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng))
                        textView.text = "Lat:${lastLocation.longitude}, Lng:${lastLocation.longitude}"
                    }
                }
            }
            // requestLocationUpdates()メソッドにlocationRequest,locationCallbackを与えて実行することで、リアルタイムに位置情報を取得できるようになる
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback,null)
            putsMarkers()
        }
    }

    private fun showToast(msg:String){
        val toast = Toast.makeText(this, msg, Toast.LENGTH_LONG)
        toast.show()
    }

    private fun putsMarkers(){
        mMap.clear()
        val realmResults = realm.where(Memo::class.java)
            .findAll()
        for (memo: Memo in realmResults){
            val latLng = LatLng(memo.lat, memo.lng)
            val marker = MarkerOptions()
                .position(latLng)  // 場所
                .title(DateFormat.format("yyyy/MM/dd kk:mm", memo.dateTime).toString())
                .snippet(memo.memo)
                .draggable(false)  // マーカーはドラッグ不可
            // マーカーの外観
            val descriptor = BitmapDescriptorFactory.defaultMarker(
                BitmapDescriptorFactory.HUE_ORANGE)
            marker.icon(descriptor)

            // マーカー追加
            mMap.addMarker(marker)
        }
    }

    override fun onPause() {
        super.onPause()
        if (locationCallback != null){
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}
