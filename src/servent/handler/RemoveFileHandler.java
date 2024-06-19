package servent.handler;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.RemoveFileMessage;
import servent.message.util.MessageUtil;

public class RemoveFileHandler implements MessageHandler{

    private Message clientMessage;

    public RemoveFileHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if(clientMessage.getMessageType() == MessageType.REMOVE_FILE){
            String filePath = AppConfig.CLOUD_PATH + clientMessage.getMessageText();
            int fileKey = ChordState.hashFileName(filePath);

            if(AppConfig.chordState.isKeyMine(fileKey)){
                AppConfig.chordState.removeFile(filePath);
                return;
            }else{
                AppConfig.timestampedStandardPrint("File is not on this node.");
                ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(fileKey);
                AppConfig.timestampedStandardPrint("Sending REMOVE_FILE message to " + nextNode.getListenerPort());

                Message removeFileMessage = new RemoveFileMessage(AppConfig.myServentInfo.getListenerPort(), nextNode.getListenerPort(), clientMessage.getMessageText());
                MessageUtil.sendMessage(removeFileMessage);
            }
        } else {
            System.out.println("Remove file handler got a message that is not REMOVE_FILE");
        }
    }
}
