package io.github.the_dagger.mlkit.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.firebase.ml.custom.FirebaseModelInterpreter
import com.google.firebase.ml.custom.FirebaseModelManager
import com.google.firebase.ml.custom.FirebaseModelOptions
import com.google.firebase.ml.custom.model.FirebaseLocalModelSource
import com.google.firebase.ml.custom.FirebaseModelDataType
import com.google.firebase.ml.custom.FirebaseModelInputOutputOptions
import kotlinx.android.synthetic.main.activity_smart_reply.*
import com.google.firebase.ml.custom.FirebaseModelInputs
import io.github.the_dagger.mlkit.R
import io.github.the_dagger.mlkit.R.id.btnSend
import io.github.the_dagger.mlkit.R.id.etText
import java.nio.charset.Charset


class SmartReplyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_smart_reply)
        //Load a local model using the FirebaseLocalModelSource Builder class
        val firebaseLocalModelSource = FirebaseLocalModelSource.Builder("smartreply")
                .setAssetFilePath("smartreply.tflite")
                .build()

        //Registering the model loaded above with the ModelManager Singleton
        FirebaseModelManager.getInstance().registerLocalModelSource(firebaseLocalModelSource)

        val firebaseModelOptions = FirebaseModelOptions.Builder().setLocalModelName("smartreply").build()

        val firebaseInterpreter = FirebaseModelInterpreter.getInstance(firebaseModelOptions)



        btnSend.setOnClickListener {
            val inputOutputOptions = FirebaseModelInputOutputOptions.Builder()
                    .setInputFormat(0, FirebaseModelDataType.BYTE, intArrayOf(etText.text.length))
                    .setOutputFormat(0, FirebaseModelDataType.BYTE, intArrayOf(16))
                    .build()


            val inputs = FirebaseModelInputs.Builder()
                    .add(etText.text.toString().toByteArray())  // add() as many input arrays as your model requires
                    .build()

            firebaseInterpreter
                    ?.run(inputs, inputOutputOptions)
                    ?.addOnSuccessListener { Log.e("TAG", it.toString()) }
                    ?.addOnFailureListener { it.printStackTrace() }

        }

    }


}