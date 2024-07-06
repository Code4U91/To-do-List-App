package com.example.to_dolist.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("task_data_table")
data class Task (

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "task_id")
    var id : Long,

    @ColumnInfo(name = "task_title")
    var title : String,

    @ColumnInfo(name = "task_content")
    var taskContent : String,

    @ColumnInfo(name = "task_date_time_created")
    var taskDateAndTimeCreated : String,

    @ColumnInfo(name = "task_date_modified")
    var modifiedDate : String,

    @ColumnInfo(name = "task_alarm_time")
    var alarmTimeInMills :  Long
    ,
    @ColumnInfo(name = "sort_timestamp")
    var sortTimestamp: Long // New column for sorting

)