package io.github.the_dagger.mlkit.activity

import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.CoordinatorLayout
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.View
import io.github.the_dagger.mlkit.R
import kotlinx.android.synthetic.main.activity_main.*

abstract class BaseCameraActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var sheetBehavior: BottomSheetBehavior<*>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnRetry.setOnClickListener {
            if (cameraView.visibility == View.VISIBLE) showPreview() else hidePreview()
        }
        fab_take_photo.setOnClickListener(this)
    }

    fun setupBottomSheet(@LayoutRes id : Int){
        //Using a ViewStub since changing the layout of an <include> tag dynamically wasn't possible
        stubView.layoutResource = id
        val inflatedView = stubView.inflate()
        //Set layout parameters for the inflated bottomsheet
        val lparam = inflatedView.layoutParams as CoordinatorLayout.LayoutParams
        lparam.behavior = BottomSheetBehavior<View>()
        inflatedView.layoutParams = lparam
        sheetBehavior = BottomSheetBehavior.from(inflatedView)
        sheetBehavior.peekHeight = 224
        //Anchor the FAB to the end of inflated bottom sheet
        val lp = fabProgressCircle.layoutParams as CoordinatorLayout.LayoutParams
        lp.anchorId = inflatedView.id
        lp.anchorGravity = Gravity.END
        fabProgressCircle.layoutParams = lp
        //Hide the fab as bottomSheet is expanded
        sheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                fab_take_photo.animate().scaleX(1 - slideOffset).scaleY(1 - slideOffset).setDuration(0).start()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        cameraView.start()
    }

    override fun onPause() {
        cameraView.stop()
        super.onPause()
    }

    protected fun showPreview() {
        framePreview.visibility = View.VISIBLE
        cameraView.visibility = View.GONE
    }

    protected fun hidePreview() {
        framePreview.visibility = View.GONE
        cameraView.visibility = View.VISIBLE
    }
}