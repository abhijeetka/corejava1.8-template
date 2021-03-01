def getFolderName() {
  def array = pwd().split("/")
  return array[array.length - 2];
}
pipeline {
  agent any
  environment {
    BRANCHES = "${env.GIT_BRANCH}"
    COMMIT = "${env.GIT_COMMIT}"
    RELEASE_NAME = "corejava"
    SERVICE_PORT = "${APP_PORT}"
    DOCKERHOST = "${DOCKERHOST_IP}"
    REGISTRY_URL = "${DOCKER_REPO_URL}"
    ACTION = "${ACTION}"
    PROMOTE_ID = "${PROMOTE_ID}"
    PROMOTE_STAGE = "${PROMOTE_STAGE}"
    PROMOTE_JOB_NAME = "${PROMOTE_JOB_NAME}"
    BUILD_VERSION = "${BUILD_VERSION}"
    foldername = getFolderName()
    DEPLOYMENT_TYPE = "${DEPLOYMENT_TYPE == ""? "EC2":DEPLOYMENT_TYPE}"
    KUBE_SECRET = "${KUBE_SECRET}"
    BUILD_TAG = "${env.ACTION == "PROMOTE"? env.PROMOTE_JOB_NAME == "null"?env.JOB_BASE_NAME:env.PROMOTE_JOB_NAME : env.JOB_BASE_NAME}-${env.ACTION == "PROMOTE"? env.PROMOTE_STAGE: env.foldername}-${BUILD_VERSION}"
    PROMOTE_TAG = "${JOB_BASE_NAME}-${foldername}-${PROMOTE_ID}"
    PROMOTE_SOURCE = "${JOB_BASE_NAME}-${foldername}-latest"
    CHROME_BIN = "/usr/bin/google-chrome"
    ARTIFACTORY = "${ARTIFACTORY == ""? "ECR":ARTIFACTORY}"
    ARTIFACTORY_CREDENTIALS = "${ARTIFACTORY_CREDENTIAL_ID}"
  }

  stages {
    stage('init') {
      steps {
        script {

          def job_name = "$env.JOB_NAME"
          print(job_name)
          def values = job_name.split('/')
          namespace_prefix = values[0].replaceAll("[^a-zA-Z0-9]+","").toLowerCase().take(50)
          namespace = "$namespace_prefix-$env.foldername".toLowerCase()
          service = values[2].replaceAll("[^a-zA-Z0-9]+","").toLowerCase().take(50)
          print("kube namespace: $namespace")
          print("service name: $service")
          env.namespace_name=namespace
          env.service=service
          if (env.ARTIFACTORY == "ECR") {

            def url_string = "$REGISTRY_URL"
            url = url_string.split('\\.')
            env.AWS_ACCOUNT_NUMBER = url[0]
            env.ECR_REGION = url[3]
            echo "ecr region: $ECR_REGION"
            echo "ecr acc no: $AWS_ACCOUNT_NUMBER"

            if (env.ARTIFACTORY_CREDENTIALS != null) {
                  withCredentials([string(credentialsId: "$ARTIFACTORY_CREDENTIALS", variable: 'awskey')]) {
                    script {

                            def string = "$awskey"
                            def data = string.split(',')
                            env.aws_region = data[0]
                            env.aws_access_key = data[1]
                            env.aws_secret_key = data[2]
                            env.aws_role_arn = data[3]
                            env.aws_external_id = data[4]

                      }
                    }
                  if (env.aws_role_arn != 'null') {
                    env.sts_credentails = sh (returnStdout: true, script: '''
                                              set +x
                                              export AWS_ACCESS_KEY_ID=$aws_access_key
                                              export AWS_SECRET_ACCESS_KEY=$aws_secret_key
                                              aws sts assume-role --role-arn $aws_role_arn --role-session-name tests --external-id $aws_external_id | jq -r .Credentials
                                              set -x
                                               ''').trim()
                    env.AWS_ACCESS_KEY_ID = sh (returnStdout: true, script: ''' echo ${sts_credentails} | jq -r .AccessKeyId ''').trim()
                    env.AWS_SECRET_ACCESS_KEY = sh (returnStdout: true, script: ''' echo ${sts_credentails} | jq -r .SecretAccessKey ''').trim()
                    env.AWS_SESSION_TOKEN = sh (returnStdout: true, script: ''' echo ${sts_credentails} | jq -r .SessionToken ''').trim()


                  } else {
                    env.AWS_ACCESS_KEY_ID = "$aws_access_key"
                    env.AWS_SECRET_ACCESS_KEY  = "$aws_secret_key"
                  }
              } else {
                env.AWS_ACCESS_KEY_ID = ""
                env.AWS_SECRET_ACCESS_KEY  = ""
              }
          }
        }
      }
    }
    stage('Unit Tests') {
        agent any

      when {
        expression {
          env.ACTION == 'DEPLOY'
        }
      }
      steps {
        sh 'mvn clean test --batch-mode'
      }
    }
    stage('SonarQube Scan') {
      agent any
      when {
        expression {
          env.ACTION == 'DEPLOY'
        }
      }
      steps {

        withSonarQubeEnv('pg-sonar') {
            sh "mvn --batch-mode -V -U -e org.sonarsource.scanner.maven:sonar-maven-plugin:3.5.0.1254:sonar -Dsonar.java.binaries='.' -Dsonar.exclusions='pom.xml, target/**/*' -Dsonar.projectKey=$service -Dsonar.projectName=$service"

        }
      }
    }
    stage('Build') {
        agent any

      when {
        expression {
          env.ACTION == 'DEPLOY'
        }
      }
      steps {
        script {
          echo "echoed folder--- $foldername"
          echo "echoed BUILD_TAG--- $BUILD_TAG"
          echo "echoed PROMOTE_TAG--- $PROMOTE_TAG"

          sh 'mvn clean install -Dmaven.test.skip=true'
          if (env.ARTIFACTORY == 'ECR') {
            sh 'set +x; eval $(aws ecr get-login --no-include-email --registry-ids "$AWS_ACCOUNT_NUMBER" --region "$ECR_REGION" | sed \'s|https://||\') ;set -x'
          }
          if (env.ARTIFACTORY == 'JFROG') {
              withCredentials([usernamePassword(credentialsId: "$ARTIFACTORY_CREDENTIALS", usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                  sh 'docker login -u "$USERNAME" -p "$PASSWORD" "$REGISTRY_URL"'
              }
          }
          if (env.ARTIFACTORY == 'ACR') {
              withCredentials([usernamePassword(credentialsId: "$ARTIFACTORY_CREDENTIALS", usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                  sh 'docker login -u "$USERNAME" -p "$PASSWORD" "$REGISTRY_URL"'

                }
          }

          sh 'docker build -t "$REGISTRY_URL:$BUILD_TAG" .'
          sh 'docker push "$REGISTRY_URL:$BUILD_TAG"'
          sh 'docker rmi "$REGISTRY_URL:$BUILD_TAG" || true'
        }

      }
    }

    stage('Deploy') {
      when {
        expression {
          env.ACTION == 'DEPLOY' || env.ACTION == 'PROMOTE' || env.ACTION == 'ROLLBACK'
        }
      }

      steps {
        script {
          echo "echoed folder--- $foldername"
          echo "echoed BUILD_TAG--- $BUILD_TAG"
          echo "echoed PROMOTE_TAG--- $PROMOTE_TAG"
          echo "echoed PROMOTE_SOURCE--- $PROMOTE_SOURCE"
          if (env.DEPLOYMENT_TYPE == 'EC2') {
            if (env.ARTIFACTORY == 'ECR') {
              sh 'set +x; ssh -o "StrictHostKeyChecking=no" ciuser@$DOCKERHOST "AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY AWS_SESSION_TOKEN=$AWS_SESSION_TOKEN `aws ecr get-login --no-include-email --region "$ECR_REGION" --registry-ids "$AWS_ACCOUNT_NUMBER"` " ;set -x'
            }
            if (env.ARTIFACTORY == 'JFROG') {
              withCredentials([usernamePassword(credentialsId: "$ARTIFACTORY_CREDENTIALS", usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                  sh 'ssh -o "StrictHostKeyChecking=no" ciuser@$DOCKERHOST "docker login -u "$USERNAME" -p "$PASSWORD" "$REGISTRY_URL""'
              }
            }
            if (env.ARTIFACTORY == 'ACR') {
              withCredentials([usernamePassword(credentialsId: "$ARTIFACTORY_CREDENTIALS", usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                  sh 'ssh -o "StrictHostKeyChecking=no" ciuser@$DOCKERHOST "docker login -u "$USERNAME" -p "$PASSWORD" "$REGISTRY_URL""'
              }
            }

            if (env.ACTION == 'PROMOTE') {
              echo "-------------------------------------- inside promote condition -------------------------------"
              sh 'ssh -o "StrictHostKeyChecking=no" ciuser@$DOCKERHOST "docker image tag "$REGISTRY_URL:$PROMOTE_SOURCE" "$REGISTRY_URL:$PROMOTE_TAG""'
              sh 'ssh -o "StrictHostKeyChecking=no" ciuser@$DOCKERHOST "docker push "$REGISTRY_URL:$PROMOTE_TAG""'
            }

            sh 'ssh -o "StrictHostKeyChecking=no" ciuser@$DOCKERHOST "sleep 5s"'
            sh 'ssh -o "StrictHostKeyChecking=no" ciuser@$DOCKERHOST "docker pull "$REGISTRY_URL:$BUILD_TAG""'
            sh 'ssh -o "StrictHostKeyChecking=no" ciuser@$DOCKERHOST "docker stop ${JOB_BASE_NAME} || true && docker rm ${JOB_BASE_NAME} || true"'
            sh 'ssh -o "StrictHostKeyChecking=no" ciuser@$DOCKERHOST "docker run -d --name ${JOB_BASE_NAME} -p $SERVICE_PORT:$SERVICE_PORT $REGISTRY_URL:$BUILD_TAG"'

            if (env.ACTION == 'PROMOTE' || env.ACTION == 'ROLLBACK') {
              echo "-------------------------------------- inside promote/rollback condition -------------------------------"
              sh 'ssh -o "StrictHostKeyChecking=no" ciuser@$DOCKERHOST "docker image tag "$REGISTRY_URL:$BUILD_TAG" "$REGISTRY_URL:$PROMOTE_SOURCE""'
              sh 'ssh -o "StrictHostKeyChecking=no" ciuser@$DOCKERHOST "docker push "$REGISTRY_URL:$PROMOTE_SOURCE""'
            }

          }
          if (env.DEPLOYMENT_TYPE == 'KUBERNETES') {
            if (env.ACTION == 'PROMOTE') {
              echo "-------------------------------------- inside promote condition -------------------------------"
              sh '''
                docker pull "$REGISTRY_URL:$PROMOTE_SOURCE"
                docker image tag "$REGISTRY_URL:$PROMOTE_SOURCE" "$REGISTRY_URL:$PROMOTE_TAG"
                docker push "$REGISTRY_URL:$PROMOTE_TAG"
              '''
            }
            if (env.ARTIFACTORY == 'JFROG') {
                withCredentials([file(credentialsId: "$KUBE_SECRET", variable: 'KUBECONFIG'), usernamePassword(credentialsId: "$ARTIFACTORY_CREDENTIALS", usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                  sh '''
                    kubectl create ns "$namespace_name" || true
                    kubectl -n "$namespace_name" create secret docker-registry regcred --docker-server="$REGISTRY_URL" --docker-username="$USERNAME" --docker-password="$PASSWORD" || true
                  '''
              }
            }
            
            if (env.ARTIFACTORY == 'ACR') {
                withCredentials([file(credentialsId: "$KUBE_SECRET", variable: 'KUBECONFIG'), usernamePassword(credentialsId: "$ARTIFACTORY_CREDENTIALS", usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                  sh '''
                    kubectl create ns "$namespace_name" || true
                    kubectl -n "$namespace_name" create secret docker-registry regcred --docker-server="$REGISTRY_URL" --docker-username="$USERNAME" --docker-password="$PASSWORD" || true
                  '''
              }
            }
            withCredentials([file(credentialsId: "$KUBE_SECRET", variable: 'KUBECONFIG')]) {
                  sh '''
                    rm -rf kube
                    mkdir -p kube
                    cp "$KUBECONFIG" kube
                    sed -i s+#SERVICE_NAME#+"$service"+g ./helm_chart/values.yaml ./helm_chart/Chart.yaml
                    kubectl create ns "$namespace_name" || true
                    helm upgrade --install $RELEASE_NAME -n "$namespace_name" helm_chart --atomic --timeout 300s --set image.repository="$REGISTRY_URL" --set image.tag="$BUILD_TAG" --set image.registrySecret="regcred"  --set service.internalport="$SERVICE_PORT"
                    sleep 10
                  '''
                  script {
                    env.temp_service_name = "$RELEASE_NAME-$service".take(63)
                    def url = sh (returnStdout: true, script: '''kubectl get svc -n "$namespace_name" | grep "$temp_service_name" | awk '{print $4}' ''').trim()
                    if (url != "<pending>") {
                      print("##\$@\$ http://$url ##\$@\$")
                    }
                  }
            }
            if (env.ACTION == 'PROMOTE' || env.ACTION == 'ROLLBACK') {
              echo "-------------------------------------- inside rollback condition -------------------------------"
              sh '''
                docker pull "$REGISTRY_URL:$BUILD_TAG"
                docker image tag "$REGISTRY_URL:$BUILD_TAG" "$REGISTRY_URL:$PROMOTE_SOURCE"
                docker push "$REGISTRY_URL:$PROMOTE_SOURCE"
              '''

            }
          }
        }
      }
    }

    stage('Destroy') {
      when {
        expression {
          env.DEPLOYMENT_TYPE == 'EC2' && env.ACTION == 'DESTROY'
        }
      }
      steps {
        script {
          if (env.DEPLOYMENT_TYPE == 'EC2') {
            sh 'ssh -o "StrictHostKeyChecking=no" ciuser@$DOCKERHOST "docker stop ${JOB_BASE_NAME} || true && docker rm ${JOB_BASE_NAME} || true"'
          }
          if (env.DEPLOYMENT_TYPE == 'KUBERNETES') {
            withCredentials([file(credentialsId: "$KUBE_SECRET", variable: 'KUBECONFIG')]) {
                  sh '''
                    helm uninstall $RELEASE_NAME -n "$namespace_name"
                  '''
            }
          }
        }
      }
    }
  }
}
