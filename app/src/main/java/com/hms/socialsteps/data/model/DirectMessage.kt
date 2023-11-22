package com.hms.socialsteps.data.model

data class DirectMessage(
    var userList: List<Users>,
    var messages: List<MessageItem>
)
