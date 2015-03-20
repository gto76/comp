import scala.io.Source
import scala.collection.mutable.Map
import sys.exit

object Comp {
  
	val TEST_FILE = "data/fibbBin"
	val DRAWING_FILE = "data/asciiDrawingOfComputer"
	
	val DRAWING = scala.io.Source.fromFile(DRAWING_FILE, "UTF-8").mkString
  val RAM_SIZE = 15
  
  val ROWS = 25

	var debug = true
	var auto = true
  var fq = 300
  
  //only class can have undefined values, so I defined them, although they are reset in main.
	var ramValues = readRamFromFile(TEST_FILE)
	var ram = new Ram(ramValues)
	var proc = new Proc()

	var output = ""
	
	def main(args: Array[String]): Unit = {
    val cmd = Cli.getCommandLine(args)
    val  (help, wait, onlyOutput, speed) = Cli.setParameters(cmd)
    if (help) { Cli.printHelp; return }
    auto = !wait
    debug = !onlyOutput
    fq = speed
    
		val arguments = cmd.getArgs()
		val file: String = if (arguments.length > 0) arguments(0) else TEST_FILE

    val fileDoesntExist = !(new java.io.File(file).exists())
    if (fileDoesntExist) {
      println("Input file "+file+" doesn't exist.")
      exit
    }
    
		ramValues = readRamFromFile(file)
		ram = new Ram(ramValues)
		proc = new Proc()

		proc.exec()
	}
		
	// adr 15 is io
	// val names: Array[Array[String]] = new Array[Array[String]](10, 10)
	class Ram(var state: Array[Array[Boolean]]/* Boolean[RAM_SIZE][8]*/) {
		def get(adr: Array[Boolean]): Array[Boolean] = {
			if (getInt(adr) < RAM_SIZE)
				state(getInt(adr))
			else {
				print("ERROR 01")
				exit(1)
			}
		}
		def set(adr: Array[Boolean], data: Array[Boolean]) {
			if (getInt(adr) != RAM_SIZE)
				state(getInt(adr)) = data
			else {
				val outputLine = getString(data) + " " + "%3d".format(getInt(data)) + "\n"
				output = output + outputLine
				if (!debug) {
					print(outputLine)
				}
			}
		}
		def getStr() = getString(state)				
	}
	
	class Proc {
		var reg = Array( false, false, false, false, false, false, false, false )
		var pc = Array( false, false, false, false )
    var cycle = 0
    
    var printerOutput = ""
	
		def read(adr: Array[Boolean]) {
			reg = ram.get(adr)
			pc = getBoolNib(getInt(pc)+1)
		}
		def write(adr: Array[Boolean]) {
			ram.set(adr, reg)
			pc = getBoolNib(getInt(pc)+1)
		}
		def add(adr: Array[Boolean]) {
			reg = getBool( getInt(reg) + getInt(ram.get(adr)) )
	 		pc = getBoolNib(getInt(pc)+1)
		}
		def subtract(adr: Array[Boolean]) {
			reg = getBool( getInt(reg) - getInt(ram.get(adr)) )
			pc = getBoolNib(getInt(pc)+1)
		}
		def jump(adr: Array[Boolean]) {
			pc = adr
		}
		def readPointer(adr: Array[Boolean]) {
			reg = ram.get(getSecondNibble(reg))
			pc = getBoolNib(getInt(pc)+1)
		}
		def jumpIf(adr: Array[Boolean]) {
			if (getInt(reg) >= 127)
				pc = adr
			else
				pc = getBoolNib(getInt(pc)+1)
		}
		def jumpIfSmaller(adr: Array[Boolean]) {
			if (getInt(reg) < 127)
				pc = adr
			else
				pc = getBoolNib(getInt(pc)+1)
		}
	
		def exec() {
			if (getInt(pc) >= RAM_SIZE) {
			  exit(0)
			}
			val tmp = ram.get(pc)
			val inst = getFirstNibble(tmp)
			val adr = getSecondNibble(tmp)
			if (debug) {
				val out: String = renderState(inst, adr)
        for (i <- out.split("\n").length to ROWS)
          println()
				print(out)
        if (auto && cycle != 0) {
          Thread sleep fq 
        } else {
          if (cycle == 0)
            print("Press Enter to begin execution")
				  readLine()
        }
			}
			getInt(inst) match {
				case 0 => read(adr)
				case 1 => write(adr)
				case 2 => add(adr)
				case 3 => subtract(adr)
				case 4 => jump(adr)
				case 5 => readPointer(adr)
				case 6 => jumpIf(adr)
				case 7 => jumpIfSmaller(adr)
			}
      cycle = cycle + 1
			exec()
		}		
		
    // FANCY OUTPUT:
    
		def renderState(inst: Array[Boolean], adr: Array[Boolean]): String = {
			val sb = new StringBuilder()
			setPrinterOutput()
			val switchIndex: Map[Char,Int] = Map()
			
			for (line <- DRAWING.split('\n')) {
				val processedLine = insertActualValues(line, switchIndex)
				sb.append(processedLine+"\n")			
			}
      sb.deleteCharAt(sb.size-1)
      sb.toString()
    }
    
    def setPrinterOutput() = {
      var outputLines = output.split("\n").reverse
      if (output.length <= 0) {
        printerOutput = "|0|______________|0|"
      } else {
        outputLines = outputLines.map(line => "|0| "+line+" |0|")
        outputLines = outputLines :+ "|0|______________|0|"
        printerOutput = outputLines.mkString("")
      }
    }
    
