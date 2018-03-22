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
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.sap.inspection.MyApplication;
import com.sap.inspection.R;
import com.sap.inspection.model.TextMarkDisplayOptionsModel;
import com.sap.inspection.model.TextMarkModel;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.tools.ExifUtil;

import java.io.File;
import java.io.FileOutputStream;

public class ImageUtil {
	public static final int MenuShootImage = 101;
	
	public static void resizeAndSaveImage(String imageUri) {
        try {

            BitmapFactory.Options options = new BitmapFactory.Options();

            options.inSampleSize = 4;
            File tempDir;
            if (Utility.isExternalStorageAvailable())
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
            if (Utility.isExternalStorageAvailable())
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
            if (Utility.isExternalStorageAvailable()) {
                DebugLog.d("external storage available");
                tempDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/Camera/");
            } else {
                DebugLog.d("external storage not available");
                tempDir = new File(ctx.getFilesDir()+"/Camera/");
            }
            String path = tempDir.getAbsolutePath()+"/TowerInspection/"+scheduleId+"/"+imageUri.substring(imageUri.lastIndexOf('/'));

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds=true;
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
            if (Utility.isExternalStorageAvailable()) {
                DebugLog.d("external storage available");
                tempDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/Camera/");
            } else {
                DebugLog.d("external storage not available");
                tempDir = new File(ctx.getFilesDir()+"/Camera/");
            }
            String path = tempDir.getAbsolutePath()+"/TowerInspection/"+scheduleId+"/"+imageUri.substring(imageUri.lastIndexOf('/'));

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds=true;
            BitmapFactory.decodeFile(path,options);
            int imageHeight = options.outHeight;
            int imageWidth = options.outWidth;

            float factorH = x / (float)imageHeight;
            float factorW = x / (float)imageWidth;
            float factorToUse = (factorH > factorW) ? factorW : factorH;
            DebugLog.d("factorToUse="+factorToUse);
            Bitmap bitmap_Source = writeTextOnDrawable(ctx, path, textMarks);
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
            if (Utility.isExternalStorageAvailable())
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
        if (Utility.isExternalStorageAvailable())
            tempDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/Camera/");
        else
            tempDir = new File(MyApplication.getContext().getFilesDir()+"/Camera/");

		tempDir=new File(tempDir.getAbsolutePath()+"/TowerInspection/");
		if(!tempDir.exists())
		{
			tempDir.mkdir();
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

    public static Bitmap writeTextOnDrawable(Context mContext, String imagePath, String[] texts) {

        Bitmap bm = BitmapFactory.decodeFile(imagePath)
                .copy(Bitmap.Config.ARGB_8888, true);

        Canvas canvas = new Canvas(bm);

        Paint greyPaint = new Paint();
        greyPaint.setColor(mContext.getResources().getColor(R.color.transparent_gray));
        canvas.drawRect(0, canvas.getHeight() * 3 / 4, canvas.getWidth(), canvas.getHeight(), greyPaint);

        TextMarkModel textMark = TextMarkModel.getInstance();

        for (int i = 0; i < texts.length; i++) {
            textMark.setTextMark(texts[i]);
            Paint textPaint = textMark.generateTextPaint();


            //If the text is bigger than the canvas , reduce the font size
            if(textMark.getTextRect().width() >= (canvas.getWidth() - 4))     //the padding on either sides is considered as 4, so as to appropriately fit in the text
                textPaint.setTextSize(convertToPixels(mContext, 36));        //Scaling needs to be used for different dpi's
            DebugLog.d("Canvas width : " + canvas.getWidth());
            //Calculate the positions
            int xPos = convertToPixels(mContext, canvas.getWidth() / 50);    //-2 is for regulating the x position offset

            //"- ((paint.descent() + paint.ascent()) / 2)" is the distance from the baseline to the center.
            /*int yPos = (int) ((canvas.getHeight() / 2) - ((textPaint.descent() + textPaint.ascent()) / 2));*/
            int yPos = (int) ((canvas.getHeight() * 13/16) + convertToPixels(mContext,i * 60));
            //canvas.translate(xPos, yPos);
            canvas.drawText(textMark.getTextMark(), xPos, yPos, textPaint);
        }
        return bm;
    }

    public static int convertToPixels(Context context, int nDP)
    {
        final float conversionScale = context.getResources().getDisplayMetrics().density;

        return (int) ((nDP * conversionScale) + 0.5f) ;

    }
}
