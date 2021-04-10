package tenx.banking.transfer.core.entity;

import lombok.Data;
import lombok.NonNull;

import java.util.UUID;

@Data
public class TransactionId {
    @NonNull
    private final UUID id;
}
