package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.Constants
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.udacity.project4.BuildConfig

private const val TAG = "SaveReminderFragment"

class SaveReminderFragment : BaseFragment() {
    private var permissions: Boolean = false
    private lateinit var reminderDataItem: ReminderDataItem
    private lateinit var geofencing: GeofencingClient
    private val runningQOrLaterCheck = android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.Q

    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding

    //Pending intent to handle geofence trigger
    private val geofencePendingIntent: PendingIntent by lazy {
        val intentGeofence = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        intentGeofence.action = Constants.ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(
            requireContext(),
            0,
            intentGeofence,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        geofencing = LocationServices.getGeofencingClient(requireActivity())

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }
        binding.saveReminder.setOnClickListener {

            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value

            reminderDataItem = ReminderDataItem(title, description, location, latitude, longitude)

            if (_viewModel.validateEnteredData(reminderDataItem)) {
                val foregroundLocation = (
                        PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
                        ))


                val backgroundPermission =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                            requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
                    } else {
                        true
                    }

                permissions = foregroundLocation && backgroundPermission
                if (permissions) {
                    checkDeviceLocationAndstartGeofence()
                } else {
                    requestForegroundandBackgroundPermission()
                }

            }
        }
    }


    private fun requestForegroundandBackgroundPermission() {
        if (permissions)
            return
        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val requestCode = when {
            runningQOrLaterCheck -> {
                permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                Constants.REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            }
            else -> Constants.REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        }
        requestPermissions(permissionsArray, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.REQUEST_TURN_DEVICE_LOCATION_ON) {
            if (resultCode == Activity.RESULT_OK) {
                addGeoFenceRemainder()
            } else {
                checkDeviceLocationAndstartGeofence(false)
            }

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {

        if (grantResults.isNotEmpty() &&
            grantResults[Constants.LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_GRANTED ||
            (requestCode == Constants.REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE &&
                    grantResults[Constants.BACKGROUND_LOCATION_PERMISSION_INDEX] ==
                    PackageManager.PERMISSION_GRANTED) &&
            grantResults[Constants.LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_GRANTED
        ) {
            checkDeviceLocationAndstartGeofence()
        } else {
            Snackbar.make(
                binding.saveLayout,
                R.string.permission_denied_explanation,
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.settings) {
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()
        }

    }

    private fun checkDeviceLocationAndstartGeofence(resolve:Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val settings = LocationServices.getSettingsClient(requireActivity())
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val locationSettingsResponse = settings.checkLocationSettings(builder.build())

        locationSettingsResponse.addOnFailureListener {
            if (it is ResolvableApiException && resolve) {
                try {
                    startIntentSenderForResult(
                        it.resolution.intentSender, Constants.REQUEST_TURN_DEVICE_LOCATION_ON,
                        null, 0, 0, 0, null
                    )

                } catch (Ex: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error getting location settings resolution: " + Ex.message)
                }
            } else {
                Snackbar.make(
                    requireView(),
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationAndstartGeofence()
                }.show()
            }

        }

        locationSettingsResponse.addOnCompleteListener {
            if (it.isSuccessful) {
                Log.i("Successful Response:", "$it")
                addGeoFenceRemainder()
            }
        }
    }

    private fun addGeoFenceRemainder() {
        val currentGeofenceItem = reminderDataItem

        val myGeofence = Geofence.Builder()
            .setRequestId(currentGeofenceItem.id)
            .setCircularRegion(
                currentGeofenceItem.latitude!!,
                currentGeofenceItem.longitude!!,
                120f
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        val geofenceRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(myGeofence)
            .build()

        geofencing.addGeofences(geofenceRequest, geofencePendingIntent)?.run {
            addOnSuccessListener {
                _viewModel.validateAndSaveReminder(reminderDataItem)
            }
            addOnFailureListener {
                Toast.makeText(
                    requireContext(),
                    "Error occurred! Can't save the Geofence ",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }
}
