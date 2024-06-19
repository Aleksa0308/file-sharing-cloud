package servent.handler;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import servent.message.*;
import servent.message.util.MessageUtil;

public class ViewFilesHandler implements MessageHandler{

    private Message clientMessage;

    public ViewFilesHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }
    @Override
    public void run() {
        if(clientMessage.getMessageType() == MessageType.VIEW_FILES) {
            try {
                int msgReceiverChordId = ChordState.getChordIdFromMessage(clientMessage.getMessageText());
                ServentInfo myInfo = AppConfig.myServentInfo;
                AppConfig.timestampedStandardPrint("Received view files from " + clientMessage.getSenderPort());
                if(AppConfig.chordState.isKeyMine(msgReceiverChordId)) {
                    // We are the rightful receiver we should get the files
                    String fileInfo = AppConfig.chordState.tellFiles(clientMessage.getSenderPort());
                    AppConfig.timestampedStandardPrint("Sending back files to " + clientMessage.getSenderPort());
                    // Find the next node by key to forward the message
                    ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(clientMessage.getSenderPort());
                    Message tellFilesMessage = new TellFilesMessage(myInfo.getListenerPort(),
                            nextNode.getListenerPort(),
                            "localhost:" + clientMessage.getSenderPort() + " " + fileInfo);

                    MessageUtil.sendMessage(tellFilesMessage);
                }else{
                    // We are not the receiver we should forward the message
                    ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(msgReceiverChordId);
                    AppConfig.timestampedStandardPrint("View files forwarded from " + myInfo.getListenerPort() + " to " + nextNode.getListenerPort());
                    Message viewFilesMessage = new ViewFilesMessage(clientMessage.getSenderPort(),
                            nextNode.getListenerPort(),
                            clientMessage.getMessageText());

                    MessageUtil.sendMessage(viewFilesMessage);
                }

            } catch (NumberFormatException e) {
                AppConfig.timestampedErrorPrint("Got view files with bad text: " + clientMessage.getMessageText());
            }
        } else {
            AppConfig.timestampedErrorPrint("View files handler got a message that is not VIEW_FILES");
        }
    }
}
