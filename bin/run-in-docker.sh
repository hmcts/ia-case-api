#!/usr/bin/env sh

print_help() {
  echo "Script to run docker container for Immigration & Asylum case API

  Usage:

  ./run-in-docker.sh [OPTIONS]

  Options:
    --clean, -c                   Clean and install current state of source code
    --install, -i                 Install current state of source code
    --param PARAM=, -p PARAM=     Parse script parameter
    --help, -h                    Print this help block

  Available parameters:

  SERVER_PORT                     HTTP port number the API will listen on (default: 8090)
  "
}

GRADLE_CLEAN=false
GRADLE_INSTALL=false

execute_script() {
  cd $(dirname "$0")/..

  if [ ${GRADLE_CLEAN} = true ]
  then
    echo "Clearing previous build.."
    ./gradlew clean
  fi

  if [ ${GRADLE_INSTALL} = true ]
  then
    echo "Assembling distribution.."
    ./gradlew assemble
  fi

  echo "Assigning environment variables.."
  export SERVER_PORT="${SERVER_PORT:-8090}"

  chmod +x bin/*

  echo "Bringing up docker containers.."
  docker-compose up
}

while true ; do
  case "$1" in
    -h|--help) print_help ; shift ; break ;;
    -c|--clean) GRADLE_CLEAN=true ; GRADLE_INSTALL=true ; shift ;;
    -i|--install) GRADLE_INSTALL=true ; shift ;;
    -p|--param)
      case "$2" in
        SERVER_PORT=*) SERVER_PORT="${2#*=}" ; shift 2 ;;
        *) shift 2 ;;
      esac ;;
    *) execute_script ; break ;;
  esac
done
