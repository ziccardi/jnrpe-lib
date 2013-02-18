@echo off
setlocal enableextensions enabledelayedexpansion
set /A count=0

FOR /R "$INSTALL_PATH\bin\lib" %%G IN (*.*) DO (
  set /A count+=1
  echo wrapper.java.classpath.!count!=%%G >> "$INSTALL_PATH\wrapper.conf"
)

FOR %%G IN ("$INSTALL_PATH\bin\"*.jar) DO (
  set /A count+=1
  echo wrapper.java.classpath.!count!=%%G >> "$INSTALL_PATH\wrapper.conf"
)

set /A count+=1
echo wrapper.java.classpath.!count!=$INSTALL_PATH >> "$INSTALL_PATH\wrapper.conf"