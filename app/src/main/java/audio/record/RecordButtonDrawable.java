package audio.record;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

public class RecordButtonDrawable extends Drawable {

  private Paint mPaint;
  private Drawable mCenterIcon;

  private int mCenterX, mCenterY;
  private int mInitRadius;
  private int mRunningRadius;
  private RectF mPauseRect = new RectF();
  private float mPauseCornerRadius;

  private long mStartTime;
  private long mDuration = 200;
  private Interpolator mInterpolator = new AccelerateDecelerateInterpolator();

  private RectF mTemp = new RectF();

  private enum State {
    INIT, PLAY, PAUSE
  }

  private State mCurrentState = State.INIT;
  private State mPreviousState = null;

  private interface DrawMethod {
    void draw(Canvas canvas);
  }

  private DrawMethod mDraw;

  private PureStatus mPure;

  public interface PureStatus {
    boolean isPure();
  }

  public RecordButtonDrawable(Drawable centerIcon, int color) {
    mCenterIcon = centerIcon;
    mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mPaint.setStyle(Paint.Style.FILL);
    mPaint.setColor(color);
  }

  public void setPureStatus(PureStatus status) {
    mPure = status;
  }

  @Override
  public void draw(Canvas canvas) {
    if (mDraw != null) {
      mDraw.draw(canvas);
    } else {
      drawInit(canvas);
    }
  }

  @Override
  public void setAlpha(int alpha) {

  }

  @Override
  public void setColorFilter(ColorFilter colorFilter) {

  }

  @Override
  public int getOpacity() {
    return PixelFormat.TRANSLUCENT;
  }

  @Override
  protected void onBoundsChange(Rect bounds) {
    super.onBoundsChange(bounds);
    int centerX = bounds.centerX();
    int centerY = bounds.centerY();
    int size = Math.min(bounds.width(), bounds.height());
    int halfSize = size / 2;

    mCenterX = centerX;
    mCenterY = centerY;

    if (mCenterIcon != null) {
      float centerSize = 4f / 7f * halfSize;
      mCenterIcon.setBounds((int) (centerX - centerSize),
          (int) (centerY - centerSize),
          (int) (centerX + centerSize),
          (int) (centerY + centerSize));
    }

    mInitRadius = halfSize;
    mRunningRadius = mInitRadius / 2;

    float pauseSize = 5 / 14f * halfSize;
    mPauseRect.set(centerX - pauseSize,
        centerY - pauseSize,
        centerX + pauseSize,
        centerY + pauseSize
        );
    mPauseCornerRadius = 1 / 14f * size;
  }

  @Override
  protected boolean onStateChange(int[] states) {
    boolean changed = false;
    if (mCenterIcon.isStateful()) {
      changed = mCenterIcon.setState(states);
    }
    for (int state : states) {
      if (state == android.R.attr.state_selected) {
        return setNextState(State.PAUSE) | changed;
      }
    }
    boolean pure = mPure == null ? false : mPure.isPure();
    return setNextState(pure ? State.INIT : State.PLAY) | changed;
  }

  @Override
  public boolean isStateful() {
    return true;
  }

  private void drawInit(Canvas canvas) {
    canvas.drawCircle(mCenterX, mCenterY, mInitRadius, mPaint);
    mCenterIcon.draw(canvas);
  }

  private void drawInitToPause(Canvas canvas) {
    float ratio = getRatio();
    float target = mPauseRect.width() / 2f;
    float r = mInitRadius + (target - mInitRadius) * ratio;
    float cr = mInitRadius + (mPauseCornerRadius - mInitRadius) * ratio;
    mTemp.set(mCenterX - r, mCenterY - r, mCenterX + r, mCenterY + r);
    canvas.drawRoundRect(mTemp, cr, cr, mPaint);
    if (ratio < 0.5f) {
      mCenterIcon.setAlpha((int) ((1 - 2 * ratio) * 255));
      mCenterIcon.draw(canvas);
    }
  }

  private void drawInitToPlay(Canvas canvas) {
    float ratio = getRatio();
    float r = mInitRadius + (mRunningRadius - mInitRadius) * ratio;
    canvas.drawCircle(mCenterX, mCenterY, r, mPaint);
    if (ratio < 0.5f) {
      mCenterIcon.setAlpha((int) ((1 - 2 * ratio) * 255));
      mCenterIcon.draw(canvas);
    }
  }

