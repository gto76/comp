package si.gto76.comp

import scala.io.Source
import scala.collection.mutable.Map
import sys.exit
import scala.io.BufferedSource

object Comp {

  val TEST_FILE = "/fibbBin"
  val TEST_URL = getClass.getResource(TEST_FILE)
  val TEST: BufferedSource = scala.io.Source.fromURL(TEST_URL, "UTF-8")
  
  val DRAWING_FILE = "/drawing"
  val DRAWING_URL = getClass.getResource(DRAWING_FILE)
  val DRAWING = scala.io.Source.fromURL(DRAWING_URL, "UTF-8").mkString
  
  val RAM_SIZE = 15
  val ROWS = 25

  var debug = true
  var auto = true
  var fq = 300

  var ramValues: Array[Array[Boolean]] = null
  var ram: Ram = null
  var proc: Proc = null

  var output = ""

  def main(args: Array[String]): Unit = {
    val cmd = Cli.getCommandLine(args)
    val (help, wait, onlyOutput, assemble, speed) = Cli.setParameters(cmd)
    if (help) { Cli.printHelp; return }
    auto = !wait
    debug = !onlyOutput
    fq = speed
    val arguments = cmd.getArgs()
    
    if (assemble) {
      Assembler.assemble(arguments)
      exit
    }

    val file = getBufferedFile(arguments)
    ramValues = Util.readRamFromFile(file)
    ram = new Ram(ramValues)
    proc = new Proc()
    proc.exec()
  }
  
  def getBufferedFile(arguments: Array[String]): BufferedSource = {
    if (arguments.length > 0) {
      val filename = arguments(0)
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

  ////////////////
  ///// RAM //////
  ////////////////
  
  // adr 15 is io
  // val names: Array[Array[String]] = new Array[Array[String]](10, 10)
  class Ram(var state: Array[Array[Boolean]] /* Boolean[RAM_SIZE][8]*/ ) {
    def get(adr: Array[Boolean]): Array[Boolean] = {
      if (Util.getInt(adr) < RAM_SIZE)
        state(Util.getInt(adr))
      else {
        print("ERROR 01")
        exit(1)
      }
    }
    def set(adr: Array[Boolean], data: Array[Boolean]) {
      if (Util.getInt(adr) != RAM_SIZE)
        state(Util.getInt(adr)) = data
      else {
        val outputLine = Util.getString(data) + " " + "%3d".format(Util.getInt(data)) + "\n"
        output = output + outputLine
        if (!debug) {
          print(outputLine)
        }
      }
    }
    def getStr() = Util.getString(state)
  }

  ////////////////
  ///// CPU //////
  ////////////////
  
  class Proc {
    var reg = Array(false, false, false, false, false, false, false, false)
    var pc = Array(false, false, false, false)
    var cycle = 0

    var printerOutput = ""
    
    // INSTRUCTIONS:

    def read(adr: Array[Boolean]) {
      reg = ram.get(adr)
      pc = Util.getBoolNib(Util.getInt(pc) + 1)
    }
    def write(adr: Array[Boolean]) {
      ram.set(adr, reg)
      pc = Util.getBoolNib(Util.getInt(pc) + 1)
    }
    def add(adr: Array[Boolean]) {
      reg = Util.getBool(Util.getInt(reg) + Util.getInt(ram.get(adr)))
      pc = Util.getBoolNib(Util.getInt(pc) + 1)
    }
    def subtract(adr: Array[Boolean]) {
      reg = Util.getBool(Util.getInt(reg) - Util.getInt(ram.get(adr)))
      pc = Util.getBoolNib(Util.getInt(pc) + 1)
    }
    def jump(adr: Array[Boolean]) {
      pc = adr
    }
    def readPointer(adr: Array[Boolean]) {
      reg = ram.get(Util.getSecondNibble(reg))
      pc = Util.getBoolNib(Util.getInt(pc) + 1)
    }
    def jumpIf(adr: Array[Boolean]) {
      if (Util.getInt(reg) >= 127)
        pc = adr
      else
        pc = Util.getBoolNib(Util.getInt(pc) + 1)
    }
    def jumpIfSmaller(adr: Array[Boolean]) {
      if (Util.getInt(reg) < 127)
        pc = adr
      else
        pc = Util.getBoolNib(Util.getInt(pc) + 1)
    }
    
    // MAIN LOOP:

    def exec() {
      if (Util.getInt(pc) >= RAM_SIZE) {
        exit(0)
      }
      val tmp = ram.get(pc)
      val inst = Util.getFirstNibble(tmp)
      val adr = Util.getSecondNibble(tmp)
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
          //readLine()
          scala.io.StdIn.readLine()
        }
      }
      Util.getInt(inst) match {
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

    // FANCY OUTPUT OF THE STATE:

    def renderState(inst: Array[Boolean], adr: Array[Boolean]): String = {
      val sb = new StringBuilder()
      setPrinterOutput()
      val switchIndex: Map[Char, Int] = Map()

      for (line <- DRAWING.split('\n')) {
        val processedLine = insertActualValues(line, switchIndex)
        sb.append(processedLine + "\n")
      }
      sb.deleteCharAt(sb.size - 1)
      sb.toString()
    }

    def setPrinterOutput() = {
      var outputLines = output.split("\n").reverse
      if (output.length <= 0) {
        printerOutput = "|0|______________|0|"
      } else {
        outputLines = outputLines.map(line => "|0| " + line + " |0|")
        outputLines = outputLines :+ "|0|______________|0|"
        printerOutput = outputLines.mkString("")
      }
    }

    def insertActualValues(line: String, switchIndex: Map[Char, Int]): String = {
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

    def getLightbulb(c: Char, switchIndex: Map[Char, Int]): Char = {
      val i = if (switchIndex.contains(c)) {
        val i = switchIndex.get(c).get
        switchIndex.put(c, i + 1)
        i
      } else {
        switchIndex.put(c, 1)
        0
      }

      val patRam = "[0-9a-e]".r
      c.toString() match {
        case "p" => Util.getChar(pcIsPointingToAddress(i))
        case "s" => Util.getChar(instructionIsPointingToAddress(i))
        case "r" => Util.getChar(reg(i))
        case "i" => Util.getChar(instructionHasId(i))
        case "o" => getFormattedOutput(i)
        case patRam => getRam(c, i)
      }
    }

    def pcIsPointingToAddress(addr: Int): Boolean = {
      Util.getInt(pc) == addr
    }

    def instructionIsPointingToAddress(addr: Int): Boolean = {
      Util.getInt(Util.getSecondNibble(ram.get(pc))) == addr
    }

    def instructionHasId(id: Int): Boolean = {
      Util.getInt(Util.getFirstNibble(ram.get(pc))) == id
    }

    def getFormattedOutput(i: Int): Char = {
      if (printerOutput.length <= i)
        ' '
      else
        printerOutput.charAt(i)
    }

    def getRam(c: Char, i: Int): Char = {
      val j = Util.hexToInt(c)
      val ramLines = ram.getStr.split("\n")
      ramLines(j)(i)
    }
  }
  
  def error(e: Int) {
    exit(e)
  }
}