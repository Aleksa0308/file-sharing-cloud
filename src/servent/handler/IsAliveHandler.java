package servent.handler;

import app.AppConfig;
import servent.message.IsAliveMessage;
import servent.message.IsAliveResponseMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

public class IsAliveHandler implements MessageHandler{
    private Message clientMessage;

    public IsAliveHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if(clientMessage.getMessageType().equals(MessageType.IS_ALIVE)){
            // Check if it's from my successor
            String[] messageParts = clientMessage.getMessageText().split(" ");
            String nodeToCheck = messageParts[0];
            int nodeToCheckPort = Integer.parseInt(nodeToCheck.split(":")[1]);

            String nodeThatWantsToCheck = messageParts[1];
            int nodeThatWantsToCheckPort = Integer.parseInt(nodeThatWantsToCheck.split(":")[1]);

            if(AppConfig.myServentInfo.getListenerPort() == nodeToCheckPort){
                // I am being checked
                // send isAliveResponse message to the sender
                Message isAliveResonseMessage = new IsAliveResponseMessage(AppConfig.myServentInfo.getListenerPort(), clientMessage.getSenderPort(), clientMessage.getMessageText());
                AppConfig.timestampedStandardPrint("Sending isAliveResponse message to " + clientMessage.getSenderPort());
                MessageUtil.sendMessage(isAliveResonseMessage);
            }else{
                // Send isAlive message to the checkPort
                Message isAliveMessage = new IsAliveMessage(AppConfig.myServentInfo.getListenerPort(), nodeToCheckPort, clientMessage.getMessageText());
                AppConfig.timestampedStandardPrint("Sending isAlive message to " + nodeToCheckPort);
                MessageUtil.sendMessage(isAliveMessage);
            }
        } else {
            System.out.println("IsAlive handler got a message that is not IS_ALIVE");
        }
    }
}
