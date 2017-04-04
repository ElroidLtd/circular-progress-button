package com.dd;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.util.StateSet;

import com.dd.circular.progress.button.R;

public class CircularProgressButton extends AppCompatButton
{
	public static final int IDLE_STATE_PROGRESS = 0;
	public static final int ERROR_STATE_PROGRESS = -1;
	public static final int SUCCESS_STATE_PROGRESS = 100;
	public static final int INDETERMINATE_STATE_PROGRESS = 50;

	private StrokeGradientDrawable background;
	private CircularAnimatedDrawable animatedDrawable;
	private CircularProgressDrawable progressDrawable;

	private ColorStateList idleColorState;
	private ColorStateList completeColorState;
	private ColorStateList errorColorState;

	private StateListDrawable idleStateDrawable;
	private StateListDrawable completeStateDrawable;
	private StateListDrawable errorStateDrawable;

	private StateManager stateManager;
	private State state;
	private String idleText;
	private String completeText;
	private String errorText;
	private String progressText;

	private int colorProgress;
	private int colorIndicator;
	private int colorIndicatorBackground;
	private int iconComplete;
	private int iconError;
	private int strokeWidth;
	private int paddingProgress;
	private float cornerRadius;
	private boolean indeterminateProgressMode;
	private boolean configurationChanged;

	private enum State
	{
		PROGRESS, IDLE, COMPLETE, ERROR
	}

	private int maxProgress;
	private int progress;

	private boolean morphingInProgress;

	public CircularProgressButton(Context context){
		super(context);
		init(context, null);
	}

	public CircularProgressButton(Context context, AttributeSet attrs){
		super(context, attrs);
		init(context, attrs);
	}

	public CircularProgressButton(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attributeSet){
		strokeWidth = (int) getContext().getResources().getDimension(R.dimen.cpb_stroke_width);

		initAttributes(context, attributeSet);

		maxProgress = 100;
		state = State.IDLE;
		stateManager = new StateManager(this);

		setText(idleText);

		initIdleStateDrawable();
		setBackgroundCompat(idleStateDrawable);
	}

	private void initErrorStateDrawable(){
		int colorPressed = getPressedColor(errorColorState);

		StrokeGradientDrawable drawablePressed = createDrawable(colorPressed);
		errorStateDrawable = new StateListDrawable();

		errorStateDrawable.addState(new int[]{android.R.attr.state_pressed}, drawablePressed.getGradientDrawable());
		errorStateDrawable.addState(StateSet.WILD_CARD, background.getGradientDrawable());
	}

	private void initCompleteStateDrawable(){
		int colorPressed = getPressedColor(completeColorState);

		StrokeGradientDrawable drawablePressed = createDrawable(colorPressed);
		completeStateDrawable = new StateListDrawable();

		completeStateDrawable.addState(new int[]{android.R.attr.state_pressed}, drawablePressed.getGradientDrawable());
		completeStateDrawable.addState(StateSet.WILD_CARD, background.getGradientDrawable());
	}

	private void initIdleStateDrawable(){
		int colorNormal = getNormalColor(idleColorState);
		int colorPressed = getPressedColor(idleColorState);
		int colorFocused = getFocusedColor(idleColorState);
		int colorDisabled = getDisabledColor(idleColorState);
		if(background == null){
			background = createDrawable(colorNormal);
		}

		StrokeGradientDrawable drawableDisabled = createDrawable(colorDisabled);
		StrokeGradientDrawable drawableFocused = createDrawable(colorFocused);
		StrokeGradientDrawable drawablePressed = createDrawable(colorPressed);
		idleStateDrawable = new StateListDrawable();

		idleStateDrawable.addState(new int[]{android.R.attr.state_pressed}, drawablePressed.getGradientDrawable());
		idleStateDrawable.addState(new int[]{android.R.attr.state_focused}, drawableFocused.getGradientDrawable());
		idleStateDrawable.addState(new int[]{-android.R.attr.state_enabled}, drawableDisabled.getGradientDrawable());
		idleStateDrawable.addState(StateSet.WILD_CARD, background.getGradientDrawable());
	}

	private int getNormalColor(ColorStateList colorStateList){
		return colorStateList.getColorForState(new int[]{android.R.attr.state_enabled}, 0);
	}

	private int getPressedColor(ColorStateList colorStateList){
		return colorStateList.getColorForState(new int[]{android.R.attr.state_pressed}, 0);
	}

