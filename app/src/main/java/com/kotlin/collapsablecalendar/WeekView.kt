package com.kotlin.collapsablecalendar

import android.content.Context
import android.graphics.*
import android.os.Build
import android.os.Handler
import android.support.v4.view.GestureDetectorCompat
import android.support.v4.view.ViewCompat
import android.support.v4.view.animation.FastOutLinearInInterpolator
import android.text.*
import android.text.format.DateFormat
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.OverScroller
import com.kotlin.collapsablecalendar.WeekViewUtil.isSameDay
import com.kotlin.collapsablecalendar.WeekViewUtil.today
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class WeekView @JvmOverloads constructor(
    private val mContext: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(mContext, attrs, defStyleAttr) {

    var timeSlotMap = HashMap<Int, Float>()
    private var mTimeTextPaint: Paint? = null
    private var mTimeTextWidth: Float = 0.toFloat()
    private var mTimeTextHeight: Float = 0.toFloat()
    private var mHeaderTextPaint: Paint? = null
    private var mHeaderTextHeight: Float = 0.toFloat()
    private var mHeaderHeight: Float = 0.toFloat()
    private var mGestureDetector: GestureDetectorCompat? = null
    private var mScroller: OverScroller? = null
    private val mCurrentOrigin = PointF(0f, 0f)
    private var mCurrentScrollDirection = Direction.NONE
    private var mHeaderBackgroundPaint: Paint? = null
    private var mWidthPerDay: Float = 0.toFloat()
    private var mDayBackgroundPaint: Paint? = null
    private var mHourSeparatorPaint: Paint? = null
    private var mHeaderMarginBottom: Float = 0.toFloat()
    private var mTodayBackgroundPaint: Paint? = null
    private var mInActiveSlotBackgroundPaint: Paint? = null
    private var mFutureBackgroundPaint: Paint? = null
    private var mPastBackgroundPaint: Paint? = null
    private var mFutureWeekendBackgroundPaint: Paint? = null
    private var mPastWeekendBackgroundPaint: Paint? = null
    private var mNowLinePaint: Paint? = null
    private var mTodayHeaderTextPaint: Paint? = null
    private var mEventBackgroundPaint: Paint? = null
    private var mHeaderColumnWidth: Float = 0.toFloat()
    private var mEventRects: MutableList<EventRect>? = null
    private var mPreviousPeriodEvents: List<WeekViewEvent>? = null
    private var mCurrentPeriodEvents: List<WeekViewEvent>? = null
    private var mNextPeriodEvents: List<WeekViewEvent>? = null
    private var mEventTextPaint: TextPaint? = null
    private var mHeaderColumnBackgroundPaint: Paint? = null
    private var mFetchedPeriod = -1 // the middle period the calendar has fetched.
    private var mRefreshEvents = false
    private var mCurrentFlingDirection = Direction.NONE
    private var mScaleDetector: ScaleGestureDetector? = null
    private var mIsZooming: Boolean = false
    /**
     * Returns the first visible day in the week view.
     *
     * @return The first visible day in the week view.
     */
    var firstVisibleDay: Calendar? = null
        private set
    /**
     * Returns the last visible day in the week view.
     *
     * @return The last visible day in the week view.
     */
    var lastVisibleDay: Calendar? = null
        private set
    var isShowFirstDayOfWeekFirst = false
    private var mDefaultEventColor: Int = 0
    private var mMinimumFlingVelocity = 0
    private var mScaledTouchSlop = 0
    // Attributes and their default values.
    private var mHourHeight = 50
    private var mNewHourHeight = -1
    private var mMinHourHeight = 0 //no minimum specified (will be dynamic, based on screen)
    private var mMaxHourHeight = 250
    private var mEffectiveMinHourHeight = mMaxHourHeight //compensates for the fact that you can't keep zooming In.

    //private int mColumnGap = 10;
    private var mColumnGap = 1
    private var mFirstDayOfWeek = Calendar.MONDAY
    private var mTextSize = 12
    private var mHeaderColumnPadding = 10
    //private int mHeaderColumnPadding = 0;
    private var mHeaderColumnTextColor = Color.BLACK
    private var mNumberOfVisibleDays = 3
    private var mHeaderRowPadding = 10
    private var mHeaderRowBackgroundColor = Color.GRAY
    private var mDayBackgroundColor = Color.BLACK
    // Color.rgb(245, 245, 245);
    private var mPastBackgroundColor = Color.rgb(227, 227, 227)
    private var mFutureBackgroundColor = Color.rgb(245, 245, 245)
    private var mPastWeekendBackgroundColor = 0
    private var mFutureWeekendBackgroundColor = 0
    private var mNowLineColor = Color.rgb(232, 114, 0)
    //    private int mNowLineColor = Color.BLUE;
    private var mNowLineThickness = 3
    //    private int mHourSeparatorColor = Color.rgb(230, 230, 230);//lavender color
    private var mHourSeparatorColor = Color.rgb(220, 220, 220)//black color
    private var mHourSeparatorHeight = 1
    private var mTodayBackgroundColor = Color.rgb(239, 247, 254)

    private var mInActiveSlotBackgroundColor = Color.rgb(220, 220, 220)
    private var mTodayHeaderTextColor = Color.rgb(232, 114, 0)
    private val mNewEventBackgroundColor = Color.rgb(250, 181, 0)
    //Color.rgb(39, 137, 228);
    private var mEventTextSize = 12
    private var mEventTextColor = Color.BLACK
    private var mEventPadding = 8
    private var mHeaderColumnBackgroundColor = Color.GRAY
    private var mIsFirstDraw = true
    private var mAreDimensionsInvalid = true
    @Deprecated("")
    private var mDayNameLength = LENGTH_LONG
    private var mOverlappingEventGap = 0
    private var mEventMarginVertical = 0
    /**
     * Get the scrolling speed factor in horizontal direction.
     *
     * @return The speed factor in horizontal direction.
     */
    /**
     * Sets the speed for horizontal scrolling.
     *
     * @param xScrollingSpeed The new horizontal scrolling speed.
     */
    var xScrollingSpeed = 1f
    private var mScrollToDay: Calendar? = null
    private var mScrollToHour = -1.0
    /**
     * Set corner radius for event rect.
     *
     * @param eventCornerRadius the radius in px.
     */
    var eventCornerRadius = 0
    private var mShowDistinctWeekendColor = false
    private var mShowNowLine = true
    private var mShowDistinctPastFutureColor = true
    /**
     * Get whether the week view should fling horizontally.
     *
     * @return True if the week view has horizontal fling enabled.
     */
    /**
     * Set whether the week view should fling horizontally.
     *
     * @return True if it should have horizontal fling enabled.
     */
    var isHorizontalFlingEnabled = true
    /**
     * Get whether the week view should fling vertically.
     *
     * @return True if the week view has vertical fling enabled.
     */
    /**
     * Set whether the week view should fling vertically.
     *
     * @return True if it should have vertical fling enabled.
     */
    var isVerticalFlingEnabled = true
    /**
     * Get the height of AllDay-events.
     *
     * @return Height of AllDay-events.
     */
    /**
     * Set the height of AllDay-events.
     */
    var allDayEventHeight = 100
    /**
     * Get scroll duration
     *
     * @return scroll duration
     */
    /**
     * Set the scroll duration
     */
    var scrollDuration = 250

    // Listeners.
    var eventClickListener: EventClickListener? = null

    var eventLongPressListener: EventLongPressListener ? = null

    var emptyViewClickListener: EmptyViewClickListener ? = null

    var emptyViewLongPressListener: EmptyViewLongPressListener? = null


    private var mDateTimeInterpreter: DateTimeInterpreter? = null
    var weekViewLoader: WeekViewLoader? = null


    /**
     * Get the interpreter which provides the text to show in the header column and the header row.
     *
     * @return The date, time interpreter.
     */
    fun getDateTimeInterpreter(): DateTimeInterpreter {
        if (mDateTimeInterpreter == null) {
            mDateTimeInterpreter = object : DateTimeInterpreter {
                override fun interpretDate(date: Calendar): String {
                    try {
                        val sdf = if (mDayNameLength == LENGTH_SHORT) SimpleDateFormat(
                            "EEEEE M/dd",
                            Locale.getDefault()
                        ) else SimpleDateFormat("EEE M/dd", Locale.getDefault())
                        return sdf.format(date.time).toUpperCase()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        return ""
                    }

                }

                override fun interpretTime(hour: Int): String {
                    val calendar = Calendar.getInstance()
                    calendar.set(Calendar.HOUR_OF_DAY, hour)
                    calendar.set(Calendar.MINUTE, 0)

                    try {
                        val sdf = if (DateFormat.is24HourFormat(context)) SimpleDateFormat(
                            "HH:mm",
                            Locale.getDefault()
                        ) else SimpleDateFormat("hh a", Locale.getDefault())
                        return sdf.format(calendar.time)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        return ""
                    }

                    // return "";
                }
            }
        }
        return mDateTimeInterpreter as DateTimeInterpreter
    }

    fun setDateTimeInterpreter(dateTimeInterpreter: DateTimeInterpreter) {
        this.mDateTimeInterpreter = dateTimeInterpreter

        // Refresh time column width.
        //   initTextTimeWidth();
    }

    var scrollListener: ScrollListener? = null

    private var touched = false
    private var touchedTime: Calendar? = null
    // private float touchPointX, touchPointy;


    private var isDoubleTab = false
    internal var handler = Handler()
    internal var runnable: Runnable = Runnable {
        isDoubleTab = false
        invalidate()
    }

    private var lastSelectedTime = -1.0
    private var lastSelectedDate = -1.0

    lateinit var slotStartTime: Date
    lateinit var slotEndTime: Date

    private val dayInMillis = 1000 * 60 * 60 * 24

    private val mGestureListener = object : GestureDetector.SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent): Boolean {
            goToNearestOrigin()
            return true
        }

        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            // Check if view is zoomed.
            if (mIsZooming)
                return true

            when (mCurrentScrollDirection) {
                WeekView.Direction.NONE -> {
                    // Allow scrolling only in one direction.
                    if (Math.abs(distanceX) > Math.abs(distanceY)) {
                        if (distanceX > 0) {
                            mCurrentScrollDirection = Direction.LEFT
                        } else {
                            mCurrentScrollDirection = Direction.RIGHT
                        }
                    } else {
                        mCurrentScrollDirection = Direction.VERTICAL
                    }
                }
                WeekView.Direction.LEFT -> {
                    // Change direction if there was enough change.
                    if (Math.abs(distanceX) > Math.abs(distanceY) && distanceX < -mScaledTouchSlop) {
                        mCurrentScrollDirection = Direction.RIGHT
                    }
                }
                WeekView.Direction.RIGHT -> {
                    // Change direction if there was enough change.
                    if (Math.abs(distanceX) > Math.abs(distanceY) && distanceX > mScaledTouchSlop) {
                        mCurrentScrollDirection = Direction.LEFT
                    }
                }
            }

            // Calculate the new origin after scroll.
            when (mCurrentScrollDirection) {
                WeekView.Direction.LEFT, WeekView.Direction.RIGHT -> {
                    mCurrentOrigin.x -= distanceX * xScrollingSpeed
                    ViewCompat.postInvalidateOnAnimation(this@WeekView)
                }
                WeekView.Direction.VERTICAL -> {
                    mCurrentOrigin.y -= distanceY
                    ViewCompat.postInvalidateOnAnimation(this@WeekView)
                }
            }
            return true
        }

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            if (mIsZooming)
                return true

            if (mCurrentFlingDirection == Direction.LEFT && !isHorizontalFlingEnabled ||
                mCurrentFlingDirection == Direction.RIGHT && !isHorizontalFlingEnabled ||
                mCurrentFlingDirection == Direction.VERTICAL && !isVerticalFlingEnabled
            ) {
                return true
            }

            mScroller!!.forceFinished(true)

            mCurrentFlingDirection = mCurrentScrollDirection
            when (mCurrentFlingDirection) {
                WeekView.Direction.LEFT, WeekView.Direction.RIGHT -> mScroller!!.fling(
                    mCurrentOrigin.x.toInt(),
                    mCurrentOrigin.y.toInt(),
                    (velocityX * xScrollingSpeed).toInt(),
                    0,
                    Integer.MIN_VALUE,
                    Integer.MAX_VALUE,
                    (-((mHourHeight * 24).toFloat() + mHeaderHeight + (mHeaderRowPadding * 2).toFloat() + mHeaderMarginBottom + mTimeTextHeight / 2 - height)).toInt(),
                    0
                )
                WeekView.Direction.VERTICAL -> mScroller!!.fling(
                    mCurrentOrigin.x.toInt(),
                    mCurrentOrigin.y.toInt(),
                    0,
                    velocityY.toInt(),
                    Integer.MIN_VALUE,
                    Integer.MAX_VALUE,
                    (-((mHourHeight * 24).toFloat() + mHeaderHeight + (mHeaderRowPadding * 2).toFloat() + mHeaderMarginBottom + mTimeTextHeight / 2 - height)).toInt(),
                    0
                )
            }

            ViewCompat.postInvalidateOnAnimation(this@WeekView)
            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            Log.d(TAG, "onSingleTapUp: " + e.action)

            return super.onSingleTapUp(e)
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            Log.d(TAG, "onDoubleTap: ")
            return super.onDoubleTap(e)
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            Log.d(TAG, "onSingleTapConfirmed: " + e.action)

            // If the tap was on an event then trigger the callback.
            if (mEventRects != null && eventClickListener != null) {
                val reversedEventRects = mEventRects
                Collections.reverse(reversedEventRects!!)
                for (event in reversedEventRects) {
                    if (event.rectF != null && e.x > event.rectF!!.left && e.x < event.rectF!!.right && e.y > event.rectF!!.top && e.y < event.rectF!!.bottom) {
                        eventClickListener!!.onEventClick(event.originalEvent, event.rectF)
                        playSoundEffect(SoundEffectConstants.CLICK)
                        return super.onSingleTapConfirmed(e)
                    }
                }
            }

            // If the tap was on in an empty space, then trigger the callback.
            if (emptyViewClickListener != null && e.x > mHeaderColumnWidth && e.y > mHeaderHeight + (mHeaderRowPadding * 2).toFloat() + mHeaderMarginBottom) {
                val selectedTime = getTimeFromPoint(e.x, e.y)

                if (selectedTime != null) {
                    //                    playSoundEffect(SoundEffectConstants.CLICK);
                    //                    mEmptyViewClickListener.onEmptyViewClicked(selectedTime);
                    val currentTime = Calendar.getInstance()

                    var endTime = findSlotEndTime().hours.toFloat()

                    if (findSlotEndTime().hours == 23) {
                        endTime = 24f
                    }
                    handler.removeCallbacks(runnable)

                    if (lastSelectedTime == selectedTime.get(Calendar.HOUR_OF_DAY).toDouble() && lastSelectedDate == selectedTime.get(
                            Calendar.DATE
                        ).toDouble() && isDoubleTab
                    ) {
                        emptyViewClickListener!!.onEmptyViewClicked(selectedTime, getEndTimeFromPoint(e.x, e.y), true)
                        //invalidate();
                        handler.post(runnable)

                    } else {

                        if (!selectedTime.time.before(currentTime.time)
                            && selectedTime.get(Calendar.HOUR_OF_DAY) >= findSlotStartTime().hours
                            // && !selectedTime.getTime().before(new Date(findSlotStartTime().getTime() - 1 * hourInMillis))
                            && selectedTime.get(Calendar.HOUR_OF_DAY) < endTime
                        ) {
                            touched = true

                            isDoubleTab = true

                            touchedTime = selectedTime

                            // performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);

                            invalidate()

                            //    clickTime = System.currentTimeMillis();


                        }//&& !(selectedTime.getTime().after(new Date(findSlotEndTime().getTime() - 1 * hourInMillis)))
                    }
                }
            }

            return super.onSingleTapConfirmed(e)
        }

        override fun onLongPress(e: MotionEvent) {
            super.onLongPress(e)
            //            touchPointX = 0;
            //            touchPointy = 0;
            //

            if (eventLongPressListener != null && mEventRects != null) {
                val reversedEventRects = mEventRects
                Collections.reverse(reversedEventRects!!)
                for (event in reversedEventRects) {
                    if (event.rectF != null && e.x > event.rectF!!.left && e.x < event.rectF!!.right && e.y > event.rectF!!.top && e.y < event.rectF!!.bottom) {
                        eventLongPressListener!!.onEventLongPress(event.originalEvent, event.rectF)
                        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        return
                    }
                }
            }

            // If the tap was on in an empty space, then trigger the callback.
            if (emptyViewLongPressListener != null && e.x > mHeaderColumnWidth && e.y > mHeaderHeight + (mHeaderRowPadding * 2).toFloat() + mHeaderMarginBottom) {
                val selectedTime = getTimeFromPoint(e.x, e.y)

                if (selectedTime != null) {

                    val currentTime = Calendar.getInstance()

                    var endTime = findSlotEndTime().hours.toFloat()

                    if (findSlotEndTime().hours == 23) {
                        endTime = 24f
                    }

                    if (!selectedTime.time.before(currentTime.time)
                        && selectedTime.get(Calendar.HOUR_OF_DAY) >= findSlotStartTime().hours
                        // && !selectedTime.getTime().before(new Date(findSlotStartTime().getTime() - 1 * hourInMillis))
                        && selectedTime.get(Calendar.HOUR_OF_DAY) < endTime
                    ) {

                        //                        touchPointX = e.getX();
                        //                        touchPointy = e.getY();
                        touchedTime = selectedTime

                        touched = true

                        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)

                        emptyViewLongPressListener!!.onEmptyViewLongPress(selectedTime, getEndTimeFromPoint(e.x, e.y))

                        invalidate()
                    }//&& !(selectedTime.getTime().after(new Date(findSlotEndTime().getTime() - 1 * hourInMillis)))
                }
            }
        }
    }

    fun getMonthChangeListener(): MonthLoader.MonthChangeListener? {
        return if (weekViewLoader is MonthLoader) (weekViewLoader as MonthLoader).onMonthChangeListener else null
    }
    fun setMonthChangeListener(monthChangeListener: MonthLoader.MonthChangeListener) {
        this.weekViewLoader = MonthLoader(monthChangeListener)
    }



    fun setOnEmptyViewClickListener(emptyViewClickListener: EmptyViewClickListener) {
        this.emptyViewClickListener = emptyViewClickListener
    }


    fun setOnEmptyViewLongPressListener(emptyViewLongPressListener: EmptyViewLongPressListener) {
        this.emptyViewLongPressListener = emptyViewLongPressListener
    }


    fun setOnEventLongPressListener(listener: EventLongPressListener) {
        this.eventLongPressListener = listener
    }



    /**
     * Get the number of visible days in a week.
     *
     * @return The number of visible days in a week.
     */
    /**
     * Set the number of visible days in a week.
     *
     * @param numberOfVisibleDays The number of visible days in a week.
     */
    fun setNumberOfVisibleDays(numberOfVisibleDays : Int)
    {
        mNumberOfVisibleDays = numberOfVisibleDays
        mCurrentOrigin.x = 0f
        mCurrentOrigin.y = 0f
        invalidate()
    }

    fun setHourHeight(hourHeight: Int) {
        mNewHourHeight = hourHeight
        invalidate()
    }

    fun setColumnGap(columnGap: Int) {
        mColumnGap = columnGap
        invalidate()
    }

    fun setTextSize(textSize: Int) {
        mTextSize = textSize
        mTodayHeaderTextPaint?.setTextSize(mTextSize.toFloat())
        mHeaderTextPaint?.setTextSize(mTextSize.toFloat())
        mTimeTextPaint?.setTextSize(mTextSize.toFloat())
        invalidate()
    }


    /**
     * Set the first day of the week. First day of the week is used only when the week view is first
     * drawn. It does not of any effect after user starts scrolling horizontally.
     *
     *
     * **Note:** This method will only work if the week view is set to display more than 6 days at
     * once.
     *
     *
     * @param firstDayOfWeek The supported values are [Calendar.SUNDAY],
     * [Calendar.MONDAY], [Calendar.TUESDAY],
     * [Calendar.WEDNESDAY], [Calendar.THURSDAY],
     * [Calendar.FRIDAY].
     */
    var firstDayOfWeek: Int
        get() = mFirstDayOfWeek
        set(firstDayOfWeek) {
            mFirstDayOfWeek = firstDayOfWeek
            invalidate()
        }

    var headerColumnPadding: Int
        get() = mHeaderColumnPadding
        set(headerColumnPadding) {
            mHeaderColumnPadding = headerColumnPadding
            invalidate()
        }

    var headerColumnTextColor: Int
        get() = mHeaderColumnTextColor
        set(headerColumnTextColor) {
            mHeaderColumnTextColor = headerColumnTextColor
            mHeaderTextPaint!!.color = mHeaderColumnTextColor
            mTimeTextPaint!!.color = mHeaderColumnTextColor
            invalidate()
        }

    var headerRowPadding: Int
        get() = mHeaderRowPadding
        set(headerRowPadding) {
            mHeaderRowPadding = headerRowPadding
            invalidate()
        }

    var headerRowBackgroundColor: Int
        get() = mHeaderRowBackgroundColor
        set(headerRowBackgroundColor) {
            mHeaderRowBackgroundColor = headerRowBackgroundColor
            mHeaderBackgroundPaint!!.color = mHeaderRowBackgroundColor
            invalidate()
        }

    var dayBackgroundColor: Int
        get() = mDayBackgroundColor
        set(dayBackgroundColor) {
            mDayBackgroundColor = dayBackgroundColor
            mDayBackgroundPaint!!.color = mDayBackgroundColor
            invalidate()
        }

    var hourSeparatorColor: Int
        get() = mHourSeparatorColor
        set(hourSeparatorColor) {
            mHourSeparatorColor = hourSeparatorColor
            mHourSeparatorPaint!!.color = mHourSeparatorColor
            invalidate()
        }

    var todayBackgroundColor: Int
        get() = mTodayBackgroundColor
        set(todayBackgroundColor) {
            mTodayBackgroundColor = todayBackgroundColor
            mTodayBackgroundPaint!!.color = mTodayBackgroundColor
            invalidate()
        }

    fun setHourSeparatorHeight(hourSeparatorHeight: Int) {
        mHourSeparatorHeight = hourSeparatorHeight
        mHourSeparatorPaint?.setStrokeWidth(mHourSeparatorHeight.toFloat())
        invalidate()
    }
    fun setEventTextSize(eventTextSize: Int) {
        mEventTextSize = eventTextSize
        mEventTextPaint?.setTextSize(mEventTextSize.toFloat())
        invalidate()
    }

    fun setEventTextColor(eventTextColor: Int) {
        mEventTextColor = eventTextColor
        mEventTextPaint?.setColor(mEventTextColor)
        invalidate()
    }

    fun setEventPadding(eventPadding: Int) {
        mEventPadding = eventPadding
        invalidate()
    }

    fun setHeaderColumnBackgroundColor(headerColumnBackgroundColor: Int) {
        mHeaderColumnBackgroundColor = headerColumnBackgroundColor
        mHeaderColumnBackgroundPaint?.setColor(mHeaderColumnBackgroundColor)
        invalidate()
    }

    fun setDefaultEventColor(defaultEventColor: Int) {
        mDefaultEventColor = defaultEventColor
        invalidate()
    }
    /**
     * **Note:** Use [.setDateTimeInterpreter] and
     * [.getDateTimeInterpreter] instead.
     *
     * @return Either long or short day name is being used.
     */
    /**
     * Set the length of the day name displayed in the header row. Example of short day names is
     * 'M' for 'Monday' and example of long day names is 'Mon' for 'Monday'.
     *
     *
     * **Note:** Use [.setDateTimeInterpreter] instead.
     *
     *
     * @param length Supported values are [WeekView.LENGTH_SHORT] and
     * [WeekView.LENGTH_LONG].
     */
    var dayNameLength: Int
        @Deprecated("")
        get() = mDayNameLength
        @Deprecated("")
        set(length) {
            if (length != LENGTH_LONG && length != LENGTH_SHORT) {
                throw IllegalArgumentException("length parameter must be either LENGTH_LONG or LENGTH_SHORT")
            }
            this.mDayNameLength = length
        }

    /**
     * Set the gap between overlapping events.
     *
     * @param overlappingEventGap The gap between overlapping events.
     */
    var overlappingEventGap: Int
        get() = mOverlappingEventGap
        set(overlappingEventGap) {
            this.mOverlappingEventGap = overlappingEventGap
            invalidate()
        }

    /**
     * Set the top and bottom margin of the event. The event will release this margin from the top
     * and bottom edge. This margin is useful for differentiation consecutive events.
     *
     * @param eventMarginVertical The top and bottom margin.
     */
    var eventMarginVertical: Int
        get() = mEventMarginVertical
        set(eventMarginVertical) {
            this.mEventMarginVertical = eventMarginVertical
            invalidate()
        }

    /**
     * Whether weekends should have a background color different from the normal day background
     * color. The weekend background colors are defined by the attributes
     * `futureWeekendBackgroundColor` and `pastWeekendBackgroundColor`.
     *
     * @return True if weekends should have different background colors.
     */
    /**
     * Set whether weekends should have a background color different from the normal day background
     * color. The weekend background colors are defined by the attributes
     * `futureWeekendBackgroundColor` and `pastWeekendBackgroundColor`.
     *
     * @param showDistinctWeekendColor True if weekends should have different background colors.
     */
    var isShowDistinctWeekendColor: Boolean
        get() = mShowDistinctWeekendColor
        set(showDistinctWeekendColor) {
            this.mShowDistinctWeekendColor = showDistinctWeekendColor
            invalidate()
        }

    /**
     * Whether past and future days should have two different background colors. The past and
     * future day colors are defined by the attributes `futureBackgroundColor` and
     * `pastBackgroundColor`.
     *
     * @return True if past and future days should have two different background colors.
     */
    /**
     * Set whether weekends should have a background color different from the normal day background
     * color. The past and future day colors are defined by the attributes `futureBackgroundColor`
     * and `pastBackgroundColor`.
     *
     * @param showDistinctPastFutureColor True if past and future should have two different
     * background colors.
     */
    var isShowDistinctPastFutureColor: Boolean
        get() = mShowDistinctPastFutureColor
        set(showDistinctPastFutureColor) {
            this.mShowDistinctPastFutureColor = showDistinctPastFutureColor
            invalidate()
        }

    /**
     * Get whether "now" line should be displayed. "Now" line is defined by the attributes
     * `nowLineColor` and `nowLineThickness`.
     *
     * @return True if "now" line should be displayed.
     */
    /**
     * Set whether "now" line should be displayed. "Now" line is defined by the attributes
     * `nowLineColor` and `nowLineThickness`.
     *
     * @param showNowLine True if "now" line should be displayed.
     */
    var isShowNowLine: Boolean
        get() = mShowNowLine
        set(showNowLine) {
            this.mShowNowLine = showNowLine
            invalidate()
        }

    /**
     * Get the "now" line color.
     *
     * @return The color of the "now" line.
     */
    /**
     * Set the "now" line color.
     *
     * @param nowLineColor The color of the "now" line.
     */
    var nowLineColor: Int
        get() = mNowLineColor
        set(nowLineColor) {
            this.mNowLineColor = nowLineColor
            invalidate()
        }

    /**
     * Get the "now" line thickness.
     *
     * @return The thickness of the "now" line.
     */
    /**
     * Set the "now" line thickness.
     *
     * @param nowLineThickness The thickness of the "now" line.
     */
    var nowLineThickness: Int
        get() = mNowLineThickness
        set(nowLineThickness) {
            this.mNowLineThickness = nowLineThickness
            invalidate()
        }

    /**
     * Get the first hour that is visible on the screen.
     *
     * @return The first hour that is visible.
     */
    val firstVisibleHour: Double
        get() {
            Log.e(TAG, "visiblehour=" + -mCurrentOrigin.y / mHourHeight)
            return (-mCurrentOrigin.y / mHourHeight).toDouble()
        }

    private enum class Direction {
        NONE, LEFT, RIGHT, VERTICAL
    }


    fun findSlotStartTime(): Date {
        // return new Date(slotStartTime.getTime() - dayInMillis);
        return slotStartTime
    }

    fun addSlotStartTime(slotStartTime: Date) {
        this.slotStartTime = slotStartTime
    }

    fun findSlotEndTime(): Date {
        return Date(slotEndTime.time - dayInMillis)
        // return slotEndTime;
    }

    fun addSlotEndTime(slotEndTime: Date) {
        this.slotEndTime = slotEndTime
    }

    init {

        // Get the attribute values (if any).
        val a = mContext.theme.obtainStyledAttributes(attrs, R.styleable.WeekView, 0, 0)
        try {
            mFirstDayOfWeek = a.getInteger(R.styleable.WeekView_firstDayOfWeek, mFirstDayOfWeek)
            mHourHeight = a.getDimensionPixelSize(R.styleable.WeekView_hourHeight, mHourHeight)
            mMinHourHeight = a.getDimensionPixelSize(R.styleable.WeekView_minHourHeight, mMinHourHeight)
            mEffectiveMinHourHeight = mMinHourHeight
            mMaxHourHeight = a.getDimensionPixelSize(R.styleable.WeekView_maxHourHeight, mMaxHourHeight)
            mTextSize = a.getDimensionPixelSize(
                R.styleable.WeekView_textSize,
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_SP,
                    mTextSize.toFloat(),
                    mContext.resources.displayMetrics
                ).toInt()
            )
            mHeaderColumnPadding =
                    a.getDimensionPixelSize(R.styleable.WeekView_headerColumnPadding, mHeaderColumnPadding)
            mColumnGap = a.getDimensionPixelSize(R.styleable.WeekView_columnGap, mColumnGap)
            mHeaderColumnTextColor = a.getColor(R.styleable.WeekView_headerColumnTextColor, mHeaderColumnTextColor)
            mNumberOfVisibleDays = a.getInteger(R.styleable.WeekView_noOfVisibleDays, mNumberOfVisibleDays)
            isShowFirstDayOfWeekFirst =
                    a.getBoolean(R.styleable.WeekView_showFirstDayOfWeekFirst, isShowFirstDayOfWeekFirst)
            mHeaderRowPadding = a.getDimensionPixelSize(R.styleable.WeekView_headerRowPadding, mHeaderRowPadding)
            mHeaderRowBackgroundColor =
                    a.getColor(R.styleable.WeekView_headerRowBackgroundColor, mHeaderRowBackgroundColor)
            mDayBackgroundColor = a.getColor(R.styleable.WeekView_dayBackgroundColor, mDayBackgroundColor)
            mFutureBackgroundColor = a.getColor(R.styleable.WeekView_futureBackgroundColor, mFutureBackgroundColor)
            mPastBackgroundColor = a.getColor(R.styleable.WeekView_pastBackgroundColor, mPastBackgroundColor)
            mFutureWeekendBackgroundColor = a.getColor(
                R.styleable.WeekView_futureWeekendBackgroundColor,
                mFutureBackgroundColor
            ) // If not set, use the same color as in the week
            mPastWeekendBackgroundColor =
                    a.getColor(R.styleable.WeekView_pastWeekendBackgroundColor, mPastBackgroundColor)
            mNowLineColor = a.getColor(R.styleable.WeekView_nowLineColor, mNowLineColor)
            mNowLineThickness = a.getDimensionPixelSize(R.styleable.WeekView_nowLineThickness, mNowLineThickness)
            mHourSeparatorColor = a.getColor(R.styleable.WeekView_hourSeparatorColor, mHourSeparatorColor)
            mTodayBackgroundColor = a.getColor(R.styleable.WeekView_todayBackgroundColor, mTodayBackgroundColor)
            mInActiveSlotBackgroundColor =
                    a.getColor(R.styleable.WeekView_inActiveSlotBackgroundColor, mInActiveSlotBackgroundColor)
            mHourSeparatorHeight =
                    a.getDimensionPixelSize(R.styleable.WeekView_hourSeparatorHeight, mHourSeparatorHeight)
            mTodayHeaderTextColor = a.getColor(R.styleable.WeekView_todayHeaderTextColor, mTodayHeaderTextColor)
            mEventTextSize = a.getDimensionPixelSize(
                R.styleable.WeekView_eventTextSize,
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_SP,
                    mEventTextSize.toFloat(),
                    mContext.resources.displayMetrics
                ).toInt()
            )
            mEventTextColor = a.getColor(R.styleable.WeekView_eventTextColor, mEventTextColor)
            mEventPadding = a.getDimensionPixelSize(R.styleable.WeekView_eventPadding, mEventPadding)
            mHeaderColumnBackgroundColor =
                    a.getColor(R.styleable.WeekView_headerColumnBackground, mHeaderColumnBackgroundColor)
            mDayNameLength = a.getInteger(R.styleable.WeekView_dayNameLength, mDayNameLength)
            mOverlappingEventGap =
                    a.getDimensionPixelSize(R.styleable.WeekView_overlappingEventGap, mOverlappingEventGap)
            mEventMarginVertical =
                    a.getDimensionPixelSize(R.styleable.WeekView_eventMarginVertical, mEventMarginVertical)
            xScrollingSpeed = a.getFloat(R.styleable.WeekView_xScrollingSpeed, xScrollingSpeed)
            eventCornerRadius = a.getDimensionPixelSize(R.styleable.WeekView_eventCornerRadius, eventCornerRadius)
            mShowDistinctPastFutureColor =
                    a.getBoolean(R.styleable.WeekView_showDistinctPastFutureColor, mShowDistinctPastFutureColor)
            mShowDistinctWeekendColor =
                    a.getBoolean(R.styleable.WeekView_showDistinctWeekendColor, mShowDistinctWeekendColor)
            mShowNowLine = a.getBoolean(R.styleable.WeekView_showNowLine, mShowNowLine)
            isHorizontalFlingEnabled =
                    a.getBoolean(R.styleable.WeekView_horizontalFlingEnabled, isHorizontalFlingEnabled)
            isVerticalFlingEnabled = a.getBoolean(R.styleable.WeekView_verticalFlingEnabled, isVerticalFlingEnabled)
            allDayEventHeight = a.getDimensionPixelSize(R.styleable.WeekView_allDayEventHeight, allDayEventHeight)
            scrollDuration = a.getInt(R.styleable.WeekView_scrollDuration, scrollDuration)
        } finally {
            a.recycle()
        }

        init()
    }// Hold references.

    private fun init() {
        // Scrolling initialization.
        mGestureDetector = GestureDetectorCompat(mContext, mGestureListener)
        mScroller = OverScroller(mContext, FastOutLinearInInterpolator())

        mMinimumFlingVelocity = ViewConfiguration.get(mContext).scaledMinimumFlingVelocity
        mScaledTouchSlop = ViewConfiguration.get(mContext).scaledTouchSlop

        // Measure settings for time column.
        mTimeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mTimeTextPaint!!.textAlign = Paint.Align.RIGHT
        mTimeTextPaint!!.textSize = mTextSize.toFloat()
        mTimeTextPaint!!.color = mHeaderColumnTextColor
        val rect = Rect()
        mTimeTextPaint!!.getTextBounds("00 PM", 0, "00 PM".length, rect)
        mTimeTextHeight = rect.height().toFloat()
        // mHeaderMarginBottom = mTimeTextHeight / 2;
        mHeaderMarginBottom = 0f
        initTextTimeWidth()

        // Measure settings for header row.
        mHeaderTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mHeaderTextPaint!!.color = mHeaderColumnTextColor
        mHeaderTextPaint!!.textAlign = Paint.Align.CENTER
        mHeaderTextPaint!!.textSize = mTextSize.toFloat()
        mHeaderTextPaint!!.getTextBounds("00 PM", 0, "00 PM".length, rect)
        mHeaderTextHeight = rect.height().toFloat()
        mHeaderTextPaint!!.typeface = Typeface.DEFAULT_BOLD

        // Prepare header background paint.
        mHeaderBackgroundPaint = Paint()
        mHeaderBackgroundPaint!!.color = mHeaderRowBackgroundColor

        // Prepare day background color paint.
        mDayBackgroundPaint = Paint()
        mDayBackgroundPaint!!.color = mDayBackgroundColor
        mFutureBackgroundPaint = Paint()
        mFutureBackgroundPaint!!.color = mFutureBackgroundColor
        mPastBackgroundPaint = Paint()
        mPastBackgroundPaint!!.color = mPastBackgroundColor
        mFutureWeekendBackgroundPaint = Paint()
        mFutureWeekendBackgroundPaint!!.color = mFutureWeekendBackgroundColor
        mPastWeekendBackgroundPaint = Paint()
        mPastWeekendBackgroundPaint!!.color = mPastWeekendBackgroundColor

        // Prepare hour separator color paint.
        mHourSeparatorPaint = Paint()
        mHourSeparatorPaint!!.style = Paint.Style.STROKE
        mHourSeparatorPaint!!.strokeWidth = mHourSeparatorHeight.toFloat()
        mHourSeparatorPaint!!.color = mHourSeparatorColor

        // Prepare the "now" line color paint
        mNowLinePaint = Paint()
        mNowLinePaint!!.strokeWidth = mNowLineThickness.toFloat()
        mNowLinePaint!!.color = mNowLineColor

        // Prepare today background color paint.
        mTodayBackgroundPaint = Paint()
        mTodayBackgroundPaint!!.color = mTodayBackgroundColor

        // Prepare inactive slot background color paint.
        mInActiveSlotBackgroundPaint = Paint()
        mInActiveSlotBackgroundPaint!!.color = mInActiveSlotBackgroundColor


        // Prepare today header text color paint.
        mTodayHeaderTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mTodayHeaderTextPaint!!.textAlign = Paint.Align.CENTER
        mTodayHeaderTextPaint!!.textSize = mTextSize.toFloat()
        mTodayHeaderTextPaint!!.typeface = Typeface.DEFAULT_BOLD
        mTodayHeaderTextPaint!!.color = mNewEventBackgroundColor

        // Prepare event background color.
        mEventBackgroundPaint = Paint()
        mEventBackgroundPaint!!.color = Color.rgb(174, 208, 238)

        // Prepare header column background color.
        mHeaderColumnBackgroundPaint = Paint()
        mHeaderColumnBackgroundPaint!!.color = mHeaderColumnBackgroundColor

        // Prepare event text size and color.
        mEventTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.LINEAR_TEXT_FLAG)
        mEventTextPaint!!.style = Paint.Style.FILL
        mEventTextPaint!!.color = Color.BLACK
        mEventTextPaint!!.textSize = mEventTextSize.toFloat()

        // Set default event color.
        //  mDefaultEventColor = Color.parseColor("#9fc6e7");
        mDefaultEventColor = resources.getColor(R.color.event_color_04)


        mScaleDetector = ScaleGestureDetector(mContext, object : ScaleGestureDetector.OnScaleGestureListener {
            override fun onScaleEnd(detector: ScaleGestureDetector) {
                mIsZooming = false
            }

            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                mIsZooming = true
                goToNearestOrigin()
                return true
            }

            override fun onScale(detector: ScaleGestureDetector): Boolean {
                mNewHourHeight = Math.round(mHourHeight * detector.scaleFactor)
                invalidate()
                return true
            }
        })
    }

    // fix rotation changes
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mAreDimensionsInvalid = true
    }

    /**
     * Initialize time column width. Calculate value with all possible hours (supposed widest text).
     */
    private fun initTextTimeWidth() {
        mTimeTextWidth = 0f
        for (i in 0..23) {
            // Measure time string and get max width.
            val time = getDateTimeInterpreter().interpretTime(i)
                ?: throw IllegalStateException("A DateTimeInterpreter must not return null time")
            mTimeTextWidth = Math.max(mTimeTextWidth, mTimeTextPaint!!.measureText(time))
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw the header row.
        drawHeaderRowAndEvents(canvas)

        // Draw the time column and all the axes/separators.
        drawTimeColumnAndAxes(canvas)

        touched = false
    }

    private fun calculateHeaderHeight() {
        //Make sure the header is the right size (depends on AllDay events)
        var containsAllDayEvent = false
        if (mEventRects != null && mEventRects!!.size > 0) {
            for (dayNumber in 0 until mNumberOfVisibleDays) {
                val day = firstVisibleDay!!.clone() as Calendar
                day.add(Calendar.DATE, dayNumber)
                for (i in mEventRects!!.indices) {

                    if (isSameDay(mEventRects!![i].event.startTime!!, day) && mEventRects!![i].event.isAllDay) {
                        containsAllDayEvent = true
                        break
                    }
                }
                if (containsAllDayEvent) {
                    break
                }
            }
        }
        mHeaderHeight = mHeaderTextHeight + 30

        //  Log.d(TAG, "calculateHeaderHeight: " + mHeaderHeight + ">>" + mHeaderTextHeight);
        //        if (containsAllDayEvent) {
        //            mHeaderHeight = mHeaderTextHeight + (mAllDayEventHeight + mHeaderMarginBottom);
        //        } else {
        //            mHeaderHeight = mHeaderTextHeight;
        //        }
    }

    private fun drawTimeColumnAndAxes(canvas: Canvas) {


        // Draw the background color for the header column.
        canvas.drawRect(
            0f,
            mHeaderHeight + mHeaderRowPadding * 2,
            mHeaderColumnWidth,
            height.toFloat(),
            mHeaderColumnBackgroundPaint!!
        )

        // Clip to paint in left column only.
        canvas.clipRect(
            0f,
            mHeaderHeight + mHeaderRowPadding * 2,
            mHeaderColumnWidth,
            height.toFloat(),
            Region.Op.REPLACE
        )

        for (i in 0..23) {
            val top =
                mHeaderHeight + (mHeaderRowPadding * 2).toFloat() + mCurrentOrigin.y + (mHourHeight * i).toFloat() + mHeaderMarginBottom
            // Draw the text if its y position is not outside of the visible area. The pivot point of the text is the point at the bottom-right corner.
            val time = getDateTimeInterpreter().interpretTime(i)
                ?: throw IllegalStateException("A DateTimeInterpreter must not return null time")
            if (top < height) {
                canvas.drawText(time, mTimeTextWidth + mHeaderColumnPadding, top + mTimeTextHeight, mTimeTextPaint!!)
                //  Log.d(TAG, "drawTimeColumnAndAxes:" + i + "=" + top + "=" + time + "=" + mTimeTextWidth + mHeaderColumnPadding);

                if (!touched) {
                    timeSlotMap[i] = top
                }

            }
            //  Log.d("drawTimeColumnAndAxes", "timeslot size=" + timeSlotMap.size());
        }

    }

    private fun drawHeaderRowAndEvents(canvas: Canvas) {
        // Calculate the available width for each day.
        mHeaderColumnWidth = mTimeTextWidth + mHeaderColumnPadding * 2
        mWidthPerDay = width.toFloat() - mHeaderColumnWidth - (mColumnGap * (mNumberOfVisibleDays - 1)).toFloat()
        mWidthPerDay = mWidthPerDay / mNumberOfVisibleDays

        calculateHeaderHeight() //Make sure the header is the right size (depends on AllDay events)

        val today = today()
        if (mAreDimensionsInvalid) {

            mEffectiveMinHourHeight = Math.max(
                mMinHourHeight,
                ((height.toFloat() - mHeaderHeight - (mHeaderRowPadding * 2).toFloat() - mHeaderMarginBottom) / 24).toInt()
            )

            mAreDimensionsInvalid = false
            if (mScrollToDay != null)
                goToDate(mScrollToDay!!)

            mAreDimensionsInvalid = false

            if (mScrollToHour >= 0)
                goToHour(mScrollToHour)

            mScrollToDay = null
            mScrollToHour = -1.0
            mAreDimensionsInvalid = false
        }
        if (mIsFirstDraw) {
            mIsFirstDraw = false

            // If the week view is being drawn for the first time, then consider the first day of the week.
            if (mNumberOfVisibleDays >= 7 && today.get(Calendar.DAY_OF_WEEK) != mFirstDayOfWeek && isShowFirstDayOfWeekFirst) {
                val difference = today.get(Calendar.DAY_OF_WEEK) - mFirstDayOfWeek
                mCurrentOrigin.x += (mWidthPerDay + mColumnGap) * difference
            }
        }

        // Calculate the new height due to the zooming.
        if (mNewHourHeight > 0) {
            if (mNewHourHeight < mEffectiveMinHourHeight)
                mNewHourHeight = mEffectiveMinHourHeight
            else if (mNewHourHeight > mMaxHourHeight)
                mNewHourHeight = mMaxHourHeight

            mCurrentOrigin.y = mCurrentOrigin.y / mHourHeight * mNewHourHeight
            mHourHeight = mNewHourHeight
            mNewHourHeight = -1
        }

        // If the new mCurrentOrigin.y is invalid, make it valid.
        if (mCurrentOrigin.y < height.toFloat() - (mHourHeight * 24).toFloat() - mHeaderHeight - (mHeaderRowPadding * 2).toFloat() - mHeaderMarginBottom - mTimeTextHeight / 2)
            mCurrentOrigin.y = height.toFloat() - (mHourHeight * 24).toFloat() - mHeaderHeight -
                    (mHeaderRowPadding * 2).toFloat() - mHeaderMarginBottom - mTimeTextHeight / 2

        // Don't put an "else if" because it will trigger a glitch when completely zoomed out and
        // scrolling vertically.
        if (mCurrentOrigin.y > 0) {
            mCurrentOrigin.y = 0f
        }

        // Consider scroll offset.
        val leftDaysWithGaps = (-Math.ceil((mCurrentOrigin.x / (mWidthPerDay + mColumnGap)).toDouble())).toInt()
        val startFromPixel = mCurrentOrigin.x + (mWidthPerDay + mColumnGap) * leftDaysWithGaps +
                mHeaderColumnWidth
        var startPixel = startFromPixel

        // Prepare to iterate for each day.
        var day = today.clone() as Calendar
        day.add(Calendar.HOUR, 6)

        // Prepare to iterate for each hour to draw the hour lines.
        var lineCount = ((height.toFloat() - mHeaderHeight - (mHeaderRowPadding * 2).toFloat() -
                mHeaderMarginBottom) / mHourHeight).toInt() + 1
        lineCount = lineCount * (mNumberOfVisibleDays + 1)
        val hourLines = FloatArray(lineCount * 4)

        // Clear the cache for event rectangles.
        if (mEventRects != null) {
            for (eventRect in mEventRects!!) {
                eventRect.rectF = null
            }
        }

        // Clip to paint events only.
        canvas.clipRect(
            mHeaderColumnWidth,
            mHeaderHeight + (mHeaderRowPadding * 2).toFloat() + mHeaderMarginBottom + mTimeTextHeight / 2,
            width.toFloat(),
            height.toFloat(),
            Region.Op.REPLACE
        )


        // Iterate through each day.
        val oldFirstVisibleDay = firstVisibleDay
        firstVisibleDay = today.clone() as Calendar
        firstVisibleDay!!.add(Calendar.DATE, -Math.round(mCurrentOrigin.x / (mWidthPerDay + mColumnGap)))
        if (firstVisibleDay != oldFirstVisibleDay && scrollListener != null) {
            scrollListener!!.onFirstVisibleDayChanged(firstVisibleDay, oldFirstVisibleDay)
        }
        for (dayNumber in leftDaysWithGaps + 1..leftDaysWithGaps + mNumberOfVisibleDays + 1) {

            // Check if the day is today.
            day = today.clone() as Calendar
            lastVisibleDay = day.clone() as Calendar
            day.add(Calendar.DATE, dayNumber - 1)
            lastVisibleDay!!.add(Calendar.DATE, dayNumber - 2)
            val sameDay = isSameDay(day, today)

            // Get more events if necessary. We want to store the events 3 months beforehand. Get
            // events only when it is the first iteration of the loop.
            if (mEventRects == null || mRefreshEvents ||
                dayNumber == leftDaysWithGaps + 1 && mFetchedPeriod != weekViewLoader!!.toWeekViewPeriodIndex(day).toInt() &&
                Math.abs(mFetchedPeriod - weekViewLoader!!.toWeekViewPeriodIndex(day)) > 0.5
            ) {
                getMoreEvents(day)
                mRefreshEvents = false
            }

            // Draw background color for each day.
            val start = if (startPixel < mHeaderColumnWidth) mHeaderColumnWidth else startPixel


            if (mWidthPerDay + startPixel - start > 0) {
                val startY =
                    mHeaderHeight + (mHeaderRowPadding * 2).toFloat() + mTimeTextHeight / 2 + mHeaderMarginBottom + mCurrentOrigin.y

                //  Log.d(TAG, "drawHeaderRowAndEvents: " + startY + " day " + day);

                if (mShowDistinctPastFutureColor) {
                    val isWeekend =
                        day.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || day.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
                    val pastPaint =
                        if (isWeekend && mShowDistinctWeekendColor) mPastWeekendBackgroundPaint else mPastBackgroundPaint
                    val futurePaint =
                        if (isWeekend && mShowDistinctWeekendColor) mFutureWeekendBackgroundPaint else mFutureBackgroundPaint

                    val startY =
                        mHeaderHeight + (mHeaderRowPadding * 2).toFloat() + mTimeTextHeight / 2 + mHeaderMarginBottom + mCurrentOrigin.y

                    if (sameDay) {
                        val now = Calendar.getInstance()
                        val beforeNow = (now.get(Calendar.HOUR_OF_DAY) + now.get(Calendar.MINUTE) / 60.0f) * mHourHeight
                        canvas.drawRect(start, startY, startPixel + mWidthPerDay, startY + beforeNow, pastPaint!!)
                        canvas.drawRect(
                            start,
                            startY + beforeNow,
                            startPixel + mWidthPerDay,
                            height.toFloat(),
                            futurePaint!!
                        )

                    } else if (day.before(today)) {

                        canvas.drawRect(start, startY, startPixel + mWidthPerDay, height.toFloat(), pastPaint!!)


                    } else {

                        canvas.drawRect(start, startY, startPixel + mWidthPerDay, height.toFloat(), futurePaint);

                    }
                } else {
                    canvas.drawRect(
                        start,
                        mHeaderHeight + (mHeaderRowPadding * 2).toFloat() + mTimeTextHeight / 2 + mHeaderMarginBottom,
                        startPixel + mWidthPerDay,
                        height.toFloat(),
                        if (sameDay) mTodayBackgroundPaint else mDayBackgroundPaint
                    )

                }
            }

            // Prepare the separator lines for hours.
            var i = 0
            for (hourNumber in 0..23) {
                val top =
                    mHeaderHeight + (mHeaderRowPadding * 2).toFloat() + mCurrentOrigin.y + (mHourHeight * hourNumber).toFloat() + mTimeTextHeight / 2 + mHeaderMarginBottom
                if (top > mHeaderHeight + (mHeaderRowPadding * 2).toFloat() + mTimeTextHeight / 2 + mHeaderMarginBottom - mHourSeparatorHeight && top < height && startPixel + mWidthPerDay - start > 0) {
                    hourLines[i * 4] = start
                    hourLines[i * 4 + 1] = top
                    hourLines[i * 4 + 2] = startPixel + mWidthPerDay
                    hourLines[i * 4 + 3] = top
                    i++
                }
            }

            // Draw the lines for hours.
            canvas.drawLines(hourLines, mHourSeparatorPaint!!)

            // Draw the events.
            drawEvents(day, startPixel, canvas)

            // Draw the line at the current time.
            if (mShowNowLine && sameDay) {
                val startY =
                    mHeaderHeight + (mHeaderRowPadding * 2).toFloat() + mTimeTextHeight / 2 + mHeaderMarginBottom + mCurrentOrigin.y
                val now = Calendar.getInstance()
                val beforeNow = (now.get(Calendar.HOUR_OF_DAY) + now.get(Calendar.MINUTE) / 60.0f) * mHourHeight
                canvas.drawLine(
                    start,
                    startY + beforeNow,
                    startPixel + mWidthPerDay,
                    startY + beforeNow,
                    mNowLinePaint!!
                )
            }

            // In the next iteration, start from the next day.
            startPixel += mWidthPerDay + mColumnGap
        }

        // Hide everything in the first cell (top left corner).
        canvas.clipRect(
            0f,
            0f,
            mTimeTextWidth + mHeaderColumnPadding * 2,
            mHeaderHeight + mHeaderRowPadding * 2,
            Region.Op.REPLACE
        )
        canvas.drawRect(
            0f,
            0f,
            mTimeTextWidth + mHeaderColumnPadding * 2,
            mHeaderHeight + mHeaderRowPadding * 2,
            mHeaderBackgroundPaint!!
        )

        // Clip to paint header row only.
        canvas.clipRect(
            mHeaderColumnWidth,
            0f,
            width.toFloat(),
            mHeaderHeight + mHeaderRowPadding * 2,
            Region.Op.REPLACE
        )

        // Draw the header background.
        canvas.drawRect(0f, 0f, width.toFloat(), mHeaderHeight + mHeaderRowPadding * 2, mHeaderBackgroundPaint!!)

        // Draw the header row texts.
        startPixel = startFromPixel
        for (dayNumber in leftDaysWithGaps + 1..leftDaysWithGaps + mNumberOfVisibleDays + 1) {
            // Check if the day is today.
            day = today.clone() as Calendar
            day.add(Calendar.DATE, dayNumber - 1)
            val sameDay = isSameDay(day, today)

            // Draw the day labels.
            val dayLabel = getDateTimeInterpreter().interpretDate(day)
                ?: throw IllegalStateException("A DateTimeInterpreter must not return null date")
// canvas.drawText(dayLabel, startPixel + mWidthPerDay / 2, mHeaderTextHeight + mHeaderRowPadding, sameDay ? mTodayHeaderTextPaint : mHeaderTextPaint);
            //  drawAllDayEvents(day, startPixel, canvas);
            val x = startPixel + mWidthPerDay / 2
            var y = mHeaderTextHeight + mHeaderRowPadding
            mTodayHeaderTextPaint!!.textSize = 40f
            mHeaderTextPaint!!.textSize = 40f
            for (line in dayLabel.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
                canvas.drawText(line, x, y, if (sameDay) mTodayHeaderTextPaint else mHeaderTextPaint)
                mTodayHeaderTextPaint!!.textSize = 24f
                mHeaderTextPaint!!.textSize = 24f
                y += -mHeaderTextPaint!!.ascent() + mHeaderTextPaint!!.descent()
            }

            startPixel += mWidthPerDay + mColumnGap
        }

    }

    /**
     * Get the time and date where the user clicked on.
     *
     * @param x The x position of the touch event.
     * @param y The y position of the touch event.
     * @return The time and date at the clicked position.
     */
    private fun getTimeFromPoint(x: Float, y: Float): Calendar? {

        val leftDaysWithGaps = (-Math.ceil((mCurrentOrigin.x / (mWidthPerDay + mColumnGap)).toDouble())).toInt()
        var startPixel = mCurrentOrigin.x + (mWidthPerDay + mColumnGap) * leftDaysWithGaps +
                mHeaderColumnWidth
        for (dayNumber in leftDaysWithGaps + 1..leftDaysWithGaps + mNumberOfVisibleDays + 1) {
            val start = if (startPixel < mHeaderColumnWidth) mHeaderColumnWidth else startPixel
            if (mWidthPerDay + startPixel - start > 0 && x > start && x < startPixel + mWidthPerDay) {
                val day = today()
                day.add(Calendar.DATE, dayNumber - 1)
                val pixelsFromZero = (y - mCurrentOrigin.y - mHeaderHeight
                        - (mHeaderRowPadding * 2).toFloat() - mTimeTextHeight / 2 - mHeaderMarginBottom)
                val hour = (pixelsFromZero / mHourHeight).toInt()
                //   int minute = (int) (60 * (pixelsFromZero - hour * mHourHeight) / mHourHeight);
                day.add(Calendar.HOUR, hour)
                day.set(Calendar.MINUTE, 0)
                return day
            }
            startPixel += mWidthPerDay + mColumnGap
        }
        return null
    }

    private fun getEndTimeFromPoint(x: Float, y: Float): Calendar? {

        val leftDaysWithGaps = (-Math.ceil((mCurrentOrigin.x / (mWidthPerDay + mColumnGap)).toDouble())).toInt()
        var startPixel = mCurrentOrigin.x + (mWidthPerDay + mColumnGap) * leftDaysWithGaps +
                mHeaderColumnWidth
        for (dayNumber in leftDaysWithGaps + 1..leftDaysWithGaps + mNumberOfVisibleDays + 1) {
            val start = if (startPixel < mHeaderColumnWidth) mHeaderColumnWidth else startPixel
            if (mWidthPerDay + startPixel - start > 0 && x > start && x < startPixel + mWidthPerDay) {
                val day = today()
                day.add(Calendar.DATE, dayNumber - 1)
                val pixelsFromZero = (y - mCurrentOrigin.y - mHeaderHeight
                        - (mHeaderRowPadding * 2).toFloat() - mTimeTextHeight / 2 - mHeaderMarginBottom)
                val hour = (pixelsFromZero / mHourHeight).toInt()
                //                int minute = (int) (60 * (pixelsFromZero - hour * mHourHeight) / mHourHeight);
                day.add(Calendar.HOUR, hour + 1)
                day.set(Calendar.MINUTE, 0)
                return day
            }
            startPixel += mWidthPerDay + mColumnGap
        }
        return null
    }

    /**
     * Draw all the events of a particular day.
     *
     * @param date           The day.
     * @param startFromPixel The left position of the day area. The events will never go any left from this value.
     * @param canvas         The canvas to draw upon.
     */
    private fun drawEvents(date: Calendar, startFromPixel: Float, canvas: Canvas) {
        if (mEventRects != null && mEventRects!!.size > 0) {
            for (i in mEventRects!!.indices) {
                if (isSameDay(mEventRects!![i].event.startTime!!, date) && !mEventRects!![i].event.isAllDay) {

                    // Calculate top.
                    val top =
                        mHourHeight.toFloat() * 24f * mEventRects!![i].top / 1440 + mCurrentOrigin.y + mHeaderHeight + (mHeaderRowPadding * 2).toFloat() + mHeaderMarginBottom + mTimeTextHeight / 2 + mEventMarginVertical.toFloat()

                    // Calculate bottom.
                    var bottom = mEventRects!![i].bottom
                    bottom = mHourHeight.toFloat() * 24f * bottom / 1440 + mCurrentOrigin.y + mHeaderHeight +
                            (mHeaderRowPadding * 2).toFloat() + mHeaderMarginBottom + mTimeTextHeight / 2 -
                            mEventMarginVertical

                    // Calculate left and right.
                    //   Log.d(TAG, "drawEvents: " + mEventRects.get(i).width + "yyy" + startFromPixel);
                    //  float left = startFromPixel + mEventRects.get(i).left * mWidthPerDay;
                    //                    if (left < startFromPixel)
                    //                        left += mOverlappingEventGap;
                    // float right = left + mEventRects.get(i).width * mWidthPerDay;
                    val right = startFromPixel + mWidthPerDay
                    //                    if (right < startFromPixel + mWidthPerDay)
                    //                        right -= mOverlappingEventGap;

                    //  Log.d(TAG, "drawEvents: " + left + "YYY" + right);

                    // Draw the event and the event name on top of it.
                    if (startFromPixel < right &&
                        startFromPixel < width &&
                        top < height &&
                        right > mHeaderColumnWidth &&
                        bottom > mHeaderHeight + (mHeaderRowPadding * 2).toFloat() + mTimeTextHeight / 2 + mHeaderMarginBottom
                    ) {
                        mEventRects!![i].rectF = RectF(startFromPixel, top, right, bottom)
                        mEventBackgroundPaint!!.color =
                                if (mEventRects!![i].event.color == 0) mDefaultEventColor else mEventRects!![i].event.color
                        //                          Log.d(TAG, "drawEvents:="+mEventBackgroundPaint.getColor()+"yyy"+mDefaultEventColor);
                        canvas.drawRoundRect(
                            mEventRects!![i].rectF!!,
                            eventCornerRadius.toFloat(),
                            eventCornerRadius.toFloat(),
                            mEventBackgroundPaint!!
                        )
                        drawEventTitle(mEventRects!![i].event, mEventRects!![i].rectF!!, canvas, top, startFromPixel)
                    } else
                        mEventRects!![i].rectF = null
                }
            }
        }
    }

    /**
     * Draw all the Allday-events of a particular day.
     *
     * @param date           The day.
     * @param startFromPixel The left position of the day area. The events will never go any left from this value.
     * @param canvas         The canvas to draw upon.
     */
    private fun drawAllDayEvents(date: Calendar, startFromPixel: Float, canvas: Canvas) {
        if (mEventRects != null && mEventRects!!.size > 0) {
            for (i in mEventRects!!.indices) {
                if (isSameDay(mEventRects!![i].event.startTime!!, date) && mEventRects!![i].event.isAllDay) {

                    // Calculate top.
                    val top =
                        (mHeaderRowPadding * 2).toFloat() + mHeaderMarginBottom + +mTimeTextHeight / 2 + mEventMarginVertical.toFloat()

                    // Calculate bottom.
                    val bottom = top + mEventRects!![i].bottom

                    // Calculate left and right.
                    var left = startFromPixel + mEventRects!![i].left * mWidthPerDay
                    if (left < startFromPixel)
                        left += mOverlappingEventGap.toFloat()
                    var right = left + mEventRects!![i].width * mWidthPerDay
                    if (right < startFromPixel + mWidthPerDay)
                        right -= mOverlappingEventGap.toFloat()

                    // Draw the event and the event name on top of it.
                    if (left < right &&
                        left < width &&
                        top < height &&
                        right > mHeaderColumnWidth &&
                        bottom > 0
                    ) {
                        mEventRects!![i].rectF = RectF(left, top, right, bottom)
                        mEventBackgroundPaint!!.color =
                                if (mEventRects!![i].event.color == 0) mDefaultEventColor else mEventRects!![i].event.color
                        canvas.drawRoundRect(
                            mEventRects!![i].rectF!!,
                            eventCornerRadius.toFloat(),
                            eventCornerRadius.toFloat(),
                            mEventBackgroundPaint!!
                        )
                        drawEventTitle(mEventRects!![i].event, mEventRects!![i].rectF!!, canvas, top, left)
                    } else
                        mEventRects!![i].rectF = null
                }
            }
        }
    }


    /**
     * Draw the name of the event on top of the event rectangle.
     *
     * @param event        The event of which the title (and location) should be drawn.
     * @param rect         The rectangle on which the text is to be drawn.
     * @param canvas       The canvas to draw upon.
     * @param originalTop  The original top position of the rectangle. The rectangle may have some of its portion outside of the visible area.
     * @param originalLeft The original left position of the rectangle. The rectangle may have some of its portion outside of the visible area.
     */
    private fun drawEventTitle(
        event: WeekViewEvent,
        rect: RectF,
        canvas: Canvas,
        originalTop: Float,
        originalLeft: Float
    ) {
        if (rect.right - rect.left - (mEventPadding * 2).toFloat() < 0) return
        if (rect.bottom - rect.top - (mEventPadding * 2).toFloat() < 0) return

        // Prepare the name of the event.
        val bob = SpannableStringBuilder()
        if (event.name != null) {
            bob.append(event.name)
            bob.setSpan(StyleSpan(Typeface.BOLD), 0, bob.length, 0)
            bob.append(' ')
        }

        // Prepare the location of the event.
        if (event.location != null) {
            bob.append(event.location)
        }

        val availableHeight = (rect.bottom - originalTop - (mEventPadding * 2).toFloat()).toInt()
        val availableWidth = (rect.right - originalLeft - (mEventPadding * 2).toFloat()).toInt()

        // Get text dimensions.
        var textLayout =
            StaticLayout(bob, mEventTextPaint, availableWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false)

        val lineHeight = textLayout.height / textLayout.lineCount

        if (availableHeight >= lineHeight) {
            // Calculate available number of line counts.
            var availableLineCount = availableHeight / lineHeight
            do {
                // Ellipsize text to fit into event rect.
                textLayout = StaticLayout(
                    TextUtils.ellipsize(
                        bob,
                        mEventTextPaint,
                        (availableLineCount * availableWidth).toFloat(),
                        TextUtils.TruncateAt.END
                    ),
                    mEventTextPaint,
                    (rect.right - originalLeft - (mEventPadding * 2).toFloat()).toInt(),
                    Layout.Alignment.ALIGN_NORMAL,
                    1.0f,
                    0.0f,
                    false
                )

                // Reduce line count.
                availableLineCount--

                // Repeat until text is short enough.
            } while (textLayout.height > availableHeight)

            // Draw text.
            canvas.save()
            canvas.translate(originalLeft + mEventPadding, originalTop + mEventPadding)
            textLayout.draw(canvas)
            canvas.restore()
        }
    }


    /**
     * A class to hold reference to the events and their visual representation. An EventRect is
     * actually the rectangle that is drawn on the calendar for a given event. There may be more
     * than one rectangle for a single event (an event that expands more than one day). In that
     * case two instances of the EventRect will be used for a single event. The given event will be
     * stored in "originalEvent". But the event that corresponds to rectangle the rectangle
     * instance will be stored in "event".
     */
    private inner class EventRect
    /**
     * Create a new instance of event rect. An EventRect is actually the rectangle that is drawn
     * on the calendar for a given event. There may be more than one rectangle for a single
     * event (an event that expands more than one day). In that case two instances of the
     * EventRect will be used for a single event. The given event will be stored in
     * "originalEvent". But the event that corresponds to rectangle the rectangle instance will
     * be stored in "event".
     *
     * @param event         Represents the event which this instance of rectangle represents.
     * @param originalEvent The original event that was passed by the user.
     * @param rectF         The rectangle.
     */
        (var event: WeekViewEvent, var originalEvent: WeekViewEvent, var rectF: RectF?) {
        var left: Float = 0.toFloat()
        var width: Float = 0.toFloat()
        var top: Float = 0.toFloat()
        var bottom: Float = 0.toFloat()
    }


    /**
     * Gets more events of one/more month(s) if necessary. This method is called when the user is
     * scrolling the week view. The week view stores the events of three months: the visible month,
     * the previous month, the next month.
     *
     * @param day The day where the user is currently is.
     */
    private fun getMoreEvents(day: Calendar) {

        // Get more events if the month is changed.
        if (mEventRects == null)
            mEventRects = ArrayList()
        if (weekViewLoader == null && !isInEditMode)
            throw IllegalStateException("You must provide a MonthChangeListener")

        // If a refresh was requested then reset some variables.
        if (mRefreshEvents) {
            mEventRects!!.clear()
            mPreviousPeriodEvents = null
            mCurrentPeriodEvents = null
            mNextPeriodEvents = null
            mFetchedPeriod = -1
        }

        if (weekViewLoader != null) {
            val periodToFetch = weekViewLoader!!.toWeekViewPeriodIndex(day).toInt()
            if (!isInEditMode && (mFetchedPeriod < 0 || mFetchedPeriod != periodToFetch || mRefreshEvents)) {
                var previousPeriodEvents: List<WeekViewEvent>? = null
                var currentPeriodEvents: List<WeekViewEvent>? = null
                var nextPeriodEvents: List<WeekViewEvent>? = null

                if (mPreviousPeriodEvents != null && mCurrentPeriodEvents != null && mNextPeriodEvents != null) {
                    if (periodToFetch == mFetchedPeriod - 1) {
                        currentPeriodEvents = mPreviousPeriodEvents
                        nextPeriodEvents = mCurrentPeriodEvents
                    } else if (periodToFetch == mFetchedPeriod) {
                        previousPeriodEvents = mPreviousPeriodEvents
                        currentPeriodEvents = mCurrentPeriodEvents
                        nextPeriodEvents = mNextPeriodEvents
                    } else if (periodToFetch == mFetchedPeriod + 1) {
                        previousPeriodEvents = mCurrentPeriodEvents
                        currentPeriodEvents = mNextPeriodEvents
                    }
                }
                if (currentPeriodEvents == null)
                    currentPeriodEvents = weekViewLoader!!.onLoad(periodToFetch)
                if (previousPeriodEvents == null)
                    previousPeriodEvents = weekViewLoader!!.onLoad(periodToFetch - 1)
                if (nextPeriodEvents == null)
                    nextPeriodEvents = weekViewLoader!!.onLoad(periodToFetch + 1)


                // Clear events.
                mEventRects!!.clear()
                sortAndCacheEvents(previousPeriodEvents)
                sortAndCacheEvents(currentPeriodEvents)
                sortAndCacheEvents(nextPeriodEvents)
                calculateHeaderHeight()

                mPreviousPeriodEvents = previousPeriodEvents
                mCurrentPeriodEvents = currentPeriodEvents
                mNextPeriodEvents = nextPeriodEvents
                mFetchedPeriod = periodToFetch
            }
        }

        // Prepare to calculate positions of each events.
        val tempEvents = mEventRects
        mEventRects = ArrayList()

        // Iterate through each day with events to calculate the position of the events.
        while (tempEvents!!.size > 0) {
            val eventRects = ArrayList<EventRect>(tempEvents.size)

            // Get first event for a day.
            val eventRect1 = tempEvents.removeAt(0)
            eventRects.add(eventRect1)

            var i = 0
            while (i < tempEvents.size) {
                // Collect all other events for same day.
                val eventRect2 = tempEvents[i]
                if (isSameDay(eventRect1.event.startTime!!, eventRect2.event.startTime!!)) {
                    tempEvents.removeAt(i)
                    eventRects.add(eventRect2)
                } else {
                    i++
                }
            }
            computePositionOfEvents(eventRects)
        }
    }

    /**
     * Cache the event for smooth scrolling functionality.
     *
     * @param event The event to cache.
     */
    private fun cacheEvent(event: WeekViewEvent) {
        if (event.startTime!!.compareTo(event.endTime) >= 0)
            return
        val splitedEvents = event.splitWeekViewEvents()
        for (splitedEvent in splitedEvents) {
            //            Log.d(TAG, "cacheEvent: "+splitedEvent.);
            mEventRects!!.add(EventRect(splitedEvent, event, null))
        }
    }

    /**
     * Sort and cache events.
     *
     * @param events The events to be sorted and cached.
     */
    private fun sortAndCacheEvents(events: List<WeekViewEvent>) {
        sortEvents(events)
        for (event in events) {
            cacheEvent(event)
        }
    }

    /**
     * Sorts the events in ascending order.
     *
     * @param events The events to be sorted.
     */
    private fun sortEvents(events: List<WeekViewEvent>) {
        Collections.sort(events) { event1, event2 ->
            val start1 = event1.startTime!!.timeInMillis
            val start2 = event2.startTime!!.timeInMillis
            var comparator = if (start1 > start2) 1 else if (start1 < start2) -1 else 0
            if (comparator == 0) {
                val end1 = event1.endTime!!.timeInMillis
                val end2 = event2.endTime!!.timeInMillis
                comparator = if (end1 > end2) 1 else if (end1 < end2) -1 else 0
            }
            comparator
        }
    }

    /**
     * Calculates the left and right positions of each events. This comes handy specially if events
     * are overlapping.
     *
     * @param eventRects The events along with their wrapper class.
     */
    private fun computePositionOfEvents(eventRects: List<EventRect>) {
        // Make "collision groups" for all events that collide with others.
        var collisionGroups = ArrayList<ArrayList<EventRect>>()
        for (eventRect in eventRects) {
            var isPlaced = false

            outerLoop@ for (collisionGroup in collisionGroups) {
                for (groupEvent in collisionGroup) {
                    if (isEventsCollide(
                            groupEvent.event,
                            eventRect.event
                        ) && groupEvent.event.isAllDay == eventRect.event.isAllDay
                    ) {
                        collisionGroup.add(eventRect)
                        isPlaced = true
                        break@outerLoop
                    }
                }
            }

            if (!isPlaced) {
                val newGroup = ArrayList<EventRect>()
                newGroup.add(eventRect)
                collisionGroups.add(newGroup)
            }
        }

        for (collisionGroup in collisionGroups) {
            expandEventsToMaxWidth(collisionGroup)
        }
    }

    /**
     * Expands all the events to maximum possible width. The events will try to occupy maximum
     * space available horizontally.
     *
     * @param collisionGroup The group of events which overlap with each other.
     */
    private fun expandEventsToMaxWidth(collisionGroup: List<EventRect>) {
        // Expand the events to maximum possible width.
        val columns = mutableListOf<ArrayList<EventRect>>()

        for (eventRect in collisionGroup) {

            var isPlaced = false

            columns.forEach { collisionGroup ->
                collisionGroup.forEach { groupEvent ->
                    if (isEventsCollide(
                            groupEvent.event,
                            eventRect.event
                        ) && groupEvent.event.isAllDay == eventRect.event.isAllDay
                    ) {

                    }

                }
            }

//            for (column in columns) {
//                if (column.size == 0) {
//                    column.add(eventRect)
//                    isPlaced = true
//                } else if (!isEventsCollide(eventRect.event, column[column.size - 1].event)) {
//                    column.add(eventRect)
//                    isPlaced = true
//                    break
//                }
//            }
            if (!isPlaced) {
                val newColumn = ArrayList<EventRect>()
                newColumn.add(eventRect)
                columns.add(newColumn)
            }
        }


        // Calculate left and right position for all the events.
        // Get the maxRowCount by looking in all columns.
        var maxRowCount = 0
        for (column in columns) {
            maxRowCount = Math.max(maxRowCount, column.size)
        }
        for (i in 0 until maxRowCount) {
            // Set the left and right values of the event.
            var j = 0f
            for (column in columns) {
                if (column.size >= i + 1) {
                    val eventRect = column[i]
                    eventRect.width = 1f / columns.size
                    eventRect.left = j / columns.size
                    if (!eventRect.event.isAllDay) {
                        eventRect.top =
                                (eventRect.event.startTime!!.get(Calendar.HOUR_OF_DAY) * 60 + eventRect.event.startTime!!.get(
                                    Calendar.MINUTE
                                )).toFloat()
                        eventRect.bottom =
                                (eventRect.event.endTime!!.get(Calendar.HOUR_OF_DAY) * 60 + eventRect.event.endTime!!.get(
                                    Calendar.MINUTE
                                )).toFloat()
                        //   Log.d(TAG, "expandEventsToMaxWidth: "+eventRect.top+"YYY"+eventRect.bottom);
                        eventRect.bottom = eventRect.bottom - 1
                        eventRect.top = eventRect.top + 1

                    } else {
                        eventRect.top = 0f
                        eventRect.bottom = allDayEventHeight.toFloat()
                    }
                    mEventRects!!.add(eventRect)
                }
                j++
            }
        }
    }


    /**
     * Checks if two events overlap.
     *
     * @param event1 The first event.
     * @param event2 The second event.
     * @return true if the events overlap.
     */
    private fun isEventsCollide(event1: WeekViewEvent, event2: WeekViewEvent): Boolean {
        val start1 = event1.startTime!!.timeInMillis
        val end1 = event1.endTime!!.timeInMillis
        val start2 = event2.startTime!!.timeInMillis
        val end2 = event2.endTime!!.timeInMillis
        return !(start1 >= end2 || end1 <= start2)
    }


    /**
     * Checks if time1 occurs after (or at the same time) time2.
     *
     * @param time1 The time to check.
     * @param time2 The time to check against.
     * @return true if time1 and time2 are equal or if time1 is after time2. Otherwise false.
     */
    private fun isTimeAfterOrEquals(time1: Calendar?, time2: Calendar?): Boolean {
        return !(time1 == null || time2 == null) && time1.timeInMillis >= time2.timeInMillis
    }

    override fun invalidate() {
        super.invalidate()
        mAreDimensionsInvalid = true
    }

    /////////////////////////////////////////////////////////////////
    //
    //      Functions related to setting and getting the properties.
    //
    /////////////////////////////////////////////////////////////////

    fun setOnEventClickListener(listener: EventClickListener) {
        this.eventClickListener = listener
    }


    fun getmInActiveSlotBackgroundColor(): Int {
        return mInActiveSlotBackgroundColor
    }

    fun setmInActiveSlotBackgroundColor(mInActiveSlotBackgroundColor: Int) {
        this.mInActiveSlotBackgroundColor = mInActiveSlotBackgroundColor
        mInActiveSlotBackgroundPaint!!.color = mInActiveSlotBackgroundColor
        invalidate()

    }

    /////////////////////////////////////////////////////////////////
    //
    //      Functions related to scrolling.
    //
    /////////////////////////////////////////////////////////////////

    override fun onTouchEvent(event: MotionEvent): Boolean {
        mScaleDetector!!.onTouchEvent(event)
        val `val` = mGestureDetector!!.onTouchEvent(event)

        // Check after call of mGestureDetector, so mCurrentFlingDirection and mCurrentScrollDirection are set.
        if (event.action == MotionEvent.ACTION_UP && !mIsZooming && mCurrentFlingDirection == Direction.NONE) {
            if (mCurrentScrollDirection == Direction.RIGHT || mCurrentScrollDirection == Direction.LEFT) {
                goToNearestOrigin()
            }
            mCurrentScrollDirection = Direction.NONE
        }

        return `val`
    }

    private fun goToNearestOrigin() {
        var leftDays = (mCurrentOrigin.x / (mWidthPerDay + mColumnGap)).toDouble()

        if (mCurrentFlingDirection != Direction.NONE) {
            // snap to nearest day
            leftDays = Math.round(leftDays).toDouble()
        } else if (mCurrentScrollDirection == Direction.LEFT) {
            // snap to last day
            leftDays = Math.floor(leftDays)
        } else if (mCurrentScrollDirection == Direction.RIGHT) {
            // snap to next day
            leftDays = Math.ceil(leftDays)
        } else {
            // snap to nearest day
            leftDays = Math.round(leftDays).toDouble()
        }

        val nearestOrigin = (mCurrentOrigin.x - leftDays * (mWidthPerDay + mColumnGap)).toInt()

        if (nearestOrigin != 0) {
            // Stop current animation.
            mScroller!!.forceFinished(true)
            // Snap to date.
            mScroller!!.startScroll(
                mCurrentOrigin.x.toInt(),
                mCurrentOrigin.y.toInt(),
                -nearestOrigin,
                0,
                (Math.abs(nearestOrigin) / mWidthPerDay * scrollDuration).toInt()
            )
            ViewCompat.postInvalidateOnAnimation(this@WeekView)
        }
        // Reset scrolling and fling direction.
        mCurrentFlingDirection = Direction.NONE
        mCurrentScrollDirection = mCurrentFlingDirection
    }


    override fun computeScroll() {
        super.computeScroll()

        if (mScroller!!.isFinished) {
            if (mCurrentFlingDirection != Direction.NONE) {
                // Snap to day after fling is finished.
                goToNearestOrigin()
            }
        } else {
            if (mCurrentFlingDirection != Direction.NONE && forceFinishScroll()) {
                goToNearestOrigin()
            } else if (mScroller!!.computeScrollOffset()) {
                mCurrentOrigin.y = mScroller!!.currY.toFloat()
                mCurrentOrigin.x = mScroller!!.currX.toFloat()
                ViewCompat.postInvalidateOnAnimation(this)
            }
        }
    }

    /**
     * Check if scrolling should be stopped.
     *
     * @return true if scrolling should be stopped before reaching the end of animation.
     */
    private fun forceFinishScroll(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            // current velocity only available since api 14
            mScroller!!.currVelocity <= mMinimumFlingVelocity
        } else {
            false
        }
    }


    /////////////////////////////////////////////////////////////////
    //
    //      Public methods.
    //
    /////////////////////////////////////////////////////////////////

    /**
     * Show today on the week view.
     */
    fun goToToday() {
        val today = Calendar.getInstance()
        goToDate(today)
    }

    /**
     * Show a specific day on the week view.
     *
     * @param date The date to show.
     */
    fun goToDate(date: Calendar) {
        mScroller!!.forceFinished(true)
        mCurrentFlingDirection = Direction.NONE
        mCurrentScrollDirection = mCurrentFlingDirection

        date.set(Calendar.HOUR_OF_DAY, 0)
        date.set(Calendar.MINUTE, 0)
        date.set(Calendar.SECOND, 0)
        date.set(Calendar.MILLISECOND, 0)

        if (mAreDimensionsInvalid) {
            mScrollToDay = date
            return
        }

        mRefreshEvents = true

        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)

        val day = 1000L * 60L * 60L * 24L
        val dateInMillis = date.timeInMillis + date.timeZone.getOffset(date.timeInMillis)
        val todayInMillis = today.timeInMillis + today.timeZone.getOffset(today.timeInMillis)
        val dateDifference = dateInMillis / day - todayInMillis / day
        mCurrentOrigin.x = -dateDifference * (mWidthPerDay + mColumnGap)
        invalidate()
    }

    /**
     * Refreshes the view and loads the events again.
     */
    fun notifyDatasetChanged() {
        mRefreshEvents = true
        invalidate()
    }

    /**
     * Vertically scroll to a specific hour in the week view.
     *
     * @param hour The hour to scroll to in 24-hour format. Supported values are 0-24.
     */
    fun goToHour(hour: Double) {

        // Calendar now = Calendar.getInstance();
        // hour = (now.get(Calendar.HOUR_OF_DAY));
        if (mAreDimensionsInvalid) {
            mScrollToHour = hour
            return
        }
        // mRefreshEvents = true;

        var verticalOffset = 0
        if (hour > 24)
            verticalOffset = mHourHeight * 24
        else if (hour > 0)
            verticalOffset = (mHourHeight * hour).toInt()

        if (verticalOffset > (mHourHeight * 24 - height).toFloat() + mHeaderHeight + (mHeaderRowPadding * 2).toFloat() + mHeaderMarginBottom)
            verticalOffset =
                    ((mHourHeight * 24 - height).toFloat() + mHeaderHeight + (mHeaderRowPadding * 2).toFloat() + mHeaderMarginBottom).toInt()

        mCurrentOrigin.y = (-verticalOffset).toFloat()
        invalidate()
    }


    /////////////////////////////////////////////////////////////////
    //
    //      Interfaces.
    //
    /////////////////////////////////////////////////////////////////

    interface EventClickListener {
        /**
         * Triggered when clicked on one existing event
         *
         * @param event:     event clicked.
         * @param eventRect: view containing the clicked event.
         */
        fun onEventClick(event: WeekViewEvent, eventRect: RectF?)
    }

    interface EventLongPressListener {
        /**
         * Similar to [WeekView.EventClickListener] but with a long press.
         *
         * @param event:     event clicked.
         * @param eventRect: view containing the clicked event.
         */
        fun onEventLongPress(event: WeekViewEvent, eventRect: RectF?)
    }

    interface EmptyViewClickListener {
        fun onEmptyViewClicked(selectedTime: Calendar?, endTimeFromPoint: Calendar?, b: Boolean)
        /**
         * Triggered when the users clicks on a empty space of the calendar.
         *
         * @param time: [Calendar] object set with the date and time of the clicked position on the view.
         */
        //void onEmptyViewClicked(Calendar time);
    }

    interface EmptyViewLongPressListener {
        /**
         * Similar to [WeekView.EmptyViewClickListener] but with long press.
         *
         * @param time: [Calendar] object set with the date and time of the long pressed position on the view.
         */
        fun onEmptyViewLongPress(startTime: Calendar?, endTime: Calendar?)
    }

    interface ScrollListener {
        /**
         * Called when the first visible day has changed.
         *
         *
         * (this will also be called during the first draw of the weekview)
         *
         * @param newFirstVisibleDay The new first visible day
         * @param oldFirstVisibleDay The old first visible day (is null on the first call).
         */
        fun onFirstVisibleDayChanged(newFirstVisibleDay: Calendar?, oldFirstVisibleDay: Calendar?)
    }

    companion object {

        val TAG = "WeekView"

        @Deprecated("")
        val LENGTH_SHORT = 1
        @Deprecated("")
        val LENGTH_LONG = 2
    }
}
