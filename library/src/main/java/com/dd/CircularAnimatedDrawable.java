package com.dd;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.Property;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

class CircularAnimatedDrawable extends Drawable implements Animatable
{
	private static final Interpolator ANGLE_INTERPOLATOR = new LinearInterpolator();
	private static final Interpolator SWEEP_INTERPOLATOR = new DecelerateInterpolator();
	private static final int ANGLE_ANIMATOR_DURATION = 2000;
	private static final int SWEEP_ANIMATOR_DURATION = 600;
	private static final int MIN_SWEEP_ANGLE = 30;
	private final RectF bounds = new RectF();

	private ObjectAnimator objectAnimatorSweep;
	private ObjectAnimator objectAnimatorAngle;
	private boolean modeAppearing;
	private Paint paint;
	private float currentGlobalAngleOffset;
	private float currentGlobalAngle;
	private float currentSweepAngle;
	private float borderWidth;
	private boolean running;

	CircularAnimatedDrawable(int color, float borderWidth){
		this.borderWidth = borderWidth;

		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(borderWidth);
		paint.setColor(color);

		setupAnimations();
	}

	@Override
	public void draw(@NonNull Canvas canvas){
		float startAngle = currentGlobalAngle - currentGlobalAngleOffset;
		float sweepAngle = currentSweepAngle;
		if(!modeAppearing){
			startAngle = startAngle + sweepAngle;
			sweepAngle = 360 - sweepAngle - MIN_SWEEP_ANGLE;
		}
		else {
			sweepAngle += MIN_SWEEP_ANGLE;
		}
		canvas.drawArc(bounds, startAngle, sweepAngle, false, paint);
	}

	@Override
	public void setAlpha(int alpha){
		paint.setAlpha(alpha);
	}

	@Override
	public void setColorFilter(ColorFilter cf){
		paint.setColorFilter(cf);
	}

	@Override
	public int getOpacity(){
		return PixelFormat.TRANSPARENT;
	}

	private void toggleAppearingMode(){
		modeAppearing = !modeAppearing;
		if(modeAppearing){
			currentGlobalAngleOffset = (currentGlobalAngleOffset + MIN_SWEEP_ANGLE * 2) % 360;
		}
	}

	@Override
	protected void onBoundsChange(Rect bounds){
		super.onBoundsChange(bounds);
		this.bounds.left = bounds.left + borderWidth / 2f + .5f;
		this.bounds.right = bounds.right - borderWidth / 2f - .5f;
		this.bounds.top = bounds.top + borderWidth / 2f + .5f;
		this.bounds.bottom = bounds.bottom - borderWidth / 2f - .5f;
	}

	private Property<CircularAnimatedDrawable, Float> mAngleProperty =
			new Property<CircularAnimatedDrawable, Float>(Float.class, "angle")
			{
				@Override
				public Float get(CircularAnimatedDrawable object){
					return object.getCurrentGlobalAngle();
				}

				@Override
				public void set(CircularAnimatedDrawable object, Float value){
					object.setCurrentGlobalAngle(value);
				}
			};

	private Property<CircularAnimatedDrawable, Float> mSweepProperty
			= new Property<CircularAnimatedDrawable, Float>(Float.class, "arc")
	{
		@Override
		public Float get(CircularAnimatedDrawable object){
			return object.getCurrentSweepAngle();
		}

		@Override
		public void set(CircularAnimatedDrawable object, Float value){
			object.setCurrentSweepAngle(value);
		}
	};

	private void setupAnimations(){
		objectAnimatorAngle = ObjectAnimator.ofFloat(this, mAngleProperty, 360f);
		objectAnimatorAngle.setInterpolator(ANGLE_INTERPOLATOR);
		objectAnimatorAngle.setDuration(ANGLE_ANIMATOR_DURATION);
		objectAnimatorAngle.setRepeatMode(ValueAnimator.RESTART);
		objectAnimatorAngle.setRepeatCount(ValueAnimator.INFINITE);

		objectAnimatorSweep = ObjectAnimator.ofFloat(this, mSweepProperty, 360f - MIN_SWEEP_ANGLE * 2);
		objectAnimatorSweep.setInterpolator(SWEEP_INTERPOLATOR);
		objectAnimatorSweep.setDuration(SWEEP_ANIMATOR_DURATION);
		objectAnimatorSweep.setRepeatMode(ValueAnimator.RESTART);
		objectAnimatorSweep.setRepeatCount(ValueAnimator.INFINITE);
		objectAnimatorSweep.addListener(new Animator.AnimatorListener()
		{
			@Override
			public void onAnimationStart(Animator animation){

			}

			@Override
			public void onAnimationEnd(Animator animation){

			}

			@Override
			public void onAnimationCancel(Animator animation){

			}

			@Override
			public void onAnimationRepeat(Animator animation){
				toggleAppearingMode();
			}
		});
	}

	@Override
	public void start(){
		if(isRunning()){
			return;
		}
		running = true;
		objectAnimatorAngle.start();
		objectAnimatorSweep.start();
		invalidateSelf();
	}

	@Override
	public void stop(){
		if(!isRunning()){
			return;
		}
		running = false;
		objectAnimatorAngle.cancel();
		objectAnimatorSweep.cancel();
		invalidateSelf();
	}

	@Override
	public boolean isRunning(){
		return running;
	}


	@SuppressWarnings("WeakerAccess")
	public void setCurrentGlobalAngle(float currentGlobalAngle){
		this.currentGlobalAngle = currentGlobalAngle;
		invalidateSelf();
	}

	@SuppressWarnings("WeakerAccess")
	public float getCurrentGlobalAngle(){
		return currentGlobalAngle;
	}

	@SuppressWarnings("WeakerAccess")
	public void setCurrentSweepAngle(float currentSweepAngle){
		this.currentSweepAngle = currentSweepAngle;
		invalidateSelf();
	}

	@SuppressWarnings("WeakerAccess")
	public float getCurrentSweepAngle(){
		return currentSweepAngle;
	}

}