	private int getFocusedColor(ColorStateList colorStateList){
		return colorStateList.getColorForState(new int[]{android.R.attr.state_focused}, 0);
	}

	private int getDisabledColor(ColorStateList colorStateList){
		return colorStateList.getColorForState(new int[]{-android.R.attr.state_enabled}, 0);
	}

	private StrokeGradientDrawable createDrawable(int color){
		GradientDrawable drawable = (GradientDrawable)
				ViewUtils.drawable(R.drawable.cpb_background, this).mutate();
		drawable.setColor(color);
		drawable.setCornerRadius(cornerRadius);
		StrokeGradientDrawable strokeGradientDrawable = new StrokeGradientDrawable(drawable);
		strokeGradientDrawable.setStrokeColor(color);
		strokeGradientDrawable.setStrokeWidth(strokeWidth);

		return strokeGradientDrawable;
	}

	@Override
	protected void drawableStateChanged(){
		if(state == State.COMPLETE){
			initCompleteStateDrawable();
			setBackgroundCompat(completeStateDrawable);
		}
		else if(state == State.IDLE){
			initIdleStateDrawable();
			setBackgroundCompat(idleStateDrawable);
		}
		else if(state == State.ERROR){
			initErrorStateDrawable();
			setBackgroundCompat(errorStateDrawable);
		}

		if(state != State.PROGRESS){
			super.drawableStateChanged();
		}
	}

	private void initAttributes(Context context, AttributeSet attributeSet){
		TypedArray attr = getTypedArray(context, attributeSet, R.styleable.CircularProgressButton);
		if(attr == null){
			return;
		}

		try {

			idleText = attr.getString(R.styleable.CircularProgressButton_cpb_textIdle);
			completeText = attr.getString(R.styleable.CircularProgressButton_cpb_textComplete);
			errorText = attr.getString(R.styleable.CircularProgressButton_cpb_textError);
			progressText = attr.getString(R.styleable.CircularProgressButton_cpb_textProgress);

			iconComplete = attr.getResourceId(R.styleable.CircularProgressButton_cpb_iconComplete, 0);
			iconError = attr.getResourceId(R.styleable.CircularProgressButton_cpb_iconError, 0);
			cornerRadius = attr.getDimension(R.styleable.CircularProgressButton_cpb_cornerRadius, 0);
			paddingProgress = attr.getDimensionPixelSize(R.styleable.CircularProgressButton_cpb_paddingProgress, 0);

			int blue = ViewUtils.color(R.color.cpb_blue, this);
			int white = ViewUtils.color(R.color.cpb_white, this);
			int grey = ViewUtils.color(R.color.cpb_grey, this);

			int idleStateSelector = attr.getResourceId(R.styleable.CircularProgressButton_cpb_selectorIdle,
					R.color.cpb_idle_state_selector);
			idleColorState = ViewUtils.colorStateList(idleStateSelector, this);

			int completeStateSelector = attr.getResourceId(R.styleable.CircularProgressButton_cpb_selectorComplete,
					R.color.cpb_complete_state_selector);
			completeColorState = ViewUtils.colorStateList(completeStateSelector, this);

			int errorStateSelector = attr.getResourceId(R.styleable.CircularProgressButton_cpb_selectorError,
					R.color.cpb_error_state_selector);
			errorColorState = ViewUtils.colorStateList(errorStateSelector, this);

			colorProgress = attr.getColor(R.styleable.CircularProgressButton_cpb_colorProgress, white);
			colorIndicator = attr.getColor(R.styleable.CircularProgressButton_cpb_colorIndicator, blue);
			colorIndicatorBackground =
					attr.getColor(R.styleable.CircularProgressButton_cpb_colorIndicatorBackground, grey);
		}
		finally {
			attr.recycle();
		}
	}

	protected TypedArray getTypedArray(Context context, AttributeSet attributeSet, int[] attr){
		return context.obtainStyledAttributes(attributeSet, attr, 0, 0);
	}

	@Override
	protected void onDraw(Canvas canvas){
		super.onDraw(canvas);

		if(progress > 0 && state == State.PROGRESS && !morphingInProgress){
			if(indeterminateProgressMode){
				drawIndeterminateProgress(canvas);
			}
			else {
				drawProgress(canvas);
			}
		}
	}

