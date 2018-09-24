package io.github.the_dagger.mlkit.adapter

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.github.the_dagger.mlkit.model.Pokemon
import io.github.the_dagger.mlkit.R
import kotlinx.android.synthetic.main.item_row.view.*

class PokemonAdapter(private val pokeList: List<Pokemon>) : RecyclerView.Adapter<PokemonAdapter.PokeHolder>() {

    private lateinit var context: Context

    class PokeHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokeHolder {
        context = parent.context
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_row, parent, false)
        return PokeHolder(view)
    }

    override fun getItemCount() = pokeList.size

    override fun onBindViewHolder(holder: PokeHolder, position: Int) {

        val currentItem = pokeList[position]

        when {
            currentItem.accuracy > .70 -> holder.itemView.itemAccuracy.setTextColor(ContextCompat.getColor(context, R.color.green))
            currentItem.accuracy < .30 -> holder.itemView.itemAccuracy.setTextColor(ContextCompat.getColor(context, R.color.red))
            else -> holder.itemView.itemAccuracy.setTextColor(ContextCompat.getColor(context, R.color.orange))
        }
        holder.itemView.itemName.text = currentItem.name
        holder.itemView.itemAccuracy.text = "Probability : ${(currentItem.accuracy * 100).toInt()}%"
    }

}