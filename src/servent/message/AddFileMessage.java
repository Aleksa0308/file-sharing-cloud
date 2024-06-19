package servent.message;

public class AddFileMessage extends BasicMessage{
    private static final long serialVersionUID = -8558546546520315033L;

    public AddFileMessage(int senderPort, int receiverPort, String text) {
        super(MessageType.ADD_FILE, senderPort, receiverPort, text);
    }
}
