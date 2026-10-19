package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.json.JSONObject

/**
 * JavaScript interface bridge between Android WebView and local KaTeX engine.
 */
class KatexJsInterface(private val onHeightChanged: (Int) -> Unit) {
    @JavascriptInterface
    fun onHeightChanged(heightCssPx: Int) {
        onHeightChanged.invoke(heightCssPx)
    }
}

/**
 * Production-ready hybrid WebView composable that renders 2D mathematical formulas
 * and mixed content using the local offline KaTeX distribution in `assets/math/`.
 * Supports LaTeX notation ($$...$$, $...$, \[...\], \(...\)), [EQ]/[IEQ] tags,
 * and auto-sizes dynamically without clipping or cutoff glitches.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun KatexMathView(
    mathContent: String,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = isSystemInDarkTheme(),
    fontSizeSp: Float = 15f,
    forceBlock: Boolean = false,
    minHeight: Dp = if (forceBlock) 44.dp else 36.dp
) {
    // Height in CSS pixels, which maps 1:1 to Android dp with viewport=device-width
    var contentHeightDp by remember(mathContent) { mutableIntStateOf(0) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isPageFinished by remember { mutableStateOf(false) }

    fun sendRenderCommand(view: WebView) {
        val quotedContent = JSONObject.quote(mathContent)
        val js = "javascript:renderMath($quotedContent, $isDarkMode, $fontSizeSp, $forceBlock);"
        view.post {
            view.evaluateJavascript(js) { result ->
                val parsedHeight = result?.toIntOrNull() 
                    ?: result?.toDoubleOrNull()?.toInt()
                if (parsedHeight != null && parsedHeight in 10..8000) {
                    contentHeightDp = parsedHeight
                }
            }
        }
    }

    LaunchedEffect(mathContent, isDarkMode, fontSizeSp, forceBlock, isPageFinished) {
        webViewRef?.let { view ->
            if (isPageFinished) {
                sendRenderCommand(view)
            }
        }
    }

    val heightModifier = if (contentHeightDp > 0) {
        // Add safety padding buffer of 14dp so tall radicals, fraction lines, exponents, and descenders never clip
        val dpHeight = (contentHeightDp + 14).dp
        Modifier.height(maxOf(dpHeight, minHeight))
    } else {
        Modifier.height(minHeight)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(heightModifier),
        contentAlignment = if (forceBlock) Alignment.Center else Alignment.TopStart
    ) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(Color.TRANSPARENT)
                    setLayerType(View.LAYER_TYPE_SOFTWARE, null)

                    isVerticalScrollBarEnabled = false
                    isHorizontalScrollBarEnabled = false
                    isNestedScrollingEnabled = false
                    overScrollMode = View.OVER_SCROLL_NEVER

                    // Let parent scroll containers handle drag gestures smoothly
                    setOnTouchListener { _, _ -> false }

                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        allowFileAccess = true
                        cacheMode = WebSettings.LOAD_NO_CACHE
                        loadWithOverviewMode = false
                        useWideViewPort = false
                        textZoom = 100
                    }

                    addJavascriptInterface(
                        KatexJsInterface { heightCssPx ->
                            post {
                                if (heightCssPx in 10..8000) {
                                    contentHeightDp = heightCssPx
                                }
                            }
                        },
                        "AndroidInterface"
                    )

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isPageFinished = true
                            view?.let { sendRenderCommand(it) }
                        }
                    }

                    loadUrl("file:///android_asset/math/index.html")
                    webViewRef = this
                }
            },
            update = { view ->
                webViewRef = view
                if (isPageFinished) {
                    sendRenderCommand(view)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewRef?.destroy()
            webViewRef = null
        }
    }
}
