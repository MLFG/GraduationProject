package cn.edu.lin.graduationproject.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.viewpagerindicator.CirclePageIndicator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTouch;
import cn.bmob.im.BmobRecordManager;
import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.im.bean.BmobInvitation;
import cn.bmob.im.bean.BmobMsg;
import cn.bmob.im.inteface.EventListener;
import cn.bmob.im.inteface.OnRecordChangeListener;
import cn.bmob.im.inteface.UploadListener;
import cn.bmob.v3.listener.PushListener;
import cn.edu.lin.graduationproject.R;
import cn.edu.lin.graduationproject.adapter.ChatAdapter;
import cn.edu.lin.graduationproject.adapter.EmoGridViewAdapter;
import cn.edu.lin.graduationproject.adapter.EmoPagerAdapter;
import cn.edu.lin.graduationproject.app.MyApp;
import cn.edu.lin.graduationproject.constant.Constants;
import cn.edu.lin.graduationproject.receiver.MyReceiver;
import cn.edu.lin.graduationproject.util.DialogUtil;
import cn.edu.lin.graduationproject.util.EmoUtil;
import cn.edu.lin.graduationproject.util.NetUtil;
import cn.edu.lin.graduationproject.util.PermissionUtils;

public class ChatActivity extends BaseActivity implements EventListener {

    // 与当前登录用户进行聊天的用户
    // 从 MessageFragment 或从 UserInfoActivity 传递过来的
    BmobChatUser targetUser;
    String targetUsername; // targetUser 的 username
    String targetId;       // targetUser 的 objectId
    String myId;           // 当前登录用户的 objectId
    @BindView(R.id.lv_chat_listview)
    ListView listView;
    List<BmobMsg> messages;
    ChatAdapter adapter;
    @BindView(R.id.et_chat_content)
    EditText etContent;
    @BindView(R.id.btn_chat_add)
    Button btnAdd;
    @BindView(R.id.btn_chat_send)
    Button btnSend;

    // 与表情布局相关的属性
    @BindView(R.id.ll_chat_morelayoutcontainer)
    LinearLayout moreContainer;

    RelativeLayout emoLayout;

    ViewPager emoViewPager;
    CirclePageIndicator emoCpi;
    EmoPagerAdapter emoPagerAdapter;

    // 与发送图片以及位置聊天相关的属性
    LinearLayout addLayout;
    String cameraPath;
    // 与语音聊天消息相关的内容
    @BindView(R.id.ll_chat_textinputcontainer)
    LinearLayout textinputContainer;
    @BindView(R.id.ll_chat_voiceinputcontainer)
    LinearLayout voiceinputContainer;
    @BindView(R.id.ll_chat_voicecontainer)
    LinearLayout voiceContainer;
    @BindView(R.id.iv_chat_voicevolumn)
    ImageView ivVoiceVolum;
    @BindView(R.id.tv_chat_voicetip)
    TextView tvVoiceTip;
    @BindView(R.id.btn_chat_speak)
    Button btnSpeak;

    int[] volumImages;// 录音时表示音量大小的图片

    // 录音工具类
    BmobRecordManager recordManager;

    PermissionUtils permissionUtils;

    InputMethodManager imm ; // 软键盘管理类

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setMyContentView() {
        setContentView(R.layout.activity_chat);
    }

    @Override
    public void init() {
        super.init();
        targetUser = (BmobChatUser) getIntent().getSerializableExtra("user");
        targetUsername = targetUser.getUsername();
        targetId = targetUser.getObjectId();
        myId = userManager.getCurrentUserObjectId();
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        initHeaderView();
        initView();
        initListView();
    }

    private void initHeaderView(){
        setHeaderTitle(targetUsername, Constants.Position.CENTER);
        setHeaderImage(Constants.Position.LEFT, R.drawable.back_arrow_2, false, v -> finish());
    }

    private void initView(){
        permissionUtils = new PermissionUtils(this);
        initContentInput();
        initEmoLayout();
        initAddLayout();
        initVoiceLayout();
    }

