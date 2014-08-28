import scala.io.Source
import sys.exit

object Assembler {
    
    val MEMORY_LENGTH = 15
    val MEMORY_WIDTH = 8
    
    val DEBUG = true
    val TEST_FILE = "/home/minerva/code/scala/Comp/src/d"
    
    var binaryCode = getEmptyMemory
        
    def main(args: Array[String]) {
    	val assemblyStatements: Array[String] = readAssemblyFromFile(TEST_FILE);
    	var i = 0
    	for (line <- assemblyStatements) {
    	    val tokens = line.split(' ')
    	    val instructionCode = Commands.get(tokens(0))
    	    val addressCode = Addresses.get(tokens(1)) 
    	    val sentenceCode = instructionCode ++ addressCode
    	    binaryCode(i) = sentenceCode
    	    i=i+1
    	}
    	print(Comp.getString(binaryCode))
    	if (Addresses.getNumOfAdresses + i > MEMORY_LENGTH) {
    	    println("OUT OF MEMORY")
    	    exit(5)
    	}
    }
    
    def getEmptyMemory() = {
       var mem = new Array[Array[Boolean]](MEMORY_LENGTH) 
       for (i <- 0 to MEMORY_LENGTH-1) {
           mem(i) = new Array[Boolean](MEMORY_WIDTH)
       }
       mem
    }
    
    def writeToAddress(address: Int, value : Array[Boolean]) = {
        binaryCode(address) = value
    }
    
    def readAssemblyFromFile(fileName: String): Array[String] = {
        var data = new Array[String](Source.fromFile(fileName).getLines().length)
        println("filename: "+fileName)
		var i = 0
        for(line <- Source.fromFile(fileName).getLines()) {
        	println(line)
        	data(i) = line
	  		i = i+1
        }
	  	data
    }
    
    def getString(sss: Array[String]) = {
        var sOut = ""
        for (s <- sss) {
            sOut = sOut + s
        }
        sOut
    }
    
	    
	object Addresses {
	    
	    private val addresses = collection.mutable.Map[String, Int]()
	    
	    private def getAddress(name : String): Int = {
	         val adr = addresses.get(name)
	         if (adr.isEmpty) {
	        	 val numOfVars = addresses.size
	        	 val address = (MEMORY_LENGTH-1) - numOfVars
	             addresses += (name -> address)
	             address
	         } 
	         else {
	             adr.get
	         }
	    }
	    
	    def getNumOfAdresses = addresses.size
	    
	    // a19 -> absolute address
	    // 10 -> pointer to value (not logical for WRITE, JUMP, POINT, BIGGER
	    // v1 -> variable (not logical for JUMP, POINT, BIGGER
	    def get(adrStr: String): Array[Boolean] = {
	  		// ABSOLUTE ADDRESS:
	        if (adrStr.head == 'a') {
	  		    val adr = adrStr.drop(1)
	  		    Comp.getBoolNib(adr.toInt)
	  		} 
	        // VARIABLE:
	  		else if (adrStr.head == 'v') {
	  			val adrInt = getAddress(adrStr)
	  		    Comp.getBoolNib(adrInt)
	  		}
	        // INT VALUE:
	        else if (adrStr.head.isDigit) {
	  		    val intVal = adrStr.toInt
	  		    if ( intVal < 0 || intVal > 255) {
	  		        println("ERROR 5: Integer value out of bounds")
	  		        exit(5)
	  		    }
	  		    val adrInt = getAddress(adrStr)
	  		    writeToAddress(adrInt, Comp.getBool(intVal))
	  		    Comp.getBoolNib(adrInt)
	  		}
	        else {
	            println("ERROR 4: Wrong address")
	            exit(4)
	        }
	    }
	}
	
	object Commands {
	    def get(comStr: String): Array[Boolean] = {
	        for (com <- commandList) {
	            if (com.name == comStr) {
	                return Comp.getBoolNib(com.id)
	            }
	        }
	        print("ERROR 01: wrong command.")
	        exit(1)
	    }
	    
	    val commandList = Array(
	        new Command("READ", 0),
			new Command("WRITE", 1),
			new Command("ADD", 2),
			new Command("MINUS", 3),
			//new Command("MULTIPLY", 5),
			//new Command("DIVIDE", 6),
			new Command("JUMP", 4),
			new Command("POINT", 5),
			new Command("BIGGER", 6),
			new Command("SMALLER", 7)
		)	
		
	    class Command(val name: String, val id: Int) {}
	}

}

