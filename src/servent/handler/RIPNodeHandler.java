package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.RIPNodeMessage;
import servent.message.util.MessageUtil;

public class RIPNodeHandler implements MessageHandler{
    private Message clientMessage;

    public RIPNodeHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }
    @Override
    public void run() {
        if(clientMessage.getMessageType() == MessageType.RIP_NODE){
            if(AppConfig.myServentInfo.getListenerPort() != clientMessage.getSenderPort()){
                String[] splitMessage = clientMessage.getMessageText().split(":");
                int deadNodePort = Integer.parseInt(splitMessage[1]);
                AppConfig.chordState.removeNode(deadNodePort);
                AppConfig.timestampedStandardPrint("Node " + deadNodePort + " is dead.");

                // send RIP_NODE message to all other nodes
                if(AppConfig.chordState.getNextNodePort() != -1){
                    Message ripMessage = new RIPNodeMessage(clientMessage.getSenderPort(), AppConfig.chordState.getNextNodePort(), clientMessage.getMessageText());
                    MessageUtil.sendMessage(ripMessage);
                }
            }else{
                // rip message came back to the sender
                AppConfig.timestampedErrorPrint("Everybody paid their respects to the dead node.");
            }
        } else {
            AppConfig.timestampedErrorPrint("RIPNode handler got a message that is not RIP_NODE");
        }
    }
}
