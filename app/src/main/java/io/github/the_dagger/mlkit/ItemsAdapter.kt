package io.github.the_dagger.mlkit

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.ml.vision.cloud.label.FirebaseVisionCloudLabel
import com.google.firebase.ml.vision.label.FirebaseVisionLabel
import kotlinx.android.synthetic.main.item_row.view.*

class ItemsAdapter(private val firebaseVisionList: List<Any>) : RecyclerView.Adapter<ItemsAdapter.ItemHolder>() {

    lateinit var context: Context

    class ItemHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {

    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        //Change the cast to FirebaseVisionLabel when using on device labeling
        val currentItem = firebaseVisionList[position] as FirebaseVisionCloudLabel
        if (currentItem.confidence > .70)
            holder.itemView.itemAccuracy.setTextColor(ContextCompat.getColor(context, R.color.green))
        else if (currentItem.confidence < .30)
            holder.itemView.itemAccuracy.setTextColor(ContextCompat.getColor(context, R.color.red))
        else
            holder.itemView.itemAccuracy.setTextColor(ContextCompat.getColor(context, R.color.orange))
        holder.itemView.itemName.text = currentItem.label
        holder.itemView.itemAccuracy.text = "Accuracy : ${(currentItem.confidence * 100).toInt()}%"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        context = parent.context
        return ItemHolder(LayoutInflater.from(context).inflate(R.layout.item_row, parent, false))
    }

    override fun getItemCount() = firebaseVisionList.size

}