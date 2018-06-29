#!groovy

node("docker") {
    stage "Env"
    echo "WORKSPACE is $WORKSPACE"
    echo "PWD is $PWD"

    stage "Checkout"
    git "https://github.com/baozengkai/xiaobao-graph.git"
}