package com.dam2.app_pet2.ui.fragments.news

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebViewClient
import com.dam2.app_pet2.R
import com.dam2.app_pet2.databinding.ActivityWebViewBinding
import com.dam2.app_pet2.network.listaArticulos

class WebViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        webviewLoad()
    }

    fun webviewLoad(){
        val position = intent.getIntExtra("article", 0)
        binding.webview.webViewClient = WebViewClient()
        binding.webview.loadUrl(listaArticulos[position].url)
    }
}