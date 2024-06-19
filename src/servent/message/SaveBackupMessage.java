package servent.message;

import cloud.CloudFile;
import servent.message.BasicMessage;
import servent.message.MessageType;

public class SaveBackupMessage extends BasicMessage {

    private static final long serialVersionUID = -8394364376436434454L;

    private CloudFile cloudFile;
    public SaveBackupMessage(int senderPort, int receiverPort, CloudFile cloudFile) {
        super(MessageType.SAVE_BACKUP, senderPort, receiverPort);
        this.cloudFile = cloudFile;
    }

    public CloudFile getCloudFile() {
        return cloudFile;
    }
}
