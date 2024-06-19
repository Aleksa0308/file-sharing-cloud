package servent.handler;

import app.AppConfig;
import servent.message.IsAliveResponseMessage;
import servent.message.Message;
import servent.message.MessageType;

public class IsAliveResponseHandler implements MessageHandler{
    private Message clientMessage;

    public IsAliveResponseHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }
    @Override
    public void run() {
        if(clientMessage.getMessageType() == MessageType.IS_ALIVE_RESPONSE){
            // Check if it's from my successor
            String[] messageParts = clientMessage.getMessageText().split(" ");
            String nodeToCheck = messageParts[0];
            int nodeToCheckPort = Integer.parseInt(nodeToCheck.split(":")[1]);

            String nodeThatWantsToCheck = messageParts[1];
            int nodeThatWantsToCheckPort = Integer.parseInt(nodeThatWantsToCheck.split(":")[1]);

            if(AppConfig.myServentInfo.getListenerPort() == nodeThatWantsToCheckPort){
                // Update the last heartbeat time of the predecessor
                AppConfig.chordState.setLastPredecessorHeartbeat(System.currentTimeMillis());
            }else{
                // Send isAlive message to the checkPort
                System.out.println("Sending isAlive Response message to " + nodeThatWantsToCheckPort);
                Message isAliveResponseMessage = new IsAliveResponseMessage(AppConfig.myServentInfo.getListenerPort(), nodeThatWantsToCheckPort, clientMessage.getMessageText());
            }
        }
    }
}
