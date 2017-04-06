package cn.edu.lin.graduationproject.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.im.BmobChatManager;
import cn.bmob.im.BmobNotifyManager;
import cn.bmob.im.BmobUserManager;
import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.im.bean.BmobInvitation;
import cn.bmob.im.bean.BmobMsg;
import cn.bmob.im.inteface.EventListener;
import cn.bmob.im.inteface.OnReceiveListener;
import cn.bmob.im.util.BmobJsonUtil;
import cn.bmob.push.PushConstants;
import cn.bmob.v3.BmobInstallation;
import cn.bmob.v3.listener.FindListener;
import cn.edu.lin.graduationproject.R;
import cn.edu.lin.graduationproject.app.MyApp;
import cn.edu.lin.graduationproject.constant.Constants;
import cn.edu.lin.graduationproject.ui.MainActivity;
import cn.edu.lin.graduationproject.util.SPUtil;

/**
 * Created by liminglin on 17-2-28.
 */

public class MyReceiver extends BroadcastReceiver {

    private static final String TAG = "MyReceiver";

    private static List<EventListener> list = new ArrayList<>();

    public static void regist(EventListener listener){
        list.add(listener);
    }

    public static void unRegist(EventListener listener){
        list.remove(listener);
    }

    private SPUtil spUtil = new SPUtil(MyApp.context, BmobUserManager.getInstance(MyApp.context).getCurrentUserObjectId());

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(PushConstants.ACTION_MESSAGE.equals(action)){
            try{
                String message = intent.getStringExtra(PushConstants.EXTRA_PUSH_MESSAGE_STRING);
                Log.d(TAG, BmobInstallation.getInstallationId(context)+"收到的内容："+message);
                JSONObject jsonObject = new JSONObject(message);
                if(jsonObject.has("tag")){
                    String tag = jsonObject.getString("tag");
                    // tag的值为offline
                    // 当前设备的登录用户的帐号已经在另外一台设备上登录
                    // 让当前设备的登录用户下线
                    if("offline".equals(tag)){
                        if(list.size() > 0){
                            // 此时有订阅者
                            // MyReceiver 通过调用订阅者的 offline 方法告知订阅者
                            // 再由订阅者具体处理让当前登录用户下线
                            for(EventListener listener:list){
                                listener.onOffline();
                            }
                        }else{
                            // 没有订阅者，则 MyReceiver 自行处理让当前登录用户下线，并退出程序
                            MyApp.logout();
                        }
                    }

                    if("add".equals(tag)){
                        // 收到了一个添加好友申请
                        String tid = BmobJsonUtil.getString(jsonObject,"tId");
                        if(tid != null){
                            // 收到了一条有效的添加好友申请
                            handleAddFriendInvitation(context,message,tid);
                        }
                    }

                    if("agree".equals(tag)){
                        // 收到了一个同意好友添加的回执
                        String tid = BmobJsonUtil.getString(jsonObject,"tId");
                        if(tid != null){
                            // 收到了一条有效的同意好友添加的回执
                            addFriend(context,message,tid);
                        }
                    }

                    if("".equals(tag)){
                        // 收到了一条聊天信息
                        String tid = BmobJsonUtil.getString(jsonObject,"tId");
                        if(tid != null){
                            // 收到了一条有效的聊天消息
                            saveMsg(context,message,tid);
                        }
                    }
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    }

    private void saveMsg(final Context context, String message, final String tid) {
        // 将 Json 字符串格式的聊天消息保存到本地数据库中
        // 1.根据收到的 json 字符串创建 JsonObject 对象，并判断消息的发送方是否是当前登录用户的好友
        // 2.根据 JsonObject 对象创建 BmobMsg 对象，BmobMsg 对象的 isRead 属性值为 2，status 属性值为 3
        // 3.根据当前设备是否有登录用户，以及如果油等录用户是否是消息的接收方来做出不用的处理
        //   只有在当前登录用户失效西接收方的情况下，才会保存 BmobMsg 对象后调用所传入监听器中的相应方法
        //   其余情况仅保存 BmobMsg 对象
        // 4.如果当前登录用户是消息的接收方，在保存前还会判断下，该消息是否曾经保存过
        // 5.保存 BmobMsg 对象时，向消息的接收方所对应的数据库的 chat 表和 recent 表中保存
        // 6.BmobMsg 对象保存完毕，向消息的放送方发送一个回执，tag 属性值为 readed（已读）
        // 7.回执发送完毕后，会更新接收到的聊天消息在 BmobMsg 数据表中保存的数据记录的 isreaded 字段值从 0 -- 1
        // 8.调用监听器
        // 注意：只有当前设备登录用户与消息接收方是同一个人时，自己的监听器才会被调用
        //      调用时间是在 BmobMsg 对象保存完毕之后执行，它有可能在第 6，7步之前就被执行

        BmobChatManager.getInstance(context).createReceiveMsg(message, new OnReceiveListener() {
            @Override
            public void onSuccess(BmobMsg bmobMsg) {
                // 保存聊天消息成功，参数就是根据 Json 字符串所获得的 BmobMsg 对象
                // 如果聊天消息是发送给当前登录用户的话
                // 则通知当前登录用户
                String uid = BmobUserManager.getInstance(context).getCurrentUserObjectId();
                if(tid.equals(uid)){
                   if(list.size() > 0){
                       for(EventListener listener : list){
                           listener.onMessage(bmobMsg);
                       }
                   }else{
                       if(spUtil.isAllowNotification()){
                           String ticker = "";
                           switch(bmobMsg.getMsgType()){
                               case 1:
                                   ticker = bmobMsg.getBelongUsername() + "说：" + bmobMsg.getContent();
                                   break;
                               case 2:
                                   ticker = bmobMsg.getBelongUsername() + "发送了一个：[图片]";
                                   break;
                               case 3:
                                   ticker = bmobMsg.getBelongUsername() + "发送了一个：[位置]";
                                   break;
                               case 4:
                                   ticker = bmobMsg.getBelongUsername() + "发送了一个：[语音]";
                                   break;
                               default:
                                   throw new RuntimeException("错误的消息类型.");
                           }
                           BmobNotifyManager.getInstance(context).showNotify(
                                   spUtil.isAllowSound(),
                                   spUtil.isAllowVibrate(),
                                   R.drawable.ic_notification,
                                   ticker,
                                   "聊天内容",
                                   ticker,
                                   MainActivity.class
                           );
                       }
                   }
                }
            }

            @Override
            public void onFailure(int i, String s) {
                switch(i){
                    case 1002:
                        Log.d(TAG, "一秒钟之内收到了来自同一用户的多天聊天信息.");
                        break;
                    default:
                        Log.d(TAG, "保存聊天消失时出现错误，错误编码：" + i + "," + s);
                }
            }
        });
    }

    private void addFriend(final Context context, final String message, final String tid) {
        try{
            final String targetName = BmobJsonUtil.getString(new JSONObject(message),"fu");
            // 1.根据 targetName 在服务器 _user 表中查找对应的用户
            // 2.如果确实存在该用户，则当前登录用户在 _user 表中所对应的数据记录的 contacts 字段值，完成两人好友关系建立
            // 3.在当前登录用户所对应本地数据库 friends 表中添加好友信息
            // 4.调用自己的监听器的回调方法
            BmobUserManager.getInstance(context).addContactAfterAgree(targetName, new FindListener<BmobChatUser>() {
                @Override
                public void onSuccess(List<BmobChatUser> list) {
                    // 判断该回执的接收人是不是当前设备的登录用户
                    String uid = BmobUserManager.getInstance(context).getCurrentUserObjectId();
                    if(tid.equals(uid)){
                        // 如果是，则要通知当前登录用户
                        // 订阅者对收到了一个同意好友添加的回执事情并不感兴趣（并没有订阅这个事情）
                        if(spUtil.isAllowNotification()){
                            BmobNotifyManager.getInstance(context).showNotify(
                                    spUtil.isAllowSound(),
                                    spUtil.isAllowVibrate(),
                                    R.drawable.ic_notification,
                                    targetName + "同意了您添加好友的申请",
                                    "同意添加好友",
                                    targetName + "同意了您添加好友的申请",
                                    MainActivity.class
                            );
                        }

                        // 1.根据收到的 json 字符串创建了一个 BmobMsg 对象
                        // 2.将 1 创建出来的 BmobMsg 对象，作为两人之间的一条聊天记录，保存到本地数据库的 chat 表中
                        // 3.根据 1 所创建出来的 BmobMsg 对象，提取部分属性构建了一个 BmobRecent 对象
                        // 4.将 3 所创建的 BmobRecent 对象保存到本地数据库的 Recent 数据表中
                        // 5.要更新回执信息在 BmobMsg 数据表中 isReaded 字段值（0 -- 1）
                        BmobMsg.createAndSaveRecentAfterAgree(context,message);

                        // 发送通知，即时刷新好友列表（FriendFragment 的 listView）
                        // 刷新会话列表（MessageFragment 的 listView）
                        // 刷新 MainActivity 的总的纬度消息数量
                        Intent intent2 = new Intent(Constants.ADD_FRIEND);
                        context.sendBroadcast(intent2);
                    }
                }

                @Override
                public void onError(int i, String s) {

                }
            });
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    private void handleAddFriendInvitation(Context context, String message, String tid) {
        // 将收到的 Json 字符串格式的添加好友申请保存到本地数据库的数据表中
        // 1.根据收到的 Json 字符串创建 BmobInvitation 实体类对象，该对象一个重要的属性值 status 为 2
        //   status 为 2 意味着收到了一条添加好友申请但尚未处理，该对象最终也是 saveReceiveInite 方法的返回值
        // 2.将 1 创建的试题类对象的相关内容写入到 tid 所对应的数据库的 tab_new_contacts 数据表中
        //   该数据记录的 status 字段的值也是为2
        // 3.将收到的这条好友申请在服务器 BmobMsg 表中所对应数据记录的 isReaded 字段值从 0 -- 1
        //   意味着该添加好友申请已经收到了
        BmobInvitation bmobInvitation = BmobChatManager.getInstance(context).saveReceiveInvite(message,tid);

        // 该好友申请事发给 tid，那是 tid 是当前设备的登录用户时告知当前设备上的登录用户
        String uid = BmobUserManager.getInstance(context).getCurrentUserObjectId();
        if(tid.equals(uid)){
            if(list.size() > 0){
                // 如果有订阅者，将收到添加好友申请的事情告诉订阅者，再由订阅者告诉当前设备登录用户
                for(EventListener listener : list){
                    listener.onAddUser(bmobInvitation);
                }
            }else{
                // 如果没有订阅者，MyReceiver 通过发送通知的方式告诉当前设备登录用户
                if(spUtil.isAllowNotification()){
                    BmobNotifyManager.getInstance(context).showNotify(
                            spUtil.isAllowSound(),
                            spUtil.isAllowVibrate(),
                            R.drawable.ic_notification,
                            bmobInvitation.getFromname() + "请求添加您为好友",
                            "添加好友",
                            bmobInvitation.getFromname() + "请求添加您为好友",
                            MainActivity.class
                    );
                }
            }
        }
    }
}
