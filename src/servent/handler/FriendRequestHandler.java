package servent.handler;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import servent.message.FriendAcceptMessage;
import servent.message.FriendRequestMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

public class FriendRequestHandler implements MessageHandler{
    private Message clientMessage;

    public FriendRequestHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }
    @Override
    public void run() {
        if(clientMessage.getMessageType() == MessageType.FRIEND_REQUEST) {
            try {
                int msgReceiverChordId = ChordState.getChordIdFromMessage(clientMessage.getMessageText());
                ServentInfo myInfo = AppConfig.myServentInfo;
                AppConfig.timestampedStandardPrint("Received friend request from " + clientMessage.getSenderPort());
                if(AppConfig.chordState.isKeyMine(msgReceiverChordId)) {
                    // We are the rightful receiver we should accept the friend request
                    AppConfig.chordState.addFriend(clientMessage.getSenderPort());
                    AppConfig.timestampedStandardPrint("Friend request accepted from " + clientMessage.getSenderPort());
                    // Find the next node by key to forward the message
                    ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(clientMessage.getSenderPort());
                    Message friendAcceptMessage = new FriendAcceptMessage(myInfo.getListenerPort(),
                                                                          nextNode.getListenerPort(),
                                                              "localhost:" + clientMessage.getSenderPort());

                    MessageUtil.sendMessage(friendAcceptMessage);
                }else{
                    // We are not the receiver we should forward the message
                    ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(msgReceiverChordId);
                    AppConfig.timestampedStandardPrint("Friend request forwarded from " + myInfo.getListenerPort() + " to " + nextNode.getListenerPort());
                    Message friendRequestMessage = new FriendRequestMessage(clientMessage.getSenderPort(),
                                                                            nextNode.getListenerPort(),
                                                                            clientMessage.getMessageText());

                    MessageUtil.sendMessage(friendRequestMessage);
                }

            } catch (NumberFormatException e) {
                AppConfig.timestampedErrorPrint("Got friend request with bad text: " + clientMessage.getMessageText());
            }
        } else {
            AppConfig.timestampedErrorPrint("Friend request handler got a message that is not FRIEND_REQUEST");
        }
    }
}
