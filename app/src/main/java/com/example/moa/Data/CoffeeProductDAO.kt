package com.example.project.Data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.project.Data.CoffeeProduct

@Dao
interface CoffeeProductDAO {

    @Insert
    fun insertProduct(product: CoffeeProduct)

    @Query("SELECT * FROM coffeeProducts WHERE pName = :name")
    fun findProduct(name:String): List<CoffeeProduct>

    @Query("DELETE FROM coffeeProducts WHERE pName = :name")
    fun deleteProduct(name:String)

    @Query("SELECT * FROM coffeeProducts")
    fun getAll():LiveData<List<CoffeeProduct>>

}