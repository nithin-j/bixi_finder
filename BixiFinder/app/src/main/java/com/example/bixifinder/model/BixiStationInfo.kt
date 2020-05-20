package com.example.bixifinder.model

data class BixiStationInfo(val stationId: Int, val externalId: String, val name: String, val shortName: Int, val latitude: Double,
                           val longitude: Double, /*val rentalMethods: ArrayList<String>,*/ val capacity: Int, val electricBikeSurchargeWavier: Boolean,
                           val eightdHasKeyDispenser: Boolean)