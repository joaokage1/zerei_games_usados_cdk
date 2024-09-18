package com.myorg;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Fn;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.applicationautoscaling.EnableScalingProps;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.ecs.AwsLogDriverProps;
import software.amazon.awscdk.services.ecs.Cluster;
import software.amazon.awscdk.services.ecs.ContainerImage;
import software.amazon.awscdk.services.ecs.CpuUtilizationScalingProps;
import software.amazon.awscdk.services.ecs.LogDriver;
import software.amazon.awscdk.services.ecs.ScalableTaskCount;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck;
import software.amazon.awscdk.services.events.targets.SnsTopic;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.sns.subscriptions.SqsSubscription;
import software.amazon.awscdk.services.sqs.DeadLetterQueue;
import software.amazon.awscdk.services.sqs.Queue;
import software.amazon.awscdk.services.sqs.QueueEncryption;
import software.constructs.Construct;

import java.util.HashMap;
import java.util.Map;

public class Service01CdkStack extends Stack {
    public Service01CdkStack(final Construct scope, final String id, final Cluster cluster, final SnsTopic stockChangedTopic, final Table gameEventsDdb) {
        this(scope, id, null, cluster, stockChangedTopic, gameEventsDdb);
    }

    public Service01CdkStack(final Construct scope, final String id, final StackProps props, final Cluster cluster, final SnsTopic stockChangedTopic, final Table gameEventsDdb) {
        super(scope, id, props);

        //To be able to listen the topic
        Queue gameStockEventsDLQ = Queue.Builder.create(this, "gameStockEventsDLQ")
                .queueName("stock_changed_v1_dlq")
                .enforceSsl(false)
                .encryption(QueueEncryption.UNENCRYPTED)
                .build();

        DeadLetterQueue deadLetterQueue = DeadLetterQueue.builder()
                .queue(gameStockEventsDLQ)
                .maxReceiveCount(3)
                .build();

        Queue gameStockEvents = Queue.Builder.create(this, "gameStockEvents")
                .queueName("stock_changed_v1")
                .enforceSsl(false)
                .encryption(QueueEncryption.UNENCRYPTED)
                .deadLetterQueue(deadLetterQueue)
                .build();


        Map<String, String> envVariables = new HashMap<>();
        envVariables.put("SPRING_DATASOURCE_URL", "jdbc:mariadb://"
                + Fn.importValue("uri-db")
                + ":3306/aws_project01_db?createDatabaseIfNotExist=true");
        envVariables.put("SPRING_DATASOURCE_USERNAME", "service01app");
        envVariables.put("SPRING_DATASOURCE_PASSWORD", Fn.importValue("password-db"));
        envVariables.put("AWS_REGION", "us-east-1");
        envVariables.put("AWS_SNS_STOCK_TOPIC", stockChangedTopic.getTopic().getTopicArn());
        envVariables.put("AWS_SQS_QUEUE_STOCK_EVENTS_NAME", gameStockEvents.getQueueName());

        ApplicationLoadBalancedFargateService service01 = ApplicationLoadBalancedFargateService.Builder.create(this, "ALB01")
                .serviceName("service-01")
                .cluster(cluster)
                //.cpu(512) - using default (256)
                //.memoryLimitMiB(1024) - - using default (512)
                //.desiredCount(2) - using default (1)
                .listenerPort(8080)
                .assignPublicIp(true)
                .taskImageOptions(
                        ApplicationLoadBalancedTaskImageOptions.builder()
                                .image(ContainerImage.fromRegistry("DOCKER_IMAGE_URI"))
                                .containerName("aws_project01")
                                .containerPort(8080)
                                .logDriver(LogDriver.awsLogs(AwsLogDriverProps.builder()
                                        .logGroup(LogGroup.Builder.create(this, "Service01LogGroup")
                                                .logGroupName("Service01")
                                                .removalPolicy(RemovalPolicy.DESTROY)
                                                .build())
                                                .streamPrefix("Service01")
                                        .build()))
                                .environment(envVariables)
                                .build()
                )
                .publicLoadBalancer(true)
                .build();

        service01.getTargetGroup().configureHealthCheck(new HealthCheck.Builder()
                .path("/actuator/health")
                .port("8080")
                .healthyHttpCodes("200")
                .build());

        ScalableTaskCount scalableTaskCount = service01.getService()
                .autoScaleTaskCount(EnableScalingProps.builder()
                        .minCapacity(1)
                        .maxCapacity(2)
                        .build());

        scalableTaskCount.scaleOnCpuUtilization("service01AutoScaling",
                CpuUtilizationScalingProps.builder()
                        .targetUtilizationPercent(80)
                        .scaleInCooldown(Duration.seconds(60))
                        .scaleOutCooldown(Duration.seconds(60))
                        .build());

        //Grant publishing/consuming
        stockChangedTopic.getTopic().grantPublish(service01.getTaskDefinition().getTaskRole());
        gameStockEvents.grantConsumeMessages(service01.getTaskDefinition().getTaskRole());

        //Adding a subscription
        SqsSubscription sqsSubscription = SqsSubscription.Builder.create(gameStockEvents).build();
        stockChangedTopic.getTopic().addSubscription(sqsSubscription);

        gameEventsDdb.grantReadWriteData(service01.getTaskDefinition().getTaskRole());
    }
}
