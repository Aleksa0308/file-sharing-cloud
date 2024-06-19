package servent.message;

public class IsAliveMessage extends BasicMessage{
    private static final long serialVersionUID = -8558025345455345033L;
    public IsAliveMessage(int senderPort, int receiverPort, String messageText) {
        super(MessageType.IS_ALIVE, senderPort, receiverPort, messageText);
    }
}
