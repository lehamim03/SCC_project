package com.scc.main1.util;
import android.graphics.Color;

import java.util.Random;

public class ColorUtils {
    public static int getRandomColor() {
        Random random = new Random();
        return Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }
}

