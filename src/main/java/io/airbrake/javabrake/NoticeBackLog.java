package io.airbrake.javabrake;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import okhttp3.Call;
import okhttp3.Response;

public class NoticeBackLog extends TimerTask {
    static Timer rTimer = new Timer(true);
    static boolean hasStarted = false;
    static String status = "";

    protected static ConcurrentLinkedQueue<PayLoad> noticeBackLogList = new ConcurrentLinkedQueue<>();

    protected static void start() {

        if (!hasStarted) {
            hasStarted = true;
            rTimer.schedule(new NoticeBackLog(), 0, Constant.BACKLOG_FLUSH_PERIOD * 1000);
        }
    }

    @Override
    public void run() {
        // Sync task
        // pop out queue items every 1 sec
        // Ignoring empty queue
        if (Notifier.config.backlogEnabled) {
            hasStarted = true;
            while (noticeBackLogList.size() > 0) {
                status = "";
                PayLoad payLoad = noticeBackLogList.poll();
                payLoad.retryCount += 1;
                Notice notice = (Notice) payLoad.data;

                Call call = OkSender.okhttp.newCall(new OkAsyncSender(Notifier.config).buildErrorRequest(notice));
                try (Response resp = call.execute()) {
                    if (resp != null) {
                        status = resp.message();
                        if (Constant.getStatusCodeCriteriaForBacklog().contains(resp.code())) {
                            NoticeBackLog.add(payLoad);
                        }
                    }
                } catch (IOException e) {
                    notice.exception = e;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } else {
            stop();
        }
    }

    protected static void stop() {
        rTimer.cancel();
    }

    protected static void add(PayLoad object) {
        start();
        if (noticeBackLogList.size() != Notifier.config.maxBacklogSize) {
            noticeBackLogList.add(object);
        }
    }
}
