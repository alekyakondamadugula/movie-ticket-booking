pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps { checkout scm }
        }
        stage('Maven tests and package') {
            steps {
                dir('user-service') { bat 'mvn clean package' }
                dir('movie-service') { bat 'mvn clean package' }
                dir('booking-service') { bat 'mvn clean package' }
            }
        }
        stage('Build Docker images') {
            steps {
                dir('user-service') { bat 'docker build -t user-service:latest .' }
                dir('movie-service') { bat 'docker build -t movie-service:latest .' }
                dir('booking-service') { bat 'docker build -t booking-service:latest .' }
            }
        }
    }
}
