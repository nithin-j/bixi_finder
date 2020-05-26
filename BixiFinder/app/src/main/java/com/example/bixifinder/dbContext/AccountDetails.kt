package com.example.bixifinder.dbContext

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import kotlin.collections.mapOf

@IgnoreExtraProperties
data class AccountDetails(val id: String = "", val name: String = "", val address: String = "", val pinCode: String = "", val dob: String = "",
                          val gender: String = "", val membershipType: String = "", val membershipStatus: String = "",
                          val membershipDate: String = "", val validUpTo: String = "")