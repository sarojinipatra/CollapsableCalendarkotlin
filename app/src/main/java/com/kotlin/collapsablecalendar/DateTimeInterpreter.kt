package com.kotlin.collapsablecalendar

import java.util.Calendar


interface DateTimeInterpreter {
    fun interpretDate(date: Calendar): String
    fun interpretTime(hour: Int): String
}
