package com.example.cashflow.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cashflow.ui.books.BooksScreen
import com.example.cashflow.ui.booksEntry.BooksEntry
import com.example.cashflow.ui.category.CategoryScreen
import com.example.cashflow.ui.main.MainScreen
import com.example.cashflow.viewmodel.BookViewModel

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val viewModel: BookViewModel = viewModel()
    NavHost(
        navController = navController,
        startDestination = "main",
        enterTransition = { fadeIn(animationSpec = tween(0)) },
        exitTransition = { fadeOut(animationSpec = tween(0)) },
        popEnterTransition = { fadeIn(animationSpec = tween(0)) },
        popExitTransition = { fadeOut(animationSpec = tween(0)) }
    ) {
        composable("main") {
            MainScreen(navController)
        }
        composable("category") {
            CategoryScreen(navController)
        }
      
        composable("bookEntry/{bookId}") { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
            BooksEntry(navController, bookId, viewModel)
        }
        composable("book/{type}") { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: "cash"
            BooksScreen(type = type, navController)
        }
    }
}