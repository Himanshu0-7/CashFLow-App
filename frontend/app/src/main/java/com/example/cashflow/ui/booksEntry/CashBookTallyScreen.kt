// ui/booksEntry/CashBookTallyScreen.kt
package com.example.cashflow.ui.booksEntry

import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cashflow.data.TallySyncService
import com.example.cashflow.model.Ledger
import com.example.cashflow.model.TallyEntry
import com.example.cashflow.viewmodel.TallyViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

// Tally-standard ledger groups
val TALLY_GROUPS = listOf(
    "Cash", "Bank Accounts", "Sundry Debtors", "Sundry Creditors",
    "Sales Accounts", "Purchase Accounts", "Direct Expenses",
    "Indirect Expenses", "Direct Income", "Indirect Income",
    "Capital Account", "Loans (Liability)", "Fixed Assets", "Current Assets"
)

val BG = Color(0xFF080A1B)
val CARD = Color(0xFF0f1230)
val GREEN = Color(0xFF8cdba2)
val RED = Color(0xFFe74c3c)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashBookTallyScreen(
    vm: TallyViewModel = viewModel()
) {
    val companies by vm.companies.collectAsState()
    val activeCompany by vm.activeCompany.collectAsState()
    val ledgers by vm.ledgers.collectAsState()
    val entries by vm.entries.collectAsState()

    var showAddEntry by remember { mutableStateOf(false) }
    var showAddLedger by remember { mutableStateOf(false) }
    var showConnectTally by remember { mutableStateOf(false) }
    var showCompanySwitcher by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BG)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top Bar ─────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Cash Book",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        activeCompany?.name ?: "No company connected",
                        color = GREEN, fontSize = 13.sp
                    )
                }
                Row {
                    IconButton(onClick = { showCompanySwitcher = true }) {
                        Icon(Icons.Default.Business, null, tint = Color.White)
                    }
                    IconButton(onClick = { showAddLedger = true }) {
                        Icon(Icons.Default.AccountBalance, null, tint = Color.White)
                    }
                    IconButton(onClick = { showAddEntry = true }) {
                        Icon(Icons.Default.Add, null, tint = GREEN, modifier = Modifier.size(28.dp))
                    }
                }
            }

            // ── Summary Row ──────────────────────────────────────
            if (activeCompany != null) {
                SummaryRow(entries, ledgers)
            }

            // ── Entry List or Empty State ─────────────────────────
            if (activeCompany == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Business, null,
                            tint = Color.Gray, modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text("Not connected to Tally", color = Color.Gray)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Make sure Tally is open on your PC",
                            color = Color.Gray.copy(0.6f), fontSize = 12.sp
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { showConnectTally = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GREEN, contentColor = Color.Black
                            )
                        ) {
                            Icon(Icons.Default.Link, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Connect to Tally")
                        }
                    }
                }
            } else {
                EntryList(entries, ledgers)
            }
        }

        // ── Dialogs ──────────────────────────────────────────────
        if (showConnectTally) {
            ConnectTallyDialog(
                onDismiss = { showConnectTally = false },
                onConnect = { name, ip ->
                    vm.addCompany(name, ip)
                    showConnectTally = false
                }
            )
        }

        if (showCompanySwitcher) {
            CompanySwitcherDialog(
                companies = companies,
                activeId = activeCompany?.id,
                onSelect = { vm.switchCompany(it); showCompanySwitcher = false },
                onAddNew = { showCompanySwitcher = false; showConnectTally = true },
                onDismiss = { showCompanySwitcher = false }
            )
        }

        if (showAddLedger) {
            AddLedgerDialog(
                onDismiss = { showAddLedger = false },
                onAdd = { name, group -> vm.addLedger(name, group); showAddLedger = false }
            )
        }

        if (showAddEntry && activeCompany != null) {
            AddEntrySheet(
                ledgers = ledgers,
                detectType = { dr, cr -> vm.detectVoucherType(dr, cr) },
                onAdd = { dr, cr, amt, narration, date ->
                    vm.addEntry(dr, cr, amt, narration, date)
                    showAddEntry = false
                },
                onDismiss = { showAddEntry = false }
            )
        }
    }
}

// ── Summary Row ───────────────────────────────────────────────────────────────

