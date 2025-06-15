package com.example.plantsaver

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.border
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.room.Room
import com.example.plantsaver.BuildConfig.GOOGLE_MAPS_API_KEY
import com.example.plantsaver.ui.theme.PlantDarkGreen
import com.example.plantsaver.ui.theme.PlantGreen
import com.example.plantsaver.ui.theme.PlantSaverTheme
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {

    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            LocalDB::class.java, "local_db"
        ).fallbackToDestructiveMigration(true).build()
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the SDK
        Places.initialize(this, GOOGLE_MAPS_API_KEY)

        // Create a new PlacesClient instance
        val placesClient = Places.createClient(this)

        val repository = PlantRepository(db)
        val viewModelFactory = ViewModelFactory(repository)
        val dataBaseViewModel = ViewModelProvider(this, viewModelFactory)[DataBaseViewModel::class.java]

        enableEdgeToEdge()
        setContent {
            PlantSaverTheme {
                PlantSaver(dataBaseViewModel, placesClient)
            }
        }
    }
}

@Composable
fun PlantSaver(dataBaseViewModel: DataBaseViewModel, placesClient: PlacesClient) {
    val navController = rememberNavController()
    val weatherViewModel = viewModel<WeatherViewModel>()
    weatherViewModel.placesClient = placesClient

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
    var locationDialog by remember { mutableStateOf(false) }

    when{
        locationDialog->{
            LocationDialog(onDismissRequest = {
                locationDialog = false
            },
                weatherViewModel
            )
        }
    }

    Scaffold(

    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ){
                    Text(
                        text = "Your location",
                        modifier = Modifier
                        .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = weatherData?.location?.name.toString(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = {
                                locationDialog = true
                            }),
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
    val wateringHistory by dataBaseViewModel.watering
    var plantDetail by remember { mutableIntStateOf(-1) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        floatingActionButton = {
            AddPlant(dataBaseViewModel)
        }
    ) {
        innerPadding->
        Column(
            modifier = Modifier.
            padding(innerPadding)
        ) {
            LazyColumn(

            ) {
                items(plants) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = {
                                if(plantDetail == it.plantId) {
                                    plantDetail = -1
                                } else {
                                    plantDetail = it.plantId
                                    dataBaseViewModel.getPlantHistory(it.plantId)
                                }
                            })
                    ) {
                        if(it.plantId == plantDetail) {
                            Column {
                                Row {
                                    Text(it.plantName)
                                }
                                Row {
                                    Text(it.plantSpecies)
                                }
                                JetpackComposeBasicLineChart(Modifier)
                            }
                        } else {
                            Row {
                                Text(it.plantName)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun JetpackComposeBasicLineChart(
    modelProducer: CartesianChartModelProducer,
    modifier: Modifier = Modifier,
) {
    CartesianChartHost(
        chart =
            rememberCartesianChart(
                rememberLineCartesianLayer(),
                startAxis = VerticalAxis.rememberStart(),
                bottomAxis = HorizontalAxis.rememberBottom(),
            ),
        modelProducer = modelProducer,
        modifier = modifier,
    )
}

@Composable
fun JetpackComposeBasicLineChart(modifier: Modifier = Modifier) {
    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(Unit) {
        modelProducer.runTransaction {
            // Learn more: https://patrykandpatrick.com/vmml6t.
            lineSeries { series(13, 8, 7, 12, 0, 1, 15, 14, 0, 11, 6, 12, 0, 11, 12, 11) }
        }
    }
    JetpackComposeBasicLineChart(modelProducer, modifier)
}

@Composable
fun LocationDialog(
    onDismissRequest: () -> Unit,
    weatherViewModel: WeatherViewModel
) {

    var text by remember { mutableStateOf("") }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(1.35, 103.87), 15f)
    }

    val mapUiSettings by remember { mutableStateOf(MapUiSettings()) }
    //val mapProperties by remember { mutableStateOf(MapProperties(isMyLocationEnabled = true)) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(weatherViewModel.currentLatLong.value) {
        cameraPositionState.animate(CameraUpdateFactory.newLatLng(weatherViewModel.currentLatLong.value))
    }

    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving) {
            //weatherViewModel.getAddress(cameraPositionState.position.target)
        }
    }

    Dialog(
        onDismissRequest = {
            onDismissRequest()
        }
    ) {
        Box(
            modifier = Modifier
                .padding(top=50.dp,bottom=50.dp)
                .fillMaxSize()
                ,
        ) {
            GoogleMap(modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = mapUiSettings,
                onMapClick = {
                    scope.launch {
                        cameraPositionState.animate(CameraUpdateFactory.newLatLng(it))
                    }
                })
            Icon(
                painter = painterResource(id = R.drawable.ic_marker),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.Center)
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(8.dp)
                    .fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedVisibility(
                        weatherViewModel.locationAutofill.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(weatherViewModel.locationAutofill) {
                                Row(modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .clickable{
                                        text = it.address
                                        weatherViewModel.locationAutofill.clear()
                                        weatherViewModel.getCoordinates(it)
                                    }) {
                                    Text(it.address)
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                    OutlinedTextField(
                        value = text, onValueChange = {
                            text = it
                            weatherViewModel.searchPlaces(it)
                        }, modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddPlant(dataBaseViewModel: DataBaseViewModel) {

    var click by remember { mutableStateOf(false) }

    FloatingActionButton(
        onClick = { click = !click },
    ) {
        when {
            click->{
                AddPlantDialog(
                    onDismissRequest = {click = !click},
                    dataBaseViewModel
                )
            }
            else->{
                Icon(painterResource(id = R.drawable.ic_add), "Floating action button.")
            }
        }
    }
}

@Composable
fun AddPlantDialog(
    onDismissRequest: () -> Unit,
    dataBaseViewModel: DataBaseViewModel
) {
    var nameText by remember { mutableStateOf("") }
    var speciesText by remember { mutableStateOf("") }
    var frequencyText by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = {
            onDismissRequest()
        }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            shape = RoundedCornerShape(16.dp),
        ){
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ){
                Row {
                    TextField(
                        value = nameText,
                        onValueChange = {
                            nameText = it
                        },
                        label = {
                            Text("Name")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp, 0.dp, 10.dp, 0.dp),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = PlantGreen,
                            unfocusedIndicatorColor = PlantDarkGreen,
                            disabledIndicatorColor = Color.Transparent
                        )
                    )
                }
                Row {
                    TextField(
                        value = speciesText,
                        onValueChange = {
                            speciesText = it
                        },
                        label = {
                            Text("Species")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp, 0.dp, 10.dp, 0.dp),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = PlantGreen,
                            unfocusedIndicatorColor = PlantDarkGreen,
                            disabledIndicatorColor = Color.Transparent
                        )
                    )
                }
                Row {
                    TextField(
                        value = frequencyText,
                        onValueChange = {
                            frequencyText = it
                        },
                        label = {
                            Text("Watering frequency")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp, 0.dp, 10.dp, 0.dp),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = PlantGreen,
                            unfocusedIndicatorColor = PlantDarkGreen,
                            disabledIndicatorColor = Color.Transparent
                        )
                    )
                }
                Row {
                    Button(
                        onClick = {
                            dataBaseViewModel.addPlant(nameText.toString(), speciesText.toString(), frequencyText.toInt())
                            onDismissRequest()
                        }
                    ) {
                        Text("Add plant")
                    }
                }
            }
        }
    }
}