package com.hms.socialsteps.data.model

enum class TargetType {
    STEP{
        override fun toString(): String {
            return "STEP"
        }
    },
    CALORIE{
        override fun toString(): String {
            return "CALORIE"
        }
    },
    STEPCALORIE{
        override fun toString(): String {
            return "STEP&CALORIE"
        }
    }
}