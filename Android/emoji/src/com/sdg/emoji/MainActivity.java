package com.sdg.emoji;

import java.io.File;



import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

public class MainActivity extends UnityPlayerActivity {


	private final static int SeletPicture = 100;
	
	private final static int UpdateAlbum = 101;
	
	private final static int TakePhoto = 1;
	
	private final static int LocalAlbum = 2;
	
	private final static int REQUEST_PERMISSIONS = 6;
	
	//ͼƬ�ļ���
	private String fileName = "/CustomEmoji/";
	//ͼƬ����
	private String ImageName = "image.png";
	
	private String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
	
	private File imageFile = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

	///ͳһ����untiy����
	private Handler unityEvent = new Handler(){
		public void handleMessage(Message paramMessage){
			switch (paramMessage.what) {
			case SeletPicture:
				ApplyPermission();
				break;
			case UpdateAlbum:
				UpdateAlbum(paramMessage.obj.toString());
				break;
			default:
				break;
			}
		}
	};
	
	///ͳһ����unity����
	public void GetUntiyMessage(final int type,final Object paramObject){
		this.unityEvent.obtainMessage(type,paramObject).sendToTarget();
	}
	
	//�������
	void UpdateAlbum(String filePath)
	{
        Log.i("test", "------------filePath" + filePath);//��ӡ�����ļ�·����־
        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        scanIntent.setData(Uri.fromFile(new File(filePath)));
        this.sendBroadcast(scanIntent);
        UnityPlayer.UnitySendMessage("SDKCallBackObj","CommonTip","����ɹ�");
	}
	
	void ApplyPermission()
	{
		//6.0��̬�ж�Ȩ��
		if(Build.VERSION.SDK_INT>=23)
		{
			boolean needApply = false;
			
			for(int i=0;i<permissions.length;i++)
			{
                int chechpermission= ContextCompat.checkSelfPermission(getApplicationContext(),
                		permissions[i]);
                if(chechpermission!=PackageManager.PERMISSION_GRANTED)
                {
                	needApply = true;
                	break;
                }
			}
			if(needApply)
			{
				 ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS);
			}
			else
			{
				ShowPickDialog();
			}
		}
		else
		{
			ShowPickDialog();
		}
	}
	
	
	
	/**
     * ѡ����ʾ�Ի���
     */
    private void ShowPickDialog() {
    	int IconId = getResources().getIdentifier("gamelogo","drawable",getPackageName());
    	Log.d("test", "Icon:"+IconId);
    	//DialogFragment
        new AlertDialog.Builder(this)
                .setTitle("ѡ��ͼ��")
                .setIcon(IconId)
                .setNegativeButton("���", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        OpenAlbum();
                    }
                })
                .setPositiveButton("����", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                        OpenCamera();
                    }
                }).show();
    }
    
    //�����
    void OpenAlbum()
    {
        CustomEmojiUtil.openPic(this,LocalAlbum);
    }

    //�����
    void OpenCamera()
    {
   	 	Uri uri = null;
   	 	imageFile = new File(this.getExternalFilesDir(null), ImageName);
        Log.d("test", "openCamera file:"+imageFile.toString());
		if(!imageFile.getParentFile().exists())
		{
			imageFile.getParentFile().mkdirs();
		}
    	//7.0����
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
    	{
    		Log.d("test", "FileProvider 7.0����");
            String authorty = this.getPackageName()+".fileprovider";
            Log.d("test", "authorty:"+authorty);
    	    uri = FileProvider.getUriForFile(this, authorty, imageFile);
    	}
    	//7.0����
    	else
    	{
    		Log.d("test", "fromFile 7.0����");
    		uri = Uri.fromFile(imageFile);
    	}
    	Log.d("test", "openCamera uri:"+uri.getPath());
        CustomEmojiUtil.takePicture(this, uri, TakePhoto);
    }
    
    //��������ص�
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if(resultCode == 0)
		{
    		Log.d("test", resultCode+":resultCode");
    		return;
		}
    	Bitmap bitmap = null;
    	int valueDegree = 0;
    	boolean sizeIsOk = false;
        switch (requestCode) {
        // �����ֱ�Ӵ�����ȡ
        case LocalAlbum:
        	Log.d("test", resultCode+":LocalAlbum");
    		String path = CustomEmojiUtil.getRealPathFromUri(this,data.getData());
    		Log.d("test", "path:"+path);
			bitmap = CustomEmojiUtil.lessenUriImage(path);
			sizeIsOk = CustomEmojiUtil.CheckOutImageSize(bitmap);
			if(sizeIsOk)
			{
				valueDegree = CustomEmojiUtil.readPictureDegree(path);
				if(valueDegree!=0)
				{
					bitmap = CustomEmojiUtil.rotateBitmapByDegree(bitmap,valueDegree);
				}
				CustomEmojiUtil.bitmapToBase64(bitmap);
			}
			else
			{
				UnityPlayer.UnitySendMessage("SDKCallBackObj","CommonTip","��ͼƬ�ֱ��ʲ�֧��");
			}
            break;
        // ����ǵ����������ʱ
        case TakePhoto:
        	Log.d("test", "TakePhoto:"+imageFile.toString());
        	Uri uri = null;
        	//�����ȡ7.0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) 
            {
            	uri = FileProvider.getUriForFile(this,this.getPackageName()+".fileprovider", imageFile);
            	Log.d("test", "7.0+ uri:"+uri.toString());
            }
            else
            {
            	uri = Uri.fromFile(imageFile);
            	Log.d("test", "7.0- uri:"+uri.toString());
            }
        	
    		String photoPath = CustomEmojiUtil.getRealPathFromUri(this,uri);
    	
    		Log.d("test", "path:"+photoPath);
    		if(photoPath==null) {
    			photoPath = imageFile.getPath().toString();
    			Log.d("test", "�����photoPath"+photoPath);
    		}
			bitmap = CustomEmojiUtil.lessenUriImage(photoPath);
			sizeIsOk = CustomEmojiUtil.CheckOutImageSize(bitmap);
			if(sizeIsOk)
			{
				valueDegree = CustomEmojiUtil.readPictureDegree(photoPath);
				if(valueDegree!=0)
				{
					bitmap = CustomEmojiUtil.rotateBitmapByDegree(bitmap,valueDegree);
				}
				CustomEmojiUtil.bitmapToBase64(bitmap);
			}
			else
			{
				UnityPlayer.UnitySendMessage("SDKCallBackObj","CommonTip","��ͼƬ�ֱ��ʲ�֧��");
			}
            break;
        default:
            break;  
        }
        super.onActivityResult(requestCode, resultCode, data);
    }  
    
    //Ȩ�޻ص�
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch(requestCode)
        {
	        case REQUEST_PERMISSIONS:
	        {
	        	int isApply = 0;
	        	for(int i=0;i<grantResults.length;i++){
	                if(grantResults[i]==PackageManager.PERMISSION_GRANTED){
	                	isApply++;
	                }
	            }
	        	if(isApply == grantResults.length)
	        	{
	        		Log.d("test","��ǰ�����Ȩ��ȫ��ͬ����");
	        		ShowPickDialog();
	        	}
	        	else
	        	{
	        		Log.d("test","��Ȩ���û�û��ͳһ");
	        	}
	        	break;
	        }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
