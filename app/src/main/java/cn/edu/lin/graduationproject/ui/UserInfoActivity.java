package cn.edu.lin.graduationproject.ui;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;
import cn.edu.lin.graduationproject.R;
import cn.edu.lin.graduationproject.bean.MyUser;
import cn.edu.lin.graduationproject.constant.Constants;
import cn.edu.lin.graduationproject.view.CircleImageView;

public class UserInfoActivity extends BaseActivity {

    @BindView(R.id.civ_uesrinfo_avatar)
    CircleImageView ivAvatar;
    @BindView(R.id.iv_userinfo_avatareditor)
    ImageView ivAvatarEditor;

    @BindView(R.id.tv_userinfo_nickname)
    TextView tvNickname;
    @BindView(R.id.iv_userinfo_nicknameeditor)
    ImageView ivNicknameEditor;

    @BindView(R.id.et_userinfo_nickname)
    EditText etNickname;
    @BindView(R.id.ib_userinfo_nicknameconfirm)
    ImageButton ibConfirm;
    @BindView(R.id.ib_userinfo_nicknamecancel)
    ImageButton ibCancel;

    @BindView(R.id.ll_userinfo_shownicknamecontainer)
    LinearLayout llShownicknameContainer;
    @BindView(R.id.ll_userinfo_editnicknamecontainer)
    LinearLayout llEditnicknameContainer;

    @BindView(R.id.tv_userinfo_username)
    TextView tvUsername;

    @BindView(R.id.iv_userinfo_gender)
    ImageView ivGender;

    @BindView(R.id.btn_userinfo_update)
    Button btnUpdate;
    @BindView(R.id.btn_userinfo_chat)
    Button btnChat;
    @BindView(R.id.btn_userinfo_black)
    Button btnBlack;

    String from;    // 标识从哪一个界面跳转过来 friend、me、stragner
    String name;
    MyUser user;    // 根据 name 从 _user 表中查询得到的 MyUser 对象

    String avatarUrl;   // 上传头像完毕后的网络地址
    String cameraPath;  // 相机拍摄头像图片的地址

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setMyContentView() {
        setContentView(R.layout.activity_user_info);
    }

    @Override
    public void init() {
        super.init();
        from = getIntent().getStringExtra("from");
        if("me".equals(from)){
            name = userManager.getCurrentUserName();
        }else{
            name = getIntent().getStringExtra("name");
        }

        initHeaderView();
        initView();
    }

