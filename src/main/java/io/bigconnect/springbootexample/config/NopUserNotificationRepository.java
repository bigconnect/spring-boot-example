package io.bigconnect.springbootexample.config;

import com.mware.core.model.notification.UserNotification;
import com.mware.core.model.notification.UserNotificationRepository;
import com.mware.core.user.User;

import java.util.stream.Stream;

public class NopUserNotificationRepository extends UserNotificationRepository {
    public NopUserNotificationRepository() {
        super(null, null);
    }

    @Override
    public Stream<UserNotification> getActiveNotifications(User user) {
        return Stream.of();
    }

    @Override
    public Stream<UserNotification> findAll(User user) {
        return Stream.of();
    }

    @Override
    public void saveNotification(UserNotification notification, User authUser) {
    }
}
