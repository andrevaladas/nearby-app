package com.chronosystems.nearbyapp.helper;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;

import com.chronosystems.nearbyapp.R;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Class to manipulate images
 * <p>
 * Created by andrevaladas on 09/10/2015.
 */
public class ImageHelper {

    public static String reduceProfileRequest(Context context, Bitmap profile) {

        final Resources resources = context.getResources();
        final int widthPixelSize = resources.getDimensionPixelSize(R.dimen.profile_w);
        final int heightPixelSize = resources.getDimensionPixelSize(R.dimen.profile_h);
        //Log.e("@@@@@@@@@ PROFILE W", "" + widthPixelSize);
        //Log.e("@@@@@@@@@ PROFILE H", "" + heightPixelSize);

        return encodeToBase64(decodeToLowResolution(toArray(profile), widthPixelSize, heightPixelSize));
    }

    public static String reduceProfileRequest(Context context, Drawable drawable) {
        return reduceProfileRequest(context, toBitmap(drawable));
    }

    public static String reduceProfileRequest(Context context, int resource) {
        final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resource);
        return reduceProfileRequest(context, bitmap);
    }

    public static String reduceCoverImageRequest(Context context, Bitmap coverImage) {

        final Resources resources = context.getResources();
        final int widthPixelSize = resources.getDimensionPixelSize(R.dimen.cover_w);
        final int heightPixelSize = resources.getDimensionPixelSize(R.dimen.cover_h);
        //Log.e("@@@@@@@@@ COVER W", "" + widthPixelSize);
        //Log.e("@@@@@@@@@ COVER H", "" + heightPixelSize);

        return encodeToBase64(decodeToLowResolution(toArray(coverImage), widthPixelSize, heightPixelSize));
    }

    public static String reduceCoverImageRequest(Context context, Drawable drawable) {
        return reduceCoverImageRequest(context, toBitmap(drawable));
    }

    public static String reduceCoverImageRequest(Context context, int resource) {
        final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resource);
        return reduceCoverImageRequest(context, bitmap);
    }

    public static Bitmap decodeToLowResolution(final byte[] image, final int width, final int height) {
        try {
            //Decode image size
            final BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new ByteArrayInputStream(image), null, o);

            //The new size we want to scale to
            final int REQUIRED_SIZE_WIDTH = (int) (width * 0.7);
            final int REQUIRED_SIZE_HEIGHT = (int) (height * 0.7);

            //Find the correct scale value. It should be the power of 2.
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE_WIDTH || height_tmp / 2 < REQUIRED_SIZE_HEIGHT) {
                    break;
                }
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            //Decode with inSampleSize
            final BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new ByteArrayInputStream(image), null, o2);
        } catch (final OutOfMemoryError e) {
        }
        return null;
    }

    public static byte[] toArray(Bitmap image) {
        Bitmap immagex = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immagex.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }

    public static String encodeToBase64(Bitmap image) {
        Bitmap immagex = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immagex.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

        //Log.e("ENCODE LENGTH", "" + imageEncoded.length());
        return imageEncoded;
    }

    public static Bitmap decodeFromBase64(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }

    public static Bitmap toBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}