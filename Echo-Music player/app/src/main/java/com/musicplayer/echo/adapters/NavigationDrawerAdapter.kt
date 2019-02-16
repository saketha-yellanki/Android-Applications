package com.musicplayer.echo.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.musicplayer.echo.R
import com.musicplayer.echo.activities.MainActivity
import com.musicplayer.echo.fragments.AboutUsFragment
import com.musicplayer.echo.fragments.FavouriteFragment
import com.musicplayer.echo.fragments.MainScreenFragment
import com.musicplayer.echo.fragments.SettingsFragment

class NavigationDrawerAdapter(contentList:ArrayList<String>,getImages:IntArray,_context :Context)
    : RecyclerView.Adapter<NavigationDrawerAdapter.NavViewHolder>() {

    var contentList : ArrayList<String>?=null
    var getImages : IntArray?=null
    var _context:Context?=null
    init {
        this.contentList=contentList
        this.getImages=getImages
        this._context=_context
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NavViewHolder
    {
        val itemView = LayoutInflater.from(parent?.context)
                .inflate(R.layout.row_custom_navigationdrawyer,parent,false)
        val returnThis = NavViewHolder(itemView)
        return returnThis
    }

    override fun getItemCount(): Int {

        return (contentList  as ArrayList).size
    }

    override fun onBindViewHolder(holder: NavViewHolder, position: Int) {
        holder?.icon_get?.setBackgroundResource(getImages?.get(position)as Int)
        holder?.text_get?.setText(contentList?.get(position))
        holder?.contentHolder?.setOnClickListener({
            if(position==0) {
                val mainScreenFragment = MainScreenFragment()
                (_context as MainActivity).supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.details_fragment, mainScreenFragment)
                        .commit()
            }
            else if (position == 1){
                    val favouriteFragment = FavouriteFragment()
                    (_context as MainActivity).supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.details_fragment,favouriteFragment)
                            .commit()
                }
            else if (position == 2){
                    val settingsFragment = SettingsFragment()
                    (_context as MainActivity).supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.details_fragment,settingsFragment)
                            .commit()
                }
            else{
                    val aboutUsFragment = AboutUsFragment()
                    (_context as MainActivity).supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.details_fragment,aboutUsFragment)
                            .commit()
                }
            MainActivity.Statified.drawerLayout?.closeDrawers()

        })
    }

    class NavViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        var icon_get : ImageView? = null
        var text_get : TextView? = null
        var contentHolder : RelativeLayout? = null

        init {

            icon_get = itemView?.findViewById(R.id.icon_navdrawer)
            text_get = itemView?.findViewById(R.id.text_navdrawer)
            contentHolder = itemView?.findViewById(R.id.nav_drawer_item_content_holder)
        }

    }
}