package com.example.bixifinder.dbContext

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class AccountDetails(val id: String = "", var name: String = "", var address: String = "", var pinCode: String = "", var dob: String = "",
                          var gender: String = "", var membershipType: String = "", val membershipStatus: String = "",
                          val membershipDate: String = "", val validUpTo: String = "")