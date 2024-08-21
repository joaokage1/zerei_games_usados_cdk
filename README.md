# Tecnologias usadas

### AWS CloudFormation
Processo de criação de recursos automatizados (stacks),
com versionamento de templates e possibilidade de
definição utilizando ``json`` ou ``yml`` para definição da
arquitetura.

_O AWS CloudFormation é um serviço que ajuda você a
modelar e configurar seus recursos da AWS para despender
menos tempo gerenciando esses recursos e mais tempo se
concentrando em seus aplicativos executados AWS.
Você cria um modelo que descreve todos os recursos da
AWS desejados (como funções do Amazon EC2 e tabelas do
Amazon RDS), e o CloudFormation cuida do provisionamento
e da configuração desses recursos para você._

### AWS CDK
_AWS Cloud Development Kit (AWS CDK) É uma estrutura de
desenvolvimento de software de código aberto para definir
a infraestrutura de nuvem em código e provisioná-la por
meio dela. AWS CloudFormation_

Comandos utilizados:

``cdk init app --language java``
### [Referência](https://docs.aws.amazon.com/)
___

### AWS Fargate
_O AWS Fargate é um mecanismo de computação com 
tecnologia sem servidor e pagamento conforme o uso que 
permite que você se concentre no desenvolvimento de 
aplicações sem a necessidade de gerenciar servidores._

#### VPC
Recurso de criação de redes virtuais e permite isolamento 
dos componentes fora dessa rede.

#### Amazon ECS
Recurso de cluster gerado com fargate, o que retira 
necessidade de gerenciamento de instâncias.
Orquestrador de containers.

#### Auto Scaling
Recurso de auto escalonamento horizontal de criação/destruição de
instâncias baseados em métricas de utilização.

#### Application Load Balancer
Recurso de balanceamento de cargas entre instâncias.

### [Referência](https://aws.amazon.com/pt/fargate/)

#### RDS
É um serviço de banco de dados relacional de fácil 
gerenciamento e otimizado para o custo total de propriedade.
Use o comando seguinte para passar as variáveis:

`cdk deploy --parameters RDS:databasePassword=db-password RDS Service01` 

___
# GENERATED:
# Welcome to your CDK Java project!

This is a blank project for CDK development with Java.

The `cdk.json` file tells the CDK Toolkit how to execute your app.

It is a [Maven](https://maven.apache.org/) based project, so you can open this project with any Maven compatible Java IDE to build and run tests.

## Useful commands

 * `mvn package`     compile and run tests
 * `cdk ls`          list all stacks in the app
 * `cdk synth`       emits the synthesized CloudFormation template
 * `cdk deploy`      deploy this stack to your default AWS account/region
 * `cdk diff`        compare deployed stack with current state
 * `cdk docs`        open CDK documentation

Enjoy!