@Composable
fun SummaryRow(entries: List<TallyEntry>, ledgers: List<Ledger>) {
    val formatter = remember {
        NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("en").setRegion("IN").build())
    }
    val totalPayment = entries.filter { it.voucherType == "Payment" }.sumOf { it.amount }
    val totalReceipt = entries.filter { it.voucherType == "Receipt" }.sumOf { it.amount }
    val unsynced = entries.count { !it.synced }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SummaryCard("Receipt", formatter.format(totalReceipt), GREEN, Modifier.weight(1f))
        SummaryCard("Payment", formatter.format(totalPayment), RED, Modifier.weight(1f))
        SummaryCard("Unsynced", "$unsynced", Color(0xFFf39c12), Modifier.weight(1f))
    }
    Spacer(Modifier.height(8.dp))
}

@Composable
fun SummaryCard(label: String, value: String, color: Color, modifier: Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(CARD)
            .padding(10.dp)
    ) {
        Text(label, color = Color.Gray, fontSize = 11.sp)
        Text(value, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

// ── Entry List ────────────────────────────────────────────────────────────────

@Composable
fun EntryList(entries: List<TallyEntry>, ledgers: List<Ledger>) {
    val formatter = remember {
        NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("en").setRegion("IN").build())
    }
    if (entries.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No entries yet. Tap + to add.", color = Color.Gray)
        }
        return
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        items(entries) { entry ->
            val dr = ledgers.find { it.id == entry.debitLedgerId }
            val cr = ledgers.find { it.id == entry.creditLedgerId }
            val color = when (entry.voucherType) {
                "Receipt" -> GREEN; "Payment" -> RED
                "Contra" -> Color(0xFF5dade2); else -> Color.White
            }
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
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CARD),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            entry.voucherType.take(2),
                            color = color,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            "${dr?.name ?: "?"} → ${cr?.name ?: "?"}",
                            color = Color.White, fontSize = 14.sp
                        )
                        Text(
                            entry.narration.ifBlank { entry.voucherType },
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                        Text(formatDate(entry.date), color = Color.Gray, fontSize = 11.sp)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        formatter.format(entry.amount),
                        color = color,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                    if (!entry.synced) Text(
                        "● Unsynced",
                        color = Color(0xFFf39c12),
                        fontSize = 10.sp
                    )
                }
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(Color.White.copy(0.06f))
            )
        }
    }
}

