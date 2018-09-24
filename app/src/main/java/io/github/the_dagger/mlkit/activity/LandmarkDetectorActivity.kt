package io.github.the_dagger.mlkit.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import io.github.the_dagger.mlkit.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_landmark.*
import pl.aprilapps.easyphotopicker.DefaultCallback
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File


class LandmarkDetectorActivity : BaseCameraActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBottomSheet(R.layout.layout_landmark)
        cameraView.visibility = View.GONE
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, Array<String>(1) { Manifest.permission.WRITE_EXTERNAL_STORAGE }, 12345)
            fab_take_photo.setOnClickListener(null)
        } else {
            fab_take_photo.setOnClickListener(this)
        }
    }

    override fun onClick(v: View?) {
        //onClick attribute for the FloatingActionButton
        startIntentForPicker()
    }

    private fun startIntentForPicker() {
        EasyImage.openGallery(this, 0)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 12345) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            //The library requires write access to the External Storage,
            //so ensure that the permission is granted
                fab_take_photo.setOnClickListener(this)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            EasyImage.handleActivityResult(requestCode, resultCode, data, this, object : DefaultCallback() {

                override fun onImagePicked(imageFile: File?, source: EasyImage.ImageSource?, type: Int) {
                    val bitmap = BitmapFactory.decodeFile(imageFile?.path)
                    getLandmarkFromCloud(bitmap)
                    imagePreview.setImageBitmap(bitmap)
                    framePreview.visibility = View.VISIBLE
                    btnRetry.visibility = View.GONE
                }

                override fun onImagePickerError(e: Exception?, source: EasyImage.ImageSource?, type: Int) {
                    //Some error handling since no image was picked
                }
            })
        }
    }

    private fun getLandmarkFromCloud(bitmap: Bitmap) {
        val image = FirebaseVisionImage.fromBitmap(bitmap)
        val detector = FirebaseVision.getInstance()
                .visionCloudLandmarkDetector

        detector.detectInImage(image)
                .addOnCompleteListener {
                    Log.e("TAG", "completed")
                    for (firebaseVisionLandmarks in it.result) {
                        val landmark = firebaseVisionLandmarks.landmark
                        tvLocationName.text = landmark
                        for (location in firebaseVisionLandmarks.locations) {
                            val lat = location.latitude
                            val long = location.longitude
                            tvLatitude.text = lat.toString()
                            tvLongitude.text = long.toString()
                        }
                        tvAccuracy.text = (firebaseVisionLandmarks.confidence * 100).toInt().toString()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
                }
                .addOnCompleteListener {
                    sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
    }

}