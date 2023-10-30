package com.abhilash.apps.composedatepicker

import android.icu.text.DateFormat
import android.icu.text.DisplayContext
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.view.View
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.text.layoutDirection
import com.abhilash.apps.composedatepicker.data.YearMonth
import com.abhilash.apps.composedatepicker.data.on
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.DayOfWeek
import java.util.Calendar
import java.util.Locale

@Stable
enum class DatePickerMode {
    YEAR,
    MONTH
}

@Stable
interface DatePickerListener {
    fun onDateSelected(date: Calendar)

    fun oKButtonClicked()

    fun cancelButtonClicked()
}

data class DatePickerProperty(
    val startYear: Int,
    val endYear: Int
)

data class DatePickerColor(
    val themeColor: Color,
    val backgroundColor: Color,
    val datePickerHeaderColor: Color = themeColor,
    val monthHeaderIconTintColor: Color,
    val selectedDayBgColor: Color = themeColor,
    val currentDayBgColor: Color = themeColor,
    val yearPickerSeparatorColor: Color,
)

data class DatePickerTextStyle(
    val datePickerSelectedTextStyle: TextStyle,
    val datePickerUnselectedTextStyle: TextStyle,
    val monthHeaderTextStyle: TextStyle,
    val weekDayTextStyle: TextStyle,
    val unSelectedDayTextStyle: TextStyle,
    val selectedDayTextStyle: TextStyle,
    val currentDayTextStyle: TextStyle,
    val buttonTextStyle: TextStyle,
    val yearPickerSelectedTextStyle: TextStyle,
    val yearPickerUnselectedTextStyle: TextStyle
)


@Composable
fun DatePicker(
    locale: Locale,
    property: DatePickerProperty = DatePickerProperty(
        startYear = 1900,
        endYear = 2100
    ),
    datePickerColor: DatePickerColor,
    dateTextStyle: DatePickerTextStyle,
    listener: DatePickerListener
) {
    val layoutDirection = when(locale.layoutDirection) {
        View.LAYOUT_DIRECTION_RTL -> LayoutDirection.Rtl
        else -> LayoutDirection.Ltr
    }

    Dialog(
        onDismissRequest = {
            listener.cancelButtonClicked()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
        )
    ) {
        CompositionLocalProvider(
            LocalLayoutDirection provides layoutDirection
        ) {
            DatePickerContent(
                locale = locale,
                listener = listener,
                property = property,
                dateTextStyle = dateTextStyle,
                datePickerColor = datePickerColor
            )
        }
    }
}

@Composable
private fun DatePickerContent(
    locale: Locale,
    selectedDate: Calendar = Calendar.getInstance(locale),
    property: DatePickerProperty,
    datePickerColor: DatePickerColor,
    dateTextStyle: DatePickerTextStyle,
    listener: DatePickerListener
) {
    val currentDateState = remember {
        mutableStateOf(DatePickerMode.MONTH)
    }

    val currentSelectedDate = remember {
        mutableStateOf(selectedDate)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(32.dp)
            .shadow(16.dp, RoundedCornerShape(16.dp))
            .background(datePickerColor.backgroundColor)
    ) {
        DatePickerHeader(
            currentSelectedDate = currentSelectedDate.value,
            currentMode = currentDateState.value,
            dateTextStyle = dateTextStyle,
            datePickerColor = datePickerColor,
            locale = locale
        ) {
            currentDateState.value = it
        }

        DateAndYearPicker(
            currentSelectedDate = currentSelectedDate.value,
            currentDateState = currentDateState.value,
            locale = locale,
            property = property,
            dateTextStyle = dateTextStyle,
            datePickerColor = datePickerColor,
            listener = listener,
            setDate = {
                currentSelectedDate.value = it
            },
            setDateMode = {
                currentDateState.value = it
            }
        )

        DialogButton(
            locale = locale,
            listener = listener,
            dateTextStyle = dateTextStyle
        )
    }
}

