package com.hms.socialsteps.utils

class MapUtils {
    companion object {
        const val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"
        const val API_KEY = "DAEDAGO6nr3yZbf/tl5LuUraFc2X6H/SQ+PGDM+HrQoGj3/qiYH84nZPeOPw9x02PzfGZaEfSUjK7brcb5x989xirHo+nzZcvY6c7Q=="
        const val ZOOM_VALUE: Float = 15.0F
    }

    enum class DirectionType(val type: String) {
        WALKING("walking"),
        BICYCLING("bicycling"),
        DRIVING("driving")
    }
}