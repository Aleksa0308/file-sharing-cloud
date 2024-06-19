package servent.message;

public class RIPNodeMessage extends BasicMessage{

        private static final long serialVersionUID = -883272346235632341L;

        public RIPNodeMessage(int senderPort, int receiverPort, String messageText) {
            super(MessageType.RIP_NODE, senderPort, receiverPort, messageText);
        }

}
