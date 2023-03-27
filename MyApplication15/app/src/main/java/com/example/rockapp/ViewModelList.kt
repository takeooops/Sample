package com.example.rockapp

import androidx.lifecycle.ViewModel
import androidx.room.Room
import com.example.rockapp.db.RockPackageDataBase

class ViewModelList  : ViewModel(){
    val database = Room.databaseBuilder(MyApplication().getInstance(), RockPackageDataBase::class.java, "tbl_rock_app").build()
    val userDao = database.rockPackageDao()
    var liveData = userDao.listMembersLiveData() //更新対象データ
}