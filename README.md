# codesonar-clion

CodeSonar warnings in CLion, from a CodeSonar hub or an exported SARIF file.

The work is done by the [codesonar-lsp] language server: it talks to the
hub, moves warnings along with the code while it is edited and hides
warnings whose code changed since the analysis. This plugin starts the
server with the LSP support of the IDE and adds what LSP has no counterpart
for:

- choose, show, hide and delete the analyses of the hub project
- resolve warnings on the hub as fixed or as false positives
- disable the check of a warning in `codesonar.conf`
- explain warnings in a popup, with the events that lead to them and the
  rules they may violate
- run `codesonar analyze` for the workspace, incrementally, with the hub,
  project, presets and checks of its `codesonar.conf`
- choose the presets and checks of analyses in a dialog
- list the shown warnings in a tool window and open them on the hub
- show the analysis and what is loading in the status bar

The warnings are regular diagnostics of the IDE: they are highlighted in
the editor, explained on hover and listed in the Problems tool window.
`Alt+Enter` on a warning offers its quick fixes, "Show details", "Open
in CodeSonar Hub", for warnings of a hub "Mark as fixed" and "Mark as false
positive", and "Disable check".

It is the counterpart of [codesonar.vim] for CLion.

[codesonar-lsp]: https://github.com/jclsn/codesonar-lsp
[codesonar.vim]: https://github.com/jclsn/codesonar.vim

## Installation

Requirements: CLion 2026.3 or newer and `codesonar-lsp` (`cargo install
--path .` in its repository). Running analyses needs the CodeSonar command
line tools.

Build the plugin and install the ZIP file in `build/distributions` with
**Settings | Plugins | ⚙ | Install Plugin from Disk**:

```sh
./gradlew buildPlugin
```

The build downloads CLion. To build against an installed one instead, pass
its path, or set `CLION_HOME`:

```sh
./gradlew buildPlugin -PclionPath=/opt/clion
```

## Setup

The server is looked up in `$PATH` and in `~/.cargo/bin`. It starts with
the first C or C++ file of a workspace: the closest directory above the
file with a `codesonar.conf`, a `.codesonar` or a `.git`, else the project
directory.

**Settings | Tools | CodeSonar** has the settings of the server. Usually
only the hub credentials are needed:

| Setting             | Description                                                     |
| ------------------- | --------------------------------------------------------------- |
| Server              | Path of `codesonar-lsp`, or its name in `$PATH`.                |
| URL                 | Hub URL, if `codesonar.conf` has no `HUB_ADDRESS`, or to use `https`. |
| User                | Hub user. Empty for anonymous access.                           |
| Password            | Hub password, kept in the password store of the IDE.            |
| Password file       | File with the password instead. Without either, `$CODESONAR_PASSWORD` is used. |
| Project             | Hub project, if `codesonar.conf` names none.                    |
| SARIF file          | SARIF file to show instead of a hub, relative to the workspace root. |
| Show warnings in changed code as hints | They are hidden otherwise, since they are usually fixed. |
| Additional settings | Further settings of the server as a JSON object, e.g. `{"cachedAnalyses": 5, "pathMappings": [{"from": "/builds/app", "to": "/home/me/src/app"}]}`. |

The fields win over the same settings in the JSON. Applying the settings
restarts the server. See the reference of codesonar-lsp for all settings.

The hub, the project and how analyses run come from the `codesonar.conf`
of each workspace: open a file of your project and run **Tools | CodeSonar
| Workspace Configuration**. It asks for the hub address, the hub project
and the build commands and writes them into the file.

## Actions

The actions are in **Tools | CodeSonar** and in the context menu of editors
with a running server. They have no shortcuts; assign some in **Settings |
Keymap**. The actions need a running server, so use them with a file of the
workspace open.

