package com.example.plantsaver

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.*
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.system.exitProcess
import androidx.compose.ui.window.Dialog
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.launch
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.room.Room
import com.example.plantsaver.ui.theme.PlantSaverTheme
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

class MainActivity : ComponentActivity() {

    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            LocalDB::class.java, "local_db"
        ).fallbackToDestructiveMigration(true).build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = PlantRepository(db)
        val viewModelFactory = ViewModelFactory(repository)
        val dataBaseViewModel = ViewModelProvider(this, viewModelFactory)[DataBaseViewModel::class.java]

        enableEdgeToEdge()
        setContent {
            PlantSaverTheme {
                PlantSaver(dataBaseViewModel)
            }
        }
    }
}

@Composable
fun PlantSaver(dataBaseViewModel: DataBaseViewModel) {
    val navController = rememberNavController()
    val weatherViewModel = viewModel<WeatherViewModel>()

    weatherViewModel.getCurrentWeather()

    val topLevelRoutes = listOf(
        TopLevelRoute("Weather", "weather", ImageVector.vectorResource(R.drawable.ic_sun)),
        TopLevelRoute("Plants", "plants", ImageVector.vectorResource(R.drawable.ic_flower))
    )

    Scaffold(
        bottomBar =  {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                topLevelRoutes.forEach { topLevelRoute ->
                    NavigationBarItem(
                        icon = {
                            Icon(topLevelRoute.icon, topLevelRoute.name)
                        },
                        label = {Text(topLevelRoute.name)},
                        selected =  currentDestination?.route == topLevelRoute.route,
                        onClick = {
                            navController.navigate(topLevelRoute.route) {
                                popUpTo(navController.graph.id) {
                                    saveState = true
                                    inclusive = true
                                }

                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding->
        NavHost(
            navController = navController,
            startDestination = "weather",
            modifier = Modifier.padding(bottom=innerPadding.calculateBottomPadding())
        ) {
            composable("weather") {
                Weather(
                    weatherViewModel
                )
            }
            composable("plants") {
                Plants(dataBaseViewModel)
            }
        }
    }
}

@Composable
fun Weather(weatherViewModel: WeatherViewModel) {

    val weatherData by weatherViewModel.weatherData
/*
    val singapore = LatLng(1.35, 103.87)
    val singaporeMarkerState = rememberMarkerState(position = singapore)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(singapore, 10f)
    }
*/
    Scaffold(

    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ){
            /*GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                Marker(
                    state = singaporeMarkerState,
                    title = "Singapore",
                    snippet = "Marker in Singapore"
                )
            }*/
                    Text(
                        text = "Your location",
                        modifier = Modifier
                        .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = weatherData?.location?.name.toString(),
                        modifier = Modifier
                            .fillMaxWidth(),
                        style = TextStyle(
                            fontSize = 30.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "${weatherData?.current?.temp_c.toString()}°C",
                        modifier = Modifier
                            .fillMaxWidth(),
                        style = TextStyle(
                            fontSize = 50.sp
                        ),
                        textAlign = TextAlign.Center
                    )
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ){
                Button(onClick = {
                    weatherViewModel.getCurrentWeather()
                }){
                    Icon(Icons.Filled.Refresh, "Refresh")
                }
            }




        }
    }
}

@Composable
fun Plants(dataBaseViewModel: DataBaseViewModel) {

    val plants by dataBaseViewModel.plants

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        floatingActionButton = {
            AddPlant()
        }
    ) {
        innerPadding->
        Column(
            modifier = Modifier.
            padding(innerPadding)
        ) {
            Row() {
                Spacer(Modifier.weight(1f))
                Text("ID")
                Spacer(Modifier.weight(1f))
                Text("Nazwa")
                Spacer(Modifier.weight(1f))
                Text("Gatunek")
                Spacer(Modifier.weight(1f))
            }
            LazyColumn(

            ) {
                items(plants) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Spacer(Modifier.weight(1f))
                        Text(it.plantId.toString())
                        Spacer(Modifier.weight(1f))
                        Text(it.plantName)
                        Spacer(Modifier.weight(1f))
                        Text(it.plantSpecies)
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }

}

@Composable
fun AddPlant() {
    FloatingActionButton(
        onClick = {  },
    ) {
        Icon(painterResource(id = R.drawable.ic_add), "Floating action button.")
    }
}