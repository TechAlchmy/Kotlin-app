package com.mustfaibra.shoesstore.screens.holder

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.mustfaibra.shoesstore.components.AppBottomNav
import com.mustfaibra.shoesstore.components.CustomSnackBar
import com.mustfaibra.shoesstore.models.CartItem
import com.mustfaibra.shoesstore.models.User
import com.mustfaibra.shoesstore.providers.LocalNavHost
import com.mustfaibra.shoesstore.screens.bookmarks.BookmarksScreen
import com.mustfaibra.shoesstore.screens.cart.CartScreen
import com.mustfaibra.shoesstore.screens.checkout.CheckoutScreen
import com.mustfaibra.shoesstore.screens.home.HomeScreen
import com.mustfaibra.shoesstore.screens.locationpicker.LocationPickerScreen
import com.mustfaibra.shoesstore.screens.login.LoginScreen
import com.mustfaibra.shoesstore.screens.notifications.NotificationScreen
import com.mustfaibra.shoesstore.screens.onboard.OnboardScreen
import com.mustfaibra.shoesstore.screens.orderhistory.OrdersHistoryScreen
import com.mustfaibra.shoesstore.screens.productdetails.ProductDetailsScreen
import com.mustfaibra.shoesstore.screens.profile.ProfileScreen
import com.mustfaibra.shoesstore.screens.search.SearchScreen
import com.mustfaibra.shoesstore.screens.signup.SignupScreen
import com.mustfaibra.shoesstore.screens.splash.SplashScreen
import com.mustfaibra.shoesstore.sealed.Screen
import com.mustfaibra.shoesstore.utils.UserPref
import com.mustfaibra.shoesstore.utils.getDp
import com.skydoves.whatif.whatIfNotNull
import kotlinx.coroutines.launch

