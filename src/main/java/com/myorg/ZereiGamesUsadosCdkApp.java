package com.myorg;

import software.amazon.awscdk.App;

public class ZereiGamesUsadosCdkApp {
    public static void main(final String[] args) {
        App app = new App();

        var vpcStack = new VPCCdkStack(app, "VPC");

        var clusterStack = new ECSCdkStack(app, "Cluster", vpcStack.getVpc());
        clusterStack.addDependency(vpcStack);

        var rdsStack = new RDSCdkStack(app, "RDS", vpcStack.getVpc());
        rdsStack.addDependency(vpcStack);

        var snsStack = new SNSCdkStack(app, "SNS");

        var serviceStack = new Service01CdkStack(app, "Service01", clusterStack.getCluster(), snsStack.getGamesStockEventsTopic());
        serviceStack.addDependency(clusterStack);
        serviceStack.addDependency(rdsStack);
        serviceStack.addDependency(snsStack);

        app.synth();
    }
}

