package com.github.florent37.camerafragment.sample

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresPermission
import android.support.v4.app.ActivityCompat
import android.support.v4.view.ViewCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import butterknife.Bind
import butterknife.ButterKnife
import butterknife.OnClick
import com.github.florent37.camerafragment.CameraFragment
import com.github.florent37.camerafragment.CameraFragmentApi
import com.github.florent37.camerafragment.PreviewActivity
import com.github.florent37.camerafragment.configuration.Configuration
import com.github.florent37.camerafragment.listeners.CameraFragmentResultListener
import com.github.florent37.camerafragment.listeners.CameraFragmentStateListener
import java.io.File
import java.util.*

class MainActivityCustoms : AppCompatActivity() {

    @Bind(R.id.settings_view) internal var settingsView: Button? = null
    @Bind(R.id.flash_switch_view) internal var flashSwitchView: Button? = null
    @Bind(R.id.front_back_camera_switcher) internal var cameraSwitchView: Button? = null
    @Bind(R.id.record_button) internal var recordButton: Button? = null
    @Bind(R.id.photo_video_camera_switcher) internal var mediaActionSwitchView: Button? = null

    @Bind(R.id.cameraLayout) internal var cameraLayout: View? = null
    @Bind(R.id.addCameraButton) internal var addCameraButton: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_customs)
        ButterKnife.bind(this)
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    fun addCamera() {
        addCameraButton!!.visibility = View.GONE
        cameraLayout!!.visibility = View.VISIBLE

        val builder = Configuration.Builder()
        builder
                .setCamera(Configuration.CAMERA_FACE_FRONT)
                .setFlashMode(Configuration.FLASH_MODE_ON)
                .setMediaAction(Configuration.MEDIA_ACTION_VIDEO)

        val cameraFragment = CameraFragment.newInstance(builder.build())
        supportFragmentManager.beginTransaction()
                .replace(R.id.content, cameraFragment, FRAGMENT_TAG)
                .commit()

        if (cameraFragment != null) {
            cameraFragment.setResultListener(object : CameraFragmentResultListener {
                override fun onVideoRecorded(filePath: String) {
                    val intent = PreviewActivity.newIntentVideo(this@MainActivityCustoms, filePath)
                    startActivityForResult(intent, REQUEST_PREVIEW_CODE)
                }

                override fun onPhotoTaken(bytes: ByteArray, filePath: String) {
                    val intent = PreviewActivity.newIntentPhoto(this@MainActivityCustoms, filePath)
                    startActivityForResult(intent, REQUEST_PREVIEW_CODE)
                }
            })

            cameraFragment.setStateListener(object : CameraFragmentStateListener {

                override fun onCurrentCameraBack() {
                    cameraSwitchView!!.text = "back"
                }

                override fun onCurrentCameraFront() {
                    cameraSwitchView!!.text = "front"
                }

                override fun onFlashAuto() {
                    flashSwitchView!!.text = "auto"
                }

                override fun onFlashOn() {
                    flashSwitchView!!.text = "on"
                }

                override fun onFlashOff() {
                    flashSwitchView!!.text = "off"
                }

                override fun onCameraSetupForPhoto() {
                    mediaActionSwitchView!!.text = "photo"
                    recordButton!!.text = "take photo"
                    flashSwitchView!!.visibility = View.VISIBLE
                }

                override fun onCameraSetupForVideo() {
                    mediaActionSwitchView!!.text = "video"
                    recordButton!!.text = "capture video"
                    flashSwitchView!!.visibility = View.GONE
                }

                override fun shouldRotateControls(degrees: Int) {
                    ViewCompat.setRotation(cameraSwitchView, degrees.toFloat())
                    ViewCompat.setRotation(mediaActionSwitchView, degrees.toFloat())
                    ViewCompat.setRotation(flashSwitchView, degrees.toFloat())
                }

                override fun onRecordStateVideoReadyForRecord() {
                    recordButton!!.text = "take video"
                }

                override fun onRecordStateVideoInProgress() {
                    recordButton!!.text = "stop"
                }

                override fun onRecordStatePhoto() {
                    recordButton!!.text = "take photo"
                }

                override fun onStopVideoRecord() {
                    settingsView!!.visibility = View.VISIBLE
                }

                override fun onStartVideoRecord(outputFile: File) {}
            })

        }
    }

    @OnClick(R.id.flash_switch_view)
    fun onFlashSwitcClicked() {
        val cameraFragment = cameraFragment
        cameraFragment?.toggleFlashMode()
    }

    @OnClick(R.id.front_back_camera_switcher)
    fun onSwitchCameraClicked() {
        val cameraFragment = cameraFragment
        cameraFragment?.switchCameraTypeFrontBack()
    }

    @OnClick(R.id.record_button)
    fun onRecordButtonClicked() {
        val cameraFragment = cameraFragment
        cameraFragment?.takePhotoOrCaptureVideo(object : CameraFragmentResultListener {
            override fun onVideoRecorded(filePath: String) {

            }

            override fun onPhotoTaken(bytes: ByteArray, filePath: String) {

            }
        },
                "/storage/self/primary",
                "photo0")
    }

    @OnClick(R.id.settings_view)
    fun onSettingsClicked() {
        val cameraFragment = cameraFragment
        cameraFragment?.openSettingDialog()
    }

    @OnClick(R.id.photo_video_camera_switcher)
    fun onMediaActionSwitchClicked() {
        val cameraFragment = cameraFragment
        cameraFragment?.switchActionPhotoVideo()
    }

    @OnClick(R.id.addCameraButton)
    fun onAddCameraClicked() {
        if (Build.VERSION.SDK_INT > 15) {
            val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)

            val permissionsToRequest = ArrayList<String>()
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(permission)
                }
            }
            if (!permissionsToRequest.isEmpty()) {
                ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), REQUEST_CAMERA_PERMISSIONS)
            } else
                addCamera()
        } else {
            addCamera()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.size != 0) {
            addCamera()
        }
    }

    private val cameraFragment: CameraFragmentApi?
        get() = supportFragmentManager.findFragmentByTag(FRAGMENT_TAG) as CameraFragmentApi

    companion object {

        private val REQUEST_CAMERA_PERMISSIONS = 931
        private val REQUEST_PREVIEW_CODE = 1001

        val FRAGMENT_TAG = "camera"
    }
}
