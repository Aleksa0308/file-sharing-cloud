package servent.message;

public class HeartbeatMessage extends BasicMessage{
    private static final long serialVersionUID = -8560311262303150432L;

    public HeartbeatMessage(int senderPort, int receiverPort) {
        super(MessageType.HEARTBEAT, senderPort, receiverPort);
    }
}
