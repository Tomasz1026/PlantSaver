package com.example.plantsaver

import android.content.res.Configuration
import android.location.Geocoder
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.launch
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.room.Room
import com.example.plantsaver.BuildConfig.GOOGLE_MAPS_API_KEY
import com.example.plantsaver.ui.theme.PlantDarkGreen
import com.example.plantsaver.ui.theme.PlantGreen
import com.example.plantsaver.ui.theme.PlantSaverTheme
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {

    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            LocalDB::class.java, "local_db"
        ).fallbackToDestructiveMigration(true).build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        Places.initialize(this, GOOGLE_MAPS_API_KEY)

        val repository = PlantRepository(db)
        val viewModelFactory = ViewModelFactory(repository)
        val dataBaseViewModel = ViewModelProvider(this, viewModelFactory)[DataBaseViewModel::class.java]

        val weatherViewModel = ViewModelProvider(this)[WeatherViewModel::class.java]
        val placesClient = Places.createClient(this)
        val geoCoder = Geocoder(this)
        weatherViewModel.geoCoder = geoCoder
        weatherViewModel.placesClient = placesClient

        //dataBaseViewModel.deleteAll()
        //dataBaseViewModel.deleteAllWatering()
        //dataBaseViewModel.addPlant("kwiatek", "Paprotka", LocalDate.parse("2025-05-20").atStartOfDay(ZoneId.of("UTC")).toEpochSecond(), 1)
        //dataBaseViewModel.addPlant("krzak", "Rzeżucha", LocalDate.parse("2025-06-10").atStartOfDay(ZoneId.of("UTC")).toEpochSecond(), 2)
        //dataBaseViewModel.addWatering(10, LocalDate.parse("2025-05-25").atStartOfDay(ZoneId.of("UTC")).toEpochSecond())
        //dataBaseViewModel.addWatering(10, LocalDate.parse("2025-06-01").atStartOfDay(ZoneId.of("UTC")).toEpochSecond())
        //dataBaseViewModel.addWatering(11, LocalDate.parse("2025-06-11").atStartOfDay(ZoneId.of("UTC")).toEpochSecond())
        //dataBaseViewModel.addWatering(11, LocalDate.parse("2025-06-15").atStartOfDay(ZoneId.of("UTC")).toEpochSecond())

        //dataBaseViewModel.getPlantsToWater()

        enableEdgeToEdge()
        setContent {
            PlantSaverTheme {
                PlantSaver(dataBaseViewModel, weatherViewModel)
            }
        }
    }
}

