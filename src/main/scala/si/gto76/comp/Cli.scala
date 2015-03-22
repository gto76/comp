package si.gto76.comp

import org.apache.commons.cli._

object Cli {

  /*
   * GET COMMAND LINE
   */

  def getCommandLine(args: Array[String]): CommandLine = {
    val options = getOptions
    val parser: CommandLineParser = new GnuParser()
    val cmd: CommandLine = parser.parse(options, args)
    cmd
  }

  private def getOptions: Options = {
    val options = new Options()
    options.addOption("h", "help", false, "Print this message.")
    options.addOption("w", "wait", false, "Wait for enter after every cycle.")
    options.addOption("o", "only-output", false, "Only print output.")
    options.addOption("a", "assemble", false, "Instead of execution it assembles the passed assembly file.")
    addOptionWithArg(options, 's', "speed", "Cycle speed in miliseconds. Default is " + Comp.fq)
    options
  }

  private def addOptionWithArg(options: Options, shortName: Char, longName: String, description: String) {
    OptionBuilder.withLongOpt(longName)
    OptionBuilder.hasArg
    OptionBuilder.withDescription(description)
    val option = OptionBuilder.create(shortName)
    options.addOption(option)
  }

  /*
   * SET PARAMETERS 
   */

  def setParameters(cmd: org.apache.commons.cli.CommandLine): (Boolean, Boolean, Boolean, Boolean, Int) = {
    val help = cmd.hasOption("h")
    val wait = cmd.hasOption("w")
    val onlyOutput = cmd.hasOption("o")
    val assemble = cmd.hasOption("a")
    val speed = if (cmd.hasOption("s")) cmd.getOptionValue("s").toInt else Comp.fq
    (help, wait, onlyOutput, assemble, speed)
  }

  /*
   * PRINT HELP
   */

  def printHelp() {
    val formatter = new HelpFormatter()
    formatter.printHelp("comp [OPTION]... [FILE]", Cli.getOptions)
  }

}