    def insertActualValues(line: String, switchIndex: Map[Char,Int]): String = {
      val sb = new StringBuilder()
      for (c <- line) {
        val cOut = if ("[0-9a-z]".r.findAllIn(c.toString).length != 1) {
          c
        } else {
          getLightbulb(c, switchIndex)
        }
        sb.append(cOut)
      }
      sb.toString
    }
    
    def getLightbulb(c: Char, switchIndex: Map[Char,Int]): Char = {
      val i = if (switchIndex.contains(c)) {
          val i = switchIndex.get(c).get
          switchIndex.put(c, i+1)
          i
        } else {
          switchIndex.put(c, 1)
          0
        }
      
      val patRam = "[0-9a-e]".r 
      c.toString() match {
        case "p" => getChar(pcIsPointingToAddress(i))
        case "s" => getChar(instructionIsPointingToAddress(i))
        case "r" => getChar(reg(i))
        case "i" => getChar(instructionHasId(i))
        case "o" => getFormattedOutput(i)
        case patRam => getRam(c, i)
      }
    }
    
    def pcIsPointingToAddress(addr: Int): Boolean = {
      getInt(pc) == addr
    }
        
    def instructionIsPointingToAddress(addr: Int): Boolean = {
      getInt(getSecondNibble(ram.get(pc))) == addr
    }
    
    def instructionHasId(id: Int): Boolean = {
      getInt(getFirstNibble(ram.get(pc))) == id
    }
    
    def getFormattedOutput(i: Int): Char = {
      if (printerOutput.length <= i)
        ' '
      else
        printerOutput.charAt(i)
    }
    
    def getRam(c: Char, i: Int): Char = {
      val j = hexToInt(c)
      val ramLines = ram.getStr.split("\n")
      ramLines(j)(i)
    }
	}

  /*
	 * UTILS:
	 */
	def readRamFromFile(filename: String): Array[Array[Boolean]] = {
    val source = Source.fromURL(getClass.getResource(filename))
				
		var data = new Array[Array[Boolean]](RAM_SIZE) //(RAM_SIZE, 8)
		var i = 0
		for (line <- source.getLines()) {
	  		data(i) = getBool(line)
	  		i = i+1
	  	}
		source.close()
	  	data
	}
	
	def getInt(bIn: Array[Boolean]): Int = {
		val bbb = bIn.reverse
		var sum: Int = 0;
		var i = 0;
		for (b <- bbb) {
			if ( b ) {
				sum = sum + Math.pow(2, i).asInstanceOf[Int]
			}
			i = i+1
		}
		sum
	}
	
	
	def getString(bbb: Array[Array[Boolean]]): String = {
		var sss = ""
		for (b <- bbb) {
			sss = sss + "\n" + getString(b)
		}
		sss.drop(1)
	}
	
	def getString(bbb: Array[Boolean]): String = {
		var s = ""
		var sum = 0;
		for (b <- bbb) {
			if ( b ) 
				s = s + "*"
			else
				s = s + "-"
		}
		s
	}
	
	def getInstructionName(inst: Array[Boolean]): String = {
		getInt(inst) match {
			case 0 => "READ"
			case 1 => "WRITE"
			case 2 => "ADD"
			case 3 => "SUBTRACT"
			case 4 => "JUMP"
			case 5 => "POINT"
			case 6 => "BIGGER"
			case 7 => "SMALLER"
		}
	}
	
	def getChar(b: Boolean) = {
		if (b) 
			'*'
		else
			'-'
	}
	
	def getBool(str: String): Array[Boolean] = {
		val out = new Array[Boolean](8)
		var i = 0
		for (c <- str.toCharArray()) {
			if (c == '-')
				out(i) = false
			else if (c == '*')	
				out(i) = true
			else {
				print("Input Error 02 - Unrecognized char")
				exit(2)
			}	
			i = i+1
		}
		out
	}
  
  def getInt(str: String): Int = {
    getInt(getBool(str))
  }
	
	def getBool(numIn: Int): Array[Boolean] = {
		if (numIn > 255) {
			print("ERROR 03")
			exit(3)
		}
		var num = numIn
		//if (num < 0) 
		//  num = 0
		val out = new Array[Boolean](8)
		var j = 0
		for (i <- 0 to 7 reverse) {
			val delitelj: Int = Math.pow(2, i).asInstanceOf[Int]
			val rez = num / delitelj
			if ( rez > 0 ) {
				out(j) = true
			}
			else {
				out(j) = false
			}
			num = num % delitelj
			j = j+1
		}
		out
	}
	
	def getBoolNib(numIn: Int): Array[Boolean] = {
		if (numIn > RAM_SIZE) { 
			print("ERROR 01")
			exit(4)
		}
		var num = numIn
		val out = new Array[Boolean](4)
		var j = 0
		for (i <- 0 to 3 reverse) {
			val delitelj = Math.pow(2, i).asInstanceOf[Int]
			val rez = num / delitelj
			if ( rez > 0 ) {
				out(j) = true
			}
			else {
				out(j) = false
			}
			num = num % delitelj
			j = j+1
		}
		out
	}
	
	def getFirstNibble(b: Array[Boolean]): Array[Boolean] = {
		Array( b(0), b(1), b(2), b(3) )
	}
	
	def getSecondNibble(b: Array[Boolean]): Array[Boolean] = {
		Array( b(4), b(5), b(6), b(7) )
	}
  
  
  def hexToInt(c: Char): Int = { Integer.parseInt(c.toString(), 16) }
	
}

