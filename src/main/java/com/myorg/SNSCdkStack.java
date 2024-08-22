package com.myorg;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.events.targets.SnsTopic;
import software.amazon.awscdk.services.sns.Topic;
import software.amazon.awscdk.services.sns.subscriptions.EmailSubscription;
import software.constructs.Construct;

public class SNSCdkStack extends Stack {

    private final SnsTopic gamesStockEventsTopic;

    public SNSCdkStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public SNSCdkStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        gamesStockEventsTopic = SnsTopic.Builder.create(
                Topic.Builder
                        .create(this, "StockChangedTopic")
                        .topicName("stock_changed_v1")
                        .build())
                .build();

        gamesStockEventsTopic.getTopic()
                .addSubscription(EmailSubscription.Builder
                        .create("EMAIL@email.com")
                        .json(true)
                        .build());
    }

    public SnsTopic getGamesStockEventsTopic() {
        return gamesStockEventsTopic;
    }
}
