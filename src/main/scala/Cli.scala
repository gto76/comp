import org.apache.commons.cli._

/**
 * Created by minerva on 28.8.2014.
 */
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
    //addOptionWithArg(options, 'f', "filename", "Specify filename containing initial state of the ram.")
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
   * PRINT HELP
   */

  def printHelp() {
    val formatter = new HelpFormatter()
    formatter.printHelp("wolfram-cells", Cli.getOptions)
  }

}