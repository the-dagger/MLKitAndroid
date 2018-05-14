package io.github.the_dagger.mlkit

import android.graphics.Bitmap
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.wonderkiln.camerakit.CameraKitImage
import android.support.design.widget.BottomSheetBehavior
import android.support.v7.widget.LinearLayoutManager
import com.google.firebase.ml.vision.label.FirebaseVisionLabel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_bottom_sheet.*
import android.view.View
import android.R.attr.bitmap
import com.google.firebase.ml.vision.cloud.label.FirebaseVisionCloudLabelDetector
import android.support.annotation.NonNull
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.ml.vision.cloud.label.FirebaseVisionCloudLabel
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task


class MainActivity : AppCompatActivity() {

    private var itemsList: ArrayList<Any> = ArrayList()

    private val itemsAdapter: ItemsAdapter by lazy {
        ItemsAdapter(itemsList)
    }

    private lateinit var sheetBehavior: BottomSheetBehavior<*>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val floatingActionButton = findViewById<FloatingActionButton>(R.id.fab_take_photo)

        rvLabel.layoutManager = LinearLayoutManager(this)
        rvLabel.adapter = itemsAdapter

        sheetBehavior = BottomSheetBehavior.from(bottomLayout)

        sheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                fab_take_photo.animate().scaleX(1 - slideOffset).scaleY(1 - slideOffset).setDuration(0).start()
            }
        })

        floatingActionButton.setOnClickListener {
            fabProgressCircle.show()
            camera.captureImage { cameraKitImage ->
                getLabelsFromClod(captureImage(cameraKitImage))
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
                    itemsAdapter.notifyDataSetChanged()
                    sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
                .addOnFailureListener {
                    // Task failed with an exception
                    fabProgressCircle.hide()
                }
    }

    private fun getLabelsFromClod(bitmap: Bitmap) {
        val image = FirebaseVisionImage.fromBitmap(bitmap)
        val detector = FirebaseVision.getInstance()
                .visionCloudLabelDetector
        itemsList.clear()
        val result = detector.detectInImage(image)
                .addOnSuccessListener {
                    // Task completed successfully
                    fabProgressCircle.hide()
                    itemsList.addAll(it)
                    itemsAdapter.notifyDataSetChanged()
                    sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
                .addOnFailureListener {
                    // Task failed with an exception
                    fabProgressCircle.hide()
                }
    }

    override fun onResume() {
        super.onResume()
        camera.start()
    }

    override fun onPause() {
        camera.stop()
        super.onPause()
    }


    private fun captureImage(cameraKitImage: CameraKitImage): Bitmap {
        return cameraKitImage.bitmap
    }

}
