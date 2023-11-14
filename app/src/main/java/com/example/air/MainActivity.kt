package com.example.air

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context.LOCATION_SERVICE
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Geocoder.GeocodeListener
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import com.example.air.databinding.ActivityMainBinding
import com.example.air.retrofit.AirQualityResponse
import com.example.air.retrofit.AirQualityService
import com.example.air.retrofit.RetrofitConnection
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    private val PERMISSIONS_REQUEST_CODE = 100  //런타임시필요한요청코드
    var REQUIRED_PERMISSONS = arrayOf(          // 요청할 권한 목록
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    //위치 서비스 요청 시 필요한 런처
    lateinit var getGPSPermissionLauncher: ActivityResultLauncher<Intent>

    lateinit var locationProvider: LocationProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkAllPermissions()
        updateUI()
        setRefreshButton()
    }

    private fun setRefreshButton() {
        binding.btnRefresh.setOnClickListener {
            updateUI()
        }
    }

    private fun updateUI() {
        locationProvider = LocationProvider(this@MainActivity)
        var latitude:Double=locationProvider.getLocationLatitude()
        var longitude: Double=locationProvider.getLocationLongitude()
        if (latitude == 0.0 || longitude == 0.0) {
            latitude = locationProvider.getLocationLatitude()
            longitude = locationProvider.getLocationLongitude()
        }

        if (latitude != 0.0 || longitude != 0.0) {
            // [[[[[[[[[[[[[ 수정된 코드 시작]]]]]]]]]]]]]]]]]
            //1. 현재 위치를 가져오고 UI 업데이트
            //현재 위치를 가져오기
            if (Build.VERSION.SDK_INT < 33) { // SDK 버전이 33보다 큰 경우에만 아래 함수를 씁니다.
                val address = getCurrentAddress(latitude, longitude) //주소가 null 이 아닐 경우 UI 업데이트
                address?.let {
                    binding.tvLocationTitle.text = "${it.thoroughfare}" // 예시: 역삼 1동
                    binding.tvLocationSubtitle.text =
                        "${it.countryName} ${it.adminArea}" // 예시 : 대한민국 서울특별시
                }
            }else { // SDK 버전이 33보다 크거나 같은 경우
                val geocoder = Geocoder(this, Locale.getDefault())
                var address: Address? = null
                val geocodeListener = @RequiresApi(33) object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        // 주소 리스트를 가지고 할 것을 적어주면 됩니다.
                        address =  addresses[0];
                        address?.let {
                            binding.tvLocationTitle.text = "${it.thoroughfare}" // 예시: 역삼 1동
                            binding.tvLocationSubtitle.text =
                                "${it.countryName} ${it.adminArea}" // 예시 : 대한민국 서울특별시
                        }
                    }
                    override fun onError(errorMessage: String?) {
                        address = null
                        Toast.makeText(this@MainActivity, "주소가 발견되지 않았습니다.", Toast.LENGTH_LONG).show()
                    }
                }
                geocoder.getFromLocation(latitude, longitude, 7, geocodeListener)
            }

            // [[[[[[[[[[[[[ 수정된 코드 끝]]]]]]]]]]]]]]]]]


            //2. 현재 미세먼지 농도 가져오고 UI 업데이트
            getAirQualityData(latitude, longitude)

        } else {
            Toast.makeText(
                this@MainActivity, "위도, 경도 정보를 가져올 수 없었습니다. 새로고침을 눌러주세요.", Toast.LENGTH_LONG
            ).show()
        }

    }

    private fun getAirQualityData(latitude: Double, longitude: Double) {
        val retrofitAPI = RetrofitConnection.getInstance().create(
            AirQualityService::class.java
        )

        retrofitAPI.getAirQualityData(
            latitude.toString(),
            longitude.toString(),
            "fdfe5991-9c57-459a-8dca-a451e8af813c"
        ).enqueue(object : Callback<AirQualityResponse> {
            override fun onResponse(
                call: Call<AirQualityResponse>,
                response: Response<AirQualityResponse>
            ) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        this@MainActivity,
                        "최신 정보 업데이트", Toast.LENGTH_SHORT
                    ).show()
                    response.body()?.let { updateAirUI(it) }
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "최신 정보 실패", Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<AirQualityResponse>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

    private fun updateAirUI(airQualityData: AirQualityResponse) {
        val pollutionData = airQualityData.data.current.pollution

        binding.tvCount.text = pollutionData.aqius.toString()

        val dateTime =
            ZonedDateTime.parse(pollutionData.ts).withZoneSameInstant(
                ZoneId.of("Asia/Seoul")
            )
                .toLocalDateTime()
        val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

        binding.tvCheckTime.text = dateTime.format(dateFormatter).toString()

        when (pollutionData.aqius) {
            in 0..50 -> {
                binding.tvTitle.text = "좋음"
                binding.imgBg.setImageResource(R.drawable.bg_good)
            }
            in 51..150 -> {
                binding.tvTitle.text = "보통"
                binding.imgBg.setImageResource(R.drawable.bg_soso)
            }
            in 151..200 -> {
                binding.tvTitle.text = "나쁨"
                binding.imgBg.setImageResource(R.drawable.bg_bad)
            }
            else -> {
                binding.tvTitle.text = "매우 나쁨"
                binding.imgBg.setImageResource(R.drawable.bg_worst)
            }
        }
    }

    fun getCurrentAddress(latitude: Double, longitude: Double): Address? {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses:List<Address>?
        val geocoderListener = @RequiresApi(33) object : Geocoder.GeocodeListener{
            override fun onGeocode(addresses: MutableList<Address>) {
            }
        }
        addresses = try {
            geocoder.getFromLocation(latitude,longitude,7)
        }catch (ioException:IOException){
            Toast.makeText(this,"지오코더 사용 불가",Toast.LENGTH_LONG).show()
            return null
        }catch (illegalArgumentException:IllegalArgumentException){
            Toast.makeText(this,"위도경도 잘못됨",Toast.LENGTH_LONG).show()
            return null
        }
        if(addresses==null||addresses.size==0){
            Toast.makeText(this,"주소 없음",Toast.LENGTH_LONG).show()
            return null
        }
        val address: Address = addresses[0]

        return address
    }

    private fun checkAllPermissions() {
        if (!isLocationServicesAvailable()) {   //GPS
            showDialogForLocationServiceSetting();
        } else {                                //런타임 앱 권한 모두 허용?
            isRunTimePermissionsGranted();
        }
    }


    fun isLocationServicesAvailable(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as
                LocationManager

        return (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
    }

    fun isRunTimePermissionsGranted() {
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(
            this@MainActivity,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
            this@MainActivity,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED
            || hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                REQUIRED_PERMISSONS,
                PERMISSIONS_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE && grantResults.size ==
            REQUIRED_PERMISSONS.size
        ) {
            var checkResult = true

            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    checkResult = false
                    break
                }
            }
            if (checkResult) {
                updateUI()
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "퍼미션 거부됨",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun showDialogForLocationServiceSetting() {
        getGPSPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->

            if (result.resultCode == Activity.RESULT_OK) {
                if (isLocationServicesAvailable()) {
                    isRunTimePermissionsGranted()
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "위치 서비스 불가",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
            }
        }
        val builder: AlertDialog.Builder = AlertDialog.Builder(
            this@MainActivity
        )
        builder.setTitle("위치 서비스 비활성화")
        builder.setMessage("위치 서비스 꺼져있어요. 설정하세요")
        builder.setCancelable(true)//창 바깥 터치 시 닫힘

        builder.setPositiveButton("설정",
            DialogInterface.OnClickListener { dialog, id ->
                val callGPSSettingIntent = Intent(
                    Settings.ACTION_LOCATION_SOURCE_SETTINGS
                )
                getGPSPermissionLauncher.launch(callGPSSettingIntent)
            })
        builder.setNegativeButton("취소",
            DialogInterface.OnClickListener { dialog, id ->
                dialog.cancel()
                Toast.makeText(
                    this@MainActivity,
                    "위치서비스(GPS) 설정해주세요", Toast.LENGTH_SHORT
                ).show()
                finish()
            })
        builder.create().show()
    }
}

