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

	
}