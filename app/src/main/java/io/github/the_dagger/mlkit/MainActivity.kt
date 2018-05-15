package io.github.the_dagger.mlkit

import android.graphics.Bitmap
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Toast
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_bottom_sheet.*


class MainActivity : AppCompatActivity() {

    private var itemsList: ArrayList<Any> = ArrayList()
    private lateinit var itemAdapter: ItemAdapter

    private lateinit var sheetBehavior: BottomSheetBehavior<*>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rvLabel.layoutManager = LinearLayoutManager(this)
        btnRetry.setOnClickListener {
            if (cameraView.visibility == View.VISIBLE) showPreview() else hidePreview()
        }

        sheetBehavior = BottomSheetBehavior.from(bottomLayout)
        sheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                fab_take_photo.animate().scaleX(1 - slideOffset).scaleY(1 - slideOffset).setDuration(0).start()
            }
        })

        fab_take_photo.setOnClickListener {
            fabProgressCircle.show()
            cameraView.captureImage { cameraKitImage ->
                // Get the Bitmap from the captured shot
                getLabelsFromClod(cameraKitImage.bitmap)
                runOnUiThread {
                    showPreview()
                    imagePreview.setImageBitmap(cameraKitImage.bitmap)
                }
            }
        }
    }

    private fun getLabelsFromDevice(bitmap: Bitmap) {
        val image = FirebaseVisionImage.fromBitmap(bitmap)
        val detector = FirebaseVision.getInstance().visionLabelDetector
        itemsList.clear()
        detector.detectInImage(image)
                .addOnSuccessListener {
                    // Task completed successfully
                    fabProgressCircle.hide()
                    itemsList.addAll(it)
                    itemAdapter = ItemAdapter(itemsList,false)
                    rvLabel.adapter = itemAdapter
                    sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED)
                }
                .addOnFailureListener {
                    // Task failed with an exception
                    fabProgressCircle.hide()
                    Toast.makeText(baseContext,"Sorry, something went wrong!",Toast.LENGTH_SHORT).show()
                }
    }

    private fun getLabelsFromClod(bitmap: Bitmap) {
        val image = FirebaseVisionImage.fromBitmap(bitmap)
        val detector = FirebaseVision.getInstance()
                .visionCloudLabelDetector
        itemsList.clear()
        detector.detectInImage(image)
                .addOnSuccessListener {
                    // Task completed successfully
                    fabProgressCircle.hide()
                    itemsList.addAll(it)
                    itemAdapter = ItemAdapter(itemsList,true)
                    rvLabel.adapter = itemAdapter
                    sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED)
                }
                .addOnFailureListener {
                    // Task failed with an exception
                    fabProgressCircle.hide()
                    Toast.makeText(baseContext,"Sorry, something went wrong!",Toast.LENGTH_SHORT).show()
                }
    }

    override fun onResume() {
        super.onResume()
        cameraView.start()
    }

    override fun onPause() {
        cameraView.stop()
        super.onPause()
    }

    private fun showPreview() {
        framePreview.visibility = View.VISIBLE
        cameraView.visibility = View.GONE
    }

    private fun hidePreview() {
        framePreview.visibility = View.GONE
        cameraView.visibility = View.VISIBLE
    }

}
