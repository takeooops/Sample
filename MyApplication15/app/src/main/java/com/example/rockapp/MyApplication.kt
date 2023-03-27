package com.example.rockapp

import android.app.Application
import android.content.Context

class MyApplication : Application() {

    companion object {
        lateinit var instance : MyApplication private set
        private var mContext: Context? = null
        var relieveRock = false //ロックが解除されているかのフラグ
        var sleepFlg = false //画面がスリープ状態であるかフラグ
        var launchServiceFlg = false //サービスが開始されているかのフラグ
        var rockDisplayOnFlg = false
    }

    @JvmName("getInstance")
    fun getInstance() : MyApplication {return  instance}

    fun getContext(): Context? {
        return mContext
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        mContext = this
    }

    fun setRelieveRock(item : Boolean){
        relieveRock = item
    }

    fun getRelieveRock(): Boolean{
        return relieveRock
    }

    fun setSleepFlg(item : Boolean){
        sleepFlg = item
    }

    fun getSleepFlg(): Boolean{
        return sleepFlg
    }

    fun setLaunchServiceFlg(item : Boolean){
        launchServiceFlg = item
    }

    fun getLaunchServiceFlg(): Boolean{
        return launchServiceFlg
    }

    fun setRockDisplayOnFlg(item : Boolean){
        rockDisplayOnFlg = item
    }

    fun getRockDisplayOnFlg(): Boolean{
        return rockDisplayOnFlg
    }
}