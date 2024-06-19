package servent.message;

public class FriendAcceptMessage extends BasicMessage{

        private static final long serialVersionUID = 1234567890123456789L;

        public FriendAcceptMessage(int senderPort, int receiverPort, String messageText) {
            super(MessageType.FRIEND_ACCEPT, senderPort, receiverPort, messageText);
        }
}
