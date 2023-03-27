package com.example.rockapp

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.*
import android.os.Handler
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.room.Room
import com.example.rockapp.Activity.InputPinActivity
import com.example.rockapp.db.RockPackageDataBase
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class MyAccessibilityService : Service() {

    private var lastForegroundAppPackage: String? = null

    private var foregroundAppDisposable: Disposable? = null

    val handler = Handler()

    val appContext = MyApplication().getContext() as Context

    //DB検索
    val database = Room.databaseBuilder(MyApplication().getInstance(), RockPackageDataBase::class.java, "tbl_rock_app").build()
    val userDao = database.rockPackageDao()
    var getApp: List<String>? = null

    private var screenOnOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_ON -> {
                    println("スクリーンON")
                    MyApplication().setSleepFlg(false) //スリープ状態のフラグをオフに
                }
                Intent.ACTION_SCREEN_OFF -> {
                    //スクリーンOFF時、ロック画面を表示
                    println("スクリーンOFF")
                    //タスクをすべて削除
                    val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                    am.appTasks.forEach { it.finishAndRemoveTask() }
                    //画面表示
                    val intent = InputPinActivity.newIntent(appContext,"com.example.rockapp")
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION
                    handler.post{startActivity(intent)}
                    MyApplication().setRelieveRock(false) //ロック解除フラグをオフに
                    MyApplication().setSleepFlg(true) //スリープ状態のフラグをオンに
                }
            }
        }
    }

    companion object {
        val CHANNEL_ID = "1"
    }

    override fun onBind(p0: Intent?): IBinder? {
        println("サービス開始")
        return null
    }

    override fun onCreate() {
        super.onCreate()
    }

    @SuppressLint("ServiceCast")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        //サービス開始フラグをオンに
        MyApplication().setLaunchServiceFlg(true)

        //通知の準備
        initNotify()

        //ロックアプリ一覧取得
        GlobalScope.launch(Dispatchers.IO) {
            getApp = userDao.get()
        }

        //アプリ一覧定期取得
        val schedule = Executors.newSingleThreadScheduledExecutor()
        //10秒ごとに値更新
        schedule.scheduleAtFixedRate(
            {
                getApp = userDao.get()
            },
            0,10000, TimeUnit.MILLISECONDS
        )

        //フォアグラウンドアプリの監視
        initForegroundApp()

        //レシーバー準備
        registerScreenReceiver()

        //サービス終了時、サービスを再起動
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        //レシーバを止める
        unregisterReceiver(screenOnOffReceiver)
    }

    //レシーバ作成
    private fun registerScreenReceiver() {
        val screenFilter = IntentFilter()
        screenFilter.addAction(Intent.ACTION_SCREEN_ON)
        screenFilter.addAction(Intent.ACTION_SCREEN_OFF)
        registerReceiver(screenOnOffReceiver, screenFilter)
    }

    /**
     * 通知の表示
     */
    fun initNotify(){

        //テスト
        val mChannel = NotificationChannel(
            CHANNEL_ID, // id: パッケージ内で一意の、通知チャネルの識別ID(String型)
            "ここが通知チャネル名として表示される", // name: ユーザに表示される通知チャネル名
            NotificationManager.IMPORTANCE_DEFAULT // importance: 通知チャネルの重要度
        )
        mChannel.apply {
            description = "ここが通知チャネルの説明として表示される"
        }
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        // 既存の通知チャネルを作成しても問題ない
        manager.createNotificationChannel(mChannel)

        // サービスをフォアグラウンドで実行　Android 8.0 よりも前では `CHANNEL_ID` は無視される
        val notification = NotificationCompat.Builder(this, CHANNEL_ID).build()
        startForeground(1, notification)
    }

    /**
     * アプリがフォアグラウンドになったことを検知
     */
    fun initForegroundApp(){
        //フォアグラウンドのアプリを監視
        foregroundAppDisposable = getForegroundObservableHigherLollipop()
            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { foregroundAppPackage -> onAppForeground(foregroundAppPackage) },
                { error -> println("bbb") }
            )
    }

    /**
     * フォアグラウンドアプリの監視
     */
    fun getForegroundObservableHigherLollipop(): Flowable<String> {
        return Flowable.interval(100, TimeUnit.MILLISECONDS)
            .map {
                var usageEvent: UsageEvents.Event? = null
                val mUsageStatsManager = appContext.getSystemService(
                    USAGE_STATS_SERVICE
                ) as UsageStatsManager
                val time = System.currentTimeMillis()
                val usageEvents = mUsageStatsManager.queryEvents(time - 1000, time)
                val event = UsageEvents.Event()
                while (usageEvents.hasNextEvent()) {
                    usageEvents.getNextEvent(event)
                    //MOVE_TO_FOREGROUND アプリがフォアグラウンドになったときのイベントを取得
                    if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                        usageEvent = event
                    }
                }
                UsageEventWrapper(usageEvent)
            }
            .filter { it.usageEvent != null } //イベント名がnullでないもの
            .map { it.usageEvent }
            .filter { it.className != null } //クラス名がnullでないもの
            .filter { it.className.contains(InputPinActivity::class.java.simpleName).not() }
            .map { it.packageName }
            .distinctUntilChanged() //重複をなくす
    }

    /**
     * ロックするか確認
     */
    private fun onAppForeground(foregroundAppPackage: String) {
        if(MyApplication().getRockDisplayOnFlg() == false){
            //フォアグラウンドアプリが存在しているか確認
            if (getApp != null) {
                println(foregroundAppPackage)
                //ロックが解除されているか確認
                if(!MyApplication().getRelieveRock()){
                    //リストの一覧にある場合
                    if (getApp!!.contains(foregroundAppPackage)) {
                        if(
                            (MyApplication().getSleepFlg() == false) //スリープ状態ではない場合、ロック画面表示（スリープ状態中ロック画面を表示しないのは電話の画面を表示させるため）
                        ){
                            println("ロック画面表示")
                            val intent =
                                appContext.let { InputPinActivity.newIntent(it,foregroundAppPackage) }
                            if (lastForegroundAppPackage == "com.example.rockapp") {
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION
                            } else {
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION
                            }
                            handler.post{startActivity(intent)}
//                            startActivity(intent)
                        }
                    }
                }
            }
        }

        //最後にフォアグラウンドになったアプリとして登録
        lastForegroundAppPackage = foregroundAppPackage
    }
}
