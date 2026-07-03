package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PortfolioAsset
import com.example.data.SecurityTransaction
import com.example.ui.TradingViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerScreen(viewModel: TradingViewModel) {
    val portfolio by viewModel.portfolio.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val cash by viewModel.cash.collectAsState()

    var selectedTxForReceipt by remember { mutableStateOf<SecurityTransaction?>(null) }

    val totalAssetsValue = portfolio.sumOf { it.quantity * it.currentPrice }
    val netWorth = cash + totalAssetsValue
    val initialCapital = 50000.0
    val netProfit = netWorth - initialCapital
    val pnlPercent = (netProfit / initialCapital) * 100.0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0E14))
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
    ) {
        // Portfolio summary card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("portfolio_worth_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111622))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "VALEUR NETTE DU PORTEFEUILLE",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$${String.format("%,.2f", netWorth)}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Solde Liquide (Cash)", fontSize = 11.sp, color = Color.Gray)
                            Text(
                                text = "$${String.format("%,.2f", cash)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.LightGray,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Valeur des Actifs", fontSize = 11.sp, color = Color.Gray)
                            Text(
                                text = "$${String.format("%,.2f", totalAssetsValue)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.LightGray,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = Color(0xFF1E2638))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Rendement Global Simulation :",
                            fontSize = 12.sp,
                            color = Color.LightGray
                        )
                        val trendColor = if (netProfit >= 0) Color(0xFF00FF66) else Color(0xFFFF3D00)
                        Text(
                            text = "${if (netProfit >= 0) "+" else ""}$${String.format("%,.2f", netProfit)} (${String.format("%.2f", pnlPercent)}%)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = trendColor,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Section header for holdings
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACTIFS EN PORTEFEUILLE",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "${portfolio.size} Actifs",
                    color = Color(0xFF00E5FF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Portfolio Holdings items
        if (portfolio.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF111622))
                ) {
                    Text(
                        text = "Aucun actif en portefeuille. Activez le trading automatique.",
                        color = Color.Gray,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            items(portfolio) { asset ->
                HoldingRow(asset)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Ledger Header
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "HISTORIQUE DES TRANSACTIONS SÉCURISÉES",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Row {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Simulation",
                        tint = Color.Gray,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { viewModel.resetSimulation() }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Ledger transactions
        if (transactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF111622))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Registre cryptographique vide.",
                            color = Color.LightGray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Les transactions signées par IA apparaîtront ici.",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(transactions) { tx ->
                TransactionRow(tx) {
                    selectedTxForReceipt = tx
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    // Secure Verification Receipt Dialog (Modal)
    if (selectedTxForReceipt != null) {
        val tx = selectedTxForReceipt!!
        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS", Locale.FRANCE).format(Date(tx.timestamp))

        AlertDialog(
            onDismissRequest = { selectedTxForReceipt = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = Color(0xFF00FF66))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "VÉRIFICATION TRANSACTION IA",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "REÇU NUMÉRIQUE ET SCEAU CRYPTOGRAPHIQUE",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0B0E14)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF1F2937), RoundedCornerShape(8.dp))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            ReceiptItem("Horodatage", dateStr)
                            ReceiptItem("Bloc de Validation", "#${tx.blockHeight}")
                            ReceiptItem("Actif", tx.ticker)
                            ReceiptItem("Type d'Ordre", tx.action, if (tx.action == "BUY") Color(0xFF00FF66) else Color(0xFFFF3D00))
                            ReceiptItem("Prix unitaire", "$${String.format("%,.4f", tx.price)}")
                            ReceiptItem("Volume exécuté", "${tx.quantity}")
                            ReceiptItem("Total nominal", "$${String.format("%,.2f", tx.total)}")
                            ReceiptItem("Vitesse de calcul", "${tx.executionTimeMs} ms")
                            ReceiptItem("Algorithme", tx.algorithm)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "SIGNATURE NUMÉRIQUE SHA-256",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tx.signature,
                        fontSize = 10.sp,
                        color = Color(0xFF00E5FF),
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0B0E14), RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF00FF66), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Certifié conforme par Mirage AI",
                            color = Color(0xFF00FF66),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { selectedTxForReceipt = null }
                ) {
                    Text("FERMER", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF111622)
        )
    }
}

@Composable
fun HoldingRow(asset: PortfolioAsset) {
    val totalVal = asset.quantity * asset.currentPrice
    val profit = (asset.currentPrice - asset.averagePrice) * asset.quantity
    val profitPercent = if (asset.averagePrice > 0) (profit / (asset.averagePrice * asset.quantity)) * 100.0 else 0.0
    val profitColor = if (profit >= 0) Color(0xFF00FF66) else Color(0xFFFF3D00)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111622))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = asset.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${asset.quantity} ${asset.ticker.split("/")[0]} @ moy. $${String.format("%,.2f", asset.averagePrice)}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${String.format("%,.2f", totalVal)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "${if (profit >= 0) "+" else ""}${String.format("%.2f", profitPercent)}%",
                    fontSize = 11.sp,
                    color = profitColor,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun TransactionRow(tx: SecurityTransaction, onClick: () -> Unit) {
    val dateStr = SimpleDateFormat("HH:mm:ss", Locale.FRANCE).format(Date(tx.timestamp))
    val isBuy = tx.action == "BUY"
    val actionColor = if (isBuy) Color(0xFF00FF66) else Color(0xFFFF3D00)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("ledger_tx_${tx.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111622))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon lock status
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFF1B2333), shape = RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = tx.action,
                        color = actionColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = tx.ticker.split("/")[0],
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "$dateStr • Scellé: ${tx.signature.take(8)}...",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontFamily = FontFamily.Monospace
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${String.format("%,.2f", tx.total)}",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "${tx.executionTimeMs}ms",
                    color = Color(0xFF00E5FF),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun ReceiptItem(label: String, value: String, valueColor: Color = Color.LightGray) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray, fontSize = 12.sp)
        Text(
            text = value,
            color = valueColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
