package cn.edu.lin.graduationproject.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import cn.bmob.im.BmobUserManager;
import cn.bmob.im.bean.BmobMsg;
import cn.edu.lin.graduationproject.R;
import cn.edu.lin.graduationproject.constant.Constants;
import cn.edu.lin.graduationproject.ui.LocationActivity;
import cn.edu.lin.graduationproject.util.EmoUtil;
import cn.edu.lin.graduationproject.util.TimeUtil;
import cn.edu.lin.graduationproject.view.CircleImageView;

/**
 * Created by liminglin on 17-3-1.
 */

public class ChatAdapter extends MyBaseAdapter<BmobMsg> {

    private static final int RECEIVE_TEXT_MSG = 0;
    private static final int SEND_TEXT_MSG = 1;
    private static final int RECEIVE_IMAGE_MSG = 2;
    private static final int SEND_IMAGE_MSG = 3;
    private static final int RECEIVE_LOC_MSG = 4;
    private static final int SEND_LOC_MSG = 5;
    private static final int RECEIVE_VOICE_MSG = 6;
    private static final int SEND_VOICE_MSG = 7;
    private static final String TAG = "ChatAdapter";
    public ChatAdapter(Context context, List<BmobMsg> dataSource) {
        super(context, dataSource);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final ViewHolder viewHolder;
        BmobMsg msg = getItem(position);
        int msgType = msg.getMsgType();
        int itemType = getItemViewType(position);
        if(convertView == null){
            switch (msgType){
                case 1:
                    if(itemType == RECEIVE_TEXT_MSG){
                        convertView = inflater.inflate(R.layout.item_chat_text_left,parent,false);
                    }else{
                        convertView = inflater.inflate(R.layout.item_chat_text_right,parent,false);
                    }
                    viewHolder = new ViewHolder();
                    viewHolder.tvTime = (TextView) convertView.findViewById(R.id.tv_item_chat_time);
                    viewHolder.ivAvatar = (CircleImageView) convertView.findViewById(R.id.iv_item_chat_avatar);
                    viewHolder.tvContent = (TextView) convertView.findViewById(R.id.tv_item_chat_content);
                    convertView.setTag(viewHolder);
                    break;
                case 2:
                    if(itemType == RECEIVE_IMAGE_MSG){
                        convertView = inflater.inflate(R.layout.item_chat_image_left,parent,false);
                    }else{
                        convertView = inflater.inflate(R.layout.item_chat_image_right,parent,false);
                    }
                    viewHolder = new ViewHolder();
                    viewHolder.tvTime = (TextView) convertView.findViewById(R.id.tv_item_chat_time);
                    viewHolder.ivAvatar = (CircleImageView) convertView.findViewById(R.id.iv_item_chat_avatar);
                    viewHolder.ivContent = (ImageView) convertView.findViewById(R.id.iv_item_chat_content);
                    // 为 viewHolder.pbSending 赋值时，若 convertView是从 item_chat_image_left得到，则 viewHolder.pbSending 为 null
                    viewHolder.pbSending = (ProgressBar) convertView.findViewById(R.id.pb_item_chat_sending);
                    convertView.setTag(viewHolder);
                    break;
                case 3:
                    if(itemType == RECEIVE_LOC_MSG){
                        convertView = inflater.inflate(R.layout.item_chat_location_left,parent,false);
                    }else{
                        convertView = inflater.inflate(R.layout.item_chat_location_right,parent,false);
                    }
                    viewHolder = new ViewHolder();
                    viewHolder.tvTime = (TextView) convertView.findViewById(R.id.tv_item_chat_time);
                    viewHolder.ivAvatar = (CircleImageView) convertView.findViewById(R.id.iv_item_chat_avatar);
                    viewHolder.ivContent = (ImageView) convertView.findViewById(R.id.iv_item_chat_content);
                    viewHolder.tvContent = (TextView) convertView.findViewById(R.id.tv_item_chat_content);
                    convertView.setTag(viewHolder);
                    break;
                case 4:
                    if(itemType == RECEIVE_VOICE_MSG){
                        convertView = inflater.inflate(R.layout.item_chat_voice_left,parent,false);
                    }else{
                        convertView = inflater.inflate(R.layout.item_chat_voice_right,parent,false);
                    }

                    viewHolder = new ViewHolder();
                    viewHolder.tvTime = (TextView) convertView.findViewById(R.id.tv_item_chat_time);
                    viewHolder.ivAvatar = (CircleImageView) convertView.findViewById(R.id.iv_item_chat_avatar);
                    viewHolder.ivContent = (ImageView) convertView.findViewById(R.id.iv_item_chat_content);
                    viewHolder.tvContent = (TextView) convertView.findViewById(R.id.tv_item_chat_content);
                    viewHolder.pbSending = (ProgressBar) convertView.findViewById(R.id.pb_item_chat_sending);
                    break;
                default:
                    throw new RuntimeException("不正确的消息格式.");
            }
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.tvTime.setText(TimeUtil.getTime(Long.parseLong(msg.getMsgTime())*1000));
        setAvatar(msg.getBelongAvatar(),viewHolder.ivAvatar);
        switch(msgType){
            case 1:
                viewHolder.tvContent.setText(EmoUtil.getSpannableString(msg.getContent()));
                break;
            case 2:
                String imageUrl = msg.getContent();
                // 对于接收方来说：网址
                // 对于发送方来说：onStart 时 file:///filePath
                //              onSuccess 时 file:///filePath&网址
                if(imageUrl.contains("&")){
                    // 从 file:///filePath&网址取出 filePath
                    String address = imageUrl.split("&")[0];
                    address = address.split("///")[1];
                    Bitmap bm = BitmapFactory.decodeFile(address);
                    viewHolder.ivContent.setImageBitmap(bm);
                }else{
                    if(getItemViewType(position) == RECEIVE_IMAGE_MSG){
                        setAvatar(imageUrl,viewHolder.ivContent);
                    }else{
                        String address = imageUrl.split("///")[1];
                        Bitmap bm = BitmapFactory.decodeFile(address);
                        viewHolder.ivContent.setImageBitmap(bm);
                    }
                }
                if(viewHolder.pbSending!=null){
                    if(msg.getStatus() == 0){
                        viewHolder.pbSending.setVisibility(View.VISIBLE);
                    }else{
                        viewHolder.pbSending.setVisibility(View.INVISIBLE);
                    }
                }
                break;
            case 3:
                // 地址 & 图片地址 & 图片网络地址 & 纬度 & 经度
                String info = msg.getContent();
                final String[] infos = info.split("&");
                viewHolder.tvContent.setText(infos[0]);
                if(getItemViewType(position) == SEND_LOC_MSG){
                    // 发送出去的位置类型聊天信息
                    // 显示地图截图时使用本地地址
                    Bitmap bm = BitmapFactory.decodeFile(infos[1]);
                    viewHolder.ivContent.setImageBitmap(bm);
                }else{
                    // 接收到的位置类型聊天信息
                    // 显示地图截图时使用网络地址
                    setAvatar(infos[2],viewHolder.ivContent);
                }
                viewHolder.ivContent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 跳转到 LocationActivity，在百度地图上显示条目中的地址
                        Intent intent = new Intent(context, LocationActivity.class);
                        intent.putExtra("from","showaddress");
                        intent.putExtra("address",infos[2]);
                        intent.putExtra("lat", Double.parseDouble(infos[3]));
                        intent.putExtra("lng",Double.parseDouble(infos[4]));
                        context.startActivity(intent);
                    }
                });
                break;
            case 4:
                // 如果接收到的是语音消息，语音文件服务器地址 & 语音文件的长度
                // 如果是发送的语音消息
                // 第一次刷新：语音文件本地地址 & 语音文件的长度
                // 第二次刷新：语音文件本地地址 & 语音文件服务器地址 & 语音文件的长度
                final String voiceInfo = msg.getContent();
                if(getItemViewType(position) == RECEIVE_VOICE_MSG){
                    viewHolder.tvContent.setText(voiceInfo.split("&")[1]+"'");
                }else{
                    String[] voiceInfos = voiceInfo.split("&");
                    if(voiceInfos.length == 2){
                        viewHolder.tvContent.setText(voiceInfos[1] + "'");
                    }else{
                        viewHolder.tvContent.setText(voiceInfos[2] + "'");
                    }
                }
                if(viewHolder.pbSending != null){
                    if(msg.getStatus() == 1){
                        // 语音文件已经发送成功
                        viewHolder.pbSending.setVisibility(View.INVISIBLE);
                        viewHolder.tvContent.setVisibility(View.VISIBLE);
                    }else{
                        // 语音文件尚为开始发送
                        viewHolder.pbSending.setVisibility(View.VISIBLE);
                        viewHolder.tvContent.setVisibility(View.INVISIBLE);
                    }
                }
                viewHolder.ivContent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 点击开始播放语音文件
                        String voiceUrl;
                        Constants.Position pos;
                        if(getItemViewType(position) == RECEIVE_VOICE_MSG){
                            voiceUrl = voiceInfo.split("&")[0];
                            pos = Constants.Position.LEFT;
                        }else{
                            voiceUrl = voiceInfo.split("&")[0];
                            pos = Constants.Position.RIGHT;
                        }
                        playVoice(voiceUrl,pos,viewHolder.ivContent);
                    }
                });
                break;
            default:
                throw new RuntimeException("不正确的消息类型.");
        }
        return convertView;
    }

    /**
     * 播放语音
     * @param voiceUrl
     * @param pos
     * @param iv
     */
    protected void playVoice(String voiceUrl, final Constants.Position pos, final ImageView iv){
        try{
            MediaPlayer mp = new MediaPlayer();
            mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                    if(pos == Constants.Position.LEFT){
                        iv.setImageResource(R.drawable.play_voice_left);
                    }else{
                        iv.setImageResource(R.drawable.play_voice_right);
                    }
                    ((AnimationDrawable)iv.getDrawable()).start();
                }
            });
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.stop();
                    if(pos == Constants.Position.LEFT){
                        iv.setImageResource(R.drawable.voice_right3);
                    }else{
                        iv.setImageResource(R.drawable.voice_left3);
                    }
                    mp.release();
                }
            });
            mp.setDataSource(voiceUrl);
            mp.prepareAsync();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public int getViewTypeCount() {
        return 8;
    }

    @Override
    public int getItemViewType(int position) {
        BmobMsg msg = getItem(position);
        int msgType = msg.getMsgType();
        String belongId = msg.getBelongId();
        switch (msgType){
            case 1:
                if(belongId.equals(BmobUserManager.getInstance(context).getCurrentUserObjectId())){
                    return SEND_TEXT_MSG;
                }else{
                    return RECEIVE_TEXT_MSG;
                }
            case 2:
                if(belongId.equals(BmobUserManager.getInstance(context).getCurrentUserObjectId())){
                    return SEND_IMAGE_MSG;
                }else{
                    return RECEIVE_IMAGE_MSG;
                }
            case 3:
                if(belongId.equals(BmobUserManager.getInstance(context).getCurrentUserObjectId())){
                    return SEND_LOC_MSG;
                }else{
                    return RECEIVE_LOC_MSG;
                }
            case 4:
                if(belongId.equals(BmobUserManager.getInstance(context).getCurrentUserObjectId())){
                    return SEND_VOICE_MSG;
                }else{
                    return RECEIVE_VOICE_MSG;
                }
            default:
                throw new RuntimeException("不正确的聊天消息格式");
        }
    }

    class ViewHolder{
        TextView tvTime;
        CircleImageView ivAvatar;
        TextView tvContent;
        ImageView ivContent;
        ProgressBar pbSending;
    }
}
