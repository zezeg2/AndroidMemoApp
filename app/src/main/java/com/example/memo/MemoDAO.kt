package com.example.memo

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query

@Dao
interface MemoDAO {
    @Insert(onConflict = REPLACE)
    fun insert(memo : MemoEntity)

    @Query("select * from memo")
    fun getAll() : List<MemoEntity>

    @Delete
    fun delete(memo: MemoEntity)
}