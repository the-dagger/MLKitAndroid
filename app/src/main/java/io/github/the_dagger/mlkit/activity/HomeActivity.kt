package io.github.the_dagger.mlkit.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import io.github.the_dagger.mlkit.adapter.HomeAdapter
import io.github.the_dagger.mlkit.PojoApi
import io.github.the_dagger.mlkit.R
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {

    private val apiList by lazy {
       ArrayList<PojoApi>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        apiList.add(PojoApi(R.drawable.image_labelling, getString(R.string.title_labelling), getString(R.string.desc_labelling), 0))
        apiList.add(PojoApi(R.drawable.text_recognition, getString(R.string.title_text), getString(R.string.desc_text), 1))
        apiList.add(PojoApi(R.drawable.barcode_scanning, getString(R.string.title_barcode), getString(R.string.desc_barcode), 2))
        apiList.add(PojoApi(R.drawable.face_detection, getString(R.string.title_face), getString(R.string.desc_face), 3))
        apiList.add(PojoApi(R.drawable.landmark_identification, getString(R.string.title_landmark), getString(R.string.desc_landmark), 4))

        rvHome.layoutManager = LinearLayoutManager(this)
        rvHome.adapter = HomeAdapter(apiList)
    }
}
