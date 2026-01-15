package com.scc.main1.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.core.content.ContextCompat;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;
import com.scc.main1.R;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import com.scc.main1.util.ColorUtils;

public class DayDecorator implements DayViewDecorator {

    private final Drawable drawable;
    private final HashSet<CalendarDay> dates;

    public DayDecorator(Context context, Collection<CalendarDay> dateCollection) {
        drawable = ContextCompat.getDrawable(context, R.drawable.calendar_selector);
        this.dates = new HashSet<>(dateCollection);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        boolean shouldDecorate = dates.contains(day);
        return shouldDecorate;
    }

    public void decorate(DayViewFacade view) {
        // 랜덤한 색상을 가져와 배경 이미지로 설정
        int randomColor = ColorUtils.getRandomColor();
        drawable.setTint(randomColor);
        view.setBackgroundDrawable(drawable);
    }
}