@Composable
fun PlantSaver(
    dataBaseViewModel: DataBaseViewModel,
    weatherViewModel: WeatherViewModel
) {
    val navController = rememberNavController()

    if(weatherViewModel.currentLatLong.value == LatLng(0.0,0.0) && dataBaseViewModel.userData.value != UserData(1,0.0,0.0)){
        weatherViewModel.setCurrentLocation(dataBaseViewModel.userData.value)
    }
    weatherViewModel.getCurrentWeather()

    val topLevelRoutes = listOf(
        TopLevelRoute("Home", "home", ImageVector.vectorResource(R.drawable.ic_sun)),
        TopLevelRoute("Plants", "plants", ImageVector.vectorResource(R.drawable.leaf_icon_white))
    )
            Scaffold(
                bottomBar = {
                    NavigationBar {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentDestination = navBackStackEntry?.destination

                        topLevelRoutes.forEach { topLevelRoute ->
                            NavigationBarItem(
                                icon = {
                                    Icon(topLevelRoute.icon, topLevelRoute.name)
                                },
                                label = { Text(topLevelRoute.name) },
                                selected = currentDestination?.route == topLevelRoute.route,
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
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = "home",
                    modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                ) {
                    composable("home") {
                        Weather(
                            weatherViewModel,
                            dataBaseViewModel
                        )
                    }

                    composable("plants") {
                        Plants(dataBaseViewModel)
                    }
                }
            }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Weather(
    weatherViewModel: WeatherViewModel,
    dataBaseViewModel: DataBaseViewModel
) {
    val weatherData by weatherViewModel.weatherData
    val plantsToWater by dataBaseViewModel.plantsToWater
    var locationDialog by rememberSaveable { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    val scope = rememberCoroutineScope()

    when {
        locationDialog->{
            LocationDialog(onDismissRequest = {
                locationDialog = false
            },
                weatherViewModel,
                dataBaseViewModel
            )
        }
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE


    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState
            ){ snackbarData ->
                Snackbar(
                    modifier = Modifier
                        .padding(12.dp)
                        .width(200.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    containerColor = Color.LightGray,
                    contentColor = Color.Black
                ) {
                    Text(
                        text = snackbarData.visuals.message,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    ) { innerPadding ->
        if(isLandscape){
            Row(
                modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            ){
                Column(
                    modifier = Modifier
                        .weight(1f)
                ){
                    when {
                        weatherViewModel.weatherDataError.value -> {
                            Spacer(modifier=Modifier.weight(1f))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ){
                                InfiniteRotatingLoader()
                            }
                            Spacer(modifier=Modifier.weight(1f))
                        } else -> {
                        Text(
                            text = "Your location",
                            modifier = Modifier
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ){
                            Text(
                                text = weatherData?.location?.name.toString(),
                                modifier = Modifier
                                    .clickable(onClick = {
                                        locationDialog = true
                                    }),
                                style = TextStyle(
                                    fontSize = 30.sp
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
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
                            IconButton(onClick = {
                                weatherViewModel.getCurrentWeather()
                            }){
                                Icon(Icons.Filled.Refresh, "Refresh")
                            }
                        }
                    }
                    }
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                ){
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start=30.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Text(
                            modifier = Modifier
                                .wrapContentHeight(align = Alignment.CenterVertically),
                            text = "Plants to water",
                            style = TextStyle(
                                fontSize = 30.sp
                            )
                        )
                    }
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .padding(20.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .height(300.dp)
                            .padding(10.dp)
                    ) {
                        if(plantsToWater.isNotEmpty()){
                            items(plantsToWater){
                                Card{
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start=10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ){
                                        Text(
                                            modifier = Modifier
                                                .wrapContentHeight(align = Alignment.CenterVertically),
                                            text = it.plantName,
                                            style = TextStyle(
                                                fontSize = 25.sp
                                            )
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        Button(
                                            modifier = Modifier.size(80.dp).padding(5.dp),
                                            onClick = {
                                                dataBaseViewModel.addWatering(it.plantId)
                                                scope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        message = "I was watered!"
                                                    )
                                                }
                                            },colors=ButtonColors(
                                                contentColor = Color.White,
                                                disabledContentColor = Color(0xFF52BFCE),
                                                containerColor = Color(0xFF52BFCE),
                                                disabledContainerColor = Color(0xFF52BFCE)
                                            )
                                        ){
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_water),
                                                contentDescription = "Water me!"
                                            )
                                        }
                                    }

                                }
                            }
                        } else {
                            items(1){
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ){
                                    Text("There are no plants to water!")
                                }
                            }
                        }
                    }
                }
            }
        }
        else {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
            ){
                when {
                    weatherViewModel.weatherDataError.value -> {
                        Spacer(modifier=Modifier.weight(1f))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ){
                            InfiniteRotatingLoader()
                        }
                        Spacer(modifier=Modifier.weight(1f))
                    } else -> {
                    Text(
                        text = "Your location",
                        modifier = Modifier
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ){
                        Text(
                            text = weatherData?.location?.name.toString(),
                            modifier = Modifier
                                .clickable(onClick = {
                                    locationDialog = true
                                }),
                            style = TextStyle(
                                fontSize = 30.sp
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
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
                        IconButton(onClick = {
                            weatherViewModel.getCurrentWeather()
                        }){
                            Icon(Icons.Filled.Refresh, "Refresh")
                        }
                    }
                }
                }
                Spacer(modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start=30.dp),
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text(
                        modifier = Modifier
                            .wrapContentHeight(align = Alignment.CenterVertically),
                        text = "Plants to water",
                        style = TextStyle(
                            fontSize = 30.sp
                        )
                    )
                }
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .padding(20.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .height(300.dp)
                        .padding(10.dp)
                ) {
                    if(plantsToWater.isNotEmpty()){
                        items(plantsToWater){
                            Card{
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start=10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ){
                                    Text(
                                        modifier = Modifier
                                            .wrapContentHeight(align = Alignment.CenterVertically),
                                        text = it.plantName,
                                        style = TextStyle(
                                            fontSize = 20.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Button(
                                        modifier = Modifier.size(80.dp).padding(5.dp),
                                        onClick = {
                                            dataBaseViewModel.addWatering(it.plantId)
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    message = "I was watered!"
                                                )
                                            }
                                        },colors=ButtonColors(
                                            contentColor = Color.White,
                                            disabledContentColor = Color(0xFF52BFCE),
                                            containerColor = Color(0xFF52BFCE),
                                            disabledContainerColor = Color(0xFF52BFCE)
                                        )
                                    ){
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_water),
                                            contentDescription = "Water me!"
                                        )
                                    }
                                }

                            }
                        }
                    } else {
                        items(1){
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ){
                                Text("There are no plants to water!")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Plants(
    dataBaseViewModel: DataBaseViewModel
) {

    val plants by dataBaseViewModel.plants
    val lastWatering by dataBaseViewModel.lastWatering
    var plantDetail by rememberSaveable { mutableIntStateOf(-1) }

    val snackbarHostState = remember { SnackbarHostState() }

    val scope = rememberCoroutineScope()

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val insets: PaddingValues = if (isLandscape) {
        WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal).asPaddingValues()
    } else {
        PaddingValues(0.dp)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(insets),
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start=30.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Text(
                            modifier = Modifier
                                .wrapContentHeight(align = Alignment.CenterVertically),
                            text = "Your plants",
                            style = TextStyle(
                                fontSize = 30.sp
                            )
                        )
                    }
                }
            )
                 },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState
            ){ snackbarData ->
                Snackbar(
                    modifier = Modifier
                        .padding(12.dp)
                        .width(200.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    containerColor = Color.LightGray,
                    contentColor = Color.Black
                ) {
                    Text(
                        text = snackbarData.visuals.message,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
                       },
        modifier = Modifier
            .fillMaxSize(),
        floatingActionButton = {
            AddPlant(dataBaseViewModel,
                popUp={
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Plant added!"
                        )
                    }
                }
            )
        }
    ) {
        innerPadding->
        Column(
            modifier = Modifier
                .padding(insets)
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .padding(start=10.dp, end=10.dp)
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
            ) {
                items(plants) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                            .clickable(onClick = {
                                if(plantDetail == it.plantId) {
                                    plantDetail = -1
                                } else {
                                    plantDetail = it.plantId
                                    dataBaseViewModel.getPlantHistory(it.plantId)
                                }
                            })
                    ){
                        if(it.plantId == plantDetail) {
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp)
                                        .padding(start=10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        modifier = Modifier
                                            .wrapContentHeight(align = Alignment.CenterVertically),
                                        text = "Name: ${it.plantName}",
                                        style = TextStyle(
                                            fontSize = 20.sp
                                        )
                                    )
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp)
                                        .padding(start=10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        modifier = Modifier
                                            .wrapContentHeight(align = Alignment.CenterVertically),
                                        text = "Species: ${it.plantSpecies}",
                                        style = TextStyle(
                                            fontSize = 20.sp
                                        )
                                    )
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp)
                                        .padding(start=10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        modifier = Modifier
                                            .wrapContentHeight(align = Alignment.CenterVertically),
                                        style = TextStyle(
                                            fontSize = 20.sp
                                        ),
                                        text = "Date: ${DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.UTC).format(Instant.ofEpochSecond(it.plantDate))}"
                                    )
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp)
                                        .padding(start=10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        modifier = Modifier
                                            .wrapContentHeight(align = Alignment.CenterVertically),
                                        style = TextStyle(
                                            fontSize = 20.sp
                                        ),
                                        text = "Watering frequency: ${it.plantFrequency} days"
                                    )
                                }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(40.dp)
                                            .padding(start=10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if(lastWatering.isNotEmpty()) {
                                        Text(
                                            modifier = Modifier
                                                .wrapContentHeight(align = Alignment.CenterVertically),
                                            style = TextStyle(
                                                fontSize = 20.sp
                                            ),
                                            text = "Last water date: ${DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.UTC).format(Instant.ofEpochSecond(lastWatering[0].wateringDate))}"
                                        )
                                        } else {
                                            Text(
                                                modifier = Modifier
                                                    .wrapContentHeight(align = Alignment.CenterVertically),
                                                style = TextStyle(
                                                    fontSize = 20.sp
                                                ),
                                                text = "This plant hasn't been watered yet."
                                            )
                                        }
                                    }
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                                    .padding(start=10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    modifier = Modifier
                                        .wrapContentHeight(align = Alignment.CenterVertically),
                                    text = it.plantName,
                                    style = TextStyle(
                                        fontSize = 20.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationDialog(
    onDismissRequest: () -> Unit,
    weatherViewModel: WeatherViewModel,
    dataBaseViewModel: DataBaseViewModel
) {

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(weatherViewModel.currentLatLong.value, 15f)
    }

    val mapUiSettings by remember { mutableStateOf(MapUiSettings()) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(weatherViewModel.currentLatLong.value) {
        cameraPositionState.animate(CameraUpdateFactory.newLatLng(weatherViewModel.currentLatLong.value))
    }

    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving) {
            weatherViewModel.getAddress(cameraPositionState.position.target)
        }
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Popup(
        alignment = Alignment.Center,
        onDismissRequest = {  },
        properties = PopupProperties(
            focusable = true
        )
    ) {
        val focusManager = LocalFocusManager.current
        var isTextFieldFocused by remember { mutableStateOf(false) }

        val handleDismiss = {
            if (isTextFieldFocused) {
                focusManager.clearFocus()
            } else {
                dataBaseViewModel.updateUserLocation(
                    weatherViewModel.currentLatLong.value.latitude,
                    weatherViewModel.currentLatLong.value.longitude
                )
                weatherViewModel.locationAutofill.clear()
                onDismissRequest()
            }
        }

        BackHandler { handleDismiss() }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    handleDismiss()
                }
        ) {
            Box(modifier = Modifier
                .align(Alignment.Center)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
            ) {
        if(isLandscape){
            Surface(modifier = Modifier.width(550.dp)){
                Row(
                    modifier = Modifier
                        .fillMaxSize(),
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        Box{
                            GoogleMap(modifier = Modifier.fillMaxSize(),
                                cameraPositionState = cameraPositionState,
                                uiSettings = mapUiSettings,
                                onMapClick = {
                                    scope.launch {
                                        focusManager.clearFocus()
                                        cameraPositionState.animate(CameraUpdateFactory.newLatLng(it))
                                    }
                                })
                            Icon(
                                painter = painterResource(id = R.drawable.ic_marker),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(24.dp)
                                    .align(Alignment.Center),
                                tint = Color.Black
                            )
                        }
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(16.dp)
                    ) {
                        Spacer(modifier=Modifier.weight(1f))
                        AnimatedVisibility(
                            weatherViewModel.locationAutofill.isNotEmpty(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.height(300.dp)
                            ) {
                                items(weatherViewModel.locationAutofill) {
                                    Row(modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                        .clickable{
                                            weatherViewModel.textSearch.value = it.address
                                            weatherViewModel.locationAutofill.clear()
                                            weatherViewModel.getCoordinates(it)
                                        }
                                    ) {
                                        Text(it.address)
                                    }
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                        }
                        Row{
                            OutlinedTextField(
                                value = weatherViewModel.textSearch.value,
                                onValueChange = {
                                    weatherViewModel.textSearch.value = it
                                    weatherViewModel.searchPlaces(it)
                                }, modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(2.dp)
                                    .onFocusChanged { focusState ->
                                        isTextFieldFocused = focusState.isFocused
                                    }
                            )
                        }
                    }
                }
            }
        }
        else {
            Box(
                modifier = Modifier
                    .padding(30.dp, 80.dp, 30.dp, 80.dp)
                    .width(300.dp),
            ) {
                GoogleMap(modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = mapUiSettings,
                    onMapClick = {

                        scope.launch {
                            focusManager.clearFocus()
                            cameraPositionState.animate(CameraUpdateFactory.newLatLng(it))
                        }
                    })
                Icon(
                    painter = painterResource(id = R.drawable.ic_marker),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.Center),
                    tint = Color.Black
                )

                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(8.dp)
                        .fillMaxWidth(),
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
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.height(100.dp)
                            ) {
                                items(weatherViewModel.locationAutofill) {
                                    Row(modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                        .clickable{
                                            weatherViewModel.textSearch.value = it.address
                                            weatherViewModel.locationAutofill.clear()
                                            weatherViewModel.getCoordinates(it)
                                        }) {
                                        Text(it.address)
                                    }
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                        }
                        Row{
                            OutlinedTextField(
                                value = weatherViewModel.textSearch.value, onValueChange = {
                                    weatherViewModel.textSearch.value = it
                                    weatherViewModel.searchPlaces(it)
                                }, modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(2.dp)
                                    .onFocusChanged { focusState ->
                                        isTextFieldFocused = focusState.isFocused
                                    }
                            )

                        }

                    }
                }
            }
        }
    }
}
    }
}

