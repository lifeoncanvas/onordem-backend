@echo off
echo Starting Honour Your Mentor Backend with Neon PostgreSQL...
set "JAVA_EXE=C:\Program Files\Java\jdk-27.0.1.0.3\bin\java.exe"
if not exist "%JAVA_EXE%" (
    set "JAVA_EXE=java"
)
cd /d "%~dp0"
powershell -NoProfile -ExecutionPolicy Bypass -Command "$cp = Get-Content 'target\classpath.txt'; & '%JAVA_EXE%' '-Dspring.profiles.active=neon' -cp $cp 'com.honouryourmentor.HonourYourMentorApplication'"

