@echo off
rem start ../RocatMonitorVisualizer/RocatMonitorVisualizer.exe
rem timeout 10
start java -javaagent:"../RocatMonitorAgent/RocatMonitorAgent.jar" -jar PQSIM.jar
