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

	
	stage "Build rainbow"
    sh "docker volume create --name maven-repo"
	sh "docker volume create --name gradle-repo"
    sh "docker pull 192.168.84.23:5000/library/anyrobot-graph-baseimage:dev"
    withDockerContainer(args: "--name build-rainbow -v gradle-repo:/root/.gradle -v maven-repo:/root/.m2", image: "192.168.84.23:5000/library/anyrobot-graph-baseimage:dev") {
        echo "WORKSPACE is $WORKSPACE"
        sh "./is_es_start.sh"
        sh "curl -X PUT 192.168.84.30:9200/_template/graph-es -d @template"
        sh "cd $WORKSPACE/rainbow && mvn clean install && mkdir -p $WORKSPACE/report/codestyle_rainbow_results/ && cp ./target/checkstyle-result.xml $WORKSPACE/report/codestyle_rainbow_results/"
        sh "cd $WORKSPACE/report && tar -cvf ut_rainbow_coverage.tar ./ut_rainbow_coverage/"
		
		sh "cd $WORKSPACE/graph_server && gradle clean check && cp ./build/reports/codenarc/main.html $WORKSPACE/report/codestyle_graph_results/ && cp ./build/test-results/test/TEST-graph_server.GraphServiceSpec.xml $WORKSPACE/report/ut_graph_results/"
		sh "cd $WORKSPACE/graph_server && gradle clean cobertura && cp ./build/reports/cobertura/coverage.xml $WORKSPACE/report/ut_graph_coverage/ "
   }

}