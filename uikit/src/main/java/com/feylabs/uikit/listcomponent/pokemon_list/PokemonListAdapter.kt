package com.feylabs.uikit.listcomponent.pokemon_list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.feylabs.uikit.listcomponent.movie_genre.MovieGenreItemAdapter
import com.feylabs.uikit.util.ImageViewUtil.loadImageFromURL
import com.feylabs.uikit.databinding.CustomUikitInfoGenreBinding as AdapterBinding
import com.feylabs.uikit.listcomponent.uikitmodel.PokemonUiKitModel as AdapterModel

class PokemonListAdapter() :
    RecyclerView.Adapter<PokemonListAdapter.ViewHolder>() {

    var page = 1
    val data = mutableListOf<AdapterModel>()
    lateinit var itemInterface: ItemInterface

    fun setInterface(itemInterface: ItemInterface) {
        this.itemInterface = itemInterface
    }


    override fun onBindViewHolder(holder: PokemonListAdapter.ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PokemonListAdapter.ViewHolder {
        val binding = AdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = data.size

    inner class ViewHolder(private val binding: AdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: AdapterModel) {
            if (binding.root.isInEditMode.not()) {
                val context = this.binding.root.context
                itemView.setOnClickListener {
                    if (::itemInterface.isInitialized)
                        itemInterface.onClick(data.title.toString())
                }

                with(binding) {
                    binding.tvTitle.text = data.title
                    this.ivThumbnail.loadImageFromURL(context, data.image)
                }
            }
        }
    }

    interface ItemInterface {
        fun onClick(string: String)
    }
}