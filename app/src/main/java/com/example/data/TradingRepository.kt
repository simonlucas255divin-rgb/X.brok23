package com.example.data

import kotlinx.coroutines.flow.Flow

class TradingRepository(private val dao: TradingDao) {
    val transactions: Flow<List<SecurityTransaction>> = dao.getTransactionsFlow()
    val portfolio: Flow<List<PortfolioAsset>> = dao.getPortfolioFlow()

    suspend fun insertTransaction(transaction: SecurityTransaction) {
        dao.insertTransaction(transaction)
    }

    suspend fun clearAllTransactions() {
        dao.clearAllTransactions()
    }

    suspend fun getPortfolioAsset(ticker: String): PortfolioAsset? {
        return dao.getPortfolioAsset(ticker)
    }

    suspend fun savePortfolioAsset(asset: PortfolioAsset) {
        dao.insertPortfolioAsset(asset)
    }

    suspend fun deletePortfolioAsset(asset: PortfolioAsset) {
        dao.deletePortfolioAsset(asset)
    }

    suspend fun clearPortfolio() {
        dao.clearPortfolio()
    }
}
