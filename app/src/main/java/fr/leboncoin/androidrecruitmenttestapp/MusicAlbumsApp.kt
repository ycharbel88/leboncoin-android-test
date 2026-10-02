package fr.leboncoin.androidrecruitmenttestapp

import android.app.Application
import android.content.Context
import coil3.ImageLoader
import coil3.SingletonImageLoader
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MusicAlbumsApp : Application(),  SingletonImageLoader.Factory {

    // Delay creating the Hilt-provided ImageLoader until Coil first needs it.
    @Inject
    lateinit var imageLoader: dagger.Lazy<ImageLoader>

    // Connect Coil to Hilt's shared instance so AsyncImage uses our caches.
    override fun newImageLoader(context: Context): ImageLoader =
        imageLoader.get()
}