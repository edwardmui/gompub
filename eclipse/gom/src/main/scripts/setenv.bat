REM This script is designed to run in the gom installed directory where certain directory structure is expected.

set projDir=..
cd %projDir%
set classpath=.\;gom.jar
set javaCmd="%JAVA_HOME%\bin\java" -D"file.encoding=UTF-8" -cp .;gom.jar 

REM chcp: change code page from default - usually 437 for United States- to 65001 UTF-8
REM The font must be a TrueType font. NSimSun works for displaying Chinese.
REM font can be set on the Windows CMD window via the "file" -> properties -> Font -> NSimSun.
REM once that's set, future start up of the CMD window uses the same font selection.
chcp 65001
