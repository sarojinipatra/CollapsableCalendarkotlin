package com.kotlin.collapsablecalendar

import android.graphics.RectF
import android.os.Bundle
import android.support.v4.view.ViewCompat
import android.support.v7.app.AppCompatActivity
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import com.github.sundeepk.compactcalendarview.CompactCalendarView
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), WeekView.EventClickListener, MonthLoader.MonthChangeListener,
    WeekView.EventLongPressListener, WeekView.EmptyViewLongPressListener {

//    private lateinit var appBarLayout: AppBarLayout

    private val dateFormat = SimpleDateFormat("d MMMM yyyy", /*Locale.getDefault()*/Locale.ENGLISH)

//    private lateinit var compactCalendarView: CompactCalendarView

    private var isExpanded = false

    private val TYPE_DAY_VIEW = 1
    private val TYPE_THREE_DAY_VIEW = 2
    private val TYPE_WEEK_VIEW = 3
    private var mWeekViewType = TYPE_THREE_DAY_VIEW
//    private lateinit var mWeekView: WeekView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        title = "Collapsable calendar"
        // Force English
        compactcalendar_view.setLocale(TimeZone.getDefault(), /*Locale.getDefault()*/Locale.ENGLISH)

        compactcalendar_view.setShouldDrawDaysHeader(true)
        compactcalendar_view.setListener(object : CompactCalendarView.CompactCalendarViewListener {
            override fun onDayClick(dateClicked: Date) {
                setSubtitle(dateFormat.format(dateClicked))
                isExpanded = !isExpanded
                app_bar_layout.setExpanded(isExpanded, true)
                onMonthChange(2018, 12)
            }

            override fun onMonthScroll(firstDayOfNewMonth: Date) {
                setSubtitle(dateFormat.format(firstDayOfNewMonth))
            }
        })
        // Set current date to today
        setCurrentDate(Date())
        date_picker_button.setOnClickListener { v ->
            val rotation = (if (isExpanded) 0 else 180).toFloat()
            ViewCompat.animate(date_picker_arrow).rotation(rotation).start()

            isExpanded = !isExpanded
            app_bar_layout.setExpanded(isExpanded, true)
        }

        weekView.setOnEventClickListener(this)

        weekView.setMonthChangeListener(this)

        weekView.setOnEventLongPressListener(this)

        weekView.setOnEmptyViewLongPressListener(this)

        // Set up a date time interpreter to interpret how the date and time will be formatted in

        setupDateTimeInterpreter(false)

    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {

            R.id.action_day_view -> {
                if (mWeekViewType != TYPE_DAY_VIEW) {
                    item.isChecked = !item.isChecked
                    mWeekViewType = TYPE_DAY_VIEW
                    weekView.setNumberOfVisibleDays(1)

                    weekView.setColumnGap(
                        TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            1f,
                            resources.displayMetrics
                        ).toInt()
                    )
                    weekView.setTextSize(
                        TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_SP,
                            12f,
                            resources.displayMetrics
                        ).toInt()
                    )
                    weekView.setEventTextSize(
                        TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_SP,
                            12f,
                            resources.displayMetrics
                        ).toInt()
                    )
                }
                return true
            }
            R.id.action_three_day_view -> {
                if (mWeekViewType != TYPE_THREE_DAY_VIEW) {
                    item.isChecked = !item.isChecked
                    mWeekViewType = TYPE_THREE_DAY_VIEW
                    weekView.setNumberOfVisibleDays(3)

                    weekView.setColumnGap(
                        TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            1f,
                            resources.displayMetrics
                        ).toInt()
                    )
                    weekView.setTextSize(
                        TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_SP,
                            12f,
                            resources.displayMetrics
                        ).toInt()
                    )
                    weekView.setEventTextSize(
                        TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_SP,
                            12f,
                            resources.displayMetrics
                        ).toInt()
                    )
                }
                return true
            }
            R.id.action_week_view -> {
                if (mWeekViewType != TYPE_WEEK_VIEW) {
                    item.isChecked = !item.isChecked
                    mWeekViewType = TYPE_WEEK_VIEW
                    weekView.setNumberOfVisibleDays(7)

                    weekView.setColumnGap(
                        TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            1f,
                            resources.displayMetrics
                        ).toInt()
                    )
                    weekView.setTextSize(
                        TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_SP,
                            10f,
                            resources.displayMetrics
                        ).toInt()
                    )
                    weekView.setEventTextSize(
                        TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_SP,
                            10f,
                            resources.displayMetrics
                        ).toInt()
                    )
                }
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }


    override fun onEventClick(event: WeekViewEvent, eventRect: RectF?) {

    }

    override fun onMonthChange(newYear: Int, newMonth: Int): List<WeekViewEvent> {

        // Populate the week view with some events.
        val events = ArrayList<WeekViewEvent>()

        var startTime = Calendar.getInstance()
        startTime.set(Calendar.HOUR_OF_DAY, 6)
        startTime.set(Calendar.MINUTE, 0)
        startTime.set(Calendar.MONTH, newMonth - 1)
        startTime.set(Calendar.YEAR, newYear)
        var endTime = startTime.clone() as Calendar
        endTime.add(Calendar.HOUR, 1)
        endTime.set(Calendar.MONTH, newMonth - 1)
        var event = WeekViewEvent(1, getEventTitle(startTime), startTime, endTime)
        event.color=resources.getColor(R.color.event_color_04)
        events.add(event)

        startTime = Calendar.getInstance()
        startTime.set(Calendar.HOUR_OF_DAY, 3)
        startTime.set(Calendar.MINUTE, 30)
        startTime.set(Calendar.MONTH, newMonth - 1)
        startTime.set(Calendar.YEAR, newYear)
        endTime = startTime.clone() as Calendar
        endTime.set(Calendar.HOUR_OF_DAY, 4)
        endTime.set(Calendar.MINUTE, 30)
        endTime.set(Calendar.MONTH, newMonth - 1)
        event = WeekViewEvent(10, getEventTitle(startTime), startTime, endTime)
        event.color=resources.getColor(R.color.event_color_04)
        events.add(event)

        startTime = Calendar.getInstance()
        startTime.set(Calendar.HOUR_OF_DAY, 4)
        startTime.set(Calendar.MINUTE, 20)
        startTime.set(Calendar.MONTH, newMonth - 1)
        startTime.set(Calendar.YEAR, newYear)
        endTime = startTime.clone() as Calendar
        endTime.set(Calendar.HOUR_OF_DAY, 5)
        endTime.set(Calendar.MINUTE, 0)
        event = WeekViewEvent(10, getEventTitle(startTime), startTime, endTime)
        event.color=resources.getColor(R.color.event_color_04)
        events.add(event)

        startTime = Calendar.getInstance()
        startTime.set(Calendar.HOUR_OF_DAY, 5)
        startTime.set(Calendar.MINUTE, 0)
        startTime.set(Calendar.MONTH, newMonth - 1)
        startTime.set(Calendar.YEAR, newYear)
        startTime.add(Calendar.DATE, 1)
        endTime = startTime.clone() as Calendar
        endTime.add(Calendar.HOUR_OF_DAY, 3)
        endTime.set(Calendar.MONTH, newMonth - 1)
        event = WeekViewEvent(3, getEventTitle(startTime), startTime, endTime)
        event.color=resources.getColor(R.color.event_color_04)
        events.add(event)

        startTime = Calendar.getInstance()
        startTime.set(Calendar.DAY_OF_MONTH, 15)
        startTime.set(Calendar.HOUR_OF_DAY, 3)
        startTime.set(Calendar.MINUTE, 0)
        startTime.set(Calendar.MONTH, newMonth - 1)
        startTime.set(Calendar.YEAR, newYear)
        endTime = startTime.clone() as Calendar
        endTime.add(Calendar.HOUR_OF_DAY, 1)
        event = WeekViewEvent(4, getEventTitle(startTime), startTime, endTime)
        event.color=resources.getColor(R.color.event_color_04)
        events.add(event)

        startTime = Calendar.getInstance()
        startTime.set(Calendar.DAY_OF_MONTH, 1)
        startTime.set(Calendar.HOUR_OF_DAY, 3)
        startTime.set(Calendar.MINUTE, 0)
        startTime.set(Calendar.MONTH, newMonth - 1)
        startTime.set(Calendar.YEAR, newYear)
        endTime = startTime.clone() as Calendar
        endTime.add(Calendar.HOUR_OF_DAY, 3)
        event = WeekViewEvent(5, getEventTitle(startTime), startTime, endTime)
        event.color=resources.getColor(R.color.event_color_04)
        events.add(event)

        startTime = Calendar.getInstance()
        startTime.set(Calendar.DAY_OF_MONTH, startTime.getActualMaximum(Calendar.DAY_OF_MONTH))
        startTime.set(Calendar.HOUR_OF_DAY, 15)
        startTime.set(Calendar.MINUTE, 0)
        startTime.set(Calendar.MONTH, newMonth - 1)
        startTime.set(Calendar.YEAR, newYear)
        endTime = startTime.clone() as Calendar
        endTime.add(Calendar.HOUR_OF_DAY, 3)
        event = WeekViewEvent(5, getEventTitle(startTime), startTime, endTime)
        event.color=resources.getColor(R.color.event_color_04)
        events.add(event)

        startTime = Calendar.getInstance()
        startTime.set(Calendar.DAY_OF_MONTH, 8)
        startTime.set(Calendar.HOUR_OF_DAY, 2)
        startTime.set(Calendar.MINUTE, 0)
        startTime.set(Calendar.MONTH, newMonth - 1)
        startTime.set(Calendar.YEAR, newYear)
        endTime = startTime.clone() as Calendar
        endTime.set(Calendar.DAY_OF_MONTH, 10)
        endTime.set(Calendar.HOUR_OF_DAY, 23)
        event = WeekViewEvent(8, getEventTitle(startTime), null, startTime, endTime, true)
        event.color=resources.getColor(R.color.event_color_04)
        events.add(event)

        // All day event until 00:00 next day
        startTime = Calendar.getInstance()
        startTime.set(Calendar.DAY_OF_MONTH, 10)
        startTime.set(Calendar.HOUR_OF_DAY, 0)
        startTime.set(Calendar.MINUTE, 0)
        startTime.set(Calendar.SECOND, 0)
        startTime.set(Calendar.MILLISECOND, 0)
        startTime.set(Calendar.MONTH, newMonth - 1)
        startTime.set(Calendar.YEAR, newYear)
        endTime = startTime.clone() as Calendar
        endTime.set(Calendar.DAY_OF_MONTH, 11)
        event = WeekViewEvent(8, getEventTitle(startTime), null, startTime, endTime, true)
        event.color=resources.getColor(R.color.event_color_04)
        events.add(event)

        return events
    }

    override fun onEventLongPress(event: WeekViewEvent, eventRect: RectF?) {
    }

    override fun onEmptyViewLongPress(startTime: Calendar?, endTime: Calendar?) {
    }

    /**
     * Set up a date time interpreter which will show short date values when in week view and long
     * date values otherwise.
     *
     * @param shortDate True if the date values should be short.
     */
    private fun setupDateTimeInterpreter(shortDate: Boolean) {
        weekView.setDateTimeInterpreter(object : DateTimeInterpreter {
            override fun interpretDate(date: Calendar): String {
                val weekdayNameFormat = SimpleDateFormat("EEE", Locale.getDefault())
                var weekday = weekdayNameFormat.format(date.time)
                val format = SimpleDateFormat(" dd", Locale.getDefault())

                // All android api level do not have a standard way of getting the first letter of
                // the week day name. Hence we get the first char programmatically.
                if (shortDate)
                    weekday = weekday[0].toString()

                val sb = StringBuffer(format.format(date.time).trim { it <= ' ' })
                sb.append(" ").append(weekday.trim { it <= ' ' })
                return sb.toString()
            }

           override fun interpretTime(hour: Int): String {
                return if (hour > 11) (hour - 12).toString() + " PM" else if (hour == 0) "12 AM" else hour.toString() + " AM"
            }
        })
    }


    private fun setCurrentDate(date: Date) {
        setSubtitle(dateFormat.format(date))
        if (compactcalendar_view != null) {
            compactcalendar_view.setCurrentDate(date)
        }
    }

    override fun setTitle(titleText: CharSequence) {


        if (toolbarTitle != null) {
            toolbarTitle.text = titleText
        }
    }


    private fun setSubtitle(subtitle: String) {

        if (date_picker_text_view != null) {
            date_picker_text_view.text = subtitle
        }
    }

    protected fun getEventTitle(time: Calendar): String {
        return String.format(
            "Event of %02d:%02d %s/%d",
            time.get(Calendar.HOUR_OF_DAY),
            time.get(Calendar.MINUTE),
            time.get(Calendar.MONTH) + 1,
            time.get(Calendar.DAY_OF_MONTH)
        )
    }


}