@Composable
private fun DateAndYearPicker(
    currentSelectedDate: Calendar,
    currentDateState: DatePickerMode,
    locale: Locale,
    property: DatePickerProperty,
    datePickerColor: DatePickerColor,
    dateTextStyle: DatePickerTextStyle,
    listener: DatePickerListener,
    setDate: (Calendar) -> Unit,
    setDateMode: (DatePickerMode) -> Unit,

) {
    val monthPickerHeight = remember {
        mutableStateOf(Dp.Hairline)
    }
    val density = LocalDensity.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.onGloballyPositioned {
                monthPickerHeight.value  = with(density) { it.size.height.toDp() }
            }
        ) {
            MonthPicker(
                currentSelectedDate = currentSelectedDate,
                locale = locale,
                property = property,
                datePickerColor = datePickerColor,
                dateTextStyle = dateTextStyle,
                setCurrentDate = {
                    setDate(it)
                    listener.onDateSelected(it)
                }
            )
        }

        if(currentDateState == DatePickerMode.YEAR) {
            Column(
                modifier = Modifier
                    .height(monthPickerHeight.value)
            ) {
                YearPicker(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(datePickerColor.backgroundColor),
                    selectedDate = currentSelectedDate,
                    startYear = property.startYear,
                    datePickerTextStyle = dateTextStyle,
                    endYear = property.endYear,
                    offset = with(density) { -monthPickerHeight.value.toPx().toInt() / 4 }
                ) { clickedYear ->
                    setDateMode(DatePickerMode.MONTH)

                    val newDate = Calendar.getInstance(locale).apply {
                        set(Calendar.DAY_OF_MONTH, currentSelectedDate.get(Calendar.DAY_OF_MONTH))
                        set(Calendar.MONTH, currentSelectedDate.get(Calendar.MONTH))
                        set(Calendar.YEAR, clickedYear)
                    }
                    listener.onDateSelected(newDate)
                    setDate(newDate)
                }

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(datePickerColor.yearPickerSeparatorColor)
                )
            }
        }
    }
}

@Composable
private fun YearPicker(
    modifier: Modifier,
    startYear: Int,
    endYear: Int,
    offset: Int,
    datePickerTextStyle: DatePickerTextStyle,
    selectedDate: Calendar,
    onYearClick: (year: Int) -> Unit
) {
    val currentYear = selectedDate.get(Calendar.YEAR)
    val startPosition = currentYear - startYear
    val lazyState = rememberLazyListState(
        initialFirstVisibleItemIndex = startPosition - 1,
        initialFirstVisibleItemScrollOffset = offset
    )

    LazyColumn(
        modifier = modifier,
        state = lazyState
    ) {
        items(endYear - startYear) { index ->
            val position = index + 1
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        indication = rememberRipple(),
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        onYearClick(startYear + position)
                    }
                    .padding(vertical = 16.dp),
                text = "${startYear + position}",
                textAlign = TextAlign.Center,
                style = when(position) {
                    startPosition -> datePickerTextStyle.yearPickerSelectedTextStyle
                    else -> datePickerTextStyle.yearPickerUnselectedTextStyle
                }
            )
        }
    }
}


@Composable
private fun DialogButton(
    locale: Locale,
    listener: DatePickerListener,
    dateTextStyle: DatePickerTextStyle,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.End
    ) {
        val context = LocalContext.current
        val newContext = remember(locale) {
            val res = context.resources
            val conf = res.configuration
            conf.setLocale(locale)
            context.createConfigurationContext(conf)
        }
        val okText = remember(newContext) {
            newContext.resources.getString(android.R.string.ok)
        }
        val cancelText = remember(newContext) {
            newContext.resources.getString(android.R.string.cancel)
        }

        Text(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .clickable(
                    indication = rememberRipple(),
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    listener.cancelButtonClicked()
                }
                .padding(8.dp),
            text = cancelText,
            style = dateTextStyle.buttonTextStyle
        )

        Spacer(modifier = Modifier.width(32.dp))

        Text(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .clickable(
                    indication = rememberRipple(),
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    listener.cancelButtonClicked()
                }
                .padding(8.dp),
            text = okText,
            style = dateTextStyle.buttonTextStyle
        )
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MonthPicker(
    currentSelectedDate: Calendar,
    property: DatePickerProperty,
    datePickerColor: DatePickerColor,
    dateTextStyle: DatePickerTextStyle,
    locale: Locale,
    setCurrentDate: (Calendar) -> Unit
) {
    val startYear = property.startYear
    val endYear = property.endYear

    val startIndex = remember(currentSelectedDate) {
        val year = currentSelectedDate.get(Calendar.YEAR)
        val month = currentSelectedDate.get(Calendar.MONTH)

        ((year - startYear) * 12) + month
    }

    val pagerState = rememberPagerState(
        initialPage = startIndex
    )

    LaunchedEffect(key1 = currentSelectedDate) {
        if(startIndex != pagerState.currentPage) {
            pagerState.animateScrollToPage(startIndex)
        }
    }

    val scope = rememberCoroutineScope()

    HorizontalPager(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 16.dp),
        pageCount = (endYear - startYear + 1) * 12,
        state = pagerState,
        beyondBoundsPageCount = 3
    ) { page ->
        val yearMonth = remember {
            val month = (page % 12) + 1
            val year = startYear + (page / 12)
            month on year
        }

        Month(
            yearMonth = yearMonth,
            locale = locale,
            selectedDate = currentSelectedDate,
            dateTextStyle = dateTextStyle,
            datePickerColor = datePickerColor,
            listener = {
                setCurrentDate(it)
            },
            moveToPreviousMonth = {
                scope.launch {
                    pagerState.animateScrollToPage(
                        page = pagerState.currentPage - 1,
                        animationSpec = tween(durationMillis = 300, easing = LinearEasing)
                    )
                }
            },
            moveToNextMonth = {
                scope.launch {
                    pagerState.animateScrollToPage(
                        page = pagerState.currentPage + 1,
                        animationSpec = tween(durationMillis = 300, easing = LinearEasing)
                    )
                }
            }
        )
    }

}


