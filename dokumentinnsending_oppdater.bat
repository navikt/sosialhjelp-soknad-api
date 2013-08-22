@echo off
title Oppdater dokumentinnsending
setlocal

%~d0
cd %~p0

svn up
