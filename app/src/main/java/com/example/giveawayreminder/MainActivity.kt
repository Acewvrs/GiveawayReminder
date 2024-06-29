package com.example.giveawayreminder

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.giveawayreminder.data.BottomBarItem
import com.example.giveawayreminder.data.Game
import com.example.giveawayreminder.model.GameViewModel
import com.example.giveawayreminder.ui.screen.SettingsScreen
import com.example.giveawayreminder.ui.theme.GiveawayReminderTheme

/**
 * enum values that represent the screens in the app
 */
enum class AppScreen(@StringRes val title: Int) {
    Promotion(title = R.string.promotion),
    Settings(title = R.string.settings)
}

class MainActivity: ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GiveawayReminderTheme {
                GamesListScreen()
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun GamesListScreen() {
    // create bottom tabs
    val homeTab = BottomBarItem(title = "Promotion", navDestination = AppScreen.Promotion.name, selectedIcon = ImageVector.vectorResource(id = R.drawable.game_controller), unselectedIcon = ImageVector.vectorResource(id = R.drawable.game_controller))
    val settingsTab = BottomBarItem(title = "Settings", navDestination = AppScreen.Settings.name, selectedIcon = Icons.Filled.Settings, unselectedIcon = Icons.Outlined.Settings)

    val tabBarItems = listOf(homeTab, settingsTab)

    val navController: NavHostController = rememberNavController()

    Column(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = { TabView(tabBarItems, navController) }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)){
                navHost(navController)
            }
        }
    }
}

//
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun navHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = AppScreen.Promotion.name,
    ) {
        composable(route = AppScreen.Promotion.name) {
            FreeGamesList()
        }
        composable(route = AppScreen.Settings.name) {
            SettingsScreen()
        }
    }
}

// ---------------------------------------------
// Bottom Navigation Screen

// This is a wrapper view that allows us to easily and cleanly
// reuse this component in any future project
@Composable
fun TabView(tabBarItems: List<BottomBarItem>, navController: NavController) {
    var selectedTabIndex by rememberSaveable {
        mutableIntStateOf(0) // when the app first opens, we're on promotion tab
    }

    NavigationBar {
        // looping over each tab to generate the views and navigation for each item
        tabBarItems.forEachIndexed { index, tabBarItem ->
            NavigationBarItem(
                selected = selectedTabIndex == index,
                onClick = {
                    if (navController.currentDestination!!.route != tabBarItem.navDestination) {
                        selectedTabIndex = index
                        navController.navigate(tabBarItem.navDestination)
                    }
                },
                icon = {
                    TabBarIconView(
                        isSelected = selectedTabIndex == index,
                        selectedIcon = tabBarItem.selectedIcon,
                        unselectedIcon = tabBarItem.unselectedIcon,
                        title = tabBarItem.title,
                        badgeAmount = tabBarItem.badgeAmount
                    )
                },
                label = {Text(tabBarItem.title)})
        }
    }
}

// This component helps to clean up the API call from our TabBarIconView above,
// but could just as easily be added inside the TabBarIconView without creating this custom component
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TabBarBadgeView(count: Int? = null) {
    if (count != null) {
        Badge {
            Text(count.toString())
        }
    }
}

// This component helps to clean up the API call from our TabView above,
// but could just as easily be added inside the TabView without creating this custom component
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabBarIconView(
    isSelected: Boolean,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    title: String,
    badgeAmount: Int? = null
) {
    BadgedBox(badge = { TabBarBadgeView(badgeAmount) }) {
        Icon(
            imageVector = if (isSelected) {selectedIcon} else {unselectedIcon},
            contentDescription = title
        )
    }
}

// ---------------------------------------------

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun FreeGamesList(modifier: Modifier = Modifier) {
    val viewModel: GameViewModel = viewModel(factory = GameViewModel.Factory)
    val modelState = viewModel.uiState.collectAsState().value
    val gamesList: List<Game> = modelState.games

    // immediately ask users for notification permission ONLY IF we haven't asked them before
    NotificationPermissionRequest()

    LazyColumn(
        contentPadding = PaddingValues(dimensionResource(id = R.dimen.padding_medium)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium)),
        modifier = modifier
    ) {
        items(items = gamesList) {
            GameItem(selectedGame = it)
        }
    }
}

//asking for notification permission immediately on launch
@Composable
fun NotificationPermissionRequest() {
    val context = LocalContext.current

    var hasNotificationPermission by remember { mutableStateOf(false) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasNotificationPermission = isGranted
    }

    val activity = context as Activity
    val permission = Manifest.permission.POST_NOTIFICATIONS

    // check if we have notification permission
    val permissionCheckResult = ContextCompat.checkSelfPermission(context, permission)

    // ask if it's our first time asking for permission
    if (permissionCheckResult != PackageManager.PERMISSION_GRANTED && !shouldShowRequestPermissionRationale(activity, permission)) {
        LaunchedEffect(Unit) {
        requestPermissionLauncher.launch(permission)
        }
    }
}

@Composable
fun GameItem(selectedGame: Game) {
    Card(modifier = Modifier
        .padding(horizontal = 16.dp)
        .padding(top = 16.dp)
        .heightIn(60.dp, 130.dp)
    ) {

        Row {
            // Display platform image
            Column(modifier = Modifier
                .padding(4.dp)
                .weight(7F)
                .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween) {

                // Title
                Text(
                    modifier = Modifier
                        .weight(1F)
                        .padding(5.dp),
                    text = selectedGame.title,
                    textAlign = TextAlign.Start,
                    fontSize = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Row(modifier = Modifier
                    .weight(1F)
                    .fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceAround) {
                    Column {
                        // original price
                        Text(
                            text = selectedGame.getOriginalPrice(),
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            style = TextStyle(textDecoration = TextDecoration.LineThrough)
                        )
                        // discount price
                        Text(
                            text = "${selectedGame.getDiscountPrice()}",
                            fontSize = 25.sp,
                            textAlign = TextAlign.Start,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .background(color = Color.Red, shape = RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    ) {
                        // sale percentage
                        Text(
                            text = selectedGame.getSalePercentage(),
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = "Ends: ${selectedGame.getCurrentPromotionEndDate()}",
                    textAlign = TextAlign.Start,
                    fontSize = 12.sp,
                )
            }
            val gameUrl = selectedGame.getThumbnailUrl() ?: "https://demofree.sirv.com/nope-not-here.jpg"
            AsyncImage(
                modifier = Modifier
                    .weight(4F)
                    .fillMaxSize(),
                model = ImageRequest.Builder(context = LocalContext.current)
                    .data(gameUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Game Thumbnail",
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ListPreview() {
    GiveawayReminderTheme {
//        FreeGamesList()
    }
}

@Preview(showBackground = true)
@Composable
fun GameItemPreview() {
    GiveawayReminderTheme {
        Box(modifier = Modifier.fillMaxWidth()) {
            GameItem(Game(id = "0","A really Long Title", imgUrls = emptyList()))
        }
    }
}

@Composable
fun TestPreview() {
    GiveawayReminderTheme {
        Box(modifier = Modifier.fillMaxWidth()) {
            GameItem(Game(id = "0","A really Long Title", imgUrls = emptyList()))
        }
    }
}