@Composable
fun HolderScreen(
    onStatusBarColorChange: (color: Color) -> Unit,
    holderViewModel: HolderViewModel = hiltViewModel(),
) {
    val destinations = remember {
        listOf(Screen.Home, Screen.Notifications, Screen.Bookmark, Screen.Profile)
    }
    val controller = LocalNavHost.current
    val currentDestinationAsState = getActiveRoute(navController = controller)
    val productsOnCartIds = holderViewModel.productsOnCartIds
    val productsOnBookmarksIds = holderViewModel.productsOnBookmarksIds
    val user by UserPref.user
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val cartItems = holderViewModel.cartItems
    val (snackBarColor, setSnackBarColor) = remember {
        mutableStateOf(Color.White)
    }

    val snackBarTransition = updateTransition(
        targetState = scaffoldState.snackbarHostState,
        label = "SnackBarTransition"
    )

    val snackBarOffsetAnim by snackBarTransition.animateDp(
        label = "snackBarOffsetAnim",
        transitionSpec = {
            TweenSpec(
                durationMillis = 300,
                easing = LinearEasing,
            )
        }
    ) {
        when (it.currentSnackbarData) {
            null -> {
                100.getDp()
            }
            else -> {
                0.getDp()
            }
        }
    }

    Box {
        ScaffoldSection(
            controller = controller,
            scaffoldState = scaffoldState,
            user = user,
            cartItems = cartItems,
            productsOnCartIds = productsOnCartIds,
            productsOnBookmarksIds = productsOnBookmarksIds,
            onStatusBarColorChange = onStatusBarColorChange,
            bottomNavigationContent = {
                if (
                    currentDestinationAsState in destinations.map { it.route }
                    || currentDestinationAsState == Screen.Cart.route
                ) {
                    AppBottomNav(
                        activeRoute = currentDestinationAsState,
                        backgroundColor = MaterialTheme.colors.surface,
                        bottomNavDestinations = destinations,
                        onActiveRouteChange = {
                            if (it != currentDestinationAsState) {
                                /** We should navigate to that new route */
                                controller.navigate(it) {
                                    popUpTo(Screen.Home.route) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            },
            onSplashFinished = { nextDestination ->
                controller.navigate(nextDestination.route) {
                    popUpTo(Screen.Splash.route) {
                        inclusive = true
                    }
                }
            },
            onBoardFinished = {
                controller.navigate(Screen.Home.route) {
                    popUpTo(Screen.Onboard.route) {
                        inclusive = true
                    }
                }
            },
            onBackRequested = {
                controller.popBackStack()
            },
            onNavigationRequested = { route, removePreviousRoute ->
                if (removePreviousRoute) {
                    controller.popBackStack()
                }
                controller.navigate(route)
            },
            onShowProductRequest = { productId ->
                controller.navigate(
                    Screen.ProductDetails.route
                        .replace("{productId}", "$productId")
                )
            },
            onUpdateCartRequest = { productId ->
                holderViewModel.updateCart(
                    productId = productId,
                    currentlyOnCart = productId in productsOnCartIds,
                )
            },
            onUpdateBookmarkRequest = { productId ->
                holderViewModel.updateBookmarks(
                    productId = productId,
                    currentlyOnBookmarks = productId in productsOnBookmarksIds,
                )
            },
            onUserNotAuthorized = { removeCurrentRoute ->
                if (removeCurrentRoute) {
                    controller.popBackStack()
                }
                controller.navigate(Screen.Login.route)
            },
            onToastRequested = { message, color ->
                scope.launch {
                    /** dismiss the previous one if its exist */
                    scaffoldState.snackbarHostState.currentSnackbarData?.dismiss()
                    /** Update the snack bar color */
                    setSnackBarColor(color)
                    scaffoldState.snackbarHostState
                        .showSnackbar(
                            message = message,
                            duration = SnackbarDuration.Short,
                        )
                }
            }
        )
        CustomSnackBar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = snackBarOffsetAnim),
            snackHost = scaffoldState.snackbarHostState,
            backgroundColorProvider = { snackBarColor },
        )
    }
}

@Composable
fun ScaffoldSection(
    controller: NavHostController,
    scaffoldState: ScaffoldState,
    user: User?,
    cartItems: List<CartItem>,
    productsOnCartIds: List<Int>,
    productsOnBookmarksIds: List<Int>,
    onStatusBarColorChange: (color: Color) -> Unit,
    onSplashFinished: (nextDestination: Screen) -> Unit,
    onBoardFinished: () -> Unit,
    onNavigationRequested: (route: String, removePreviousRoute: Boolean) -> Unit,
    onBackRequested: () -> Unit,
    onUpdateCartRequest: (productId: Int) -> Unit,
    onUpdateBookmarkRequest: (productId: Int) -> Unit,
    onShowProductRequest: (productId: Int) -> Unit,
    onUserNotAuthorized: (removeCurrentRoute: Boolean) -> Unit,
    onToastRequested: (message: String, color: Color) -> Unit,
    bottomNavigationContent: @Composable () -> Unit,
) {
    Scaffold(
        scaffoldState = scaffoldState,
        snackbarHost = {
            scaffoldState.snackbarHostState
        },
    ) { paddingValues ->
        Column(
            Modifier.padding(paddingValues)
        ) {
            NavHost(
                modifier = Modifier.weight(1f),
                navController = controller,
                startDestination = Screen.Home.route
            ) {
                composable(Screen.Splash.route) {
                    onStatusBarColorChange(MaterialTheme.colors.primary)
                    SplashScreen(onSplashFinished = onSplashFinished)
                }
                composable(Screen.Onboard.route) {
                    onStatusBarColorChange(MaterialTheme.colors.background)
                    OnboardScreen(onBoardFinished = onBoardFinished)
                }
                composable(Screen.Signup.route) {
                    onStatusBarColorChange(MaterialTheme.colors.background)
                    SignupScreen()
                }
                composable(Screen.Login.route) {
                    onStatusBarColorChange(MaterialTheme.colors.background)
                    LoginScreen(
                        onUserAuthenticated = onBackRequested,
                        onToastRequested = onToastRequested,
                    )
                }
                composable(Screen.Home.route) {
                    onStatusBarColorChange(MaterialTheme.colors.background)
                    HomeScreen(
                        cartProductsIds = productsOnCartIds,
                        bookmarkProductsIds = productsOnBookmarksIds,
                        onProductClicked = onShowProductRequest,
                        onCartStateChanged = onUpdateCartRequest,
                        onBookmarkStateChanged = onUpdateBookmarkRequest,
                    )
                }
                composable(
                    route = Screen.ProductDetails.route,
                    arguments = listOf(
                        navArgument(name = "productId") { type = NavType.IntType }
                    ),
                ) {
                    onStatusBarColorChange(MaterialTheme.colors.background)
                    val productId = it.arguments?.getInt("productId")
                        ?: throw IllegalArgumentException("Product id is required")

                    ProductDetailsScreen(
                        productId = productId,
                        cartItemsCount = cartItems.size,
                        isOnCartStateProvider = { productId in productsOnCartIds },
                        isOnBookmarksStateProvider = { productId in productsOnBookmarksIds },
                        onUpdateCartState = onUpdateCartRequest,
                        onUpdateBookmarksState = onUpdateBookmarkRequest,
                        onBackRequested = onBackRequested,
                        onNavigateToCartRequested = {
                            onNavigationRequested(Screen.Cart.route, false)
                        }
                    )
                }
                composable(Screen.Notifications.route) {
                    onStatusBarColorChange(MaterialTheme.colors.background)
                    NotificationScreen()
                }
                composable(Screen.Search.route) {
                    onStatusBarColorChange(MaterialTheme.colors.background)
                    SearchScreen()
                }
                composable(Screen.Bookmark.route) {
                    onStatusBarColorChange(MaterialTheme.colors.background)
                    BookmarksScreen(
                        cartProductsIds = productsOnCartIds,
                        onProductClicked = onShowProductRequest,
                        onCartStateChanged = onUpdateCartRequest,
                        onBookmarkStateChanged = onUpdateBookmarkRequest,
                    )
                }
                composable(Screen.Cart.route) {
                    onStatusBarColorChange(MaterialTheme.colors.background)
                    CartScreen(
                        user = user,
                        cartItems = cartItems,
                        onProductClicked = onShowProductRequest,
                        onUserNotAuthorized = { onUserNotAuthorized(false) },
                        onCheckoutRequest = {
                            onNavigationRequested(Screen.Checkout.route, false)
                        },
                    )
                }
                composable(route = Screen.Checkout.route) {
                    onStatusBarColorChange(MaterialTheme.colors.background)
                    user.whatIfNotNull(
                        whatIf = {
                            CheckoutScreen(
                                cartItems = cartItems,
                                onBackRequested = onBackRequested,
                                onCheckoutSuccess = {
                                    onNavigationRequested(Screen.OrderHistory.route, true)
                                },
                                onToastRequested = onToastRequested,
                                onChangeLocationRequested = {
                                    onNavigationRequested(Screen.LocationPicker.route, false)
                                }
                            )
                        },
                        whatIfNot = {
                            LaunchedEffect(key1 = Unit) {
                                onUserNotAuthorized(true)
                            }
                        },
                    )
                }
                composable(Screen.LocationPicker.route) {
                    onStatusBarColorChange(MaterialTheme.colors.background)
                    LocationPickerScreen(
                        onLocationRequested = {

                        },
                        onLocationPicked = {

                        }
                    )
                }
                composable(Screen.Profile.route) {
                    user.whatIfNotNull(
                        whatIf = {
                            onStatusBarColorChange(MaterialTheme.colors.background)
                            ProfileScreen(
                                user = it,
                                onNavigationRequested = onNavigationRequested,
                            )
                        },
                        whatIfNot = {
                            LaunchedEffect(key1 = Unit) {
                                onUserNotAuthorized(true)
                            }
                        },
                    )
                }
                composable(Screen.OrderHistory.route) {
                    user.whatIfNotNull(
                        whatIf = {
                            onStatusBarColorChange(MaterialTheme.colors.background)
                            OrdersHistoryScreen(
                                onBackRequested = onBackRequested,
                            )
                        },
                        whatIfNot = {
                            LaunchedEffect(key1 = Unit) {
                                onUserNotAuthorized(true)
                            }
                        },
                    )
                }
            }
            /** Now we lay down our bottom navigation component */
            bottomNavigationContent()
        }
    }
}

/**
 * A function that is used to get the active route in our Navigation Graph , should return the splash route if it's null
 */
@Composable
fun getActiveRoute(navController: NavHostController): String {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route ?: "splash"
}
