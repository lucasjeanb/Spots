package com.bonnelife.spots.database.model

data class ContentDTO(var explain : String? = null,
                      var imageUrl : String? = null,
                      var uid : String? = null,
                      var userId : String? = null,
                      var timestamp : Long? = null,
                      var favoriteCount : Int = 0,
                      var favorites : MutableMap<String,Boolean> = HashMap()){
    data class Friend(var uid : String? = null,
                       var userId : String? = null,
                       var friend : Boolean? = null,
                       var timestamp : Long? = null)
}