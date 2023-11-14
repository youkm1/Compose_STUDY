package com.example.project.Data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [(CoffeeProduct::class)], version = 1)
abstract class CoffeeProductRoomDB : RoomDatabase() {

    abstract fun coffeeProductDao(): CoffeeProductDAO

    //companion object 출현 이유
    companion object {
        private var INSTANCE: CoffeeProductRoomDB? = null

        fun getInstance(context: Context): CoffeeProductRoomDB {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        CoffeeProductRoomDB::class.java,
                        "product_database"
                    ).fallbackToDestructiveMigration().build()


                }
                return instance
            }
        }

    }
}