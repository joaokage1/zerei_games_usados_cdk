package com.myorg;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.Vpc;
import software.constructs.Construct;

public class VPCCdkStack extends Stack {

    private final Vpc vpc;

    public VPCCdkStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public VPCCdkStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        vpc = Vpc.Builder.create(this, "Vpc01")
                .maxAzs(2)
                .natGateways(0)
                .build();
    }

    public Vpc getVpc() {
        return vpc;
    }
}
