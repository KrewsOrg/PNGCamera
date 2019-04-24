package eu.pngcamera.camera.eventplugins;

import com.eu.habbo.Emulator;
import com.eu.habbo.messages.ICallable;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.camera.CameraPublishWaitMessageComposer;
import com.eu.habbo.messages.outgoing.catalog.NotEnoughPointsTypeComposer;
import com.eu.habbo.plugin.events.users.UserPublishPictureEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CameraPublishToWebEventPlugin  implements ICallable {

    @Override
    public void call(MessageHandler messageHandler) {
        messageHandler.isCancelled = true;
        if (messageHandler.client.getHabbo().getHabboInfo().getCurrencyAmount(0) < Emulator.getConfig().getInt("camera.price.points.publish"))
        {
            messageHandler.client.sendResponse(new NotEnoughPointsTypeComposer(false, true, 0));
            return;
        }
        if (messageHandler.client.getHabbo().getHabboInfo().getPhotoTimestamp() != 0)
        {
            if (!messageHandler.client.getHabbo().getHabboInfo().getPhotoJSON().isEmpty())
            {
                if (messageHandler.client.getHabbo().getHabboInfo().getPhotoJSON().contains(messageHandler.client.getHabbo().getHabboInfo().getPhotoTimestamp() + ""))
                {
                    int timestamp = Emulator.getIntUnixTimestamp();

                    boolean published = false;
                    int timeDiff = timestamp - messageHandler.client.getHabbo().getHabboInfo().getWebPublishTimestamp();
                    int wait = 0;
                    if (timeDiff < Emulator.getConfig().getInt("camera.publish.delay"))
                    {
                        wait = timeDiff - Emulator.getConfig().getInt("camera.publish.delay");
                    }
                    else
                    {
                        UserPublishPictureEvent publishPictureEvent = new UserPublishPictureEvent(messageHandler.client.getHabbo(), messageHandler.client.getHabbo().getHabboInfo().getPhotoURL(), timestamp, messageHandler.client.getHabbo().getHabboInfo().getPhotoRoomId());
                        if (!Emulator.getPluginManager().fireEvent(publishPictureEvent).isCancelled())
                        {
                            try (Connection connection = Emulator.getDatabase().getDataSource().getConnection(); PreparedStatement statement = connection.prepareStatement("INSERT INTO camera_web (user_id, room_id, timestamp, url) VALUES (?, ?, ?, ?)"))
                            {
                                statement.setInt(1, messageHandler.client.getHabbo().getHabboInfo().getId());
                                statement.setInt(2, publishPictureEvent.roomId);
                                statement.setInt(3, publishPictureEvent.timestamp);
                                statement.setString(4, publishPictureEvent.URL);
                                statement.execute();
                                messageHandler.client.getHabbo().getHabboInfo().setWebPublishTimestamp(timestamp);
                                messageHandler.client.getHabbo().givePixels(-Emulator.getConfig().getInt("camera.price.points.publish"));
                                published = true;
                            }
                            catch (SQLException e)
                            {
                                Emulator.getLogging().logSQLException(e);
                            }
                        }
                        else
                        {
                            return;
                        }
                    }

                    messageHandler.client.sendResponse(new CameraPublishWaitMessageComposer(published, wait, published ? messageHandler.client.getHabbo().getHabboInfo().getPhotoURL() : ""));
                }
            }
        }
    }
}