    private void initVoiceLayout(){
        volumImages = new int[]{
                R.drawable.chat_icon_voice1,
                R.drawable.chat_icon_voice2,
                R.drawable.chat_icon_voice3,
                R.drawable.chat_icon_voice4,
                R.drawable.chat_icon_voice5,
                R.drawable.chat_icon_voice6
        };
        recordManager = BmobRecordManager.getInstance(this);
        recordManager.setOnRecordChangeListener(new OnRecordChangeListener() {
            @Override
            public void onVolumnChanged(int i) {
                // 监听到录音过程中音量发生变化
                // 根据传入的表示音量大小的 i ，来选择图片进行显示
                ivVoiceVolum.setImageResource(volumImages[i]);
            }

            @Override
            public void onTimeChanged(int i, String s) {
                // 监听到录音过程中时间发生变化（单位：秒）
                // i 表示此时一共录制的时长
                // s 表示此时录制的语音文件在 SD 上的存储路径
                if(i>=60){
                    // 停止录音，将已经录制好的 60 秒的语音发送出去
                    btnSpeak.setEnabled(false);
                    btnSpeak.setClickable(false);
                    btnSpeak.setPressed(false);
                    voiceContainer.setVisibility(View.INVISIBLE);
                    recordManager.stopRecording();
                    sendVoiceMessage(i,s);
                    new Handler().postDelayed(() -> {
                        btnSpeak.setEnabled(true);
                        btnSpeak.setClickable(true);
                    },1000);
                }
            }
        });
    }

    /**
     * 发送一条语音类型的聊天消息
     * @param value     语音消息的时长
     * @param localPath 语音小时在 SD 卡上的存储位置
     */
    protected void sendVoiceMessage(int value,String localPath){
        if(!NetUtil.isNetworkAvailable(this)){
            toast("当前网络不给力量");
            return;
        }
        chatManager.sendVoiceMessage(targetUser, localPath, value, new UploadListener() {
            /**
             * 在语音聊天消息发送之前，会根据传入的语音文件的路径和长度
             * 构建一个 BmobMsg 对象，此时该 BmobMsg 对象的属性为
             *  tag ""
             *  content 语音文件的路径 & 长度
             *  msgType 4
             *  status 0
             *  isreaded 0
             *  调用监听器的 onStart 将该 BmobMsg 对象传入
             * @param bmobMsg
             */
            @Override
            public void onStart(BmobMsg bmobMsg) {
                adapter.add(bmobMsg);
                listView.setSelection(adapter.getCount() - 1);
            }

            /**
             * 语音文件已经发送完毕
             * 在调用 onStart 时创建的 BmobMsg 对象已经被保存到本地数据库的 chat 表中
             * 通过调用 refresh 方法刷新 ListView
             * 此时 chat 表中 BmobMsg 对象所对应的数据记录的部分字段值与 BmobMsg 对象创建之初的属性发生了变化
             * content 语音文件的路径 & 长度 & 语音文件在服务器上的地址
             * status 1
             * isreaded 1
             */
            @Override
            public void onSuccess() {
                refresh();
            }

            @Override
            public void onFailure(int i, String s) {
                // toastAndLog("语音类型聊天消息发送失败",i,s);
            }
        });
    }

