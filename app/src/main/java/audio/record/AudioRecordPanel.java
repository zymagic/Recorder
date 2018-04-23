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
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.zy.annotationprocessor.R;

import java.io.File;

public class AudioRecordPanel extends Fragment {

  private View mControl;
  private View mPlay;
  private View mAdopt;
  private View mDrop;
  private View mDelete;
  private View mPanel;

  private AudioRecordControl mRecorder;
  private FragmentCircleProgressDrawable mProgress;
  private RecordButtonDrawable mControlButton;

  private long mMaxTime = DateUtils.MINUTE_IN_MILLIS * 1;

  private boolean isPure = true;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    android.util.Log.e("XXXXX", "onCreateView start");
    View root = inflater.inflate(R.layout.fragment_audio_record, container, false);
    mControl = root.findViewById(R.id.control);
    mAdopt = root.findViewById(R.id.finish);
    mDrop = root.findViewById(R.id.drop);
    mDelete = root.findViewById(R.id.delete);
    mPanel = root.findViewById(R.id.panel);
    mPlay = root.findViewById(R.id.play_back);

    mProgress = new FragmentCircleProgressDrawable();
    mProgress.setStrokeWidth((int) (3 * getActivity().getResources().getDisplayMetrics().density));
    mProgress.setColor(0xffff8000, 0xfff3f3f3);
    mControl.setBackground(mProgress);

    root.setOnClickListener(v -> abort());
    mPanel.setOnClickListener(v -> {});

    mControlButton = new RecordButtonDrawable(getActivity().getResources().getDrawable(R.drawable.karaoke_btn_record_normal), 0xffff8000);
    mControlButton.setPureStatus(() -> isPure);
    ((ImageView) mControl).setImageDrawable(mControlButton);
    mControl.setEnabled(false);
    mControl.setOnClickListener(v -> control());
    mDrop.setOnClickListener(v -> abort());
    mDelete.setOnClickListener(v -> delete());
    mAdopt.setOnClickListener(v -> adopt());
    mPlay.setOnClickListener(v -> play());

    mRecorder = new AudioRecordControl();
    mRecorder.setOutput(Environment.getExternalStorageDirectory().getAbsolutePath(), "rrr.aac");
    mRecorder.setMaxTime(mMaxTime);

    mRecorder.setListener(new AudioRecordControl.Listener() {
      @Override
      public void onStatusChanged(AudioRecordControl.Status status) {
        switch (status) {
          case INIT:
            mControl.setEnabled(true);
            android.util.Log.e("XXXXX", "on init");
            break;
          case START:
            mControl.setSelected(true);

            mDelete.setVisibility(View.INVISIBLE);
            mPlay.setVisibility(View.INVISIBLE);
            mAdopt.setVisibility(View.INVISIBLE);
            android.util.Log.e("XXXXX", "on Start");
            break;
          case PAUSE:
            mControl.setSelected(false);
            android.util.Log.e("XXXXX", "on pause");
            break;
          case STOP:
            mControl.setEnabled(false);
            break;
        }
      }

      @Override
      public void onProgressUpdate(int index, long length) {
        mProgress.forward().next((int) (length * 100f / mMaxTime));
      }

      @Override
      public void onPieceAdded(int count, long length) {
        mProgress.forward().finish(true);
        mAdopt.setVisibility(count > 0 ? View.VISIBLE : View.INVISIBLE);
        mDelete.setVisibility(count > 0 ? View.VISIBLE : View.INVISIBLE);
        mPlay.setVisibility(count > 0 ? View.VISIBLE : View.INVISIBLE);
      }

      @Override
      public void onPieceDeleted(int remain) {
        mProgress.pop();
        mAdopt.setVisibility(remain > 0 ? View.VISIBLE : View.INVISIBLE);
        mDelete.setVisibility(remain > 0 ? View.VISIBLE : View.INVISIBLE);
        mPlay.setVisibility(remain > 0 ? View.VISIBLE : View.INVISIBLE);
        if (remain == 0) {
          isPure = true;
          mControlButton.reset();
        }
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
    if (mControl.isSelected()) {
      mControl.setSelected(false);
      mRecorder.pause();
      android.util.Log.e("XXXXX", "click pause button");
    } else {
      mControl.setSelected(true);
      mRecorder.start();
      isPure = false;
      android.util.Log.e("XXXXX", "click start button");
    }
  }

  private void play() {

  }

  private void abort() {
    mRecorder.abort();
    dismiss();
  }

  private void delete() {
    mRecorder.backspace();
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
