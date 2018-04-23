package audio.record;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;

public class FragmentCircleProgressDrawable extends Drawable {

  private Paint mPaint;
  private ArrayList<Frag> mFrags = new ArrayList<>();
  private float mGap = 1;
  float unit = Math.max(mGap / 360f, 1.6f);
  private RectF mRect = new RectF();

  private int mMainColor, mDeleteColor, mBaseColor;

  private Forward mForward;

  FragmentCircleProgressDrawable() {
    mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mPaint.setStyle(Paint.Style.STROKE);
  }

  public void setStrokeWidth(int width) {
    mPaint.setStrokeWidth(width);
    invalidateSelf();
  }

  public void setColor(int color, int base) {
    mPaint.setColor(color);
    mMainColor = color;
    mBaseColor = base;
    invalidateSelf();
  }

  public void add(int progress) {
    mFrags.add(new Frag(progress));
  }

  public void pop() {
    if (mForward != null || mFrags.size() == 0) {
      return;
    }
    mFrags.remove(mFrags.size() - 1);
    invalidateSelf();
  }

  public Forward forward() {
    if (mForward == null) {
      mForward = new Forward();
    }
    return mForward;
  }

  @Override
  protected void onBoundsChange(Rect bounds) {
    super.onBoundsChange(bounds);
    float centerX = bounds.centerX();
    float centerY = bounds.centerY();
    float radius = Math.min(bounds.width(), bounds.height()) / 2f;
    mRect.set(centerX - radius, centerY - radius,
        centerX + radius, centerY + radius);
    mGap = (float) (2f / radius * 180f / Math.PI);
    unit = Math.max(mGap / 360f, 1.6f);
  }

  @Override
  public void draw(Canvas canvas) {
    float inset = mPaint.getStrokeWidth() / 2;
    mRect.inset(inset, inset);

    mPaint.setColor(mBaseColor);
    canvas.drawOval(mRect, mPaint);

    mPaint.setColor(mMainColor);

    float startAngle = 0;
    boolean more = false;
    for (Frag f : mFrags) {
      more |= f.draw(canvas, startAngle);
      startAngle += f.progress / 100f * 360;
    }

    if (mForward != null) {
      mForward.draw(canvas, startAngle);
      more = true;
    }

    if (more) {
      invalidateSelf();
    }

    mRect.inset(-inset, -inset);
  }

  @Override
  public void setAlpha(int alpha) {
    // ignore
  }

  @Override
  public void setColorFilter(ColorFilter colorFilter) {
    // ignore
  }

  @Override
  public int getOpacity() {
    return PixelFormat.TRANSLUCENT;
  }

  private class Frag {

    final int progress;
    int fromProgress;
    float sumUnit;

    long startTime;
    long targetTime;

    Frag(int progress) {
      this.progress = progress;
      this.fromProgress = progress;
      sumUnit = 0;
    }

    Frag(int from, int to) {
      fromProgress = from;
      progress = to;
      sumUnit = unit;
    }

    void setTimeFactor(long startTime) {
      this.startTime = startTime;
      if (startTime < 0) {
        targetTime = 2000;
      } else {
        targetTime = (System.currentTimeMillis() - startTime) / 1000 * 1000 + 2000;
      }
    }

    boolean draw(Canvas canvas, float startAngle) {
      int currentProgress = Math.min(fromProgress += sumUnit, progress);
      boolean more = currentProgress < progress;

      if (targetTime > 0) {
        more = true;
        long now = System.currentTimeMillis();
        if (startTime == -1) {
          startTime = now;
        }
        long passed = now - startTime;
        float alpha = (float) ((Math.cos(Math.PI * 2 * Math.min(passed, targetTime) / 1000) + 1) / 2f);
        mPaint.setAlpha((int) (alpha * 255));
        if (passed >= targetTime) {
          targetTime = 0;
        }
      } else {
        mPaint.setAlpha(255);
      }

      float sweep = Math.min(currentProgress / 100f * 360, 360 - startAngle - mGap);
      if (sweep > mGap || startAngle == 0) {
        if (startAngle > 0) {
          sweep -= mGap;
          startAngle += mGap;
        }
        canvas.drawArc(mRect, startAngle - 90, sweep, false, mPaint);
      }
      return more;
    }
  }

  public class Forward {

    int targetProgress;
    int currentProgress;
    long startTime = -1;

    public void next(int progress) {
      targetProgress = Math.max(currentProgress, progress);
      if (targetProgress > currentProgress) {
        invalidateSelf();
      }
    }

    public void finish(boolean adopt) {
      mForward = null;
      if (adopt) {
        Frag f = new Frag(currentProgress, targetProgress);
        f.setTimeFactor(startTime);
        mFrags.add(f);
      }
      invalidateSelf();
    }

    void draw(Canvas canvas, float startAngle) {
      if (targetProgress == 0) {
        return;
      }
      long now = System.currentTimeMillis();
      if (startTime == -1) {
        startTime = now;
      }
      long duration = now - startTime;
      float alpha = (float) ((1 + Math.cos(2 * Math.PI * duration / 1000f)) / 2f);
      mPaint.setAlpha((int) (alpha * 255));

      currentProgress = (int) Math.min(targetProgress, currentProgress + unit);

      float sweep = currentProgress / 100f * 360f;
      if (sweep > mGap) {
        canvas.drawArc(mRect, startAngle - 90, sweep - mGap, false, mPaint);
      }
    }
  }
}