    /**
     * 初始化 addLayout
     */
    private void initAddLayout(){
        addLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.add_layout,moreContainer,false);
        TextView tvPicture = (TextView) addLayout.findViewById(R.id.tv_addlayout_picture);
        tvPicture.setOnClickListener(v -> permissionUtils.setPermissions(PermissionUtils.READ, grant -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");
            startActivityForResult(intent,101);
        }));

        TextView tvCamera = (TextView) addLayout.findViewById(R.id.tv_addlayout_camera);
        tvCamera.setOnClickListener(v -> permissionUtils.setPermissions(PermissionUtils.CAMERA, grant -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), System.currentTimeMillis()+".jpg");
            cameraPath = file.getAbsolutePath();
            Uri imgUri = Uri.fromFile(file);
            intent.putExtra(MediaStore.EXTRA_OUTPUT,imgUri);
            startActivityForResult(intent,102);
        }));

        TextView tvLocation = (TextView) addLayout.findViewById(R.id.tv_addlayout_location);
        tvLocation.setOnClickListener(v -> permissionUtils.setPermissions(PermissionUtils.LOCATION, grant -> {
            // 跳转到地图界面进行定位
            Intent intent = new Intent(ChatActivity.this,LocationActivity.class);
            intent.putExtra("from","mylocation");
            intent.putExtra("lat",MyApp.lastPoint.getLatitude());
            intent.putExtra("lng",MyApp.lastPoint.getLongitude());
            startActivityForResult(intent,103);
        }));
    }

    /**
     * 初始化 emoLayout
     */
    private void initEmoLayout(){
        emoLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.emo_layout,moreContainer,false);
        emoViewPager = (ViewPager) emoLayout.findViewById(R.id.vp_emolayout_viewpager);
        emoCpi = (CirclePageIndicator) emoLayout.findViewById(R.id.cpi_emolayout_indicator);

        List<View> views = new ArrayList<>();
        // 向 views 中增加显示表情的视图
        // views 中包含几个 view 来显示表情，取决于表情的总数量
        // view 个数：表情总数量 % 21 == 0 ? 表情总数量 / 21 : 表情总数量 / 21 + 1
        int pageno = EmoUtil.emos.size() % 21 == 0 ? EmoUtil.emos.size() / 21 : EmoUtil.emos.size() / 21 + 1;
        for(int i = 0 ; i < pageno ; i++){
            View view = getLayoutInflater().inflate(R.layout.emo_gridview_layout,emoViewPager,false);
            GridView gridView = (GridView) view.findViewById(R.id.gv_emogridview);
            // 数据源，所有表情是在 EmoUtil.emos 取若干个表情
            int startPos = 21 * i;
            int endPos = Math.min(21*(i+1),EmoUtil.emos.size());
            List<String> list = EmoUtil.emos.subList(startPos,endPos);
            // 适配器
            final EmoGridViewAdapter emoGridViewAdapter = new EmoGridViewAdapter(this,list);
            gridView.setAdapter(emoGridViewAdapter);
            gridView.setOnItemClickListener((parent, view1, position, id) -> {
                String resName = emoGridViewAdapter.getItem(position);
                etContent.append(EmoUtil.getSpannableString(resName));
            });
            views.add(view);
        }
        emoPagerAdapter = new EmoPagerAdapter(views);
        emoViewPager.setAdapter(emoPagerAdapter);
        emoCpi.setViewPager(emoViewPager);
        emoCpi.setFillColor(Color.DKGRAY);
    }

    private void initContentInput(){
        // etContent 添加一个监听器，监听 etContent 的内容变化
        etContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() > 0){
                    // 一旦 etContent 中有内容，btnAdd 不可见，benSend 可见
                    btnAdd.setVisibility(View.INVISIBLE);
                    btnSend.setVisibility(View.VISIBLE);
                }else{
                    // 一旦 etContent 中有内容，btnAdd 可见，btnSend 不可见
                    btnAdd.setVisibility(View.VISIBLE);
                    btnSend.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void initListView(){
        messages = new ArrayList<>();
        adapter = new ChatAdapter(this,messages);
        listView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyReceiver.regist(this);
        refresh();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MyReceiver.unRegist(this);
    }

    private void refresh(){
        // 获取 ListView 真正的数据源，当前登录用户所对应数据库的 chat 数据表
        List<BmobMsg> list = bmobDB.queryMessages(targetId,0);
        adapter.addAll(list,true);
        // 让 ListView 滚到最后一条数据进行呈现
        listView.setSelection(adapter.getCount() - 1);
    }

    @OnClick(R.id.btn_chat_send)
    public void sendTextMessage(View view){
        String content = etContent.getText().toString();
        if(!NetUtil.isNetworkAvailable(this)){
            toast("当前网络不给力");
            return;
        }
        // 文本类型的聊天消息 msg
        // tag      ""
        // content  传入的 content 内容
        // msgType  1
        // isreaded 0 未读
        // status   1
        final BmobMsg msg = BmobMsg.createTextSendMsg(this,targetId,content);
        // 1.去服务器 _user 表中查找 targetUser 用户
        // 2.如果有，就根据 msg 对象创建一个 JsonObject 对象，然后利用 BmobPushManager 进行推送
        //   推送时的设备 ID 是 targetUser 最后一次登录时所使用的设备 ID
        // 3.推送完毕后，将 msg 对象保存到服务器的 BmobMsg 数据表中，此时这条数据记录的 isreaded 值为0
        // 4.将 msg 的 isreaded 属性值从 0 更新到 1，开始保存到本地数据库的 chat 表和 recent 表中
        // 5.保存完毕后，调用自己写的监听器的相应方法
        // 注意：第 3 步中，在推送完毕后，保存到服务器之前，先后两次设定 msg 对象的 status 为 1
        chatManager.sendTextMessage(targetUser, msg, new PushListener() {
            @Override
            public void onSuccess() {
                adapter.add(msg);
                listView.setSelection(adapter.getCount() - 1);
                etContent.setText("");
            }

            @Override
            public void onFailure(int i, String s) {
                toastAndLog("发送失败，稍后重试",i,s);
            }
        });
    }

    @OnClick(R.id.btn_chat_emo)
    public void addEmoLayout(View view){
        if(imm.isActive()) {
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
        if(moreContainer.getChildCount() > 0 ){
            // moreContainer 有子视图
            if(moreContainer.getChildAt(0) == addLayout){
                moreContainer.removeAllViews();
                moreContainer.addView(emoLayout);
            }else{
                moreContainer.removeAllViews();
            }
        }else{
            // moreContainer 中没有子视图
            moreContainer.addView(emoLayout);
        }
    }

    @OnClick(R.id.btn_chat_add)
    public void addAddLayout(View view){
        if(imm.isActive()) {
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
        if(moreContainer.getChildCount() > 0){
            if(moreContainer.getChildAt(0) == emoLayout){
                moreContainer.removeAllViews();
                moreContainer.addView(addLayout);
            }else{
                moreContainer.removeAllViews();
            }
        }else{
            moreContainer.addView(addLayout);
            if(voiceinputContainer.getVisibility() == View.VISIBLE){
                textinputContainer.setVisibility(View.VISIBLE);
                voiceinputContainer.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode ==  RESULT_OK){
            switch (requestCode){
                case 101:
                    // 图库选图
                    Uri uri = data.getData();
                    Cursor cursor = getContentResolver().query(uri,new String[]{MediaStore.Images.Media.DATA},null,null,null);
                    cursor.moveToNext();
                    String filePath = cursor.getString(0);
                    cursor.close();
                    sendImageMessage(filePath);
                    break;
                case 102:
                    // 相机拍照
                    sendImageMessage(cameraPath);
                    break;
                case 103:
                    // 定位返回结果
                    String address = data.getStringExtra("address");
                    String localFilePath = data.getStringExtra("localFilePath");
                    String url = data.getStringExtra("url");
                    sendLocationMessage(MyApp.lastPoint.getLatitude(),MyApp.lastPoint.getLongitude(),address,url,localFilePath);
                    break;
            }
        }
    }

    /**
     * 发送位置类型的聊天信息
     * @param lat       纬度
     * @param lng       经度
     * @param address   地址
     * @param url       截图的网络地址
     * @param localFilePath 截图的本地地址
     */
    private void sendLocationMessage(double lat,double lng,String address,String url,String localFilePath){
        if(!NetUtil.isNetworkAvailable(this)){
            toast("当前网络不给力");
            return;
        }
        /**
         * 创建 位置类型聊天信息 指定了 msg 对象 msgType 为 3
         * 然后将地址，纬度，经度，拼接为地址 & 纬度 & 经度大字符串后
         * 剩下的步骤均与发送文本类型的聊天消息一致
         * 经过修改后得到的 msg 的 content 就是：地址 & 本地截图地址 & 截图网络地址 & 纬度 …… 经度
         */
        final BmobMsg msg = BmobMsg.createLocationSendMsg(this,targetId,address+"&"+localFilePath+"&"+url,lat,lng);
        chatManager.sendTextMessage(targetUser, msg, new PushListener() {
            @Override
            public void onSuccess() {
                adapter.add(msg);
                listView.setSelection(adapter.getCount() - 1);
            }

            @Override
            public void onFailure(int i, String s) {
                toastAndLog("发送位置信息失败,稍后重试",i,s);
            }
        });
    }

    /**
     * 发送图像类型的聊天信息
     * @param filePath  图像在 SD 卡上的存储地址
     */
    private void sendImageMessage(String filePath){
        if(!NetUtil.isNetworkAvailable(this)){
            toast("当前网络不给力");
            return;
        }
        /**
         * sendImageMessage:
         *  1.创建 BmobMsg 对象
         *      tag ""
         *      content file:///+filePath
         *      status  0
         *      msgType 2
         *      isreaded    0
         *  2.调用 UploadListener onStart 方法，将第一步创建出来的 BmobMsg 对象作为参数传入
         *  3.上传本地图片（filePath所对应的图片）到服务器
         *  4.将图片在服务器上的长地址转化为短地址
         *  5.把第一步创建的 BmobMsg 对象的 content 属性，从file:///+filePath更改为了第四步得到的网络地址
         *  6.真正发送聊天信息（当接收方收到这条图像类型的聊天消息时，其 content 属性值就是图片在服务器上的一个地址）
         *    推送成功后，将第一步 msg 对象 status 属性值从 0 -- 1
         *    将 msg 对象保存到服务器的 BmobMsg 数据表，isreaded 0
         *    将 msg 对象的 isreaded 字段值从 0 改为 1 后，保存到本地数据库的 chat 表和 recent 表
         *  7.当聊天消息发送完毕后，又将第一步创建的 BmobMsg 对象的 content 属性从网络地址改为了 file:///+filePath&网络地址
         *    同时修改了 isreaded 属性值从 1 改为了 2，然后将该消息保存到本地数据库的 chat 表
         *    因为在第 6 步中，该 BmobMsg 对象已经被保存过了，因此第 7 步再次做保存时并不会覆盖原有的数据，仅仅会更新原有数据的三个字段
         *    content status belongavatar 字段，并不会更改 isreaded 字段
         *    所以，经过第 7 步保存后的数据记录中 content file:///+filePath&网络地址 但是 isreaded 字段值依然维持1，并未被更改
         *  8.调用 UploadListener onSuccess 方法
         */
        chatManager.sendImageMessage(targetUser, filePath, new UploadListener() {
            @Override
            public void onStart(BmobMsg bmobMsg) {
                // 此时，ListView 中新增了内容，但是 msg 还未发送
                adapter.add(bmobMsg);
                listView.setSelection(adapter.getCount() - 1);
            }

            /**
             * 此时，onStart 方法调用时所传入的 BmobMsg 对象已经被保存到了本地数据库的 chat 表
             * 但是部分属性值已经发生了改变
             * content file:///+filePath&图像在服务器的地址
             * status 1
             * isreaded 1
             * 让发送图像类型消息条目中 ProgressBar 隐藏
             */
            @Override
            public void onSuccess() {
                refresh();
            }

            @Override
            public void onFailure(int i, String s) {
                // refresh();
                // toastAndLog("图像发送失败，稍后重试",i,s);
            }
        });
    }

    @OnClick(R.id.btn_chat_voice)
    public void showVoiceInputContainer(View view){
        textinputContainer.setVisibility(View.INVISIBLE);
        voiceinputContainer.setVisibility(View.VISIBLE);
        moreContainer.removeAllViews();
        btnAdd.setVisibility(View.VISIBLE);
        btnSend.setVisibility(View.INVISIBLE);
    }

    @OnClick(R.id.btn_chat_keyboard)
    public void showTextInputContainer(View view){
        textinputContainer.setVisibility(View.VISIBLE);
        voiceinputContainer.setVisibility(View.INVISIBLE);
    }

    @OnTouch(R.id.btn_chat_speak)
    public boolean speak(View view, MotionEvent event){
        permissionUtils.setPermissions(PermissionUtils.AUDIO,grant -> {
            int action = event.getAction();
            switch (action){
                case MotionEvent.ACTION_DOWN:
                    // 录音开始
                    voiceContainer.setVisibility(View.VISIBLE);
                    recordManager.startRecording(targetId);
                    break;
                case MotionEvent.ACTION_MOVE:
                    btnSpeak.setPressed(true);
                    float y = event.getY();
                    if(y<0){
                        // 手指在按钮之外
                        tvVoiceTip.setText("松开手指，取消发送");
                    }else{
                        // 手指在按钮之内
                        tvVoiceTip.setText("手指上画，取消发送");
                    }
                    break;
                default:
                    // 录音结束
                    btnSpeak.setPressed(false);
                    voiceContainer.setVisibility(View.INVISIBLE);
                    if(event.getY() < 0){
                        // 在按钮之外抬起的手指
                        // 应该取消录制的内容
                        recordManager.cancelRecording();
                    }else{
                        // 将录制的内容作为语音类型的聊天消息发送出去
                        int value = recordManager.stopRecording();
                        String localPath = recordManager.getRecordFilePath(targetId);
                        sendVoiceMessage(value,localPath);
                    }
                    break;
            }
        });
        return true;
    }

    @Override
    public void onMessage(BmobMsg bmobMsg) {
        // 作为订阅这，将 MyReceiver 收到并保存的聊天消息
        // 放到 ListView 中呈现
        if(bmobMsg.getBelongId().equals(targetId)){
            adapter.add(bmobMsg);
            listView.setSelection(adapter.getCount() - 1);
            /**
             * 在 ListView 中呈现的消息都是 已读
             * 所以，每呈现一条消息都要去修改 chat 表中
             * 该 bmobMsg 对象 isreaded 字段值 （2 -- 1）
             */
            bmobDB.resetUnread(targetId);
        }
    }

    @Override
    public void onReaded(String s, String s1) {

    }

    @Override
    public void onNetChange(boolean b) {

    }

    @Override
    public void onAddUser(BmobInvitation bmobInvitation) {

    }

    @Override
    public void onOffline() {
        // 当收到下线通知时候，该方法会被 MyReceiver 调用
        DialogUtil.showDialog(this, "下线通知", "检测到您的帐号在另一台设备登录，请您重新登录!", false, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MyApp.logout();
            }
        });
    }
}
