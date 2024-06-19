package servent.message;

public class RemoveFileMessage extends BasicMessage{

    private static final long serialVersionUID = -8558546546939838333L;

    public RemoveFileMessage(int senderPort, int receiverPort, String messageText) {
        super(MessageType.REMOVE_FILE, senderPort, receiverPort, messageText);
    }
}
