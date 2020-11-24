def getFolderName() {
  def array = pwd().split("/")
  return array[array.length - 2];
}
pipeline {
  agent any
  environment {
    BRANCHES = "${env.GIT_BRANCH}"
    COMMIT = "${env.GIT_COMMIT}"
    RELEASE_NAME = "javaspringboot"
    SERVICE_PORT = "${APP_PORT}"
    DOCKERHOST = "${DOCKERHOST_IP}"
    REGISTRY_URL = "${DOCKER_REPO_URL}"
    ACTION = "${ACTION}"
    PROMOTE_ID = "${PROMOTE_ID}"
    PROMOTE_STAGE = "${PROMOTE_STAGE}"
    BUILD_VERSION = "${BUILD_VERSION}"
    foldername = getFolderName()
    DEPLOYMENT_TYPE = "${DEPLOYMENT_TYPE}"
    KUBE_SECRET = "${KUBE_SECRET}"
    BUILD_TAG = "${JOB_BASE_NAME}-${env.ACTION == "PROMOTE"? env.PROMOTE_STAGE: env.foldername}-${BUILD_VERSION}"
    PROMOTE_TAG = "${JOB_BASE_NAME}-${foldername}-${PROMOTE_ID}"
    PROMOTE_SOURCE = "${JOB_BASE_NAME}-${foldername}-latest"
    CHROME_BIN = "/usr/bin/google-chrome"
    ARTIFACTORY = "${ARTIFACTORY}"
    USER_CREDENTIALS = credentials("${ARTIFACTORY_CREDENTIAL_ID}")
  }

  stages {
  stage('init') {
     steps {
         script {

           def job_name = "$env.JOB_NAME"
           print(job_name)
           def values = job_name.split('/')
           namespace = values[0].replaceAll("[^a-zA-Z0-9]+","").toLowerCase().take(50)
           service = values[2].replaceAll("[^a-zA-Z0-9]+","").toLowerCase().take(50)
           print(namespace)
           print(service)
           env.namespace_name=namespace
           env.service=service


         }
       }
     }
    stage('Unit Tests') {
        agent { label 'deployer' }

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
      agent { label 'deployer' }
      when {
        expression {
          env.ACTION == 'DEPLOY'
        }
      }
      steps {

        withSonarQubeEnv('pg-sonar') {
            sh "mvn --batch-mode -V -U -e org.sonarsource.scanner.maven:sonar-maven-plugin:3.5.0.1254:sonar -Dsonar.java.binaries='.' -Dsonar.exclusions='pom.xml, target/**/*' -Dsonar.projectKey=$RELEASE_NAME -Dsonar.projectName=$RELEASE_NAME"

        }
      }
    }
    stage('Build') {
        agent { label 'deployer' }

      when {
        expression {
          env.ACTION == 'DEPLOY'
        }
      }
      steps {
        echo "echoed folder--- $foldername"
        echo "echoed BUILD_TAG--- $BUILD_TAG"
        echo "echoed PROMOTE_TAG--- $PROMOTE_TAG"

        sh 'mvn clean install -Dmaven.test.skip=true'
        if (env.ARTIFACTORY == 'ECR') {
          sh 'eval $(aws ecr get-login --no-include-email | sed \'s|https://||\')'
        }
        if (env.ARTIFACTORY == 'JFROG') {
           sh '''
           docker login -u "$USER_CREDENTIALS_USR" -p "$USER_CREDENTIALS_PSW" "$REGISTRY_URL"
           '''
        }

        sh 'docker build -t "$REGISTRY_URL:$BUILD_TAG" -t "$REGISTRY_URL:latest" .'
        sh 'docker push "$REGISTRY_URL"'
      }
    }

    stage('Deploy') {
      when {
        expression {
          env.DEPLOYMENT_TYPE == 'EC2' && (env.ACTION == 'DEPLOY' || env.ACTION == 'PROMOTE' || env.ACTION == 'ROLLBACK')
        }
      }

      steps {
        echo "echoed folder--- $foldername"
        echo "echoed BUILD_TAG--- $BUILD_TAG"
        echo "echoed PROMOTE_TAG--- $PROMOTE_TAG"
        echo "echoed PROMOTE_SOURCE--- $PROMOTE_SOURCE"
        script {
          if (env.ACTION == 'PROMOTE') {
            echo "-------------------------------------- inside promote condition -------------------------------"
            sh 'ssh -o "StrictHostKeyChecking=no" ciuser@$DOCKERHOST "docker image tag "$REGISTRY_URL:$PROMOTE_SOURCE" "$REGISTRY_URL:$PROMOTE_TAG""'
            sh 'ssh -o "StrictHostKeyChecking=no" ciuser@$DOCKERHOST "docker push "$REGISTRY_URL:$PROMOTE_TAG""'
          }
        }
        if (env.ARTIFACTORY == 'ECR') {
          sh 'ssh -o "StrictHostKeyChecking=no" ciuser@$DOCKERHOST "`aws ecr get-login --no-include-email --region us-east-1`"'
        }
        if (env.ARTIFACTORY == 'JFROG') {
          sh 'ssh -o "StrictHostKeyChecking=no" ciuser@$DOCKERHOST "docker login -u "$USER_CREDENTIALS_USR" -p "$USER_CREDENTIALS_PSW" "$REGISTRY_URL""'
        }
        sh 'ssh -o "StrictHostKeyChecking=no" ciuser@$DOCKERHOST "sleep 5s"'
        sh 'ssh -o "StrictHostKeyChecking=no" ciuser@$DOCKERHOST "docker pull "$REGISTRY_URL:$BUILD_TAG""'
        sh 'ssh -o "StrictHostKeyChecking=no" ciuser@$DOCKERHOST "docker stop ${JOB_BASE_NAME} || true && docker rm ${JOB_BASE_NAME} || true"'
        sh 'ssh -o "StrictHostKeyChecking=no" ciuser@$DOCKERHOST "docker run -d --name ${JOB_BASE_NAME} -p $SERVICE_PORT:$SERVICE_PORT $REGISTRY_URL:$BUILD_TAG"'

        script {
          if (env.ACTION == 'PROMOTE' || env.ACTION == 'ROLLBACK') {
            echo "-------------------------------------- inside promote/rollback condition -------------------------------"
            sh 'ssh -o "StrictHostKeyChecking=no" ciuser@$DOCKERHOST "docker image tag "$REGISTRY_URL:$BUILD_TAG" "$REGISTRY_URL:$PROMOTE_SOURCE""'
            sh 'ssh -o "StrictHostKeyChecking=no" ciuser@$DOCKERHOST "docker push "$REGISTRY_URL:$PROMOTE_SOURCE""'
          }
        }
      }
    }
    stage('Deploy-To-Kube') {
      when {
        expression {
          env.DEPLOYMENT_TYPE == 'KUBERNETES' && (env.ACTION == 'DEPLOY' || env.ACTION == 'PROMOTE' || env.ACTION == 'ROLLBACK')
        }
      }

      steps {

        echo "echoed folder--- $foldername"
        echo "echoed BUILD_TAG--- $BUILD_TAG"
        echo "echoed PROMOTE_TAG--- $PROMOTE_TAG"
        echo "echoed PROMOTE_SOURCE--- $PROMOTE_SOURCE"
        script {
          if (env.ACTION == 'PROMOTE') {
            echo "-------------------------------------- inside promote condition -------------------------------"
            sh '''
              docker pull "$REGISTRY_URL:$PROMOTE_SOURCE"
              docker image tag "$REGISTRY_URL:$PROMOTE_SOURCE" "$REGISTRY_URL:$PROMOTE_TAG"
              docker push "$REGISTRY_URL:$PROMOTE_TAG"
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
                helm upgrade --install $RELEASE_NAME -n "$namespace_name" helm_chart --set image.repository="$REGISTRY_URL" --set image.tag="$BUILD_TAG" --set service.internalport="$SERVICE_PORT"
                sleep 10
                url=`kubectl get svc -n "$namespace_name" | grep "$RELEASE_NAME-$service" | awk '{print $4}'`
                echo "#&&# $url #&&#"
              '''
        }


        script {
          if (env.ACTION == 'PROMOTE' || env.ACTION == 'ROLLBACK') {
            echo "-------------------------------------- inside rollback condition -------------------------------"
            sh '''
              docker image tag "$REGISTRY_URL:$BUILD_TAG" "$REGISTRY_URL:$PROMOTE_SOURCE"
              docker push "$REGISTRY_URL:$PROMOTE_SOURCE"
            '''

          }
        }
      }
    }

    stage('Delete-helm-Deployment') {
      when {
        expression {
          env.DEPLOYMENT_TYPE == 'KUBERNETES' && env.ACTION == 'DESTROY'
        }
      }
      steps {
        withCredentials([file(credentialsId: "$KUBE_SECRET", variable: 'KUBECONFIG')]) {
              sh '''
                helm uninstall $RELEASE_NAME -n "$namespace_name"
              '''
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
        sh 'ssh -o "StrictHostKeyChecking=no" ciuser@$DOCKERHOST "docker stop ${JOB_BASE_NAME} || true && docker rm ${JOB_BASE_NAME} || true"'
      }
    }

  }
}
