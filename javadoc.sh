#!/bin/bash

javadoc -sourcepath  "." -d "doc/server/" -subpackages server
javadoc -sourcepath  "." -d "doc/client/" -subpackages client
javadoc -sourcepath  "." -d "doc/common/" -subpackages common
