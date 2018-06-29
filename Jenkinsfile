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
    sh "docker pull 192.168.84.23:5000/library/anyrobot-graph-baseimage:dev"
    withDockerContainer(args: "--name build-rainbow -v maven-repo:/root/.m2", image: "192.168.84.23:5000/library/anyrobot-graph-baseimage:dev") {
        echo "WORKSPACE is $WORKSPACE"
        sh "./is_es_start.sh"
        sh "curl -X PUT 192.168.84.30:9200/_template/graph-es -d @template"
        sh "cd $WORKSPACE/rainbow && mvn clean install && mkdir -p $WORKSPACE/report/codestyle_rainbow_results/ && cp ./target/checkstyle-result.xml $WORKSPACE/report/codestyle_rainbow_results/"
        sh "cd $WORKSPACE/report && tar -cvf ut_rainbow_coverage.tar ./ut_rainbow_coverage/"
    }
	
	    stage "Clear rainbow-ut env"
    sh "docker stop rainbow-es"
    sh "docker rm -f rainbow-es"

    stage 'Build Graph Server'
    sh "cd ./graph_server/locale && python ./make.py"

    stage "Package Graph Server"
    sh "tar -cvf anyrobot-graph-service-${ar_version}.tar ./graph_server"

    stage 'Graph Server UT & Pylint'
    sh "docker pull 192.168.84.23:5000/library/anyrobot-graph-baseimage:dev"
    withDockerContainer(args: "-v /home/jenkins/workspace:/home/jenkins/workspace -v /anyrobot:/anyrobot -e WORKSPACE=${WORKSPACE} ", image: "192.168.84.23:5000/library/anyrobot-graph-baseimage:dev") {
        sh "mkdir -p $WORKSPACE/report"
        sh "cd $WORKSPACE"
        int exitCode = sh script: 'pylint $WORKSPACE/graph_server/* --output-format=parseable > $WORKSPACE/report/pylint.xml', returnStatus: true

        exitCode = sh script: 'nosetests --where=$WORKSPACE/graph_server/tests --with-coverage --cover-xml --cover-xml-file=$WORKSPACE/report/ut_coverage.xml --with-xunit --xunit-file=$WORKSPACE/report/ut_results.xml --cover-package=$WORKSPACE/graph_server', returnStatus: true

        sh "mkdir -p anyrobot-tools"
        dir("anyrobot-tools")
        {
            git 'http://192.168.84.20/bigdata/anyrobot-tools.git'
        }
        sh "cp ./anyrobot-tools/errot-status-tools/collect-error-status.py $WORKSPACE/graph_server/utils/error"

        sh "mkdir -p $WORKSPACE/error-code"
        sh "python $WORKSPACE/graph_server/utils/error/collect-error-status.py -P ${WORKSPACE}/graph_server -D ${WORKSPACE}/error-code"

        sh "cd ./error-code && tar -cvf ${WORKSPACE}/anyrobot-graph-error-code.tar MC*"
    }
	
}