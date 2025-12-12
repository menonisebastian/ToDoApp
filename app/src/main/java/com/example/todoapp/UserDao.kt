package com.example.todoapp

import androidx.room.Dao
import androidx.room.Query

@Dao
interface UserDao
{
    @Query("SELECT * FROM user WHERE username = :username")
    suspend fun getUserByUsername(username: String): User?
}