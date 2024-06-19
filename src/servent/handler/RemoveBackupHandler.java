package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.RemoveBackupMessage;

public class RemoveBackupHandler implements MessageHandler{
    private Message clientMessage;

    public RemoveBackupHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if(clientMessage.getMessageType() == MessageType.REMOVE_BACKUP) {
            AppConfig.chordState.removeBackupFile(((RemoveBackupMessage)clientMessage).getFilePath());
        }else{
            AppConfig.timestampedErrorPrint("RemoveBackupHandler got a message that is not REMOVE_BACKUP");
        }
    }
}
