package servent.message;

public class IsAliveResponseMessage extends BasicMessage{
    private static final long serialVersionUID = -8558025347689267433L;
    public IsAliveResponseMessage(int senderPort, int receiverPort, String messageText) {
        super(MessageType.IS_ALIVE_RESPONSE, senderPort, receiverPort, messageText);
    }
}
