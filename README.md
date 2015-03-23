Comp
====

Simple 4 bit virtual computer with assembler.

![screenshot](doc/screenshot.png)

How to run
----------

### UNIX
```bash
sudo apt-get install sbt=0.13.5
git clone https://github.com/gto76/comp.git
cd comp
sbt assembly
java -jar target/scala-2.11/comp-assembly-0.9.0.jar
```

### Windows
* Download and install [sbt 0.13.5](http://sourceforge.net/projects/gnuwin32/files/wget/1.11.4-1/wget-1.11.4-1-setup.exe/download)



Fibberochi numbers example
--------------------------

### Assembly code:
```
READ v2
ADD v1
WRITE v3
WRITE a15
READ v2
WRITE v1
READ v3
WRITE v2
SMALLER a1
JUMP a15
```

### Assembly code with variables changed to absolute adresses:
```
READ a14 
ADD a13
WRITE a12
WRITE a15
READ a14
WRITE a13
READ a12
WRITE a14
SMALLER a1
JUMP a15
```

All the data (values of the variables) has to be inserted in to binary manualy. Variables get adresses form the last adress backwards in order of aperance.

### Binary:
```
----***-
--*-**-*
---***--
---*****
----***-
---***-*
----**--
---****-
-***---*
-*--****
--------
--------
--------
-------*
-------*
```

To execute the programm you need to run Comp.scala with path to the binary file. Binary file represents the initial state of the memory (- is 0 and * is 1). It consists of 15 lines of 8 bits.

Assembly
--------
All statements consit of istruction keyword and an address. 

#### Instructions
* READ 		---- copy value at the address to register
* WRITE 	---* copy register to the adress
* ADD		--*- add value at the address to the register
* MINUS 	--** subtract value at the adress from the register 
* JUMP 		-*-- go to adress
* POINT 	-*-* copy value from the address that register is pointing to into register
* BIGGER	-**- go to the address if register contains value, larger or equal to 127
* SMALLER	-*** go to the address if register contains value, smaller then 127

#### Adresses are in three forms
* a<number>  -  absolute address 
* <number>   -  pointer to a value 
* v<number>  -  a variable
The actual computer only acepts absolute adresses. Other two options are managed by the asembler. The variables get at the end of the memory.


Binary
------

```
instructions - 4 bits
  |  +-- adresses - 4 bits
  v  v
----***-  <- 0  ----
--*-**-*  <- 1  ---*
---***--  <- 2  --*-
---*****  <- 3  --**
----***-  <- 4  -*--
---***-*  <- 5  -*-*
----**--  <- 6  -**-
---****-  <- 7  -***
-***---*  <- 8  *---
-*--****  <- 9  *--*
--------  <- 10 *-*-
--------  <- 11 *-**
--------  <- 12 **--
-------*  <- 13 **-*
-------*  <- 14 ***-
<OUTPUT>  <- 15 ****
```

* Execution starts at the first address (0). 
* Execution stops when programm reachess last adress (15)
* Output is achieved by writing to the last adress (15)
* Whatever is in the last adress (15) is considered as output and gets printed.
* Computer has one 8 bit register.




