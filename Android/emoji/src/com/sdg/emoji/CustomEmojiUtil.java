package com.sdg.emoji;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.unity3d.player.UnityPlayer;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;



public class CustomEmojiUtil 
{
	

    /**
     * @param activity    ��ǰactivity
     * @param imageUri    ���պ���Ƭ�洢·��
     * @param requestCode ����ϵͳ���������
     */
    public static void takePicture(Activity activity, Uri imageUri, int requestCode) { //����ϵͳ���
        Intent intentCamera = new Intent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { 
        	intentCamera.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //�����һ���ʾ��Ŀ��Ӧ����ʱ��Ȩ��Uri��������ļ�
        } 
        intentCamera.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        //�����ս��������photo_file��Uri�У��������������
        intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        activity.startActivityForResult(intentCamera, requestCode);
    }
     /**
     * @param activity    ��ǰactivity
     * @param requestCode ������������
     */
    public static void openPic(Activity activity, int requestCode) {
    	Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        photoPickerIntent.setType("image/*");
        activity.startActivityForResult(photoPickerIntent, requestCode);
    }

	//��ȡͼƬѹ����
	public static int GetBitMapSampleSize(int reqWidth, int reqHeight)
	{
		float maxLength = Math.max(reqWidth, reqHeight);
		int be = 1;
		if(maxLength>1024)
		{
			be = (int)(maxLength / (float)1024); 
		}
		return be;
	}
	
    //ѹ��ͼƬ
    public static Bitmap lessenUriImage(String path){ 
		BitmapFactory.Options options = new BitmapFactory.Options(); 
		options.inJustDecodeBounds = true; 
		Bitmap bitmap = BitmapFactory.decodeFile(path, options); //��ʱ���� bm Ϊ�� 
		options.inJustDecodeBounds = false; //���űȡ������ǹ̶��������ţ�ֻ�ø߻��߿�����һ�����ݽ��м��㼴��
		int be = CustomEmojiUtil.GetBitMapSampleSize(options.outWidth,options.outHeight);
		Log.d("test", be+":be");
		options.inSampleSize = be; //���¶���ͼƬ��ע���ʱ�Ѿ��� options.inJustDecodeBounds ��� false �� 
		
		bitmap=BitmapFactory.decodeFile(path,options); 
		int w = bitmap.getWidth(); 
		int h = bitmap.getHeight(); 
		System.out.println(w+" "+h); //after zoom
		return bitmap;
	}
	/**
	 * ����Uri��ȡͼƬ�ľ���·��
	 *
	 * @param context �����Ķ���
	 * @param uri     ͼƬ��Uri
	 * @return ���Uri��Ӧ��ͼƬ����, ��ô���ظ�ͼƬ�ľ���·��, ���򷵻�null
	 */
	public static String getRealPathFromUri(Context context, Uri uri) {
	    int sdkVersion = Build.VERSION.SDK_INT;
	    Log.d("test","sdkVersion:"+sdkVersion );
	    if (sdkVersion >= 19) { // api >= 19
	        return getRealPathFromUriAboveApi19(context, uri);
	    } else { // api < 19
	        return getRealPathFromUriBelowAPI19(context, uri);
	    }
	}
	
	/**
     * ����api19������,����uri��ȡͼƬ�ľ���·��
     *
     * @param context �����Ķ���
     * @param uri     ͼƬ��Uri
     * @return ���Uri��Ӧ��ͼƬ����, ��ô���ظ�ͼƬ�ľ���·��, ���򷵻�null
     */
    @SuppressLint("NewApi")
    private static String getRealPathFromUriAboveApi19(Context context, Uri uri) {
        String filePath = null;
        Log.d("test", "context:"+context+"    uri:"+uri);
        if (DocumentsContract.isDocumentUri(context, uri)) {
        	 Log.d("test", "DocumentsContract");
            // �����document���͵� uri, ��ͨ��document id�����д���
            String documentId = DocumentsContract.getDocumentId(uri);
            if (isMediaDocument(uri)) { // MediaProvider
                // ʹ��':'�ָ�
                String id = documentId.split(":")[1];

                String selection = MediaStore.Images.Media._ID + "=?";
                String[] selectionArgs = {id};
                filePath = getDataColumn(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection, selectionArgs);
            } else if (isDownloadsDocument(uri)) { // DownloadsProvider
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(documentId));
                filePath = getDataColumn(context, contentUri, null, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())){
        	 Log.d("test", "content");
            // ����� content ���͵� Uri
            filePath = getDataColumn(context, uri, null, null);
        } else if ("file".equals(uri.getScheme())) {
        	 Log.d("test", "file");
            // ����� file ���͵� Uri,ֱ�ӻ�ȡͼƬ��Ӧ��·��
            filePath = uri.getPath();
        }
        return filePath;
    }
    
    /**
     * @param uri the Uri to check
     * @return Whether the Uri authority is MediaProvider
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
    
    /**
     * @param uri the Uri to check
     * @return Whether the Uri authority is DownloadsProvider
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }
	
	/**
     * ����api19����(������api19),����uri��ȡͼƬ�ľ���·��
     *
     * @param context �����Ķ���
     * @param uri     ͼƬ��Uri
     * @return ���Uri��Ӧ��ͼƬ����, ��ô���ظ�ͼƬ�ľ���·��, ���򷵻�null
     */
    private static String getRealPathFromUriBelowAPI19(Context context, Uri uri) {
        return getDataColumn(context, uri, null, null);
    }
    
