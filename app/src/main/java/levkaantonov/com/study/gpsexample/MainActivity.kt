package levkaantonov.com.study.gpsexample

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import levkaantonov.com.study.gpsexample.databinding.ActivityMainBinding
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {
    private val FINE_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION
    private val COARSE_LOCATION = android.Manifest.permission.ACCESS_COARSE_LOCATION
    private val PERMISSION_REQUEST = 200

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private var _locationManager: LocationManager? = null
    private val locationManager get() = checkNotNull(_locationManager)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        _locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        checkPermission(applicationContext, FINE_LOCATION)
        checkPermission(applicationContext, COARSE_LOCATION)
        checkLocationEnableOrNot()
        getLocation()
    }

    private fun getLocation() {
        try {
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                500,
                5F,
                object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        try {
                            val geocoder = Geocoder(applicationContext, Locale.ENGLISH)
                            val addresses =
                                geocoder.getFromLocation(location.latitude, location.longitude, 1)
                            binding.tvCountryValue.text = addresses[0].countryName
                            binding.tvStateValue.text = addresses[0].adminArea
                            binding.tvCityValue.text = addresses[0].locality
                            binding.tvCodeValue.text = addresses[0].postalCode
                            binding.tvAddressValue.text = addresses[0].getAddressLine(0)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }

                    override fun onProviderDisabled(provider: String) {}
                })
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun checkLocationEnableOrNot() {
        var gpsEnable = false
        var networkEnable = false

        try {
            gpsEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            networkEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (!gpsEnable && !networkEnable) {
            AlertDialog.Builder(this)
                .setTitle("Enable GPS Service")
                .setCancelable(false)
                .setPositiveButton(
                    "Enable"
                ) { _, _ ->
                    val startSettingsActivityIntent =
                        Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(startSettingsActivityIntent)
                }.setNegativeButton("Cancel", null)
                .show()
        }
    }


    private fun checkPermission(context: Context, permission: String): Boolean {
        return if (
            Build.VERSION.SDK_INT >= 23 &&
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), PERMISSION_REQUEST)
            false
        } else {
            true
        }
    }

}