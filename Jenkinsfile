pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps { checkout scm }
        }
        stage('Maven tests and package') {
            steps {
                dir('user-service') { sh 'mvn clean package' }
                dir('movie-service') { sh 'mvn clean package' }
                dir('booking-service') { sh 'mvn clean package' }
            }
        }
        stage('Build Docker images') {
            steps {
                dir('user-service') { sh 'docker build -t user-service:latest .' }
                dir('movie-service') { sh 'docker build -t movie-service:latest .' }
                dir('booking-service') { sh 'docker build -t booking-service:latest .' }
            }
        }
    }
}