	private void drawIndeterminateProgress(Canvas canvas){
		if(animatedDrawable == null){
			int offset = (getWidth() - getHeight()) / 2;
			animatedDrawable = new CircularAnimatedDrawable(colorIndicator, strokeWidth);
			int left = offset + paddingProgress;
			int right = getWidth() - offset - paddingProgress;
			int bottom = getHeight() - paddingProgress;
			int top = paddingProgress;
			animatedDrawable.setBounds(left, top, right, bottom);
			animatedDrawable.setCallback(this);
			animatedDrawable.start();
		}
		else {
			animatedDrawable.draw(canvas);
		}
	}

	private void drawProgress(Canvas canvas){
		if(progressDrawable == null){
			int offset = (getWidth() - getHeight()) / 2;
			int size = getHeight() - paddingProgress * 2;
			progressDrawable = new CircularProgressDrawable(size, strokeWidth, colorIndicator);
			int left = offset + paddingProgress;
			progressDrawable.setBounds(left, paddingProgress, left, paddingProgress);
		}
		float sweepAngle = (360f / maxProgress) * progress;
		progressDrawable.setSweepAngle(sweepAngle);
		progressDrawable.draw(canvas);
	}

	@SuppressWarnings("unused")
	public boolean isIndeterminateProgressMode(){
		return indeterminateProgressMode;
	}

	public void setIndeterminateProgressMode(boolean indeterminateProgressMode){
		this.indeterminateProgressMode = indeterminateProgressMode;
	}

	@Override
	protected boolean verifyDrawable(@NonNull Drawable who){
		return who == animatedDrawable || super.verifyDrawable(who);
	}

	private MorphingAnimation createMorphing(){
		morphingInProgress = true;

		MorphingAnimation animation = new MorphingAnimation(this, background);
		animation.setFromCornerRadius(cornerRadius);
		animation.setToCornerRadius(cornerRadius);

		animation.setFromWidth(getWidth());
		animation.setToWidth(getWidth());

		if(configurationChanged){
			animation.setDuration(MorphingAnimation.DURATION_INSTANT);
		}
		else {
			animation.setDuration(MorphingAnimation.DURATION_NORMAL);
		}

		configurationChanged = false;

		return animation;
	}

	private MorphingAnimation createProgressMorphing(float fromCorner, float toCorner, int fromWidth, int toWidth){
		morphingInProgress = true;

		MorphingAnimation animation = new MorphingAnimation(this, background);
		animation.setFromCornerRadius(fromCorner);
		animation.setToCornerRadius(toCorner);

		animation.setPadding(paddingProgress);

		animation.setFromWidth(fromWidth);
		animation.setToWidth(toWidth);

		if(configurationChanged){
			animation.setDuration(MorphingAnimation.DURATION_INSTANT);
		}
		else {
			animation.setDuration(MorphingAnimation.DURATION_NORMAL);
		}

		configurationChanged = false;

		return animation;
	}

	private void morphToProgress(int fromProgress){
		setWidth(getWidth());
		setText(progressText);

		MorphingAnimation animation = createProgressMorphing(cornerRadius, getHeight(), getWidth(), getHeight());

		int currentColor = getNormalColor(idleColorState);
		if(fromProgress == 100) currentColor = getNormalColor(completeColorState);
		animation.setFromColor(currentColor);
		animation.setToColor(colorProgress);

		animation.setFromStrokeColor(currentColor);
		animation.setToStrokeColor(colorIndicatorBackground);

		animation.setListener(mProgressStateListener);

		animation.start();
	}

	private OnAnimationEndListener mProgressStateListener = new OnAnimationEndListener()
	{
		@Override
		public void onAnimationEnd(){
			morphingInProgress = false;
			state = State.PROGRESS;

			stateManager.checkState(CircularProgressButton.this);
		}
	};

	private void morphProgressToComplete(){
		MorphingAnimation animation = createProgressMorphing(getHeight(), cornerRadius, getHeight(), getWidth());

		animation.setFromColor(colorProgress);
		animation.setToColor(getNormalColor(completeColorState));

		animation.setFromStrokeColor(colorIndicator);
		animation.setToStrokeColor(getNormalColor(completeColorState));
		animation.setListener(mCompleteStateListener);

		animation.start();


	}

	private void morphIdleToComplete(){
		MorphingAnimation animation = createMorphing();

		animation.setFromColor(getNormalColor(idleColorState));
		animation.setToColor(getNormalColor(completeColorState));

		animation.setFromStrokeColor(getNormalColor(idleColorState));
		animation.setToStrokeColor(getNormalColor(completeColorState));
		animation.setListener(mCompleteStateListener);

		animation.start();

	}

