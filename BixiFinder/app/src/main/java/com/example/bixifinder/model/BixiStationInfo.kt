package com.example.bixifinder.model

data class BixiStationInfo(val stationId: Int, val externalId: String, val name: String, val shortName: Int, val latitude: Double,
                           val longitude: Double, val capacity: Int, val electricBikeSurchargeWavier: Boolean,
                           val eightdHasKeyDispenser: Boolean)