package com.bonnelife.spots.database.model

data class SpotDTO(
    var userId : String? = null,
    var uid : String? = null,
    var message: String? = null,
    var latitude : Double? = null,
    var longitude : Double? = null,
    var favorites : MutableMap<String,Boolean> = HashMap(),
    var imageUrl : String? = null,
    var timestamp : Long? = null
)