	private void morphErrorToComplete(){
		MorphingAnimation animation = createMorphing();

		animation.setFromColor(getNormalColor(errorColorState));
		animation.setToColor(getNormalColor(completeColorState));

		animation.setFromStrokeColor(getNormalColor(errorColorState));
		animation.setToStrokeColor(getNormalColor(completeColorState));
		animation.setListener(mCompleteStateListener);

		animation.start();

	}

	private OnAnimationEndListener mCompleteStateListener = new OnAnimationEndListener()
	{
		@Override
		public void onAnimationEnd(){
			if(iconComplete != 0){
				setText(null);
				setIcon(iconComplete);
			}
			else {
				setText(completeText);
			}
			morphingInProgress = false;
			state = State.COMPLETE;

			stateManager.checkState(CircularProgressButton.this);
		}
	};

	private void morphCompleteToIdle(){
		MorphingAnimation animation = createMorphing();

		animation.setFromColor(getNormalColor(completeColorState));
		animation.setToColor(getNormalColor(idleColorState));

		animation.setFromStrokeColor(getNormalColor(completeColorState));
		animation.setToStrokeColor(getNormalColor(idleColorState));
		animation.setListener(mIdleStateListener);

		animation.start();

	}

	private void morphErrorToIdle(){
		MorphingAnimation animation = createMorphing();

		animation.setFromColor(getNormalColor(errorColorState));
		animation.setToColor(getNormalColor(idleColorState));

		animation.setFromStrokeColor(getNormalColor(errorColorState));
		animation.setToStrokeColor(getNormalColor(idleColorState));
		animation.setListener(mIdleStateListener);

		animation.start();

	}

	private OnAnimationEndListener mIdleStateListener = new OnAnimationEndListener()
	{
		@Override
		public void onAnimationEnd(){
			removeIcon();
			setText(idleText);
			morphingInProgress = false;
			state = State.IDLE;

			stateManager.checkState(CircularProgressButton.this);
		}
	};

	private void morphIdleToError(){
		MorphingAnimation animation = createMorphing();

		animation.setFromColor(getNormalColor(idleColorState));
		animation.setToColor(getNormalColor(errorColorState));

		animation.setFromStrokeColor(getNormalColor(idleColorState));
		animation.setToStrokeColor(getNormalColor(errorColorState));
		animation.setListener(mErrorStateListener);

		animation.start();

	}

	private void morphProgressToError(){
		MorphingAnimation animation = createProgressMorphing(getHeight(), cornerRadius, getHeight(), getWidth());

		animation.setFromColor(colorProgress);
		animation.setToColor(getNormalColor(errorColorState));

		animation.setFromStrokeColor(colorIndicator);
		animation.setToStrokeColor(getNormalColor(errorColorState));
		animation.setListener(mErrorStateListener);

		animation.start();
	}

	private OnAnimationEndListener mErrorStateListener = new OnAnimationEndListener()
	{
		@Override
		public void onAnimationEnd(){
			if(iconError != 0){
				setText(null);
				setIcon(iconError);
			}
			else {
				setText(errorText);
			}
			morphingInProgress = false;
			state = State.ERROR;

			stateManager.checkState(CircularProgressButton.this);
		}
	};

	private void morphProgressToIdle(){
		MorphingAnimation animation = createProgressMorphing(getHeight(), cornerRadius, getHeight(), getWidth());

		animation.setFromColor(colorProgress);
		animation.setToColor(getNormalColor(idleColorState));

		animation.setFromStrokeColor(colorIndicator);
		animation.setToStrokeColor(getNormalColor(idleColorState));
		animation.setListener(mIdleStateListener);

		animation.start();
	}

