package failure_detection;

import app.AppConfig;
import app.Cancellable;
import servent.message.HeartbeatMessage;
import servent.message.IsAliveMessage;
import servent.message.Message;
import servent.message.RIPNodeMessage;
import servent.message.util.MessageUtil;

public class HeartbeatMonitor implements Runnable, Cancellable {
    private volatile boolean working = true;

    @Override
    public void stop() {
        working = false;
    }

    @Override
    public void run() {
        while (working) {
            try {
                // Check current time and compare it with the last heartbeat time from the predecessor
                if(AppConfig.chordState.getLastPredecessorHeartbeat() != -1) {
                    long currentTime = System.currentTimeMillis();
                    long lastPredecessorHeartbeat = AppConfig.chordState.getLastPredecessorHeartbeat();
                    long predecessorTolerance = currentTime - lastPredecessorHeartbeat;
                    checkPredecessor(predecessorTolerance);
                }


                if (AppConfig.chordState.getNextNodePort() != -1) {
                    Message heartbeatMessage = new HeartbeatMessage(AppConfig.myServentInfo.getListenerPort(), AppConfig.chordState.getNextNodePort());
                    MessageUtil.sendMessage(heartbeatMessage);
                }

                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void checkPredecessor(long predecessorTolerance) {
        if (predecessorTolerance > AppConfig.WEAK_TOLERANCE && predecessorTolerance < AppConfig.STRONG_TOLERANCE) {
            AppConfig.timestampedStandardPrint("Predecessor is possibly dead");
            // send isAlive message to my successor to check on my behalf
            String nodeIWantToCheck = "localhost:" + AppConfig.chordState.getPredecessor().getListenerPort();

            Message isAliveMessage = new IsAliveMessage(
                    AppConfig.myServentInfo.getListenerPort(),
                    AppConfig.chordState.getNextNodePort(),
                    nodeIWantToCheck + " localhost:" + AppConfig.myServentInfo.getListenerPort());
            MessageUtil.sendMessage(isAliveMessage);
        } else if (predecessorTolerance > AppConfig.STRONG_TOLERANCE) {
            AppConfig.timestampedStandardPrint("Predecessor is dead");
            // Notify the next node that the predecessor is dead
            String deadNode = "localhost:" + AppConfig.chordState.getPredecessor().getListenerPort();
            AppConfig.chordState.removeNode(AppConfig.chordState.getPredecessor().getListenerPort());
            // Transfer backed up files to my filesMap
            AppConfig.chordState.transferBackupFiles();
            AppConfig.timestampedStandardPrint("Transferred backup files");
            if(AppConfig.chordState.getNextNodePort() != -1){
                Message ripMessage = new RIPNodeMessage(AppConfig.myServentInfo.getListenerPort(), AppConfig.chordState.getNextNodePort(), deadNode);
                MessageUtil.sendMessage(ripMessage);
            }
        }
    }
}
