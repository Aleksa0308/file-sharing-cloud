package servent.message;

public class FriendRequestMessage extends BasicMessage{

    private static final long serialVersionUID = 1234567890123456789L;

    public FriendRequestMessage(int senderPort, int receiverPort, String messageText) {
        super(MessageType.FRIEND_REQUEST, senderPort, receiverPort, messageText);
    }
}
