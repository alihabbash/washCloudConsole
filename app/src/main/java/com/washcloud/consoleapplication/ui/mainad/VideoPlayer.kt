package com.washcloud.consoleapplication.ui.mainad

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import com.washcloud.consoleapplication.R

@Composable
fun VideoPlayer(context: Context, videoUri: Uri? = null, modifier: Modifier = Modifier,  onVideoEnded: () -> Unit) {
    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = if (videoUri != null) {
                MediaItem.fromUri(videoUri)
            } else {
                val rawId = RawResourceDataSource.buildRawResourceUri(R.raw.ad_1)
                MediaItem.fromUri(rawId)
            }
            setMediaItem(mediaItem)
            repeatMode = if (videoUri != null) ExoPlayer.REPEAT_MODE_ONE else ExoPlayer.REPEAT_MODE_ALL
            prepare()
            playWhenReady = true
            addListener(object : com.google.android.exoplayer2.Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == ExoPlayer.STATE_ENDED) {
                        onVideoEnded()
                    }
                }
            })
        }
    }

    DisposableEffect(
        AndroidView(
            modifier = modifier,
            factory = { ctx ->
                StyledPlayerView(ctx).apply {
                    this.player = player
                    useController = false
                }
            }
        )
    ) {
        onDispose {
            player.release()
        }
    }
}
