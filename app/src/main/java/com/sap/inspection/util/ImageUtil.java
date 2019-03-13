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
import android.util.DisplayMetrics;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.sap.inspection.MyApplication;
import com.sap.inspection.R;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.model.TextMarkModel;
import com.sap.inspection.tools.DebugLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageUtil {
	public static final int MenuShootImage = 101;
	
	public static void resizeAndSaveImage(String imageUri) {
        try {

            BitmapFactory.Options options = new BitmapFactory.Options();

            options.inSampleSize = 4;
            File tempDir;
            if (CommonUtil.isExternalStorageAvailable())
                tempDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/Camera/");
            else
                tempDir = new File(MyApplication.getContext().getFilesDir()+"/Camera/");

            String path = tempDir.getAbsolutePath()+"/TowerInspection/"+imageUri.substring(imageUri.lastIndexOf('/'));

            Bitmap bitmap = BitmapFactory.decodeFile(path,options);

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
            System.gc();

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
	
	public static File resizeAndSaveImage(String imageUri, String scheduleId) {
        File fileReturn = null;
        try {
            File tempDir;
            if (CommonUtil.isExternalStorageAvailable())
                tempDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/Camera/");
            else
                tempDir = new File(MyApplication.getContext().getFilesDir()+"/Camera/");

            String path = tempDir.getAbsolutePath()+"/TowerInspection/"+scheduleId+"/"+imageUri.substring(imageUri.lastIndexOf('/'));

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            Bitmap bitmap = BitmapFactory.decodeFile(path,options);

            File file;
            file = new File(path);
            DebugLog.d(file.getPath());
            try {
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
                out.flush();
                out.close();
                fileReturn = file;
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            bitmap = null;
            System.gc();

        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        return fileReturn;
    }

    public static File resizeAndSaveImageCheckExif(Context ctx, String imageUri, String scheduleId) {
        File fileReturn = null;
        File tempDir;
        //change to 480 from 640
        int x = 640;

        try {

            // determine image source path
            if (CommonUtil.isExternalStorageAvailable()) {
                DebugLog.d("external storage available");
                tempDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/Camera/");
            } else {
                DebugLog.d("external storage not available");
                tempDir = new File(ctx.getFilesDir()+"/Camera/");
            }
            String path = tempDir.getAbsolutePath()+"/TowerInspection/"+scheduleId+"/"+imageUri.substring(imageUri.lastIndexOf('/'));

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds=true; // avoid memory allocation

            BitmapFactory.decodeFile(path,options);
            int imageHeight = options.outHeight;
            int imageWidth = options.outWidth;

            float factorH = x / (float)imageHeight;
            float factorW = x / (float)imageWidth;
            float factorToUse = (factorH > factorW) ? factorW : factorH;

            DebugLog.d("factorToUse="+factorToUse);
            Bitmap bitmap_Source = BitmapFactory.decodeFile(path);
            Bitmap bitmap = Bitmap.createScaledBitmap(bitmap_Source,
                    (int) (imageWidth * factorToUse),
                    (int) (imageHeight * factorToUse),
                    false);

//            options.inSampleSize = 4;
//            Bitmap bitmap = BitmapFactory.decodeFile(path,options);

            bitmap = ExifUtil.rotateBitmap(path,bitmap);
            DebugLog.d("width="+bitmap.getWidth()+" height="+bitmap.getHeight());


            File file;
            file = new File(path);
            DebugLog.d(file.getPath());
            try {
                FileOutputStream out = new FileOutputStream(file);
                //kualitas
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
                out.flush();
                out.close();
                fileReturn = file;
            } catch (Exception e) {
                e.printStackTrace();
            }

            bitmap = null;
            System.gc();

        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        return fileReturn;
    }

    public static File resizeAndSaveImageCheckExifWithMark(Context ctx, String imageUri, String scheduleId, String[] textMarks) {
        File fileReturn = null;
        File tempDir;
        //change to 480 from 640
        int x = 640;

        try {
            if (CommonUtil.isExternalStorageAvailable()) {
                DebugLog.d("external storage available");
                tempDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/Camera/");
            } else {
                DebugLog.d("external storage not available");
                tempDir = new File(ctx.getFilesDir()+"/Camera/");
            }

            //String path = tempDir.getAbsolutePath()+"/TowerInspection/"+scheduleId+"/"+imageUri.substring(imageUri.lastIndexOf('/'));
            String path = tempDir.getAbsolutePath()+"/TowerInspection/"+scheduleId+"/"+ imageUri;
            DebugLog.d("image uri : " + imageUri);
            DebugLog.d("path of image : " + path);

            Bitmap bitmap = writeTextOnDrawable(ctx, path, x, textMarks);

            File file = new File(path);
            DebugLog.d("file path : " + file.getPath());
            try {
                FileOutputStream out = new FileOutputStream(file);
                //kualitas
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
                out.flush();
                out.close();
                fileReturn = file;
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.gc();

        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        return fileReturn;
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

            String path = null;
            if (url.contains("?"))
            	path = tempDir.getAbsolutePath()+"/TowerInspection/"+url.substring(url.lastIndexOf('/')+1,url.indexOf('?'));
            else
            	path = tempDir.getAbsolutePath()+"/TowerInspection/"+url.substring(url.lastIndexOf('/')+1);

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

	private static File createTemporaryFile(String part, String ext) throws Exception
	{
        File tempDir;
        boolean createDirStatus;

        if (CommonUtil.isExternalStorageAvailable())
            tempDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/Camera/");
        else
            tempDir = new File(MyApplication.getContext().getFilesDir()+"/Camera/");

		tempDir=new File(tempDir.getAbsolutePath()+"/TowerInspection/");
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

    public static Bitmap writeTextOnDrawable(Context mContext, String imagePath, String text) {

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

    public static Bitmap writeTextOnDrawable(Context mContext, String imagePath, int x,  String[] texts) {

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

        /*bitmap_Result = Bitmap.createScaledBitmap(bitmap_Result,
                (int) (imageWidth * factorToUse),
                (int) (imageHeight * factorToUse),
                false);*/
        /*
        * legacy
        * */
        //Bitmap bitmap_Source = BitmapFactory.decodeFile(imagePath);

        /*Bitmap bitmap_Result = Bitmap.createScaledBitmap(bitmap_Source,
                (int) (imageWidth * factorToUse),
                (int) (imageHeight * factorToUse),
                false);*/

        float bitmapRotation = imageOrientation(imagePath);

        bitmap_Result = ExifUtil.rotateBitmap(imagePath,bitmap_Result);
        Canvas canvas = new Canvas(bitmap_Result);

        int outputWidth  = bitmap_Result.getWidth();
        int outputHeight = bitmap_Result.getHeight();

        Paint greyPaint = new Paint();
        greyPaint.setColor(mContext.getResources().getColor(R.color.transparent_gray));

        if (isPortrait(outputWidth, outputHeight)) { // portrait
            DebugLog.d("bitmapRotation : " + bitmapRotation);
            canvas.drawRect(0, canvas.getHeight() * 83/100, canvas.getWidth(), canvas.getHeight(), greyPaint);
        } else {
                                                      // landscape
            DebugLog.d("bitmapRotation : " + bitmapRotation);
            canvas.drawRect(0, canvas.getHeight() * 60/100, canvas.getWidth(), canvas.getHeight(), greyPaint);
        }

        TextMarkModel textMark = TextMarkModel.getInstance();

        int dy_potrait  = PrefUtil.getIntPref(R.string.linespacepotrait, Constants.TEXT_LINE_SPACE_POTRAIT);
        int dy_landscape = PrefUtil.getIntPref(R.string.linespacelandscape, Constants.TEXT_LINE_SPACE_LANDSCAPE);
        float xPos = 0.0f;
        float yPos = 0.0f;

        for (int i = 0; i < texts.length; i++) {
            textMark.setTextMark(texts[i]);
            Paint textPaint = textMark.generateTextPaint();

            //If the text is bigger than the canvas , reduce the font size;

            float px = 0.0f;
            int textWidth  = textMark.getTextRect().width();
            int textHeight = textMark.getTextRect().height();
            int textSize;

            if (isPortrait(outputWidth, outputHeight)) { // potrait
                textSize = PrefUtil.getIntPref(R.string.textmarksizepotrait, Constants.TEXT_SIZE_POTRAIT);
                px = convertToPixels(mContext, textSize);

                textPaint.setTextSize(px);

                xPos = convertToPixels(mContext, 10);
                //yPos = (canvas.getHeight() * 83/100) + convertToPixels(mContext, textHeight) + dy_potrait * i;
                yPos = (canvas.getHeight() * 90/100) + dy_potrait * i;

                DebugLog.d("text size portrait : " + textSize);
                DebugLog.d("line space portrait : " + dy_potrait);

            } else {
                                                        // landscape
                textSize = PrefUtil.getIntPref(R.string.textmarksizelandscape, Constants.TEXT_SIZE_LANDSCAPE);
                px = convertToPixels(mContext, textSize);

                textPaint.setTextSize(px);
                xPos = convertToPixels(mContext, 10);
                //yPos = (canvas.getHeight() * 70/100) + convertToPixels(mContext, textHeight) + dy_potrait * i;
                yPos = (canvas.getHeight() * 70/100) + dy_landscape * i;

                DebugLog.d("text size landscape : " + textSize);
                DebugLog.d("line space landscape : " + dy_landscape);
            }

            DebugLog.d("text mark width : " + textWidth);
            DebugLog.d("px : " + px);
            DebugLog.d("Canvas width x height : " + canvas.getWidth() + " x " + canvas.getHeight());

            canvas.drawText(textMark.getTextMark(), xPos, yPos, textPaint);


        }

        DebugLog.d("required : width="+reqWidth+" height="+reqHeight);
        DebugLog.d("Bitmap Source : width="+imageWidth+" height="+imageHeight);
        DebugLog.d("Bitmap Result : width="+bitmap_Result.getWidth()+" height="+bitmap_Result.getHeight());
        return bitmap_Result;
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
	    if (width <= height) {
	        //MyApplication.getInstance().toast("portrait", Toast.LENGTH_SHORT);
            return true;
        } else {
            //MyApplication.getInstance().toast("landscape", Toast.LENGTH_SHORT);
            return false;
        }
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
                pixels = (float) (pixels * 2);
                break;
            case DisplayMetrics.DENSITY_XXHIGH:
                pixels = (float) (pixels * 3);
                break;
            case DisplayMetrics.DENSITY_XXXHIGH:
                pixels = (float) (pixels * 4);
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

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
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
