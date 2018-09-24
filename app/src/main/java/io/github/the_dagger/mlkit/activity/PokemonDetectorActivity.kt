package io.github.the_dagger.mlkit.activity

import android.graphics.Bitmap
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.google.firebase.ml.custom.*
import com.google.firebase.ml.custom.model.FirebaseLocalModelSource
import io.github.the_dagger.mlkit.model.Pokemon
import io.github.the_dagger.mlkit.R
import io.github.the_dagger.mlkit.adapter.PokemonAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_image_label.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import com.google.firebase.ml.custom.FirebaseModelManager
import com.google.firebase.ml.custom.model.FirebaseCloudModelSource

class PokemonDetectorActivity : BaseCameraActivity() {
    private val pokeArray: Array<String> = arrayOf("abra", "aerodactyl", "alakazam", "arbok", "arcanine", "articuno", "beedrill", "bellsprout",
            "blastoise", "bulbasaur", "butterfree", "caterpie", "chansey", "charizard", "charmander", "charmeleon", "clefable", "clefairy", "cloyster", "cubone", "dewgong",
            "diglett", "ditto", "dodrio", "doduo", "dragonair", "dragonite", "dratini", "drowzee", "dugtrio", "eevee", "ekans", "electabuzz",
            "electrode", "exeggcute", "exeggutor", "farfetchd", "fearow", "flareon", "gastly", "gengar", "geodude", "gloom",
            "golbat", "goldeen", "golduck", "golem", "graveler", "grimer", "growlithe", "gyarados", "haunter", "hitmonchan",
            "hitmonlee", "horsea", "hypno", "ivysaur", "jigglypuff", "jolteon", "jynx", "kabuto",
            "kabutops", "kadabra", "kakuna", "kangaskhan", "kingler", "koffing", "krabby", "lapras", "lickitung", "machamp",
            "machoke", "machop", "magikarp", "magmar", "magnemite", "magneton", "mankey", "marowak", "meowth", "metapod",
            "mew", "mewtwo", "moltres", "mrmime", "muk", "nidoking", "nidoqueen", "nidorina", "nidorino", "ninetales",
            "oddish", "omanyte", "omastar", "onix", "paras", "parasect", "persian", "pidgeot", "pidgeotto", "pidgey",
            "pikachu", "pinsir", "poliwag", "poliwhirl", "poliwrath", "ponyta", "porygon", "primeape", "psyduck", "raichu",
            "rapidash", "raticate", "rattata", "rhydon", "rhyhorn", "sandshrew", "sandslash", "scyther", "seadra",
            "seaking", "seel", "shellder", "slowbro", "slowpoke", "snorlax", "spearow", "squirtle", "starmie", "staryu",
            "tangela", "tauros", "tentacool", "tentacruel", "vaporeon", "venomoth", "venonat", "venusaur", "victreebel",
            "vileplume", "voltorb", "vulpix", "wartortle", "weedle", "weepinbell", "weezing", "wigglytuff", "zapdos", "zubat")

    companion object {
        /** Dimensions of inputs.  */
        const val DIM_IMG_SIZE_X = 224
        const val DIM_IMG_SIZE_Y = 224
        const val DIM_BATCH_SIZE = 1
        const val DIM_PIXEL_SIZE = 3
        const val IMAGE_MEAN = 128
        private const val IMAGE_STD = 128.0f
    }

    private val intValues = IntArray(DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y)
    private lateinit var imgData: ByteBuffer
    private lateinit var fireBaseInterpreter: FirebaseModelInterpreter
    private lateinit var inputOutputOptions: FirebaseModelInputOutputOptions

    private lateinit var itemAdapter: PokemonAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBottomSheet(R.layout.layout_image_label)
        imgData = ByteBuffer.allocateDirect(
                4 * DIM_BATCH_SIZE * DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE);
        imgData.order(ByteOrder.nativeOrder())

        rvLabel.layoutManager = LinearLayoutManager(this)

        //Load a cloud model using the FirebaseCloudModelSource Builder class
        val cloudSource = FirebaseCloudModelSource.Builder("pokedex")
                .enableModelUpdates(true)
                .build()

        //Registering the cloud model loaded above with the ModelManager Singleton
        FirebaseModelManager.getInstance().registerCloudModelSource(cloudSource)

        //Load a local model using the FirebaseLocalModelSource Builder class
        val fireBaseLocalModelSource = FirebaseLocalModelSource.Builder("pokedex")
                .setAssetFilePath("pokedex.tflite")
                .build()

        //Registering the model loaded above with the ModelManager Singleton
        FirebaseModelManager.getInstance().registerLocalModelSource(fireBaseLocalModelSource)

        val firebaseModelOptions = FirebaseModelOptions.Builder()
                .setLocalModelName("pokedex")
                .setCloudModelName("pokedex")
                .build()

        fireBaseInterpreter = FirebaseModelInterpreter.getInstance(firebaseModelOptions)!!

        //Input and Output options for the model
        inputOutputOptions = FirebaseModelInputOutputOptions.Builder()
                .setInputFormat(0, FirebaseModelDataType.FLOAT32, intArrayOf(1, 224, 224, 3))
                .setOutputFormat(0, FirebaseModelDataType.FLOAT32, intArrayOf(1, 149))
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

    private fun convertBitmapToByteBuffer(bitmap: Bitmap?): ByteBuffer {
        //Clear the Bytebuffer for a new image
        imgData.rewind()
        bitmap?.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        // Convert the image to floating point.
        var pixel = 0
        for (i in 0 until DIM_IMG_SIZE_X) {
            for (j in 0 until DIM_IMG_SIZE_Y) {
                val currPixel = intValues[pixel++]
                imgData.putFloat(((currPixel shr 16 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                imgData.putFloat(((currPixel shr 8 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                imgData.putFloat(((currPixel and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
            }
        }
        return imgData
    }

    private fun getPokemonFromBitmap(bitmap: Bitmap?) {
        //Creating a FirebaseModelInput object that takes in the ByteBuffer as an input
        val inputs = FirebaseModelInputs.Builder()
                .add(convertBitmapToByteBuffer(bitmap))
                .build()

        //Provide the firebaseModelInput to the FirebaseInterpreter
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
                        if (fl > .20)
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