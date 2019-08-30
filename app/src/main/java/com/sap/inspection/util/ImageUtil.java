package com.sap.inspection.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.maps.model.LatLng;
import com.sap.inspection.BuildConfig;
import com.sap.inspection.R;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.model.TextMarkModel;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.view.ui.MyApplication;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageUtil {
	public static final int MenuShootImage = 101;

    /*
     * called when image is stored
     */
    public static boolean createTimestampImage(Context context, byte[] data, LatLng currentGeoPoint){

        // Create the <timestamp>.jpg file and modify the exif data
        String filename = "/sdcard"+String.format("/%d.jpg", System.currentTimeMillis());
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(filename);
            try {
                fileOutputStream.write(data);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            fileOutputStream.flush();
            fileOutputStream.close();
            ExifInterface exif = new ExifInterface(filename);
            ExifUtil.createExifData(context, exif, currentGeoPoint.latitude, currentGeoPoint.longitude);
            exif.saveAttributes();
            return true;

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    public static void resizeAndSaveImageCheckExifWithMark(Context ctx, String path, String[] textMarks) throws IOException, NullPointerException {
        //change to 480 from 640
        int x = 640;
        Bitmap bitmap = resizeAndWriteTextOnDrawable(ctx, path, x, textMarks);
        File file = new File(path);
        FileOutputStream out = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
        out.close();
    }

    public static void addWaterMark(Context context, int res, String imagePath) {

        try {

            Bitmap bitmap = stickWatermark(context, res, imagePath);
            File file = new File(imagePath);

            try {

                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
                out.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
            System.gc();

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public static boolean resizeAndSaveImage2(Bitmap bitmap, File file) {
            DebugLog.d("path to save = " + file.getPath());
            try {
                DebugLog.d("saving photo");
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
                out.flush();
                out.close();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
    }

	public static String resizeAndSaveImage(Bitmap bitmap, String url) {
		String physicalPath = null;
        try {
        	Log.d("saving bitmap", "url : "+url);
        	Log.d("saving bitmap", "bitmap : "+bitmap);
            File tempDir;
            if (CommonUtil.isExternalStorageAvailable())
                tempDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/Camera/");
            else
                tempDir = new File(MyApplication.getContext().getFilesDir()+"/Camera/");

            String path;
            if (url.contains("?"))
            	path = tempDir.getAbsolutePath() + File.separator + BuildConfig.FOLDER_TOWER_INSPECTION + File.separator + url.substring(url.lastIndexOf('/')+1,url.indexOf('?'));
            else
            	path = tempDir.getAbsolutePath() + File.separator + BuildConfig.FOLDER_TOWER_INSPECTION + File.separator + url.substring(url.lastIndexOf('/')+1);

            File file;
            file = new File(path);
            DebugLog.d(file.getPath());
            try {
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            bitmap = null;
            physicalPath = path;
            System.gc();

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return physicalPath;
    }
	
	public static Uri takePicture(Activity activity){
        //		Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
		//		startActivityForResult(intent,CAMERA);

		Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
		File photo;
		try
		{
			// place where to store camera taken picture
			photo = createTemporaryFile("picture", ".jpg");
			DebugLog.d("photo url : "+photo.getName());
			photo.delete();
		}
		catch(Exception e)
		{
			DebugLog.d("Can't create file to take picture!");
            Crashlytics.logException(e);
//			Toast.makeText(activity, "Please check SD card! Image shot is impossible!", Toast.LENGTH_SHORT);
			return null;
		}
		Uri mImageUri = Uri.fromFile(photo);
		DebugLog.d("photo uri : "+mImageUri.getPath());
		intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);

		//        intent.putExtra("crop", "true");
        //480 , 360
		intent.putExtra("outputX", 480);
		intent.putExtra("outputY", 480);
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("scale", true);
		intent.putExtra("outputFormat",Bitmap.CompressFormat.JPEG.toString());
		//start camera intent
		//	    activity.startActivityForResult(this, intent, MenuShootImage);
		activity.startActivityForResult(intent, MenuShootImage);
		return mImageUri;
	}

	private static File createTemporaryFile(String part, String ext) throws Exception {
        File tempDir;
        boolean createDirStatus;

        if (CommonUtil.isExternalStorageAvailable())
            tempDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), BuildConfig.FOLDER_CAMERA);
        else
            tempDir = new File(MyApplication.getContext().getFilesDir(), BuildConfig.FOLDER_CAMERA);

		tempDir = new File(tempDir.getAbsolutePath(), BuildConfig.FOLDER_TOWER_INSPECTION);

		if(!tempDir.exists())
		{
            createDirStatus = tempDir.mkdir();
            if (!createDirStatus) {
                createDirStatus = tempDir.mkdirs();
                if (!createDirStatus) {
                    DebugLog.e("fail to create dir");
                    Crashlytics.log("fail to create dir");
                } else {
                    DebugLog.d("create dir success");
                }
            }
		}
		return File.createTempFile(part, ext, tempDir);
	}

    public static Bitmap resizeAndWriteTextOnDrawable(Context mContext, String imagePath, String text) {

	    Bitmap bm = BitmapFactory.decodeFile(imagePath)
                .copy(Bitmap.Config.ARGB_8888, true);

        Typeface tf = Typeface.create("Helvetica", Typeface.BOLD);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTypeface(tf);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(convertToPixels(mContext, 50));

        Rect textRect = new Rect();
        paint.getTextBounds(text, 0, text.length(), textRect);

        Canvas canvas = new Canvas(bm);

        //If the text is bigger than the canvas , reduce the font size
        if(textRect.width() >= (canvas.getWidth() - 4))     //the padding on either sides is considered as 4, so as to appropriately fit in the text
            paint.setTextSize(convertToPixels(mContext, 46));        //Scaling needs to be used for different dpi's

        //Calculate the positions
        int xPos = (canvas.getWidth() / 2) - 2;     //-2 is for regulating the x position offset

        //"- ((paint.descent() + paint.ascent()) / 2)" is the distance from the baseline to the center.
        int yPos = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2)) ;

        canvas.drawText(text, xPos, yPos, paint);

        return bm;
    }

    public static Bitmap resizeAndWriteTextOnDrawable(Context context, String imagePath, int x,  String[] texts) throws NullPointerException {

        if (TextUtils.isEmpty(imagePath) || !new File(imagePath).exists())
            throw new NullPointerException("photo file not found");

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds=true;

	    BitmapFactory.decodeFile(imagePath,options);
        int imageHeight = options.outHeight;
        int imageWidth = options.outWidth;

        float factorH = x / (float)imageHeight;
        float factorW = x / (float)imageWidth;
        float factorToUse = (factorH > factorW) ? factorW : factorH;
        DebugLog.d("factorToUse="+factorToUse);

        int reqWidth = (int) (imageWidth * factorToUse);
        int reqHeight = (int) (imageHeight * factorToUse);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        options.inMutable = true;

        Bitmap bitmap_Result = BitmapFactory.decodeFile(imagePath, options);

        if (bitmap_Result == null)
            throw new NullPointerException("bitmap result is null");

        bitmap_Result = ExifUtil.rotateBitmap(imagePath, bitmap_Result);

        Canvas canvas = new Canvas(bitmap_Result);

        int outputWidth  = bitmap_Result.getWidth();
        int outputHeight = bitmap_Result.getHeight();

        Paint greyPaint = new Paint();
        greyPaint.setColor(context.getResources().getColor(R.color.transparent_gray));


        int height_portrait = PrefUtil.getIntPref(R.string.heightbackgroundwatermarkportrait, Constants.HEIGHT_BACKGROUND_WATERMARK_PORTRAIT);
        int height_landscape = PrefUtil.getIntPref(R.string.heightbackgroundwatermarklandscape, Constants.HEIGHT_BACKGROUND_WATERMARK_LANDSCAPE);

        int top_portrait  = canvas.getHeight() - height_portrait;
        int top_landscape = canvas.getHeight() - height_landscape;
        if (isPortrait(outputWidth, outputHeight)) { // portrait
            canvas.drawRect(0, top_portrait, canvas.getWidth(), canvas.getHeight(), greyPaint);
            DebugLog.d("background rect(left, top, right, bottom) : (" + 0 + ", " + top_portrait + ", " + canvas.getWidth() + ", " + canvas.getHeight() + ")");
        } else {                                    // landscape
            canvas.drawRect(0, top_landscape, canvas.getWidth(), canvas.getHeight(), greyPaint);
            DebugLog.d("background rect(left, top, right, bottom) : (" + 0 + ", " + top_landscape + ", " + canvas.getWidth() + ", " + canvas.getHeight() + ")");
        }

        TextMarkModel textMark = TextMarkModel.getInstance();

        int dy_potrait  = PrefUtil.getIntPref(R.string.linespacepotrait, Constants.TEXT_LINE_SPACE_POTRAIT);
        int dy_landscape = PrefUtil.getIntPref(R.string.linespacelandscape, Constants.TEXT_LINE_SPACE_LANDSCAPE);
        float xPos;
        float yPos;

        for (int i = 0; i < texts.length; i++) {
            textMark.setTextMark(texts[i]);
            Paint textPaint = textMark.generateTextPaint();

            //If the text is bigger than the canvas , reduce the font size;

            float px;
            int textWidth  = textMark.getTextRect().width();
            int textHeight = textMark.getTextRect().height();
            int textSize;

            if (isPortrait(outputWidth, outputHeight)) { // potrait
                textSize = PrefUtil.getIntPref(R.string.textmarksizepotrait, Constants.TEXT_SIZE_POTRAIT);
                px = convertToPixels(context, textSize);

                textPaint.setTextSize(px);

                xPos = convertToPixels(context, 10);
                yPos = (top_portrait + canvas.getHeight() * 7f/100f) + dy_potrait * i;

                DebugLog.d("text size portrait : " + textSize);
                DebugLog.d("line space portrait : " + dy_potrait);

            } else {
                                                        // landscape
                textSize = PrefUtil.getIntPref(R.string.textmarksizelandscape, Constants.TEXT_SIZE_LANDSCAPE);
                px = convertToPixels(context, textSize);

                textPaint.setTextSize(px);
                xPos = convertToPixels(context, 10);
                yPos = (top_landscape + canvas.getHeight() * 10f/100) + dy_landscape * i;

                DebugLog.d("text size landscape : " + textSize);
                DebugLog.d("line space landscape : " + dy_landscape);
            }

            DebugLog.d("text mark width : " + textWidth);
            DebugLog.d("text size in px : " + px);
            DebugLog.d("Canvas width x height : " + canvas.getWidth() + " x " + canvas.getHeight());

            canvas.drawText(textMark.getTextMark(), xPos, yPos, textPaint);
        }

        DebugLog.d("required : width="+reqWidth+" height="+reqHeight);
        DebugLog.d("Bitmap Source : width="+imageWidth+" height="+imageHeight);
        DebugLog.d("Bitmap Result : width="+bitmap_Result.getWidth()+" height="+bitmap_Result.getHeight());
        return bitmap_Result;
    }

    public static Bitmap stickWatermark(Context context, int res, String imagePath) {

        int x = 73;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds=true;

        BitmapFactory.decodeResource(context.getResources(), res, options);
        int imageHeight = options.outHeight;
        int imageWidth = options.outWidth;

        float factorH = x / (float)imageHeight;
        float factorW = x / (float)imageWidth;
        float factorToUse = (factorH > factorW) ? factorW : factorH;
        DebugLog.d("outdimension = " + imageWidth + " x " + imageHeight);
        DebugLog.d("factor H = " + factorH);
        DebugLog.d("factor W = " + factorW);
        DebugLog.d("factorToUse = "+factorToUse);

        int reqWidth = (int) (imageWidth * factorToUse);
        int reqHeight = (int) (imageHeight * factorToUse);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        options.inMutable = true;

        Bitmap watermarkBitmap  = BitmapFactory.decodeResource(context.getResources(), res, options);

        options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inMutable = true;
        Bitmap imageBitmap = BitmapFactory.decodeFile(imagePath, options);

        int widthWaterMark  = watermarkBitmap.getWidth();
        int heightWaterMark = watermarkBitmap.getHeight();

        int widthImage  = imageBitmap.getWidth();
        int heightImage = imageBitmap.getHeight();

        DebugLog.d("required size : " + reqWidth + " x " + reqHeight);
        DebugLog.d("watermark size : " + widthWaterMark + " x " + heightWaterMark);
        DebugLog.d("image size : " + widthImage + " x " + heightImage);

        //float scale = ( heightImage * 0.10f) / heightWaterMark;
        float rectBlackTop;
        float xPos, yPos;
        float leftMargin = convertToPixels(context, 5);
        float bottomMargin = convertToPixels(context, 5);

        /**
         * to determine the y-axis position of watermark, use transparent black rectangle background's top position of the textMark
         * as reference. While for x-axis position, just use parent's left border.
         *
         * */
        if (isPortrait(widthImage, heightImage)) { // Portrait

            rectBlackTop = heightImage * 83f/100f;

        } else { // landscape

            rectBlackTop = heightImage * 60f/100f;
        }

        yPos = rectBlackTop - heightWaterMark - bottomMargin;
        xPos = 0f + leftMargin;

        Paint paintWatermark = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG);
        paintWatermark.setAlpha(50);

        Canvas canvas = new Canvas(imageBitmap);
        canvas.drawBitmap(watermarkBitmap, xPos, yPos, paintWatermark);

        watermarkBitmap.recycle(); // garbage collected

        return imageBitmap;
    }

    public static Bitmap loadDecryptedImage(String imagePath) {

        int x = 640;
        byte[] decryptedBytes = CommonUtil.getDecryptedByteBase64(new File(imagePath));

        if (decryptedBytes != null) {

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds=true;

            BitmapFactory.decodeByteArray(decryptedBytes, 0, decryptedBytes.length, options);
            int imageHeight = options.outHeight;
            int imageWidth = options.outWidth;

            float factorH = x / (float)imageHeight;
            float factorW = x / (float)imageWidth;
            float factorToUse = (factorH > factorW) ? factorW : factorH;
            DebugLog.d("outdimension = " + imageWidth + " x " + imageHeight);
            DebugLog.d("factor H = " + factorH);
            DebugLog.d("factor W = " + factorW);
            DebugLog.d("factorToUse = "+factorToUse);

            int reqWidth = (int) (imageWidth * factorToUse);
            int reqHeight = (int) (imageHeight * factorToUse);

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            options.inJustDecodeBounds = false;
            options.inMutable = true;

            return BitmapFactory.decodeByteArray(decryptedBytes, 0, decryptedBytes.length, options);
        }

        return null;
    }

    public static float imageOrientation(String src) {
        int orientation = 0;
        float degree = 0;
        try {
            orientation = ExifUtil.getExifOrientation(src);
            switch (orientation) {
                case ExifInterface.ORIENTATION_NORMAL:
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 0;
                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                case ExifInterface.ORIENTATION_TRANSPOSE:
                case ExifInterface.ORIENTATION_ROTATE_90:
                case ExifInterface.ORIENTATION_TRANSVERSE:
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 90;
                    break;
                default:
                    degree = 0;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        DebugLog.d("orientation="+orientation);

        return degree;
    }

    public static boolean isPortrait(int width, int height) {
        return width <= height;
    }

    public static int convertToPixels(Context context, int nDP)
    {
        final float conversionScale = context.getResources().getDisplayMetrics().density;
        DebugLog.d("density : " + conversionScale);
        return (int) ((nDP * conversionScale) + 0.5f) ;

    }

    public static float getDPFromPixels(Context context, float pixels) {
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        switch(metrics.densityDpi){
            case DisplayMetrics.DENSITY_LOW:
                pixels = (float) (pixels * 0.75);
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                //pixels = pixels * 1;
                break;
            case DisplayMetrics.DENSITY_HIGH:
                pixels = (float) (pixels * 1.5);
                break;
            case DisplayMetrics.DENSITY_XHIGH:
                pixels = pixels * 2;
                break;
            case DisplayMetrics.DENSITY_XXHIGH:
                pixels = pixels * 3;
                break;
            case DisplayMetrics.DENSITY_XXXHIGH:
                pixels = pixels * 4;
                break;
        }
        return pixels;
    }

    public static double getDPIFromPX(Context context, double px, float factorToUse) {
        final float density = context.getResources().getDisplayMetrics().density;
	    return px / (density * factorToUse);
    }

    public static float getPXFromLineWidth(int lineWidth, float cpl) {
	    final float RATIO = 1.618f;
	    return lineWidth * RATIO / cpl;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
