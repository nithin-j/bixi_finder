package com.example.bixifinder.model

class ItemList {

    var name: String? = null
    var rating: Int? = 0
    var title: String? = null
    var review: String? = null
    var reviewDate: String? = null

    constructor(name: String?, rating: Int?, title: String?, review: String?, reviewDate: String?) {
        this.name = name
        this.rating = rating
        this.title = title
        this.review = review
        this.reviewDate = reviewDate
    }
}