package com.example.rockapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import android.widget.ImageView
import com.example.rockapp.Activity.MainActivity

class ListAdapter(val context: Context, val UserList: ArrayList<Data>, val LockAppList: List<String>? = null) : BaseAdapter()  {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = LayoutInflater.from(context).inflate(R.layout.item_app, null)
        val Icon = view.findViewById<ImageView>(R.id.imageIcon)
        val Name = view.findViewById<TextView>(R.id.name_tv)
        val Email = view.findViewById<TextView>(R.id.email_tv)
        val rockImg = view.findViewById<ImageView>(R.id.lock_icon)

        val user = UserList[position]

        Icon.setImageDrawable(user.icon)
        Name.text = user.appNm
        Email.text = user.pkgNm
        //ロックするアプリとして登録されているか確認
        if(LockAppList?.contains(user.pkgNm) == true){
            rockImg.setImageResource(R.drawable.ic_baseline_lock_24)
        } else {
            rockImg.setImageResource(R.drawable.ic_baseline_lock_open_24)
        }

        return view
    }

    override fun getItem(position: Int): Any {
        return UserList[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return UserList.size
    }
}