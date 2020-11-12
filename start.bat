@echo off
java -Xms4G -Xmx4G -XX:+UseG1GC -XX:MinHeapFreeRatio=15 -XX:MaxHeapFreeRatio=30 -jar AC2.jar
echo.
echo.
pause