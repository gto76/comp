package si.gto76.comp

import scala.io.BufferedSource
import scala.language.postfixOps

object Util {

  def readRamFromFile(source: BufferedSource): Array[Array[Boolean]] = {
    var data = new Array[Array[Boolean]](Comp.RAM_SIZE) 
    var i = 0
    for (line <- source.getLines()) {
      data(i) = getBool(line)
      i = i + 1
    }
    source.close()
    data
  }
  
  def getChar(b: Boolean) = {
    if (b)
      '*'
    else
      '-'
  }

  def getInt(bIn: Array[Boolean]): Int = {
    val bbb = bIn.reverse
    var sum: Int = 0;
    var i = 0;
    for (b <- bbb) {
      if (b) {
        sum = sum + Math.pow(2, i).asInstanceOf[Int]
      }
      i = i + 1
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
      s = s + getChar(b)
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
        Comp.error(2)
      }
      i = i + 1
    }
    out
  }

  def getInt(str: String): Int = {
    getInt(getBool(str))
  }

  def getBool(numIn: Int): Array[Boolean] = {
    if (numIn > 255) {
      print("ERROR 03")
      Comp.error(3)
    }
    var num = numIn
    //if (num < 0) 
    //  num = 0 // is it necesry?
    val out = new Array[Boolean](8)
    var j = 0
    for (i <- 0 to 7 reverse) {
      val delitelj: Int = Math.pow(2, i).asInstanceOf[Int]
      val rez = num / delitelj
      if (rez > 0) {
        out(j) = true
      } else {
        out(j) = false
      }
      num = num % delitelj
      j = j + 1
    }
    out
  }

  def getBoolNib(numIn: Int): Array[Boolean] = {
    if (numIn > Comp.RAM_SIZE) {
      print("ERROR 01")
      Comp.error(4)
    }
    var num = numIn
    val out = new Array[Boolean](4)
    var j = 0
    for (i <- 0 to 3 reverse) {
      val delitelj = Math.pow(2, i).asInstanceOf[Int]
      val rez = num / delitelj
      if (rez > 0) {
        out(j) = true
      } else {
        out(j) = false
      }
      num = num % delitelj
      j = j + 1
    }
    out
  }

  def getFirstNibble(b: Array[Boolean]): Array[Boolean] = {
    Array(b(0), b(1), b(2), b(3))
  }

  def getSecondNibble(b: Array[Boolean]): Array[Boolean] = {
    Array(b(4), b(5), b(6), b(7))
  }

  def hexToInt(c: Char): Int = { Integer.parseInt(c.toString(), 16) }

}
