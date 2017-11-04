package de.mchllngr.example.animated_selector_example

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import de.mchllngr.example.animated_selector_example.view.DownloadStateView
import de.mchllngr.example.animated_selector_example.view.DownloadStateViewAnimatedSelector

class MainActivity : AppCompatActivity() {

    private val downloadStateViewAnimatedSelector: DownloadStateViewAnimatedSelector by lazy { findViewById<DownloadStateViewAnimatedSelector>(R.id.downloadStateViewAnimatedSelector) }
    private val downloadStateView: DownloadStateView by lazy { findViewById<DownloadStateView>(R.id.downloadStateView) }
    private val btnInitial: Button by lazy { findViewById<Button>(R.id.btn_initial) }
    private val btnLoading: Button by lazy { findViewById<Button>(R.id.btn_loading) }
    private val btnSuccess: Button by lazy { findViewById<Button>(R.id.btn_success) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnInitial.setOnClickListener {
            downloadStateViewAnimatedSelector.state = DownloadStateViewAnimatedSelector.State.INITIAL
            downloadStateView.state = DownloadStateView.State.INITIAL
        }
        btnLoading.setOnClickListener {
            downloadStateViewAnimatedSelector.state = DownloadStateViewAnimatedSelector.State.LOADING
            downloadStateView.state = DownloadStateView.State.LOADING
        }
        btnSuccess.setOnClickListener {
            downloadStateViewAnimatedSelector.state = DownloadStateViewAnimatedSelector.State.SUCCESS
            downloadStateView.state = DownloadStateView.State.SUCCESS
        }
    }
}
