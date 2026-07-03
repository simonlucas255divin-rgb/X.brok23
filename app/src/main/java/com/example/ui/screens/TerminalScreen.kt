package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.TickerState
import com.example.ui.TradingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalScreen(viewModel: TradingViewModel) {
    val tickers by viewModel.tickers.collectAsState()
    val isTradingActive by viewModel.isTradingActive.collectAsState()
    val selectedAlgo by viewModel.selectedAlgorithm.collectAsState()
    val riskLevel by viewModel.riskLevel.collectAsState()
    val leverage by viewModel.leverage.collectAsState()
    val cpuLoad by viewModel.cpuLoad.collectAsState()

    var selectedTickerKey by remember { mutableStateOf("BTC/USD") }
    val selectedTicker = tickers[selectedTickerKey]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0E14)) // Deep cyber slate black
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp) // Space for bottom nav padding
    ) {
        // AI Agent Banner
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF111622),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "M I R A G E",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF00E5FF), // Cyber Cyan
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "ENGINE QUANTITATIF HFT EN TEMPS RÉEL",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // System status pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                if (isTradingActive) Color(0xFF143026) else Color(0xFF2A2E3D),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { viewModel.toggleTradingActive() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("trading_status_pill")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    if (isTradingActive) Color(0xFF00FF66) else Color.Gray,
                                    shape = RoundedCornerShape(50)
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isTradingActive) "ACTIVE" else "PAUSE",
                            color = if (isTradingActive) Color(0xFF00FF66) else Color.LightGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Live tickers selector list
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "FLUX DE MARCHÉ FINANCIER",
            color = Color.Gray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(tickers.values.toList()) { state ->
                val isSelected = state.ticker == selectedTickerKey
                val cardBorderColor = if (isSelected) Color(0xFF00E5FF) else Color.Transparent
                val trendColor = if (state.dailyChangePercent >= 0) Color(0xFF00FF66) else Color(0xFFFF3D00)

                Card(
                    onClick = { selectedTickerKey = state.ticker },
                    modifier = Modifier
                        .width(135.dp)
                        .testTag("ticker_card_${state.ticker.replace("/", "_")}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF15202E) else Color(0xFF111622)
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = SolidColor(cardBorderColor)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = state.ticker.split("/")[0],
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Icon(
                                imageVector = if (state.dailyChangePercent >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = trendColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (state.ticker == "EUR/USD") "$${String.format("%.4f", state.currentPrice)}" else "$${String.format("%,.2f", state.currentPrice)}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${if (state.dailyChangePercent >= 0) "+" else ""}${String.format("%.2f", state.dailyChangePercent)}%",
                            fontSize = 11.sp,
                            color = trendColor,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Live Graphic Screen
        if (selectedTicker != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("chart_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111622))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header inside card
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = selectedTicker.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "ANALYSE TECHNIQUE - MA ${selectedTicker.ticker}",
                                fontSize = 10.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (selectedTicker.ticker == "EUR/USD") "$${String.format("%.4f", selectedTicker.currentPrice)}" else "$${String.format("%,.2f", selectedTicker.currentPrice)}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF00FF66),
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "RSI: ${String.format("%.1f", selectedTicker.rsi)}",
                                fontSize = 11.sp,
                                color = if (selectedTicker.rsi > 70) Color(0xFFFF3D00) else if (selectedTicker.rsi < 30) Color(0xFF00FF66) else Color.Cyan,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Draw our dynamic sparkline chart on custom canvas!
                    SparklineChart(
                        history = selectedTicker.history,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Indicator tags
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IndicatorPill(
                            label = "SMA(3)",
                            value = String.format("%.2f", selectedTicker.smaShort),
                            color = Color(0xFF00E5FF)
                        )
                        IndicatorPill(
                            label = "SMA(6)",
                            value = String.format("%.2f", selectedTicker.smaLong),
                            color = Color(0xFFFFB300)
                        )
                        IndicatorPill(
                            label = "Signal IA",
                            value = if (selectedTicker.smaShort > selectedTicker.smaLong) "ACHETER" else "VENDRE",
                            color = if (selectedTicker.smaShort > selectedTicker.smaLong) Color(0xFF00FF66) else Color(0xFFFF3D00)
                        )
                    }
                }
            }
        }

        // Quant Parameter Tuning
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "CONFIGURATION DES ALGORITHMES QUANTITATIFS",
            color = Color.Gray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("algo_config_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF111622))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // CPU load & Strategy select
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Charge CPU Agent IA :",
                        fontSize = 13.sp,
                        color = Color.LightGray,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${String.format("%.1f", cpuLoad)}% (HFT)",
                        fontSize = 13.sp,
                        color = Color(0xFF00E5FF),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { (cpuLoad / 100f).toFloat() },
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF00E5FF),
                    trackColor = Color(0xFF202737)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Selector for Strategy
                Text(
                    text = "Stratégie Mathématique Active :",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                val strategies = listOf("SMA Crossover v2.5", "RSI Volatility Scalper", "MACD Momentum")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    strategies.forEach { strategy ->
                        val isSelected = selectedAlgo == strategy
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (isSelected) Color(0xFF1E2E44) else Color(0xFF1B1E28),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.changeAlgorithm(strategy) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = strategy.replace(" v2.5", "").replace(" Volatility", "").replace(" Momentum", ""),
                                fontSize = 11.sp,
                                color = if (isSelected) Color(0xFF00E5FF) else Color.LightGray,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Risk Level Configurations
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Tolérance au Risque :",
                            fontSize = 13.sp,
                            color = Color.LightGray,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Définit la fréquence des transactions",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Low", "Medium", "High").forEach { level ->
                            val isSelected = riskLevel == level
                            val btnColor = when (level) {
                                "Low" -> Color(0xFF00E676)
                                "Medium" -> Color(0xFFFFB300)
                                else -> Color(0xFFFF1744)
                            }
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isSelected) btnColor.copy(alpha = 0.25f) else Color(0xFF1B1E28),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.changeRiskLevel(level) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = level,
                                    color = if (isSelected) btnColor else Color.LightGray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Leverage configuration
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Effet de Levier :",
                            fontSize = 13.sp,
                            color = Color.LightGray,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Multiplicateur de gains/pertes",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(1, 2, 5, 10).forEach { lev ->
                            val isSelected = leverage == lev
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        if (isSelected) Color(0xFF00E5FF).copy(alpha = 0.25f) else Color(0xFF1B1E28),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.changeLeverage(lev) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${lev}x",
                                    color = if (isSelected) Color(0xFF00E5FF) else Color.LightGray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Manual Intervention Panel
        if (selectedTicker != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "INTERVENTION MANUELLE",
                color = Color.Gray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111622))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { viewModel.triggerManualTrade(selectedTickerKey, "BUY") },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("manual_buy_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ACHETER ${selectedTickerKey.split("/")[0]}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Button(
                        onClick = { viewModel.triggerManualTrade(selectedTickerKey, "SELL") },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("manual_sell_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD50000)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "VENDRE ${selectedTickerKey.split("/")[0]}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SparklineChart(
    history: List<Double>,
    modifier: Modifier = Modifier
) {
    if (history.isEmpty()) return

    val minVal = history.minOrNull() ?: 0.0
    val maxVal = history.maxOrNull() ?: 1.0
    val valueRange = if (maxVal - minVal == 0.0) 1.0 else maxVal - minVal

    val trendIsPositive = if (history.size >= 2) history.last() >= history.first() else true
    val activeColor = if (trendIsPositive) Color(0xFF00FF66) else Color(0xFFFF3D00)

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val points = history.mapIndexed { index, value ->
            val x = if (history.size > 1) {
                index.toFloat() / (history.size - 1) * width
            } else {
                width / 2
            }
            // Invert Y coordinate so lower prices are at bottom
            val y = height - ((value - minVal) / valueRange * height).toFloat()
            Offset(x, y)
        }

        // Draw grid lines
        val gridLines = 4
        for (i in 1..gridLines) {
            val gridY = height * i / (gridLines + 1)
            drawLine(
                color = Color(0xFF222938),
                start = Offset(0f, gridY),
                end = Offset(width, gridY),
                strokeWidth = 1f
            )
        }

        // Draw path line
        val strokePath = Path().apply {
            if (points.isNotEmpty()) {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    val pPrev = points[i - 1]
                    val pCurr = points[i]
                    // Bezier smoothing curves
                    val controlX = (pPrev.x + pCurr.x) / 2
                    cubicTo(controlX, pPrev.y, controlX, pCurr.y, pCurr.x, pCurr.y)
                }
            }
        }

        drawPath(
            path = strokePath,
            color = activeColor,
            style = Stroke(width = 5f)
        )

        // Draw gradient area beneath sparkline
        if (points.isNotEmpty()) {
            val fillPath = Path().apply {
                addPath(strokePath)
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(activeColor.copy(alpha = 0.25f), Color.Transparent),
                    startY = 0f,
                    endY = height
                )
            )
        }

        // Draw outer indicator glow dot on last value
        if (points.isNotEmpty()) {
            val lastPoint = points.last()
            drawCircle(
                color = activeColor,
                radius = 6.dp.toPx(),
                center = lastPoint
            )
            drawCircle(
                color = Color.White,
                radius = 2.5.dp.toPx(),
                center = lastPoint
            )
        }
    }
}

@Composable
fun IndicatorPill(
    label: String,
    value: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .background(Color(0xFF1B1E28), shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$label: ",
                color = Color.Gray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = value,
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