	private void setIcon(@DrawableRes int icon){
		Drawable drawable = ViewUtils.drawable(icon, this);
		if(drawable != null){
			int padding = (getWidth() / 2) - (drawable.getIntrinsicWidth() / 2);
			setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);
			setPadding(padding, 0, 0, 0);
		}
	}

	protected void removeIcon(){
		setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		setPadding(0, 0, 0, 0);
	}

	/**
	 * Set the View's background. Masks the API changes made in Jelly Bean.
	 */
	@SuppressWarnings("deprecation")
	@SuppressLint({"NewApi", "ObsoleteSdkInt"})
	public void setBackgroundCompat(Drawable drawable){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
			setBackground(drawable);
		}
		else {
			setBackgroundDrawable(drawable);
		}
	}

	/*public void setProgress(int progress) {
		setProgress(progress, true);
	}*/
	public void setProgress(int progress, boolean animate){
		int fromProgress = this.progress;
		this.progress = progress;

		configurationChanged = !animate;

		if(morphingInProgress || getWidth() == 0){
			return;
		}

		stateManager.saveProgress(this);

		if(this.progress == SUCCESS_STATE_PROGRESS || this.progress >= maxProgress){
			if(state == State.PROGRESS){
				morphProgressToComplete();
			}
			else if(state == State.IDLE){
				morphIdleToComplete();
			}
			else if(state == State.ERROR){
				morphErrorToComplete();
			}
		}
		else if(this.progress == ERROR_STATE_PROGRESS){
			if(state == State.PROGRESS){
				morphProgressToError();
			}
			else if(state == State.IDLE){
				morphIdleToError();
			}
		}
		else if(this.progress == IDLE_STATE_PROGRESS){
			if(state == State.COMPLETE){
				morphCompleteToIdle();
			}
			else if(state == State.PROGRESS){
				morphProgressToIdle();
			}
			else if(state == State.ERROR){
				morphErrorToIdle();
			}
		}
		else if(this.progress > IDLE_STATE_PROGRESS){
			if(state == State.PROGRESS){
				invalidate();
			}
			else {
				morphToProgress(fromProgress);
			}
		}
	}

	public int getProgress(){
		return progress;
	}

	public void setBackgroundColor(int color){
		background.getGradientDrawable().setColor(color);
	}

	@SuppressWarnings("unused")
	public void setStrokeColor(int color){
		background.setStrokeColor(color);
	}

	@SuppressWarnings("unused")
	public String getIdleText(){
		return idleText;
	}

	@SuppressWarnings("unused")
	public String getCompleteText(){
		return completeText;
	}

	@SuppressWarnings("unused")
	public String getErrorText(){
		return errorText;
	}

	@SuppressWarnings("unused")
	public void setIdleText(String text){
		idleText = text;
	}

	@SuppressWarnings("unused")
	public void setCompleteText(String text){
		completeText = text;
	}

	@SuppressWarnings("unused")
	public void setErrorText(String text){
		errorText = text;
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom){
		super.onLayout(changed, left, top, right, bottom);
		if(changed){
			setProgress(progress, false);
		}
	}

	@Override
	public Parcelable onSaveInstanceState(){
		Parcelable superState = super.onSaveInstanceState();
		SavedState savedState = new SavedState(superState);
		savedState.mProgress = progress;
		savedState.mIndeterminateProgressMode = indeterminateProgressMode;
		savedState.mConfigurationChanged = true;

		return savedState;
	}

	@Override
	public void onRestoreInstanceState(Parcelable state){
		if(state instanceof SavedState){
			SavedState savedState = (SavedState) state;
			progress = savedState.mProgress;
			indeterminateProgressMode = savedState.mIndeterminateProgressMode;
			configurationChanged = savedState.mConfigurationChanged;
			super.onRestoreInstanceState(savedState.getSuperState());
			setProgress(progress, false);
		}
		else {
			super.onRestoreInstanceState(state);
		}
	}


	private static class SavedState extends BaseSavedState
	{

		private boolean mIndeterminateProgressMode;
		private boolean mConfigurationChanged;
		private int mProgress;

		SavedState(Parcelable parcel){
			super(parcel);
		}

		private SavedState(Parcel in){
			super(in);
			mProgress = in.readInt();
			mIndeterminateProgressMode = in.readInt() == 1;
			mConfigurationChanged = in.readInt() == 1;
		}

		@Override
		public void writeToParcel(Parcel out, int flags){
			super.writeToParcel(out, flags);
			out.writeInt(mProgress);
			out.writeInt(mIndeterminateProgressMode ? 1 : 0);
			out.writeInt(mConfigurationChanged ? 1 : 0);
		}

		public static final Creator<SavedState> CREATOR = new Creator<SavedState>()
		{

			@Override
			public SavedState createFromParcel(Parcel in){
				return new SavedState(in);
			}

			@Override
			public SavedState[] newArray(int size){
				return new SavedState[size];
			}
		};
	}
}
