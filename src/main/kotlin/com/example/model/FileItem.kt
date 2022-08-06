package com.example.model

@kotlinx.serialization.Serializable
data class FileItem(
    //唯一路径标识
    val path: String,
    val name: String,
    val length: Long,
    val lastModified: Long,
    val isDirectory: Boolean,
    //MIME type
    val type: String,
    val childrenSize: Int
)
