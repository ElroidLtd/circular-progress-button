package com.dd;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.view.View;

/**
 * Class: com.dd.ViewUtils
 * Project: SmartDriverClub
 * Created Date: 04/04/2017 07:53
 *
 * @author <a href="mailto:e@elroid.com">Elliot Long</a>
 *         Copyright (c) 2017 Elroid Ltd. All rights reserved.
 */
@SuppressWarnings("WeakerAccess") // this is a utils class!
public class ViewUtils
{
	public static
	@ColorInt
	int color(@ColorRes int colorResID, View v){
		return color(colorResID, v.getContext());
	}

	@SuppressWarnings("deprecation")
	public static
	@ColorInt
	int color(@ColorRes int colorResID, Context ctx){
		Resources r = ctx.getResources();
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
			Resources.Theme t = ctx.getTheme();
			return r.getColor(colorResID, t);
		}
		else {
			return r.getColor(colorResID);
		}
	}

	public static Drawable drawable(@DrawableRes int drawableId, View v){
		return ContextCompat.getDrawable(v.getContext(), drawableId);
	}

	public static ColorStateList colorStateList(@ColorRes int colorResID, View v){
		return ContextCompat.getColorStateList(v.getContext(), colorResID);
	}
}
