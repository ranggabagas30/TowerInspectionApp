package com.sap.inspection.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.sap.inspection.MyApplication;
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
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
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
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
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
            options.inSampleSize = 4;
            Bitmap bitmap = BitmapFactory.decodeFile(path,options);

            bitmap = ExifUtil.rotateBitmap(path,bitmap);

            File file;
            file = new File(path);
            DebugLog.d(file.getPath());
            try {
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
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
            DebugLog.d("path to save = "+file.getPath());
            try {
                DebugLog.d("saving photo");
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
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
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
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
		intent.putExtra("outputX", 1080);
		intent.putExtra("outputY", 720);
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
}
