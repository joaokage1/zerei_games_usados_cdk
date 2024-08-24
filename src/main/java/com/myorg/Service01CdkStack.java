package com.myorg;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.applicationautoscaling.EnableScalingProps;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck;
import software.amazon.awscdk.services.events.targets.SnsTopic;
import software.amazon.awscdk.services.logs.LogGroup;
import software.constructs.Construct;

import java.util.HashMap;
import java.util.Map;

public class Service01CdkStack extends Stack {
    public Service01CdkStack(final Construct scope, final String id, final Cluster cluster, final SnsTopic stockChangedTopic) {
        this(scope, id, null, cluster, stockChangedTopic);
    }

    public Service01CdkStack(final Construct scope, final String id, final StackProps props, final Cluster cluster, final SnsTopic stockChangedTopic) {
        super(scope, id, props);

        Map<String, String> envVariables = new HashMap<>();
        envVariables.put("SPRING_DATASOURCE_URL", "jdbc:mariadb://"
                + Fn.importValue("uri-db")
                + ":3306/aws_project01_db?createDatabaseIfNotExist=true");
        envVariables.put("SPRING_DATASOURCE_USERNAME", "service01app");
        envVariables.put("SPRING_DATASOURCE_PASSWORD", Fn.importValue("password-db"));
        envVariables.put("AWS_REGION", "us-east-1");
        envVariables.put("AWS_SNS_STOCK_TOPIC", stockChangedTopic.getTopic().getTopicArn());

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

        stockChangedTopic.getTopic().grantPublish(service01.getTaskDefinition().getTaskRole());
    }
}
