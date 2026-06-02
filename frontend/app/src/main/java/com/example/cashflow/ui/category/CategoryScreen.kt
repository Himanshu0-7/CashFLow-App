package com.example.cashflow.ui.category

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cashflow.viewmodel.BookViewModel
import androidx.navigation.NavController


@Composable
fun CategoryScreen(
    navController: NavController,
    viewModel: BookViewModel = viewModel(
        viewModelStoreOwner = LocalContext.current as ComponentActivity
    )
) {

    val state = viewModel.uiState.value
    val books = state.books
    val cashBooksCount = books.count { it.type == "Cash" }
    val travelBooksCount = books.count { it.type == "Travel" }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color(0xFF080A1B),

                )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(Color.Transparent),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {

                Text(
                    textAlign = TextAlign.Center,
                    text = "All Books",
                    color = Color.White,
                    fontSize = 20.sp,
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("book/Cash") }
                    .padding(12.dp),
                shape = RoundedCornerShape(5.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF0f1230)
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Cash Books",
                        fontSize = 25.sp,
                        color = Color.White
                    )
                    Text(
                        text = cashBooksCount.toString(),
                        fontSize = 25.sp,
                        color = Color.White
                    )

                }
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("book/Travel") }
                    .padding(12.dp),
                shape = RoundedCornerShape(5.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF0f1230)
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Travel Books",
                        fontSize = 25.sp,
                        color = Color.White
                    )
                    Text(
                        text = travelBooksCount.toString(),
                        fontSize = 25.sp,
                        color = Color.White
                    )

                }
            }

        }
    }
}