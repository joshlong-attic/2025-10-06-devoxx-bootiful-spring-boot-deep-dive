package com.example.adoptions.adoptions;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.core.GenericHandler;
import org.springframework.integration.dsl.DirectChannelSpec;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.messaging.MessageChannel;
import org.springframework.modulith.events.Externalized;

@Externalized(ExternalizationIntegrationConfiguration.OUTBOUND_CHANNEL)
public record DogAdoptedEvent(int dogId) {
}

@Configuration
class ExternalizationIntegrationConfiguration {

    static final String OUTBOUND_CHANNEL = "outbound";

    @Bean(OUTBOUND_CHANNEL)
    DirectChannelSpec outbound() {
        return MessageChannels.direct();
    }

    @Bean
    IntegrationFlow integrationFlow(@Qualifier(OUTBOUND_CHANNEL) MessageChannel messageChannel) {
        // gregor hohpe
        // bobby woolf
        // "enterprise integration patterns"
        return IntegrationFlow
                .from(messageChannel)
                .handle((GenericHandler<DogAdoptedEvent>) (payload, headers) -> {
                    IO.println("payload:" + payload);
                    headers.forEach((k, v) -> IO.println(k + ":" + v));
                    return null;
                })
                .get();
    }

}