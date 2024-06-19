package servent.message;

public class TellFilesMessage extends BasicMessage{

        private static final long serialVersionUID = 1234423523489423489L;

        public TellFilesMessage(int senderPort, int receiverPort, String messageText) {
            super(MessageType.TELL_FILES, senderPort, receiverPort, messageText);
        }
}
