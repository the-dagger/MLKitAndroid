package io.github.the_dagger.mlkit.activity

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector
import com.wonderkiln.camerakit.CameraKit
import io.github.the_dagger.mlkit.activity.BaseCameraActivity
import kotlinx.android.synthetic.main.activity_main.*

class FaceDetectionActivity : BaseCameraActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraView.facing = CameraKit.Constants.FACING_FRONT
    }

    private fun getFaceDetails(bitmap: Bitmap) {
//        val options : FirebaseVisionFaceDetectorOptions = FirebaseVisionFaceDetectorOptions.Builder().build()
        val image : FirebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap)
        val faceDetector : FirebaseVisionFaceDetector = FirebaseVision.getInstance().visionFaceDetector

        faceDetector.detectInImage(image)
                .addOnSuccessListener {
                    fabProgressCircle.hide()
                    for (face in it){

                    }
                }
                .addOnFailureListener{

                }
    }

    override fun onClick(v: View?) {
        fabProgressCircle.show()
        cameraView.captureImage { cameraKitImage ->
            // Get the Bitmap from the captured shot
            getFaceDetails(cameraKitImage.bitmap)
            runOnUiThread {
                showPreview()
                imagePreview.setImageBitmap(cameraKitImage.bitmap)
            }
        }
    }
}