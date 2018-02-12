node('scala-docker') {
  stage('Checkout') {
    checkout scm
  }

  try {
    stage('Build and test') {
      sh 'sbt -no-colors validate doc swaggerJson manifest'
    }
  } finally {
    stage('Archive coverage report') {
      archiveArtifacts "target/scala-2.12/scoverage-report/**"
      publishHTML(target: [reportName: "Code coverage report", reportDir: "target/scala-2.12/scoverage-report", reportFiles: "index.html"])
    }

    stage('Archive vulnerability report') {
      archiveArtifacts "target/scala-2.12/dependency-check-report.html"
      publishHTML(target: [reportName: "Vulnerabilty report", reportDir: "target/scala-2.12", reportFiles: "dependency-check-report.html"])
    }
  }

  stage('Archive Scaladoc') {
    archiveArtifacts "target/scala-2.12/api/**"
    publishHTML(target: [reportName: "Scaladoc", reportDir: "target/scala-2.12/api", reportFiles: "index.html"])
  }

  stage('Archive Swagger doc') {
    archiveArtifacts "target/swagger/**"
    publishHTML(target: [reportName: "Swagger JSON", reportDir: "target/swagger", reportFiles: "api-docs.json"])
  }

  stage('Archive Kubernetes deployment') {
    archiveArtifacts "target/deployment.yaml"
    publishHTML(target: [reportName: "Kubernetes deployment", reportDir: "target", reportFiles: "deployment.yaml"])
  }

  def imageName = sh(returnStdout: true, script: 'sbt -no-colors docker:packageName 2>&1 | tail -1 | sed -e "s|^\\[info\\] ||"')
  def imageTag = sh(returnStdout: true, script: 'sbt -no-colors docker:version 2>&1 | tail -1 | sed -e "s|^\\[info\\] ||"')
  def dockerImage = "${imageName.trim()}:${imageTag.trim()}"

  currentBuild.description = "${dockerImage}"

  stage('Publish') {
    sh 'sbt -no-colors docker:publish'
  }

  stage('Squash image') {
    build job: '/Docker/Squash image', parameters: [
      [$class: 'StringParameterValue', name: 'INPUT_IMAGE', value: "${dockerImage}"],
      [$class: 'StringParameterValue', name: 'OUTPUT_IMAGE', value: "${dockerImage}"]
    ]
  }

  stage('Vulnerability scan') {
    build job: '/Docker/Clair scan', parameters: [
      [$class: 'StringParameterValue', name: 'DOCKER_IMAGE', value: "${dockerImage}"],
      [$class: 'StringParameterValue', name: 'MINIMUM_SEVERITY', value: "Negligible"],
      [$class: 'StringParameterValue', name: 'MINIMUM_FAILURE', value: "High"]
    ]
  }
}
