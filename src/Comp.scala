import scala.io.Source

object Comp {
  
	val TEST_FILE = "/home/minerva/code/scala/Comp/src/ramIn"
	val DEBUG = false
	  
	val ramValues = readRamFromFile(TEST_FILE)
  	val ram = new Ram(ramValues)
  	val proc = new Proc()
	
	def main(args: Array[String]): Unit = {
	  	proc.exec()
	}
		
	// adr 15 is io
	// val names: Array[Array[String]] = new Array[Array[String]](10, 10)
	class Ram(var state: Array[Array[Boolean]]/* Boolean[15][8]*/) {
		def get(adr: Array[Boolean]): Array[Boolean] = {
			if (getInt(adr) < 15)
				state(getInt(adr))
			else 
				exit(1)
		}
		def set(adr: Array[Boolean], data: Array[Boolean]) {
			if (getInt(adr) != 15)
				state(getInt(adr)) = data
			else 
				println(getString(data))
		}
		def getStr() = getString(state)				
	}
	
	// TODO unmutable
	class Proc {
		var reg = Array( false, false, false, false, false, false, false, false )
		var pc = Array( false, false, false, false )
	
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
		def jumpIfZero(adr: Array[Boolean]) {
			if (getInt(reg) == 0)
				pc = adr
			else
				pc = getBoolNib(getInt(pc)+1)
		}
	
		def exec() {
			if (getInt(pc) >= 15) exit(0)
			if (DEBUG) {
			  println("RAM: " + ram.getStr)
			  println("CPU: " + this.getStr)
			}
			val tmp = ram.get(pc)
			val ukaz = getFirstNibble(tmp)
			val adr = getSecondNibble(tmp)
			if (DEBUG) {
			  println("tmp: "+getString(tmp))
			  println("ukaz: "+getString(ukaz) + " int: " + getInt(ukaz))
			  println("adr: "+getString(adr)+ " int: " + getInt(adr))
			}
			getInt(ukaz) match {
				case 0 => read(adr)
				case 1 => write(adr)
				case 2 => add(adr)
				case 3 => subtract(adr)
				case 4 => jump(adr)
				case 5 => readPointer(adr)
				case 6 => jumpIfZero(adr)
			}
			exec()
		}
		
		def getStr() = {
		  "Reg: " + getString(reg) + " Pc: " + getString(pc)
		}
	}
	
	/*
	 * UTILS:
	 */
	def readRamFromFile(filename: String): Array[Array[Boolean]] = {
		val source = scala.io.Source.fromFile(filename)
		//val lines = source.mkString
				
		var data = new Array[Array[Boolean]](15) //(15, 8)
		var i = 0
		for(line <- source.getLines()) {
	  		data(i) = getBool(line)
	  		i = i+1
	  	}
		if (DEBUG) {
		  print("RAM INIT: " + getString(data))
		}
		source.close()
	  	data
	}
	
	def getInt(bIn: Array[Boolean]): Int = {
		val bbb = bIn.reverse //TODO
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
		sss
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
	
	def getBool(str: String): Array[Boolean] = {
		val out = new Array[Boolean](8)
		var i = 0
		for (c <- str.toCharArray()) {
			if (c == '-')
				out(i) = false
			else if (c == '*')	
				out(i) = true
			else {
				print("Input Error - Unrecognized char")
				exit(2)
			}	
			i = i+1
		}
		out
	}
	
	def getBool(numIn: Int): Array[Boolean] = {
		if (numIn > 255) exit(3)
		var num = numIn
		val out = new Array[Boolean](8)
		var j = 0
		for (i <- 7 to 0) {
			val rez = num / Math.pow(2, i)
			if ( rez > 0 ) {
				out(j) = true
			}
			else {
				out(j) = false
			}
			num = num % Math.pow(2, i).asInstanceOf[Int]
			j = j+1
		}
		out
	}
	
	def getBoolNib(numIn: Int): Array[Boolean] = {
		if (numIn > 15) exit(3)
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
		//println("getBoolNib:: in: " + numIn + " out: " + getString(out))
		out
	}
	
	def getFirstNibble(b: Array[Boolean]): Array[Boolean] = {
		Array( b(0), b(1), b(2), b(3) )
	}
	
	def getSecondNibble(b: Array[Boolean]): Array[Boolean] = {
		Array( b(4), b(5), b(6), b(7) )
	}
	
}

