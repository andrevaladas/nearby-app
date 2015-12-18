package com.chronosystems.nearbyapp.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.chronosystems.nearbyapp.R;

/**
 * Custom Rounded Image View
 *
 * @author andrevaladas
 */
public class RoundedImageView extends ImageView {

    private boolean mPaintBorder;

    public RoundedImageView(Context context) {
        super(context);
    }

    public RoundedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        final TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.RoundedImageView);
        mPaintBorder = array.getBoolean(R.styleable.RoundedImageView_paintBorder, false);

        array.recycle();
    }

    public RoundedImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        Drawable drawable = getDrawable();
        if (drawable == null) {
            return;
        }

        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }
        Bitmap b = ((BitmapDrawable) drawable).getBitmap();
        Bitmap bitmap = b.copy(Bitmap.Config.ARGB_8888, true);

        int w = getWidth(), h = getHeight();

        final Bitmap roundBitmap = getCroppedBitmap(bitmap, w);
        canvas.drawBitmap(roundBitmap, 0, 0, null);

    }

    public Bitmap getCroppedBitmap(Bitmap bmp, int radius) {
        Bitmap sbmp;
        if (bmp.getWidth() != radius || bmp.getHeight() != radius) {
            sbmp = Bitmap.createScaledBitmap(bmp, radius, radius, false);
        } else {
            sbmp = bmp;
        }
        final Bitmap output = Bitmap.createBitmap(sbmp.getWidth(), sbmp.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, sbmp.getWidth(), sbmp.getHeight());

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(Color.parseColor("#BAB399"));
        canvas.drawCircle(sbmp.getWidth() / 2 + 0.7f, sbmp.getHeight() / 2 + 0.7f, sbmp.getWidth() / 2 + 0.1f, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(sbmp, rect, rect, paint);

        final Paint border = getBorder();
        if (border != null) {
            canvas.drawCircle(radius / 2 + 0.7f, radius / 2 + 0.7f, radius / 2 - border.getStrokeWidth() / 2 + 0.1f, border);
        }

        return output;
    }

    protected Paint getBorder() {
        if (mPaintBorder) {
            final Paint stroke = new Paint();
            stroke.setAntiAlias(true);
            stroke.setFilterBitmap(true);
            stroke.setDither(true);
            stroke.setColor(Color.parseColor("#ffffff"));
            stroke.setStyle(Paint.Style.STROKE);
            stroke.setStrokeWidth(4f);
            return stroke;
        }
        return null;
    }
}