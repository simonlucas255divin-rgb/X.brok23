package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.AppDatabase
import com.example.data.PortfolioAsset
import com.example.data.SecurityTransaction
import com.example.data.TradingRepository
import com.example.network.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import kotlin.random.Random

// Represents a market ticker with its price history
data class TickerState(
    val ticker: String,
    val name: String,
    val currentPrice: Double,
    val history: List<Double>,
    val dailyChangePercent: Double,
    val rsi: Double = 50.0,
    val smaShort: Double = 0.0,
    val smaLong: Double = 0.0
)

// Represents a chat message
data class ChatMessage(
    val sender: String, // "User" or "Mirage"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isPending: Boolean = false
)

class TradingViewModel(
    application: Application,
    private val repository: TradingRepository
) : AndroidViewModel(application) {

    // Market status state
    private val _tickers = MutableStateFlow<Map<String, TickerState>>(emptyMap())
    val tickers: StateFlow<Map<String, TickerState>> = _tickers.asStateFlow()

    // Portfolio Cash state (USD)
    private val _cash = MutableStateFlow(50000.0)
    val cash: StateFlow<Double> = _cash.asStateFlow()

    // Room database flows
    val portfolio: StateFlow<List<PortfolioAsset>> = repository.portfolio
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<SecurityTransaction>> = repository.transactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Telemetry and configurations
    private val _cpuLoad = MutableStateFlow(15.0)
    val cpuLoad: StateFlow<Double> = _cpuLoad.asStateFlow()

    private val _isTradingActive = MutableStateFlow(true)
    val isTradingActive: StateFlow<Boolean> = _isTradingActive.asStateFlow()

    private val _selectedAlgorithm = MutableStateFlow("SMA Crossover v2.5")
    val selectedAlgorithm: StateFlow<String> = _selectedAlgorithm.asStateFlow()

    private val _riskLevel = MutableStateFlow("Medium") // "Low", "Medium", "High"
    val riskLevel: StateFlow<String> = _riskLevel.asStateFlow()

    private val _leverage = MutableStateFlow(1) // 1x, 2x, 5x, 10x
    val leverage: StateFlow<Int> = _leverage.asStateFlow()

    // Gemini Chat state
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(listOf(
        ChatMessage("Mirage", "Bonjour, je suis l'agent IA Mirage. J'analyse le marché en temps réel et exécute automatiquement vos transactions de manière sécurisée. Comment puis-je vous aider aujourd'hui ?")
    ))
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    // Block height counter for high-frequency confirmation ledger
    private var currentBlockHeight = 18459200L

    init {
        // Initialize Ticker values
        _tickers.value = mapOf(
            "BTC/USD" to TickerState("BTC/USD", "Bitcoin", 84250.0, listOf(84100.0, 84150.0, 84200.0, 84180.0, 84250.0), 1.4),
            "TSLA/USD" to TickerState("TSLA/USD", "Tesla Inc.", 245.50, listOf(244.20, 244.80, 245.10, 245.00, 245.50), -0.8),
            "AAPL/USD" to TickerState("AAPL/USD", "Apple Inc.", 189.20, listOf(188.50, 188.90, 189.10, 189.00, 189.20), 0.5),
            "EUR/USD" to TickerState("EUR/USD", "Euro / Dollar", 1.0850, listOf(1.0830, 1.0840, 1.0845, 1.0848, 1.0850), 0.1)
        )

        // Seed initial mock portfolio asset records in DB if empty
        viewModelScope.launch {
            repository.portfolio.first().let { currentList ->
                if (currentList.isEmpty()) {
                    repository.savePortfolioAsset(PortfolioAsset("BTC/USD", "Bitcoin", 0.25, 83500.0, 84250.0))
                    repository.savePortfolioAsset(PortfolioAsset("TSLA/USD", "Tesla Inc.", 20.0, 240.0, 245.50))
                    repository.savePortfolioAsset(PortfolioAsset("AAPL/USD", "Apple Inc.", 35.0, 185.0, 189.20))
                }
            }
        }

        // Launch market ticker and high frequency trading loop
        startSimulationLoop()
    }

    private fun startSimulationLoop() {
        viewModelScope.launch {
            while (true) {
                delay(1500) // 1.5 seconds tick speed
                if (_isTradingActive.value) {
                    currentBlockHeight += Random.nextLong(1, 4)
                    _cpuLoad.value = Random.nextDouble(12.0, 24.0)

                    // 1. Fluctuating price updates
                    val updatedTickers = _tickers.value.mapValues { (_, state) ->
                        val changePercent = when (_riskLevel.value) {
                            "Low" -> Random.nextDouble(-0.0005, 0.0006)
                            "Medium" -> Random.nextDouble(-0.0015, 0.0018)
                            else -> Random.nextDouble(-0.004, 0.0045) // High risk
                        }
                        val newPrice = state.currentPrice * (1.0 + changePercent)
                        val formattedPrice = when (state.ticker) {
                            "EUR/USD" -> Math.round(newPrice * 10000.0) / 10000.0
                            else -> Math.round(newPrice * 100.0) / 100.0
                        }

                        // Update price history
                        val newHistory = (state.history + formattedPrice).takeLast(15)

                        // Calculate technical indicators (SMA & RSI)
                        val smaS = calculateSMA(newHistory, 3)
                        val smaL = calculateSMA(newHistory, 6)
                        val rsi = calculateMockRSI(newHistory)

                        state.copy(
                            currentPrice = formattedPrice,
                            history = newHistory,
                            dailyChangePercent = state.dailyChangePercent + (changePercent * 100.0),
                            smaShort = smaS,
                            smaLong = smaL,
                            rsi = rsi
                        )
                    }
                    _tickers.value = updatedTickers

                    // Update live asset prices inside the Room portfolio db
                    updatePortfolioAssetPrices(updatedTickers)

                    // 2. High-Frequency Trading Quantitative Decisions
                    evaluateTradingSignals(updatedTickers)
                }
            }
        }
    }

    private fun calculateSMA(history: List<Double>, period: Int): Double {
        if (history.size < period) return history.lastOrNull() ?: 0.0
        val subList = history.takeLast(period)
        return subList.sum() / period
    }

    private fun calculateMockRSI(history: List<Double>): Double {
        if (history.size < 2) return 50.0
        var gains = 0.0
        var losses = 0.0
        for (i in 1 until history.size) {
            val diff = history[i] - history[i - 1]
            if (diff > 0) gains += diff else losses += Math.abs(diff)
        }
        if (losses == 0.0) return 100.0
        val rs = gains / losses
        return 100.0 - (100.0 / (1.0 + rs))
    }

    private suspend fun updatePortfolioAssetPrices(updatedTickers: Map<String, TickerState>) {
        portfolio.value.forEach { asset ->
            updatedTickers[asset.ticker]?.let { state ->
                repository.savePortfolioAsset(asset.copy(currentPrice = state.currentPrice))
            }
        }
    }

    private suspend fun evaluateTradingSignals(tickerStates: Map<String, TickerState>) {
        // To make simulation responsive but not execute too many trades at once:
        // Set an execution probability based on Risk setting
        val executionThreshold = when (_riskLevel.value) {
            "Low" -> 0.08
            "Medium" -> 0.18
            else -> 0.35 // High risk = triggers more aggressive algorithmic trades
        }

        tickerStates.forEach { (ticker, state) ->
            if (state.history.size >= 6) {
                val prevSmaShort = calculateSMA(state.history.dropLast(1), 3)
                val prevSmaLong = calculateSMA(state.history.dropLast(1), 6)

                val crossedUp = (prevSmaShort <= prevSmaLong) && (state.smaShort > state.smaLong)
                val crossedDown = (prevSmaShort >= prevSmaLong) && (state.smaShort < state.smaLong)

                if ((crossedUp || crossedDown) && Random.nextDouble() < executionThreshold) {
                    val action = if (crossedUp) "BUY" else "SELL"
                    executeAutomatedSecureTrade(ticker, action, state.currentPrice)
                }
            }
        }
    }

    private suspend fun executeAutomatedSecureTrade(ticker: String, action: String, price: Double) {
        val qty = when (ticker) {
            "BTC/USD" -> Math.round((Random.nextDouble(0.01, 0.04)) * 1000.0) / 1000.0
            "EUR/USD" -> Math.round((Random.nextDouble(2000.0, 5000.0)) * 10.0) / 10.0
            else -> Math.round(Random.nextDouble(2.0, 10.0) * 10.0) / 10.0
        }
        val totalCost = qty * price
        val lev = _leverage.value
        val requiredCapital = totalCost / lev

        if (action == "BUY") {
            if (_cash.value >= requiredCapital) {
                _cash.value -= requiredCapital

                val existing = repository.getPortfolioAsset(ticker)
                if (existing != null) {
                    val newQty = existing.quantity + qty
                    val newAvgPrice = ((existing.averagePrice * existing.quantity) + totalCost) / newQty
                    repository.savePortfolioAsset(existing.copy(quantity = newQty, averagePrice = newAvgPrice))
                } else {
                    repository.savePortfolioAsset(PortfolioAsset(ticker, _tickers.value[ticker]?.name ?: ticker, qty, price, price))
                }
                saveTransactionToLedger(ticker, action, price, qty, totalCost)
            }
        } else { // SELL
            val existing = repository.getPortfolioAsset(ticker)
            if (existing != null && existing.quantity >= qty) {
                val finalQty = existing.quantity - qty
                _cash.value += requiredCapital // Return collateral

                // Gain or Loss based on sell price
                val profit = (price - existing.averagePrice) * qty * lev
                _cash.value += profit

                if (finalQty <= 0.001) {
                    repository.deletePortfolioAsset(existing)
                } else {
                    repository.savePortfolioAsset(existing.copy(quantity = finalQty))
                }
                saveTransactionToLedger(ticker, action, price, qty, totalCost)
            }
        }
    }

    private suspend fun saveTransactionToLedger(ticker: String, action: String, price: Double, qty: Double, total: Double) {
        val timestamp = System.currentTimeMillis()
        val algo = _selectedAlgorithm.value
        val block = currentBlockHeight
        val executionMs = Random.nextLong(4, 28)

        // Cryptographic transaction signing simulation
        val rawPayload = "$timestamp-$ticker-$action-$price-$qty-$algo-$block"
        val signature = sha256(rawPayload)

        val tx = SecurityTransaction(
            timestamp = timestamp,
            ticker = ticker,
            action = action,
            price = price,
            quantity = qty,
            total = total,
            signature = signature,
            algorithm = algo,
            blockHeight = block,
            executionTimeMs = executionMs
        )
        repository.insertTransaction(tx)
    }

    private fun sha256(input: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(input.toByteArray())
            hash.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            "err_signature_hash_failed"
        }
    }

    // Toggle high-frequency automation
    fun toggleTradingActive() {
        _isTradingActive.value = !_isTradingActive.value
    }

    // Change trading algorithm parameters
    fun changeAlgorithm(algoName: String) {
        _selectedAlgorithm.value = algoName
    }

    fun changeRiskLevel(level: String) {
        _riskLevel.value = level
    }

    fun changeLeverage(lev: Int) {
        _leverage.value = lev
    }

    fun clearLedger() {
        viewModelScope.launch {
            repository.clearAllTransactions()
        }
    }

    fun resetSimulation() {
        viewModelScope.launch {
            repository.clearAllTransactions()
            repository.clearPortfolio()
            _cash.value = 50000.0

            repository.savePortfolioAsset(PortfolioAsset("BTC/USD", "Bitcoin", 0.25, 83500.0, 84250.0))
            repository.savePortfolioAsset(PortfolioAsset("TSLA/USD", "Tesla Inc.", 20.0, 240.0, 245.50))
            repository.savePortfolioAsset(PortfolioAsset("AAPL/USD", "Apple Inc.", 35.0, 185.0, 189.20))
        }
    }

    // Manual Trade Execution helper from UI
    fun triggerManualTrade(ticker: String, action: String) {
        viewModelScope.launch {
            val currentPrice = _tickers.value[ticker]?.currentPrice ?: return@launch
            executeAutomatedSecureTrade(ticker, action, currentPrice)
        }
    }

    // --- Gemini API Analyst Chat integration ---
    fun sendMessageToAgent(userText: String) {
        if (userText.isBlank()) return

        val userMessage = ChatMessage("User", userText)
        _chatMessages.value = _chatMessages.value + userMessage

        val pendingMirageMsg = ChatMessage("Mirage", "Analyse en cours...", isPending = true)
        _chatMessages.value = _chatMessages.value + pendingMirageMsg

        viewModelScope.launch {
            val responseText = callGeminiApiForAnalystResponse(userText)
            _chatMessages.value = _chatMessages.value.filter { !it.isPending } + ChatMessage("Mirage", responseText)
        }
    }

    private suspend fun callGeminiApiForAnalystResponse(query: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Désolé, l'API Gemini n'est pas configurée dans les variables d'environnement. Veuillez entrer votre clé API dans le panneau Secrets (GEMINI_API_KEY) d'AI Studio."
        }

        // Construct rich market context for the prompt
        val tickerContext = _tickers.value.entries.joinToString("\n") { (t, state) ->
            "- $t: $${state.currentPrice} (Var. quotidienne: ${String.format("%.2f", state.dailyChangePercent)}%, RSI: ${String.format("%.1f", state.rsi)}, SMA Court: ${String.format("%.2f", state.smaShort)})"
        }
        val pfAssets = portfolio.value.joinToString("\n") { asset ->
            "- ${asset.name} (${asset.ticker}): ${asset.quantity} unités, acheté en moy. à $${asset.averagePrice} (Valeur act: $${asset.currentPrice})"
        }
        val totalAssetsValue = portfolio.value.sumOf { it.quantity * it.currentPrice }
        val totalNetWorth = _cash.value + totalAssetsValue

        val systemPrompt = """
            Vous êtes 'Mirage', un agent d'intelligence artificielle spécialisé dans l'analyse de données financières en temps réel et l'exécution automatisée de transactions quantitatives à haute fréquence.
            Vous agissez de façon professionnelle, ultra-précise et pédagogique, similaire à un chercheur quantitatif principal (Senior Quantitative Researcher).
            Donnez des analyses techniques pointues et utilisez un ton confiant, précis et axé sur la technologie.
            Répondez exclusivement en français.
            
            Voici le statut actuel de votre système de trading :
            - Solde de liquidités (Cash): ${String.format("%.2f", _cash.value)} USD
            - Valeur du portefeuille crypto/actions: ${String.format("%.2f", totalAssetsValue)} USD
            - Valeur nette totale (Net Worth): ${String.format("%.2f", totalNetWorth)} USD
            - Paramètres actifs: Algorithme: ${_selectedAlgorithm.value}, Niveau de Risque: ${_riskLevel.value}, Effet de Levier: ${_leverage.value}x
            
            Données du marché financier surveillées en temps réel :
            $tickerContext
            
            Actifs actuellement en portefeuille :
            $pfAssets
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(GeminiPart(text = query))
                )
            ),
            generationConfig = GeminiGenerationConfig(temperature = 0.5f),
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt)))
        )

        try {
            val response = GeminiClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Mirage n'a pas pu formuler de réponse. Veuillez réessayer."
        } catch (e: Exception) {
            "Erreur d'analyse réseau : ${e.localizedMessage ?: "Problème de connexion avec le serveur Mirage."}"
        }
    }
}

class TradingViewModelFactory(
    private val application: Application,
    private val repository: TradingRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TradingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TradingViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
