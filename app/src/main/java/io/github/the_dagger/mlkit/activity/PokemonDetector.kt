package io.github.the_dagger.mlkit.activity

import android.graphics.Bitmap
import android.os.Bundle
import android.os.SystemClock
import android.support.design.widget.BottomSheetBehavior
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import com.google.firebase.ml.custom.*
import com.google.firebase.ml.custom.model.FirebaseLocalModelSource
import io.github.the_dagger.mlkit.Pokemon
import io.github.the_dagger.mlkit.R
import io.github.the_dagger.mlkit.adapter.ImageLabelAdapter
import io.github.the_dagger.mlkit.adapter.PokemonAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_image_label.*
import java.nio.ByteBuffer
import java.nio.ByteOrder


val pokeArray: Array<String> = arrayOf("Bulbasaur", "Charmander", "Mewtwo", "Pikachu", "Squirtle")

class SmartReplyActivity : BaseCameraActivity() {
    companion object {
        /** Dimensions of inputs.  */
        const val IMAGE_MEAN = 128
        private const val IMAGE_STD = 128.0f
        const val DIM_IMG_SIZE_X = 224
        const val DIM_IMG_SIZE_Y = 224
        const val DIM_BATCH_SIZE = 1
        const val DIM_PIXEL_SIZE = 3
    }

    private val intValues = IntArray(DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y)
    private lateinit var imgData: ByteBuffer
    private lateinit var fireBaseLocalModelSource: FirebaseLocalModelSource
    private lateinit var fireBaseInterpreter: FirebaseModelInterpreter
    private lateinit var inputOutputOptions: FirebaseModelInputOutputOptions

    lateinit var itemAdapter: PokemonAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBottomSheet(R.layout.layout_image_label)
        imgData = ByteBuffer.allocateDirect(
                4 * DIM_BATCH_SIZE * DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE);
        imgData.order(ByteOrder.nativeOrder())

        rvLabel.layoutManager = LinearLayoutManager(this)
        //Load a local model using the FirebaseLocalModelSource Builder class

        fireBaseLocalModelSource = FirebaseLocalModelSource.Builder("pokedex")
                .setAssetFilePath("pokedex.tflite")
                .build()

        //Registering the model loaded above with the ModelManager Singleton
        FirebaseModelManager.getInstance().registerLocalModelSource(fireBaseLocalModelSource)

        val firebaseModelOptions = FirebaseModelOptions.Builder().setLocalModelName("pokedex").build()
        fireBaseInterpreter = FirebaseModelInterpreter.getInstance(firebaseModelOptions)!!

        inputOutputOptions = FirebaseModelInputOutputOptions.Builder()
                .setInputFormat(0, FirebaseModelDataType.FLOAT32, intArrayOf(1, 224, 224, 3))
                .setOutputFormat(0, FirebaseModelDataType.FLOAT32, intArrayOf(1, 5))
                .build()
    }

    override fun onClick(v: View?) {
        fabProgressCircle.show()
        cameraView.captureImage { cameraKitImage ->
            // Get the Bitmap from the captured shot

            val scaledBitmap = Bitmap.createScaledBitmap(cameraKitImage.bitmap, 224, 224, false)
            getPokemonFromBitmap(scaledBitmap)
            runOnUiThread {
                showPreview()
                imagePreview.setImageBitmap(cameraKitImage.bitmap)
            }
        }
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap?) {
        imgData.rewind()
        bitmap?.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        // Convert the image to floating point.
        var pixel = 0
        for (i in 0 until DIM_IMG_SIZE_X) {
            for (j in 0 until DIM_IMG_SIZE_Y) {
                val `val` = intValues[pixel++]
                imgData.putFloat(((`val` shr 16 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                imgData.putFloat(((`val` shr 8 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                imgData.putFloat(((`val` and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
            }
        }
    }

    private fun getPokemonFromBitmap(bitmap: Bitmap?) {
        convertBitmapToByteBuffer(bitmap)
        val inputs = FirebaseModelInputs.Builder()
                .add(imgData) // add() as many input arrays as your model requires
                .build()

        fireBaseInterpreter.run(inputs, inputOutputOptions)
                ?.addOnSuccessListener {
                    val pokeList = mutableListOf<Pokemon>()
                    /**
                     * Run a foreach loop through the output float array containing the probabilities
                     * corresponding to each label
                     * @see pokeArray to know what labels are supported
                     */
                    it.getOutput<Array<FloatArray>>(0)[0].forEachIndexed { index, fl ->
                        //Only consider a pokemon when the accuracy is more than 30%
                        if (fl > .30)
                            pokeList.add(Pokemon(pokeArray[index], fl))
                    }

                    rvLabel.layoutManager = LinearLayoutManager(this)
                    fabProgressCircle.hide()
                    itemAdapter = PokemonAdapter(pokeList)
                    rvLabel.adapter = itemAdapter
                    sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED)
                }
                ?.addOnFailureListener {
                    it.printStackTrace()
                    fabProgressCircle.hide()
                }
    }

}