    private void initHeaderView(){
        String title = "";
        if("me".equals(from)){
            title = "我的资料";
        }else if("friend".equals(from)){
            title = "好友资料";
        }else{
            title = name;
        }
        setHeaderTitle(title);
        setHeaderImage(Constants.Position.LEFT, R.drawable.back_arrow_2, true, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initView(){
        // 根据 name 的值，查询 name 所对应用户的相关资料
        BmobQuery<MyUser> query = new BmobQuery<>();
        query.addWhereEqualTo("username",name);
        query.findObjects(this, new FindListener<MyUser>() {
            @Override
            public void onSuccess(List<MyUser> list) {
                user = list.get(0);
                // 显示头像
                String avatar = user.getAvatar();
                if(TextUtils.isEmpty(avatar)){
                    ivAvatar.setImageResource(R.drawable.ic_launcher);
                }else{
                    ImageLoader.getInstance().displayImage(avatar,ivAvatar);
                }
                // 是否显示编辑头像的铅笔
                if("me".equals(from)){
                    ivAvatarEditor.setVisibility(View.VISIBLE);
                }else{
                    ivAvatarEditor.setVisibility(View.INVISIBLE);
                }

                llShownicknameContainer.setVisibility(View.VISIBLE);
                llEditnicknameContainer.setVisibility(View.INVISIBLE);

                if("me".equals(from)){
                    ivNicknameEditor.setVisibility(View.VISIBLE);
                }else{
                    ivNicknameEditor.setVisibility(View.INVISIBLE);
                }

                tvNickname.setText(user.getNick());
                tvUsername.setText(name);

                ivGender.setImageResource(user.getGender()?R.drawable.boy:R.drawable.girl);

                if("me".equals(from)){
                    btnUpdate.setVisibility(View.VISIBLE);
                }else{
                    btnUpdate.setVisibility(View.GONE);
                }

                if("friend".equals(from)){
                    btnChat.setVisibility(View.VISIBLE);
                    btnBlack.setVisibility(View.VISIBLE);
                }else{
                    btnChat.setVisibility(View.INVISIBLE);
                    btnBlack.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onError(int i, String s) {
                toastAndLog("查询资料失败，稍后重试",i,s);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @OnClick(R.id.iv_userinfo_avatareditor)
    public void setAvatar(View view){
        Intent intent1 = new Intent(Intent.ACTION_PICK);
        intent1.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");

        Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),System.currentTimeMillis()+".jpg");
        cameraPath = file.getAbsolutePath();
        Uri uri = Uri.fromFile(file);
        intent2.putExtra(MediaStore.EXTRA_OUTPUT,uri);

        Intent chooser = Intent.createChooser(intent1,"选择头像...");
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS,new Intent[]{intent2});
        startActivityForResult(chooser,101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try{
            super.onActivityResult(requestCode,resultCode,data);
            if(resultCode == RESULT_OK){
                if(requestCode == 101){
                    String filePath = "";
                    if(data != null){
                        // 头像图片是从图库选择
                        Uri uri = data.getData();
                        Cursor cursor = getContentResolver().query(uri,new String[]{MediaStore.Images.Media.DATA},null,null,null);
                        cursor.moveToNext();
                        filePath = cursor.getString(0);
                        cursor.close();
                    }else{
                        // 相机拍照
                        filePath = cameraPath;
                    }

                    crop(filePath);
                }

                if(requestCode == 102){
                    // 获得了系统截图程序返回的截取后的图片
                    final Bitmap bitmap = data.getParcelableExtra("data");
                    // 上传前，要将 bitmap 保存到 SD 卡
                    // 获得保存路径后，再上传
                    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),System.currentTimeMillis()+".jpg");
                    OutputStream stream = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);
                    final BmobFile bf = new BmobFile(file);
                    bf.uploadblock(this, new UploadFileListener() {
                        @Override
                        public void onSuccess() {
                            avatarUrl = bf.getFileUrl(UserInfoActivity.this);
                            log("avatarUrl:"+avatarUrl);
                            ivAvatar.setImageBitmap(bitmap);
                        }

                        @Override
                        public void onFailure(int i, String s) {
                            toastAndLog("头像上传失败稍后重试",i,s);
                        }
                    });
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 调用安卓的图片剪裁程序对用户选择的头像进行剪裁
     *
     * @param filePath 用户选取的头像在 SD 上的地址
     */
    private void crop(String filePath){
        // 隐式 intent
        Intent intent = new Intent("com.android.camera.action.CROP");
        Uri data = Uri.fromFile(new File(filePath));

        intent.setDataAndType(data,"image/*");
        intent.putExtra("crop",true);
        intent.putExtra("return-data",true);
        intent.putExtra("aspectX",1);
        intent.putExtra("aspectY",1);
        intent.putExtra("outputX",150);
        intent.putExtra("outputY",150);

        startActivityForResult(intent,102);
    }

    @OnClick(R.id.iv_userinfo_nicknameeditor)
    public void setNickname(View view){
        llShownicknameContainer.setVisibility(View.INVISIBLE);
        llEditnicknameContainer.setVisibility(View.VISIBLE);
        String nickname = tvNickname.getText().toString();
        if(TextUtils.isEmpty(nickname)){
            etNickname.setHint("请输入您的昵称...");
        }else{
            etNickname.setText(nickname);
        }
    }

    @OnClick(R.id.ib_userinfo_nicknameconfirm)
    public void confirmNickname(View view){
        // 确认昵称编辑
        String nickname = etNickname.getText().toString();
        etNickname.setText("");
        tvNickname.setText(nickname);
        llEditnicknameContainer.setVisibility(View.INVISIBLE);
        llShownicknameContainer.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.ib_userinfo_nicknamecancel)
    public void cancelNickname(View view){
        // 取消昵称编辑
        etNickname.setText("");
        llEditnicknameContainer.setVisibility(View.INVISIBLE);
        llShownicknameContainer.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.btn_userinfo_update)
    public void update(View view){
        // 更新用户资料
        if(avatarUrl != null){
            user.setAvatar(avatarUrl);
        }
        user.setNick(tvNickname.getText().toString());
        user.update(this, new UpdateListener() {
            @Override
            public void onSuccess() {
                toast("资料更新成功");
            }

            @Override
            public void onFailure(int i, String s) {
                toastAndLog("更新用户资料失败，稍后重试",i,s);
            }
        });
    }

    @OnClick(R.id.btn_userinfo_chat)
    public void chat(View view){
        Intent intent = new Intent(this,ChatActivity.class);
        intent.putExtra("user",user);
        jumpTo(intent,true);
    }
}
