package com.myorg;

import software.amazon.awscdk.App;

public class ZereiGamesUsadosCdkApp {
    public static void main(final String[] args) {
        App app = new App();

        var vpcStack = new VPCCdkStack(app, "VPC");

        var clusterStack = new ECSCdkStack(app, "Cluster", vpcStack.getVpc());
        clusterStack.addDependency(vpcStack);

        app.synth();
    }
}

