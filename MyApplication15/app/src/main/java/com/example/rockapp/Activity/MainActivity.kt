package com.example.rockapp.Activity

import android.app.AppOpsManager
import android.app.Service
import android.app.admin.DevicePolicyManager
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Process
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.widget.ListView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.room.Room
import com.example.rockapp.*
import com.example.rockapp.Receiver.Admin
import com.example.rockapp.databinding.ActivityMainBinding
import com.example.rockapp.db.RockPackageDataBase
import com.example.rockapp.db.RockPackageEntity
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener{

    val handler = Handler()
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    private val model : ViewModelList by viewModels()

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //DB接続(外部)
//        thread {
//            var con : Connection? = null
//            var state : Statement? = null
//            var result : ResultSet? = null
//            try{
//                con = DriverManager.getConnection("jdbc:postgresql://10.0.2.2:5432/demo_env_n?user=demo_env_n&password=demo_env_n")
//                state = con.createStatement()
//                result = state.executeQuery("select asb_bunsekinm from asb_dtl_teiryo_r")
//                while (result.next()) {
//                    System.out.println(result.getString("asb_bunsekinm"))
//                    System.out.println("---------------------------------");
//                }
//            } catch (e : Exception){
//                e.printStackTrace();
//            } finally {
//                if (result != null) { result.close() }
//                if (state != null) { state.close() }
//                if (con != null) { con.close() }
//            }
//        }

        //サービスが起動していなかったら
        if(!MyApplication().getLaunchServiceFlg()){
            //サービス起動（テスト）
            ContextCompat.startForegroundService(this, Intent(this, MyAccessibilityService::class.java))
        }

        //使用状況の許可
        if (!checkReadStatsPermission()) {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        }

        //端末管理者としての許可を求める
        requestDeviceAdmin()

        //画面オーバーレイの許可
        if (Settings.canDrawOverlays(this)) {
            //許可されている
        } else {
            // 許可されていない
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }

        //画面表示
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupDrawer()
        
//        //データ更新時に実行
//        model.liveData.observe(
//            this, Observer { it ->
////                foo = it
//            }
//        )

        GlobalScope.launch(Dispatchers.IO) {
            val database = Room.databaseBuilder(MyApplication().getInstance(), RockPackageDataBase::class.java, "tbl_rock_app").build()
            val userDao = database.rockPackageDao()
            var getApp = userDao.get() //ロック対象データ

            handler.post{
                var list_view = findViewById<ListView>(R.id.list_view)
                //アダプターにユーザーリストを導入
                val Adapter = ListAdapter(MyApplication().getInstance(), create(MyApplication().getInstance()) as ArrayList<Data>, getApp)
                //リストビューにアダプターを設定
                list_view.adapter = Adapter
            }
        }

        //リスト押下時にDBに追加
//        list_view.setOnItemClickListener { adapterView, _, position, _ ->
//            val appName = adapterView.getItemAtPosition(position) as Data
//            val database = Room.databaseBuilder(MyApplication().getInstance(), RockPackageDataBase::class.java, "tbl_rock_app").build()
//            val userDao = database.rockPackageDao()
//            //DB処理はサブスレッド使用
//            GlobalScope.launch(Dispatchers.IO) {
//                var getApp = userDao.get() //ロック対象データ
//                //対象データに入っていたら
//                if (getApp != null) {
//                    //ロック対象データに入っていたら削除
//                    if(getApp.contains(appName.email)){
//                        userDao.deleteApp(appName.email)
//                        handler.post{Toast.makeText(MyApplication().getContext(), appName.email + "を削除しました", Toast.LENGTH_SHORT).show()}
//                        //ロック対象データに入っていなければ追加
//                    } else {
//                        val newUser = RockPackageEntity(0, appName.email)
//                        userDao.createAddress(newUser)
//                        handler.post{Toast.makeText(MyApplication().getContext(), appName.email + "を追加しました", Toast.LENGTH_SHORT).show()}
//                    }
//                    //ロック対象データに入っていなければ追加
//                } else {
//                    val newUser = RockPackageEntity(0, appName.email)
//                    userDao.createAddress(newUser)
//                    handler.post{Toast.makeText(MyApplication().getContext(), appName.email + "を追加しました", Toast.LENGTH_SHORT).show()}
//                }
//            }
//        }
    }

    //パーミッションのチェック
    private fun checkReadStatsPermission(): Boolean {
        // AppOpsManagerを取得
        val aom = getSystemService(APP_OPS_SERVICE) as AppOpsManager
        // GET_USAGE_STATSのステータスを取得
        val mode = aom.checkOp(
            AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(),
            packageName
        )
        return if (mode == AppOpsManager.MODE_DEFAULT) {
            // AppOpsの状態がデフォルトなら通常のpermissionチェックを行う。
            // 普通のアプリならfalse
            checkPermission(
                "android.permission.PACKAGE_USAGE_STATS",
                Process.myPid(),
                Process.myUid()
            ) == PackageManager.PERMISSION_GRANTED
        } else mode == AppOpsManager.MODE_ALLOWED
        // AppOpsの状態がデフォルトでないならallowedのみtrue
    }

    inner class SettingsChangeReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // 設定が開かれたときの処理をここに記述する
            Log.d("SettingsChangeReceiver", "Settings was opened")
        }
    }

    /**
     * 端末管理者としての許可を求める
     */
    private fun requestDeviceAdmin() : Boolean {
        val dpm = getSystemService(Service.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val componentName = ComponentName(this, Admin::class.java)

        if(dpm.isAdminActive(componentName) == false){
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).also {
                it.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
            }
            startActivity(intent)
            return false
        } else {
            return true
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onRestart() {
        super.onRestart()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        finish()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    /**
     * アプリ一覧表示メソッド
     */
    fun create(context: Context): List<Data> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN)
            .also { it.addCategory(Intent.CATEGORY_LAUNCHER) }
        return pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            .asSequence()
            .mapNotNull { it.activityInfo }
            .filter { it.packageName != context.packageName } //このアプリはのぞく
            .map {
                Data(
                    it.loadIcon(pm),
                    it.loadLabel(pm).toString(),
                    it.packageName
                )
            }
            .sortedBy { it.appNm }
            .toList()
    }

    /**
     * ドロワーメニューの設定
     */
    private fun setupDrawer() {
        // ドロワーレイアウトの取得
        drawerLayout = binding.drawerLayout
        // ナビゲーションビューの取得
        val navigationView: NavigationView = binding.navView
        // アクションバーのドロワートグル設定
        toggle = ActionBarDrawerToggle(
            this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        // ドロワーリスナーの追加
        drawerLayout.addDrawerListener(toggle)
        // トグルの状態を同期
        toggle.syncState()
        // ドロワーアイコンの表示設定
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        //ナビゲーションアイテムが選択されたときの処理
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                // ロック情報更新
                R.id.nav_loc_update -> {
                    println("aaa")
                    true
                }
                // 接続先サーバー設定
                R.id.nav_server_config -> {
                    println("bbb")
                    true
                }
                else -> {
                    false
                }
            }
        }
    }

    // オプションメニューのアイテムが選択されたときの処理
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // トグルが選択された場合
        return if (toggle.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)
    }

    /**
     * 戻るボタンが押されたときの処理
     */
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // ドロワーが開いている場合
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            // ドロワーを閉じる
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            // 通常の戻るボタンの処理
//            onBackPressedDispatcher.onBackPressed()
            // 書かなければ無効にできる
        }
    }

    /**
     * ナビゲーションアイテムが選択されたときの処理
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // ドロワーを閉じる
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}