package pt.spacelabs.experience.learnify

import DBHelper
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.ui.PlayerView
import pt.spacelabs.experience.learnify.Entitys.Playback
import java.io.File

class Player : ComponentActivity() {
    private lateinit var player : ExoPlayer

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.player)

        player = ExoPlayer.Builder(this).build()
        val playerItem = findViewById<PlayerView>(R.id.playerItem)

        playerItem.player = player

        val listPlaybacks: List<Playback> = DBHelper(this).getChunksByClassID(intent.getStringExtra("classId").toString())

        if (listPlaybacks.isNotEmpty()) {
            val m3u8Item = listPlaybacks.find { it.chunk.endsWith(".m3u8", ignoreCase = true) }


            val manifestPath = "$filesDir/${m3u8Item?.chunk}"
            val uri = Uri.fromFile(File(manifestPath))

            val dataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(this)

            val mediaItem = MediaItem.fromUri(uri)
            val hlsMediaSource = HlsMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)

            player.setMediaSource(hlsMediaSource)
        } else {
            val headers = mapOf(
                "Authorization" to "Bearer " + DBHelper(this).getConfig("auth")
            )
            val httpDataSourceFactory = DefaultHttpDataSource.Factory()
                .setDefaultRequestProperties(headers)

            val dataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(this, httpDataSourceFactory)
            val mediaItem = MediaItem.fromUri("https://vis-ipv-cda.epictv.spacelabs.pt/" + intent.getStringExtra("manifestId") + "/master.m3u8")
            val hlsMediaSource = HlsMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)

            player.setMediaSource(hlsMediaSource)
        }

        player.prepare()
        player.play()
    }

    override fun onStart() {
        super.onStart()
        player.playWhenReady = true
    }

    override fun onStop() {
        super.onStop()
        player.playWhenReady = false
        player.release()
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }

    private fun enterImmersiveMode() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            enterImmersiveMode()
        }
    }
}