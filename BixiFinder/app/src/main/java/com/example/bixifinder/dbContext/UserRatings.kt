package com.example.bixifinder.dbContext

data class UserRatings(val id: String = "",val name: String = "", val userEmail: String = "",val rating: Int = 4,
                       val title: String = "N/A", val review: String = "N/A", val reviewDate: String = "")