package com.example.veoassignment

data class DirectionResponse(
    val route: DirectionRoute?
)

data class DirectionRoute(
    val legs: List<Legs>?
) {
    data class Legs(
        val maneuvers: List<Maneuver>
    ) {
        data class Maneuver(
            val startPoint: LngLat
        ) {
            data class LngLat(
                val lng: Double,
                val lat: Double
            )
        }
    }
}