package si.gto76.comp

import scala.io.Source
import sys.exit
import scala.io.BufferedSource

object Assembler {

  val MEMORY_LENGTH = 15
  val MEMORY_WIDTH = 8

  val TEST_FILE = "/fibbAsmb"
  val TEST_URL = getClass.getResource(TEST_FILE)
  val TEST = scala.io.Source.fromURL(TEST_URL, "UTF-8")

  var binaryCode = getEmptyMemory

  def assemble(args: Array[String]) {
    val file: BufferedSource = getBufferredFile(args)
    val assemblyStatements: Array[String] = readAssemblyFromFile(file);
    var i = 0
    for (line <- assemblyStatements) {
      val tokens = line.split(' ')
      val instructionCode = Commands.get(tokens(0))
      val addressCode = Addresses.get(tokens(1))
      val sentenceCode = instructionCode ++ addressCode
      binaryCode(i) = sentenceCode
      i = i + 1
    }
    println("\nBinary code:")
    print(Util.getString(binaryCode))
    if (Addresses.getNumOfAdresses + i > MEMORY_LENGTH) {
      println("OUT OF MEMORY")
      exit(5)
    }
  }
  
  def getBufferredFile(args: Array[String]): BufferedSource = {
    if (args.length > 0) {
      val filename = args(0)
      val fileDoesntExist = !(new java.io.File(filename).exists())
      if (fileDoesntExist) {
        println("Input file " + filename + " doesn't exist.")
        exit
      }
      scala.io.Source.fromFile(filename, "UTF-8")
    } else {
      TEST
    }
  }

  def getEmptyMemory() = {
    var mem = new Array[Array[Boolean]](MEMORY_LENGTH)
    for (i <- 0 to MEMORY_LENGTH - 1) {
      mem(i) = new Array[Boolean](MEMORY_WIDTH)
    }
    mem
  }

  def writeToAddress(address: Int, value: Array[Boolean]) = {
    binaryCode(address) = value
  }

  def readAssemblyFromFile(file: BufferedSource): Array[String] = {
    var data = collection.mutable.ListBuffer[String]()
    println("Assembly code:")
    for (line <- file.getLines()) {
      println(line)
      data += line
    }
    data.toArray
  }

  object Addresses {

    private val addresses = collection.mutable.Map[String, Int]()

    private def getAddress(name: String): Int = {
      val adr = addresses.get(name)
      if (adr.isEmpty) {
        val numOfVars = addresses.size
        val address = (MEMORY_LENGTH - 1) - numOfVars
        addresses += (name -> address)
        address
      } else {
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
        Util.getBoolNib(adr.toInt)
      } // VARIABLE:
      else if (adrStr.head == 'v') {
        val adrInt = getAddress(adrStr)
        Util.getBoolNib(adrInt)
      } // INT VALUE:
      else if (adrStr.head.isDigit) {
        val intVal = adrStr.toInt
        if (intVal < 0 || intVal > 255) {
          println("ERROR 5: Integer value out of bounds")
          exit(5)
        }
        val adrInt = getAddress(adrStr)
        writeToAddress(adrInt, Util.getBool(intVal))
        Util.getBoolNib(adrInt)
      } else {
        println("ERROR 4: Wrong address")
        exit(4)
      }
    }
  }

  object Commands {
    def get(comStr: String): Array[Boolean] = {
      for (com <- commandList) {
        if (com.name == comStr) {
          return Util.getBoolNib(com.id)
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
      new Command("JUMP", 4),
      new Command("POINT", 5),
      new Command("BIGGER", 6),
      new Command("SMALLER", 7))

    class Command(val name: String, val id: Int) {}
  }

}

