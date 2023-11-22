package com.hms.socialsteps.data.model

data class Group(
    var groupInformation: GroupInformation,
    var members: List<Users>
)
