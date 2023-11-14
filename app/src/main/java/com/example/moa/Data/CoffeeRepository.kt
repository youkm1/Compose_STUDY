package com.example.project.Data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.project.Data.CoffeeProduct
import com.example.project.Data.CoffeeProductDAO
import kotlinx.coroutines.*


class CoffeeRepository(private val coffeeProductDao: CoffeeProductDAO) {
    private  val coroutineScope = CoroutineScope(Dispatchers.Main)
    val searchResults = MutableLiveData<List<CoffeeProduct>>()
    val allProducts: LiveData<List<CoffeeProduct>> = coffeeProductDao.getAll()
    //lazycolumn, 사용자 인터페이스에서는 항상 최신상태의 데이터 목록을 받아야하는데 이때, 쿼리 결과문을
    // 라이브데이터 객체로 감싸 반환
    // 이유는?

    fun insertProduct(newProduct: CoffeeProduct){
        coroutineScope.launch ( Dispatchers.IO ){
            coffeeProductDao.insertProduct(newProduct)
        }
    }

    fun deleteProduct(name:String) {
        coroutineScope.launch ( Dispatchers.IO ) {
           coffeeProductDao.deleteProduct(name)
        }
    }

    private fun asyncFind(name: String): Deferred<List<CoffeeProduct>?> =
        coroutineScope.async (Dispatchers.IO){
            return@async coffeeProductDao.findProduct(name)
        }

    fun findProduct(name:String) {
        coroutineScope.async (Dispatchers.Main){
            searchResults.value = asyncFind(name).await()
        }

    }


}