@echo off
title Dokumentinnsending Jetty Server
setlocal

%~d0
cd %~p0

mvn test-compile exec:exec -Pjetty