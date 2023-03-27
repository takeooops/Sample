package com.example.rockapp.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [RockPackageEntity::class], version = 1, exportSchema = false)
abstract class RockPackageDataBase : RoomDatabase() {
    abstract fun rockPackageDao(): RockPackageDao
}