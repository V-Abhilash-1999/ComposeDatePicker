package com.abhilash.apps.composedatepicker

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.abhilash.apps.composedatepicker.ui.theme.ComposeDatePickerTheme
import java.util.Calendar
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context = this
        setContent {
            ComposeDatePickerTheme {
                val showDatePicker = remember {
                    mutableStateOf(false)
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            showDatePicker.value = true
                        }
                    ) {
                        Text(text = "Show Date Picker")
                    }
                }

                if(showDatePicker.value) {
                    val themeColor = Color.Blue
                    val onThemeColor = Color.White

                    val backgroundColor = Color.White
                    val onBackgroundColor = Color.Black
                    
                    val baseTextStyle= LocalTextStyle.current
                    
                    DatePicker(
                        locale = Locale.ENGLISH,
                        datePickerColor = DatePickerColor(
                            themeColor = themeColor,
                            monthHeaderIconTintColor = onBackgroundColor,
                            backgroundColor = backgroundColor,
                            currentDayBgColor = themeColor.copy(alpha = 0.5f),
                            yearPickerSeparatorColor = onBackgroundColor
                        ),
                        dateTextStyle = DatePickerTextStyle(
                            datePickerSelectedTextStyle = baseTextStyle.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = onThemeColor,
                            ),
                            datePickerUnselectedTextStyle = baseTextStyle.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = onThemeColor.copy(alpha = 0.75f),
                            ),
                            monthHeaderTextStyle = baseTextStyle.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = onBackgroundColor
                            ),
                            weekDayTextStyle = baseTextStyle.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = onBackgroundColor
                            ),
                            unSelectedDayTextStyle = baseTextStyle.copy(
                                color = onBackgroundColor
                            ),
                            buttonTextStyle = baseTextStyle.copy(
                                color = themeColor,
                                fontSize = 16.sp
                            ),
                            selectedDayTextStyle = baseTextStyle.copy(
                                color = onThemeColor
                            ),
                            currentDayTextStyle = baseTextStyle.copy(
                                color = onThemeColor
                            ),
                            yearPickerSelectedTextStyle = baseTextStyle.copy(
                                color = themeColor,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            yearPickerUnselectedTextStyle = baseTextStyle.copy(
                                color = onBackgroundColor
                            ),
                        ),
                        listener = object : DatePickerListener {
                            override fun onDateSelected(date: Calendar) {
                                val dateText =
                                    "${date.get(Calendar.DAY_OF_MONTH)} / ${date.get(Calendar.MONTH) + 1} / ${
                                        date.get(Calendar.YEAR)
                                    }"
                                Toast.makeText(context, "Date = $dateText", Toast.LENGTH_SHORT)
                                    .show()
                            }

                            override fun oKButtonClicked() {
                                showDatePicker.value = false
                            }

                            override fun cancelButtonClicked() {
                                showDatePicker.value = false
                            }

                        }
                    )
                }


            }
        }
    }
}