| Action                   | codesonar.vim           | Description |
| ------------------------ | ----------------------- | ----------- |
| Analyses...              | `:CodeSonarAnalyses`    | List the analyses of the hub project, newest first. The shown one is marked with `*`, one that is loading with `~`, downloaded ones with `[cached]`. `Enter` or a double click shows an analysis, `Space` shows or hides one and keeps the list open, `x` deletes its local copy, `d` deletes it from the hub after a confirmation. |
| Load Configured Analysis | `:CodeSonarLoad`        | Show the configured analysis. |
| Load SARIF File...       | `:CodeSonarLoad file`   | Show an exported SARIF file. |
| Refresh                  | `:CodeSonarRefresh`     | Load the shown analysis again. |
| Hide Analysis            | `:CodeSonarHide`        | Turn the shown analysis off until an analysis is shown again. |
| Explain Warning          | `:CodeSonarExplain`     | Explain the warnings in the line of the caret in a popup, like the "Show details" quick fix: the message, the events that lead to the warning and the rules it may violate. The places of events are links into the code. `j` `k` `Ctrl+D` `Ctrl+U` `g` `G` scroll, `o` opens the warning on the hub, `d` the documentation, `q` or `Esc` closes. |
| Open in Hub              | `:CodeSonarOpenHub`     | Open the warning at the caret on the hub, or else the shown analysis. |
| Mark as Fixed            | `:CodeSonarResolve fixed` | Resolve the warnings in the line of the caret on the hub, like the quick fix: their state becomes Fixed. The hub keeps it for later analyses, and resolved warnings are not shown any more. Your hub user needs the permission to change warnings. |
| Mark as False Positive   | `:CodeSonarResolve false-positive` | Resolve them as wrong: their finding becomes False Positive. |
| Disable Check            | "Disable check" code action | Turn off the check of the warning in the line of the caret, like the quick fix: the check is disabled in the `codesonar.conf` of the workspace for later analyses, and its warnings are hidden now. With several warnings in the line, a list asks for the check. **Checks...** turns it on again. |
| Show Warnings            | `:CodeSonarDiagnostics` | List the shown warnings in the CodeSonar tool window. `Enter` or a double click goes to a warning. The list is updated when another analysis is shown. |
| Status                   | `:CodeSonarStatus`      | Show the analysis with its page on the hub, how many of its warnings are unchanged, moved, changed, unverified and resolved, and the hub and presets of new analyses. `o` opens the analysis on the hub. A click on the status bar widget shows it as well. |
| Analyze                  | `:CodeSonarAnalyze`     | Run `codesonar analyze` for the workspace in the Run tool window. When it succeeds, the new analysis is shown. |
| Analyze As...            | `:CodeSonarAnalyze name` | Run an analysis with a name on the hub. |
| Workspace Configuration  | `:CodeSonarConf`        | Open the `codesonar.conf` of the workspace. Without it, or without the section of codesonar-lsp in it, a form asks for the basic settings first. |
| Presets...               | `:CodeSonarPresets`     | Tick the CodeSonar presets of analyses, e.g. `misrac2025`. The field filters them while typing. |
| Checks...                | `:CodeSonarChecks`      | Tick the checks (warning classes) of analyses. Checks that differ from CodeSonar's default are marked "(changed)". |
| Connect to Hub...        | `:CodeSonarConnect`     | Sign in to a hub and choose one of its projects. Without a URL, the configured hub is used. The hub, the user and the password are kept in the settings, unless "Remember" is unticked. |
| Projects...              | `:CodeSonarProjects`    | Choose another project of the hub. |

What differs from codesonar.vim:

- The progress of downloads is shown by the IDE as a background task; the
  status bar widget shows the analysis. It can be turned off in the context
  menu of the status bar.
- Hub links open in the browser of **Settings | Tools | Web Browsers and
  Preview**, so there is no `browser` setting.
- Other plugins can subscribe to `CodeSonarListener.TOPIC` on the message
  bus of the project instead of the `CodeSonarLoaded` event.

## Development

```sh
./gradlew test          # add -PclionPath=/opt/clion for an installed CLion
./gradlew runIde        # starts CLion with the plugin
```

The tests of the protocol run against `codesonar-lsp` in `$CODESONAR_LSP`,
or the one in `$PATH`, and are skipped without it:

```sh
CODESONAR_LSP=~/.cargo/bin/codesonar-lsp ./gradlew test
```