  private void drawPlay(Canvas canvas) {
    canvas.drawCircle(mCenterX, mCenterY, mRunningRadius, mPaint);
  }

  private void drawPlayToInit(Canvas canvas) {
    float ratio = getRatio();
    float r = mRunningRadius + (mInitRadius - mRunningRadius) * ratio;
    canvas.drawCircle(mCenterX, mCenterY, r, mPaint);
    if (ratio > 0.5f) {
      mCenterIcon.setAlpha((int) ((2 * ratio - 1) * 255));
      mCenterIcon.draw(canvas);
    }
  }

  private void drawPlayToPause(Canvas canvas) {
    float ratio = getRatio();
    float target = mPauseRect.width() / 2f;
    float r = mRunningRadius + (target - mRunningRadius) * ratio;
    float cr = mRunningRadius + (mPauseCornerRadius - mRunningRadius) * ratio;
    mTemp.set(mCenterX - r, mCenterY - r, mCenterX + r, mCenterY + r);
    canvas.drawRoundRect(mTemp, cr, cr, mPaint);
  }

  private void drawPause(Canvas canvas) {
    canvas.drawRoundRect(mPauseRect, mPauseCornerRadius, mPauseCornerRadius, mPaint);
  }

  private void drawPauseToPlay(Canvas canvas) {
    float ratio = getRatio();
    float from = mPauseRect.width() / 2f;
    float r = from + (mRunningRadius - from) * ratio;
    float cr = mPauseCornerRadius + (mRunningRadius - mPauseCornerRadius) * ratio;
    mTemp.set(mCenterX - r, mCenterY - r, mCenterX + r, mCenterY + r);
    canvas.drawRoundRect(mTemp, cr, cr, mPaint);
  }

  private void drawPauseToInit(Canvas canvas) {
    float ratio = getRatio();
    float from = mPauseRect.width() / 2f;
    float r = from + (mInitRadius - from) * ratio;
    float cr = mPauseCornerRadius + (mInitRadius - mPauseCornerRadius) * ratio;
    mTemp.set(mCenterX - r, mCenterY - r, mCenterX + r, mCenterY + r);
    canvas.drawRoundRect(mTemp, cr, cr, mPaint);
    if (ratio > 0.5f) {
      mCenterIcon.setAlpha((int) ((2 * ratio - 1) * 255));
      mCenterIcon.draw(canvas);
    }
  }

  private float getRatio() {
    long now = System.currentTimeMillis();
    if (mStartTime == -1) {
      mStartTime = now;
    }
    long passed = now - mStartTime;
    if (passed >= mDuration) {
      mPreviousState = null;
      updateDraw();
    }
    float n = mDuration == 0 ? 1f : passed * 1.0f / mDuration;
    invalidateSelf();
    return mInterpolator.getInterpolation(Math.min(1f, n));
  }

  private boolean setNextState(State state) {
    if (state == mCurrentState) {
      return false;
    }
    mPreviousState = mCurrentState;
    mCurrentState = state;
    mStartTime = -1;
    updateDraw();
    invalidateSelf();
    return true;
  }

  private void updateDraw() {
    switch (mCurrentState) {
      case INIT:
        if (mPreviousState == State.PAUSE) {
          mDraw = this::drawPauseToInit;
        } else if (mPreviousState == State.PLAY) {
          mDraw = this::drawPlayToInit;
        } else {
          mDraw = this::drawInit;
        }
        break;
      case PLAY:
        if (mPreviousState == State.INIT) {
          mDraw = this::drawInitToPlay;
        } else if (mPreviousState == State.PAUSE) {
          mDraw = this::drawPauseToPlay;
        } else {
          mDraw = this::drawPlay;
        }
        break;
      case PAUSE:
        if (mPreviousState == State.INIT) {
          mDraw = this::drawInitToPause;
        } else if (mPreviousState == State.PLAY) {
          mDraw = this::drawPlayToPause;
        } else {
          mDraw = this::drawPause;
        }
    }
  }

  public void reset() {
    setNextState(State.INIT);
  }
}
