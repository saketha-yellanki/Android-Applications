package com.musicplayer.echo

class CurrentSongHelper
{
    var songTitle : String?=null
    var songArtist : String?=null
    var songPath : String?=null
    var songId : Long = 0
    var currentPosition : Int = 0

    var isPlaying : Boolean = false
    var isLoop : Boolean = false
    var isShuffle : Boolean = false
    var trackposition : Int = 0
}