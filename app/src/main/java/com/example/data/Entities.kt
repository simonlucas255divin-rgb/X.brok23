package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "transactions")
@JsonClass(generateAdapter = true)
data class SecurityTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val ticker: String,
    val action: String, // "BUY", "SELL"
    val price: Double,
    val quantity: Double,
    val total: Double,
    val signature: String, // SHA-256 cryptographic verification seal
    val algorithm: String,
    val blockHeight: Long, // Mock ledger confirmation block
    val executionTimeMs: Long // HFT speed metric
)

@Entity(tableName = "portfolio")
@JsonClass(generateAdapter = true)
data class PortfolioAsset(
    @PrimaryKey val ticker: String,
    val name: String,
    val quantity: Double,
    val averagePrice: Double,
    val currentPrice: Double
)
