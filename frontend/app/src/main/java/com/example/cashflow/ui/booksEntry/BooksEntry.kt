package com.example.cashflow.ui.booksEntry

import android.app.TimePickerDialog
import androidx.activity.ComponentActivity
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.cashflow.model.Transaction
import com.example.cashflow.viewmodel.BookViewModel
import generateExcel
import generatePdf
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

// ═══════════════════════════════════════════════════════════════
// ROUTER — entry point called from NavGraph
// ═══════════════════════════════════════════════════════════════

@Composable
fun BooksEntry(
    navController: NavController,
    bookId: String,
    viewModel: BookViewModel = viewModel(
        viewModelStoreOwner = LocalContext.current as ComponentActivity
    )
) {
    val book = viewModel.getBookById(bookId) ?: return

    when (book.type) {
        "Cash" -> CashBookTallyScreen()   // ← replaces CashBooksEntry
        "Travel" -> TravelBooksEntry(navController, bookId, viewModel)
    }
}

// ═══════════════════════════════════════════════════════════════
// TRAVEL BOOK ENTRY  (your original BooksEntry, just renamed)
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelBooksEntry(
    navController: NavController,
    bookId: String,
    viewModel: BookViewModel = viewModel(
        viewModelStoreOwner = LocalContext.current as ComponentActivity
    )
) {
    var searchText by remember { mutableStateOf("") }
    var showReportSheet by remember { mutableStateOf(false) }
    var showSheet by remember { mutableStateOf(false) }
    val allItems = viewModel.getTransactionsByBook(bookId)
    var selectedDate by remember {
        mutableStateOf(getStartOfDay(System.currentTimeMillis()))
    }
    val dateFiltered = allItems.filter { isSameDay(it.date, selectedDate) }
    val finalItems = dateFiltered.filter {
        it.title.contains(searchText, true) ||
                it.category.contains(searchText, true) ||
                it.location.contains(searchText, true)
    }

    val (spending, budget, remaining) = viewModel.getSummary(bookId)
    val book = viewModel.getBookById(bookId) ?: return
    val scope = rememberCoroutineScope()
    var showDatePicker by remember { mutableStateOf(false) }
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val density = LocalDensity.current
    val screenHeightPx = with(density) { screenHeight.toPx() }
    val sheetHeightPx = with(density) { (screenHeight * 0.9f).toPx() }
    val closed = screenHeightPx
    val open = screenHeightPx - sheetHeightPx
    var isSheetOpen by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val offsetY = remember { Animatable(closed) }

    val dragModifier = Modifier.pointerInput(Unit) {
        detectDragGestures(
            onDrag = { change, dragAmount ->
                change.consume()
                scope.launch {
                    offsetY.snapTo((offsetY.value + dragAmount.y).coerceIn(open, closed))
                }
            },
            onDragEnd = {
                scope.launch {
                    val progress = (closed - offsetY.value) / sheetHeightPx
                    if (progress > 0.3f) offsetY.animateTo(open)
                    else {
                        isSheetOpen = false
                        offsetY.animateTo(closed)
                    }
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF080a1b))
    ) {
        Column(
            modifier = Modifier
                .blur(if (isSheetOpen) 6.dp else 0.dp)
                .fillMaxSize()
        ) {
            HeaderSection(
                spending = spending,
                budget = budget,
                remaining = remaining,
                type = book.type
            )
            GenerateReport(onShowSheet = { showReportSheet = true })
            SearchBar(searchText, { searchText = it }, onDateClick = { showDatePicker = true })
            DateFilter(selectedDate)
            Column { TimelineList(finalItems) }
        }

        // ── Export Report Sheet ──────────────────────────────────
        if (showReportSheet) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { showReportSheet = false }
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Color(0xFF0f1230),
                        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        "Export Report", color = Color.White, fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1a1f3a))
                            .clickable { showReportSheet = false; generatePdf(context, allItems) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_save),
                            contentDescription = "PDF", tint = Color(0xFFe74c3c),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("Export as PDF", color = Color.White, fontSize = 16.sp)
                            Text("Download PDF file", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1a1f3a))
                            .clickable { showReportSheet = false; generateExcel(context, allItems) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_save),
                            contentDescription = "Excel", tint = Color(0xFF27ae60),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("Export as Excel", color = Color.White, fontSize = 16.sp)
                            Text("Download Excel file", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }

        // ── Date Picker ──────────────────────────────────────────
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    Button(onClick = {
                        showDatePicker = false
                        selectedDate =
                            datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                    }) { Text("OK") }
                }
            ) { DatePicker(state = datePickerState) }
        }

        // ── Slide-up Sheet ───────────────────────────────────────
        Box(
            modifier = Modifier
                .offset { IntOffset(0, offsetY.value.toInt()) }
                .fillMaxWidth()
                .height(screenHeight * 0.9f)
                .then(dragModifier)
                .background(Color(0xFF020617), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp, bottom = 12.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .background(Color.Gray, RoundedCornerShape(50))
                )
                AddTransactionSheet(
                    selectedDate = selectedDate,
                    onAdd = { title, amount, category, location, date ->
                        viewModel.addTransaction(
                            Transaction(
                                id = System.currentTimeMillis().toString(),
                                title = title, amount = amount, category = category,
                                location = location, date = date, type = "expense", bookId = bookId
                            )
                        )
                        scope.launch { isSheetOpen = false; offsetY.animateTo(closed) }
                    }
                )
            }
        }

        // ── FAB ──────────────────────────────────────────────────
        if (!isSheetOpen) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(20.dp)
                    .size(60.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF8cdba2))
                    .clickable {
                        scope.launch { isSheetOpen = true; offsetY.animateTo(open) }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add, contentDescription = "Add",
                    tint = Color.Black, modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionSheet(
    selectedDate: Long,
    onAdd: (title: String, amount: Double, category: String, location: String, date: Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Food") }
    var location by remember { mutableStateOf("") }
    var dateTime by remember {
        mutableStateOf(SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.getDefault()).format(Date()))
    }
    val formatter = SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.getDefault())
    val parsedDate = try {
        formatter.parse(dateTime)?.time
    } catch (e: Exception) {
        null
    }
    var showDatePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val travelCategories = listOf("Food", "Cab", "Petrol", "Flight", "Hotel", "Shopping", "Other")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text("Add Expense", color = Color.White, fontSize = 20.sp)
        Spacer(Modifier.height(12.dp))

        Text("Category", color = Color.Gray)
        Spacer(Modifier.height(6.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            items(travelCategories) { category ->
                Column(
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (selectedCategory == category) Color(0xFF8cdba2) else Color(
                                0xFF11152a
                            )
                        )
                        .clickable { selectedCategory = category }
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = getCategoryIcon(category),
                        contentDescription = null,
                        tint = if (selectedCategory == category) Color.Black else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        category, fontSize = 9.sp,
                        color = if (selectedCategory == category) Color.Black else Color.Gray
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Text("Description", color = Color.Gray)
        TextField(
            value = title, onValueChange = { title = it },
            placeholder = { Text("e.g. Dinner") }, modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))
        Text("Location", color = Color.Gray)
        TextField(
            value = location, onValueChange = { location = it },
            placeholder = { Text("e.g. Mumbai") }, modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))
        Text("Amount", color = Color.Gray)
        TextField(
            value = amount, onValueChange = { amount = it },
            placeholder = { Text("0.00") }, modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(Modifier.height(10.dp))
        Text("Date & Time", color = Color.Gray)
        TextField(
            value = dateTime, onValueChange = { dateTime = it },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        tint = Color(0xFF8cdba2)
                    )
                }
            }
        )

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    Button(onClick = {
                        showDatePicker = false
                        val sel = datePickerState.selectedDateMillis ?: return@Button
                        val cal = Calendar.getInstance().apply { timeInMillis = sel }
                        TimePickerDialog(context, { _, h, m ->
                            cal.set(Calendar.HOUR_OF_DAY, h)
                            cal.set(Calendar.MINUTE, m)
                            dateTime = SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.getDefault())
                                .format(cal.time)
                        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show()
                    }) { Text("OK") }
                }
            ) { DatePicker(state = datePickerState) }
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                val amt = amount.toDoubleOrNull() ?: 0.0
                onAdd(title, amt, selectedCategory, location, parsedDate ?: selectedDate)
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Add") }
    }
}

