//package com.eden.livewidget.logic.update
//
//import android.app.Service
//import android.content.Intent
//import android.os.IBinder
//import android.util.Log
//
//class UpdateService: Service() {
//    override fun onBind(intent: Intent): IBinder? = null
//
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        super.onStartCommand(intent, flags, startId)
//
//        // Intent can be null from recreated service
//        if (intent != null)
//            try {
//                alertLogic?.processRequest(startId, intent)
//            } catch (e: Exception) {
//                Log.e("$javaClass", e.message, e)
//            }
//
//        // Restart if killed after this return
//        return START_STICKY
//    }
//}