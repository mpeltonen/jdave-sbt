set SCRIPT_DIR=%~dp0
java -Dfile.encoding=UTF8 -Xmx1536M -Xss1M -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=256m -jar "%SCRIPT_DIR%sbt-launch.jar" %*
