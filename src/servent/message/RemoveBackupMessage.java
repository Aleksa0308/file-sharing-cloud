package servent.message;

public class RemoveBackupMessage extends BasicMessage{

        private static final long serialVersionUID = -8394394847493434454L;

        private String filePath;
        public RemoveBackupMessage(int senderPort, int receiverPort, String filePath) {
            super(MessageType.REMOVE_BACKUP, senderPort, receiverPort);
            this.filePath = filePath;
        }

        public String getFilePath() {
            return filePath;
        }
}
