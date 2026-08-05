package com.lunaris.ansenuza.application.payment;

public interface ProcessBankEmailUseCase {
    BankEmailProcessingResult process(BankTransferNotification notification);
}
