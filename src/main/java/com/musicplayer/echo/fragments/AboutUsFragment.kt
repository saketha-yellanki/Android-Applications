package com.musicplayer.echo.fragments


import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment

import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.musicplayer.echo.R


/**
 * A simple [Fragment] subclass.
 *
 */
class AboutUsFragment : Fragment() {

    var aboutUs : RelativeLayout?=null
    var joeyPhoto : ImageView?=null
    var developerDetails : TextView?=null
    var appVersion : TextView?=null
    var myActivity : Activity?=null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.fragment_about_us, container, false)

        activity?.title = "About Us"
        aboutUs = view?.findViewById(R.id.appDeveloperInformation)
        joeyPhoto = view?.findViewById(R.id.joeyPhoto)
        developerDetails = view?.findViewById(R.id.developerDetails)
        appVersion = view?.findViewById(R.id.appVersion)

        return view
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        myActivity = context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        myActivity=activity
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        var item = menu?.findItem(R.id.action_sort)
        item?.isVisible = false

    }

}