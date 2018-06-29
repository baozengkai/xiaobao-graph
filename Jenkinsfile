#!groovy

node("docker") {
    stage "Env"
    echo "WORKSPACE is $WORKSPACE"
    echo "PWD is $PWD"

    stage "Checkout"
    git "https://github.com/baozengkai/xiaobao-graph.git"
	
	
	sh "rm -rf anyrobot-version && mkdir -p anyrobot-version"
    dir("anyrobot-version") {
        git 'http://192.168.84.20/bigdata/anyrobot-version.git'
    }
	
	stage 'Gen Version'
    def arversion = load("anyrobot-version/arversion.groovy")
    def ar_version_major = arversion.get_version_major()
    def ar_version_minor = arversion.get_version_minor()
    def ar_version_revision = arversion.get_version_revision()
    def ar_version="${ar_version_major}.${ar_version_minor}.${ar_version_revision}"
    def ar_version_full="${ar_version}.${env.BUILD_ID}"

    sh "echo -e {\\\"major\\\":${ar_version_major}, \\\"minor\\\":${ar_version_minor}, \\\"revision\\\":${ar_version_revision}, \\\"build\\\":\\\"${env.BUILD_ID}\\\", \\\"buildDate\\\":\\\"`date \'+%Y-%m-%d\'`\\\"} > docker/version.json"

	stage "Prepare rainbow-ut env"
	sh "docker stop rainbow-es  || echo"
    sh "docker rm -f rainbow-es || echo"
    sh "docker pull 192.168.84.23:5000/library/elasticsearch:dcos-5.0.2"
    sh "docker run -itd --name rainbow-es --network host -e CLUSTER_NAME=pool -e NODE_NAME=es1 -e NODE_MASTER=true -e PUBLISH_HOST=127.0.0.1 -e HTTP_PORT=9200 -e TCP_PORT=9300 -e PING_UNICAST_HOSTS=127.0.0.1:9300 192.168.84.23:5000/library/elasticsearch:dcos-5.0.2"

}