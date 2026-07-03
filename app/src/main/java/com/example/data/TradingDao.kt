package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TradingDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getTransactionsFlow(): Flow<List<SecurityTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: SecurityTransaction)

    @Query("DELETE FROM transactions")
    suspend fun clearAllTransactions()

    @Query("SELECT * FROM portfolio")
    fun getPortfolioFlow(): Flow<List<PortfolioAsset>>

    @Query("SELECT * FROM portfolio WHERE ticker = :ticker LIMIT 1")
    suspend fun getPortfolioAsset(ticker: String): PortfolioAsset?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPortfolioAsset(asset: PortfolioAsset)

    @Update
    suspend fun updatePortfolioAsset(asset: PortfolioAsset)

    @Delete
    suspend fun deletePortfolioAsset(asset: PortfolioAsset)

    @Query("DELETE FROM portfolio")
    suspend fun clearPortfolio()
}
