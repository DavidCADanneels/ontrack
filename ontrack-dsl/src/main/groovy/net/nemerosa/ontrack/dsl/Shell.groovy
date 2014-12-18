package net.nemerosa.ontrack.dsl

import org.kohsuke.args4j.CmdLineException
import org.kohsuke.args4j.CmdLineParser

class Shell {

    private final PrintWriter output

    Shell(PrintWriter output) {
        this.output = output
    }

    Shell() {
        this(new PrintWriter(System.out))
    }

    Shell withOutput(Writer writer) {
        new Shell(new PrintWriter(writer))
    }

    def call(String... args) {
        call(args as List)
    }

    def call(List<String> args) {
        // Parsing of options
        def options = new ShellOptions()
        new CmdLineParser(options).parseArgument(args)
        // Call
        call options
    }

    def call(ShellOptions options) {

        // Creates the Ontrack connector
        def connection = OntrackConnection.create(options.url).disableSsl(options.disableSsl)
        if (options.user) {
            connection = connection.authenticate(options.user, options.password)
        }
        def ontrack = connection.build()

        // Creates the local shell connector
        def local = new ShellConnector()

        // Reads the script
        def script = new File(options.path).text

        // Parsing of values
        def params = options.values.collectEntries { token ->
            List<String> tokens = token.split(/=/).collect { it.trim() }
            if (tokens.size() != 2) {
                throw new CmdLineException("Wrong value: ${token}")
            } else {
                return [tokens[0], tokens[1]]
            }
        }

        // Runs the shell
        run ontrack, local, script, params

        // Output values
        local.values.each { k, v ->
            output.format('%s=%s%n', k, v)
        }

    }

    static def run(Ontrack ontrack, ShellConnector connector, String script, Map<String, String> params) {
        // Binding
        Map<String, ?> bindingMap = [
                ontrack: ontrack,
                shell  : connector
        ] + params
        def binding = new Binding(bindingMap);

        // Shell
        def gshell = new GroovyShell(binding)

        // Running the script
        gshell.evaluate(script)
    }

    static Shell create() {
        new Shell()
    }

    static void main(String... args) {
        create()(args)
    }

}
