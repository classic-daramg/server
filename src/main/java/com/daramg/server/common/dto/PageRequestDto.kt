package com.daramg.server.common.dto

data class PageRequestDto(
    val cursor: String?,
    val size: Int?
){
    val validatedSize: Int
        get() = if (size == null || size <= 0) {
            DEFAULT_SIZE
        } else {
            size
        }

    companion object {
        private const val DEFAULT_SIZE = 10
    }
}