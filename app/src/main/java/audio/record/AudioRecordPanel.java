package audio.record;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.PopupWindow;

import com.zy.annotationprocessor.R;

import java.io.File;

public class AudioRecordPanel extends Fragment {

  private View mPlayBtn;
  private View mAdopt;
  private View mDrop;
  private View mDelete;
  private View mPanel;

  private AudioRecorder mRecorder;
  private FragmentCircleProgressDrawable mProgress;

  private long mMaxTime = DateUtils.MINUTE_IN_MILLIS * 1;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    android.util.Log.e("XXXXX", "onCreateView start");
    View root = inflater.inflate(R.layout.audio_record, container, false);
    mPlayBtn = root.findViewById(R.id.control);
    mAdopt = root.findViewById(R.id.finish);
    mDrop = root.findViewById(R.id.drop);
    mDelete = root.findViewById(R.id.delete);
    mPanel = root.findViewById(R.id.panel);

    mProgress = new FragmentCircleProgressDrawable();
    mProgress.setStrokeWidth(6);
    mProgress.setColor(0xffff8000);
    mPlayBtn.setBackground(mProgress);

    root.setOnClickListener(v -> dismiss());
    mPanel.setOnClickListener(v -> {});

    mPlayBtn.setEnabled(false);
    mPlayBtn.setOnClickListener(v -> control());
    mDrop.setOnClickListener(v -> abort());
    mDelete.setOnClickListener(v -> delete());
    mAdopt.setOnClickListener(v -> adopt());

    mRecorder = new AudioRecorder();
    mRecorder.setFile(Environment.getExternalStorageDirectory().getAbsolutePath(), "rrr.m4a");
    mRecorder.setMaxTime(mMaxTime);

    mRecorder.setListener(new AudioRecorder.Listener() {
      @Override
      public void onStatusChange(AudioRecorder.Status status) {
        switch (status) {
          case INIT:
            mPlayBtn.setEnabled(true);
            android.util.Log.e("XXXXX", "on init");
            break;
          case START:
            mPlayBtn.setSelected(true);
            android.util.Log.e("XXXXX", "on Start");
            break;
          case PAUSE:
            mPlayBtn.setSelected(false);
            android.util.Log.e("XXXXX", "on pause");
            break;
          case STOP:
            mPlayBtn.setEnabled(false);
            break;
        }
      }

      @Override
      public void onPieceRecord(long length) {
        mProgress.forward().next((int) (length * 100f / mMaxTime));
      }

      @Override
      public void onPieceAdded(int count, long length) {
        mProgress.forward().finish(true);
        mAdopt.setVisibility(count > 0 ? View.VISIBLE : View.INVISIBLE);
        mDelete.setVisibility(count > 0 ? View.VISIBLE : View.INVISIBLE);
      }

      @Override
      public void onPieceDeleted(int remain) {
        mProgress.pop();
        mAdopt.setVisibility(remain > 0 ? View.VISIBLE : View.INVISIBLE);
        mDelete.setVisibility(remain > 0 ? View.VISIBLE : View.INVISIBLE);
      }

      @Override
      public void onComposed(File file) {
        dismiss();
      }
    });
    android.util.Log.e("XXXXX", "onCreateView end");
    return root;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    Animation anim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
        Animation.RELATIVE_TO_SELF, 1, Animation.RELATIVE_TO_SELF, 0);
    anim.setDuration(300);
    mPanel.startAnimation(anim);
    android.util.Log.e("XXXXX", "onViewCreated");
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    android.util.Log.e("XXXX", "onDestroyView");
  }

  @Override
  public void onResume() {
    super.onResume();
    android.util.Log.e("XXXX", "onResume");
  }

  @Override
  public void onDetach() {
    super.onDetach();
    android.util.Log.e("XXXX", "detach");
  }

  private void control() {
    if (mPlayBtn.isSelected()) {
      mPlayBtn.setSelected(false);
      mRecorder.pause();
      android.util.Log.e("XXXXX", "click pause button");
    } else {
      mPlayBtn.setSelected(true);
      mRecorder.start();
      android.util.Log.e("XXXXX", "click start button");
    }
  }

  private void abort() {
    mRecorder.abort();
    dismiss();
  }

  private void delete() {
    mRecorder.deleteLatest();
  }

  private void adopt() {
    mRecorder.finish();
  }

  private void dismiss() {
    android.util.Log.e("XXXX", "dismiss");
    mPanel.animate().translationY(mPanel.getHeight()).withEndAction(() -> {
        getFragmentManager().beginTransaction().remove(this).commitAllowingStateLoss();
        if (listener != null) {
          listener.onDismiss();
        }
      }
    );
  }

  public PopupWindow.OnDismissListener listener;

}