@Composable
fun AddPlant(
    dataBaseViewModel: DataBaseViewModel,
    popUp: () -> Unit
) {
    var click by rememberSaveable { mutableStateOf(false) }

    LargeFloatingActionButton(
        onClick = { click = !click },
    ) {
        when {
            click->{
                AddPlantDialog(
                    onDismissRequest = {click = !click},
                    popUp={popUp()},
                    dataBaseViewModel
                )
            }
            else->{
                Icon(
                    painterResource(id = R.drawable.ic_add),
                    "Floating action button.",
                    modifier = Modifier.size(50.dp)
                )
            }
        }
    }
}

@Composable
fun AddPlantDialog(
    onDismissRequest: () -> Unit,
    popUp: () -> Unit,
    dataBaseViewModel: DataBaseViewModel
) {
    val verificationViewModel = viewModel<VerificationViewModel>()

    var nameText by verificationViewModel.nameText
    var speciesText by verificationViewModel.speciesText
    var frequencyText by verificationViewModel.frequencyText

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val mod: Modifier = if (isLandscape) {
        Modifier.width(500.dp)
    }
    else {
        Modifier.fillMaxWidth()
    }

    Popup(
        alignment = Alignment.Center,
        onDismissRequest = {  },
        properties = PopupProperties(
            focusable = true
        )
    ) {
        val focusManager = LocalFocusManager.current
        var isTextFieldFocused by remember { mutableStateOf(false) }

        val handleDismiss = {
            if (isTextFieldFocused) {
                focusManager.clearFocus()
            } else {

                onDismissRequest()
            }
        }

        BackHandler { handleDismiss() }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    handleDismiss()
                }
        ) {
            Box(
                modifier = Modifier
                .align(Alignment.Center)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
            ) {
                Card(
                    modifier = mod
                        .padding(20.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(
                        modifier = Modifier

                            .verticalScroll(rememberScrollState())
                    ) {
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
                                    .padding(0.dp, 0.dp, 10.dp, 0.dp)
                                    .onFocusChanged { focusState ->
                                        isTextFieldFocused = focusState.isFocused
                                    },
                                colors = TextFieldDefaults.colors(
                                    focusedIndicatorColor = PlantGreen,
                                    unfocusedIndicatorColor = PlantDarkGreen,
                                    disabledIndicatorColor = Color.Transparent
                                ),
                                singleLine = true,
                                isError = verificationViewModel.nameError.value,
                                supportingText = {
                                    if (verificationViewModel.nameError.value) {
                                        Text("Incorrect name format.")
                                    }
                                }
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
                                    .padding(0.dp, 0.dp, 10.dp, 0.dp)
                                    .onFocusChanged { focusState ->
                                        isTextFieldFocused = focusState.isFocused
                                    },
                                colors = TextFieldDefaults.colors(
                                    focusedIndicatorColor = PlantGreen,
                                    unfocusedIndicatorColor = PlantDarkGreen,
                                    disabledIndicatorColor = Color.Transparent
                                ),
                                singleLine = true,
                                isError = verificationViewModel.speciesError.value,
                                supportingText = {
                                    if (verificationViewModel.speciesError.value) {
                                        Text("Incorrect species format.")
                                    }
                                }
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
                                    .padding(0.dp, 0.dp, 10.dp, 0.dp)
                                    .onFocusChanged { focusState ->
                                        isTextFieldFocused = focusState.isFocused
                                    },
                                colors = TextFieldDefaults.colors(
                                    focusedIndicatorColor = PlantGreen,
                                    unfocusedIndicatorColor = PlantDarkGreen,
                                    disabledIndicatorColor = Color.Transparent
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number
                                ),
                                isError = verificationViewModel.frequencyError.value,
                                supportingText = {
                                    if (verificationViewModel.frequencyError.value) {
                                        Text("Incorrect frequency format.")
                                    }
                                }
                            )
                        }
                        Row(horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()) {
                            Button(
                                modifier = Modifier.height(70.dp),
                                onClick = {
                                    if (verificationViewModel.validation()) {
                                        dataBaseViewModel.addPlant(
                                            nameText.toString(),
                                            speciesText.toString(),
                                            frequencyText.toInt()
                                        )
                                        popUp()
                                        verificationViewModel.clear()
                                        onDismissRequest()
                                    }
                                }
                            ) {
                                Text("Add plant")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfiniteRotatingLoader(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val size = 48.dp
    val strokeWidth = 4.dp

    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    Canvas(
        modifier = modifier
            .size(size)
            .rotate(rotation)
    ) {
        drawArc(
            color = color,
            startAngle = 0f,
            sweepAngle = 270f,
            useCenter = false,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        )
    }
}