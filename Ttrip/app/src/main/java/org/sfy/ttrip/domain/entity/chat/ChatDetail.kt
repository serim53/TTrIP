package org.sfy.ttrip.domain.entity.chat

data class ChatDetail(
    val isMine: Boolean,
    val content: String,
    val createdDate: String
)
