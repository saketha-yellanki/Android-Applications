package com.musicplayer.echo.fragments


import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.*
import android.widget.*
import com.cleveroad.audiovisualization.AudioVisualization
import com.cleveroad.audiovisualization.DbmHandler
import com.cleveroad.audiovisualization.GLAudioVisualizationView
import com.musicplayer.echo.CurrentSongHelper
import com.musicplayer.echo.R
import com.musicplayer.echo.Songs
import com.musicplayer.echo.fragments.SongPlayingFragment.Statified.currentPosition
import com.musicplayer.echo.fragments.SongPlayingFragment.Statified.currentSongHelper
import com.musicplayer.echo.fragments.SongPlayingFragment.Statified.endTimeText
import com.musicplayer.echo.fragments.SongPlayingFragment.Statified.fab
import com.musicplayer.echo.fragments.SongPlayingFragment.Statified.favouriteContent
import com.musicplayer.echo.fragments.SongPlayingFragment.Statified.fetchSongs
import com.musicplayer.echo.fragments.SongPlayingFragment.Statified.mediaPlayer
import com.musicplayer.echo.fragments.SongPlayingFragment.Statified.myActivity
import com.musicplayer.echo.fragments.SongPlayingFragment.Statified.seekBar
import com.musicplayer.echo.fragments.SongPlayingFragment.Statified.songArtistView
import com.musicplayer.echo.fragments.SongPlayingFragment.Statified.songTitleView
import com.musicplayer.echo.fragments.SongPlayingFragment.Statified.startTimeText
import com.musicplayer.echo.fragments.SongPlayingFragment.Statified.updateSongTime
import databases.EchoDatabase
import kotlinx.android.synthetic.main.fragment_song_playing.*
import java.lang.Exception
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.experimental.coroutineContext


/**
 * A simple [Fragment] subclass.
 *
 */
class SongPlayingFragment : Fragment() {

    object Statified{

        var myActivity : Activity?=null
        var mediaPlayer = MediaPlayer()
        var audioManager : AudioManager?=null

        var songTitleView : TextView?=null
        var songArtistView : TextView?=null
        var seekBar : SeekBar?=null
        var startTimeText : TextView?=null
        var endTimeText : TextView?=null
        var playPauseButton : ImageButton?=null
        var playPreviousButton : ImageButton?=null
        var playNextButton : ImageButton?=null
        var loopButton : ImageButton?=null
        var shuffleButton : ImageButton?=null
        var currentPosition : Int = 0
        var fetchSongs : ArrayList<Songs>?=null

        var currentSongHelper : CurrentSongHelper?=null

        var audioVisualization : AudioVisualization?=null
        var glView : GLAudioVisualizationView?=null

        var fab : ImageButton?=null

        var favouriteContent :EchoDatabase?=null
        var mSensorManager : SensorManager?=null
        var mSensorListener : SensorEventListener?=null
        var MY_PREFS_NAME : String = "ShakeFeature"

