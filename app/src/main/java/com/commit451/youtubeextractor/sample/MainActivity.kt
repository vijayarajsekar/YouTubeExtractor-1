package com.commit451.youtubeextractor.sample

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

import com.bumptech.glide.Glide
import com.commit451.youtubeextractor.YouTubeExtraction
import com.commit451.youtubeextractor.YouTubeExtractor
import com.commit451.youtubeextractor.bestAvailableQualityThumbUri
import com.commit451.youtubeextractor.bestAvailableQualityVideoUrl
import com.devbrackets.android.exomedia.ui.widget.VideoView

import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class MainActivity : AppCompatActivity() {

    companion object {

        private val GRID_YOUTUBE_ID = "9d8wWcJLnFI"

        private val STATE_SAVED_POSITION = "saved_position"
    }

    lateinit var imageView: ImageView
    lateinit var description: TextView
    lateinit var videoView: VideoView

    private var savedPosition: Int = 0

    private val extractor = YouTubeExtractor.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imageView = findViewById(R.id.thumb)
        videoView = findViewById(R.id.video_view)
        description = findViewById(R.id.description)

        extractor.extract(GRID_YOUTUBE_ID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<YouTubeExtraction> {
                    override fun onSubscribe(d: Disposable) {}

                    override fun onSuccess(value: YouTubeExtraction) {
                        bindVideoResult(value)
                    }

                    override fun onError(e: Throwable) {
                        this@MainActivity.onError(e)
                    }
                })
        if (savedInstanceState != null) {
            savedPosition = savedInstanceState.getInt(STATE_SAVED_POSITION, 0)
        }
        videoView.setOnPreparedListener({
            videoView.setVolume(0f)
            videoView.seekTo(savedPosition.toLong())
            savedPosition = 0
            videoView.start()
        })
        videoView.setOnErrorListener { e ->
            e.printStackTrace()
            false
        }
    }

    override fun onResume() {
        super.onResume()
        videoView.seekTo(savedPosition.toLong())
        videoView.start()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_SAVED_POSITION, savedPosition)
    }

    private fun onError(t: Throwable) {
        t.printStackTrace()
        Toast.makeText(this@MainActivity, "It failed to extract. So sad", Toast.LENGTH_SHORT)
                .show()
    }

    private fun bindVideoResult(result: YouTubeExtraction) {
        val videoUrl = result.bestAvailableQualityVideoUrl()
        Log.d("OnSuccess", "Got a result with the best url: $videoUrl")
        Glide.with(this)
                .load(result.bestAvailableQualityThumbUri())
                .into(imageView)
        videoView.setVideoURI(Uri.parse(videoUrl))
    }
}
