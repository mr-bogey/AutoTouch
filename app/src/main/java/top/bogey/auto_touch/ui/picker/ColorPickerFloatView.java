package top.bogey.auto_touch.ui.picker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.SeekBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import top.bogey.auto_touch.MainAccessibilityService;
import top.bogey.auto_touch.MainApplication;
import top.bogey.auto_touch.R;
import top.bogey.auto_touch.databinding.FloatPickerColorBinding;
import top.bogey.auto_touch.room.bean.node.ColorNode;
import top.bogey.auto_touch.utils.DisplayUtils;
import top.bogey.auto_touch.utils.FloatBaseCallback;
import top.bogey.auto_touch.utils.easy_float.EasyFloat;

@SuppressLint("ViewConstructor")
public class ColorPickerFloatView extends BasePickerFloatView {
    private final ColorNode colorNode;

    private final FloatPickerColorBinding binding;

    private MainAccessibilityService service;

    private final Paint bitmapPaint;
    private Bitmap showBitmap;

    private final Paint markPaint;
    public List<Rect> markArea = new ArrayList<>();
    private boolean isMarked = false;

    private int[] color = new int[3];
    private int size = 1;

    private float lastX, lastY;
    private boolean drag = false;

    public ColorPickerFloatView(Context context, PickerCallback pickerCallback, ColorNode colorNode) {
        super(context, pickerCallback);
        this.colorNode = colorNode;

        floatCallback = new ImagePickerCallback();

        binding = FloatPickerColorBinding.inflate(LayoutInflater.from(context), this, true);

        binding.saveButton.setOnClickListener(v -> {
            pickerCallback.onComplete(this);
            dismiss();
        });

        binding.closeButton.setOnClickListener(v -> {
            isMarked = false;
            markArea.clear();
            refreshUI();
        });

        binding.closeButton.setOnLongClickListener(v -> {
            dismiss();
            return true;
        });

        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                size = progress;
                refreshUI();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        markPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        markPaint.setStyle(Paint.Style.FILL);
        markPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));

        bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bitmapPaint.setFilterBitmap(true);
        bitmapPaint.setDither(true);
    }

    public ColorNode.ColorInfo getColor(){
        return new ColorNode.ColorInfo(color, size);
    }

    public void realShow(int delay){
        postDelayed(() -> {
            EasyFloat.show(tag);
            if (service != null && service.isCaptureEnabled() && service.binder != null){
                Bitmap bitmap = service.binder.getCurrImage();
                if (bitmap != null) {
                    int[] location = new int[2];
                    getLocationOnScreen(location);
                    Point size = DisplayUtils.getScreenSize(getContext());
                    showBitmap = Bitmap.createBitmap(bitmap, location[0], location[1], size.x - location[0], size.y - location[1]);
                    if (colorNode.isValid()){
                        ColorNode.ColorInfo colorInfo = colorNode.getValue();
                        markArea = service.binder.matchColor(showBitmap, colorInfo.getColor());
                        if (markArea != null && markArea.size() > 0) {
                            isMarked = true;
                            color = colorInfo.getColor();
                            Rect rect = markArea.get(0);
                            binding.seekBar.setMax(rect.width() * rect.height());
                            binding.seekBar.setProgress(colorInfo.getSize());
                        }
                    }
                    refreshUI();
                    bitmap.recycle();
                }
            }
        }, delay);
    }

    public void onShow(){
        service = MainApplication.getService();
        if (service != null){
            if (!service.isCaptureEnabled()){
                Toast.makeText(getContext(), R.string.capture_service_on_tips_2, Toast.LENGTH_SHORT).show();
                service.startCaptureService(true, result -> {
                    if (result){
                        realShow(500);
                    }
                });
            } else {
                realShow(100);
            }
        } else {
            EasyFloat.show(tag);
        }
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        if (showBitmap != null && !showBitmap.isRecycled()){
            canvas.drawBitmap(showBitmap, 0, 0, bitmapPaint);
        }
        canvas.saveLayer(getLeft(), getTop(), getRight(), getBottom(), bitmapPaint);
        long drawingTime = getDrawingTime();
        drawChild(canvas, binding.getRoot(), drawingTime);
        for (int i = 0; i < markArea.size(); i++) {
            Rect rect = markArea.get(i);
            if (rect.width() * rect.height() >= size){
                canvas.drawRect(markArea.get(i), markPaint);
            }
        }
        canvas.restore();
        if (isMarked){
            drawChild(canvas, binding.buttonBox, drawingTime);
            drawChild(canvas, binding.barBox, drawingTime);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!isMarked) {
                    lastX = x;
                    lastY = y;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isMarked){
                    float dx = x - lastX;
                    float dy = y - lastY;
                    if (!drag && dx * dx + dy * dy < 81) break;
                    drag = true;
                    lastX = x;
                    lastY = y;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!isMarked && !drag && service.isCaptureEnabled() && service.binder != null){
                    color = DisplayUtils.getHsvColor(showBitmap, (int) lastX, (int) lastY);
                    markArea = service.binder.matchColor(showBitmap, color);
                    if (markArea != null && markArea.size() > 0) {
                        isMarked = true;
                        Rect rect = markArea.get(0);
                        int size = rect.width() * rect.height();
                        binding.seekBar.setMax(size);
                        binding.seekBar.setProgress(size);
                    }
                }
                drag = false;
                break;
        }
        refreshUI();
        return true;
    }

    private void refreshUI(){
        binding.buttonBox.setVisibility(isMarked ? VISIBLE : INVISIBLE);
        binding.barBox.setVisibility(isMarked ? VISIBLE : INVISIBLE);
        binding.numberText.setText(String.valueOf(size));
        postInvalidate();
    }

    protected class ImagePickerCallback extends FloatBaseCallback{
        private boolean first = true;
        @Override
        public void onShow(String tag) {
            if (first){
                super.onShow("");
                ColorPickerFloatView.this.onShow();
                first = false;
            }
        }
    }
}