// ═══════════════════════════════════════════════════════════════
// CASH BOOK ENTRY
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashBooksEntry(
    navController: NavController,
    bookId: String,
    viewModel: BookViewModel = viewModel(
        viewModelStoreOwner = LocalContext.current as ComponentActivity
    )
) {
    val allItems = viewModel.getTransactionsByBook(bookId)
    var searchText by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(getStartOfDay(System.currentTimeMillis())) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showReportSheet by remember { mutableStateOf(false) }
    var isSheetOpen by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val dateFiltered = allItems.filter { isSameDay(it.date, selectedDate) }
    val finalItems = dateFiltered.filter {
        it.title.contains(searchText, true) ||
                it.category.contains(searchText, true)
    }

    // Cash summary
    val totalCredit = allItems.filter { it.type == "credit" }.sumOf { it.amount }
    val totalDebit = allItems.filter { it.type == "debit" }.sumOf { it.amount }
    val netBalance = totalCredit - totalDebit

    val scope = rememberCoroutineScope()
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val density = LocalDensity.current
    val screenHeightPx = with(density) { screenHeight.toPx() }
    val sheetHeightPx = with(density) { (screenHeight * 0.9f).toPx() }
    val closed = screenHeightPx
    val open = screenHeightPx - sheetHeightPx
    val offsetY = remember { Animatable(closed) }

    val dragModifier = Modifier.pointerInput(Unit) {
        detectDragGestures(
            onDrag = { change, dragAmount ->
                change.consume()
                scope.launch {
                    offsetY.snapTo((offsetY.value + dragAmount.y).coerceIn(open, closed))
                }
            },
            onDragEnd = {
                scope.launch {
                    val progress = (closed - offsetY.value) / sheetHeightPx
                    if (progress > 0.3f) offsetY.animateTo(open)
                    else {
                        isSheetOpen = false; offsetY.animateTo(closed)
                    }
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF080A1B))
    ) {
        Column(
            modifier = Modifier
                .blur(if (isSheetOpen) 6.dp else 0.dp)
                .fillMaxSize()
        ) {
            CashHeaderSection(totalCredit, totalDebit, netBalance)
            GenerateReport(onShowSheet = { showReportSheet = true })
            SearchBar(searchText, { searchText = it }, onDateClick = { showDatePicker = true })
            DateFilter(selectedDate)
            CashTimelineList(items = finalItems)
        }

        // ── Export Sheet ─────────────────────────────────────────
        if (showReportSheet) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { showReportSheet = false }
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Color(0xFF0f1230),
                        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        "Export Report", color = Color.White, fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1a1f3a))
                            .clickable { showReportSheet = false; generatePdf(context, allItems) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_save),
                            contentDescription = "PDF", tint = Color(0xFFe74c3c),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("Export as PDF", color = Color.White, fontSize = 16.sp)
                            Text("Download PDF file", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1a1f3a))
                            .clickable { showReportSheet = false; generateExcel(context, allItems) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_save),
                            contentDescription = "Excel", tint = Color(0xFF27ae60),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("Export as Excel", color = Color.White, fontSize = 16.sp)
                            Text("Download Excel file", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }

        // ── Date Picker ──────────────────────────────────────────
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    Button(onClick = {
                        showDatePicker = false
                        selectedDate =
                            datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                    }) { Text("OK") }
                }
            ) { DatePicker(state = datePickerState) }
        }

        // ── Slide-up Sheet ───────────────────────────────────────
        Box(
            modifier = Modifier
                .offset { IntOffset(0, offsetY.value.toInt()) }
                .fillMaxWidth()
                .height(screenHeight * 0.9f)
                .then(dragModifier)
                .background(Color(0xFF020617), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp, bottom = 12.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .background(Color.Gray, RoundedCornerShape(50))
                )
                AddCashTransactionSheet(
                    selectedDate = selectedDate,
                    onAdd = { title, amount, category, date, txType ->
                        viewModel.addTransaction(
                            Transaction(
                                id = System.currentTimeMillis().toString(),
                                title = title, amount = amount, category = category,
                                location = "", date = date,
                                type = txType,   // "credit" or "debit"
                                bookId = bookId
                            )
                        )
                        scope.launch { isSheetOpen = false; offsetY.animateTo(closed) }
                    }
                )
            }
        }

        // ── FAB ──────────────────────────────────────────────────
        if (!isSheetOpen) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(20.dp)
                    .size(60.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF8cdba2))
                    .clickable {
                        scope.launch { isSheetOpen = true; offsetY.animateTo(open) }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add, contentDescription = "Add",
                    tint = Color.Black, modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// CASH HEADER
// ═══════════════════════════════════════════════════════════════

@Composable
fun CashHeaderSection(totalCredit: Double, totalDebit: Double, netBalance: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text("Cash Book", color = Color.White, fontSize = 25.sp)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 16.dp, end = 16.dp)
    ) {
        Text(
            text = formatRupee(netBalance),
            color = if (netBalance >= 0) Color(0xFF8cdba2) else Color(0xFFe74c3c),
            fontSize = 52.sp,
            fontWeight = FontWeight.Bold
        )
        Text("Net Balance", color = Color(0x80FFFFFF), fontSize = 14.sp)
        Spacer(Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            // Credit pill
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0f1230))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.ArrowDownward, contentDescription = null,
                        tint = Color(0xFF8cdba2), modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Credit", color = Color(0xFF8cdba2), fontSize = 12.sp)
                }
                Spacer(Modifier.height(4.dp))
                Text(formatRupee(totalCredit), color = Color.White, fontSize = 16.sp)
            }
            Spacer(Modifier.width(12.dp))
            // Debit pill
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0f1230))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.ArrowUpward, contentDescription = null,
                        tint = Color(0xFFe74c3c), modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Debit", color = Color(0xFFe74c3c), fontSize = 12.sp)
                }
                Spacer(Modifier.height(4.dp))
                Text(formatRupee(totalDebit), color = Color.White, fontSize = 16.sp)
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

