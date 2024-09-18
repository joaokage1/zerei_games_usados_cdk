package com.myorg;

import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.BillingMode;
import software.amazon.awscdk.services.dynamodb.Table;
import software.constructs.Construct;

public class DynamoDBCdkStack extends Stack {

    private final Table gameStockEventsTable;

    public DynamoDBCdkStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public DynamoDBCdkStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        gameStockEventsTable = Table.Builder.create(this, "gameStockEventsTable")
                .tableName("game_stock_events")
                .billingMode(BillingMode.PROVISIONED)
                .readCapacity(1)
                .writeCapacity(1)
                .partitionKey(Attribute.builder()
                        .name("pk")
                        .type(AttributeType.STRING)
                        .build())
                .sortKey(Attribute.builder()
                        .name("sk")
                        .type(AttributeType.STRING)
                        .build())
                .timeToLiveAttribute("ttl")
                .removalPolicy(RemovalPolicy.DESTROY)//If the stack is deleted, the table is deleted too
                .build();
    }

    public Table getGameStockEventsTable() {
        return gameStockEventsTable;
    }
}
