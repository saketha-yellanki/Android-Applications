package com.musicplayer.echo

import android.os.Parcel
import android.os.Parcelable

class Songs(var songId:Long, var songTitle:String, var songArtist:String, var songData:String, var songDateAdded:Long):Parcelable
{
    override fun writeToParcel(p0: Parcel?, p1: Int) {

    }

    override fun describeContents(): Int {
        return 0
          }
    object Statified{
        var nameComparator : Comparator<Songs> = Comparator<Songs>{song1, song2 ->
            val songOne = song1.songTitle.toUpperCase()
            val songTwo = song2.songTitle.toUpperCase()
            songOne.compareTo(songTwo)
        }

        var dateComparator : Comparator<Songs> = Comparator<Songs>{song1, song2 ->
            val songOne = song1.songDateAdded.toDouble()
            val songTwo = song2.songDateAdded.toDouble()
            songTwo.compareTo(songOne)

        }


    }

}
