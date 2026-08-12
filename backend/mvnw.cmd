@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM ----------------------------------------------------------------------------
@REM Maven Start Up Batch script
@REM
@REM Required ENV vars:
@REM JAVA_HOME - location of a JDK home dir
@REM
@REM Optional ENV vars
@REM MAVEN_BATCH_ECHO - set to 'on' to enable the echo of the batch commands
@REM MAVEN_BATCH_PAUSE - set to 'on' to wait for a keystroke before ending
@REM MAVEN_OPTS - parameters passed to the Java VM when running Maven
@REM     e.g. to debug Maven itself, use
@REM set MAVEN_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000
@REM MAVEN_SKIP_RC - flag to disable loading of mavenrc files
@REM ----------------------------------------------------------------------------

@IF "%MAVEN_BATCH_ECHO%" == "on"  echo %MAVEN_BATCH_ECHO%

@REM Set local scope for the variables with windows NT shell
@IF "%OS%"=="Windows_NT" @SETLOCAL
@IF "%OS%"=="Windows_NT" @SETLOCAL enabledelayedexpansion

@REM ==== START VALIDATION ====
@IF NOT "%JAVA_HOME%" == "" (
  @IF EXIST "%JAVA_HOME%\bin\java.exe" (
    @SET "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
    GOTO java_check_done
  )
)
@FOR %%i IN (java.exe) DO @SET "JAVA_EXE=%%~$PATH:i"
@IF "%JAVA_EXE%"=="" (
  @echo.
  @echo Error: JAVA_HOME not found in your environment. >&2
  @echo Please set the JAVA_HOME variable in your environment to match the >&2
  @echo location of your Java installation. >&2
  @echo.
  GOTO error
)

:java_check_done

@REM ==== END VALIDATION ====

@SET MAVEN_PROJECTBASEDIR=%~dp0

@REM Find the project base dir, i.e. the directory that contains the folder ".mvn".
@REM Fallback to current working directory if not found.
@SET EXEC_DIR=%CD%
@SET WDIR=%EXEC_DIR%
:findBaseDir
@IF EXIST "%WDIR%"\.mvn SET MAVEN_PROJECTBASEDIR=%WDIR%
@cd "%WDIR%\.." 2>NUL
@SET WDIR=%CD%
@IF NOT "%EXEC_DIR%"=="%WDIR%" GOTO findBaseDir

@SET WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
@SET WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain
@SET WRAPPER_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar"

@IF NOT EXIST %WRAPPER_JAR% (
  @echo Downloading Maven Wrapper...
  @powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::TLS12; Invoke-WebRequest -Uri '%WRAPPER_URL%' -OutFile '%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar'"
)

@SET DOWNLOAD_URL=
@FOR /F "usebackq tokens=1,2 delims==" %%A IN ("%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties") DO (
  @IF "%%A" == "distributionUrl" SET DOWNLOAD_URL=%%B
)

@cd "%EXEC_DIR%"
@"%JAVA_EXE%" %MAVEN_OPTS% -classpath %WRAPPER_JAR% "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" %WRAPPER_LAUNCHER% %MAVEN_CONFIG% %*
@IF "%OS%"=="Windows_NT" @ENDLOCAL
GOTO end

:error
@SET ERROR_CODE=1
IF "%OS%"=="Windows_NT" ENDLOCAL
GOTO mvnEnd

:end
@IF "%MAVEN_BATCH_PAUSE%" == "on" pause
@IF NOT "%MAVEN_BATCH_EXIT_CODE%" == "" EXIT /B %MAVEN_BATCH_EXIT_CODE%
@IF NOT "%MAVEN_BATCH_PAUSE%" == "on" EXIT /B %ERROR_CODE%

:mvnEnd
EXIT /B %ERROR_CODE%