// ═══════════════════════════════════════════════════════════════
// CASH TRANSACTION LIST
// ═══════════════════════════════════════════════════════════════

@Composable
fun CashTimelineList(items: List<Transaction>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        items(items) { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(45.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF11152a)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getCashCategoryIcon(item.category),
                            contentDescription = null,
                            tint = if (item.type == "credit") Color(0xFF8cdba2) else Color(
                                0xFFe74c3c
                            )
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(item.title, color = Color.White, fontSize = 16.sp)
                        Text(item.category, color = Color.Gray, fontSize = 13.sp)
                        Text(formatDate(item.date), color = Color.Gray, fontSize = 12.sp)
                    }
                }
                Text(
                    text = (if (item.type == "credit") "+" else "−") + formatRupee(item.amount),
                    color = if (item.type == "credit") Color(0xFF8cdba2) else Color(0xFFe74c3c),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            // Thin divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(Color.White.copy(alpha = 0.06f))
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// ADD CASH TRANSACTION SHEET
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCashTransactionSheet(
    selectedDate: Long,
    onAdd: (String, Double, String, Long, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Salary") }
    var txType by remember { mutableStateOf("credit") }
    var dateTime by remember {
        mutableStateOf(SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.getDefault()).format(Date()))
    }
    val formatter = SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.getDefault())
    val parsedDate = try {
        formatter.parse(dateTime)?.time
    } catch (e: Exception) {
        null
    }
    var showDatePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val cashCategories =
        listOf("Salary", "Transfer", "Bills", "Shopping", "Food", "EMI", "Investment", "Other")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text("Add Transaction", color = Color.White, fontSize = 20.sp)
        Spacer(Modifier.height(12.dp))

        // ── Credit / Debit Toggle ──────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF0f1230))
        ) {
            listOf("credit" to "↓ Credit", "debit" to "↑ Debit").forEach { (type, label) ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            when {
                                txType == type && type == "credit" -> Color(0xFF8cdba2)
                                txType == type && type == "debit" -> Color(0xFFe74c3c)
                                else -> Color.Transparent
                            }
                        )
                        .clickable { txType = type }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        color = if (txType == type) Color.Black else Color.Gray,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // ── Category Grid ──────────────────────────────────────
        Text("Category", color = Color.Gray)
        Spacer(Modifier.height(6.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            items(cashCategories) { category ->
                Column(
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (selectedCategory == category) Color(0xFF8cdba2)
                            else Color(0xFF11152a)
                        )
                        .clickable { selectedCategory = category }
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = getCashCategoryIcon(category),
                        contentDescription = null,
                        tint = if (selectedCategory == category) Color.Black else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        category, fontSize = 9.sp,
                        color = if (selectedCategory == category) Color.Black else Color.Gray
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Description ────────────────────────────────────────
        Text("Description", color = Color.Gray)
        TextField(
            value = title, onValueChange = { title = it },
            placeholder = { Text("e.g. Rent paid") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))

        // ── Amount ─────────────────────────────────────────────
        Text("Amount", color = Color.Gray)
        TextField(
            value = amount, onValueChange = { amount = it },
            placeholder = { Text("0.00") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(Modifier.height(10.dp))

        // ── Date & Time ────────────────────────────────────────
        Text("Date & Time", color = Color.Gray)
        TextField(
            value = dateTime, onValueChange = { dateTime = it },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        tint = Color(0xFF8cdba2)
                    )
                }
            }
        )

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    Button(onClick = {
                        showDatePicker = false
                        val sel = datePickerState.selectedDateMillis ?: return@Button
                        val cal = Calendar.getInstance().apply { timeInMillis = sel }
                        TimePickerDialog(context, { _, h, m ->
                            cal.set(Calendar.HOUR_OF_DAY, h)
                            cal.set(Calendar.MINUTE, m)
                            dateTime = SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.getDefault())
                                .format(cal.time)
                        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show()
                    }) { Text("OK") }
                }
            ) { DatePicker(state = datePickerState) }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                val amt = amount.toDoubleOrNull() ?: 0.0
                onAdd(title, amt, selectedCategory, parsedDate ?: selectedDate, txType)
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Add") }
    }
}

