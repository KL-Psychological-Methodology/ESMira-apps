package at.jodlidev.esmira

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.text.format.DateFormat
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.app.ActivityCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.util.*


/**
 * Created by JodliDev on 07.11.17.
 */
open class Element_DateTimeInput : LinearLayout {
	private lateinit var dateInfo: MaterialButton
	private lateinit var timeInfo: MaterialButton
	private var calendar: Calendar? = null
	
	enum class MODES {
		DateTime,
		Date,
		Time
	}
	
	interface OnChangeListener {
		fun onChanged(date: Calendar)
	}
	
	private var listener: OnChangeListener? = null
	
	constructor(context: Context) : super(context) {
		init(context, MODES.DateTime)
	}
	constructor(context: Context, attrs: AttributeSet?) : super(context, attrs, 0) {
		val theme = context.theme.obtainStyledAttributes(attrs, R.styleable.Element_DateTimeInput, 0, 0)
		val modeInt: Int
		try {
			modeInt = theme.getInt(R.styleable.Element_DateTimeInput_dateTimeMode, MODES.DateTime.ordinal)
		}
		finally {
			theme.recycle()
		}
		
		init(context, MODES.values()[modeInt])
	}
	constructor(context: Context, mode: MODES) : super(context) {
		init(context, mode)
	}
	
	private fun init(context: Context, mode: MODES) {
		when(mode) {
			MODES.DateTime -> {
				View.inflate(context, R.layout.element_date_time_input, this)
				initDate(context)
				initTime(context)
			}
			MODES.Date -> {
				View.inflate(context, R.layout.element_date_input, this)
				initDate(context)
			}
			MODES.Time -> {
				View.inflate(context, R.layout.element_time_input, this)
				initTime(context)
			}
		}
		calendar = Calendar.getInstance()
		showDate()
		showTime()
	}
	
	
	private fun initDate(context: Context) {
		dateInfo = findViewById(R.id.dateInfo)
		
		dateInfo.setOnClickListener {
			val cal = calendar ?: Calendar.getInstance()
			
			
			val dateDialog = MaterialDatePicker.Builder.datePicker()
			dateDialog.setSelection(cal.timeInMillis)
			val builder = dateDialog.build()
			builder.addOnPositiveButtonClickListener {timestamp ->
				val newCal = calendar ?: Calendar.getInstance()
				newCal.timeInMillis = timestamp
				calendar = newCal
				showDate()
				listener?.onChanged(newCal)
			}
			
			builder.show((context as AppCompatActivity).supportFragmentManager, "data_dialog")
			
			
			
//			val dateDialog = DatePickerDialog(context, OnDateSetListener { _, year, month, day ->
//				val newCal = calendar ?: Calendar.getInstance()
//				newCal[year, month] = day
//				calendar = newCal
//				showDate()
//				listener?.onChanged(newCal)
//			}, cal[Calendar.YEAR], cal[Calendar.MONTH], cal[Calendar.DAY_OF_MONTH])
//			dateDialog.show()
		}
	}
	
	private fun getCurrentTimeCalendar(): Calendar {
		val cal = Calendar.getInstance()
		cal[Calendar.HOUR_OF_DAY] = 0
		cal[Calendar.MINUTE] = 0
		
		return cal
	}
	