    /**
     * ��ȡ���ݿ���е� _data �У�������Uri��Ӧ���ļ�·��
     * @return
     */
    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        String path = null;
        Log.d("test", "getDataColumn");
        String[] projection = new String[]{MediaStore.Images.Media.DATA};
        Cursor cursor = null;
        try {
        	Log.d("test","try:"+context +"   context.getContentResolver():"+context.getContentResolver());
        	
            cursor = context.getContentResolver().query(uri, null, selection, selectionArgs, null);
            Log.d("test", "cursor:"+cursor+"  cursor.moveToFirst():"+cursor.moveToFirst()+"  projection[0]:"+projection[0]+"  cursor.getColumnIndexOrThrow(projection[0]):"+cursor.getColumnIndexOrThrow(projection[0]));
            
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(projection[0]);
                path = cursor.getString(columnIndex);
            }
        } catch (Exception e) {
        	Log.d("test", e.toString());
            if (cursor != null) {
                cursor.close();
            }
        }
        return path;
    }
    
    /**
     * ��ȡͼƬ���ԣ���ת�ĽǶ�
     *
     * @param path ͼƬ����·��
     * @return degree��ת�ĽǶ�
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }
    /**
     * ��ͼƬ����ĳ���ǶȽ�����ת
     *
     * @param bm
     *            ��Ҫ��ת��ͼƬ
     * @param degree
     *            ��ת�Ƕ�
     * @return ��ת���ͼƬ
     */
    public static Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
        Bitmap returnBm = null;
 
        // ������ת�Ƕȣ�������ת����
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        try {
            // ��ԭʼͼƬ������ת���������ת�����õ��µ�ͼƬ
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
                    bm.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
        }
        if (returnBm == null) {
            returnBm = bm;
        }
        if (bm != returnBm) {
            bm.recycle();
        }
        return returnBm;
    }
    
    ///����ͼƬ��ɳ���ļ�
	public static void SaveBitmap(Context context, Bitmap bitmap,String fileName,String ImageName) throws IOException {
		Log.d("test", "SaveBitmap");
		FileOutputStream fOut = null;
		String packageName = context.getPackageName();
		Log.d("test", "packageName:"+packageName);
		//ע��1
		String path = "/mnt/sdcard/Android/data/"+context.getPackageName()+"/files"+fileName;
		Log.d("test", "path��"+path);
		try 
		{
		  File destDir = new File(path);
		  if (!destDir.exists())
		  {
			  destDir.mkdirs();
		  }
		  fOut = new FileOutputStream(path + ImageName);
		  //��Bitmap����д�뱾��·���У�Unity��ȥ��ͬ��·������ȡ����ļ�
		  bitmap.compress(Bitmap.CompressFormat.PNG, 50, fOut);
		  UnityPlayer.UnitySendMessage("SDKCallBackObj","MessageCallBack",ImageName);
		  Log.d("test", "UnitySendMessage");
		  try 
		  {
			  fOut.flush();
		  } 
		  catch (IOException e) 
		  {
			  e.printStackTrace();
		  }
		  
		  try 
		  {
			  fOut.close();
			  bitmap.recycle();	// ����bitmap���ڴ�
			  bitmap = null;
		  } 
		  catch (IOException e) 
		  {
			  e.printStackTrace();
		  }
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
	}
	
	 /**
     * bitmapתΪbase64
     *
     * @param bitmap
     * @return
     */ public static void bitmapToBase64(Bitmap bitmap)
     {
    	 String result = null; 
    	 ByteArrayOutputStream baos = null;
    	 try 
    	 { 
    		 if (bitmap != null)
    		 { 
	    		 baos = new ByteArrayOutputStream();
	    		 bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
	    		 baos.flush(); baos.close(); byte[] bitmapBytes = baos.toByteArray();
	    		 result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT); 
    		 } 
    	 }
    	 catch (IOException e)
    	 { 
    		 e.printStackTrace(); 
    	 }
    	 finally 
    	 { 
    		 try
    		 { 
    			 if (baos != null)
			 	 {
    				 baos.flush();
    				 baos.close();
				 }
    		 } 
    		 catch (IOException e) 
    		 { 
    			 e.printStackTrace();
    		 } 
    	 }
    	 	UnityPlayer.UnitySendMessage("SDKCallBackObj","GetBase64",result);
    }
     
     ///���ͼƬ�ߴ�
     public static boolean CheckOutImageSize(Bitmap bitmap)
     {
    	 if(bitmap == null) return false;
    	 float w = bitmap.getWidth();
    	 float h = bitmap.getHeight();
    	 float min = Math.min(w, h);
    	 if(min<50)
    	 {
    		 Log.d("test","min::"+min);
    		 return false;
    	 }
    	 float value = w / h;
    	 if(value < 1/3f || value > 67/10f)
    	 {
    		 Log.d("test","value < 1/3f || value > 67/10f::"+value );
    		 return false;
    	 }
    	 return true;
     }
}