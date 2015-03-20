
echo off

mkdir bin\lib 
mkdir bin\data 
scalac -d bin -classpath lib\commons-cli-1.2.jar src\main\scala\*
copy lib\* bin\lib 
copy data\* bin\data

cd bin
scala -classpath lib\commons-cli-1.2.jar;. Comp

