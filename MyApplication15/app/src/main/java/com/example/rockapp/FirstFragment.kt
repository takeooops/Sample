package com.example.rockapp

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.rockapp.databinding.FragmentFirstBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class
FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var list_view = view.findViewById<ListView>(R.id.list_view)
        //アダプターにユーザーリストを導入
        val Adapter = ListAdapter(MyApplication().getInstance(), create(MyApplication().getInstance()) as ArrayList<Data>)
        //リストビューにアダプターを設定
        list_view.adapter = Adapter


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

}