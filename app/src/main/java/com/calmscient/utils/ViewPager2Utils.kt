/*
 *
 *      Copyright (c) 2023- NFC Solutions, - All Rights Reserved
 *      All source code contained herein remains the property of NFC Solutions Incorporated
 *      and protected by trade secret or copyright law of USA.
 *      Dissemination, De-compilation, Modification and Distribution are strictly prohibited unless
 *      there is a prior written permission or license agreement from NFC Solutions.
 *
 *      Author : @Pardha Saradhi
 */

package com.calmscient.utils

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

class ViewPager2Utils {
    companion object {
        fun disableSwipe(viewPager2: ViewPager2) {
            viewPager2.getChildAt(0)?.let { child ->
                if (child is RecyclerView) {
                    child.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
                        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                            if (e.action == MotionEvent.ACTION_MOVE) {
                                // Check for horizontal swipe
                                val dx = Math.abs(e.x - e.downTime)
                                val dy = Math.abs(e.y - e.downTime)
                                if (dx > dy) {
                                    return true // Disable swipe
                                }
                            }
                            return false // Allow other events (clicks, vertical scrolls)
                        }

                        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
                            // No touch events
                        }

                        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
                        }
                    })
                }
            }
        }
    }
}
