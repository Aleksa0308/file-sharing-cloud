package servent.message;

public class ViewFilesMessage extends BasicMessage{

    private static final long serialVersionUID = 123456789423423489L;

    public ViewFilesMessage(int senderPort, int receiverPort, String messageText) {
        super(MessageType.VIEW_FILES, senderPort, receiverPort, messageText);
    }
}
