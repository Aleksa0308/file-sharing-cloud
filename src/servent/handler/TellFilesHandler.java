package servent.handler;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import servent.message.FriendAcceptMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.TellFilesMessage;
import servent.message.util.MessageUtil;

public class TellFilesHandler implements MessageHandler{
    private Message clientMessage;

    public TellFilesHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.TELL_FILES) {
            try{
                String msgReceiverPort = clientMessage.getMessageText().split(" ")[0];
                int msgReceiverChordId = ChordState.getChordIdFromMessage(msgReceiverPort);
                ServentInfo myInfo = AppConfig.myServentInfo;

                if(AppConfig.chordState.isKeyMine(msgReceiverChordId)) {
                    // We are the rightful receiver we should accept the tell files
                    AppConfig.timestampedStandardPrint("Received tell files from " + clientMessage.getSenderPort() + "\n" + clientMessage.getMessageText());
                }else{
                    // We are not the receiver we should forward the message
                    ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(msgReceiverChordId);
                    AppConfig.timestampedStandardPrint("Tell files forwarded from " + myInfo.getListenerPort() + " to " + nextNode.getListenerPort());
                    Message tellFilesMessage = new TellFilesMessage(clientMessage.getSenderPort(),
                            nextNode.getListenerPort(),
                            clientMessage.getMessageText());

                    MessageUtil.sendMessage(tellFilesMessage);
                }
            }catch (NumberFormatException e) {
                AppConfig.timestampedErrorPrint("Got tell files with bad text: " + clientMessage.getMessageText());
            }
        }
    }
}
