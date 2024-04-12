package com.dam2.app_pet2.network.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dam2.app_pet2.R
import com.dam2.app_pet2.databinding.LayoutNewsItemBinding
import com.dam2.app_pet2.network.models.Article
import com.squareup.picasso.Picasso

class ArticleAdapter(val list:List<Article>,
                     val buttonClick: OnButtonClickListener
): RecyclerView.Adapter<ArticleAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_news_item, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        return holder.bind(list[position], buttonClick)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(view: View):RecyclerView.ViewHolder(view){
        val binding = LayoutNewsItemBinding.bind(view)

        fun bind(article : Article, onButtonClickListener: OnButtonClickListener) = with(itemView){
            binding.tvTitle.text = article.title
            binding.tvDescription.text= article.description
            Picasso.get().load(article.urlToImage).into(binding.imgViewArticle)
            binding.btnCompartir.setOnClickListener{onButtonClickListener.OnButtonCompartirClickListener(article, position)}
            binding.btnVernoticia.setOnClickListener{onButtonClickListener.OnButtonVerNoticiaClickListener(article, position)}
        }
    }

    //para poner a la esucha nuestros botones
    interface  OnButtonClickListener{
        fun OnButtonCompartirClickListener(article: Article, position: Int)
        fun OnButtonVerNoticiaClickListener(article: Article, position: Int)

    }
}