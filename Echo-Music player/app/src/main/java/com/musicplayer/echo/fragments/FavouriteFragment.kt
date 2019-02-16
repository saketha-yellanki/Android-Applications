package com.musicplayer.echo.fragments


import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import com.musicplayer.echo.R
import com.musicplayer.echo.Songs
import com.musicplayer.echo.adapters.FavouriteAdapter
import databases.EchoDatabase
import java.lang.Exception


/**
 * A simple [Fragment] subclass.
 *
 */
class FavouriteFragment : Fragment() {
    var myActivity: Activity?=null
    var getSongsList:ArrayList<Songs>?=null

    var favouriteContent : EchoDatabase?=null
    var noFavourites : TextView?=null
    var nowPlayingBottomBar : RelativeLayout?=null
    var playPauseButton : ImageButton?=null
    var songTitle : TextView?=null
    var recyclerView : RecyclerView?=null
    var trackPosition : Int = 0

    var refreshList : ArrayList<Songs>?=null
    var getListFromDatabase : ArrayList<Songs>?=null

    object Statified{
       var mediaPlayer : MediaPlayer?=null
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_favourite, container, false)
        activity?.title = "Favourites"
        favouriteContent = EchoDatabase(myActivity)
        noFavourites = view?.findViewById(R.id.noFavourites)
        nowPlayingBottomBar = view?.findViewById(R.id.hiddenBarFavScreen)
        songTitle = view?.findViewById(R.id.songTitleFavScreen)
        playPauseButton =view?.findViewById(R.id.playPauseButton)
        recyclerView = view?.findViewById(R.id.favouriteRecycler)

        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        myActivity = context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        myActivity = activity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        favouriteContent = EchoDatabase(myActivity)
           getSongsList = getSongsFromPhone()
        if(getSongsList == null)
        {
            recyclerView?.visibility = View.INVISIBLE
            noFavourites?.visibility = View.VISIBLE
        }
        else
        {
            var favouriteAdapter = FavouriteAdapter(getSongsList as ArrayList<Songs>, myActivity as Context)
            val mLayoutManager = LinearLayoutManager(activity)
            recyclerView?.layoutManager = mLayoutManager
            recyclerView?.itemAnimator = DefaultItemAnimator()
            recyclerView?.adapter = favouriteAdapter
            recyclerView?.setHasFixedSize(true)
        }
        display_favourites_by_searching()
        bottomBarSetup()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        var item = menu?.findItem(R.id.action_sort)
        item?.isVisible = false

    }

    fun getSongsFromPhone():ArrayList<Songs>
    {
        var arrayList = ArrayList<Songs>()
        var contentResolver = myActivity?.contentResolver
        var songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        var songCursor = contentResolver?.query(songUri,null,null,null,null)

        if(songCursor!=null && songCursor.moveToNext())
        {
            var songId = songCursor.getColumnIndex(MediaStore.Audio.Media._ID)
            var songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            var songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            var songData = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            var songDate = songCursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)

            while(songCursor.moveToNext())
            {
                var currentId = songCursor.getLong(songId)
                var currentTitle = songCursor.getString(songTitle)
                var currentArtist = songCursor.getString(songArtist)
                var currentData = songCursor.getString(songData)
                var currentDate  =songCursor.getLong(songDate)
                arrayList.add(Songs(currentId,currentTitle,currentArtist,currentData,currentDate))
            }
        }
        return arrayList
    }

    fun bottomBarSetup()
    {
        try
        {
            bottomBarClickHandler()
            songTitle?.setText(SongPlayingFragment.Statified.currentSongHelper?.songTitle)
            SongPlayingFragment.Statified.mediaPlayer?.setOnCompletionListener ({
                songTitle?.setText(SongPlayingFragment.Statified.currentSongHelper?.songTitle)
                SongPlayingFragment.Staticated.songOnComplete()
            })
            if(SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean)
            {
                nowPlayingBottomBar?.visibility = View.VISIBLE
            }
            else
                nowPlayingBottomBar?.visibility = View.INVISIBLE
        }catch (e:Exception)
        {e.printStackTrace()}
    }

    fun bottomBarClickHandler()
    {
        nowPlayingBottomBar?.setOnClickListener({
            Statified.mediaPlayer = SongPlayingFragment.Statified.mediaPlayer
            val songPlayingFragment = SongPlayingFragment()
            var args = Bundle()
            args.putString("songArtist",SongPlayingFragment.Statified.currentSongHelper?.songArtist)
            args.putString("songTitle",SongPlayingFragment.Statified.currentSongHelper?.songTitle)
            args.putString("path",SongPlayingFragment.Statified.currentSongHelper?.songPath)
            args.putInt("songId",SongPlayingFragment.Statified.currentSongHelper?.songId?.toInt() as Int)
            args.putInt("songPosition",SongPlayingFragment.Statified.currentSongHelper?.currentPosition?.toInt() as Int)
            args.putParcelableArrayList("songData",SongPlayingFragment.Statified.fetchSongs)
            args.putString("FavBottomBar","success")
            songPlayingFragment.arguments = args

            fragmentManager?.beginTransaction()
                    ?.replace(R.id.details_fragment,songPlayingFragment)
                    ?.addToBackStack("SongPlayingFragment")
                    ?.commit()
        })

        playPauseButton?.setOnClickListener({
            if(SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean)
            {
                SongPlayingFragment.Statified.mediaPlayer?.pause()
                trackPosition = SongPlayingFragment.Statified.mediaPlayer?.getCurrentPosition() as Int
                playPauseButton?.setBackgroundResource(R.drawable.play_icon)
            }
            else
            {
                SongPlayingFragment.Statified.mediaPlayer?.seekTo(trackPosition)
                SongPlayingFragment.Statified.mediaPlayer?.start()
                playPauseButton?.setBackgroundResource((R.drawable.pause_icon))
            }
        })
    }

    fun display_favourites_by_searching()
    {
         if(favouriteContent?.checkSize() as Int > 0)
         {
             refreshList = ArrayList<Songs>()
             getListFromDatabase = favouriteContent?.queryDbList()
             var fetchListFromDevice = getSongsFromPhone()
             if(fetchListFromDevice!=null)
             {
                 for(i in 0..fetchListFromDevice?.size-1)
                 {
                     for(j in 0..getListFromDatabase?.size as Int -1)
                     {
                         if((getListFromDatabase?.get(j)?.songId) == (fetchListFromDevice?.get(i)?.songId))
                         {
                             refreshList?.add((getListFromDatabase as ArrayList<Songs>)[j])
                         }
                     }
                 }
             }

             if(refreshList == null)
             {
                 recyclerView?.visibility = View.INVISIBLE
                 noFavourites?.visibility = View.VISIBLE
             }
             else
             {
                 val favouriteAdapter = FavouriteAdapter(refreshList as ArrayList<Songs>, myActivity as Context)
                 val mLayoutManager = LinearLayoutManager(activity)
                 recyclerView?.layoutManager = mLayoutManager
                 recyclerView?.itemAnimator = DefaultItemAnimator()
                 recyclerView?.adapter = favouriteAdapter
                 recyclerView?.setHasFixedSize(true)
             }
         }
        else
         {
             recyclerView?.visibility = View.INVISIBLE
             noFavourites?.visibility = View.VISIBLE
         }
    }


}
