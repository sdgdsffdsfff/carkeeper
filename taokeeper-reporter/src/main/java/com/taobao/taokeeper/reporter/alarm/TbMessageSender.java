package com.taobao.taokeeper.reporter.alarm;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taobao.taokeeper.model.type.Message;
import common.toolkit.java.entity.Message.MessageType;
import common.toolkit.java.util.StringUtil;
import common.toolkit.java.util.io.NetUtil;

/**
 * Description: 淘宝内部使用：旺旺消息与手机短信
 *
 * @author 银时 yinshi.nc@taobao.com
 * @Date Dec 26, 2011
 */
public class TbMessageSender implements MessageSender {

    private static final Logger LOG = LoggerFactory.getLogger(TbMessageSender.class);

    /**
     * 短信http接口url
     */
    private static String smsURL = "http://index.tv.sohuno.com:9989/alert/sms/send?phone=${phone}&msg=${msg}";

    /**
     * 邮箱http接口url
     */
    private static String emailURL = "http://index.tv.sohuno.com:9989/alert/mail/send?title=${title}&content=${content}&receiver=${receiver}";

    private Message[] messages;

    public TbMessageSender(Message... messages) {
        this.messages = messages;
    }

    @Override
    public void run() {

        if (null == messages || 0 == messages.length) {
            LOG.info("[TaoKeeper]No need to send message: messages.length: " + messages);
            return;
        }

        for (Message message : this.messages) {
            try {
                this.sendMessage(StringUtil.trimToEmpty(message.getTargetAddresses()), StringUtil.trimToEmpty(message.getSubject()),
                        StringUtil.trimToEmpty(message.getContent()), StringUtil.trimToEmpty(message.getType().toString()));
                LOG.info("[TaoKeeper]Message send success: " + message);
            } catch (Exception e) {
                e.printStackTrace();
                LOG.error("Message send error: " + message + e.getMessage());
            }
        }

    }

    /**
     * 发送消息
     *
     * @param targetAddresses
     * @param subject
     * @param content         message content
     * @param channel         messate tyep:sms,email
     * @return
     * @throws Exception
     */
    private boolean sendMessage(String targetAddresses, String subject, String content, String channel) throws Exception {

        if (StringUtil.isBlank(targetAddresses) || StringUtil.isBlank(channel))
            return false;

        Map<String, String> map = new HashMap<String, String>();
        String url = "";
        if (channel.equalsIgnoreCase(MessageType.EMAIL.toString())) {
            map.put("title", URLEncoder.encode(subject, "UTF-8"));
            map.put("content", URLEncoder.encode(content, "UTF-8"));
            map.put("receiver", URLEncoder.encode(targetAddresses, "UTF-8"));
            url = StringUtil.replacePlaceholder(emailURL, map);
            String responseStr = HttpRequestUtil.get(url, "UTF-8");
            LOG.warn("responseStr={}", responseStr);
        } else if (channel.equalsIgnoreCase(MessageType.SMS.toString())) {
            String phone = targetAddresses;
            String message = "subject:" + subject + ".content:" + content;
            //过滤空格
            message = message.replaceAll(" ", "");
            //避免过长
            if (message.length() > 100) {
                message = message.substring(0, 100);
            }
            message = URLEncoder.encode(message, "UTF-8");

            map.put("phone", phone);
            map.put("msg", message);

            url = StringUtil.replacePlaceholder(smsURL, map);
            String responseStr = HttpRequestUtil.get(url, "UTF-8");
            LOG.warn("responseStr={}", responseStr);
        }
        LOG.info("[Taokeeper]Send message: " + url);
        return "ok".equalsIgnoreCase(NetUtil.getContentOfUrl(url));

    }


}
