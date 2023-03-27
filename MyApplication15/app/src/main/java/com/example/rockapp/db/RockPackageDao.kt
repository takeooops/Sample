package com.example.rockapp.db

import androidx.lifecycle.LiveData
import androidx.room.*
import io.reactivex.Flowable

@Dao
interface RockPackageDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun createAddress(packageName: RockPackageEntity)

    @Query("SELECT package_nm FROM tbl_rock_app")
    fun get(): List<String>?

    @Query("DELETE FROM tbl_rock_app WHERE package_nm = :packageNm")
    fun deleteApp(packageNm : String)

    @Delete
    fun delete(packageName: RockPackageEntity)

    /**
     * LiveDataで監視する
     */
    @Query("SELECT * FROM tbl_rock_app")
    fun listMembersLiveData(): LiveData<List<RockPackageEntity>>

    @Query("SELECT package_nm FROM tbl_rock_app")
    fun getLockedApps(): Flowable<List<String>>

}