package com.maks.caloriecounter.data.repository

import com.maks.caloriecounter.data.local.dao.ProductDao
import com.maks.caloriecounter.domain.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProductRepository(private val productDao: ProductDao) {
    fun observeProducts(): Flow<List<Product>> = productDao.observeAllProducts().map { products -> products.map { it.toDomain() } }

    suspend fun getProduct(id: Long): Product? = productDao.getProductById(id)?.toDomain()

    suspend fun findProductByName(name: String): Product? = productDao.findProductByName(name.trim())?.toDomain()

    suspend fun insertProduct(product: Product): Long = productDao.insertProduct(product.toEntity())

    suspend fun upsertProductByName(product: Product): Long {
        val existing = findProductByName(product.name)
        return existing?.id ?: insertProduct(product.copy(name = product.name.trim()))
    }

    suspend fun updateProduct(product: Product) = productDao.updateProduct(product.toEntity())

    suspend fun deleteProduct(product: Product) = productDao.deleteProduct(product.toEntity())
}
