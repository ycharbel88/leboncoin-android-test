package fr.leboncoin.androidrecruitmenttestapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.adevinta.spark.SparkTheme
import dagger.hilt.android.AndroidEntryPoint
import fr.leboncoin.androidrecruitmenttestapp.ui.AlbumsScreenRoute
import fr.leboncoin.androidrecruitmenttestapp.utils.AnalyticsHelper
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var analyticsHelper: AnalyticsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SparkTheme {
                AlbumsScreenRoute(
                    onItemSelected = {
                        analyticsHelper.trackSelection(it.id.toString())
                        startActivity(Intent(this, DetailsActivity::class.java))
                    }
                )
            }
        }
    }
}
