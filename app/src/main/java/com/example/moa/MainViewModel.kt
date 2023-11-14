package com.example.moa

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.project.Data.CoffeeProduct
import com.example.project.Data.CoffeeProductRoomDB
import com.example.project.Data.CoffeeRepository

//애플리케이션 참조를 인수로 전달하려면 프로바이더 팩토리 필요
class MainViewModel(application: Application): ViewModel() {

    val allProducts: LiveData<List<CoffeeProduct>>
    private val repository: CoffeeRepository
    val searchResults: MutableLiveData<List<CoffeeProduct>>

    //DB를 하나로 만들어줌
    init {
        val productDb= CoffeeProductRoomDB.getInstance(application)
        val productDao = productDb.coffeeProductDao()
        repository = CoffeeRepository(productDao)

        allProducts = repository.allProducts
        searchResults = repository.searchResults
    }
    fun insertProduct(product: CoffeeProduct) {
        repository.insertProduct(product)
    }

    fun findProduct(name:String) {
        repository.findProduct(name)
    }

    fun deleteProduct(name: String) {
        repository.deleteProduct(name)
    }
}
