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
    BUILD_TAG = "${JOB_BASE_NAME}-${env.ACTION == "PROMOTE"? env.PROMOTE_STAGE: env.foldername}-${BUILD_VERSION}"
    PROMOTE_TAG = "${JOB_BASE_NAME}-${foldername}-${PROMOTE_ID}"
    PROMOTE_SOURCE = "${JOB_BASE_NAME}-${foldername}-latest"
    CHROME_BIN = "/usr/bin/google-chrome"
  }

  stages {
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
        sh 'aws ecr get-login --no-include-email --region us-east-1'
        sh 'docker build -t "$REGISTRY_URL:$BUILD_TAG" -t "$REGISTRY_URL:latest" .'
        sh 'eval $(aws ecr get-login --no-include-email | sed \'s|https://||\')'
        sh 'docker push "$REGISTRY_URL"'
      }
    }

    stage('Deploy') {
      when {
        expression {
          env.ACTION == 'DEPLOY' || env.ACTION == 'PROMOTE' || env.ACTION == 'ROLLBACK'
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
    stage('Destroy') {
      when {
        expression {
          env.ACTION == 'DESTROY'
        }
      }
      steps {
        sh 'ssh -o "StrictHostKeyChecking=no" ciuser@$DOCKERHOST "docker stop ${JOB_BASE_NAME} || true && docker rm ${JOB_BASE_NAME} || true"'
      }
    }

  }
}