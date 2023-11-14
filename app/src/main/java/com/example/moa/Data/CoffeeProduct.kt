package com.example.project.Data

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "coffeeProducts")
class CoffeeProduct {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name="pId")
    var id: Int = 0

    @ColumnInfo(name="pName")
    var pName: String = ""

    var quantity: Int = 0

    constructor()

    constructor(pName: String, quantity: Int) {
        this.pName = pName
        this.quantity = quantity
    }
}