package com.maks.caloriecounter.data.repository

import com.maks.caloriecounter.data.local.dao.ProductDao
import com.maks.caloriecounter.domain.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProductRepository(private val productDao: ProductDao) {
    fun observeProducts(): Flow<List<Product>> = productDao.observeAllProducts().map { products -> products.map { it.toDomain() } }

    suspend fun getProduct(id: Long): Product? = productDao.getProductById(id)?.toDomain()

    suspend fun findProductByName(name: String): Product? = productDao.findProductByName(name.trim())?.toDomain()

    suspend fun findProductByBarcode(barcode: String): Product? = productDao.findProductByBarcode(barcode.trim())?.toDomain()
    
    suspend fun findProductByBarcodes(barcodes: List<String>): Product? {
        for (barcode in barcodes.map { it.trim() }.filter { it.isNotBlank() }.distinct()) {
            val product = findProductByBarcode(barcode)
            if (product != null) return product
        }
        return null
    }
    
    suspend fun insertProduct(product: Product): Long = productDao.insertProduct(product.toEntity())

    suspend fun upsertProductByName(product: Product): Long {
        val existing = findProductByName(product.name)
        return if (existing == null) {
            insertProduct(product.copy(name = product.name.trim()))
        } else {
            updateProduct(
                product.copy(
                    id = existing.id,
                    createdAt = existing.createdAt,
                    isFavorite = existing.isFavorite,
                    lastUsedAt = existing.lastUsedAt,
                    barcode = product.barcode ?: existing.barcode,
                    barcodeFormat = product.barcodeFormat ?: existing.barcodeFormat,
                    source = product.source.takeIf { it != "manual" } ?: existing.source,
                ),
            )
            existing.id
        }
    }

    suspend fun updateProduct(product: Product) = productDao.updateProduct(product.toEntity())

    suspend fun deleteProduct(product: Product) = productDao.deleteProduct(product.toEntity())

    suspend fun toggleFavorite(product: Product) = productDao.updateFavorite(product.id, !product.isFavorite)

    suspend fun updateLastUsedAt(productId: Long, lastUsedAt: Long = System.currentTimeMillis()) = productDao.updateLastUsedAt(productId, lastUsedAt)
}