// ── Add Entry Sheet ───────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntrySheet(
    ledgers: List<Ledger>,
    detectType: (Ledger, Ledger) -> String,
    onAdd: (Ledger, Ledger, Double, String, Long) -> Unit,
    onDismiss: () -> Unit
) {
    var debitLedger by remember { mutableStateOf<Ledger?>(null) }
    var creditLedger by remember { mutableStateOf<Ledger?>(null) }
    var amount by remember { mutableStateOf("") }
    var narration by remember { mutableStateOf("") }
    var dateTime by remember {
        mutableStateOf(SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.getDefault()).format(Date()))
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDebitDropdown by remember { mutableStateOf(false) }
    var showCreditDropdown by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val voucherType = if (debitLedger != null && creditLedger != null)
        detectType(debitLedger!!, creditLedger!!) else "—"

    val voucherColor = when (voucherType) {
        "Receipt" -> GREEN; "Payment" -> RED
        "Contra" -> Color(0xFF5dade2); "Journal" -> Color(0xFFf39c12)
        else -> Color.Gray
    }

    val formatter = SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.getDefault())
    val parsedDate = try {
        formatter.parse(dateTime)?.time
    } catch (e: Exception) {
        null
    }

    // Overlay
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(0.5f))
            .clickable { onDismiss() })

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(Color(0xFF0d1025))
                .padding(20.dp)
        ) {
            // Drag handle
            Box(
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(40.dp)
                    .height(4.dp)
                    .background(Color.Gray, RoundedCornerShape(50))
            )
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "New Entry",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                // Auto-detected voucher type badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(voucherColor.copy(0.15f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        voucherType,
                        color = voucherColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Debit Ledger ─────────────────────────────────────
            Text("Dr (Debit) Ledger", color = Color.Gray, fontSize = 12.sp)
            Spacer(Modifier.height(4.dp))
            LedgerDropdown(
                selected = debitLedger,
                ledgers = ledgers,
                expanded = showDebitDropdown,
                onToggle = { showDebitDropdown = !showDebitDropdown },
                onSelect = { debitLedger = it; showDebitDropdown = false },
                onDismiss = { showDebitDropdown = false }
            )

            Spacer(Modifier.height(12.dp))

            // ── Credit Ledger ────────────────────────────────────
            Text("Cr (Credit) Ledger", color = Color.Gray, fontSize = 12.sp)
            Spacer(Modifier.height(4.dp))
            LedgerDropdown(
                selected = creditLedger,
                ledgers = ledgers,
                expanded = showCreditDropdown,
                onToggle = { showCreditDropdown = !showCreditDropdown },
                onSelect = { creditLedger = it; showCreditDropdown = false },
                onDismiss = { showCreditDropdown = false }
            )

            // Contra hint
            AnimatedVisibility(voucherType == "Contra") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF5dade2).copy(0.1f))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.SwapHoriz,
                        null,
                        tint = Color(0xFF5dade2),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Contra entry detected — Cash ↔ Bank transfer",
                        color = Color(0xFF5dade2),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Amount ───────────────────────────────────────────
            Text("Amount", color = Color.Gray, fontSize = 12.sp)
            Spacer(Modifier.height(4.dp))
            TextField(
                value = amount, onValueChange = { amount = it },
                placeholder = { Text("0.00") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = CARD, unfocusedContainerColor = CARD,
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White
                )
            )

            Spacer(Modifier.height(12.dp))

            // ── Narration ────────────────────────────────────────
            Text("Narration", color = Color.Gray, fontSize = 12.sp)
            Spacer(Modifier.height(4.dp))
            TextField(
                value = narration, onValueChange = { narration = it },
                placeholder = { Text("Optional note") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = CARD, unfocusedContainerColor = CARD,
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White
                )
            )

            Spacer(Modifier.height(12.dp))

            // ── Date & Time ──────────────────────────────────────
            Text("Date & Time", color = Color.Gray, fontSize = 12.sp)
            Spacer(Modifier.height(4.dp))
            TextField(
                value = dateTime, onValueChange = { dateTime = it },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = CARD, unfocusedContainerColor = CARD,
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White
                ),
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, null, tint = GREEN)
                    }
                }
            )

            if (showDatePicker) {
                val dps = rememberDatePickerState()
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        Button(onClick = {
                            showDatePicker = false
                            val sel = dps.selectedDateMillis ?: return@Button
                            val cal = Calendar.getInstance().apply { timeInMillis = sel }
                            TimePickerDialog(
                                context,
                                { _, h, m ->
                                    cal.set(Calendar.HOUR_OF_DAY, h); cal.set(Calendar.MINUTE, m)
                                    dateTime = SimpleDateFormat(
                                        "dd MMM yyyy hh:mm a",
                                        Locale.getDefault()
                                    ).format(cal.time)
                                },
                                cal.get(Calendar.HOUR_OF_DAY),
                                cal.get(Calendar.MINUTE),
                                false
                            ).show()
                        }) { Text("OK") }
                    }
                ) { DatePicker(state = dps) }
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    val dr = debitLedger ?: return@Button
                    val cr = creditLedger ?: return@Button
                    val amt = amount.toDoubleOrNull() ?: return@Button
                    onAdd(dr, cr, amt, narration, parsedDate ?: System.currentTimeMillis())
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = debitLedger != null && creditLedger != null && amount.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GREEN,
                    contentColor = Color.Black
                )
            ) { Text("Save Entry", fontWeight = FontWeight.Bold) }

            Spacer(Modifier.height(8.dp))
        }
    }
}

// ── Ledger Dropdown ───────────────────────────────────────────────────────────

@Composable
fun LedgerDropdown(
    selected: Ledger?, ledgers: List<Ledger>,
    expanded: Boolean, onToggle: () -> Unit,
    onSelect: (Ledger) -> Unit, onDismiss: () -> Unit
) {
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(CARD)
                .clickable { onToggle() }
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    selected?.name ?: "Select ledger",
                    color = if (selected != null) Color.White else Color.Gray
                )
                if (selected != null) Text(selected.group, color = GREEN, fontSize = 11.sp)
            }
            Icon(Icons.Default.ArrowDropDown, null, tint = Color.Gray)
        }
        DropdownMenu(
            expanded = expanded, onDismissRequest = onDismiss,
            modifier = Modifier.background(Color(0xFF1a1f3a))
        ) {
            if (ledgers.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No ledgers. Add one first.", color = Color.Gray) },
                    onClick = onDismiss
                )
            }
            ledgers.forEach { ledger ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(ledger.name, color = Color.White)
                            Text(ledger.group, color = GREEN, fontSize = 11.sp)
                        }
                    },
                    onClick = { onSelect(ledger) }
                )
            }
        }
    }
}

// ── Add Ledger Dialog ─────────────────────────────────────────────────────────