// ═══════════════════════════════════════════════════════════════
// SHARED HELPERS  (used by both Travel and Cash)
// ═══════════════════════════════════════════════════════════════

fun getStartOfDay(timestamp: Long): Long {
    val cal = Calendar.getInstance().apply {
        timeInMillis = timestamp
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
    return cal.timeInMillis
}

fun isSameDay(t1: Long, t2: Long): Boolean {
    val c1 = Calendar.getInstance().apply { timeInMillis = t1 }
    val c2 = Calendar.getInstance().apply { timeInMillis = t2 }
    return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
            c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
}

fun formatDate(timestamp: Long): String =
    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(timestamp))

@Composable
fun formatRupee(amount: Double): String {
    val formatter = remember {
        NumberFormat.getCurrencyInstance(
            Locale.Builder().setLanguage("en").setRegion("IN").build()
        )
    }
    return formatter.format(amount)
}

@Composable
fun DateFilter(selectedDate: Long) {
    val isToday = isSameDay(selectedDate, System.currentTimeMillis())
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.DateRange, contentDescription = null,
            tint = Color(0xFF8cdba2), modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(formatDate(selectedDate), color = Color.White, fontSize = 13.sp)
        Spacer(Modifier.width(6.dp))
        if (isToday) Text("Today", color = Color.Gray, fontSize = 12.sp)
    }
}