	private fun initTime(context: Context) {
		timeInfo = findViewById(R.id.timeInfo)
		timeInfo.setOnClickListener {
			val cal = calendar ?: getCurrentTimeCalendar()
			//TODO: update MaterialTimePicker to 1.3.1
//			MaterialTimePicker.Builder()
//				.setTimeFormat(if(DateFormat.is24HourFormat(getContext())) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H)
//				.setHour(cal[Calendar.HOUR_OF_DAY])
//				.setMinute(cal[Calendar.MINUTE])
//				.build()
//				.apply {
//					addOnPositiveButtonClickListener {
//						val newCal = calendar ?: getCurrentTimeCalendar()
//
//						newCal[Calendar.HOUR_OF_DAY] = hour
//						newCal[Calendar.MINUTE] = minute
//						calendar = newCal
//						showTime()
//						listener?.onChanged(newCal)
//					}
//				}
//				.show((context as AppCompatActivity).supportFragmentManager, "time_dialog")

			val timeDialog = MaterialTimePicker.newInstance()
			timeDialog.setTimeFormat(if(DateFormat.is24HourFormat(getContext())) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H)
			timeDialog.hour = cal[Calendar.HOUR_OF_DAY]
			timeDialog.minute = cal[Calendar.MINUTE]
			timeDialog.setListener { dialog ->
				val newCal = calendar ?: getCurrentTimeCalendar()

				newCal[Calendar.HOUR_OF_DAY] = dialog.hour
				newCal[Calendar.MINUTE] = dialog.minute
				calendar = newCal
				showTime()
				listener?.onChanged(newCal)
			}
			timeDialog.show((context as AppCompatActivity).supportFragmentManager, "time_dialog")
			
			
//			val timeDialog = TimePickerDialog(context, OnTimeSetListener { _, hour, min ->
//				val newCal = calendar ?: getCurrentTimeCalendar()
//
//				newCal[Calendar.HOUR_OF_DAY] = hour
//				newCal[Calendar.MINUTE] = min
//				calendar = newCal
//				showTime()
//				listener?.onChanged(newCal)
//			}, cal[Calendar.HOUR_OF_DAY], cal[Calendar.MINUTE], DateFormat.is24HourFormat(getContext()))
//			timeDialog.show()
		}
	}
	
	fun setListener(listener: OnChangeListener?) {
		this.listener = listener
	}
	
	fun setTimestamp(timestamp: Long?) {
		if(timestamp != null && timestamp != 0L) {
			val cal = Calendar.getInstance()
			cal.timeInMillis = timestamp
			calendar = cal
		}
		else
			calendar = null
		showDate()
		showTime()
	}
	
	private fun showTime() {
		if(this::timeInfo.isInitialized) {
			val c = calendar
			if(c == null)
				timeInfo.text = context.getString(R.string.no_dateTime_data)
			else
				timeInfo.text = returnTimeString(context, c)
		}
	}

	private fun showDate() {
		if(this::dateInfo.isInitialized) {
			val c = calendar
			if(c == null)
				dateInfo.text = context.getString(R.string.no_dateTime_data)
			else
				dateInfo.text = returnDateString(context, c)
		}
	}
	
	fun getTimestamp(): Long {
		return calendar?.timeInMillis ?: 0
	}
	
	public override fun onSaveInstanceState(): Parcelable {
		val s = SavedState(super.onSaveInstanceState())
		val c = calendar
		if(c != null)
			s.state = c.timeInMillis
		return s
	}
	
	public override fun onRestoreInstanceState(state: Parcelable) {
		val s = state as SavedState
		super.onRestoreInstanceState(s.superState)
		val c = Calendar.getInstance()
		if(s.state != 0L)
			c.timeInMillis = s.state
		calendar = c
	}
	
	private class SavedState : BaseSavedState {
		var state: Long = 0
		
		constructor(parcel: Parcel) : super(parcel) {
			state = parcel.readLong()
		}
		
		constructor (parcelable: Parcelable?) : super(parcelable)
		
		override fun writeToParcel(parcel: Parcel, flags: Int) {
			super.writeToParcel(parcel, flags)
			parcel.writeLong(state)
		}
		
		companion object CREATOR : Parcelable.Creator<SavedState> {
			override fun createFromParcel(parcel: Parcel): SavedState {
				return SavedState(parcel)
			}
			
			override fun newArray(size: Int): Array<SavedState?> {
				return arrayOfNulls(size)
			}
		}
	}
	
	
	companion object {
		fun returnDateString(context: Context, cal: Calendar): String {
			val format = DateFormat.getDateFormat(context)
			return format.format(cal.time)
		}
		
		fun returnTimeString(context: Context, cal: Calendar): String {
			val format = DateFormat.getTimeFormat(context)
			return format.format(cal.time)
		}
	}
}