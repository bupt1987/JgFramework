package com.zhaidaosi.game.jgframework.session;

import com.zhaidaosi.game.jgframework.Boot;
import com.zhaidaosi.game.jgframework.Router;
import com.zhaidaosi.game.jgframework.common.queue.BaseQueue;
import com.zhaidaosi.game.jgframework.common.queue.BaseQueueElement;
import com.zhaidaosi.game.jgframework.connector.IBaseConnector;
import com.zhaidaosi.game.jgframework.message.IBaseMessage;
import com.zhaidaosi.game.jgframework.message.InMessage;
import com.zhaidaosi.game.jgframework.message.OutMessage;
import com.zhaidaosi.game.jgframework.model.entity.IBaseCharacter;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SessionManager {

    public final static String SECRET = "secret";
    public final static int ADD_SESSION_SUCC = 1;
    public final static int ADD_SESSION_ERROR = -1;
    public final static int ADD_SESSION_WAIT = 0;

    private static final Logger log = LoggerFactory.getLogger(SessionManager.class);
    private static IBaseSecretFactory secretFactory = new BaseSecretFactory();
    private static ConcurrentMap<Integer, Channel> userIdChannels = new ConcurrentHashMap<>();
    private static ConcurrentMap<Integer, BaseQueueElement<Channel>> waitUserIdChannels = new ConcurrentHashMap<>();
    private static BaseQueue<Channel> waitQueue = new BaseQueue<>();
    private static Timer timer;
    private static int maxUser = 0;

    public static int checkSession(InMessage msg, Channel ch) {
        IBaseCharacter player = ch.attr(IBaseConnector.PLAYER).get();
        if (player == null) {
            return ADD_SESSION_ERROR;
        }
        if (player.getId() <= 0) {
            Object secret = msg.getMember(SECRET);
            if (secret == null || secret.equals("")) {
                return ADD_SESSION_ERROR;
            }
            try {
                int userId = checkSecret((String) secret);
                if (userId == 0) {
                    return ADD_SESSION_ERROR;
                }
                player.setId(userId);
                return SessionManager.addSession(player, ch);
            } catch (Exception e) {
                return ADD_SESSION_ERROR;
            }
        }
        if (maxUser > 0 && player.isInQueue()) {
            return ADD_SESSION_WAIT;
        }
        return ADD_SESSION_SUCC;
    }

    /**
     * 加入session
     * @param player
     * @param ch
     * @return
     */
    private static int addSession(IBaseCharacter player, Channel ch) {
        int userId;
        if (player == null || (userId = player.getId()) <= 0) {
            return ADD_SESSION_ERROR;
        }

        Channel _ch = userIdChannels.get(userId);
        BaseQueueElement<Channel> queueElement = null;
        if (_ch == null && maxUser > 0) {
            queueElement = waitUserIdChannels.get(userId);
            if (queueElement != null) {
                _ch = queueElement.getValue();
            }
        }

        boolean same = _ch != null && _ch.hashCode() == ch.hashCode();

        if (_ch != null && !same) {
            IBaseCharacter _player = _ch.attr(IBaseConnector.PLAYER).get();
            if (_player != null) {
                _player.setId(0);
                // 保持排队名次
                if (queueElement != null) {
                    player.setIsInQueue(true);
                    queueElement.setValue(ch);
                }
            }
            _ch.close();
        }

        if (maxUser > 0) {
            if (queueElement != null) {
                return ADD_SESSION_WAIT;
            } else if (userIdChannels.size() >= maxUser) {
                player.setIsInQueue(true);
                BaseQueueElement<Channel> element = waitQueue.put(ch);
                if (_ch == null || !same) {
                    waitUserIdChannels.put(userId, element);
                }
                return ADD_SESSION_WAIT;
            }
        }

        if (_ch == null || !same) {
            userIdChannels.put(userId, ch);
        }

        player.loginHook();
        return ADD_SESSION_SUCC;
    }

    public static void removeSession(Channel ch) {
        IBaseCharacter player = ch.attr(IBaseConnector.PLAYER).get();
        if (player != null) {
            int userId = player.getId();
            if (maxUser > 0 && player.isInQueue()) {
                if (userId > 0) {
                    BaseQueueElement<Channel> queueElement = waitUserIdChannels.get(userId);
                    waitUserIdChannels.remove(userId);
                    waitQueue.remove(queueElement);
                }
            } else {
                if (userId > 0) {
                    userIdChannels.remove(userId);
                }
                player.logoutHook();
            }
        }
    }

    public static List<IBaseCharacter> getOnlineUser() {
        List<IBaseCharacter> onlineUser = new ArrayList<IBaseCharacter>();
        for (Channel ch : userIdChannels.values()) {
            IBaseCharacter player = ch.attr(IBaseConnector.PLAYER).get();
            if (player != null && player.getId() > 0) {
                onlineUser.add(player);
            }
        }
        return onlineUser;
    }

    public static IBaseCharacter getPlayerByUserId(Integer uid) {
        Channel ch = userIdChannels.get(uid);
        IBaseCharacter player = ch == null ? null : ch.attr(IBaseConnector.PLAYER).get();
        return (player != null && player.getId() > 0) ? player : null;
    }

    public static boolean isAuthHandler(InMessage msg) {
        return msg.getH().startsWith(Boot.getAuthHandler());
    }

    public static String getServerIp(int userId) {
        int AuthCount = Boot.getServiceCount();
        int index = userId % AuthCount;
        return Boot.getServiceIps().get(index);
    }

    public static String createSecret(int userId) throws Exception {
        return secretFactory.createSecret(userId);
    }

    private static int checkSecret(String secret) throws Exception {
        return secretFactory.checkSecret(secret);
    }

    public static void init() {
        if (maxUser > 0) {
            initTimer();
        }
    }

    public static void destroy() {
        userIdChannels.clear();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (maxUser > 0) {
            waitUserIdChannels.clear();
            waitQueue.clear();
        }
    }

    public static long getWaitCount() {
        return waitQueue.size();
    }

    public static int getUserCount() {
        return userIdChannels.size();
    }

    public static ConcurrentMap<Integer, Channel> getChannels() {
        return userIdChannels;
    }

    public static void setSercretFactory(IBaseSecretFactory secretFactory) {
        SessionManager.secretFactory = secretFactory;
    }

    public static void setMaxUser(int max) {
        if (max > 0) {
            maxUser = max;
        }
    }

    private static void initTimer() {
        if (timer == null) {
            timer = new Timer("QueueTimerTask");
            timer.schedule(new QueueTimerTask(), 10000, 10000);
        }
    }

    public static IBaseMessage getWaitMessage(IBaseCharacter player) {
        long index = 0;
        if (player != null && player.isInQueue()) {
            BaseQueueElement<Channel> queueElement = waitUserIdChannels.get(player.getId());
            index = waitQueue.findIndex(queueElement);
        }
        return OutMessage.showSucc(index, Router.WAIT_HANDLERNAME);
    }

    private static class QueueTimerTask extends TimerTask {
        @Override
        public void run() {
            try {
                while (userIdChannels.size() < maxUser && waitQueue.size() > 0) {
                    BaseQueueElement<Channel> element = waitQueue.take();
                    if (element == null) {
                        continue;
                    }
                    Channel ch = element.getValue();
                    if (ch == null) {
                        continue;
                    }
                    IBaseCharacter player = ch.attr(IBaseConnector.PLAYER).get();
                    if (player == null) {
                        continue;
                    }
                    int userId = player.getId();
                    if (userId <= 0) {
                        continue;
                    }
                    waitUserIdChannels.remove(userId);
                    player.setIsInQueue(false);
                    userIdChannels.put(userId, ch);
                    ch.writeAndFlush(getWaitMessage(player));
                }
                BaseQueueElement<Channel> start = waitQueue.getStart();
                while (start != null) {
                    Channel ch = start.getValue();
                    IBaseCharacter player = ch.attr(IBaseConnector.PLAYER).get();
                    ch.writeAndFlush(getWaitMessage(player));
                    start = start.getNext();
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
