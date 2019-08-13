package com.example.customtasks

import android.os.Build
import android.util.Log
import android.widget.Chronometer
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import org.joda.time.Duration
import org.joda.time.Instant
import java.io.Serializable
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.TimeUnit

@Entity(tableName = "items")
class Item (): Serializable{

    @PrimaryKey(autoGenerate = true)
    var id : Int = 0

    var name : String = ""

    var data : String = ""

    var startTime : String = ""
    var duration : Long = 0

    constructor( name : String, data : String) : this() {
        this.name = name
        this.data = data
    }

    @Ignore
    var onTaskStart = fun(startTime:String, duration:Long){}
    @Ignore
    var onTaskStop = fun(stopTime:String, duration:Long){}

    fun trigger(){
        if (startTime.isNotEmpty()) {
            val lastMoment : Instant = Instant.parse(startTime)
            val thisMoment : Instant = Instant.now()
            val difference = Duration.millis(thisMoment.millis - lastMoment.millis)
            duration += difference.toStandardSeconds().seconds
            startTime = ""
            onTaskStop(thisMoment.toString(), duration)
        }
        else{
            val instant = Instant.now()
            startTime = instant.toString()
            onTaskStart(startTime, duration)
        }
    }

}