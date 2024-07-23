package com.washcloud.consoleapplication.ui.mainad

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import com.washcloud.consoleapplication.R



@Composable
fun VideoPlayer(context: Context, modifier: Modifier = Modifier) {
    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            val rawId = RawResourceDataSource.buildRawResourceUri(R.raw.ad_1)
            val mediaItem = MediaItem.fromUri(rawId)
            setMediaItem(mediaItem)
            repeatMode = ExoPlayer.REPEAT_MODE_ALL
            prepare()
            playWhenReady = true
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
