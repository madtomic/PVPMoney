@echo off
mvn -DaltDeploymentRepository=snapshot-repo::default::file:dist/ clean deploy