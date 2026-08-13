package com.lunaris.ansenuza.infrastructure.config;

import com.lunaris.ansenuza.domain.model.payment.TransferAccountDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MercadoPagoTransferConfig {

    @Bean
    TransferAccountDetails mercadoPagoTransferAccount(
            @Value("${mercadopago.transfer-account.alias}") String alias,
            @Value("${mercadopago.transfer-account.cvu}") String cvu,
            @Value("${mercadopago.transfer-account.cuit}") String cuit,
            @Value("${mercadopago.transfer-account.holder}") String holder) {
        return new TransferAccountDetails(alias, cvu, cuit, holder);
    }
}
