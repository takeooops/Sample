package com.example.rockapp.Activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.rockapp.MyApplication
import com.example.rockapp.R
import com.example.rockapp.databinding.ActivityInputPinBinding

class InputPinActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInputPinBinding
    private val correctPin = "1234"
    private val enteredPin = mutableListOf<String>()
    private val pinDigits: List<ImageView> by lazy {
        listOf(
            binding.pinDigit1,
            binding.pinDigit2,
            binding.pinDigit3,
            binding.pinDigit4
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MyApplication().setRockDisplayOnFlg(true)
        binding = ActivityInputPinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ボタンのIDリスト
        val buttonIds = listOf(
            binding.button0,
            binding.button1,
            binding.button2,
            binding.button3,
            binding.button4,
            binding.button5,
            binding.button6,
            binding.button7,
            binding.button8,
            binding.button9
        )

        // ボタンクリックアニメーション
        val buttonClickAnimation = AnimationUtils.loadAnimation(this, R.anim.button_click)

        // ボタンのクリックリスナー設定
        buttonIds.forEach { button ->
            button.setOnTouchListener(getButtonTouchListener(buttonClickAnimation))
            button.setOnClickListener {
                onButtonClick(it)
            }
        }

        // バックスペースボタンのクリックリスナー設定
        binding.buttonBackspace.setOnClickListener {
            if (enteredPin.isNotEmpty()) {
                val lastIndex = enteredPin.size - 1
                pinDigits[lastIndex].setImageResource(R.drawable.circle)
                enteredPin.removeAt(lastIndex)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        MyApplication().setRockDisplayOnFlg(false)
    }

    /**
     * ボタンタッチアニメーション
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun getButtonTouchListener(animation: Animation): View.OnTouchListener {
        return View.OnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    view.startAnimation(animation)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    view.clearAnimation()
                    onButtonClick(view)
                }
            }
            true
        }
    }

    /**
     * ボタンクリック時の処理
     */
    private fun onButtonClick(view: View) {
        // 入力されたPINが4桁未満の場合
        if (enteredPin.size < 4) {
            // クリックされた数値をenteredPinに追加
            enteredPin.add((view as Button).text.toString())
            // 入力された桁数に応じてcircleを変更
            pinDigits[enteredPin.size - 1].setImageResource(R.drawable.circle2)

            // 入力されたPINが4桁になった場合
            if (enteredPin.size == 4) {
                // enteredPinを文字列に変換
                val pinString = enteredPin.joinToString("")

                // 正しいPINと一致する場合
                if (pinString == correctPin) {
                    // ログイン成功時の画面遷移
                    goToLoggedInActivity()
                } else {
                    // エラー表示
                    showError()
                    // PIN入力画面のクリア
                    clearPinDigits(pinDigits)
                    // enteredPinのクリア
                    enteredPin.clear()
                }
            }
        }
    }

    /**
     * ログイン成功時
     */
    private fun goToLoggedInActivity() {
//        val intent = Intent(this, MainActivity::class.java)
//        startActivity(intent)
        MyApplication().setRelieveRock(true)
        finish()
    }

    /**
     * エラーメッセージ表示
     */
    private fun showError() {
        Toast.makeText(this, "PINコードが正しくありません。", Toast.LENGTH_SHORT).show()
    }

    /**
     * PIN入力状態のクリア
     */
    private fun clearPinDigits(pinDigits: List<ImageView>) {
        for (digit in pinDigits) {
            digit.setImageResource(R.drawable.circle)
        }
    }

    /**
     * 端末戻るボタン押下時
     */
    override fun onBackPressed() {
        super.onBackPressed()
        //ホームに戻る
        val intent = Intent("android.intent.action.MAIN")
        intent.addCategory("android.intent.category.HOME")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    companion object {
        private const val KEY_PACKAGE_NAME = "KEY_PACKAGE_NAME"
        fun newIntent(context: Context, packageName: String): Intent {
            val intent = Intent(context, InputPinActivity::class.java)
            intent.putExtra(KEY_PACKAGE_NAME, packageName)
            return intent
        }
    }
}