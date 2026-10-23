package bisq.support.notifications;

import bisq.chat.bisq_easy.open_trades.BisqEasyOpenTradeMessage;
import bisq.chat.mu_sig.open_trades.MuSigOpenTradeMessage;
import bisq.common.observable.Observable;
import bisq.common.proto.NetworkProto;
import bisq.common.proto.PersistableProto;
import bisq.common.validation.NetworkDataValidation;
import bisq.notifications.Notification;
import bisq.persistence.PersistableStore;
import bisq.support.moderator.ReportToModeratorMessage;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Optional;
import java.util.stream.Collectors;

@ToString
@Getter
@EqualsAndHashCode
public class SupportNotification implements PersistableProto, NetworkProto, Notification {

    private final String id;
    private final Optional<String> channelId;
    private final SupportNotificationType type;
    private final String title;
    private final String message;
    @EqualsAndHashCode.Exclude
    private final Observable<Boolean> isConsumed = new Observable<>();

    public SupportNotification(ReportToModeratorMessage message) {
        this.id = createId(message);
        this.channelId = Optional.empty();
        this.type = SupportNotificationType.MODERATOR_NOTIFICATION;
        this.title = "User Report";
        this.message = message.getMessage();
        this.setConsumed(false);
    }

    public SupportNotification(BisqEasyOpenTradeMessage message) {
        this.id = message.getId();
        this.channelId = Optional.of(message.getChannelId());
        this.type = SupportNotificationType.BISQ_EASY_MEDIATOR_NOTIFICATION;
        this.title = "Bisq Easy Trade Message";
        this.message = message.getText().orElse("");
        this.setConsumed(false);
    }

    public SupportNotification(MuSigOpenTradeMessage message) {
        this.id = message.getId();
        this.channelId = Optional.of(message.getChannelId());
        this.type = SupportNotificationType.MU_SIG_MEDIATOR_NOTIFICATION;
        this.title = "Mu-Sig Trade Message";
        this.message = message.getText().orElse("");
        this.setConsumed(false);
    }

    public SupportNotification(
            bisq.support.protobuf.SupportNotificationType supportNotificationType,
            String id,
            String channelId,
            String message,
            String title,
            boolean isConsumed
    ) {
        this.type = SupportNotificationType.fromProtoEnum(supportNotificationType);
        this.id = id;
        this.message = message;
        this.title = title;
        this.channelId = channelId.isEmpty() ? Optional.empty() : Optional.of(channelId);
        this.isConsumed.set(isConsumed);
    }

    public void setConsumed(boolean consumed) {
        this.isConsumed.set(consumed);
    }

    public boolean isNotConsumed() {
        return isConsumed.get() == false;
    }

    public boolean isMediatorNotification(){
        return type == SupportNotificationType.BISQ_EASY_MEDIATOR_NOTIFICATION
                || type == SupportNotificationType.MU_SIG_MEDIATOR_NOTIFICATION;
    }

    public boolean isBisqEasyMediatorNotification(){
        return type == SupportNotificationType.BISQ_EASY_MEDIATOR_NOTIFICATION;
    }

    public boolean isMuSigMediatorNotification(){
        return type == SupportNotificationType.MU_SIG_MEDIATOR_NOTIFICATION;
    }

    public boolean isModeratorNotification(){
        return type == SupportNotificationType.MODERATOR_NOTIFICATION;
    }

    public static PersistableStore<?> fromProto(bisq.support.protobuf.SupportNotificationsStore proto) {
        return new SupportNotificationsStore(
                proto.getSupportNotificationsList().stream()
                        .map(SupportNotificationType::fromProto)
                        .collect(Collectors.toSet()));
    }

    public bisq.support.protobuf.SupportNotification toProto(boolean serializeForHash) {
        return unsafeToProto(serializeForHash);
    }

    @Override
    public bisq.support.protobuf.SupportNotification.Builder getBuilder(boolean serializeForHash) {
        return bisq.support.protobuf.SupportNotification.newBuilder()
                .setId(id)
                .setChannelId(getChannelId().isPresent()? getChannelId().get() : "")
                .setTitle(title)
                .setMessage(message)
                .setSupportNotificationType(type.toProtoEnum())
                .setIsConsumed(isConsumed.get());
    }

    public static String createId(ReportToModeratorMessage message) {
        return message.getDate() + "." + message.getReporterUserProfileId();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void verify() {
        NetworkDataValidation.validateId(channelId);
    }
}
