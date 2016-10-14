package cn.fcrj.pickphoto;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class GetPhotoActivity extends AppCompatActivity implements View.OnClickListener{
    private Button btn1, btn2, btn3;
    private ImageView image;
    private String capturePath = "";
    private View contentview;
    private PopupWindow popupWindow = null;
    private static final int PHONE_PHOTO = 0;//相册
    private static final int TAKE_PHOTO = 1;//相机
    private static final int RESULT_PHOTO = 2;//剪切完毕
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_photo);

        image = (ImageView) findViewById(R.id.image);
        btn1 = (Button) findViewById(R.id.btn_1);
        btn2 = (Button) findViewById(R.id.btn_2);
        btn3 = (Button) findViewById(R.id.btn_3);
        btn1.setOnClickListener(this);
        contentview = this.getLayoutInflater().inflate(R.layout.config_userinfo_face_popwindow_layout, null);
        //相机点击
        contentview.findViewById(R.id.btn_take_photo).setOnClickListener(this);
        //相册点击
        contentview.findViewById(R.id.btn_phone_photo).setOnClickListener(this);
        //取消点击
        contentview.findViewById(R.id.btn_cancel).setOnClickListener(this);
    }
    //设置照片样式
    public void startPhoneZoom(Uri uri){
        Intent intent=new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri,"image/*");
        //设置裁剪
        intent.putExtra("crop","true");
        //设置宽度，高度比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY",1);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 400);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, RESULT_PHOTO);
    }
    //设置照片传送的格式
    public String convertBitmap(Bitmap bitmap) {
        ByteArrayOutputStream bos=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        try {
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] bytes=bos.toByteArray();
        byte[] enCode= Base64.encode(bytes, Base64.DEFAULT);
        return new String(enCode);
    }
    //跳出选项框
    public PopupWindow getPopwindow(View view) {
        PopupWindow popupWindow = new PopupWindow(view,
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.alpha = 0.6f;
        getWindow().setAttributes(layoutParams);
        popupWindow.setBackgroundDrawable(new ColorDrawable());
//        popupWindow.showAtLocation(findViewById(R.id.image), Gravity.BOTTOM, 0, 0);
//        popupWindow.showAsDropDown(btn1);
        popupWindow.setAnimationStyle(R.style.anim_menu_bottombar);
        popupWindow.setAnimationStyle(android.R.style.Animation_InputMethod);
        popupWindow.update();
        popupWindow.setTouchable(true);

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
                layoutParams.alpha = 1f;
                getWindow().setAttributes(layoutParams);
            }
        });
        return popupWindow;
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_1:
                popupWindow = getPopwindow(contentview);
                popupWindow.showAsDropDown(btn1);
                break;
            //相机
            case R.id.btn_take_photo:
                String state = Environment.getExternalStorageState();
                if (state.equals(Environment.MEDIA_MOUNTED)) {
                    Intent getImageByCamera = new Intent("android.media.action.IMAGE_CAPTURE");
                    File parent= FileUitlity.getInstance(getApplicationContext()).makeDir("head_img");
                    capturePath = parent.getPath()+File.separatorChar + System.currentTimeMillis() + ".jpg";
                    getImageByCamera.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(capturePath)));
                    getImageByCamera.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                    startActivityForResult(getImageByCamera, TAKE_PHOTO);
                }
                else {
                    Toast.makeText(getApplicationContext(), "请确认已经插入SD卡", Toast.LENGTH_LONG).show();
                }
                break;
            //相册
            case R.id.btn_phone_photo:
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PHONE_PHOTO);
                break;
            //取消
            case R.id.btn_cancel:
                popupWindow.dismiss();
                break;
            default:

                break;
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != Activity.RESULT_OK){
            return;
        }
        switch (requestCode){
            case PHONE_PHOTO:
                Cursor cursor = this.getContentResolver().query(data.getData(),
                        new String[]{ MediaStore.Images.Media.DATA },
                        null, null, null);
                cursor.moveToFirst();
                capturePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                cursor.close();
                startPhoneZoom(Uri.fromFile(new File(capturePath)));
                break;
            case TAKE_PHOTO:
                startPhoneZoom(Uri.fromFile(new File(capturePath)));
                break;
            case RESULT_PHOTO:
                Bundle bundle = data.getExtras();
                if(bundle!=null){
                    Bitmap bitmap = bundle.getParcelable("data");
                    image.setImageBitmap(bitmap);
                    alertDialogShow(bitmap);
                }
                break;
        }
        popupWindow.dismiss();
    }
    //上传照片
    public void alertDialogShow(final Bitmap bitmap1){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        View view= LayoutInflater.from(this).inflate(R.layout.diy_dialog_1, null);
        builder.setView(view);
        final AlertDialog ad=builder.create();
        view.findViewById(R.id.PositiveButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //上传图片
                ad.dismiss();

                uploadPic(convertBitmap(bitmap1));
            }
        });
        view.findViewById(R.id.NegativeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ad.dismiss();
            }
        });
        TextView title=(TextView) view.findViewById(R.id.Title);
        title.setText("提示");
        TextView subtitle=(TextView) view.findViewById(R.id.SubTitle);
        subtitle.setText("是否上传此头像？");
        ad.show();
    }

    private void uploadPic(String s) {


        Log.d("=====", "uploadPic: "+capturePath);
        Log.d("=====", "uploadPic: "+s);
//        SocketService service = new SocketService(s, s, s, 1,1)
    }
}