@Composable
fun AddLedgerDialog(onDismiss: () -> Unit, onAdd: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var selectedGroup by remember { mutableStateOf(TALLY_GROUPS.first()) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0d1025),
        title = { Text("Add Ledger", color = Color.White) },
        text = {
            Column {
                TextField(
                    value = name, onValueChange = { name = it },
                    placeholder = { Text("Ledger name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = CARD, unfocusedContainerColor = CARD,
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White
                    )
                )
                Spacer(Modifier.height(12.dp))
                Text("Group", color = Color.Gray, fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CARD)
                            .clickable { expanded = true }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(selectedGroup, color = Color.White)
                        Icon(Icons.Default.ArrowDropDown, null, tint = Color.Gray)
                    }
                    DropdownMenu(
                        expanded = expanded, onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Color(0xFF1a1f3a))
                    ) {
                        TALLY_GROUPS.forEach { group ->
                            DropdownMenuItem(
                                text = { Text(group, color = Color.White) },
                                onClick = { selectedGroup = group; expanded = false }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onAdd(name, selectedGroup) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = GREEN,
                    contentColor = Color.Black
                )
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) } }
    )
}

// ── Add Company Dialog ────────────────────────────────────────────────────────
@Composable
fun ConnectTallyDialog(
    onDismiss: () -> Unit,
    onConnect: (name: String, ip: String) -> Unit
) {
    var ip by remember { mutableStateOf("192.168.1.") }
    var port by remember { mutableStateOf("9000") }
    var companies by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0d1025),
        title = { Text("Connect to Tally", color = Color.White) },
        text = {
            Column {
                // ── IP Input ─────────────────────────────────────
                Text("Tally PC IP Address", color = Color.Gray, fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextField(
                        value = ip,
                        onValueChange = { ip = it },
                        placeholder = { Text("192.168.1.10") },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = CARD, unfocusedContainerColor = CARD,
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White
                        )
                    )
                    TextField(
                        value = port,
                        onValueChange = { port = it },
                        placeholder = { Text("9000") },
                        modifier = Modifier.width(80.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = CARD, unfocusedContainerColor = CARD,
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White
                        )
                    )
                }

                Spacer(Modifier.height(10.dp))

                // ── Fetch Button ──────────────────────────────────
                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            error = ""
                            companies = emptyList()
                            val result =
                                TallySyncService.fetchCompanies(ip, port.toIntOrNull() ?: 9000)
                            if (result.isEmpty()) {
                                error =
                                    "Could not connect. Make sure Tally is open and Gateway is enabled."
                            } else {
                                companies = result
                            }
                            isLoading = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2563eb), contentColor = Color.White
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(if (isLoading) "Connecting..." else "Fetch Companies")
                }

                // ── Error ─────────────────────────────────────────
                if (error.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(error, color = RED, fontSize = 12.sp)
                }

                // ── Company List ──────────────────────────────────
                if (companies.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text("Select Company", color = Color.Gray, fontSize = 12.sp)
                    Spacer(Modifier.height(6.dp))
                    companies.forEach { companyName ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CARD)
                                .clickable { onConnect(companyName, ip) }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Business, null,
                                    tint = GREEN, modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(companyName, color = Color.White, fontSize = 14.sp)
                            }
                            Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
                        }
                    }
                }

                Spacer(Modifier.height(6.dp))
                Text(
                    "Tally must be open with Gateway enabled (F12 → Gateway of Tally → Yes)",
                    color = Color.Gray, fontSize = 10.sp
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) }
        }
    )
}

// ── Company Switcher Dialog ───────────────────────────────────────────────────

@Composable
fun CompanySwitcherDialog(
    companies: List<com.example.cashflow.model.Company>,
    activeId: String?,
    onSelect: (com.example.cashflow.model.Company) -> Unit,
    onAddNew: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0d1025),
        title = { Text("Switch Company", color = Color.White) },
        text = {
            Column {
                companies.forEach { company ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (company.id == activeId) GREEN.copy(0.1f) else Color.Transparent)
                            .clickable { onSelect(company) }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(company.name, color = Color.White, fontWeight = FontWeight.Medium)
                            Text(company.tallyIp, color = Color.Gray, fontSize = 11.sp)
                        }
                        if (company.id == activeId) {
                            Icon(
                                Icons.Default.Check,
                                null,
                                tint = GREEN,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onAddNew, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Add, null, tint = GREEN)
                    Spacer(Modifier.width(6.dp))
                    Text("Add New Company", color = GREEN)
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close", color = Color.Gray) } }
    )
}