package servent.handler;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import servent.message.FriendAcceptMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

public class FriendAcceptHandler implements MessageHandler{
    private Message clientMessage;

    public FriendAcceptHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.FRIEND_ACCEPT) {
            try{
                int msgReceiverChordId = ChordState.getChordIdFromMessage(clientMessage.getMessageText());
                ServentInfo myInfo = AppConfig.myServentInfo;

                if(AppConfig.chordState.isKeyMine(msgReceiverChordId)) {
                    // We are the rightful receiver we should accept the friend request
                    AppConfig.chordState.addFriend(clientMessage.getSenderPort());
                    AppConfig.timestampedStandardPrint("Friend accepted from " + clientMessage.getSenderPort());
                }else{
                    // We are not the receiver we should forward the message
                    ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(msgReceiverChordId);
                    AppConfig.timestampedStandardPrint("Friend accept forwarded from " + myInfo.getListenerPort() + " to " + nextNode.getListenerPort());
                    Message friendAcceptMessage = new FriendAcceptMessage(clientMessage.getSenderPort(),
                                                                          nextNode.getListenerPort(),
                                                                          clientMessage.getMessageText());

                    MessageUtil.sendMessage(friendAcceptMessage);
                }
            }catch (NumberFormatException e) {
                AppConfig.timestampedErrorPrint("Got friend accept with bad text: " + clientMessage.getMessageText());
            }
        }
    }

}
