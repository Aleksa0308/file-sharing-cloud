package servent.handler;

import app.AppConfig;
import cloud.CloudFile;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.SaveBackupMessage;

public class SaveBackupHandler implements MessageHandler{
    private Message clientMessage;

    public SaveBackupHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if(clientMessage.getMessageType() == MessageType.SAVE_BACKUP) {
            AppConfig.chordState.addBackupFile(((SaveBackupMessage)clientMessage).getCloudFile().getFilePath(), ((SaveBackupMessage)clientMessage).getCloudFile());
        }else{
            AppConfig.timestampedErrorPrint("SaveBackupHandler got a message that is not SAVE_BACKUP");
        }
    }
}
