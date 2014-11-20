package brooklyn.location.softlayer.bms.client;

import com.google.common.base.Objects;

public class CancelServerRequest {
    private int attachmentId;
    private String reason;
    private boolean cancelAssociatedItems;
    private String attachmentType;

    protected CancelServerRequest(int attachmentId, String reason, boolean cancelAssociatedItems,
                                  String attachmentType) {
        this.attachmentId = attachmentId;
        this.reason = reason;
        this.cancelAssociatedItems = cancelAssociatedItems;
        this.attachmentType = attachmentType;
    }

    public String getAttachmentType() {
        return attachmentType;
    }

    public int getAttachmentId() {
        return attachmentId;
    }

    public String getReason() {
        return reason;
    }

    public boolean isCancelAssociatedItems() {
        return cancelAssociatedItems;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("attachmentType", attachmentType)
                .add("attachmentId", attachmentId)
                .add("reason", reason)
                .add("cancelAssociatedItems", cancelAssociatedItems)
                .toString();
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return builder().fromCancelServerRequest(this);
    }

    public static final class Builder {
        private int attachmentId;
        private String reason;
        private boolean cancelAssociatedItems;
        private String attachmentType;

        public Builder attachmentId(int attachmentId) {
            this.attachmentId = attachmentId;
            return this;
        }

        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public Builder cancelAssociatedItems(boolean cancelAssociatedItems) {
            this.cancelAssociatedItems = cancelAssociatedItems;
            return this;
        }

        public Builder attachmentType(String attachmentType) {
            this.attachmentType = attachmentType;
            return this;
        }

        public CancelServerRequest build() {
            return new CancelServerRequest(attachmentId, reason, cancelAssociatedItems, attachmentType);
        }

        public Builder fromCancelServerRequest(CancelServerRequest in) {
            return this
                    .attachmentId(in.getAttachmentId())
                    .reason(in.getReason())
                    .cancelAssociatedItems(in.isCancelAssociatedItems())
                    .attachmentType(in.getAttachmentType());
        }
    }
}
