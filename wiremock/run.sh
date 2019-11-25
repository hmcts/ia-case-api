#!/bin/bash
#
# Run a wiremock as a standalone dependency to represent Ref Data
#
# See ./mappings for stubbing configuration
#
java -jar wiremock-standalone-2.25.1.jar --port 8990 --verbose