@Composable
private fun DatePickerHeader(
    currentSelectedDate: Calendar,
    currentMode: DatePickerMode,
    datePickerColor: DatePickerColor,
    dateTextStyle: DatePickerTextStyle,
    locale: Locale,
    setMode: (currentMode: DatePickerMode) -> Unit
) {
    val year = currentSelectedDate.get(Calendar.YEAR).getFormattedNumber(locale)
    val monthText = remember(currentSelectedDate) {
        val dateFormat = DateFormat.getInstanceForSkeleton("EMMMd", locale)
        dateFormat.setContext(DisplayContext.CAPITALIZATION_FOR_STANDALONE)
        dateFormat.format(currentSelectedDate.time)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(datePickerColor.datePickerHeaderColor)
            .padding(16.dp)
    ) {
        Text(
            modifier = Modifier.clickable {
                setMode(DatePickerMode.YEAR)
            },
            text = year,
            fontSize = 14.sp,
            style = when(currentMode) {
                DatePickerMode.YEAR -> dateTextStyle.datePickerSelectedTextStyle
                DatePickerMode.MONTH -> dateTextStyle.datePickerUnselectedTextStyle
            }
        )

        Text(
            modifier = Modifier.clickable {
                setMode(DatePickerMode.MONTH)
            },
            text = monthText,
            fontSize = 24.sp,
            style = when(currentMode) {
                DatePickerMode.YEAR -> dateTextStyle.datePickerUnselectedTextStyle
                DatePickerMode.MONTH -> dateTextStyle.datePickerSelectedTextStyle
            }
        )
    }
}

@Composable
private fun Month(
    yearMonth: YearMonth,
    locale: Locale,
    selectedDate: Calendar,
    dateTextStyle: DatePickerTextStyle,
    datePickerColor: DatePickerColor,
    listener: (Calendar) -> Unit,
    moveToNextMonth: () -> Unit,
    moveToPreviousMonth: () -> Unit
) {
    val year = yearMonth.year
    val month = yearMonth.month
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        MonthHeader(
            year = year,
            month = month,
            locale = locale,
            datePickerColor = datePickerColor,
            dateTextStyle = dateTextStyle,
            moveToNextMonth = moveToNextMonth,
            moveToPreviousMonth = moveToPreviousMonth
        )

        WeekDays(
            dateTextStyle = dateTextStyle,
            locale = locale
        )

        MonthDays(
            year = year,
            month = month,
            locale = locale,
            datePickerColor = datePickerColor,
            dateTextStyle = dateTextStyle,
            selectedDate = selectedDate,
            listener = listener
        )
    }
}

@Composable
private fun MonthHeader(
    year: Int,
    month: Int,
    locale: Locale,
    datePickerColor: DatePickerColor,
    dateTextStyle: DatePickerTextStyle,
    moveToNextMonth: () -> Unit,
    moveToPreviousMonth: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier
                .iconModifier {
                    moveToPreviousMonth()
                },
            imageVector = Icons.Default.KeyboardArrowLeft,
            tint = datePickerColor.monthHeaderIconTintColor,
            contentDescription = "Previous"
        )

        val monthText = remember(year, month) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                java.time.YearMonth.of(year, month).month.getDisplayName(java.time.format.TextStyle.FULL, locale)
            } else {
                Calendar.getInstance(locale).apply {
                    set(year, month - 1, 1)
                }.getDisplayName(Calendar.MONTH, Calendar.LONG, locale) ?: ""
            }
        }

        Text(
            text = "$monthText ${year.getFormattedNumber(locale)}",
            style = dateTextStyle.monthHeaderTextStyle
        )


        Icon(
            modifier = Modifier
                .iconModifier {
                    moveToNextMonth()
                },
            imageVector = Icons.Default.KeyboardArrowRight,
            tint = datePickerColor.monthHeaderIconTintColor,
            contentDescription = "Next"
        )

    }
}

private fun Modifier.iconModifier(
    onClick: () -> Unit
) = composed {
    size(48.dp)
        .clip(RoundedCornerShape(50))
        .clickable(
            interactionSource = remember {
                MutableInteractionSource()
            },
            indication = rememberRipple(),
            onClick = onClick
        )
        .padding(12.dp)
        .rotate(
            if(LocalLayoutDirection.current == LayoutDirection.Rtl) 180f else 0f
        )
}

