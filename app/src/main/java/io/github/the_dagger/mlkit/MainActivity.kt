package io.github.the_dagger.mlkit

import android.Manifest
import android.content.Context
import android.hardware.camera2.CameraManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.view.SurfaceView
import android.view.TextureView
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.params.StreamConfigurationMap
import android.util.Size
import android.content.pm.PackageManager




lateinit var cameraManager: CameraManager
const val facingBack = CameraCharacteristics.LENS_FACING_BACK
lateinit var actualCameraId : String

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ActivityCompat.requestPermissions(this, Array(1) { Manifest.permission.CAMERA }, 123)
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val surfaceTextureListener = object : TextureView.SurfaceTextureListener {

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) = Unit

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) = Unit

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?) = false

            override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {

            }
        }
    }

    fun setupCamera() {
        try {
            for (cameraId: String in cameraManager.cameraIdList) {
                val cameraCharacteristics: CameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId)
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == facingBack){
                    val streamConfigurationMap = cameraCharacteristics.get(
                            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    val previewSize: Size = streamConfigurationMap.getOutputSizes(SurfaceTexture::class.java)[0]
                    actualCameraId = cameraId
                }
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun openCamera() {
        try {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                cameraManager.openCamera(cameraId, stateCallback, backgroundHandler)
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

    }
}
