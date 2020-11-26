package com.sth4nothing.bluetoothhid.listeners

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View

class CompositeListener : View.OnTouchListener {

    private var registeredListeners : MutableList<View.OnTouchListener> = ArrayList<View.OnTouchListener>()


    fun registerListener(listener : View.OnTouchListener): Unit{
        registeredListeners.add(listener)

    }
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {

        for(listener:View.OnTouchListener in registeredListeners)
        {
            listener.onTouch(v,event)
        }
        return true

    }
}