@Composable
private fun WeekDays(
    dateTextStyle: DatePickerTextStyle,
    locale: Locale
) {
    val firstDayOfWeek = Calendar.getInstance(locale).firstDayOfWeek

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        (0..6).forEach { weekDay ->
            val dayIndex = (weekDay + (firstDayOfWeek - 1)) % 7
            val currentDay = Calendar.getInstance(locale).apply {
                set(2023, 0, 1 + dayIndex)
            }

            val weekText = remember(weekDay, locale) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val dayOrdinal = if (dayIndex == 0) 7 else dayIndex
                    DayOfWeek.of(dayOrdinal).getDisplayName(java.time.format.TextStyle.NARROW, locale).toString()
                } else {
                    val dateFormat = SimpleDateFormat("EEEEE", locale)
                    dateFormat.format(currentDay.time)
                }.toString()
            }

            Text(
                modifier = Modifier.weight(1f),
                text = weekText,
                textAlign = TextAlign.Center,
                style = dateTextStyle.weekDayTextStyle
            )
        }
    }
}

@Composable
private fun MonthDays(
    year: Int,
    month: Int,
    locale: Locale,
    dateTextStyle: DatePickerTextStyle,
    datePickerColor: DatePickerColor,
    selectedDate: Calendar,
    listener: (Calendar) -> Unit,
) {
    val currentMonth = remember(year, month, locale) {
        Calendar.getInstance(locale).apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }

    val totalDaysOfMonth = currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH)

    var currentDay = remember(year, month, locale) {
        -1
    }

    val firstDayOfWeek = currentMonth.firstDayOfWeek
    val weekDayList = (0..6).map { ((firstDayOfWeek + it) % 7).takeIf { it > 0 } ?: 7  }


    val firstDayOfMonth = remember (year, month, locale){
        Calendar.getInstance(locale).apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }.get(Calendar.DAY_OF_WEEK)
    }

    val initialDayInWeek = weekDayList.indexOf(firstDayOfMonth) + 1

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        (1..6).forEach { _ ->
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                (1..7).forEach { day ->
                    val dayCalendar =  when {
                        currentDay == -1 && (day == initialDayInWeek) -> {
                            currentDay = 1
                            Calendar.getInstance(locale).apply {
                                set(Calendar.YEAR, year)
                                set(Calendar.MONTH, month - 1)
                                set(Calendar.DAY_OF_MONTH, 1)
                            }
                        }

                        currentDay == -1 || currentDay >= totalDaysOfMonth -> {
                            null
                        }

                        else -> {
                            currentDay = (currentDay + 1)
                            Calendar.getInstance(locale).apply {
                                set(Calendar.YEAR, year)
                                set(Calendar.MONTH, month - 1)
                                set(Calendar.DAY_OF_MONTH, currentDay)
                            }
                        }
                    }

                    val dayCalendarText = dayCalendar
                        ?.get(Calendar.DAY_OF_MONTH)
                        ?.getFormattedNumber(locale)
                        ?: ""

                    Day(
                        text = dayCalendarText,
                        datePickerColor = datePickerColor,
                        dateTextStyle = dateTextStyle,
                        isPlaceHolderDay = dayCalendar == null,
                        isSelectedDay = dayCalendar?.isSameAs(selectedDate) ?: false,
                        isToday = dayCalendar?.isSameAs(Calendar.getInstance(locale)) ?: false,
                        onClick = {
                            dayCalendar?.let(listener)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.Day(
    text: String,
    datePickerColor: DatePickerColor,
    dateTextStyle: DatePickerTextStyle,
    isSelectedDay: Boolean,
    isPlaceHolderDay: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .padding(1.dp)
            .align(Alignment.CenterVertically)
            .clip(RoundedCornerShape(50))
            .clickable(
                enabled = !isPlaceHolderDay
            ) {
                onClick()
            }
            .background(
                color = when {
                    isSelectedDay -> datePickerColor.selectedDayBgColor
                    isToday -> datePickerColor.currentDayBgColor
                    else -> Color.Transparent
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
            style = when {
                isPlaceHolderDay -> LocalTextStyle.current.copy(color = Color.Transparent)
                isToday -> dateTextStyle.currentDayTextStyle
                isSelectedDay -> dateTextStyle.selectedDayTextStyle
                else -> dateTextStyle.unSelectedDayTextStyle
            }
        )
    }
}

private fun Int.getFormattedNumber(locale: Locale): String {
    val numberFormat = NumberFormat.getNumberInstance(locale)
    return numberFormat.format(this).toString().replace(",","").replace("\u066c","")
}

private fun Calendar.isSameAs(calendar: Calendar): Boolean {
    return get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
            && get(Calendar.MONTH) == calendar.get(Calendar.MONTH)
            && get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH)
}