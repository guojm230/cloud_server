package com.example.clouddemo.model

/**
 * 一个账户对应多个用户
 */
data class Account(
    val id: Int,
    val password: String,
    val tel: String,
    val email: String,
    val users: List<User>
)
