package com.example.bixifinder.dbContext

import com.google.gson.internal.LinkedTreeMap

data class AccessPass(val id: Int, val name: String, val description: String, val price: Double){
    constructor() : this(0, "",
        "", 0.0)

    override fun toString(): String {
        return name
    }
}