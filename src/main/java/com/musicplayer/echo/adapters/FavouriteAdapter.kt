package com.musicplayer.echo.adapters

import android.content.Context
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import com.musicplayer.echo.R
import com.musicplayer.echo.Songs
import com.musicplayer.echo.fragments.SongPlayingFragment

class FavouriteAdapter(arrayList:ArrayList<Songs>, _context : Context): RecyclerView.Adapter<FavouriteAdapter.MyViewHolder>()
{

    var songDetails:ArrayList<Songs>?=null
    var mContext: Context?=null

    init
    {
        this.songDetails = arrayList
        this.mContext = _context
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder
    {
        val itemView = LayoutInflater.from(parent?.context)
                .inflate(R.layout.row_custom_mainscreen,parent,false)

        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int
    {
        if(songDetails==null)
            return 0
        else
            return (songDetails as ArrayList<Songs>).size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int)
    {
        val songObject = songDetails?.get(position)
        holder.trackTitle?.text = songObject?.songTitle
        holder.trackArtist?.text = songObject?.songArtist
        holder.contentHolder?.setOnClickListener({

            val songPlayingFragment = SongPlayingFragment()
            var args = Bundle()
            args.putString("songArtist",songObject?.songArtist)
            args.putString("songTitle",songObject?.songTitle)
            args.putString("path",songObject?.songData)
            args.putInt("songId",songObject?.songId?.toInt() as Int)
            args.putInt("songPosition",position)
            args.putParcelableArrayList("songData",songDetails)
            songPlayingFragment.arguments = args
            (mContext as FragmentActivity).supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.details_fragment, songPlayingFragment)
                    .addToBackStack("SongPlayingFragmentFavourite")
                    .commit()
        })
    }
    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
    {
        var trackTitle : TextView?=null
        var trackArtist : TextView?=null
        var contentHolder : RelativeLayout?=null

        init
        {
            trackTitle = view.findViewById(R.id.trackTitle)
            trackArtist = view.findViewById(R.id.trackArtist)
            contentHolder = view.findViewById(R.id.contentRow)
        }

    }

}