        var updateSongTime = object : Runnable{

            override fun run() {

                val getCurrent = mediaPlayer?.currentPosition
                startTimeText?.setText(String.format("%d:%d",

                        TimeUnit.MILLISECONDS.toMinutes(getCurrent?.toLong()),
                        (TimeUnit.MILLISECONDS.toSeconds(getCurrent?.toLong() as Long) - TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(getCurrent?.toLong())))%60))
                Handler().postDelayed(this,1000)

            }
        }
    }

    object Staticated{
        var MY_PREFS_SHUFFLE = "shuffle feature"
        var MY_PREFS_LOOP = "loop feature"

        fun songOnComplete()
        {
            if(currentSongHelper?.isShuffle as Boolean)
            {
                playNext("PlayNextLikeNormalShuffle")
                currentSongHelper?.isPlaying = true
            }
            else
            {
                if (currentSongHelper?.isLoop as Boolean)
                {
                    currentSongHelper?.isPlaying = true
                    var nextSong = fetchSongs?.get(currentPosition)

                    currentSongHelper?.songTitle = nextSong?.songTitle
                    currentSongHelper?.songPath = nextSong?.songData
                    currentSongHelper?.songId = nextSong?.songId as Long
                    currentSongHelper?.currentPosition = currentPosition

                    updateTextViews(currentSongHelper?.songTitle as String,currentSongHelper?.songArtist as String)

                    mediaPlayer?.reset()
                    try {
                        mediaPlayer?.setDataSource(myActivity, Uri.parse(nextSong.songData))
                        mediaPlayer?.prepare()
                        mediaPlayer?.start()
                        processInformation(mediaPlayer as MediaPlayer)
                    }
                    catch (e:Exception)
                    {e.printStackTrace()}
                }
                else
                {
                    playNext("PlayNextNormal")
                    currentSongHelper?.isPlaying = true
                }
                if(favouriteContent?.checkifIdExists(currentSongHelper?.songId?.toInt() as Int) as Boolean)
                {
                    fab?.setImageDrawable(ContextCompat.getDrawable(myActivity as Context,R.drawable.favorite_on))
                }
                else
                {
                    fab?.setImageDrawable(ContextCompat.getDrawable(myActivity as Context,R.drawable.favorite_off))
                }

            }
        }

        fun updateTextViews(songTitle : String,songArtist : String)
        {
            var songTitleUpdated = songTitle
            var songArtistUpdated = songArtist
            if(songTitle.equals("<unknown>",true))
            {
                songTitleUpdated = "unknown"
            }
            if(songArtist.equals("<unknown>",true))
            {
                songArtistUpdated = "unknown"
            }
            songTitleView?.setText(songTitleUpdated)
            songArtistView?.setText(songArtistUpdated)
        }

        fun processInformation(mediaPlayer: MediaPlayer)
        {
            val finalTime = mediaPlayer.duration
            val startTime = mediaPlayer.currentPosition
            Statified.seekBar?.max = finalTime
            startTimeText?.setText(String.format("%d:%d",

                    TimeUnit.MILLISECONDS.toMinutes(startTime.toLong()),
                    TimeUnit.MILLISECONDS.toSeconds(startTime.toLong()) - TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(startTime.toLong() as Long))))

            endTimeText?.setText(String.format("%d:%d",

                    TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong()),
                    (TimeUnit.MILLISECONDS.toSeconds(finalTime.toLong()) - TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong() as Long)))%60))

            Statified.seekBar?.setProgress(startTime)
            Handler().postDelayed(updateSongTime,1000)

            Statified.seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int,fromUser: Boolean) {

                    if(mediaPlayer!=null && fromUser)
                    {
                        Statified.mediaPlayer.seekTo(progress)
                    }
                }
                override fun onStopTrackingTouch(seekBar: SeekBar?) {

                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {

                }

            })

        }

        fun playNext(check : String)
        {
            if(check.equals("PlayNextNormal",true))
                Statified.currentPosition = Statified.currentPosition + 1
            else if(check.equals("PlayNextLikeNormalShuffle",true))
            {
                var randomObject = Random()
                var randomPosition = randomObject.nextInt(Statified.fetchSongs?.size?.plus(1) as Int)
                Statified.currentPosition = randomPosition
            }
            if(Statified.currentPosition==Statified.fetchSongs?.size)
                Statified.currentPosition=0

            Statified.currentSongHelper?.isLoop = false

            var nextSongs = Statified.fetchSongs?.get(Statified.currentPosition)
            Statified.currentSongHelper?.songTitle = nextSongs?.songTitle
            Statified.currentSongHelper?.songPath = nextSongs?.songData
            Statified.currentSongHelper?.songId = nextSongs?.songId as Long
            Statified.currentSongHelper?.songArtist = nextSongs?.songArtist
            Statified.currentSongHelper?.currentPosition = Statified.currentPosition

            updateTextViews(Statified.currentSongHelper?.songTitle as String,Statified.currentSongHelper?.songArtist as String)

            Statified.mediaPlayer?.reset()

            try
            {
                Statified.mediaPlayer?.setDataSource(myActivity,Uri.parse(currentSongHelper?.songPath))
                Statified.mediaPlayer?.prepare()
                Statified.mediaPlayer?.start()
                processInformation(mediaPlayer as MediaPlayer)
            }
            catch(e:Exception)
            {e.printStackTrace()}

            if(favouriteContent?.checkifIdExists(currentSongHelper?.songId?.toInt() as Int) as Boolean)
            {
                fab?.setImageDrawable(ContextCompat.getDrawable(myActivity as Context,R.drawable.favorite_on))
            }
            else
            {
                fab?.setImageDrawable(ContextCompat.getDrawable(myActivity as Context,R.drawable.favorite_off))
            }
        }
    }


    var mAccelaration: Float = 0f
    var mAccelarationCurrent: Float = 0f
    var mAccelarationLast: Float = 0f

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.fragment_song_playing, container, false)
        setHasOptionsMenu(true)
        activity?.title = "Now Playing"
        Statified.songTitleView = view?.findViewById(R.id.songTitle)
        Statified.songArtistView = view?.findViewById(R.id.songArtist)
        Statified.seekBar = view?.findViewById(R.id.seekBar)
        Statified.startTimeText = view?.findViewById(R.id.startTime)
        Statified.endTimeText = view?.findViewById(R.id.endTime)
        Statified.playPauseButton = view?.findViewById(R.id.playPauseButton)
        Statified.playPreviousButton = view?.findViewById(R.id.previousButton)
        Statified.playNextButton = view?.findViewById(R.id.nextButton)
        Statified.loopButton = view?.findViewById(R.id.loopButton)
        Statified.shuffleButton = view?.findViewById(R.id.shuffleButton)
        Statified.glView = view?.findViewById(R.id.visualizer_view)
        Statified.fab = view?.findViewById(R.id.favouriteIcon)
        Statified.fab?.alpha = 0.8f


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Statified.audioVisualization = Statified.glView as AudioVisualization
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        Statified.myActivity = context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        Statified.myActivity = activity
    }

    override fun onResume() {
        super.onResume()
        Statified.audioVisualization?.onResume()
        Statified.mSensorManager?.registerListener(Statified.mSensorListener, Statified.mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        Statified.audioVisualization?.onPause()
        Statified.mSensorManager?.unregisterListener(Statified.mSensorListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Statified.audioVisualization?.release()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Statified.mSensorManager = Statified.myActivity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAccelaration = 0.0f
        mAccelarationCurrent = SensorManager.GRAVITY_EARTH
        mAccelarationLast = SensorManager.GRAVITY_EARTH
        bindShakeListener()

    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.clear()
        inflater?.inflate(R.menu.song_playing_menu,menu)

    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        val item : MenuItem? = menu?.findItem(R.id.action_redirect)
        item?.isVisible = true
        val item2 : MenuItem? = menu?.findItem(R.id.action_sort)
        item2?.isVisible = true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId)
        {
            R.id.action_redirect -> {
                Statified.myActivity?.onBackPressed()
                return false
            }
        }
        return false
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        Statified.favouriteContent = EchoDatabase(Statified.myActivity)

        Statified.currentSongHelper = CurrentSongHelper()
        Statified.currentSongHelper?.isPlaying = true
        Statified.currentSongHelper?.isLoop = false
        Statified.currentSongHelper?.isShuffle = false

        var path:String?=null
        var songTitle:String?=null
        var songArtist:String?=null
        var songId:Long = 0

        try
        {
           path = arguments?.getString("path")
           songTitle = arguments?.getString("songTitle")
           songArtist = arguments?.getString("songArtist")
           songId = arguments?.getInt("songId")?.toLong() as Long
            Statified.currentPosition = arguments?.getInt("songPosition") as Int
            Statified.fetchSongs = arguments?.getParcelableArrayList("songData")

            Statified.currentSongHelper?.songPath = path
            Statified.currentSongHelper?.songTitle = songTitle
            Statified.currentSongHelper?.songArtist = songArtist
            Statified.currentSongHelper?.songId = songId
            Statified.currentSongHelper?.currentPosition = Statified.currentPosition

            Staticated.updateTextViews(Statified.currentSongHelper?.songTitle as String,currentSongHelper?.songArtist as String)
        }
        catch (e:Exception)
        {e.printStackTrace()}

        var  fromFavBottomBar = arguments?.get("FavBottomBar") as? String
        if(fromFavBottomBar!=null)
        {
            Statified.mediaPlayer = FavouriteFragment.Statified.mediaPlayer as MediaPlayer
        }
        else
        {
            Statified.mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
            if (Statified.mediaPlayer != null && Statified.mediaPlayer?.isPlaying() as Boolean) {
                Statified.mediaPlayer.pause()
                Statified.mediaPlayer.reset()
            }
            try {
                Statified.mediaPlayer?.setDataSource(Statified.myActivity, Uri.parse(path))
                Statified.mediaPlayer?.prepare()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Statified.mediaPlayer?.start()
        }

        var  fromMainScreenBottomBar = arguments?.get("mainScreenBottomBar") as? String
        if(fromMainScreenBottomBar!=null)
        {
            Statified.mediaPlayer = MainScreenFragment.Statified.mediaPlayer as MediaPlayer
        }
        else
        {
            Statified.mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
            if (Statified.mediaPlayer != null && Statified.mediaPlayer?.isPlaying() as Boolean) {
                Statified.mediaPlayer.pause()
                Statified.mediaPlayer.reset()
            }
            try {
                Statified.mediaPlayer?.setDataSource(Statified.myActivity, Uri.parse(path))
                Statified.mediaPlayer?.prepare()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Statified.mediaPlayer?.start()
        }
        Staticated.processInformation(mediaPlayer as MediaPlayer)

        if(Statified.currentSongHelper?.isPlaying as Boolean)
        {
            playPauseButton?.setBackgroundResource(R.drawable.pause_icon)
        }
        else
        {
            playPauseButton?.setBackgroundResource(R.drawable.play_icon)
        }
        Statified.mediaPlayer?.setOnCompletionListener {
            Staticated.songOnComplete()
        }

      songPLayingClickHandler()

        var visualizationHandler = DbmHandler.Factory.newVisualizerHandler(myActivity as Context,0)
        Statified.audioVisualization?.linkTo(visualizationHandler)

        var prefsForShuffle = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE,Context.MODE_PRIVATE)
        var isShuffleAllowed = prefsForShuffle?.getBoolean("feature",false)
        if(isShuffleAllowed as Boolean)
        {
            Statified.currentSongHelper?.isShuffle = true
            Statified.currentSongHelper?.isLoop = false
            shuffleButton?.setBackgroundResource(R.drawable.shuffle_icon)
            loopButton?.setBackgroundResource(R.drawable.loop_white_icon)
        }
        else
        {
            Statified.currentSongHelper?.isShuffle = false
            shuffleButton?.setBackgroundResource(R.drawable.shuffle_white_icon )
        }

        var prefsForLoop = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_LOOP,Context.MODE_PRIVATE)
        var isLoopAllowed = prefsForLoop?.getBoolean("feature",false)
        if(isLoopAllowed as Boolean)
        {
            Statified.currentSongHelper?.isLoop = true
            Statified.currentSongHelper?.isShuffle = false
            shuffleButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
            loopButton?.setBackgroundResource(R.drawable.loop_icon)
        }
        else
        {
            Statified.currentSongHelper?.isLoop = false
            loopButton?.setBackgroundResource(R.drawable.loop_white_icon )
        }

        if(Statified.favouriteContent?.checkifIdExists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean)
        {
            Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity as Context,R.drawable.favorite_on))
        }
        else
        {
            Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity as Context,R.drawable.favorite_off))
        }


    }

    fun songPLayingClickHandler()
    {
        Statified.fab?.setOnClickListener({
            if(Statified.favouriteContent?.checkifIdExists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean)
            {
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity as Context,R.drawable.favorite_off))
                Statified.favouriteContent?.deleteFavourite(Statified.currentSongHelper?.songId?.toInt() as Int)
                Toast.makeText(Statified.myActivity,"Removed from favourites",Toast.LENGTH_SHORT).show()
            }
            else
            {
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(myActivity as Context,R.drawable.favorite_on))
                Statified.favouriteContent?.storeAsFavourite(Statified.currentSongHelper?.songId?.toInt(),currentSongHelper?.songArtist, currentSongHelper?.songTitle,currentSongHelper?.songPath)
                Toast.makeText(Statified.myActivity,"song added to favourites",Toast.LENGTH_SHORT).show()
            }
        })

        playPauseButton?.setOnClickListener({

            if(Statified.mediaPlayer?.isPlaying() as Boolean)
            {
                Statified.mediaPlayer?.pause()
                Statified.currentSongHelper?.isPlaying = false
                playPauseButton?.setBackgroundResource(R.drawable.play_icon)
            }
            else
            {
                Statified.mediaPlayer?.start()
                Statified.currentSongHelper?.isPlaying = true
                playPauseButton?.setBackgroundResource(R.drawable.pause_icon)

            }
        })
        Statified.playPreviousButton?.setOnClickListener({

            Statified.currentSongHelper?.isPlaying=true
            if(Statified.currentSongHelper?.isLoop as Boolean)
                previousButton.setBackgroundResource(R.drawable.loop_white_icon)

            playPrevious()
        })

        Statified.playNextButton?.setOnClickListener({
            Statified.currentSongHelper?.isPlaying = true
            Statified.playPauseButton?.setBackgroundResource(R.drawable.pause_icon)
            if(Statified.currentSongHelper?.isShuffle as Boolean)
                Staticated.playNext("PlayNextLikeNormalShuffle")
            else
                Staticated.playNext("PlayNextNormal")
        })

        shuffleButton?.setOnClickListener({

            var editorShuffle = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE,Context.MODE_PRIVATE)?.edit()
            var editorLoop = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_LOOP,Context.MODE_PRIVATE)?.edit()
            if(Statified.currentSongHelper?.isShuffle as Boolean)
            {
                Statified.currentSongHelper?.isShuffle = false
                shuffleButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                editorShuffle?.putBoolean("feature",false)
                editorShuffle?.apply()
            }
            else
            {
                Statified.currentSongHelper?.isShuffle = true
                Statified.currentSongHelper?.isLoop = false
                shuffleButton?.setBackgroundResource(R.drawable.shuffle_icon)
                loopButton?.setBackgroundResource(R.drawable.loop_white_icon)
                editorShuffle?.putBoolean("feature",true)
                editorShuffle?.apply()
                editorLoop?.putBoolean("feature",false)
                editorLoop?.apply()
            }
        })
        loopButton?.setOnClickListener({

            var editorShuffle = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE,Context.MODE_PRIVATE)?.edit()
            var editorLoop = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_LOOP,Context.MODE_PRIVATE)?.edit()
             if(Statified.currentSongHelper?.isLoop as Boolean)
             {
                 Statified.currentSongHelper?.isLoop=false
                 loopButton?.setBackgroundResource(R.drawable.loop_white_icon)
                 editorLoop?.putBoolean("feature",false)
                 editorLoop?.apply()
             }
            else
             {
                 Statified.currentSongHelper?.isLoop = true
                 Statified.currentSongHelper?.isShuffle = false
                 loopButton?.setBackgroundResource(R.drawable.loop_icon)
                 shuffleButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                 editorLoop?.putBoolean("feature",true)
                 editorLoop?.apply()
                 editorShuffle?.putBoolean("feature",false)
                 editorShuffle?.apply()
             }
        })
    }

    fun playPrevious()
    {
        currentPosition = currentPosition - 1
        if(currentPosition==-1)
            currentPosition=0
        if(currentSongHelper?.isPlaying as Boolean)
            playPauseButton?.setBackgroundResource(R.drawable.pause_icon)
        else
            playPauseButton?.setBackgroundResource(R.drawable.play_icon)

        currentSongHelper?.isLoop=false
        val nextSongs = fetchSongs?.get(currentPosition)
        currentSongHelper?.songTitle = nextSongs?.songTitle
        currentSongHelper?.songPath = nextSongs?.songData
        currentSongHelper?.currentPosition = currentPosition
        currentSongHelper?.songId = nextSongs?.songId as Long

        Staticated.updateTextViews(currentSongHelper?.songTitle as String,currentSongHelper?.songArtist as String)

        mediaPlayer?.reset()
        try {
            mediaPlayer?.setDataSource(myActivity, Uri.parse(currentSongHelper?.songPath))
            mediaPlayer?.prepare()
            mediaPlayer?.start()
            Staticated.processInformation(mediaPlayer as MediaPlayer)
        }
        catch (e:Exception)
        {e.printStackTrace()}

        if(favouriteContent?.checkifIdExists(currentSongHelper?.songId?.toInt() as Int) as Boolean)
        {
            fab?.setImageDrawable(ContextCompat.getDrawable(myActivity as Context,R.drawable.favorite_on))
        }
        else
        {
            fab?.setImageDrawable(ContextCompat.getDrawable(myActivity as Context,R.drawable.favorite_off))
        }
    }

    fun bindShakeListener()
    {
        Statified.mSensorListener = object: SensorEventListener{
            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

            }

            override fun onSensorChanged(p0: SensorEvent)
            {
                val x = p0.values[0]
                val y = p0.values[1]
                val z = p0.values[2]

                mAccelarationLast = mAccelarationCurrent
                mAccelarationCurrent = Math.sqrt(((x*x + y*y + z*z).toDouble())).toFloat()
                val delta = mAccelarationCurrent - mAccelarationLast
                mAccelaration = mAccelaration*0.9f + delta

                if(mAccelaration>12)
                {
                    val prefs = Statified.myActivity?.getSharedPreferences(Statified.MY_PREFS_NAME,Context.MODE_PRIVATE)
                    val isAllowed = prefs?.getBoolean("feature",false)
                    if(isAllowed as Boolean)
                    {
                        Staticated.playNext("PlayNextNormal")
                    }

                }
            }

        }
    }
}