@Composable
fun GenerateReport(onShowSheet: () -> Unit) {
    Button(
        onClick = onShowSheet,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xff2563eb), contentColor = Color.White
        ),
        shape = RoundedCornerShape(10.dp)
    ) { Text("Generate Report", fontSize = 18.sp) }
}

@Composable
fun SearchBar(value: String, onValueChange: (String) -> Unit, onDateClick: () -> Unit) {
    val customShape = RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1.2f)
                .height(56.dp)
                .clip(customShape)
                .background(Brush.horizontalGradient(listOf(Color(0xFF0F172A), Color(0xFF020617))))
                .border(1.dp, Color.White.copy(alpha = 0.15f), customShape),
            contentAlignment = Alignment.CenterStart
        ) {
            TextField(
                value = value, onValueChange = onValueChange,
                placeholder = { Text("Search", color = Color.White.copy(alpha = 0.4f)) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search, contentDescription = null,
                        tint = Color.White.copy(alpha = 0.6f)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = customShape
            )
        }
        Spacer(Modifier.width(12.dp))
        IconButton(onClick = {}) {
            Icon(
                Icons.Default.FilterList, contentDescription = "Filter",
                tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(26.dp)
            )
        }
        IconButton(onClick = onDateClick) {
            Icon(
                Icons.Default.DateRange, contentDescription = "Calendar",
                tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
fun HeaderSection(spending: Double, budget: Double, remaining: Double, type: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text("Travel Book", color = Color.White, fontSize = 25.sp)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 16.dp)
    ) {
        Text(formatRupee(spending), color = Color.White, fontSize = 60.sp)
        Text("Total Spending", color = Color(0x80FFFFFF), fontSize = 16.sp)
    }
    Spacer(Modifier.height(16.dp))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        if (type == "Travel") {
            Text("Budget: ${formatRupee(budget)}", color = Color(0xFF57876c))
        }
        Spacer(Modifier.height(8.dp))
        val progress = if (budget == 0.0) 0f else (spending / budget).toFloat()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.Transparent)
                .border(1.dp, Color.Gray, RoundedCornerShape(50))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF8cdba2))
            )
        }
        Spacer(Modifier.height(8.dp))
        if (type == "Travel") {
            Text(
                "Remaining: ${formatRupee(remaining)}",
                color = Color(0x80FFFFFF),
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun TimelineList(items: List<Transaction>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        items(items) { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(45.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF11152a)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            getCategoryIcon(item.category), contentDescription = null,
                            tint = Color(0xFF8cdba2)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(item.title, color = Color.White, fontSize = 18.sp)
                        Text(item.location, color = Color.Gray, fontSize = 14.sp)
                        Text(formatDate(item.date), color = Color.Gray, fontSize = 12.sp)
                    }
                }
                Text(formatRupee(item.amount), color = Color.White, fontSize = 18.sp)
            }
        }
    }
}

// ── Icon helpers ─────────────────────────────────────────────────────────────

@Composable
fun getCategoryIcon(category: String): ImageVector = when (category) {
    "Food" -> Icons.Default.Fastfood
    "Cab" -> Icons.Default.DirectionsCar
    "Petrol" -> Icons.Default.LocalGasStation
    "Flight" -> Icons.Default.Flight
    "Hotel" -> Icons.Default.Hotel
    "Shopping" -> Icons.Default.ShoppingCart
    else -> Icons.Default.Receipt
}

fun getCashCategoryIcon(category: String): ImageVector = when (category) {
    "Salary" -> Icons.Default.AccountBalance
    "Transfer" -> Icons.Default.SwapHoriz
    "Bills" -> Icons.Default.Receipt
    "Shopping" -> Icons.Default.ShoppingCart
    "Food" -> Icons.Default.Fastfood
    "EMI" -> Icons.Default.CreditCard
    "Investment" -> Icons.Default.TrendingUp
    else -> Icons.Default.AttachMoney
}