package servent.handler;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import cloud.AccessType;
import servent.message.AddFileMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

public class AddFileHandler implements MessageHandler{

    private Message clientMessage;

    public AddFileHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }
    @Override
    public void run() {
        if(clientMessage.getMessageType() == MessageType.ADD_FILE){
            String filePath = AppConfig.CLOUD_PATH + clientMessage.getMessageText().split(" ")[0];
            AccessType accessType = AccessType.valueOf(clientMessage.getMessageText().split(" ")[1].toUpperCase());

            // convert file name to chord key
            int fileKey = ChordState.hashFileName(filePath);
            if(AppConfig.chordState.isKeyMine(fileKey)){
                AppConfig.chordState.addFile(filePath, accessType);
                return;
            }else{
                // forward the message to the next node
                ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(fileKey);
                AppConfig.timestampedStandardPrint("Sending ADD_FILE message to " + nextNode.getListenerPort());

                Message addFileMessage = new AddFileMessage(AppConfig.myServentInfo.getListenerPort(), nextNode.getListenerPort(), clientMessage.getMessageText());
                MessageUtil.sendMessage(addFileMessage);
            }
        } else {
            System.out.println("Add file handler got a message that is not ADD_FILE");
        }
    }
}
