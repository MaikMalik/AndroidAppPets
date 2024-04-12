package com.dam2.app_pet2.ui.fragments.news

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.dam2.app_pet2.R
import com.dam2.app_pet2.databinding.FragmentNewsBinding
import com.dam2.app_pet2.databinding.ToolbarNewsBinding
import com.dam2.app_pet2.network.adapter.ArticleAdapter
import com.dam2.app_pet2.network.iterator
import com.dam2.app_pet2.network.listaArticulos
import com.dam2.app_pet2.network.models.Article
import com.dam2.app_pet2.network.url
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class NewsFragment : Fragment(), ArticleAdapter.OnButtonClickListener {

    private var _binding: FragmentNewsBinding? = null
    private val binding get() = _binding!!

    private var isRefreshing = false

    val TAG = "NewsHomeFragment.kt"
    var articleAdapter: ArticleAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        peticionNoticias()
        recyclerViewDisplay()
        binding.NewsRefreshLayout.setOnRefreshListener {
            if (!isRefreshing) {
                isRefreshing = true
                peticionNoticias()
            } else {
                binding.NewsRefreshLayout.isRefreshing = false
            }
        }
    }

    fun peticionNoticias() {

        val coladepeticiones = Volley.newRequestQueue(requireContext())
        val peticionJSON = JsonObjectRequest(Request.Method.GET,
            url,
            null,
            Response.Listener<JSONObject> { response ->
                try {
                    listaArticulos.removeAll(listaArticulos)
                    listaArticulos.addAll(processJSON(response))
                    articleAdapter!!.notifyDataSetChanged()

                    // Restablecer el estado de isRefreshing y detener la animación de actualización
                    isRefreshing = false
                    binding.NewsRefreshLayout.isRefreshing = false

                    //para verificar que se está haciendo correctamente
                    for (i in listaArticulos) {
                        Log.d(TAG, i.toString())
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error -> Log.d(TAG, error.toString()) })
        coladepeticiones.add(peticionJSON)
    }

    //metodo para rellenar nuestor objeto Article
    fun processJSON(json: JSONObject): ArrayList<Article> {
        val articles: JSONArray = json.getJSONArray("articles")//lista creada
        val listArticles = ArrayList<Article>() //array donde estan todos los articulos
        for (i in articles) {
            val author = i.getString("author")
            val title = i.getString("title")
            val description = i.getString("description")
            val url = i.getString("url")
            val urlToImg = i.getString("urlToImage")

            listArticles.add(Article(author, title, description, url, urlToImg))
        }
        return listArticles
    }

    fun recyclerViewDisplay() {
        articleAdapter = ArticleAdapter(listaArticulos, this)
        //binding.myRecycler.setHasFixedSize(true)//tamaño de items iguales
        binding.myRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.myRecycler.adapter = articleAdapter
    }

    override fun OnButtonCompartirClickListener(article: Article, position: Int) {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, article.url)
        startActivity(intent)
    }

    override fun OnButtonVerNoticiaClickListener(article: Article, position: Int) {
        val intent = Intent(requireContext(), WebViewActivity::class.java)
        intent.putExtra("article", position)
        startActivity(intent)

    }

}