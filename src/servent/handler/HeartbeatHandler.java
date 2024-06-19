package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;

public class HeartbeatHandler implements MessageHandler {
    private Message clientMessage;

    public HeartbeatHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.HEARTBEAT) {
            // Check if it's from my predecessor
            int senderPort = clientMessage.getSenderPort();
            int predecessorPort = AppConfig.chordState.getPredecessor().getListenerPort();
            if (senderPort == predecessorPort) {
                // Update predecessor's timestamp
                AppConfig.chordState.setLastPredecessorHeartbeat(System.currentTimeMillis());
            }
        } else {
            System.out.println("Heartbeat handler got a message that is not HEARTBEAT");
        }
    }
}
